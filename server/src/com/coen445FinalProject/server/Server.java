package com.coen445FinalProject.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private Socket socket = null;
    private ServerSocket serverSocket = null;
    //private DataInputStream dataInputStream = null;
    private ObjectInputStream objectInputStream = null;
    private ObjectOutputStream objectOutputStream = null;
    private User user = null;

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

            String line = "";

            while (!line.equals("Done")) {
                line = (String) objectInputStream.readObject();
                System.out.println(line);

                String oistring = (String)objectInputStream.readObject();
                //System.out.println(oistring);
                if (!(oistring instanceof String)) {
                    user = (User) objectInputStream.readObject();

                    user.setUserName("bob");
                    user.setIPAddress("localhost");
                    user.setSocketNumber("5001");

                    objectOutputStream.writeObject(user);
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
