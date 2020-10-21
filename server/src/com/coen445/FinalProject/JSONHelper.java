package com.coen445.FinalProject;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class JSONHelper {

    //todo: update the return to return if the creation was successful or not. If not, why did it fail. Return this is the register-denied frame.
    public void saveNewUser(User user) throws IOException {
        writeNewUserToFile(user);
    }

    public void updateUser(User updatedUser) throws IOException {
        //get current user and compare differences

        //first we want to find the user we need to update
        Gson gson = new Gson();
        final Type USER_TYPE = new TypeToken<List<User>>(){}.getType();
        JsonReader jsonReader = new JsonReader(new FileReader("users.json"));
        List<User> users = gson.fromJson(jsonReader, USER_TYPE);


        //for (User user: users) { //consider using binary search to reduce time (need to confirm if the users.json will always be in order based on ID)
        for (int i = 0; i < users.size(); i++) {
            if(updatedUser.getUserID() == users.get(i).getUserID()){//if this is the user we want to update
                //for each var, check what is different starting with the most likely case
                //only use if because even though it can slow things down its possible that multiple things change
                if(!updatedUser.getInterests().equals(users.get(i).getInterests())){
                    users.get(i).setInterests(updatedUser.getInterests());
                }
                if(!updatedUser.getIPAddress().equals(users.get(i).getIPAddress())){
                    users.get(i).setIPAddress(updatedUser.getIPAddress());
                }
                if(updatedUser.getSocketNumber() != users.get(i).getSocketNumber()){
                    users.get(i).setSocketNumber(updatedUser.getSocketNumber());
                }
                if(!updatedUser.getUserName().equals(users.get(i).getUserName())){
                    users.get(i).setUserName(updatedUser.getUserName());
                }
                //todo: is it worth updating the rest??? (DONT ALLOW UPDATING USERID!!!)

                //upon finishing the changes break so we don't have to go through the rest
                break;
            }
        }
        writeUpdatedUserToFile(users);
    }

    //todo: add a lock when threaded since this writes to a file
    private void writeNewUserToFile(User newUser) throws IOException{

        System.out.println("Currently saving user: " + newUser.getUserID());
        /* TODO: 2020-10-17 Change the check to also see if the file is empty instead of the user,
            this is because the person might try to add a new user zero. */
        if(newUser.getUserID() > 0) { //only do this if there are currently users in the file

            //Create objects we will need
            Gson gson = new Gson();
            final Type USER_TYPE = new TypeToken<List<User>>(){}.getType();
            JsonReader jsonReader = new JsonReader(new FileReader("users.json"));
            List<User> users = gson.fromJson(jsonReader, USER_TYPE); //get all current users
            JsonArray jsonArray = new JsonArray();


            //check for duplicated. If not duplicate, add it to the list of users
            for (User user: users) { //check duplicates
                if(user.getUserID() == newUser.getUserID()) { //if we have a duplicate (since id is unique)
                    System.out.println("This user already exists, user will not be added!");
                    return;
                }
                else {
                    jsonArray.add(gson.toJsonTree(user, User.class));
                }
            }

            //add the new user to the list of users
            jsonArray.add(gson.toJsonTree(newUser, User.class));

            //write the array to the json user file
            FileWriter writer = new FileWriter("users.json", false);
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.setIndent(" ");
            gson.toJson(jsonArray, jsonWriter);
            writer.close();

            //todo: quicksort based on userID (is this even needed tho???)

        } else{ //case of adding first user (where users.json is empty)
            Gson gson = new Gson();
            User[] userList = new User[1];
            userList[0] = newUser;
            FileWriter writer = new FileWriter("users.json", false);
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.setIndent(" ");
            gson.toJson(userList, User[].class, jsonWriter);

            writer.close();
        }
    }

    //todo: add a lock when threaded since this writes to a file
    private void writeUpdatedUserToFile(List<User> users) throws IOException {
        Gson gson = new Gson();
        JsonArray jsonArray = new Gson().toJsonTree(users).getAsJsonArray();

        FileWriter writer = new FileWriter("users.json", false);
        JsonWriter jsonWriter = new JsonWriter(writer);
        jsonWriter.setIndent(" ");

        gson.toJson(jsonArray, jsonWriter);
        writer.close();
    }


}
