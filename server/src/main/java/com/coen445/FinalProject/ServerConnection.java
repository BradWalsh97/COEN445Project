//package com.coen445.FinalProject;
//
//import com.google.protobuf.InvalidProtocolBufferException;
//
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;
//
//public class ServerConnection extends Thread {
//    private int serverPort;
//    private ServerSocket serverSocket = null;
//    private boolean isStopped = false;
//    private Thread runningThread = null;
//    //private Lock lock = new ReentrantLock();
//
//    public ServerConnection(int port) throws IOException {
//        this.serverPort = port;
//    }
//
//    @Override
//    public void run() {
//        synchronized (this){
//            this.runningThread = Thread.currentThread();
//        }
//        openServerSocket();
//        while(!isStopped()){
//            Socket client = null;
//            try{
//                System.out.println("Waiting for client");
//                client = this.serverSocket.accept();
//                System.out.println("Client Connected");
//            }catch (IOException e){
//                if(isStopped()){
//                    System.out.println("Server Stopped");
//                    return;
//                }
//                throw new RuntimeException("Error accepting client connection", e);
//            }
//            ClientHandlerClass clientThread = null;
//            try {
//                clientThread = new ClientHandlerClass(client, Main.clients);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            Main.clients.add(clientThread);
//
//            Main.pool.execute(clientThread);
//        }
//
//        System.out.println("Server stopped");
//
//    }
//
//    private synchronized boolean isStopped(){
//        return this.isStopped;
//    }
//
//    public synchronized void stopServer(){
//        this.isStopped = true;
//        try{
//            this.serverSocket.close();
//        }catch (IOException e){
//            throw new RuntimeException("Error closing server", e);
//        }
//    }
//
//    private void openServerSocket(){
//        try{
//            this.serverSocket = new ServerSocket(this.serverPort);
//        }catch (IOException e){
//            throw new RuntimeException("Error closing server", e);
//        }
//    }
//}
