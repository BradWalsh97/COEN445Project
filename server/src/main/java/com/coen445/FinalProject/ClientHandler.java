package com.coen445.FinalProject;

import com.google.protobuf.InvalidProtocolBufferException;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ClientHandler extends Thread {
    private DatagramSocket socket = null;

    public ClientHandler(int port) throws IOException {
        socket = new DatagramSocket(port);
    }

    public DatagramSocket getSocket(){return socket;}

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
        try {
            while (true) {
                byte[] buffer = new byte[65535];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                byte[] data = packet.getData();
                ByteArrayInputStream in = new ByteArrayInputStream(data);
                ObjectInputStream inputStream = new ObjectInputStream(in);
                Request.Register rq = (Request.Register) inputStream.readObject();

                RQ receivedRQ = new RQ(rq);
                JSONHelper helper = new JSONHelper(Main.whichServer);

                switch (receivedRQ.getRegisterCode()) {
                    case 0: //REGISTER  //todo: make an exception for an empty username
                        if(Main.isServing) {
                            try {
                                //start by receiving the message and logging its info
                                System.out.println("Registering new user " + receivedRQ.getName() + " " + receivedRQ.getIp() + " " + receivedRQ.getSocketNum());

                                //check validity of new user, start by making sure that their username is unique.
                                //This is done with the json helper's return value.
                                User newUser = new User(receivedRQ.getName(), receivedRQ.getPassword(),
                                        receivedRQ.getIp(), Integer.toString(receivedRQ.getSocketNum())); //todo: check with jo if its ok if I change user.class socket to int. If so, change it
                                if (!helper.saveNewUser(newUser)) { //if false then it tells user why
                                    System.out.println("The user already exists");
                                    try {
                                        //sending RQ to client
                                        RQ returnRQ = new RQ(2, receivedRQ.getRqNum(), "The user already exists");
                                        Request.Register message = returnRQ.getRequestOut();
                                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                        ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
                                        outputStream.writeObject(message);
                                        byte[] dataSent = byteArrayOutputStream.toByteArray();
                                        DatagramPacket dp = new DatagramPacket(dataSent, dataSent.length, packet.getAddress(), packet.getPort());
                                        socket.send(dp);

                                        //sending RQ to other server
                                        RQ toServerRQ = new RQ(4, receivedRQ.getRqNum(), receivedRQ.getName(), receivedRQ.getIp(), receivedRQ.getSocketNum());
                                        Request.Register messageToServer = toServerRQ.getRequestOut();
                                        ByteArrayOutputStream byteArrayOutputStreamToServer = new ByteArrayOutputStream();
                                        ObjectOutputStream outputStreamToServer = new ObjectOutputStream(byteArrayOutputStreamToServer);
                                        outputStreamToServer.writeObject(messageToServer);
                                        byte[] dataSentToServer = byteArrayOutputStreamToServer.toByteArray();
                                        DatagramPacket dpToServer = new DatagramPacket(dataSentToServer, dataSentToServer.length, InetAddress.getByName(ServerInfo.SERVER_A_ADDRESS), Main.altServerPort);
                                        socket.send(dpToServer);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                } else {
                                    //server.sendObject("REGISTERED");
                                    System.out.println("New user added to database");
                                    try {
                                        //sending RQ to client
                                        RQ returnRQ = new RQ(1, receivedRQ.getRqNum()); //todo: what to do with the 1
                                        Request.Register message = returnRQ.getRequestOut();
                                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                        ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
                                        outputStream.writeObject(message);
                                        byte[] dataSent = byteArrayOutputStream.toByteArray();
                                        DatagramPacket dp = new DatagramPacket(dataSent, dataSent.length, packet.getAddress(), packet.getPort());
                                        socket.send(dp);

                                        //sending RQ to other server
                                        RQ toServerRQ = new RQ(3, receivedRQ.getRqNum(), receivedRQ.getName(), receivedRQ.getIp(), receivedRQ.getSocketNum());
                                        Request.Register messageToServer = toServerRQ.getRequestOut();
                                        ByteArrayOutputStream byteArrayOutputStreamToServer = new ByteArrayOutputStream();
                                        ObjectOutputStream outputStreamToServer = new ObjectOutputStream(byteArrayOutputStreamToServer);
                                        outputStreamToServer.writeObject(messageToServer);
                                        byte[] dataSentToServer = byteArrayOutputStreamToServer.toByteArray();
                                        DatagramPacket dpToServer = new DatagramPacket(dataSentToServer, dataSentToServer.length, InetAddress.getByName(ServerInfo.SERVER_A_ADDRESS), Main.altServerPort);
                                        socket.send(dpToServer);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            } catch (IOException e) {
                                if (e instanceof EOFException || e instanceof SocketException) {
                                    System.out.println("A user disconnected while server trying to send a message");
                                    break;
                                } else
                                    e.printStackTrace();
                            }
                        }

                        break;

                    case 3: //REGISTERED from serving server
                        System.out.println("REGISTERED from serving server");
                        helper.saveNewUser(new User(receivedRQ.getName(), receivedRQ.getPassword(),
                                receivedRQ.getIp(), Integer.toString(receivedRQ.getSocketNum())));
                        break;

                    case 4: //REGISTERED-DENIED from serving server
                        System.out.println("REGISTERED-DENIED from serving server, " + receivedRQ.getName() + " will not be saved");
                        break;

                    case 5://DE-REGISTER
                        //todo check if user exists and delete
                        if(Main.isServing) {
                            try {
                                if (helper.deleteUserWithCheck(receivedRQ.getName())) {//if true: user deleted
                                    System.out.println("User " + receivedRQ.getName() + " has been deleted");
                                    //server.sendObject(new RQ(6, receivedRQ.getName()).getMessage()); //send DE-REGISTER response to other server
                                    try {
                                        //server sends DE-REGISTERED to client
                                        RQ returnRQ = new RQ(6, receivedRQ.getName());
                                        Request.Register message = returnRQ.getRequestOut();
                                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                        ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
                                        outputStream.writeObject(message);
                                        byte[] dataSent = byteArrayOutputStream.toByteArray();
                                        DatagramPacket dp = new DatagramPacket(dataSent, dataSent.length, packet.getAddress(), packet.getPort());
                                        socket.send(dp);

                                        //server sends DE-REGISTERED to other server
                                        RQ toServerRQ = new RQ(6, receivedRQ.getName());
                                        Request.Register messageToServer = toServerRQ.getRequestOut();
                                        ByteArrayOutputStream byteArrayOutputStreamToServer = new ByteArrayOutputStream();
                                        ObjectOutputStream outputStreamToServer = new ObjectOutputStream(byteArrayOutputStreamToServer);
                                        outputStreamToServer.writeObject(messageToServer);
                                        byte[] dataSentToServer = byteArrayOutputStreamToServer.toByteArray();
                                        DatagramPacket dpToServer = new DatagramPacket(dataSentToServer, dataSentToServer.length, InetAddress.getByName(ServerInfo.SERVER_A_ADDRESS), Main.altServerPort);
                                        socket.send(dpToServer);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    //todo: send to other server the update
                                } else { //user not found
                                    System.out.println("The user " + receivedRQ.getName() + " was not found and thus could not be deleted.");
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case 6: //DE-REGISTERED from serving server
                        System.out.println("DE-REGISTERED from other server, " + receivedRQ.getName() + " will be deleted from database.");
                        helper.deleteUserWithoutCheck(receivedRQ.getName());
                        break;

                    case 7://UPDATE
                        //todo: open a connection here if its a login scenario
                        //todo check if user exists and update ip and port upon login, else send user does not exit
                        //start by checking to see if the user exists
                        if(Main.isServing) {
                            try {
                                if (helper.checkIfUserExists(receivedRQ.getName())) {
                                    //if the user exists, update their info
                                    try {
                                        //UPDATE CONFIRMED to client
                                        helper.updateUser(new User(receivedRQ.getName(), receivedRQ.getPassword(), receivedRQ.getIp(), Integer.toString(receivedRQ.getSocketNum())));
                                        RQ returnRQ = new RQ(8, receivedRQ.getRqNum(), receivedRQ.getName(), receivedRQ.getIp(), receivedRQ.getSocketNum());
                                        Request.Register message = returnRQ.getRequestOut();
                                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                        ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
                                        outputStream.writeObject(message);
                                        byte[] dataSent = byteArrayOutputStream.toByteArray();
                                        DatagramPacket dp = new DatagramPacket(dataSent, dataSent.length, packet.getAddress(), packet.getPort());
                                        socket.send(dp);

                                        //UPDATE CONFIRMED to other server
                                        RQ toServerRQ = new RQ(8, receivedRQ.getRqNum(), receivedRQ.getName(), receivedRQ.getIp(), receivedRQ.getSocketNum());
                                        Request.Register messageToServer = toServerRQ.getRequestOut();
                                        ByteArrayOutputStream byteArrayOutputStreamToServer = new ByteArrayOutputStream();
                                        ObjectOutputStream outputStreamToServer = new ObjectOutputStream(byteArrayOutputStreamToServer);
                                        outputStreamToServer.writeObject(messageToServer);
                                        byte[] dataSentToServer = byteArrayOutputStreamToServer.toByteArray();
                                        DatagramPacket dpToServer = new DatagramPacket(dataSentToServer, dataSentToServer.length, InetAddress.getByName(ServerInfo.SERVER_A_ADDRESS), Main.altServerPort);
                                        socket.send(dpToServer);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    try {
                                        //UPDATE DENIED to client
                                        RQ returnRQ = new RQ(9, "Username or password did not match an existing user");
                                        Request.Register message = returnRQ.getRequestOut();
                                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                        ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
                                        outputStream.writeObject(message);
                                        byte[] dataSent = byteArrayOutputStream.toByteArray();
                                        DatagramPacket dp = new DatagramPacket(dataSent, dataSent.length, packet.getAddress(), packet.getPort());
                                        socket.send(dp);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }

                        //needs to send update-confirmed (8) to both client and server
                        break;

                    case 8: //UPDATE CONFIRMED from serving server
                        System.out.println("UPDATE-CONFIRMED from other server, " + receivedRQ.getName() + " will be updated.");
                        helper.updateUser(new User(receivedRQ.getName(), receivedRQ.getPassword(), receivedRQ.getIp(), Integer.toString(receivedRQ.getSocketNum())));
                        break;

                    case 10://SUBJECTS (Client to Server -> we receive the new subjects)
                        if(Main.isServing) {
                            try {
                                if (helper.updateUserSubjects(receivedRQ.getName(), receivedRQ.getSubjects())) {
                                    //send to client and other server update confirmed.
                                    //server.sendObject(new RQ(11, receivedRQ.getRqNum(), receivedRQ.getName(), receivedRQ.getSubjects()).getMessage()); //send to client
                                    try {
                                        //SUBJECTS UPDATED to client
                                        RQ returnRQ = new RQ(11, receivedRQ.getRqNum(), receivedRQ.getName(), receivedRQ.getSubjects());
                                        Request.Register message = returnRQ.getRequestOut();
                                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                        ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
                                        outputStream.writeObject(message);
                                        byte[] dataSent = byteArrayOutputStream.toByteArray();
                                        DatagramPacket dp = new DatagramPacket(dataSent, dataSent.length, packet.getAddress(), packet.getPort());
                                        socket.send(dp);

                                        //SUBJECTS UPDATED to other server
                                        RQ toServerRQ = new RQ(11, receivedRQ.getRqNum(), receivedRQ.getName(), receivedRQ.getSubjects());
                                        Request.Register messageToServer = toServerRQ.getRequestOut();
                                        ByteArrayOutputStream byteArrayOutputStreamToServer = new ByteArrayOutputStream();
                                        ObjectOutputStream outputStreamToServer = new ObjectOutputStream(byteArrayOutputStreamToServer);
                                        outputStreamToServer.writeObject(messageToServer);
                                        byte[] dataSentToServer = byteArrayOutputStreamToServer.toByteArray();
                                        DatagramPacket dpToServer = new DatagramPacket(dataSentToServer, dataSentToServer.length, InetAddress.getByName(ServerInfo.SERVER_A_ADDRESS), Main.altServerPort);
                                        socket.send(dpToServer);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

//                                clientOutputStream.writeObject(new RQ(11, receivedRQ.getRqNum(), receivedRQ.getName(), receivedRQ.getSubjects()).getMessage());
                                    //server.sendObject(new RQ(11, receivedRQ.getRqNum(), receivedRQ.getName(), receivedRQ.getSubjects()).getMessage()); //send to other server //todo: update to send to server (using objectOutputStream)
                                } else {
                                    //send subjects-rejected to client
//                                clientOutputStream.writeObject(new RQ(12, receivedRQ.getRqNum(), receivedRQ.getName(), receivedRQ.getSubjects()).getMessage());//send to client
                                    try {
                                        //SUBJECTS DENIED to client
                                        RQ returnRQ = new RQ(12, receivedRQ.getRqNum());
                                        Request.Register message = returnRQ.getRequestOut();
                                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                        ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
                                        outputStream.writeObject(message);
                                        byte[] dataSent = byteArrayOutputStream.toByteArray();
                                        DatagramPacket dp = new DatagramPacket(dataSent, dataSent.length, packet.getAddress(), packet.getPort());
                                        socket.send(dp);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case 11: //SUBJECTS UPDATED from other server
                        System.out.println("SUBJECTS-UPDATED from serving server, " + receivedRQ.getName() + " subjects will be updated.");
                        helper.updateUserSubjects(receivedRQ.getName(), receivedRQ.getSubjects());
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
                                            //MESSAGE to client
                                            RQ returnRQ = new RQ(14, receivedRQ.getName(), receivedRQ.getSubjects(), receivedRQ.getText());
                                            Request.Register message = returnRQ.getRequestOut();
                                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                            ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
                                            outputStream.writeObject(message);
                                            byte[] dataSent = byteArrayOutputStream.toByteArray();
                                            DatagramPacket dp = new DatagramPacket(dataSent, dataSent.length, InetAddress.getByName(user.getIPAddress()), Integer.parseInt(user.getSocketNumber()));
                                            socket.send(dp);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } else {
                                    try {
                                        //PUBLISH DENIED to client
                                        RQ returnRQ = new RQ(15, receivedRQ.getRqNum(), "The subject chosen is not in your list of interests, please update your interests and try again.");
                                        Request.Register message = returnRQ.getRequestOut();
                                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                        ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
                                        outputStream.writeObject(message);
                                        byte[] dataSent = byteArrayOutputStream.toByteArray();
                                        DatagramPacket dp = new DatagramPacket(dataSent, dataSent.length, packet.getAddress(), packet.getPort());
                                        socket.send(dp);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;

                    case 16: //CHANGE SERVER from serving sevrer
                        System.out.println("CHANGE SERVER, it is my turn to serve!!!");
                        Main.isServing = true;
                        Random randTimerValue = new Random();
                        Main.servingTimer.schedule(Main::toggleIsServer, randTimerValue.nextInt(2) + 3, TimeUnit.MINUTES); //choose a random number between 2 & 5 minutes.
                        //Main.servingTimer.schedule(Main::toggleIsServer, 30, TimeUnit.SECONDS); //use for testing purposes
                        break;

                    case 17: //update the serving server with the alt server info

                        break;

                    case 18:
                        System.out.println("LOGGING OUT, " + receivedRQ.getName() + " will be logged out.");
                        helper.userLogOnLogOff(receivedRQ.getName());
                        try{
                            RQ toServerRQ = new RQ(19, receivedRQ.getName());
                            Request.Register messageToServer = toServerRQ.getRequestOut();
                            ByteArrayOutputStream byteArrayOutputStreamToServer = new ByteArrayOutputStream();
                            ObjectOutputStream outputStreamToServer = new ObjectOutputStream(byteArrayOutputStreamToServer);
                            outputStreamToServer.writeObject(messageToServer);
                            byte[] dataSentToServer = byteArrayOutputStreamToServer.toByteArray();
                            DatagramPacket dpToServer = new DatagramPacket(dataSentToServer, dataSentToServer.length, InetAddress.getByName(ServerInfo.SERVER_A_ADDRESS), Main.altServerPort);
                            socket.send(dpToServer);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        break;

                    case 19: //LOGGING OUT from other server
                        System.out.println("LOGGING OUT from serving server, " + receivedRQ.getName() + " will be logged out.");
                        helper.userLogOnLogOff(receivedRQ.getName());
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + receivedRQ);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}


