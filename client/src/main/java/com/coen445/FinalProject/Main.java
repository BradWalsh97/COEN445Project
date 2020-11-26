package com.coen445.FinalProject;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static boolean registerSuccess = false;
    public static String username = "";

    //todo about updating if logging in from new computer
    //khendek said that the update can server as a login (since you're updating the ip address). If you update from a
    //new computer, you can't know which server is currently serving and thus you'll send it to both
    //if you're updating on the same computer you only send it to the server serving.
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        //for now, ask a user to register every time
        int rq = 1;
        Scanner scanner = new Scanner(System.in);
        InetAddress clientAddress = InetAddress.getByName("localhost");
        System.out.println("Hello, lets get some info about the servers you want to connect to. \nWhat is the ip of server a?");
        String serverAIp = scanner.nextLine();
        System.out.println("What about server b's address?");
        String serverBIp = scanner.nextLine();
        String currentUser = "";
        boolean validChoice = false;
        Socket socketA = null;
        ServerConnection serverConnectionA = null;
        ObjectOutputStream outputStreamA = null;
        Socket socketB = null;
        ServerConnection serverConnectionB = null;
        ObjectOutputStream outputStreamB = null;
        boolean serverAConnect = false;
        boolean serverBConnect = false;

        try {
            socketA = new Socket(ServerInfo.SERVER_A_ADDRESS, ServerInfo.SERVER_A_PORT);
            serverConnectionA = new ServerConnection(socketA);
            outputStreamA = new ObjectOutputStream(socketA.getOutputStream());
            System.out.println("Connected to server A");
            serverAConnect = true;
            new Thread(serverConnectionA).start();
        } catch (ConnectException e) {
            System.out.println("Server A currently unavailable");
        }
        try {
            socketB = new Socket(ServerInfo.SERVER_B_ADDRESS, ServerInfo.SERVER_B_PORT);
            serverConnectionB = new ServerConnection(socketB);
            outputStreamB = new ObjectOutputStream(socketB.getOutputStream());
            System.out.println("Connected to server B");
            serverBConnect = true;
            new Thread(serverConnectionB).start();

        } catch (Exception e) {
            System.out.println("Server B is unavailable");
        }

        if (!serverAConnect || !serverBConnect) {
            System.out.println("The RSS server is currently experiencing an outage. \n" +
                    "Please try again in a few minutes while we work to restore the service! <3");
            return;
        }

        while (true) {
            do {
                do {
                    System.out.println("\n\nWould you like to login or register? LOGIN/REGISTER");
                    String loginOrRegister = scanner.nextLine();

                    if (loginOrRegister.equalsIgnoreCase("login")) {
                        validChoice = true;
                        System.out.println("Please enter your username");
                        username = scanner.nextLine();
                        System.out.println("Please enter your password");
                        String password = scanner.nextLine();

                        RQ updateRQ = new RQ(7, rq++, username, socketA.getLocalAddress().getHostAddress(), socketA.getLocalPort());
                        System.out.println("Logging in Client: " + updateRQ.getName());
                        outputStreamA.writeObject(updateRQ.getMessage());
                        //outputStreamB.writeObject(updateRQ.getMessage());
                        Thread.sleep(1000);


                    } else if (loginOrRegister.equalsIgnoreCase("register")) {
                        validChoice = true;
                        //do {
                        //todo: make it loop until its correct
                        System.out.println("Please enter a username: ");
                        username = scanner.nextLine();
                        System.out.println("Your username is : " + username + "\n" + "Please enter a password: ");
                        String password = scanner.nextLine();
                        System.out.println("Your password is: " + password);

                        int portCheck = 5003; //5001/2 are reserved for the servers
                        while (!available(portCheck)) {
                            System.out.println("Port: " + portCheck + " is occupied");
                            portCheck++;
                        }
                        RQ registerRQ = new RQ(0, rq++, username, socketA.getLocalAddress().getHostAddress(), socketA.getLocalPort());
                        System.out.println("Registering Client: " + registerRQ.getName());
                        outputStreamA.writeObject(registerRQ.getMessage());
                        outputStreamB.writeObject(registerRQ.getMessage());
                        Thread.sleep(1000);
                        // } while (!registerSuccess);
                    } else //invalid choice
                        System.out.println("Please enter a valid input.");
                } while (!registerSuccess);
            } while (!validChoice);

            while (true) {
                System.out.println("Command list: +" +
                        //"\nTo update your user: UPDATE" +
                        "\nTo delete a user: DE-REGISTER" +
                        "\nTo update your subjects: SUBJECTS " +
                        "\nTo publish a message: PUBLISH" +
                        "\nFor a joke: JOKE" +
                        "\nTo exit: DONE");
                String userCommand = scanner.nextLine().toUpperCase();

                //Clear the console
                //clearScreen(); //todo: fix clear screen


                switch (userCommand) {
//                    case "UPDATE":
//                        //IMPORT ASSUMPTION FOR THE UPDATE: updating port and IP will be done automatically so that the
//                        //user cannot mess things up. Futhermore, the prof said that everything wrt to ip and socketA (save for the server address)
//                        //should be done automatically. This also gives users a better experience.
//                        System.out.println("Do you want manually or automatically update your IP and socketA? AUTO/MAN ");
//
//                        String newIP = clientAddress.getHostAddress().trim();
//                        int updatePort = 5003;
//                        while (!available(updatePort)) updatePort++;
//                        System.out.println("New IP: " + newIP + ", New port: " + updatePort);
//                        //todo: add josephs update thing
//                        //todo: make sure its valid (name exists) and tell the user the result
//                        //if it fails they go back to the menu and need to try to update again
//                        break;

                    case "DE-REGISTER":
                        System.out.println("Are you sure you want to de-register? You will need to recreate an account to " +
                                "continue using this service! (Y/N)");
                        String areYouSureOrNAWH = scanner.nextLine();
                        if (areYouSureOrNAWH.equalsIgnoreCase("y")) {
                            RQ deRegisterRQ = new RQ(5, rq++, username);
                            //client.sendMessage(deRegisterRQ.getMessage());
                            outputStreamA.writeObject(deRegisterRQ.getMessage());
                        }
                        Thread.sleep(1000);
                        //RQ receivedDeRegisterRq = new RQ((byte[]) client.readObjectFromServer());

                        break;
                    case "SUBJECTS":
                        System.out.println("Changing the interests for user " + username);
                        //String user = scanner.nextLine();
                        System.out.println("Please chose among the following interests. Enter the numbers, separated by commas");
                        System.out.println(Subjects.INTEREST1 + "\n" + Subjects.INTEREST2 + "\n" + Subjects.INTEREST3 + "\n" +
                                Subjects.INTEREST4 + "\n" + Subjects.INTEREST5);
                        String choices = scanner.nextLine();

                        //convert the numbers inputted by the user to their associated values
                        ArrayList<String> interestList = new ArrayList<String>();
                        for (String choice : Arrays.asList(choices.split(","))) {
                            switch (choice) {
                                case "1":
                                    interestList.add(Subjects.INTEREST1_FOR_SERVER);
                                    break;
                                case "2":
                                    interestList.add(Subjects.INTEREST2_FOR_SERVER);
                                    break;
                                case "3":
                                    interestList.add(Subjects.INTEREST3_FOR_SERVER);
                                    break;
                                case "4":
                                    interestList.add(Subjects.INTEREST4_FOR_SERVER);
                                    break;
                                case "5":
                                    interestList.add(Subjects.INTEREST5_FOR_SERVER);
                                    break;
                            }
                        }

                        //now send the server the subjects.
                        RQ subjectsRQ = new RQ(10, rq++, username, interestList);
                        //client.sendMessage(subjectsRQ.getMessage());
                        outputStreamA.writeObject(subjectsRQ.getMessage());
                        Thread.sleep(1000);
                        //RQ receivedSubjectsRq = new RQ((byte[]) client.readObjectFromServer());

                        break;
                    case "PUBLISH":
                        System.out.println(socketA.getLocalPort());
                        //todo: the system needs to be implemented such that when the client is booted up
                        //you must either register or update. Once this is done, you will have the username
                        //to do the rest of the stuff. So, if update get the username and see if it exists. If it does
                        //check to see if the database ip and socketA are different from what the user currently runs on
                        //if they are different send the auto update, if not
                        System.out.println("Please enter the user for which you want ");
                        String userPublish = scanner.nextLine(); //todo: set this to the currently logged in user
                        System.out.println("Select the subject you want to publish to:");
                        System.out.println(Subjects.INTEREST1 + "\n" + Subjects.INTEREST2 + "\n" + Subjects.INTEREST3 + "\n" +
                                Subjects.INTEREST4 + "\n" + Subjects.INTEREST5);
                        String publishChoiceNumber = scanner.nextLine();
                        ArrayList<String> userList = new ArrayList<String>();
                        switch (publishChoiceNumber) {
                            case "1":
                                userList.add(Subjects.INTEREST1_FOR_SERVER);
                                break;
                            case "2":
                                userList.add(Subjects.INTEREST2_FOR_SERVER);
                                break;
                            case "3":
                                userList.add(Subjects.INTEREST3_FOR_SERVER);
                                break;
                            case "4":
                                userList.add(Subjects.INTEREST4_FOR_SERVER);
                                break;
                            case "5":
                                userList.add(Subjects.INTEREST5_FOR_SERVER);
                                break;
                        }
                        System.out.println("Type in the message you would like to publish"); //todo: add error checking (no empty messages)
                        String publishedMessage = scanner.nextLine();
                        RQ publishRQ = new RQ(13, rq++, userPublish, userList, publishedMessage);
                        //client.sendMessage(publishRQ.getMessage());
                        outputStreamA.writeObject(publishRQ.getMessage());
                        Thread.sleep(1000);
                        //RQ receivedPublishRq = new RQ((byte[]) client.readObjectFromServer());
                        break;

                    case "DONE":
                        //client.closeConnections();
                        socketA.close();
                        outputStreamA.close();
                        System.out.println("Client disconnected from server. Have a nice day! :)");
                        return;

                    case "JOKE":
                        break;
                }
            }
        }
    }


