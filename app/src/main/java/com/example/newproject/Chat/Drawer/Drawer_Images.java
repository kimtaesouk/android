package com.example.newproject.Chat.Drawer;

public class Drawer_Images {

    String Image_path;
    String sender_pid;

    public Drawer_Images(String image_path, String sender_pid) {
        Image_path = image_path;
        this.sender_pid = sender_pid;
    }

    public String getImage_path() {
        return Image_path;
    }

    public void setImage_path(String image_path) {
        Image_path = image_path;
    }

    public String getSender_pid() {
        return sender_pid;
    }

    public void setSender_pid(String sender_pid) {
        this.sender_pid = sender_pid;
    }
}
