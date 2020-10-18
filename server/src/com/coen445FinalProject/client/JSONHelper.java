package com.coen445FinalProject.client;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class JSONHelper {

    //todo: update the return to return if the creation was succesful or not. If not, why did it fail. Return this is the register-denied frame.
    public void saveUserToJSON(User user) throws IOException {

        JsonObject jsonObject = new JsonObject();
        JsonObject innerUserObject = new JsonObject();
        ArrayList<String> interests = user.getInterests();
        JsonArray interestArray = new JsonArray();

        for (String interest: interests) {
            interestArray.add(interest);
        }

        innerUserObject.addProperty("UserName", user.getUserName());
        innerUserObject.addProperty("Password", user.getPassword());
        innerUserObject.add("Interests", interestArray);
        innerUserObject.addProperty("IPAddress", user.getIPAddress());
        innerUserObject.addProperty("SocketNumber", user.getSocketNumber());
        jsonObject.add(Integer.toString(user.getUserID()), innerUserObject);

        writeNewUserToFile(jsonObject, user);
    }
    public void updateUserToJSON(User user){
        //get current user and compare differences
    }

    //private void writeNewUserToFile(JsonObject newUserJson, int ID) throws IOException {
    //private void writeNewUserToFile(User user) throws IOException {
    //todo: add a lock when threaded since this writes to a file
    private void writeNewUserToFile(JsonObject newUserJson, User user) throws IOException{

        System.out.println("Currently saving user: " + user.getUserID());
        // TODO: 2020-10-17 Change the check to also see if the file is empty instead of the user
        // this is because the person might try to add a new user zero.
        if(user.getUserID() > 0) { //only do this if there are currently users in the file

            JsonParser parser = new JsonParser();
            Object obj = parser.parse(new FileReader("users.json"));
            JsonObject currentUsersJsonObject = (JsonObject) obj;
            System.out.println("Current users.json: " + currentUsersJsonObject);

            //check if user already exists while also adding users to the user list
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            List<Map.Entry<String, JsonElement>> users = new ArrayList<Map.Entry<String, JsonElement>>();
            final JsonObject currentUsers = gson.toJsonTree(currentUsersJsonObject).getAsJsonObject();
            for(Map.Entry<String, JsonElement> entry : currentUsers.entrySet()) {
                if(entry.getKey().equals(user.getUserID())){ //todo: for update just copy this stuff and replace the contents of the if with the update. For delete just set isDeleted to true.
                    System.out.println("User already Exists! User will not be added!");
                    return;
                }
            }


            JsonArray jArray = new JsonArray();
            jArray.add(currentUsers);
            jArray.add(newUserJson);
            //write all users including new user to list
            FileWriter writer = new FileWriter("users.json", false);
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.setIndent(" ");
            //System.out.println(gson.toJson(jArray));
            gson.toJson(jArray, jsonWriter);
            writer.close();

            //todo: quicksort based on userID

        } else{ //case of adding first user (where users.json is empty)
            FileWriter writer = new FileWriter("users.json", false);
            JsonWriter jsonWriter = new JsonWriter(writer);
            writer.write(newUserJson.toString());
            writer.close();
        }
    }

    private void updateUserToFile(JsonObject json){

    }

}
