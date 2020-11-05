package com.coen445.FinalProject;

import java.io.IOException;
import java.net.DatagramSocket;
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

        //now we need to dynamically get a port number
        int portCheck = 5001;
        while(!available(portCheck)){
            System.out.println("Port: " + portCheck + " is occupied");
            portCheck++;
        }

        User user = new User(username, password, "localhost", Integer.toString(portCheck));

        String line = "";
	    Client client = new Client("localhost", Integer.parseInt(user.getSocketNumber()));
	    client.connectToServer();

	    while(true){ //wait for the server to connect, then attempt to register

            Object serverResponse = client.readObjectFromServer(); //first message will be to register
            if (serverResponse.toString().equalsIgnoreCase("TOREGISTER")) {
                //format registration frame
                String register = "REGISTER " + "1 " + user.getUserName() + " localhost " + user.getSocketNumber();
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
