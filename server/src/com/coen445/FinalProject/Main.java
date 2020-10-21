package com.coen445FinalProject.server;

import java.io.IOException;

public class Main {
//todo: make sure socket number is unique
    public static void main(String[] args) throws IOException {
        //Server server = new Server(5001);

        //Test creating new users
        User user0 = new User("Bob", "Password123");
        user0.addInterest("Soccer");
        user0.addInterest("Football");
        user0.addInterest("Hockey");
        user0.addInterest("Rugby");
        user0.setSocketNumber(5000);
        user0.setIPAddress("localhost");


        User user1 = new User("Jim", "Password123");
        user1.addInterest("Soccer");
        user1.addInterest("Football");
        user1.addInterest("Hockey");
        user1.addInterest("Rugby");
        user1.setSocketNumber(5001);
        user1.setIPAddress("localhost");

        User user2 = new User("Tim", "Password123");
        user2.addInterest("Soccer");
        user2.addInterest("Football");
        user2.addInterest("Hockey");
        user2.addInterest("Rugby");
        user2.setSocketNumber(5002);
        user2.setIPAddress("localhost");

        JSONHelper helper = new JSONHelper();
        helper.saveNewUser(user0);
        helper.saveNewUser(user1);
        helper.saveNewUser(user2);

        //test updating user
        user0.setSocketNumber(5002);
        helper.updateUser(user0);

        user1.setUserName("JimBo :P");
        helper.updateUser(user1);


    }
}
