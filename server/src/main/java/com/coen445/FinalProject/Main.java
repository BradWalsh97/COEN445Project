package com.coen445.FinalProject;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static ArrayList<ClientHandlerClass> clients = new ArrayList<>();
    public static ExecutorService pool = Executors.newFixedThreadPool(4);

    public static boolean isServing = true;
    public static ScheduledExecutorService servingTimer = Executors.newScheduledThreadPool(1);
    public static int port;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        boolean servingDone = false;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Hello Systems Administrator. Please chose which port you will run this server on: ");
        port = Integer.parseInt(scanner.nextLine());
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

        ServerSocket listener = null;
        if(available(5001))
            listener = new ServerSocket(5001);
        else
            listener = new ServerSocket(5002);

        //now that the server has been created, start a timer between 3 & 5 minutes
        Random randTimerValue = new Random();
        if(isPrimary){ //start n minute timer
//            servingTimer.schedule(Main::toggleIsServer, randTimerValue.nextInt(2) + 3, TimeUnit.MINUTES);
            servingTimer.schedule(Main::toggleIsServer, 5, TimeUnit.MINUTES);

        }

        //server.startSever();
        while (true) {
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
        }
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
        isServing = !isServing;
    }
}



