package com.coen445.FinalProject;

import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        //for now, ask a user to register every time
        int rq = 1;
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

        //User user = new User(username, password,clientAddress.getHostAddress().trim() , Integer.toString(portCheck));

        String line = "";

        //start by trying to connect to server a
        Client client = new Client(ServerInfo.SERVER_A_ADDRESS, ServerInfo.SERVER_A_PORT);
	    try {client.connectToServer();}
	    catch (ConnectException e){
	        if(e.getLocalizedMessage().equals("Connection refused")) {
                System.out.println("The server is currently offline. Please try again in a few minutes. :)");
                return;
            }
        }

	    //todo: if server a rejects the connection (since server b is currently serving), connect to server b

	    while(true){ //wait for the server to connect, then attempt to register


            try{
                Object serverResponse = client.readObjectFromServer(); //first message will be to register
                if (serverResponse.toString().equalsIgnoreCase("TOREGISTER")) {
                    RQ registerRQ = new RQ(0, rq++, username, clientAddress.getHostAddress().trim(), portCheck);
                    System.out.println("Registering Client: " + registerRQ.getName());
                    //format registration frame
                    //String register = "REGISTER " + "1 " + registerRQ.getName() + " " + registerRQ.getIp() + " " + registerRQ.getSocketNum();
                    //client.sendMessage(register);// second message is one we should send with the desire to register
                    client.sendMessage(registerRQ.getMessage());
                    System.out.println(client.readObjectFromServer());
                }
                if(client.readLine().equalsIgnoreCase("done")){
                    break;
                }
            }
            catch (IOException e){
                //if we get this exception here, that means that the server is no longer reachable for some reason.
                //if this happens we terminate the connection and tell them to try again later.
                if(e instanceof EOFException){
                    System.out.println("The RSS server is currently experiencing an outage. \n" +
                            "Please try again in a few minutes while we work to restore the service! <3");
                    break;
                }
                else
                    e.printStackTrace();
            }
            catch(ClassNotFoundException e){
                e.printStackTrace();
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
