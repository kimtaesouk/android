package com.example.newproject.FriendsListRecy;

public class Friends {
    String name;
    String pid;
    String profile_image;
    String birth;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public Friends(String name, String pid, String profile_image, String birth) {
        this.name = name;
        this.pid = pid;
        this.profile_image = profile_image;
        this.birth = birth;
    }




}
