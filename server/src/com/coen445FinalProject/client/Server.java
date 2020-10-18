package com.coen445FinalProject.client;

import com.coen445FinalProject.client1.ClientUser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private Socket socket = null;
    private ServerSocket serverSocket = null;
    //private DataInputStream dataInputStream = null;
    private BufferedReader bufferedReader = null;
    private ObjectInputStream objectInputStream = null;
    private ObjectOutputStream objectOutputStream = null;

    public Server(int port) throws IOException, ClassNotFoundException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server Started");

        System.out.println("Waiting for client...");

        while(true) {
            socket = serverSocket.accept();
            System.out.println("Client Connected");

            //dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

            bufferedReader = new BufferedReader(new InputStreamReader(System.in));

            String line = "";

            while (!line.equalsIgnoreCase("Done")) {
                line = (String)objectInputStream.readObject();
                System.out.println(line);


                if (line.equalsIgnoreCase("complete")) {
                    User user = (User) objectInputStream.readObject();

                    System.out.println(user.getUserName());
                }
            }

            System.out.println("Closing Connection");

            socket.close();
            //dataInputStream.close();
            objectInputStream.close();
            objectOutputStream.close();
        }
    }
}
