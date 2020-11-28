package com.coen445.FinalProject;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ClientHandler extends Thread {
    private DatagramSocket socket = null;
    private byte[] buffer = new byte[65535];
    private DatagramPacket packet = null;
    private ByteArrayOutputStream bstream = null;
    private ObjectOutputStream outputStream = null;

    public ClientHandler(int port) throws IOException {
        this.socket = new DatagramSocket(port);
        bstream = new ByteArrayOutputStream();
        outputStream = new ObjectOutputStream(bstream);
    }

    public StringBuilder data(byte[] a) {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0) {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }


    @Override
    public void run() {
        while (true) {
            packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] data = packet.getData();
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream inputStream = null;
            try {
                inputStream = new ObjectInputStream(in);
            } catch (IOException e) {
                System.out.println("Stream corrupted");
            }

            Request.Register rq = null;
            try {
                rq = (Request.Register) inputStream.readObject();
            } catch (IOException | ClassNotFoundException | NullPointerException e) {
                System.out.println("Nothing from client");
            }

            RQ receivedRQ = null;
            try {
                receivedRQ = new RQ(rq);
            } catch (InvalidProtocolBufferException | NullPointerException e) {
                System.out.println("Nothing from client");
            }
            JSONHelper helper = new JSONHelper(Main.whichServer);
            Request.Register message;
            byte[] dataSent;

            try {
                switch (receivedRQ.getRegisterCode()) {
                    case 0: //register  //todo: make an exception for an empty username
                        try {
                            //start by receiving the message and logging its info
                            System.out.println("Registering new user " + receivedRQ.getName() + " " + receivedRQ.getIp() + " " + receivedRQ.getSocketNum());

                            //check validity of new user, start by making sure that their username is unique.
                            //This is done with the json helper's return value.
                            User newUser = new User(receivedRQ.getName(), receivedRQ.getPassword(),
                                    receivedRQ.getIp(), Integer.toString(receivedRQ.getSocketNum())); //todo: check with jo if its ok if I change user.class socket to int. If so, change it
                            if (!helper.saveNewUser(newUser)) { //if false then it tells user why
                                System.out.println("The user already exists");
                                RQ returnRQ = new RQ(2, receivedRQ.getRqNum());
                                message = returnRQ.getRequestOut();
                                outputStream.writeObject(message);
                                dataSent = bstream.toByteArray();
                                packet = new DatagramPacket(dataSent, dataSent.length, packet.getAddress(), packet.getPort());
                                socket.send(packet);

                            } else {
                                //server.sendObject("REGISTERED");
                                System.out.println("New user added to database");
                                RQ returnRQ = new RQ(1, receivedRQ.getRqNum(), receivedRQ.getName(), receivedRQ.getIp(), receivedRQ.getSocketNum()); //todo: what to do with the 1
                                message = returnRQ.getRequestOut();
                                outputStream.writeObject(message);
                                dataSent = bstream.toByteArray();
                                packet = new DatagramPacket(dataSent, dataSent.length, packet.getAddress(), packet.getPort());
                                socket.send(packet);
                                //server.sendObject(returnRQ.getMessage());
                                //outputStream.writeObject(returnRQ.getMessage());
                                //outputStream.writeObject(new RQ(3, receivedRQ.getRqNum(), receivedRQ.getName(), receivedRQ.getIp(), receivedRQ.getSocketNum()).getMessage());
                                //server.setRegistered(true);

                                //after sending registered to the client, we must also send it to the server not currently serving
                                /*if (Main.serverPort == 5001) {
                                    Socket socket = new Socket("localhost", 5002);
                                    ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                                    outputStream.writeObject(new RQ(3, receivedRQ.getRqNum(), receivedRQ.getName(), receivedRQ.getIp(), receivedRQ.getSocketNum()).getMessage());
                                } else if (Main.serverPort == 5002) {
                                    Socket socket = new Socket("localhost", 5001);
                                    ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                                    outputStream.writeObject(new RQ(3, receivedRQ.getRqNum(), receivedRQ.getName(), receivedRQ.getIp(), receivedRQ.getSocketNum()).getMessage());
                                }*/

                            }
                        } catch (IOException e) {
                            if (e instanceof EOFException || e instanceof SocketException) {
                                System.out.println("A user disconnected while server trying to send a message");
                                break;
                            } else
                                e.printStackTrace();
                        }

                        break;

                    case 5://DE-REGISTER
                        //todo check if user exists and delete
                        try {
                            if (helper.deleteUserWithCheck(receivedRQ.getName())) {//if true: user deleted
                                System.out.println("User " + receivedRQ.getName() + " has been deleted");
                                //server.sendObject(new RQ(6, receivedRQ.getName()).getMessage()); //send DE-REGISTER response to other server
                                RQ returnRQ = new RQ(6, receivedRQ.getName());
                                message = returnRQ.getRequestOut();
                                outputStream.writeObject(message);
                                dataSent = bstream.toByteArray();
                                packet = new DatagramPacket(dataSent, dataSent.length, packet.getAddress(), packet.getPort());
                                socket.send(packet);
                                //todo: send to other server the update
                            } else { //user not found
                                System.out.println("The user " + receivedRQ.getName() + " was not found and thus could not be deleted.");
                            }

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;

                    case 7://UPDATE
                        //todo: open a connection here if its a login scenario
                        //todo check if user exists and update ip and port upon login, else send user does not exit
                        //start by checking to see if the user exists
                        try {
                            if (helper.checkIfUserExists(receivedRQ.getName())) {
                                //if the user exists, update their info
                                try {
                                    helper.updateUser(new User(receivedRQ.getName(), receivedRQ.getPassword(), receivedRQ.getIp(), Integer.toString(receivedRQ.getSocketNum())));
                                    RQ returnRQ = new RQ(8, receivedRQ.getRqNum(), receivedRQ.getName(), receivedRQ.getIp(), receivedRQ.getSocketNum());
                                    message = returnRQ.getRequestOut();
                                    outputStream.writeObject(message);
                                    dataSent = bstream.toByteArray();
                                    packet = new DatagramPacket(dataSent, dataSent.length, packet.getAddress(), packet.getPort());
                                    socket.send(packet);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    RQ returnRQ = new RQ(9, "Username or password did not match an existing user");
                                    message = returnRQ.getRequestOut();
                                    outputStream.writeObject(message);
                                    dataSent = bstream.toByteArray();
                                    packet = new DatagramPacket(dataSent, dataSent.length, packet.getAddress(), packet.getPort());
                                    socket.send(packet);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        //needs to send update-confirmed (8) to both client and server
                        break;

                    case 10://SUBJECTS (Client to Server -> we receive the new subjects)
                        try {
                            if (helper.updateUserSubjects(receivedRQ.getName(), receivedRQ.getSubjects())) {
                                //send to client and other server update confirmed.
                                //server.sendObject(new RQ(11, receivedRQ.getRqNum(), receivedRQ.getName(), receivedRQ.getSubjects()).getMessage()); //send to client
                                RQ returnRQ = new RQ(11, receivedRQ.getRqNum(), receivedRQ.getName(), receivedRQ.getSubjects());
                                message = returnRQ.getRequestOut();
                                outputStream.writeObject(message);
                                dataSent = bstream.toByteArray();
                                packet = new DatagramPacket(dataSent, dataSent.length, packet.getAddress(), packet.getPort());
                                socket.send(packet);

//                                clientOutputStream.writeObject(new RQ(11, receivedRQ.getRqNum(), receivedRQ.getName(), receivedRQ.getSubjects()).getMessage());
                                //server.sendObject(new RQ(11, receivedRQ.getRqNum(), receivedRQ.getName(), receivedRQ.getSubjects()).getMessage()); //send to other server //todo: update to send to server (using objectOutputStream)
                            } else {
                                //send subjects-rejected to client
//                                clientOutputStream.writeObject(new RQ(12, receivedRQ.getRqNum(), receivedRQ.getName(), receivedRQ.getSubjects()).getMessage());//send to client
                                RQ returnRQ = new RQ(12, receivedRQ.getRqNum());
                                message = returnRQ.getRequestOut();
                                outputStream.writeObject(message);
                                dataSent = bstream.toByteArray();
                                packet = new DatagramPacket(dataSent, dataSent.length, packet.getAddress(), packet.getPort());
                                socket.send(packet);
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;

                    case 13://PUBLISH
                        try {
                            //first we ensure that the person who sends the publish request is a valid user.
                            if (helper.checkIfUserExists(receivedRQ.getName())) {//if they are, check if they have the appropriate interest
                                if (helper.checkIfUserHasInterest(receivedRQ.getName(), receivedRQ.getSubjects().get(0))) { //and if they have the interest
                                    //get all users with that interest
                                    ArrayList<User> users = new ArrayList<>(helper.getAllUsersWithInterest(receivedRQ.getSubjects().get(0), receivedRQ.getName()));
                                    for (User user : users) {//for each user show shares that interest, send them the new message
                                        try {
                                            RQ returnRQ = new RQ(13, receivedRQ.getRqNum(), receivedRQ.getName(), receivedRQ.getSubjects(), receivedRQ.getText());
                                            message = returnRQ.getRequestOut();
                                            outputStream.writeObject(message);
                                            dataSent = bstream.toByteArray();
                                            packet = new DatagramPacket(dataSent, dataSent.length, InetAddress.getByName(user.getIPAddress()), Integer.parseInt(user.getSocketNumber()));
                                            socket.send(packet);

                                            //System.out.println("ip " + client.getLocalAddress().getHostAddress() + " socket " + client.getLocalPort());
                                            //Socket socket = new Socket(user.getIPAddress(), Integer.parseInt(user.getSocketNumber()));
                                            //Socket socket = client;
                                            //System.out.println(socket.getLocalPort());
                                            //ObjectOutputStream clientOutputStream = new ObjectOutputStream(socket.getOutputStream());
                                            //clientOutputStream.writeObject(new RQ(14, receivedRQ.getName(),
                                            //receivedRQ.getSubjects(), receivedRQ.getText()).getMessage());
                                            //socket.close();
                                            //clientOutputStream.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } else {
                                    RQ returnRQ = new RQ(15, receivedRQ.getRqNum(), "The subject chosen is not in your list of interests, please update your interests and try again.");
                                    message = returnRQ.getRequestOut();
                                    outputStream.writeObject(message);
                                    dataSent = bstream.toByteArray();
                                    packet = new DatagramPacket(dataSent, dataSent.length, packet.getAddress(), packet.getPort());
                                    socket.send(packet);
                                }
                            }

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;

                    case 17://todo update server
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + receivedRQ);
                }
            } catch (NullPointerException e) {
                System.out.println("Nothing from client");
            }
        }
    }
}