//        //start by trying to connect to server a
//        //Client client = new Client(ServerInfo.SERVER_A_ADDRESS, ServerInfo.SERVER_A_PORT); //todo: update this with the proper info
//        try {
//            //client.connectToServer();
//            socket = new Socket(ServerInfo.SERVER_A_ADDRESS, ServerInfo.SERVER_A_PORT);
//            outputStream = new ObjectOutputStream(socket.getOutputStream());
//            serverConnection = new ServerConnection(socket);
//            new Thread(serverConnection).start();
//        } catch (ConnectException e) {
//            if (e.getLocalizedMessage().equals("Connection refused")) {
//                System.out.println("The server is currently offline. Please try again in a few minutes. :)");
//                return;
//            }
//        }//todo: if server a rejects the connection (since server b is currently serving), connect to server b
//
//
//        //uncomment when we're ready to implement the rest of the login stuff
//        boolean validChoice = false;
//        do {
//            System.out.println("\n\n\n\n\nWould you like to login or register? LOGIN/REGISTER");
//            String loginOrRegister = scanner.nextLine();
//
//            if (loginOrRegister.equalsIgnoreCase("login")) {
//                validChoice = true;
//                System.out.println("Please enter your username");
//                username = scanner.nextLine();
//                System.out.println("Please enter your password");
//                String password = scanner.nextLine();
//
//                //todo use a frame that will call the "checkIfuserExists" method. if it does, call the get user method and check the password
//
//
//            } else if (loginOrRegister.equalsIgnoreCase("register")) {
//                validChoice = true;
//                do {
//                    //todo: make it loop until its correct
//                    System.out.println("Please enter a username: ");
//                    username = scanner.nextLine();
//                    System.out.println("Your username is : " + username + "\n" + "Please enter a password: ");
//                    String password = scanner.nextLine();
//                    System.out.println("Your password is: " + password);
//
//                /*
//                //code to dynamically get a free port to be assigned to a user
//
//
//                //To make the user we need to get the IP address of the machine its currently running on.
//                //TODO: also needs to be updated everytime you boot the client up
//
//                //User user = new User(username, password, "localhost", Integer.toString(portCheck));
//                //User user = new User(username, password,clientAddress.getHostAddress().trim() , Integer.toString(portCheck));
//
//                String line = "";
//*/
//                    try {
//                        //Object serverResponse = client.readObjectFromServer(); //first message will be to register
//
//                        int portCheck = 5003; //5001/2 are reserved for the servers
//                        while (!available(portCheck)) {
//                            System.out.println("Port: " + portCheck + " is occupied");
//                            portCheck++;
//                        }
//                        //if (serverResponse.toString().equalsIgnoreCase("TOREGISTER")) {
//                        RQ registerRQ = new RQ(0, rq++, username, clientAddress.getHostAddress().trim(), portCheck);
//                        System.out.println("Registering Client: " + registerRQ.getName());
//                        //format registration frame
//                        //client.sendMessage(registerRQ.getMessage());
//                        outputStream.writeObject(registerRQ.getMessage());
//                        //Object serverResponse = client.readObjectFromServer();
//                        //System.out.println(serverResponse);
//                        /*RQ receivedRq = new RQ((byte[]) serverResponse);
//                        if (receivedRq.getRegisterCode() == 1) {
//                            System.out.println(registerRQ.getName() + " has been registered!");
//                            registerSuccess = true;
//                        } else if (receivedRq.getRegisterCode() == 2)
//                            System.out.println(registerRQ.getName() + " already exists. Please chose another name");*/
//
//
//                        //}
//
//                    } catch (IOException e) {
//                        //if we get this exception here, that means that the server is no longer reachable for some reason.
//                        //if this happens we terminate the connection and tell them to try again later.
//                        if (e instanceof EOFException) {
//                            System.out.println("The RSS server is currently experiencing an outage. \n" +
//                                    "Please try again in a few minutes while we work to restore the service! <3");
//                            break;
//                        } else
//                            e.printStackTrace();
//                    }
//
//
//                    // TODO: 2020-11-04 make the message to be sent automatically a registration frame.
//
//
//                } while (!registerSuccess);
//            } else //invalid choice
//                System.out.println("Please enter a valid input.");
//
//        } while (!validChoice);
//
//
//        while (true) {
//            System.out.println("Command list: \nTo update your user: UPDATE" +
//                    "\nTo delete a user: DE-REGISTER \nTo update your subjects: SUBJECTS \nTo publish a message: PUBLISH" +
//                    "\nTo exit: DONE");
//            String userCommand = scanner.nextLine().toUpperCase();
//
//            //Clear the console
//            clearScreen(); //todo: fix clear screen
//
//
//            switch (userCommand) {
////                case "REGISTER":
////
////                    break;
//                case "UPDATE":
//                    //IMPORT ASSUMPTION FOR THE UPDATE: updating port and IP will be done automatically so that the
//                    //user cannot mess things up. Futhermore, the prof said that everything wrt to ip and socket (save for the server address)
//                    //should be done automatically. This also gives users a better experience.
//                    System.out.println("Do you want manually or automatically update your IP and socket? AUTO/MAN ");
//
//                    String newIP = clientAddress.getHostAddress().trim();
//                    int updatePort = 5003;
//                    while (!available(updatePort)) updatePort++;
//                    System.out.println("New IP: " + newIP + ", New port: " + updatePort);
//                    //todo: add josephs update thing
//                    //todo: make sure its valid (name exists) and tell the user the result
//                    //if it fails they go back to the menu and need to try to update again
//                    break;
//
//                case "DE-REGISTER":
//                    System.out.println("Please enter the name of the user you want to delete: ");
//                    String userToDelete = scanner.nextLine();
//                    RQ deRegisterRQ = new RQ(5, rq++, userToDelete);
//                    //client.sendMessage(deRegisterRQ.getMessage());
//                    outputStream.writeObject(deRegisterRQ.getMessage());
//                    //RQ receivedDeRegisterRq = new RQ((byte[]) client.readObjectFromServer());
//
//                    break;
//                case "SUBJECTS":
//                    System.out.println("Changing the interests for user " + username);
//                    //String user = scanner.nextLine();
//                    System.out.println("Please chose among the following interests. Enter the numbers, separated by commas");
//                    System.out.println(Subjects.INTEREST1 + "\n" + Subjects.INTEREST2 + "\n" + Subjects.INTEREST3 + "\n" +
//                            Subjects.INTEREST4 + "\n" + Subjects.INTEREST5);
//                    String choices = scanner.nextLine();
//
//                    //convert the numbers inputted by the user to their associated values
//                    ArrayList<String> interestList = new ArrayList<String>();
//                    for(String choice: Arrays.asList(choices.split(","))){
//                        switch (choice){
//                            case "1":
//                                interestList.add(Subjects.INTEREST1_FOR_SERVER);
//                                break;
//                            case "2":
//                                interestList.add(Subjects.INTEREST2_FOR_SERVER);
//                                break;
//                            case "3":
//                                interestList.add(Subjects.INTEREST3_FOR_SERVER);
//                                break;
//                            case "4":
//                                interestList.add(Subjects.INTEREST4_FOR_SERVER);
//                                break;
//                            case "5":
//                                interestList.add(Subjects.INTEREST5_FOR_SERVER);
//                                break;
//                        }
//                    }
//
//                    //now send the server the subjects.
//                    RQ subjectsRQ = new RQ(10, rq++, username, interestList);
//                    //client.sendMessage(subjectsRQ.getMessage());
//                    outputStream.writeObject(subjectsRQ.getMessage());
//                    //RQ receivedSubjectsRq = new RQ((byte[]) client.readObjectFromServer());
//
//                    break;
//                case "PUBLISH":
//                    //todo: the system needs to be implemented such that when the client is booted up
//                    //you must either register or update. Once this is done, you will have the username
//                    //to do the rest of the stuff. So, if update get the username and see if it exists. If it does
//                    //check to see if the database ip and socket are different from what the user currently runs on
//                    //if they are different send the auto update, if not
//                    System.out.println("Please enter the user for which you want ");
//                    String userPublish = scanner.nextLine();
//                    System.out.println("Select the subject you want to publish to:");
//                    System.out.println(Subjects.INTEREST1 + "\n" + Subjects.INTEREST2 + "\n" + Subjects.INTEREST3 + "\n" +
//                            Subjects.INTEREST4 + "\n" + Subjects.INTEREST5);
//                    String publishChoice = scanner.nextLine();
//                    ArrayList<String> userList = new ArrayList<String>();
//                    userList.add(publishChoice);
//                    System.out.println("Type in the message you would like to publish");
//                    String publishedMessage = scanner.nextLine();
//                    RQ publishRQ = new RQ(13, rq++, userPublish, userList, publishedMessage);
//                    //client.sendMessage(publishRQ.getMessage());
//                    outputStream.writeObject(publishRQ.getMessage());
//                    //RQ receivedPublishRq = new RQ((byte[]) client.readObjectFromServer());
//                    break;
//
//                case "DONE":
//                    //client.closeConnections();
//                    socket.close();
//                    outputStream.close();
//                    System.out.println("Client disconnected from server. Have a nice day! :)");
//                    return;
//            }
//        }
//
//
////	    while(true) {
////            System.out.println("Would you like to register? yes/no");
////            line = client.readLine();
////            if (!(line.equalsIgnoreCase("yes") || line.equalsIgnoreCase("no"))) {
////                System.out.println("Invalid input. Please enter valid input.");
////            }else{
////                break;
////            }
////        }
////
////        if(line.equalsIgnoreCase("yes")) {
////            client.registerClient();
////        }


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
