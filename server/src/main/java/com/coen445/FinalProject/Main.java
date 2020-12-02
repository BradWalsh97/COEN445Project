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
    public static String whichServer;
    public static boolean otherServerConnected = false;
    public static boolean backupRegistered = false;

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        boolean servingDone = false;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Hello Systems Administrator. Is this server A or B? (A/B)");
        whichServer = scanner.nextLine();
        System.out.println("Is this server the primary server? (Y/N)");
        String isPrimaryString = scanner.nextLine();

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
        if(whichServer.equalsIgnoreCase("a")) {
            serverPort = ServerInfo.SERVER_A_PORT;
            //listener = new ServerSocket(serverPort);
            altServerPort = ServerInfo.SERVER_B_PORT;
        }else if(whichServer.equalsIgnoreCase("b")){
            serverPort = ServerInfo.SERVER_B_PORT;
            //listener = new ServerSocket(serverPort);
            altServerPort = ServerInfo.SERVER_A_PORT;
        }

        //now that the server has been created, start a timer between 3 & 5 minutes
        Random randTimerValue = new Random();
        if(isPrimary) { //start n minute timer
//            servingTimer.schedule(Main::toggleIsServer, randTimerValue.nextInt(2) + 3, TimeUnit.MINUTES);
            servingTimer.schedule(Main::toggleIsServer, 5, TimeUnit.MINUTES);
        }

        //Thread.sleep(5000);
        //ServerConnection serverConnection = new ServerConnection(serverPort);
        //new Thread(serverConnection).start();

        new ClientHandler(serverPort).start();

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

    public static void toggleIsServer(){

        /*RQ returnRQ = new RQ(15, receivedRQ.getRqNum(), "The subject chosen is not in your list of interests, please update your interests and try again.");
        Request.Register message = returnRQ.getRequestOut();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
        outputStream.writeObject(message);
        byte[] dataSent = byteArrayOutputStream.toByteArray();
        DatagramPacket dp = new DatagramPacket(dataSent, dataSent.length, packet.getAddress(), packet.getPort());
        socket.send(dp);*/
        isServing = !isServing;
    }
}



