package com.example.newproject.ChattingRoomListRecy;

public class ChattingRoom {
    String pid ;
    String roomname ;
    String Participants;
    String create;
    int status;
    String last_msg;
    int count;

    public ChattingRoom(String pid, String roomname, String participants, String create, int status, String last_msg, int count) {
        this.pid = pid;
        this.roomname = roomname;
        Participants = participants;
        this.create = create;
        this.status = status;
        this.last_msg = last_msg;
        this.count = count;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getRoomname() {
        return roomname;
    }

    public void setRoomname(String roomname) {
        this.roomname = roomname;
    }

    public String getParticipants() {
        return Participants;
    }

    public void setParticipants(String participants) {
        Participants = participants;
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

    public String getLast_msg() {
        return last_msg;
    }

    public void setLast_msg(String last_msg) {
        this.last_msg = last_msg;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
