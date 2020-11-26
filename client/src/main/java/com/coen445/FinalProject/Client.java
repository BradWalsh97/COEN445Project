package com.coen445.FinalProject;

import java.io.*;
import java.net.Socket;

public class Client {
    private String address;
    private String line = "";
    private int port;
    private Socket socket = null;
    private BufferedReader bufferedReader = null;
    //private DataOutputStream dataOutputStream = null;
    private ObjectOutputStream objectOutputStream = null;
    private ObjectInputStream objectInputStream = null;

    public Client(String address, int port) throws IOException, ClassNotFoundException {
        this.address = address;
        this.port = port;
    }

    public void connectToServer() throws IOException {
        socket = new Socket(address,port);
        System.out.println("Connected");

        bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectInputStream = new ObjectInputStream(socket.getInputStream());
    }

    public void closeConnections() throws IOException {
        bufferedReader.close();
        objectOutputStream.close();
        objectInputStream.close();
        socket.close();
    }

    public String readLine() throws IOException {
        return bufferedReader.readLine();
    }

    private void writeObjectToServer(Object object) throws IOException {
        objectOutputStream.writeObject(object);
    }

    public Object readObjectFromServer() throws IOException, ClassNotFoundException {
        return objectInputStream.readObject();
    }

    public void registerClient() throws IOException {
        writeObjectToServer("REGISTER");
        System.out.println("Write in username");
        line = readLine();
        String name = line;
        writeObjectToServer(name);
        System.out.println("Write in password");
        line = readLine();
        String pass = line;
        writeObjectToServer(pass);
        writeObjectToServer("complete");
        User user = new User(name, pass, "localhost", "5001");
        writeObjectToServer(user);
    }

    public void sendMessage() throws IOException {
        while(!line.equalsIgnoreCase("Done")){
            line = readLine();
            writeObjectToServer(line);
        }
    }

    public void sendMessage(Object message) throws IOException{
        writeObjectToServer(message);
    }
}
