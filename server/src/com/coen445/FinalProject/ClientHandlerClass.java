package com.coen445.FinalProject;

import java.io.DataInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

//todo: in client make sure that socket is unique. if not unique, chose another random socket until a free one is found
//todo: while also making sure that they're above the reserved sockets [(thus do rand() % (max socket - amount of reserved sockets)] + amount of reserved sockets

public class ClientHandlerClass extends Thread{
    ObjectInputStream inputStream;
    ObjectOutputStream outputStream;
    Socket socket;

    public ClientHandlerClass(ObjectOutputStream outputStream, ObjectInputStream inputStream, Socket socket){
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        this.socket = socket;
    }

    @Override
    public void run() {
        super.run();
        String received = "";
        String toReturn = "";
        while(true){

        }
    }
}
