package com.example.newproject.Chat.Drawer;

public class Participants {
        private String name;
        private String pid;

        public Participants(String name, String pid) {
            this.name = name;
            this.pid = pid;
        }

        public String getName() {
            return name;
        }

        public String getPid() {
            return pid;
        }
}
