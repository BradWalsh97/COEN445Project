package com.coen445.FinalProject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static boolean registerSuccess = false;
    public static String username = "";
    public static String serverAip = "";
    public static String serverBip = "";
    public static int serverAPort;
    public static int serverBPort;
    public static int servingPort;
    public static int altServingPort;
    public static String servingIP = "";
    public static String altIP = "";
    public static boolean whoServing = false;

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        //for now, ask a user to register every time
        int rq = 1;
        Scanner scanner = new Scanner(System.in);
        InetAddress clientAddress = InetAddress.getLocalHost();
        System.out.println("Hello, lets get some info about the servers you want to connect to. \nWhat is the ip of server A?");
        serverAip = scanner.nextLine();
        System.out.println("What about server B ip?");
        serverBip = scanner.nextLine();
        System.out.println("What is the port of server A?");
        serverAPort = Integer.parseInt(scanner.nextLine());
        System.out.println("What is server B port?");
        serverBPort = Integer.parseInt(scanner.nextLine());
        boolean validChoice = false;
        boolean loop = true;
        DatagramSocket socket = null;
        ServerConnection serverConnection = null;

        try {
            socket = new DatagramSocket();
            serverConnection = new ServerConnection(socket);
            System.out.println("Client bound to port " + socket.getLocalPort());
            new Thread(serverConnection).start();
        } catch (ConnectException e) {
            System.out.println("Socket unable to connect");
        }

        //Here we check with both servers to determine who is the serving server
        do {
            System.out.println("Asking both servers who is serving");
            try {
                RQ servingRQ = new RQ(20, "who is serving?");
                Request.Register message = servingRQ.getRequestOut();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
                outputStream.writeObject(message);
                byte[] data = byteArrayOutputStream.toByteArray();
                DatagramPacket dpA = new DatagramPacket(data, data.length, InetAddress.getByName(serverAip), serverAPort);
                socket.send(dpA);
                DatagramPacket dpB = new DatagramPacket(data, data.length, InetAddress.getByName(serverBip), serverBPort);
                socket.send(dpB);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Thread.sleep(1000);
            if (!whoServing) {
                System.out.println("No response waiting 30 seconds.");
                Thread.sleep(30000);
            }
        } while (!whoServing);

        while (true) {
            do {
                loop = true;
                do {
                    System.out.println("\n\nWould you like to login or register? LOGIN/REGISTER");
                    String loginOrRegister = scanner.nextLine();

                    if (loginOrRegister.equalsIgnoreCase("login")) {
                        validChoice = true;
                        loop = true;
                        System.out.println("Please enter your username");
                        username = scanner.nextLine();


                        //UPDATE to serving server
                        try {
                            RQ updateRQ = new RQ(7, rq++, username, clientAddress.getHostAddress(), socket.getLocalPort());
                            Request.Register message = updateRQ.getRequestOut();
                            System.out.println("Logging in Client: " + updateRQ.getName());
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
                            outputStream.writeObject(message);
                            byte[] data = byteArrayOutputStream.toByteArray();
                            DatagramPacket dp = new DatagramPacket(data, data.length, InetAddress.getByName(servingIP), servingPort);
                            socket.send(dp);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Thread.sleep(1000);


                    } else if (loginOrRegister.equalsIgnoreCase("register")) {
                        validChoice = true;
                        System.out.println("Please enter a username: ");
                        username = scanner.nextLine();
                        System.out.println("Your username is : " + username + "\n" + "Please enter a password: ");
                        String password = scanner.nextLine();
                        System.out.println("Your password is: " + password);

                        //send REGISTER to server A and server B
                        try {
                            RQ registerRQ = new RQ(0, rq++, username, clientAddress.getHostAddress(), socket.getLocalPort());
                            Request.Register message = registerRQ.getRequestOut();
                            System.out.println("Registering Client: " + registerRQ.getName());
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
                            outputStream.writeObject(message);
                            byte[] data = byteArrayOutputStream.toByteArray();
                            DatagramPacket dpA = new DatagramPacket(data, data.length, InetAddress.getByName(servingIP), servingPort);
                            socket.send(dpA);
                            DatagramPacket dpB = new DatagramPacket(data, data.length, InetAddress.getByName(altIP), altServingPort);
                            socket.send(dpB);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Thread.sleep(1000);
                    } else //invalid choice
                        System.out.println("Please enter a valid input.");
                } while (!registerSuccess);
            } while (!validChoice);

            while (loop) {
                System.out.println("Command list: " +
                        "\nTo delete a user: DE-REGISTER" +
                        "\nTo update your subjects: SUBJECTS " +
                        "\nTo publish a message: PUBLISH" +
                        "\nFor a joke: JOKE" +
                        "\nTo exit: LOG OUT");
                String userCommand = scanner.nextLine().toUpperCase();


                //switch case for each possible message
                switch (userCommand) {

                    case "DE-REGISTER":
                        System.out.println("Are you sure you want to de-register? You will need to recreate an account to " +
                                "continue using this service! (Y/N)");
                        String confirmDeRegister = scanner.nextLine();


                        if (confirmDeRegister.equalsIgnoreCase("y")) {
                            validChoice = false;
                            registerSuccess = false;
                            //DE-REGISTER to serving server
                            try {
                                RQ deRegisterRQ = new RQ(5, rq++, username);
                                Request.Register message = deRegisterRQ.getRequestOut();
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
                                outputStream.writeObject(message);
                                byte[] data = byteArrayOutputStream.toByteArray();
                                DatagramPacket dp = new DatagramPacket(data, data.length, InetAddress.getByName(servingIP), servingPort);
                                socket.send(dp);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Thread.sleep(1000);
                        loop = false;
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
                        try {
                            RQ subjectsRQ = new RQ(10, rq++, username, interestList);
                            Request.Register message = subjectsRQ.getRequestOut();
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
                            outputStream.writeObject(message);
                            byte[] data = byteArrayOutputStream.toByteArray();
                            DatagramPacket dp = new DatagramPacket(data, data.length, InetAddress.getByName(servingIP), servingPort);
                            socket.send(dp);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Thread.sleep(1000);

                        break;
                    case "PUBLISH":
                        System.out.println(socket.getLocalPort());
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
                        try {
                            RQ publishRQ = new RQ(13, rq++, username, userList, publishedMessage);
                            Request.Register message = publishRQ.getRequestOut();
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
                            outputStream.writeObject(message);
                            byte[] data = byteArrayOutputStream.toByteArray();
                            DatagramPacket dp = new DatagramPacket(data, data.length, InetAddress.getByName(servingIP), servingPort);
                            socket.send(dp);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Thread.sleep(1000);
                        break;

                    case "LOG OUT":
                        //client.closeConnections();
                        validChoice = false;
                        registerSuccess = false;
                        try {
                            RQ logOutRQ = new RQ(18, username);
                            Request.Register message = logOutRQ.getRequestOut();
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
                            outputStream.writeObject(message);
                            byte[] data = byteArrayOutputStream.toByteArray();
                            DatagramPacket dp = new DatagramPacket(data, data.length, InetAddress.getByName(servingIP), servingPort);
                            socket.send(dp);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        System.out.println("Client disconnected from server. Have a nice day! :)");
                        loop = false;
                        break;

                    case "JOKE":
                        System.out.println("I'd tell you a joke about UDP, but you probably won't get it :D HAHAHAHAHAHAHA");
                        break;
                }
            }
        }
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
