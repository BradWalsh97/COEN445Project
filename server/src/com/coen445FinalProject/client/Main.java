package com.coen445FinalProject.client;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // TODO: 2020-10-18 change package names in both projects 
        Server server = new Server(5001);
        server.startSerer();
        while (true) {
            server.acceptClient();
            String done = server.checkMessage();
            if(done.equalsIgnoreCase("done")){
                break;
            }
        }

        server.endConnection();
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
