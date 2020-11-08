package com.coen445.FinalProject;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        //for now, ask a user to register every time
        Scanner scanner = new Scanner(System.in);
        System.out.println("Hello there, please enter a username: ");
        String username = scanner.nextLine();
        System.out.println("Your username is : " + username + "\n" +
                "Please enter a password: ");
        String password = scanner.nextLine();
        System.out.println("Your password is: " + password);

        //code to dynamically get a free port to be assigned to a user
        int portCheck = 5003; //5001/2 are reserved for the servers
        while(!available(portCheck)){
            System.out.println("Port: " + portCheck + " is occupied");
            portCheck++;
        }

        //User user = new User(username, password, "localhost", Integer.toString(portCheck));

        //To make the user we need to get the IP address of the machine its currently running on.
        //TODO: also needs to be updated everytime you boot the client up
        InetAddress clientAddress = InetAddress.getLocalHost();

        User user = new User(username, password,clientAddress.getHostAddress().trim() , Integer.toString(portCheck));

        String line = "";

        //start by trying to connect to server a
        Client client = new Client(ServerInfo.SERVER_A_ADDRESS, ServerInfo.SERVER_A_PORT);
	    client.connectToServer();

	    //todo: if server a rejects the connection (since server b is currently serving), connect to server b

	    while(true){ //wait for the server to connect, then attempt to register

            Object serverResponse = client.readObjectFromServer(); //first message will be to register
            if (serverResponse.toString().equalsIgnoreCase("TOREGISTER")) {
                System.out.println("Registering Client: " + user.getUserName());
                //format registration frame
                String register = "REGISTER " + "1 " + user.getUserName() + " " + user.getIPAddress() + " " + user.getSocketNumber();
                client.sendMessage(register);// second message is one we should send with the desire to register
            }
            if(client.readLine().equalsIgnoreCase("done")){
                break;
            }
            // TODO: 2020-11-04 make the message to be sent automatically a registration frame.
        }

//	    while(true) {
//            System.out.println("Would you like to register? yes/no");
//            line = client.readLine();
//            if (!(line.equalsIgnoreCase("yes") || line.equalsIgnoreCase("no"))) {
//                System.out.println("Invalid input. Please enter valid input.");
//            }else{
//                break;
//            }
//        }
//
//        if(line.equalsIgnoreCase("yes")) {
//            client.registerClient();
//        }

	    client.closeConnections();
    }

    //function obtains from: https://stackoverflow.com/questions/434718/sockets-discover-port-availability-using-java
    //provided by user: David Santamaria on stackoverflow.com
    public static boolean available(int port) {
        if (port < 5001|| port > 65353) {
            throw new IllegalArgumentException("Invalid start port: " + port);
        }

        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }

        return false;
    }
}
