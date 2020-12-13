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
    private DatagramSocket client;
    //private ObjectInputStream inputStream;

    public ServerConnection(DatagramSocket socket) throws IOException {
        this.client = socket;
        //inputStream = new ObjectInputStream(server.getInputStream());
    }

    @Override
    public void run() {
        try{
            while(true){
                //Object serverResponse = inputStream.readObject();
                byte[] receive = new byte[65535];
                DatagramPacket dp = new DatagramPacket(receive, receive.length);
                client.receive(dp);
                byte[] data = dp.getData();
                ByteArrayInputStream in = new ByteArrayInputStream(data);
                ObjectInputStream inputStream = new ObjectInputStream(in);

                Request.Register rq = (Request.Register) inputStream.readObject();

                RQ receivedRQ = new RQ(rq);


                if(receivedRQ == null) break;

                switch(receivedRQ.getRegisterCode()){
                    case 1: //REGISTERED from server
                        System.out.println(Main.username + " has been registered!");
                        Main.registerSuccess = true;
                        break;

                    case 2: //REGISTER DENIED
                        System.out.println(receivedRQ.getText());
                        break;

                    case 14: //MESSAGE
                        System.out.println(receivedRQ.getSubjects().get(0) + ": " + receivedRQ.getText() + " from " + receivedRQ.getName());
                        break;

                    case 8: //UPDATE CONFIRMED
                        System.out.println("Successfully logged in");
                        Main.registerSuccess = true;
                        break;

                    case 9: //UPDATE DENIED
                        System.out.println(receivedRQ.getText());
                        break;

                    case 13: //PUBLISH
                        System.out.println("Message from");

                    case 15: //PUBLISH DENIED
                        System.out.println("Message could not be published.. " + receivedRQ.getText());
                        break;

                    case 16: //CHANGE SERVER
                        System.out.println("Serving server is changing");
                        Main.altIP = Main.servingIP;
                        Main.servingIP = receivedRQ.getIp();
                        Main.altServingPort = Main.servingPort;
                        Main.servingPort = receivedRQ.getSocketNum();
                        break;

                    case 21: //who is serving
                        System.out.println("Serving server is " + receivedRQ.getName());
                        if(receivedRQ.getName().equalsIgnoreCase("ServerA")){
                            Main.servingPort = Main.serverAPort;
                            Main.altServingPort = Main.serverBPort;
                            Main.servingIP = Main.serverAip;
                            Main.altIP = Main.serverBip;
                        }else{
                            Main.servingPort = Main.serverBPort;
                            Main.altServingPort = Main.serverAPort;
                            Main.servingIP = Main.serverBip;
                            Main.altIP = Main.serverAip;
                        }
                        Main.whoServing = true;
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
            client.close();
            //inputStream.close();
        }
    }
}
