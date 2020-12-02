package com.coen445.FinalProject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    //public static ArrayList<ClientHandlerClass> clients = new ArrayList<>();
    public static ExecutorService pool = Executors.newFixedThreadPool(4);

    public static boolean isServing = true;
    public static ScheduledExecutorService servingTimer = Executors.newScheduledThreadPool(1);
    public static int serverPort;
    public static int altServerPort;
    public static String altServerIP;
    public static String whichServer;
    public static boolean otherServerConnected = false;
    public static boolean backupRegistered = false;
    public static ClientHandler clientHandler = null;
    public static boolean wantToUpdate = false;

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        boolean servingDone = false;
        boolean correctInput = false;
        boolean correctPortInput = false;
        String isPrimaryString = "";
        Scanner scanner = new Scanner(System.in);

        //Start by getting some initial info
        while(!correctInput) {
            //get user inputs
            System.out.println("Hello Systems Administrator. Is this server A or B? (A/B)"); //used for database recognition
            whichServer = scanner.nextLine();
            System.out.println("Is this server the primary server? (Y/N)");
            isPrimaryString = scanner.nextLine();

            //check for correct inputs:
            if ((whichServer.equalsIgnoreCase("a") || whichServer.equalsIgnoreCase("b"))
            && (isPrimaryString.equalsIgnoreCase("Y") || isPrimaryString.equalsIgnoreCase("N"))){
                correctInput = true;
            } else {
                System.out.println("Invalid inputs, please try again :)");
            }
        }

        //get which port the server will listen on and then print it to the terminal.
        serverPort = 5001;
        while(!available(serverPort)) //get the server to run on a available port
            serverPort++;
        System.out.println("This server is listening on port: " + serverPort);

        System.out.println("This server is running on port: " + InetAddress.getLocalHost().getHostAddress());


        while(!correctPortInput){
            System.out.println("Please enter the ip which the other server is running on: ");
            altServerIP = scanner.nextLine();

            //Pattern sourced from: https://stackoverflow.com/questions/5667371/validate-ipv4-address-in-java
            String ipv4Pattern = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
            if(altServerIP.matches(ipv4Pattern)){
                correctInput = true;
            } else{
                System.out.println("Hmm, that IP address doesn't look right. Please try again");
                continue;
            }
            System.out.println("Please enter the port which the other server is listening on: ");
            altServerPort = Integer.parseInt(scanner.nextLine());
            if(altServerPort > 5001 && altServerPort < 65535)
                correctPortInput = true;
            else System.out.println("Invalid port number, please try again. \n\n");

        }


        boolean isPrimary;
        if(isPrimaryString.equalsIgnoreCase("Y")) {
            isPrimary = true;
            isServing = true;
        }
        else {
            isPrimary = false;
            isServing = false;
        }




        //todo do server socket between server A and B

        //ServerSocket listener = null;
        //Socket otherServerSocket = null;
        //if(available(serverPort))
//        if(whichServer.equalsIgnoreCase("a")) {
//            serverPort = ServerInfo.SERVER_A_PORT;
//            //listener = new ServerSocket(serverPort);
//            altServerPort = ServerInfo.SERVER_B_PORT;
//        }else if(whichServer.equalsIgnoreCase("b")){
//            serverPort = ServerInfo.SERVER_B_PORT;
//            //listener = new ServerSocket(serverPort);
//            altServerPort = ServerInfo.SERVER_A_PORT;
//        }

        //now that the server has been created, start a timer between 3 & 5 minutes
        Random randTimerValue = new Random();
        if(isPrimary) { //start n minute timer
//            servingTimer.schedule(Main::toggleIsServer, randTimerValue.nextInt(2) + 3, TimeUnit.MINUTES);
            servingTimer.schedule(Main::toggleIsServer, 30, TimeUnit.SECONDS);
        }

        //Thread.sleep(5000);
        //ServerConnection serverConnection = new ServerConnection(serverPort);
        //new Thread(serverConnection).start();

        clientHandler = new ClientHandler(serverPort);
        if(wantToUpdate){
            updateServer(1);
            wantToUpdate = false;
        }
        clientHandler.start();

        //System.out.println("Stopping server");
        //serverConnection.stopServer();


        //server.startSever();
        /*while (true) {
            //server.acceptClient();
            //Thread t = new ClientHandlerClass(server);
            //t.start();

            System.out.println("Waiting for client connection...");
            Socket client = listener.accept();



            System.out.println("socket is " + client.getLocalPort() + " and " + client.getPort());
            System.out.println("Connected to client");
            ClientHandlerClass clientThread = new ClientHandlerClass(client, clients);
            clients.add(clientThread);

            pool.execute(clientThread);


            //server.checkMessage();
            //server.endConnection();
        }*/
        /*User user = new User("Bob", "Password123");
        user.addInterest("Soccer");
        user.addInterest("Football");
        user.addInterest("Hockey");
        user.addInterest("Rugby");
        User user1 = new User("Jim", "Password123");
        user1.addInterest("Soccer");
        user1.addInterest("Football");
        user1.addInterest("Hockey");
        user1.addInterest("Rugby");
        JSONHelper helper = new JSONHelper();
        helper.saveUserToJSON(user);
        helper.saveUserToJSON(user1);*/
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

    public static void updateServer(int otherServerPort){
        try {
            RQ returnRQ = new RQ(17, ServerInfo.SERVER_A_ADDRESS, serverPort);
            Request.Register message = returnRQ.getRequestOut();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
            outputStream.writeObject(message);
            byte[] dataSent = byteArrayOutputStream.toByteArray();
            DatagramPacket dp = new DatagramPacket(dataSent, dataSent.length, InetAddress.getByName(ServerInfo.SERVER_A_ADDRESS), otherServerPort);
            clientHandler.getSocket().send(dp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void toggleIsServer(){

        JSONHelper helper = new JSONHelper(whichServer);
        ArrayList<User> users = new ArrayList<>(helper.getLoggedInUsers());

        //CHANGE SERVER to clients
        for(User user : users) {
            try {
                RQ returnRQ = new RQ(16, ServerInfo.SERVER_A_ADDRESS, altServerPort);
                Request.Register message = returnRQ.getRequestOut();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
                outputStream.writeObject(message);
                byte[] dataSent = byteArrayOutputStream.toByteArray();
                DatagramPacket dp = new DatagramPacket(dataSent, dataSent.length, InetAddress.getByName(user.getIPAddress()), Integer.parseInt(user.getSocketNumber()));
                clientHandler.getSocket().send(dp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //CHANGE SERVER to other server
        try {
            RQ returnRQ = new RQ(16, ServerInfo.SERVER_A_ADDRESS, altServerPort);
            Request.Register message = returnRQ.getRequestOut();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
            outputStream.writeObject(message);
            byte[] dataSent = byteArrayOutputStream.toByteArray();
            DatagramPacket dp = new DatagramPacket(dataSent, dataSent.length, InetAddress.getByName(ServerInfo.SERVER_A_ADDRESS), altServerPort);
            clientHandler.getSocket().send(dp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        isServing = !isServing;
    }
}



