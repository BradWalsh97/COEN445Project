package com.coen445.FinalProject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static ArrayList<ClientHandlerClass> clients = new ArrayList<>();
    private static ExecutorService pool = Executors.newFixedThreadPool(4);

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        //Server server = new Server(5001); //todo: use same free port thing as client but only check for 5001 or 5002
        ServerSocket listener = new ServerSocket(5001);
        //server.startSever();
        while (true) {
            //server.acceptClient();
            //Thread t = new ClientHandlerClass(server);
            //t.start();
            System.out.println("Waiting for client connection...");
            Socket client = listener.accept();
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
}
