package com.example.newproject.InfoChange_Ac;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.newproject.R;
import com.example.newproject.singup.NetworkStatus;

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

public class NameChange_Activity extends AppCompatActivity {

    EditText edit_text;

    ImageButton clear_button, ib_back;

    Button btn_name_change;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_change);
        edit_text = findViewById(R.id.edit_text);
        clear_button = findViewById(R.id.clear_button);
        ib_back = findViewById(R.id.ib_back);
        btn_name_change = findViewById(R.id.btn_name_change);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String pid = intent.getStringExtra("pid");

        edit_text.setText(name);

        clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_text.setText("");
            }
        });
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        edit_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(!name.equals(edit_text.getText().toString())){
                    btn_name_change.setEnabled(true);
                }else{
                    btn_name_change.setEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        btn_name_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setData(pid , edit_text.getText().toString());
                String newName = edit_text.getText().toString();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("new_name", newName);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    private void setData(String pid, String name) {
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
                .add("name", name)
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