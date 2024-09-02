package com.example.newproject.AddFriend;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.example.newproject.R;

public class Ch_Activity extends AppCompatActivity {

    LinearLayout ll_add_id;

    ImageButton ib_back;

    String pid;

    private static final int REQUEST_CODE_ADD_FRIEND = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ch);
        ll_add_id = findViewById(R.id.ll_add_id);
        ib_back = findViewById(R.id.ib_back);

        Intent intent = getIntent();
        pid = intent.getStringExtra("pid");

        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ll_add_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddFriend_ID_Activity.class);
                intent.putExtra("pid", pid);
                startActivityForResult(intent, REQUEST_CODE_ADD_FRIEND);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_FRIEND && resultCode == RESULT_OK) {
            // 친구 추가 후 결과를 처리합니다.
            setResult(RESULT_OK);
            finish();
        }
    }
}