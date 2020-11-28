package com.coen445.FinalProject;

import com.coen445.FinalProject.Request;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;

public class RQ {

    private int registerCode;
    private int rqNum;
    private String name;
    private String ip;
    private int socketNum;
    private String email;
    private String password;
    private ArrayList<String> subjects = new ArrayList<String>();
    private String text;
    private byte[] message;

    private Request.Register.Builder requestOut = Request.Register.newBuilder();
    private Request.Register requestIn = Request.Register.newBuilder().build();

    //For deserializing a request
    public RQ(Request.Register RQIn) throws InvalidProtocolBufferException {
        this.requestIn = RQIn;

        System.out.println(requestIn);

        this.registerCode = requestIn.getRegisterCode();
        generateRQ();

    }

    public Request.Register getRequestOut(){
        return requestOut.build();
    }

    private void generateRQ(){

        switch(this.registerCode) {
            case 0:

            case 3:

            case 4:

            case 7:

            case 8:
                this.rqNum = requestIn.getRqNum();
                this.name = requestIn.getName();
                this.ip = requestIn.getIp();
                this.socketNum = requestIn.getSocketNum();
                break;

            case 1:
                this.rqNum = requestIn.getRqNum();
                break;

            case 2:

            case 9:
                this.rqNum = requestIn.getRqNum();
                this.text = requestIn.getText();
                break;

            case 5:
                this.rqNum = requestIn.getRqNum();
                this.name = requestIn.getName();
                break;

            case 6:
                this.name = requestIn.getName();
                break;

            case 10:

            case 11:

            case 12:
                this.rqNum = requestIn.getRqNum();
                this.name = requestIn.getName();
                decodeSubjects();
                break;

            case 13:
                this.rqNum = requestIn.getRqNum();
                this.name = requestIn.getName();
                this.text = requestIn.getText();
                decodeSubjects();
                break;

            case 14:
                this.name = requestIn.getName();
                this.text = requestIn.getText();
                decodeSubjects();
                break;

            case 16:

            case 17:
                this.ip = requestIn.getIp();
                this.socketNum = requestIn.getSocketNum();
        }

    }

    //subject fetching method
    private void decodeSubjects(){
        Request.Subjects subjectList = Request.Subjects.newBuilder().build();
        subjectList = requestIn.getSubjects();
        int i = 0;

        while(i < subjectList.getSubjectsCount()){
            subjects.add(subjectList.getSubjects(i).getSubject());
            i++;
        }

        Request.Subject.Builder subject = Request.Subject.newBuilder();
    }

    //For Server Change/Update (16/17)
    public RQ(int registerCode, String ip, int socketNum) {
        this.registerCode = registerCode;
        this.ip = ip;
        this.socketNum = socketNum;

        requestOut.setRegisterCode(registerCode);
        requestOut.setIp(ip);
        requestOut.setSocketNum(socketNum);

        System.out.println(requestOut);

        message = requestOut.build().toByteArray();
    }

    //For user registration confirmation from Server (1)
    public RQ(int registerCode, int rqNum) {
        this.registerCode = registerCode;
        this.rqNum = rqNum;

        requestOut.setRegisterCode(registerCode);
        requestOut.setRqNum(rqNum);

        System.out.println(requestOut);

        message = requestOut.build().toByteArray();
    }

    //For Reqister-Denied/Update-Denied/Publish-Denied response to user and client de-Register (2/5/9/15)
    //TODO conditions for whether the input is a message (like reason) or a NAME
    public RQ(int registerCode, int rqNum, String text) {
        this.registerCode = registerCode;
        this.rqNum = rqNum;
        this.text = text;

        requestOut.setRegisterCode(registerCode);
        requestOut.setRqNum(rqNum);

        if(registerCode == 5){
            requestOut.setName(text);
        } else {
          requestOut.setText(text);
        }

        System.out.println(requestOut);

        message = requestOut.build().toByteArray();
    }

    //For Register, Registered/Register-Denied(server to server), Update, Update-Confirmed(sent to user and b-server) (0/3/4/7/8)
    public RQ(int registerCode, int rqNum, String name, String ip, int socketNum) {
        this.registerCode = registerCode;
        this.rqNum = rqNum;
        this.name = name;
        this.ip = ip;
        this.socketNum = socketNum;

        requestOut.setRegisterCode(registerCode);
        requestOut.setRqNum(rqNum);
        requestOut.setName(name);
        requestOut.setIp(ip);
        requestOut.setSocketNum(socketNum);

        System.out.println(requestOut);

        message = requestOut.build().toByteArray();
    }

    //For de-register info pass to back-up server (6)
    public RQ(int registerCode, String name) {
        this.registerCode = registerCode;
        this.name = name;

        requestOut.setRegisterCode(registerCode);
        requestOut.setName(name);

        System.out.println(requestOut);

        message = requestOut.build().toByteArray();
    }

    //For user adding subjects, subject-update info pass to back-up server & Subjects rejected (10/11/12)
    public RQ(int registerCode, int rqNum, String name, ArrayList <String> subjects) {
        this.registerCode = registerCode;
        this.rqNum = rqNum;
        this.name = name;


        requestOut.setRegisterCode(registerCode);
        requestOut.setRqNum(rqNum);
        requestOut.setName(name);
        requestOut.setSubjects(generateSubjectList(subjects));

        System.out.println(requestOut);

        message = requestOut.build().toByteArray();
    }

    //For Publish a message (13)
    public RQ(int registerCode, int rqNum, String name, ArrayList <String> subjects, String text) {
        this.registerCode = registerCode;
        this.rqNum = rqNum;
        this.name = name;
        this.text = text;

        requestOut.setRegisterCode(registerCode);
        requestOut.setRqNum(rqNum);
        requestOut.setName(name);
        requestOut.setSubjects(generateSubjectList(subjects));
        requestOut.setText(text);

        System.out.println(requestOut);

        message = requestOut.build().toByteArray();

    }

    //For Sharing a message (14)
    public RQ(int registerCode, String name, ArrayList <String> subjects, String text) {
        this.registerCode = registerCode;
        this.name = name;
        this.text = text;

        requestOut.setRegisterCode(registerCode);
        requestOut.setName(name);
        requestOut.setSubjects(generateSubjectList(subjects));
        requestOut.setText(text);

        System.out.println(requestOut);


        message = requestOut.build().toByteArray();
    }


    private Request.Subjects generateSubjectList(ArrayList <String> subs){
        Request.Subjects.Builder subList = Request.Subjects.newBuilder();

        for(String sub : subs){
            Request.Subject.Builder subject = Request.Subject.newBuilder();
            subList.addSubjects(subject.setSubject(sub).build());
        }

        return subList.build();
    }

    public byte[] getMessage() {
        return message;
    }

    public int getRegisterCode() {
        return registerCode;
    }

    public int getRqNum() {
        return rqNum;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public int getSocketNum() {
        return socketNum;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public ArrayList<String> getSubjects() {
        return subjects;
    }

    public String getText() {
        return text;
    }
}
