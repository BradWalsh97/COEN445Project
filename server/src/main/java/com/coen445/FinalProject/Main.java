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
    public static ScheduledExecutorService servingTimer = Executors.newScheduledThreadPool(2);
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
        boolean correctUpdateInput = false;
        String isPrimaryString = "";
        Scanner scanner = new Scanner(System.in);
        String serverIP = InetAddress.getLocalHost().getHostAddress();
        DatagramSocket socket = null;

        //Start by getting some initial info
        while(!correctInput) {
            //get user inputs
            System.out.println("Hello Systems Administrator. Is this server A or B? (A/B)"); //used for database recognition
            whichServer = scanner.nextLine().toUpperCase();
            System.out.println("Is this server the primary server? (Y/N)");
            isPrimaryString = scanner.nextLine();
            int serverSwitchTime = 5; //stored in minutes

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
        socket = new DatagramSocket(serverPort);
        System.out.println("This server is listening on port: " + serverPort);

        System.out.println("This server is running on port: " + InetAddress.getLocalHost().getHostAddress());

        //get info about the other server
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
            if(altServerPort >= 5001 && altServerPort <= 65535)
                correctPortInput = true;
            else System.out.println("Invalid port number, please try again. \n\n");

        }

        //now, in the event of a server reset, give the systems administrator the option to update the other server.
        while(!correctUpdateInput){
            System.out.println("Do you want to update the other server with your current into? (Y/N)");
            String input = scanner.nextLine();
            if(input.equalsIgnoreCase("Y")) {
                wantToUpdate = true;
                correctUpdateInput = true;
            } else if(input.equalsIgnoreCase("N")){
                wantToUpdate = false;
                correctUpdateInput = true;
            }else{
                System.out.println("Incorrect input, please try again.");
                correctUpdateInput = false;
            }

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
            int delay = randTimerValue.nextInt(2) + 3;
            System.out.println("Server will stop serving in " + delay + " minutes.");
            servingTimer.schedule(Main::toggleIsServer, delay, TimeUnit.MINUTES); //choose a random number between 2 & 5 minutes.
//            servingTimer.schedule(Main::toggleIsServer, 30, TimeUnit.SECONDS);
        }else{
            System.out.println("In 6 minutes, i will check if serving server shut down so i can serve");
            servingTimer.schedule(Main::checkIfOtherServerIsOff, 6, TimeUnit.MINUTES);
        }

        //Thread.sleep(5000);
        //ServerConnection serverConnection = new ServerConnection(serverPort);
        //new Thread(serverConnection).start();

        clientHandler = new ClientHandler(socket);
        if(wantToUpdate){
            updateServer(serverIP, altServerPort);
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

    public static void updateServer(String serverIP, int otherServerPort){
        try {
            RQ returnRQ = new RQ(17, serverIP, serverPort);
            Request.Register message = returnRQ.getRequestOut();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
            outputStream.writeObject(message);
            byte[] dataSent = byteArrayOutputStream.toByteArray();
            DatagramPacket dp = new DatagramPacket(dataSent, dataSent.length, InetAddress.getByName(altServerIP), otherServerPort);
            clientHandler.getSocket().send(dp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void checkIfOtherServerIsOff(){
        if(!isServing){

            JSONHelper helper = new JSONHelper(whichServer);
            ArrayList<User> users = new ArrayList<>(helper.getLoggedInUsers());

            //CHANGE SERVER to clients
            for(User user : users) {
                try {
                    RQ returnRQ = new RQ(16, altServerIP, altServerPort);
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
                RQ returnRQ = new RQ(16, altServerIP, altServerPort);
                Request.Register message = returnRQ.getRequestOut();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
                outputStream.writeObject(message);
                byte[] dataSent = byteArrayOutputStream.toByteArray();
                DatagramPacket dp = new DatagramPacket(dataSent, dataSent.length, InetAddress.getByName(altServerIP), altServerPort);
                clientHandler.getSocket().send(dp);
            } catch (Exception e) {
                e.printStackTrace();
            }

            isServing = true;
        }
    }

    public static void toggleIsServer(){
        if(!available(altServerPort)) {

            JSONHelper helper = new JSONHelper(whichServer);
            ArrayList<User> users = new ArrayList<>(helper.getLoggedInUsers());

            //CHANGE SERVER to clients
            for (User user : users) {
                try {
                    RQ returnRQ = new RQ(16, altServerIP, altServerPort);
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
                RQ returnRQ = new RQ(16, altServerIP, altServerPort);
                Request.Register message = returnRQ.getRequestOut();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
                outputStream.writeObject(message);
                byte[] dataSent = byteArrayOutputStream.toByteArray();
                DatagramPacket dp = new DatagramPacket(dataSent, dataSent.length, InetAddress.getByName(altServerIP), altServerPort);
                clientHandler.getSocket().send(dp);
            } catch (Exception e) {
                e.printStackTrace();
            }

            isServing = !isServing;
            System.out.println("In 6 minutes, i will check if serving server shut down so i can serve");
            servingTimer.schedule(Main::checkIfOtherServerIsOff, 6, TimeUnit.MINUTES);
        }else{
            System.out.print("Other server is off so i will keep serving.");
            Random randTimerValue = new Random();
            int delay = randTimerValue.nextInt(2) + 3;
            System.out.println("Server will stop serving in " + delay + " minutes.");
            servingTimer.schedule(Main::toggleIsServer, delay, TimeUnit.MINUTES); //choose a random number between 2 & 5 minutes.
        }
    }
}



