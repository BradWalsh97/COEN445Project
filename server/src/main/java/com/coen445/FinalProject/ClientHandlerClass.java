package com.coen445.FinalProject;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

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
        Object received = null;
        byte[] message = null;
        Object toReturn = null;
        RQ receivedRQ = null;
        System.out.println("Request on port: " + server.getPort());

        loop: while(true){
            if(!server.getRegistered()){
                try {
                    toReturn = "TOREGISTER";
                    server.sendObject(toReturn);
                    server.setRegistered(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                //spit the received message. Each part of the frame is separated by a space. Thus
                //the type of message will be the first element.
                try {
                    received = server.readObject();
                } catch (IOException e) {
                    //in the event a client randomly disconnects, it will throw and end of file exception.
                    //When this happens, we're going to catch it, print the log that says a user disconnected, and then move on
                    //if(e.equals(EOFException.class)){
                    if(e instanceof EOFException || e instanceof SocketException){
                        System.out.println("A user disconnected while server waiting to receive a message");
                        break;
                    }
                    else
                        e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                //String[] messageSegments = received.split(" ");
                message = (byte[])received;
                try {
                    receivedRQ = new RQ(message);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
                switch (receivedRQ.getRegisterCode()) {
                    case 0:
                        try {
                            System.out.println("Registered new user " + receivedRQ.getName() + " " + receivedRQ.getIp() + " " + receivedRQ.getSocketNum());
                            server.sendObject("REGISTERED");
                            RQ returnRQ = new RQ(1, receivedRQ.getRqNum());
                            server.sendObject(returnRQ.getMessage());
                        } catch (IOException e) {
                            if(e instanceof EOFException || e instanceof SocketException){
                                System.out.println("A user disconnected while server trying to send a message");
                                break loop;
                            }
                            else
                            e.printStackTrace();
                        }
                        // TODO: 2020-11-08 Add new user to the database (Use semaphore n shit)

                        //now that we have the data from the user we need to save it.

                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + receivedRQ);
                }
            }
        }
        System.out.println("Session Terminated");
    }
}
