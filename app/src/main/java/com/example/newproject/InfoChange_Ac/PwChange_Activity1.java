package com.example.newproject.InfoChange_Ac;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newproject.R;
import com.example.newproject.SendEmail;
import com.example.newproject.findpw.Findpw_Activity;
import com.example.newproject.findpw.Findpw_Activity2;
import com.example.newproject.singup.CheckEmailTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class PwChange_Activity1 extends AppCompatActivity {

    ImageButton ib_back;
    Button btn_next, btn_findpw_email_confire,  btn_email_certification_check;
    EditText et_email_findpw, et_email_certification;
    TextView tv_certification_timer;
    LinearLayout ll_email_certification;
    String emailCode = null;

    boolean certification = false;

    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pw_change1);
        ib_back = findViewById(R.id.ib_back);
        btn_next = findViewById(R.id.btn_next);
        et_email_findpw = findViewById(R.id.et_email_findpw);
        btn_findpw_email_confire = findViewById(R.id.btn_findpw_email_confire);
        ll_email_certification = findViewById(R.id.ll_email_certification);
        tv_certification_timer = findViewById(R.id.tv_certification_timer);
        btn_email_certification_check = findViewById(R.id.btn_email_certification_check);
        et_email_certification = findViewById(R.id.et_email_certification);

        Intent intent = getIntent();
        String pid = intent.getStringExtra("pid");
        String email = intent.getStringExtra("email");


        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PwChange_Activity2.class);
                intent.putExtra("pid" , pid);
                startActivity(intent);
            }
        });

        et_email_findpw.setText(email);

        btn_findpw_email_confire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = et_email_findpw.getText().toString();
                if (!email.isEmpty()) {
                    CheckEmailTask task = new CheckEmailTask(getApplicationContext());
                    boolean isEmailValid = false;
                    try {
                        String response = task.execute(email).get();
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");
                        System.out.println(success);
                        if(!success){
                            sendEmail();
                            ll_email_certification.setVisibility(View.VISIBLE);
                            isEmailValid = success;
                        }else{
                            Toast.makeText(PwChange_Activity1.this, "중복된 사용자입니다.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    et_email_findpw.setError("이메일을 입력하세요");
                }
            }
        });

        btn_email_certification_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String certification_code = et_email_certification.getText().toString();
                if (certification_code.equals(emailCode)) {
                    Toast.makeText(PwChange_Activity1.this, "인증 완료되었습니다", Toast.LENGTH_SHORT).show();
                    certification = true;
                    stopTimer();
                    btn_next.setEnabled(true);
                } else {
                    et_email_certification.setError("인증번호가 다릅니다");
                }
            }
        });
    }

    private void sendEmail() {
        SendEmail mailServer = new SendEmail();
        mailServer.sendSecurityCode(getApplicationContext(), et_email_findpw.getText().toString());
        emailCode = mailServer.emailCode;
        System.out.println(emailCode);
        startTimer(3 * 60 * 1000);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            tv_certification_timer.setText("인증 완료");
            tv_certification_timer.setTextColor(Color.parseColor("#00FF00"));
        }
    }

    private void startTimer(long durationMillis) {
        if (timer != null) {
            timer.cancel();
        }
        timer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 1000 / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                String timeLeft = String.format("%02d:%02d", minutes, seconds);
                tv_certification_timer.setText(timeLeft);
            }

            @Override
            public void onFinish() {
                emailCode = null;
                tv_certification_timer.setText("");
            }
        }.start();
    }


}