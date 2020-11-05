package com.coen445.FinalProject;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

//todo: in client make sure that socket is unique. if not unique, chose another random socket until a free one is found
//todo: while also making sure that they're above the reserved sockets [(thus do rand() % (max socket - amount of reserved sockets)] + amount of reserved sockets

public class ClientHandlerClass extends Thread{
    private Server server;

    public ClientHandlerClass(Server server){
        this.server = server;
    }

    @Override
    public void run() {
        super.run();
        String received = "";
        String toReturn = "";
        while(true){
            if(!server.getRegistered()){
                try {
                    toReturn = "It appears that you are not signed in, would you like to do so?\n" +
                            "Yes: enter 1\n" +
                            "No: enter 2";
                    server.sendObject(toReturn);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                //spit the received message. Each part of the frame is separated by a space. Thus
                //the type of message will be the first element.
                String[] messageSegments = received.split(" ");
                String first = messageSegments[0];
                switch (first) {
                    case "REGISTER":
                        try {
                            server.sendObject("REGISTERED");
                            server.setRegistered(true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + messageSegments[0].toUpperCase());
                }
            }
        }
    }
}
