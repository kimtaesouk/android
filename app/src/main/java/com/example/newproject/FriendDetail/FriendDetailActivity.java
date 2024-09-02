package com.example.newproject.FriendDetail;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.newproject.Chat.ChattingActivity;
import com.example.newproject.R;

public class FriendDetailActivity extends AppCompatActivity {

    TextView tv_friend_name;

    ImageButton ib_back, ib_enter_chat;

    String friend_name, friend_pid, my_pid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_detail);

        Intent intent = getIntent();
        friend_name = intent.getStringExtra("friend_name");
        friend_pid = intent.getStringExtra("friend_pid");
        my_pid = intent.getStringExtra("my_pid");

        tv_friend_name = findViewById(R.id.tv_friend_name);
        ib_back = findViewById(R.id.ib_back);
        ib_enter_chat = findViewById(R.id.ib_enter_chat);


        tv_friend_name.setText(friend_name);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ib_enter_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChattingActivity.class);
                intent.putExtra("friend_pid", friend_pid);
                intent.putExtra("my_pid" , my_pid);
                startActivity(intent);
                finish();
            }
        });
    }
}