package com.example.newproject.singup;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.newproject.Login.Login_Activity;
import com.example.newproject.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Singup_Activity3 extends AppCompatActivity {
    Button btn_complet;
    EditText et_name_signup, et_birth_signup;
    ImageButton ib_birth, ib_back;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singup3);
        btn_complet = findViewById(R.id.btn_complet);
        et_name_signup = findViewById(R.id.et_name_signup);
        et_birth_signup = findViewById(R.id.et_birth_signup);
        ib_birth = findViewById(R.id.ib_birth);
        ib_back = findViewById(R.id.ib_back);
        progressBar = findViewById(R.id.cpb);

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        String pw = intent.getStringExtra("pw");

        System.out.println(email);
        System.out.println(pw);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_complet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setData(email, pw, et_name_signup.getText().toString(), et_birth_signup.getText().toString());
            }
        });
        ib_birth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
        et_birth_signup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String name = et_name_signup.getText().toString();
                String birth = et_birth_signup.getText().toString();
                btn_complet.setEnabled(!name.isEmpty() && !birth.isEmpty());
            }
        });
        et_name_signup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    btn_complet.setEnabled(false);
                }
            }
        });

    }

    private void showDatePickerDialog() {
        // 기본 날짜를 현재 날짜로 설정
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // EditText에 이미 날짜가 입력되어 있는 경우, 해당 날짜로 초기화
        String birthDate = et_birth_signup.getText().toString();
        if (!TextUtils.isEmpty(birthDate)) {
            String[] parts = birthDate.split("-");
            if (parts.length == 3) {
                try {
                    year = Integer.parseInt(parts[0]);
                    month = Integer.parseInt(parts[1]) - 1; // 월은 0부터 시작하므로 1을 빼줌
                    day = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    // 날짜 형식이 잘못된 경우 현재 날짜를 사용
                    e.printStackTrace();
                }
            }
        }

        // DatePickerDialog 생성 및 표시
        DatePickerDialog datePickerDialog = new DatePickerDialog(Singup_Activity3.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        // 선택된 날짜를 EditText에 설정 (월은 0부터 시작하므로 1을 더해야 함)
                        et_birth_signup.setText(selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void setData(String email, String pw, String name, String birth) {
        progressBar.setVisibility(View.VISIBLE); // ProgressBar 표시
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
            Toast.makeText(getApplicationContext(), "네트워크 연결을 확인하세요.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE); // ProgressBar 숨기기
            return;
        }

        // get방식 파라미터 추가
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Signup.php").newBuilder();
        urlBuilder.addQueryParameter("v", "1.0"); // 예시
        String url = urlBuilder.build().toString();
        System.out.println(birth);

        // POST 파라미터 추가
        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("pw", pw)
                .add("name", name)
                .add("birth", birth)
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
                        progressBar.setVisibility(View.GONE); // ProgressBar 숨기기
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
                            progressBar.setVisibility(View.GONE); // ProgressBar 숨기기

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
                                        finish(); // 현재 액티비티를 종료합니다
                                    } else {
                                        Toast.makeText(getApplicationContext(), "회원가입에 실패 했습니다.", Toast.LENGTH_SHORT).show();
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
