package com.example.newproject.InfoChange_Ac;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newproject.Login.Login_Activity;
import com.example.newproject.R;
import com.example.newproject.singup.NetworkStatus;
import com.example.newproject.singup.Singup_Activity3;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PwChange_Activity2 extends AppCompatActivity {

    Button btn_next;

    EditText et_pw_findpw, et_pwcheck_findpw;

    TextView tv_check_pw;

    ImageButton ib_back;
    String pid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pw_change2);
        btn_next = findViewById(R.id.btn_next);
        et_pw_findpw = findViewById(R.id.et_pw_findpw);
        tv_check_pw = findViewById(R.id.tv_check_pw);
        et_pwcheck_findpw = findViewById(R.id.et_pwcheck_findpw);
        ib_back = findViewById(R.id.ib_back);

        Intent intent = getIntent();
        pid = intent.getStringExtra("pid");
        System.out.println(pid);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        et_pw_findpw.addTextChangedListener(new TextWatcher() {
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
        et_pwcheck_findpw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String pw = et_pw_findpw.getText().toString();
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
                setData(pid,et_pw_findpw.getText().toString() );
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

    private void setData(String pid, String pw) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
            Toast.makeText(getApplicationContext(), "네트워크 연결을 확인하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // get방식 파라미터 추가
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Change_profile.php").newBuilder();
        urlBuilder.addQueryParameter("v", "1.0"); // 예시
        String url = urlBuilder.build().toString();

        // POST 파라미터 추가
        RequestBody formBody = new FormBody.Builder()
                .add("pid", pid)
                .add("pw", pw)
                .build();

        // 요청 만들기
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "네트워크 요청 실패", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (response.equals(false)) {
                                // 응답 실패
                                Log.i("tag", "응답 실패");
                                Toast.makeText(getApplicationContext(), "네트워크 문제 발생", Toast.LENGTH_SHORT).show();
                            } else {
                                // 응답 성공
                                Log.i("tag", "응답 성공");
                                final String responseData = response.body().string();
                                Log.i("tag", "서버 응답: " + responseData); // 응답 데이터 로그 기록

                                try {
                                    JSONObject jsonResponse = new JSONObject(responseData);
                                    boolean success = jsonResponse.getBoolean("success");

                                    if (success) {
                                        Intent intent = new Intent(getApplicationContext(), Login_Activity.class);
                                        // 모든 기존 액티비티를 제거하고 새로운 루트 액티비티를 시작합니다
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        Toast.makeText(getApplicationContext(), "비밀번호 변경 됐습니다.", Toast.LENGTH_SHORT).show();
                                        finish(); // 현재 액티비티를 종료합니다
                                    } else {
                                        Toast.makeText(getApplicationContext(), "변경 실패 했습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "응답 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}