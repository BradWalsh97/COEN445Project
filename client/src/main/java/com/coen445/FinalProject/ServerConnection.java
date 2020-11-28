package com.coen445.FinalProject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;

public class ServerConnection extends Thread {
    private DatagramSocket server;
    //private ObjectInputStream inputStream;

    public ServerConnection(DatagramSocket socket) throws IOException {
        this.server = socket;
        //inputStream = new ObjectInputStream(server.getInputStream());
    }

    @Override
    public void run() {
        try{
            while(true){
                //Object serverResponse = inputStream.readObject();
                byte[] receive = new byte[65535];
                DatagramPacket dp = new DatagramPacket(receive, receive.length);
                server.receive(dp);
                byte[] data = dp.getData();
                ByteArrayInputStream in = new ByteArrayInputStream(data);
                ObjectInputStream inputStream = new ObjectInputStream(in);

                Request.Register rq = (Request.Register) inputStream.readObject();

                RQ receivedRQ = new RQ(rq);


                if(receivedRQ == null) break;

                switch(receivedRQ.getRegisterCode()){
                    case 1:
                        System.out.println(Main.username + " has been registered!");
                        Main.registerSuccess = true;
                        break;

                    case 2:
                        System.out.println(Main.username + " already exists. Please chose another name");
                        break;

                    case 14:
                        System.out.println(receivedRQ.getSubjects().get(0) + ": " + receivedRQ.getText() + " from " + receivedRQ.getName());
                        break;

                    case 8:
                        System.out.println("Successfully logged in");
                        Main.registerSuccess = true;
                        break;

                    case 9:
                        System.out.println(receivedRQ.getText());
                        break;

                    case 13:
                        System.out.println("Message from");

                    case 15:
                        System.out.println("Message could not be published.. " + receivedRQ.getText());
                        break;
                }

                //System.out.println(receivedRq);
            }
        }catch (IOException | ClassNotFoundException e){
            if(e instanceof SocketException){
                System.out.println("The RSS server is currently experiencing an outage. \n" +
                        "Please try again in a few minutes while we work to restore the service! <3");
            }else
                e.printStackTrace();
        }finally {
            server.close();
            //inputStream.close();
        }
    }
}
