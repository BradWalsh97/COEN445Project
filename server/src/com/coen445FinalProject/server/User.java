package com.coen445FinalProject.server;

import java.util.ArrayList;
import java.util.List;

public class User {
    private static int nextID = 0;
    private int userID;
    private String userName;
    private String password;
    private ArrayList<String> interests;

    private String IPAddress;

    private int socketNumber;
    private boolean isDeleted;

    User(String Name, String Password){
        this.userID = User.nextID;
        User.nextID++;
        this.userName = Name;
        this.password = Password;
        this.interests = new ArrayList<String>();
        this.isDeleted = false;
    }

    public String getIPAddress() {
        return IPAddress;
    }

    public void setIPAddress(String IPAddress) {
        this.IPAddress = IPAddress;
    }

    public int getSocketNumber() {
        return socketNumber;
    }

    public void setSocketNumber(int socketNumber) {
        this.socketNumber = socketNumber;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public String getUserName() {
        return userName;
    }
    public int getUserID(){return userID;}

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ArrayList<String> getInterests() {
        return interests;
    }

    public void setInterests(ArrayList<String> interests) {
        this.interests = interests;
    }

    public void addInterest(String interest){//todo: avoid repetition
        this.interests.add(interest);
    }
}
