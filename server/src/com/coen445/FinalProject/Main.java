package com.coen445.FinalProject;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Server server = new Server(5001); //todo: use same free port thing as client but only check for 5001 or 5002
        server.startSever();
        while (true) {
            server.acceptClient();
            Thread t = new ClientHandlerClass(server);
            t.start();
            //server.checkMessage();
            //server.endConnection();
        }
        /*User user = new User("Bob", "Password123");
        user.addInterest("Soccer");
        user.addInterest("Football");
        user.addInterest("Hockey");
        user.addInterest("Rugby");
        User user1 = new User("Jim", "Password123");
        user1.addInterest("Soccer");
        user1.addInterest("Football");
        user1.addInterest("Hockey");
        user1.addInterest("Rugby");
        JSONHelper helper = new JSONHelper();
        helper.saveUserToJSON(user);
        helper.saveUserToJSON(user1);*/
    }
}
