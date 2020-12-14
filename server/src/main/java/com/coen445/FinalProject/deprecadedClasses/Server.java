package com.coen445.FinalProject.deprecadedClasses;

import com.coen445.FinalProject.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private int port;
    private String line = "";
    private Socket socket = null;
    private boolean registered = false;
    private ServerSocket serverSocket = null;
    //private DataInputStream dataInputStream = null;
    private BufferedReader bufferedReader = null;
    private ObjectInputStream objectInputStream = null;
    private ObjectOutputStream objectOutputStream = null;

    public Server(int port){
        this.port = port;

    }

    public boolean getRegistered(){
        return this.registered;
    }

    public void setRegistered(boolean r){
        this.registered = r;
    }

    public void startSever() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

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

    public Object readObject() throws IOException, ClassNotFoundException {
        return objectInputStream.readObject();
    }

    public void checkMessage() throws IOException, ClassNotFoundException {

            while (!line.equalsIgnoreCase("Done")) {
                line = (String) readObject();

                if(line.equalsIgnoreCase("register"))
                    System.out.println(line);


                if (line.equalsIgnoreCase("complete")) {
                    User user = (User) readObject();

                    System.out.println(user.getUserName());
                }
            }

            //return "done";

    }

    public void sendObject(Object object) throws IOException {
        objectOutputStream.writeObject(object);
    }

    public int getPort() {
        return port;
    }
}
