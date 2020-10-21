package com.coen445FinalProject.client;

import com.coen445FinalProject.client1.ClientUser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private int port;
    private String line = "";
    private Socket socket = null;
    private ServerSocket serverSocket = null;
    //private DataInputStream dataInputStream = null;
    private BufferedReader bufferedReader = null;
    private ObjectInputStream objectInputStream = null;
    private ObjectOutputStream objectOutputStream = null;

    public Server(int port){
        this.port = port;

    }

    public void startSerer() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server Started");

        System.out.println("Waiting for client...");
    }

    public void acceptClient() throws IOException {
        socket = serverSocket.accept();
        System.out.println("Client Connected");

        objectInputStream = new ObjectInputStream(socket.getInputStream());
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

        bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    }

    public void endConnection() throws IOException {
        System.out.println("Closing Connection");

        socket.close();
        objectInputStream.close();
        objectOutputStream.close();
    }

    private Object readObject() throws IOException, ClassNotFoundException {
        return objectInputStream.readObject();
    }

    public String checkMessage() throws IOException, ClassNotFoundException {

            while (!line.equalsIgnoreCase("Done")) {
                line = (String) readObject();


                if (line.equalsIgnoreCase("complete")) {
                    User user = (User) readObject();
                }
            }

            return "done";

    }
}
