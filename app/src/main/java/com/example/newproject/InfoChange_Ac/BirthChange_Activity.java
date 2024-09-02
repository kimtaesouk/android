package com.example.newproject.InfoChange_Ac;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.Toast;

import com.example.newproject.R;
import com.example.newproject.singup.NetworkStatus;
import com.example.newproject.singup.Singup_Activity3;

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

public class BirthChange_Activity extends AppCompatActivity {

    EditText et_birth;

    ImageButton ib_back, ib_clander_button;

    Button btn_birth_change;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_birth_change);

        et_birth = findViewById(R.id.et_birth);
        ib_back = findViewById(R.id.ib_back);
        ib_clander_button = findViewById(R.id.ib_clander_button);
        btn_birth_change = findViewById(R.id.btn_birth_change);


        Intent intent = getIntent();
        String birth = intent.getStringExtra("birth");
        String pid = intent.getStringExtra("pid");

        et_birth.setText(birth);

        ib_clander_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        et_birth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!birth.equals(et_birth.getText().toString())){
                    btn_birth_change.setEnabled(true);
                }else{
                    btn_birth_change.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        btn_birth_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setData(pid, et_birth.getText().toString());
                String newBirth = et_birth.getText().toString();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("new_birth", newBirth);
                setResult(RESULT_OK, resultIntent);
                finish();
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
        String birthDate = et_birth.getText().toString();
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
        DatePickerDialog datePickerDialog = new DatePickerDialog(BirthChange_Activity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        // 선택된 날짜를 EditText에 설정 (월은 0부터 시작하므로 1을 더해야 함)
                        et_birth.setText(selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void setData(String pid, String birth) {
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
                                        // 로그인 정보 저장
                                    } else {
                                        Toast.makeText(getApplicationContext(), "데이터 로드 실패 했습니다.", Toast.LENGTH_SHORT).show();
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