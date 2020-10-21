package com.coen445.FinalProject;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String line = "";
	    Client client = new Client("localhost", 5001);
	    client.connectToServer();

	    while(true) {
            System.out.println("Would you like to register? yes/no");
            line = client.readLine();
            if (!(line.equalsIgnoreCase("yes") || line.equalsIgnoreCase("no"))) {
                System.out.println("Invalid input. Please enter valid input.");
            }else{
                break;
            }
        }

        if(line.equalsIgnoreCase("yes")) {
            client.registerClient();
        }

        client.sendMessage();

	    client.closeConnections();
    }
}
