package com.example.newproject.singup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.newproject.R;

public class Singup_Activity2 extends AppCompatActivity {
    Button btn_next;

    EditText et_pw_signup, et_pwcheck_signup;

    TextView tv_check_pw;

    ImageButton ib_back;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singup2);
        btn_next = findViewById(R.id.btn_next);
        et_pw_signup = findViewById(R.id.et_pw_signup);
        tv_check_pw = findViewById(R.id.tv_check_pw);
        et_pwcheck_signup = findViewById(R.id.et_pwcheck_signup);
        ib_back = findViewById(R.id.ib_back);

        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        System.out.println(email);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        et_pw_signup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String password = s.toString();
                if (isValidPassword(password)) {
                    // 비밀번호 형식이 맞는 경우
                    tv_check_pw.setText("사용가능한 비밀번호 입니다.");
                    tv_check_pw.setTextColor(Color.parseColor("#00FF00"));
                } else {
                    // 비밀번호 형식이 맞지 않는 경우
                    tv_check_pw.setText("비밀번호 형식이 다릅니다.");
                    tv_check_pw.setTextColor(Color.parseColor("#FB0000"));
                }
            }
        });
        et_pwcheck_signup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String pw = et_pw_signup.getText().toString();
                String pwc = s.toString();
                if (pw.equals(pwc)){
                    btn_next.setEnabled(true);
                }else{
                    btn_next.setEnabled(false);
                }
            }
        });
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext() , Singup_Activity3.class);
                intent.putExtra("email" ,  email);
                intent.putExtra("pw" ,  et_pw_signup.getText().toString());
                startActivity(intent);
            }
        });
    }

    private boolean isValidPassword(String password) {
        // 8자 이상, 대문자, 소문자, 숫자, 특수기호 포함
        if (password.length() < 8) {
            return false;
        }
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecialChar = true;
            }
        }

        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
    }

}