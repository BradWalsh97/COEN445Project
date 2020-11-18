package com.coen445.FinalProject;

import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

    //todo about updating if loggin in from new computer
    //hkendek said that the update can server as a login (since you're updating the ip address). If you update from a
    //new computer, you can't know which server is currently serving and thus you'll send it to both
    //if you're updating on the same computer you only send it to the server serving.
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        //for now, ask a user to register every time
        int rq = 1;
        Scanner scanner = new Scanner(System.in);
        InetAddress clientAddress = InetAddress.getLocalHost();
        System.out.println("Hello, lets get some info about the servers you want to connect to. \nWhat is the ip of server a?");
        String serverAIp = scanner.nextLine();
        System.out.println("What about server b's address?");
        String serverBIp = scanner.nextLine();
        String currentUser = "";

        //start by trying to connect to server a
        Client client = new Client(ServerInfo.SERVER_A_ADDRESS, ServerInfo.SERVER_A_PORT); //todo: update this with the proper info
        try {
            client.connectToServer();
        } catch (ConnectException e) {
            if (e.getLocalizedMessage().equals("Connection refused")) {
                System.out.println("The server is currently offline. Please try again in a few minutes. :)");
                return;
            }
        }//todo: if server a rejects the connection (since server b is currently serving), connect to server b


        //uncomment when we're ready to implement the rest of the login stuff
        boolean validChoice = false;
        do {
            System.out.println("\n\n\n\n\nWould you like to login or register? LOGIN/REGISTER");
            String loginOrRegister = scanner.nextLine();

            if (loginOrRegister.equalsIgnoreCase("login")) {
                validChoice = true;
                System.out.println("Please enter your username");
                String username = scanner.nextLine();
                System.out.println("Please enter your password");
                String password = scanner.nextLine();

                //todo use a frame that will call the "checkIfuserExists" method. if it does, call the get user method and check the password


            } else if (loginOrRegister.equalsIgnoreCase("register")) {
                boolean registerSuccess = false;
                validChoice = true;
                do {
                    //todo: make it loop until its correct
                    System.out.println("Please enter a username: ");
                    String username = scanner.nextLine();
                    System.out.println("Your username is : " + username + "\n" + "Please enter a password: ");
                    String password = scanner.nextLine();
                    System.out.println("Your password is: " + password);

                /*
                //code to dynamically get a free port to be assigned to a user


                //To make the user we need to get the IP address of the machine its currently running on.
                //TODO: also needs to be updated everytime you boot the client up

                //User user = new User(username, password, "localhost", Integer.toString(portCheck));
                //User user = new User(username, password,clientAddress.getHostAddress().trim() , Integer.toString(portCheck));

                String line = "";
*/
                    try {
                        //Object serverResponse = client.readObjectFromServer(); //first message will be to register

                        int portCheck = 5003; //5001/2 are reserved for the servers
                        while (!available(portCheck)) {
                            System.out.println("Port: " + portCheck + " is occupied");
                            portCheck++;
                        }
                        //if (serverResponse.toString().equalsIgnoreCase("TOREGISTER")) {
                        RQ registerRQ = new RQ(0, rq++, username, clientAddress.getHostAddress().trim(), portCheck);
                        System.out.println("Registering Client: " + registerRQ.getName());
                        //format registration frame
                        client.sendMessage(registerRQ.getMessage());
                        Object serverResponse = client.readObjectFromServer();
                        //System.out.println(serverResponse);
                        RQ receivedRq = new RQ((byte[]) serverResponse);
                        if (receivedRq.getRegisterCode() == 1) {
                            System.out.println(registerRQ.getName() + " has been registered!");
                            registerSuccess = true;
                        } else if (receivedRq.getRegisterCode() == 2)
                            System.out.println(registerRQ.getName() + " already exists. Please chose another name");


                        //}

                    } catch (IOException e) {
                        //if we get this exception here, that means that the server is no longer reachable for some reason.
                        //if this happens we terminate the connection and tell them to try again later.
                        if (e instanceof EOFException) {
                            System.out.println("The RSS server is currently experiencing an outage. \n" +
                                    "Please try again in a few minutes while we work to restore the service! <3");
                            break;
                        } else
                            e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }


                    // TODO: 2020-11-04 make the message to be sent automatically a registration frame.


                } while (!registerSuccess);
            } else //invalid choice
                System.out.println("Please enter a valid input.");

        } while (!validChoice);


        while (true) {
            System.out.println("Command list: \nTo update your user: UPDATE" +
                    "\nTo delete a user: DE-REGISTER \nTo update your subjects: SUBJECTS \nTo publish a message: PUBLISH" +
                    "\nTo exit: DONE");
            String userCommand = scanner.nextLine().toUpperCase();

            //Clear the console
            clearScreen(); //todo: fix clear screen


            switch (userCommand) {
//                case "REGISTER":
//
//                    break;
                case "UPDATE":
                    //IMPORT ASSUMPTION FOR THE UPDATE: updating port and IP will be done automatically so that the
                    //user cannot mess things up. Futhermore, the prof said that everything wrt to ip and socket (save for the server address)
                    //should be done automatically. This also gives users a better experience.
                    System.out.println("Do you want manually or automatically update your IP and socket? AUTO/MAN ");

                    String newIP = clientAddress.getHostAddress().trim();
                    int updatePort = 5003;
                    while (!available(updatePort)) updatePort++;
                    System.out.println("New IP: " + newIP + ", New port: " + updatePort);
                    //todo: add josephs update thing
                    //todo: make sure its valid (name exists) and tell the user the result
                    //if it fails they go back to the menu and need to try to update again
                    break;

                case "DE-REGISTER":
                    System.out.println("Please enter the name of the user you want to delete: ");
                    String userToDelete = scanner.nextLine();
                    //todo: add Joseph's delete method and do the error checking/status reporting

                    break;
                case "SUBJECTS":
                    System.out.println("Please enter the user for which you want ");
                    System.out.println("Please chose among the following interests. Enter the numbers, separated by commas");
                    System.out.println(Subjects.INTEREST1 + "\n" + Subjects.INTEREST2 + "\n" + Subjects.INTEREST3 + "\n" +
                            Subjects.INTEREST4 + "\n" + Subjects.INTEREST5);
                    String choices = scanner.nextLine();
                    ArrayList<String> interestList = new ArrayList<String>(Arrays.asList(choices.split(",")));
                    //todo: call joseph's thing to update

                    break;
                case "PUBLISH":
                    //todo: the system needs to be implemented such that when the client is booted up
                    //you must either register or update. Once this is done, you will have the username
                    //to do the rest of the stuff. So, if update get the username and see if it exists. If it does
                    //check to see if the database ip and socket are different from what the user currently runs on
                    //if they are different send the auto update, if not
                    System.out.println("Select the subject you want to publish to:");
                    ArrayList<String> userList = new ArrayList<String>();
                    break;

                case "DONE":
                    client.closeConnections();
                    System.out.println("Client disconnected from server. Have a nice day! :)");
                    return;
            }
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

    }


    public static void clearScreen() {
        System.out.println("\\033[H\\033[2J"); //todo: maybe get this working? Or not, doesn't matter honestly
        System.out.flush();
    }


    //function obtains from: https://stackoverflow.com/questions/434718/sockets-discover-port-availability-using-java
    //provided by user: David Santamaria on stackoverflow.com
    public static boolean available(int port) {
        if (port < 5001 || port > 65353) {
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
