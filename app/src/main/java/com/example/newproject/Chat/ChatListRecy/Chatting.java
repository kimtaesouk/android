package com.example.newproject.Chat.ChatListRecy;

import android.graphics.Bitmap;

public class Chatting {
    String pid ;
    String room_pid ;
    String sender_pid;

    String sender_name;
    String msg;
    int count;
    String create;
    int status;
    private String imagePath;  // 이미지 추가


    public Chatting(String pid, String room_pid, String sender_pid, String sender_name, String msg, int count, String create, int status) {
        this.pid = pid;
        this.room_pid = room_pid;
        this.sender_pid = sender_pid;
        this.sender_name = sender_name;
        this.msg = msg;
        this.count = count;
        this.create = create;
        this.status = status;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getRoom_pid() {
        return room_pid;
    }

    public void setRoom_pid(String room_pid) {
        this.room_pid = room_pid;
    }

    public String getSender_pid() {
        return sender_pid;
    }

    public void setSender_pid(String sender_pid) {
        this.sender_pid = sender_pid;
    }

    public String getSender_name() {
        return sender_name;
    }

    public void setSender_name(String sender_name) {
        this.sender_name = sender_name;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getCreate() {
        return create;
    }

    public void setCreate(String create) {
        this.create = create;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
