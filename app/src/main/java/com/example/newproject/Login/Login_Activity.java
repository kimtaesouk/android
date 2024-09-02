package com.example.newproject.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newproject.Main.Main_Activity;
import com.example.newproject.R;
import com.example.newproject.findpw.Findpw_Activity;
import com.example.newproject.singup.NetworkStatus;
import com.example.newproject.singup.Singup_Activity;

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

public class Login_Activity extends AppCompatActivity {

    private static final String SHARED_PREFS_NAME = "loginPrefs";
    private static final String LAST_FRAGMENT_TAG = "lastFragmentTag";

    TextView tv_signup, tv_findpw;
    Button btn_login;
    EditText et_email, et_pw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SharedPreferences에서 로그인 정보 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        String savedEmail = sharedPreferences.getString("email", null);
        String savedPassword = sharedPreferences.getString("password", null);

        if (savedEmail != null && savedPassword != null) {
            // 자동 로그인 시도
            attemptLogin(savedEmail, savedPassword, true);
        } else {
            // 로그인 정보가 없으면 로그인 화면을 설정
            setLoginView();
        }
    }

    private void setLoginView() {
        setContentView(R.layout.activity_login);

        tv_signup = findViewById(R.id.tv_signup);
        tv_findpw = findViewById(R.id.tv_findpw);
        btn_login = findViewById(R.id.btn_login);
        et_email = findViewById(R.id.et_email);
        et_pw = findViewById(R.id.et_pw);

        tv_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Singup_Activity.class);
                startActivity(intent);
            }
        });

        tv_findpw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Findpw_Activity.class);
                startActivity(intent);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = et_email.getText().toString();
                String password = et_pw.getText().toString();
                attemptLogin(email, password, false);
            }
        });
    }

    private void attemptLogin(final String email, final String pw, final boolean isAutoLogin) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
            Toast.makeText(getApplicationContext(), "네트워크 연결을 확인하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // get방식 파라미터 추가
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Login.php").newBuilder();
        urlBuilder.addQueryParameter("v", "1.0"); // 예시
        String url = urlBuilder.build().toString();

        // POST 파라미터 추가
        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
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
                            if (!response.isSuccessful()) {
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
                                        String pid = jsonResponse.getString("pid");
                                        System.out.println(pid);
                                        // 로그인 정보 저장
                                        if (!isAutoLogin) {
                                            saveLoginInfo(email, pw);
                                        }

                                        Intent intent = new Intent(getApplicationContext(), Main_Activity.class);
                                        intent.putExtra("pid", pid);
                                        startActivity(intent);
                                        finish(); // 로그인 화면 종료
                                    } else {
                                        // 자동 로그인 실패 시 SharedPreferences에 저장된 로그인 정보 삭제 및 직접 로그인 시도
                                        if (isAutoLogin) {
                                            clearLoginInfo();
                                        }
                                        setLoginView();
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


    private void saveLoginInfo(String email, String pw) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.putString("password", pw);
        editor.apply();
    }

    private void clearLoginInfo() {
        // loginPrefs SharedPreferences에서 이메일과 비밀번호 삭제
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("email");
        editor.remove("password");
        editor.apply();

        // fragmentPrefs SharedPreferences에서 LAST_FRAGMENT_TAG 삭제
        SharedPreferences fragmentSharedPreferences = getSharedPreferences("fragmentPrefs", MODE_PRIVATE);
        SharedPreferences.Editor fragmentEditor = fragmentSharedPreferences.edit();
        fragmentEditor.remove(LAST_FRAGMENT_TAG);
        fragmentEditor.apply();
    }

}
