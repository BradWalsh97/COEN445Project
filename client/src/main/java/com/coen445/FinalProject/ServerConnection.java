package com.coen445.FinalProject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ServerConnection extends Thread {
    private DatagramSocket client;

    public ServerConnection(DatagramSocket socket) throws IOException {
        this.client = socket;
    }

    @Override
    public void run() {
        try{
            while(true){
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
                        System.out.println("\n" + Main.username + " has been registered!");
                        Main.registerSuccess = true;
                        break;

                    case 2: //REGISTER DENIED
                        System.out.println("\n" + receivedRQ.getText() + "\n");
                        break;

                    case 14: //MESSAGE
                        System.out.println("\n" + receivedRQ.getSubjects().get(0) + ": " + receivedRQ.getText() + " from " + receivedRQ.getName() + "\n");
                        break;

                    case 8: //UPDATE CONFIRMED
                        System.out.println("\nSuccessfully logged in\n");
                        Main.registerSuccess = true;
                        break;

                    case 9: //UPDATE DENIED
                        System.out.println("\n" + receivedRQ.getText() + "\n");
                        break;

                    case 11:
                        System.out.println("\nInterests have been updated!\n");
                        break;

                    case 12: //SUBJECTS DENIED
                        System.out.println("\nProvided subjects were denied by the server\n");
                        break;

                    case 15: //PUBLISH DENIED
                        System.out.println("\nMessage could not be published.. " + receivedRQ.getText() + "\n");
                        break;

                    case 16: //CHANGE SERVER
                        System.out.println("\nServing server is changing\n");
                        Main.altIP = Main.servingIP;
                        Main.servingIP = receivedRQ.getIp();
                        Main.altServingPort = Main.servingPort;
                        Main.servingPort = receivedRQ.getSocketNum();
                        System.out.println("IP:" + Main.servingIP + "port: " + receivedRQ.getSocketNum()); //uncomment for debug
                        break;

                    case 21: //who is serving
                        System.out.println("\nServing server is " + receivedRQ.getName() + "\n");
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

            }
        }catch (IOException | ClassNotFoundException e){
            if(e instanceof SocketException){
                System.out.println("\nThe RSS server is currently experiencing an outage. \n" +
                        "Please try again in a few minutes while we work to restore the service! <3\n");
            }else
                e.printStackTrace();
        }finally {
            client.close();
        }
    }
}
