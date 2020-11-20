package com.coen445.FinalProject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class ServerConnection extends Thread {
    private Socket server;
    private ObjectInputStream inputStream;

    public ServerConnection(Socket s) throws IOException {
        this.server = s;
        inputStream = new ObjectInputStream(server.getInputStream());
    }

    @Override
    public void run() {
        try{
            while(true){
                Object serverResponse = inputStream.readObject();

                if(serverResponse == null) break;

                RQ receivedRq = new RQ((byte[]) serverResponse);
                if (receivedRq.getRegisterCode() == 1) {
                    System.out.println(receivedRq.getName() + " has been registered!");
                    Main.registerSuccess = true;
                } else if (receivedRq.getRegisterCode() == 2)
                    System.out.println(receivedRq.getName() + " already exists. Please chose another name");

                //System.out.println(receivedRq);
            }
        }catch (IOException | ClassNotFoundException e){
            if(e instanceof SocketException){
                System.out.println("The RSS server is currently experiencing an outage. \n" +
                        "Please try again in a few minutes while we work to restore the service! <3");
            }else
                e.printStackTrace();
        }finally {
            try{
                inputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
