package com.coen445.FinalProject;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

//todo: in client make sure that socket is unique. if not unique, chose another random socket until a free one is found
//todo: while also making sure that they're above the reserved sockets [(thus do rand() % (max socket - amount of reserved sockets)] + amount of reserved sockets

public class ClientHandlerClass extends Thread {
    private Server server;

    public ClientHandlerClass(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        super.run();
        Object received = null;
        byte[] message = null;
        Object toReturn = null;
        RQ receivedRQ = null;
//        System.out.println(server.getObjectInputStream().toString());
        System.out.println("Request on server port: " + server.getPort());
//        Socket socket = new Socket();
//        ObjectOutputStream clientOutputStream = null;
//        try {
//            clientOutputStream = new ObjectOutputStream(socket.getOutputStream());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        loop:
        while (true) {
//            if (!server.getRegistered()) {
//                try {
//                    toReturn = "TOREGISTER";
//                    server.sendObject(toReturn);
//                    server.setRegistered(true);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            } else {
                //spit the received message. Each part of the frame is separated by a space. Thus
                //the type of message will be the first element.
                try {
                    received = server.readObject();
                } catch (IOException e) {
                    //in the event a client randomly disconnects, it will throw and end of file exception.
                    //When this happens, we're going to catch it, print the log that says a user disconnected, and then move on
                    //if(e.equals(EOFException.class)){
                    if (e instanceof EOFException || e instanceof SocketException) {
                        System.out.println("A user disconnected while server waiting to receive a message");
                        break;
                    } else
                        e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                //String[] messageSegments = received.split(" ");
                message = (byte[]) received;
                try {
                    receivedRQ = new RQ(message);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }

                //Handle all incoming packets
            JSONHelper helper = new JSONHelper();
                switch (receivedRQ.getRegisterCode()) {
                    case 0: //REGISTER
                        try {
                            //start by receiving the message and logging its info
                            System.out.println("Registering new user " + receivedRQ.getName() + " " + receivedRQ.getIp() + " " + receivedRQ.getSocketNum());
                            server.setObjectOutputStream(new ObjectOutputStream(new Socket(receivedRQ.getIp(),receivedRQ.getSocketNum()).getOutputStream()));

//                            socket = new Socket(receivedRQ.getIp(), receivedRQ.getSocketNum());
//                            clientOutputStream = new ObjectOutputStream(socket.getOutputStream());
                            //check validity of new user, start by making sure that their username is unique.
                            //This is done with the json helper's return value.
                            User newUser = new User(receivedRQ.getName(), receivedRQ.getPassword(),
                                    receivedRQ.getIp(), Integer.toString(receivedRQ.getSocketNum())); //todo: check with jo if its ok if I change user.class socket to int. If so, change it
                            if (!helper.saveNewUser(newUser)) { //if false then it tells user why
                                System.out.println("The user already exists");
                                //server.sendObject("REGISTER-FAILED, USER ALREADY EXISTS");
                                //RQ returnRQ = new RQ(2, receivedRQ.getRqNum());
                                server.sendObject(new RQ(2, receivedRQ.getRqNum()).getMessage()); //send register-denies response
                            } else {
                                //server.sendObject("REGISTERED");
                                System.out.println("New user added to database");
                                RQ returnRQ = new RQ(1, receivedRQ.getRqNum()); //send registered response
                                server.sendObject(returnRQ.getMessage());
                                //server.setRegistered(true);
                            }
                        } catch (IOException e) {
                            if (e instanceof EOFException || e instanceof SocketException) {
                                System.out.println("A user disconnected while server trying to send a message");
                                break loop;
                            } else
                                e.printStackTrace();
                        }
                        break;

                    case 3: //REGISTERED (From other server)
                        //todo

                        break;
                    case 4: //REGISTER-DENIED (From other server)
                        //todo
                        break;
                    case 5: //DE-REGISTER
                        //todo kick user out as soon as they delete a user.
                        try {
                                if(helper.deleteUserWithCheck(receivedRQ.getName())) {//if true: user deleted
                                    System.out.println("User " + receivedRQ.getName() + " has been deleted");
                                    server.sendObject(new RQ(6, receivedRQ.getName()).getMessage()); //send DE-REGISTER response to other server
                                    //todo: send to other server the update
                                }
                                else { //user not found
                                    System.out.println("The user " + receivedRQ.getName() + " was not found and thus could not be deleted.");
                                }

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 6: //other server has told this server to delete the user from the DB
                        try {
                            helper.deleteUserWithoutCheck(receivedRQ.getName());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 7: //UPDATE //todo: when we hear back from khendek
                        //todo: open a connection here if its a login scenario

                        //needs to send update-confirmed (8) to both client and server
                        break;
                    case 8: //UPDATE-CONFIRMED (From server to server)
                        //received the update confirmed. Now update the user accordingly
                        try {
                            helper.updateUser(new User(receivedRQ.getName(), receivedRQ.getPassword(),
                                    receivedRQ.getIp(), Integer.toString(receivedRQ.getSocketNum())));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 10: //SUBJECTS (Client to Server -> we receive the new subjects)
                        try {
                            if(helper.updateUserSubjects(receivedRQ.getName(), receivedRQ.getSubjects())){
                                //send to client and other server update confirmed.
                                server.sendObject(new RQ(11, receivedRQ.getRqNum(), receivedRQ.getName(), receivedRQ.getSubjects()).getMessage()); //send to client

//                                clientOutputStream.writeObject(new RQ(11, receivedRQ.getRqNum(), receivedRQ.getName(), receivedRQ.getSubjects()).getMessage());
                                //server.sendObject(new RQ(11, receivedRQ.getRqNum(), receivedRQ.getName(), receivedRQ.getSubjects()).getMessage()); //send to other server //todo: update to send to server (using objectOutputStream)
                            }
                            else{
                                //send subjects-rejected to client
//                                clientOutputStream.writeObject(new RQ(12, receivedRQ.getRqNum(), receivedRQ.getName(), receivedRQ.getSubjects()).getMessage());//send to client
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 11:
                        try {
                            helper.updateUserSubjects(receivedRQ.getName(), receivedRQ.getSubjects());
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 13:

                        try {
                            //first we ensure that the person who sends the publish request is a valid user.
                            if(helper.checkIfUserExists(receivedRQ.getName())) {//if they are, check if they have the appropriate interest
                                if(helper.checkIfUserHasInterest(receivedRQ.getName(), receivedRQ.getSubjects().get(0))) { //and if they have the interest
                                    //get all users with that interest
                                    ArrayList<User> users = new ArrayList<>(helper.getAllUsersWithInterest(receivedRQ.getSubjects().get(0)));
                                    for (User user : users) {//for each user show shares that interest, send them the new message
                                        try {
                                            Socket socket = new Socket(user.getIPAddress(), Integer.parseInt(user.getSocketNumber()));
                                            ObjectOutputStream clientOutputStream = new ObjectOutputStream(socket.getOutputStream());
                                            clientOutputStream.writeObject(new RQ(14, receivedRQ.getName(),
                                                    receivedRQ.getSubjects(), receivedRQ.getText()).getMessage());
                                            socket.close();
                                            clientOutputStream.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 17:
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + receivedRQ);
                }
            //} //end else for that old bs if
        }
        System.out.println("Session Terminated");
    }
}
