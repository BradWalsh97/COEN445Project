package com.coen445.FinalProject;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JSONHelper {
    private Lock lock = new ReentrantLock();
    private String serverName;

    public JSONHelper(String serverName){
        this.serverName = serverName;
    }

    //todo: update the return to return if the creation was successful or not. If not, why did it fail. Return this in the register-denied frame.
    public boolean saveNewUser(User user) throws IOException {
        lock.lock();
        try {
            return writeNewUserToFile(user);
        } finally {
            lock.unlock();
        }

    }

    public void updateUser(User updatedUser) throws IOException {
        lock.lock();
        try {
            //get current user and compare differences
            //first we want to find the user we need to update
            Gson gson = new Gson();
            final Type USER_TYPE = new TypeToken<List<User>>() {
            }.getType();
            JsonReader jsonReader = new JsonReader(new FileReader("users" + serverName + ".json"));
            List<User> users = gson.fromJson(jsonReader, USER_TYPE);


            //for (User user: users) { //consider using binary search to reduce time (need to confirm if the users.json will always be in order based on ID)
            for (int i = 0; i < users.size(); i++) {
                if (updatedUser.getUserName().equalsIgnoreCase(users.get(i).getUserName())) {//if this is the user we want to update
                    //for each var, check what is different starting with the most likely case
                    //only use if because even though it can slow things down its possible that multiple things change
//                    if (!updatedUser.getInterests().equals(users.get(i).getInterests())) { //update interests
//                        users.get(i).setInterests(updatedUser.getInterests());
//                    }
                    if (!updatedUser.getIPAddress().equals(users.get(i).getIPAddress())) { //update ip address
                        users.get(i).setIPAddress(updatedUser.getIPAddress());
                    }
                    if (updatedUser.getSocketNumber() != users.get(i).getSocketNumber()) { //update socket number
                        users.get(i).setSocketNumber(updatedUser.getSocketNumber());
                    }
//                    if (!updatedUser.getUserName().equals(users.get(i).getUserName())) { //todo: this should probably be deprecated
//                        users.get(i).setUserName(updatedUser.getUserName());
//                    }
                    //todo: is it worth updating the rest??? (DONT ALLOW UPDATING USERID!!!)

                    //upon finishing the changes break so we don't have to go through the rest
                    break;
                }
            }
            writeUpdatedUserToFile(users);
        } finally {
            lock.unlock();
        }
    }

    //todo: add a lock when threaded since this writes to a file
    private boolean writeNewUserToFile(User newUser) throws IOException {

        System.out.println("Currently saving user: " + newUser.getUserName());
        /* TODO: 2020-10-17 Change the check to also see if the file is empty instead of the user,
            this is because the person might try to add a new user zero. */


        File newFile = new File("users" + serverName + ".json"); //use this to check if the file is empty

        if (newFile.length() > 0) { //if file not empty:

            //Create objects we will need
            Gson gson = new Gson();
            final Type USER_TYPE = new TypeToken<List<User>>() {}.getType();
            JsonReader jsonReader = new JsonReader(new FileReader("users" + serverName + ".json"));
            List<User> users = gson.fromJson(jsonReader, USER_TYPE); //get all current users
            JsonArray jsonArray = new JsonArray();


            //check for duplicated. If not duplicate, add it to the list of users
            for (User user : users) { //check duplicates
                if (user.getUserName().equalsIgnoreCase(newUser.getUserName())) { //if we have a duplicate (since username is unique)
                    System.out.println("This user already exists, user will not be added!");
                    return false;
                } else {
                    jsonArray.add(gson.toJsonTree(user, User.class));
                }
            }

            //add the new user to the list of users
            jsonArray.add(gson.toJsonTree(newUser, User.class));

            //write the array to the json user file
            FileWriter writer = new FileWriter("users" + serverName + ".json", false);
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.setIndent(" ");
            gson.toJson(jsonArray, jsonWriter);
            writer.close();

            //todo: quicksort based on userID (is this even needed tho???)

        } else { //case of adding first user (where users.json is empty)
            Gson gson = new Gson();
            User[] userList = new User[1];
            userList[0] = newUser;
            FileWriter writer = new FileWriter("users" + serverName + ".json", false);
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.setIndent(" ");
            gson.toJson(userList, User[].class, jsonWriter);

            writer.close();
        }
        return true;
    }

    //todo: add a lock when threaded since this writes to a file
    private void writeUpdatedUserToFile(List<User> users) throws IOException {
        Gson gson = new Gson();
        JsonArray jsonArray = new Gson().toJsonTree(users).getAsJsonArray();

        FileWriter writer = new FileWriter("users" + serverName + ".json", false);
        JsonWriter jsonWriter = new JsonWriter(writer);
        jsonWriter.setIndent(" ");

        gson.toJson(jsonArray, jsonWriter);
        writer.close();
    }

    //checks if user exists. If yes: delete and return true. Else, return false
    public boolean deleteUserWithCheck(String username) throws IOException {
        lock.lock();
        try {
            Gson gson = new Gson();
            final Type USER_TYPE = new TypeToken<List<User>>() {
            }.getType();
            JsonReader jsonReader = new JsonReader(new FileReader("users" + serverName + ".json"));
            List<User> users = gson.fromJson(jsonReader, USER_TYPE);
            JsonArray jsonArray = new JsonArray();
            boolean retVal = false;

            //check every user to see if they match. If they don't, add them to the list of users to keep. If they do, don't add them to the keep list
            for (User user : users) {
                if (!username.equalsIgnoreCase(user.getUserName())) {//only add the users we want to keep to the list to store again
                    jsonArray.add(gson.toJsonTree(user, User.class));
                } else
                    retVal = true;
            }

            //write change to the file
            FileWriter writer = new FileWriter("users" + serverName + ".json", false);
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.setIndent(" ");
            gson.toJson(jsonArray, jsonWriter);
            writer.close();

            return retVal;
        } finally {
            lock.unlock();
        }
    }

    // delete user without verifying that is exists. This is essentially done regardless but we're just not returning
    // anything because this will only be called if we know the user does not exists
    public void deleteUserWithoutCheck(String username) throws IOException{
        lock.lock();
        try {
            Gson gson = new Gson();
            final Type USER_TYPE = new TypeToken<List<User>>() {
            }.getType();
            JsonReader jsonReader = new JsonReader(new FileReader("users" + serverName + ".json"));
            List<User> users = gson.fromJson(jsonReader, USER_TYPE);
            JsonArray jsonArray = new JsonArray();

            //check every user to see if they match. If they don't, add them to the list of users to keep. If they do, don't add them to the keep list
            for (User user : users) {
                if (!username.equalsIgnoreCase(user.getUserName())) {//only add the users we want to keep to the list to store again
                    jsonArray.add(gson.toJsonTree(user, User.class));
                }
            }

            //write change to the file
            FileWriter writer = new FileWriter("users" + serverName + ".json", false);
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.setIndent(" ");
            gson.toJson(jsonArray, jsonWriter);
            writer.close();

        } finally {
            lock.unlock();
        }
    }

    //this should only be called if we know the user exists
    public Optional<User> getUser(String username) throws FileNotFoundException {
        lock.lock();
        try {
            Gson gson = new Gson();
            final Type USER_TYPE = new TypeToken<List<User>>() {
            }.getType();
            JsonReader jsonReader = new JsonReader(new FileReader("users" + serverName + ".json"));
            List<User> users = gson.fromJson(jsonReader, USER_TYPE);
            JsonArray jsonArray = new JsonArray();

            //check every user to see if they match. If they don't, add them to the list of users to keep. If they do, don't add them to the keep list
            for (User user : users) {
                if (username.equalsIgnoreCase(user.getUserName())) {//only add the users we want to keep to the list to store again
                    return Optional.of(user);
                }
            }

            return Optional.empty();
        } finally {
            lock.unlock();
        }
    }

    public ArrayList<User> getAllUsersWithInterest(String interest, String userName){
        lock.lock();
        ArrayList<User> allUsers = new ArrayList<>();
        ArrayList<User> users = new ArrayList<>();
        try {
            Gson gson = new Gson();
            final Type USER_TYPE = new TypeToken<List<User>>() {
            }.getType();
            JsonReader jsonReader = new JsonReader(new FileReader("users" + serverName + ".json"));
            allUsers = gson.fromJson(jsonReader, USER_TYPE);
            for (User user : allUsers) {//for every user
                if(user.getUserName().equalsIgnoreCase(userName))
                    continue;
                for (String userInterest : user.getInterests()) {//and every interest of that user
                    //check to see if they have that interest.
                    if (userInterest.equalsIgnoreCase(interest)) {
                        users.add(user);
                        continue;
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return users;
    }

    //checks if a user exists in the database. returns true if exists. False if not
    public boolean checkIfUserExists(String username) throws FileNotFoundException {
        //assume we don't need a lock because if a user is checking to see if their username exists, they should not
        //at the same time, be adding that user into the database.
        Gson gson = new Gson();
        final Type USER_TYPE = new TypeToken<List<User>>() {}.getType();
        JsonReader jsonReader = new JsonReader(new FileReader("users" + serverName + ".json"));
        List<User> users = gson.fromJson(jsonReader, USER_TYPE); //get all current users
        for (User user : users) { //check duplicates
            if (user.getUserName().equalsIgnoreCase(username)) { //if we have a duplicate (since username is unique)
                return true;
            }
        }
        return false;
    }

    public boolean checkIfUserHasInterest(String username, String interest) throws FileNotFoundException {
        Optional<User> user = getUser(username);
        if(user.isPresent()){
            for(String userInterest: user.get().getInterests()){
                if (userInterest.equalsIgnoreCase(interest))
                    return true;
            }
        }
        return false;
    }

    public boolean updateUserSubjects(String username, ArrayList<String> newInterests ) throws FileNotFoundException {
        if(checkIfUserExists(username)) { //if user exists, make the change. else, return false
            lock.lock();
            try {
                Gson gson = new Gson();
                final Type USER_TYPE = new TypeToken<List<User>>() {
                }.getType();
                JsonReader jsonReader = new JsonReader(new FileReader("users" + serverName + ".json"));
                List<User> users = gson.fromJson(jsonReader, USER_TYPE);
                JsonArray jsonArray = new JsonArray();

                //check every user to see if they match. If they don't, add them to the list of users to keep. If they do, update their interest
                for (User user : users) {
                    if (!username.equalsIgnoreCase(user.getUserName())) {//only add the users we want to keep to the list to store again
                        jsonArray.add(gson.toJsonTree(user, User.class));
                    }
                    else{ //add the new interests to the user and save them again
                        user.setInterests(newInterests);
                        jsonArray.add(gson.toJsonTree(user, User.class));
                    }
                }

                //write change to the file
                FileWriter writer = new FileWriter("users" + serverName + ".json", false);
                JsonWriter jsonWriter = new JsonWriter(writer);
                jsonWriter.setIndent(" ");
                gson.toJson(jsonArray, jsonWriter);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
        else
            return false;

        return true;
    }

    public void userLogOnLogOff(boolean logOn, String username) { //true == user logging on, false == user logging off
        lock.lock();
        try {
            Gson gson = new Gson();
            final Type USER_TYPE = new TypeToken<List<User>>() {
            }.getType();
            JsonReader jsonReader = new JsonReader(new FileReader("users" + serverName + ".json"));
            List<User> users = gson.fromJson(jsonReader, USER_TYPE);
            JsonArray jsonArray = new JsonArray();

            //check every user to see if they match. If they don't, add them to the list of users to keep. If they do, update their interest
            for (User user : users) {
                if (!username.equalsIgnoreCase(user.getUserName())) {
                    jsonArray.add(gson.toJsonTree(user, User.class));
                }
                else{ //toggle login status and save the user again
                    user.setLoggedIn(!user.getLoggedIn());
                    jsonArray.add(gson.toJsonTree(user, User.class));
                }
            }

            //write change to the file
            FileWriter writer = new FileWriter("users" + serverName + ".json", false);
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.setIndent(" ");
            gson.toJson(jsonArray, jsonWriter);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }


}
