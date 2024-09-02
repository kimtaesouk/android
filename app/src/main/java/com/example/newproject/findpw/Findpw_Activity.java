package com.example.newproject.findpw;

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
import com.example.newproject.singup.CheckEmailTask;
import com.example.newproject.singup.Singup_Activity;
import com.example.newproject.singup.Singup_Activity2;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class Findpw_Activity extends AppCompatActivity {
    ImageButton ib_back;
    Button btn_next, btn_findpw_email_confire,  btn_email_certification_check;
    EditText et_email_findpw, et_email_certification;
    TextView tv_check_email, tv_certification_timer;
    LinearLayout ll_email_certification;
    String emailCode = null;

    boolean certification = false;

    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findpw);
        ib_back = findViewById(R.id.ib_back);
        btn_next = findViewById(R.id.btn_next);
        et_email_findpw = findViewById(R.id.et_email_findpw);
        btn_findpw_email_confire = findViewById(R.id.btn_findpw_email_confire);
        tv_check_email = findViewById(R.id.tv_check_email);
        ll_email_certification = findViewById(R.id.ll_email_certification);
        tv_certification_timer = findViewById(R.id.tv_certification_timer);
        btn_email_certification_check = findViewById(R.id.btn_email_certification_check);
        et_email_certification = findViewById(R.id.et_email_certification);

        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = et_email_findpw.getText().toString();
                Intent intent = new Intent(getApplicationContext(), Findpw_Activity2.class);
                intent.putExtra("email" , email);
                startActivity(intent);
            }
        });

        et_email_findpw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkEmailValidity();
            }
        });

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
                        if(success){
                            sendEmail();
                            ll_email_certification.setVisibility(View.VISIBLE);
                            isEmailValid = success;
                        }else{
                            Toast.makeText(Findpw_Activity.this, "사용자가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(Findpw_Activity.this, "인증 완료되었습니다", Toast.LENGTH_SHORT).show();
                    certification = true;
                    stopTimer();
                    btn_next.setEnabled(true);
                } else {
                    et_email_certification.setError("인증번호가 다릅니다");
                }
            }
        });
    }
    private void checkEmailValidity() {
        String email = et_email_findpw.getText().toString().trim();
        if (isValidEmail(email)) {
            tv_check_email.setVisibility(View.VISIBLE);
            tv_check_email.setText("사용가능한 이메일");
            tv_check_email.setTextColor(Color.parseColor("#00FF00"));
            btn_findpw_email_confire.setEnabled(true);
        } else {
            tv_check_email.setVisibility(View.VISIBLE);
            tv_check_email.setText("이메일 형식이 다릅니다.");
            tv_check_email.setTextColor(Color.parseColor("#FB0000"));
        }
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


    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.(com|net)";
        return email.matches(emailPattern);
    }
}