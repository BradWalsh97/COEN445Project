package com.coen445.FinalProject;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class ServerConnection extends Thread {
    private Socket server;
    private ObjectInputStream inputStream;

    public ServerConnection(Socket s) throws IOException {
        this.server = s;
        inputStream = new ObjectInputStream(server.getInputStream());
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object serverResponse = null;

                serverResponse = inputStream.readObject();


                RQ receivedRQ = new RQ((byte[]) serverResponse);


                switch (receivedRQ.getRegisterCode()) {

                    case 0:

                    case 1:

                    case 2:

                    case 5:

                    case 7:

                    case 9:

                    case 10:

                    case 12:

                    case 13:

                    case 14:

                    case 15:

                    case 16:

                    case 17:
                        System.out.println("Backup server ignores client messages while not serving");
                        break;

                    case 3: //REGISTERED from other server
                        //todo create 2nd database, choose which db to edit depending on port
                        System.out.println("Other server has registered user " + receivedRQ.getName());
                        break;

                    case 4: //REGISTER-DENIED from other server
                        System.out.println("Other server has denied registration to user " + receivedRQ.getName());
                        break;

                    case 6://DE-REGISTER (server to server)
                        //todo add code to delete user user from db from case 5 in serving
                        /*try {
                            //helper.deleteUserWithoutCheck(receivedRQ.getName());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/
                        break;

                    case 8://UPDATE-CONFIRMED (From server to server)
                        //received the update confirmed. Now update the user accordingly
                        //todo add code to update ip and socket from db from case 7 in serving
                        /*try {
                            //helper.updateUser(new User(receivedRQ.getName(), receivedRQ.getPassword(),
                                    //receivedRQ.getIp(), Integer.toString(receivedRQ.getSocketNum())));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/
                        break;

                    case 11: //SUBJECTS UPDATED (server to server)
                        //todo add code to update subjects in db from case 10 in serving
                        /*try {
                            helper.updateUserSubjects(receivedRQ.getName(), receivedRQ.getSubjects());
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }*/
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }
}
