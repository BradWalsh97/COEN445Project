package com.coen445FinalProject.client;

import java.io.Serializable;

public class User implements Serializable {
    private String username;
    private String IP;
    private String socket;
    private String password;

    public User(String username, String password, String IP, String socket){
        this.username = username;
        this.password = password;
        this.IP = IP;
        this.socket = socket;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public void setSocket(String socket) {
        this.socket = socket;
    }

    public String getUsername() {
        return username;
    }

    public String getIP() {
        return IP;
    }

    public String getSocket() {
        return socket;
    }
}
