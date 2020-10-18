package com.coen445FinalProject.client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    private Socket socket = null;
    private BufferedReader bufferedReader = null;
    //private DataOutputStream dataOutputStream = null;
    private ObjectOutputStream objectOutputStream = null;
    private ObjectInputStream objectInputStream = null;

    public Client(String address, int port) throws IOException, ClassNotFoundException {
        socket = new Socket(address, port);
        System.out.println("Connected");

        bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        //dataOutputStream = new DataOutputStream(socket.getOutputStream());

        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectInputStream = new ObjectInputStream(socket.getInputStream());

        String line = "";

        while(true) {
            System.out.println("Would you like to register? yes/no");
            line = bufferedReader.readLine();
            if (!(line.equalsIgnoreCase("yes") || line.equalsIgnoreCase("no"))) {
                System.out.println("Invalid input. Please enter valid input.");
            }else{
                break;
            }
        }
        if(line.equalsIgnoreCase("yes")) {
            objectOutputStream.writeObject("REGISTER");
            //dataOutputStream.writeUTF("REGISTER");
        }

        System.out.println("Write in username");
        line = bufferedReader.readLine();
        String name = line;
        objectOutputStream.writeObject(name);
        System.out.println("Write in password");
        line = bufferedReader.readLine();
        String pass = line;
        objectOutputStream.writeObject(pass);
        objectOutputStream.writeObject("complete");
        User user = new User(name, pass, "localhost", "5001");
        System.out.println(user.getUserName());
        objectOutputStream.writeObject(user);
        //user = (User)objectInputStream.readObject();

        //System.out.println(user.getUsername() + " " + user.getIP() + " " + user.getSocket());



        while(!line.equals("Done")){
            line = bufferedReader.readLine();
            objectOutputStream.writeObject(line);
        }

        //bufferedReader.close();
        //dataOutputStream.close();
        objectInputStream.close();
        objectOutputStream.close();
        socket.close();
    }
}
