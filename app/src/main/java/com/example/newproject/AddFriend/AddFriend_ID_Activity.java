package com.example.newproject.AddFriend;

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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newproject.Chat.ChattingActivity;
import com.example.newproject.FriendsListRecy.Friends;
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

public class AddFriend_ID_Activity extends AppCompatActivity {

    TextView tv_myid, tv_friend_name, tv_isnot, tv_isregistered_fr;

    EditText et_friend_id;

    Button btn_idserch, btn_talk, btn_addfriends, btn_isblock, btn_unblock;

    ImageButton ib_back, clear_button;

    LinearLayout ll_my_id, ll_friend_pf;

    String pid = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend_id);

        tv_myid = findViewById(R.id.tv_myid);
        et_friend_id = findViewById(R.id.et_friend_id);
        btn_idserch = findViewById(R.id.btn_idserch);
        ll_my_id = findViewById(R.id.ll_my_id);
        ll_friend_pf = findViewById(R.id.ll_friend_pf);
        tv_friend_name = findViewById(R.id.tv_friend_name);
        btn_addfriends = findViewById(R.id.btn_addfriends);
        btn_talk = findViewById(R.id.btn_talk);
        tv_isnot = findViewById(R.id.tv_isnot);
        tv_isregistered_fr = findViewById(R.id.tv_isregistered_fr);
        ib_back = findViewById(R.id.ib_back);
        btn_isblock = findViewById(R.id.btn_isblock);
        btn_unblock = findViewById(R.id.btn_unblock);
        clear_button = findViewById(R.id.clear_button);

        Intent intent = getIntent();
        pid = intent.getStringExtra("pid");
        System.out.println(pid);

        if(pid != null){
            getData(pid);
        }

        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_friend_id.setText("");
            }
        });

        et_friend_id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btn_idserch.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_idserch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_my_id.setVisibility(View.GONE);
                getData2(et_friend_id.getText().toString());
            }
        });


    }

    private void onAddFriendClick(String f_pid) {
        // 친구 추가 버튼 클릭 시 실행될 코드
        setData(pid, f_pid);
    }

    private void onUnblockClick(String f_pid) {
        // 차단 해제 버튼 클릭 시 실행될 코드
        setData2(f_pid, pid, "unblock");
    }

    private void onIsblockClick(String f_pid) {
        // 차단 해제 버튼 클릭 시 실행될 코드
        setData2(f_pid, pid, "isBlock");
    }

    private void onTalkClick(String f_pid) {
        // 1:1 대화 버튼 클릭 시 실행될 코드
        Intent intent = new Intent(getApplicationContext(), ChattingActivity.class);
        intent.putExtra("friend_pid", f_pid);
        intent.putExtra("my_pid" , pid);
        startActivity(intent);
        finish();
    }


    private void getData(String pid) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
            Toast.makeText(getApplicationContext(), "네트워크 연결을 확인하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // get방식 파라미터 추가
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Get_addfrd_id.php").newBuilder();
        urlBuilder.addQueryParameter("v", "1.0"); // 예시
        String url = urlBuilder.build().toString();

        // POST 파라미터 추가
        RequestBody formBody = new FormBody.Builder()
                .add("pid", pid)
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
                                    String id = jsonResponse.getString("id");

                                    tv_myid.setText(id);
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

    private void getData2(String id) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
            Toast.makeText(getApplicationContext(), "네트워크 연결을 확인하세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        // get방식 파라미터 추가
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Get_addfrd_friendid.php").newBuilder();
        urlBuilder.addQueryParameter("v", "1.0"); // 예시
        String url = urlBuilder.build().toString();

        // POST 파라미터 추가
        RequestBody formBody = new FormBody.Builder()
                .add("id", id)
                .add("pid", pid)
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
                                    String isfriendly = jsonResponse.getString("friend");
                                    String f_pid = jsonResponse.getString("f_pid");
                                    String f_name = jsonResponse.getString("name");

                                    if (success){
                                        tv_friend_name.setText(f_name);
                                        ll_friend_pf.setVisibility(View.VISIBLE);
                                        if(isfriendly.equals("isfrind")){
                                            btn_talk.setVisibility(View.VISIBLE);
                                            btn_talk.setOnClickListener(v -> onTalkClick(f_pid));
                                            btn_addfriends.setVisibility(View.GONE);
                                            tv_isregistered_fr.setVisibility(View.VISIBLE);
                                            btn_isblock.setVisibility(View.VISIBLE);
                                            btn_isblock.setOnClickListener(v -> onIsblockClick(f_pid));
                                            btn_unblock.setVisibility(View.GONE);

                                            tv_isnot.setVisibility(View.GONE);
                                        }else if(isfriendly.equals("notfriend")){
                                            if(pid.equals(f_pid)){
                                                btn_talk.setVisibility(View.VISIBLE);
                                                btn_talk.setText("나와의 대화");
                                                btn_talk.setTextSize(18);
                                                btn_isblock.setVisibility(View.GONE);
                                                btn_unblock.setVisibility(View.GONE);
                                                btn_addfriends.setVisibility(View.GONE);
                                                btn_addfriends.setOnClickListener(v -> onAddFriendClick(f_pid));
                                                tv_isregistered_fr.setVisibility(View.GONE);

                                                tv_isnot.setVisibility(View.GONE);
                                            }else {
                                                btn_isblock.setVisibility(View.VISIBLE);
                                                btn_isblock.setOnClickListener(v -> onIsblockClick(f_pid));
                                                btn_unblock.setVisibility(View.GONE);
                                                btn_talk.setVisibility(View.GONE);
                                                btn_addfriends.setVisibility(View.VISIBLE);
                                                btn_addfriends.setOnClickListener(v -> onAddFriendClick(f_pid));
                                                tv_isregistered_fr.setVisibility(View.GONE);

                                                tv_isnot.setVisibility(View.GONE);
                                            }
                                        } else if (isfriendly.equals("isHide")) {
                                            btn_isblock.setVisibility(View.VISIBLE);
                                            btn_talk.setVisibility(View.VISIBLE);
                                            btn_addfriends.setVisibility(View.GONE);
                                            btn_unblock.setVisibility(View.GONE);
                                            tv_isregistered_fr.setVisibility(View.GONE);
                                            tv_isregistered_fr.setVisibility(View.VISIBLE);
                                            tv_isnot.setVisibility(View.GONE);
                                        } else if (isfriendly.equals("isBlock")) {
                                            tv_isregistered_fr.setText("차단한 친구 입니다.");
                                            btn_unblock.setVisibility(View.VISIBLE);

                                            btn_talk.setVisibility(View.GONE);
                                            btn_unblock.setOnClickListener(v -> onUnblockClick(f_pid));

                                            btn_addfriends.setVisibility(View.GONE);

                                            tv_isregistered_fr.setVisibility(View.VISIBLE);

                                            btn_isblock.setVisibility(View.GONE);

                                            tv_isnot.setVisibility(View.GONE);

                                        } else if (isfriendly.equals("not_allow")) {
                                            ll_friend_pf.setVisibility(View.GONE);
                                            tv_isnot.setVisibility(View.VISIBLE);
                                        }
                                    }else {
                                        ll_friend_pf.setVisibility(View.GONE);
                                        tv_isnot.setVisibility(View.VISIBLE);
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

    //친구등록이 없다가 처음 친구로 추가 될때
    private void setData(String my_pid, String f_pid) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
            Toast.makeText(getApplicationContext(), "네트워크 연결을 확인하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // get방식 파라미터 추가
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Set_friend.php").newBuilder();
        urlBuilder.addQueryParameter("v", "1.0"); // 예시
        String url = urlBuilder.build().toString();

        // POST 파라미터 추가
        RequestBody formBody = new FormBody.Builder()
                .add("my_pid", my_pid)
                .add("f_pid", f_pid)
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

                                    if (success){
                                        Intent resultIntent = new Intent();
                                        setResult(RESULT_OK, resultIntent);
                                        btn_idserch.performClick();
                                    }else{
                                        Toast.makeText(getApplicationContext(), "친구추가 실패", Toast.LENGTH_SHORT).show();
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

    // 차단 되있던 친구를 다시 친구목록으로 가져옴
    private void setData2(String f_pid, String my_pid, String state) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
            Toast.makeText(getApplicationContext(), "네트워크 연결을 확인하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // GET 방식 파라미터 추가
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Set_friends_state.php").newBuilder();
        urlBuilder.addQueryParameter("v", "1.0"); // 예시
        String url = urlBuilder.build().toString();

        // POST 방식 파라미터 추가
        RequestBody formBody = new FormBody.Builder()
                .add("my_pid", my_pid)
                .add("f_pid", f_pid)
                .add("state", state)
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
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "네트워크 요청 실패", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    try {
                        if (!response.isSuccessful()) {
                            // 응답 실패
                            Log.i("tag", "응답 실패");
                            Toast.makeText(getApplicationContext(), "네트워크 문제 발생", Toast.LENGTH_SHORT).show();
                        } else {
                            // 응답 성공
                            Log.i("tag", "응답 성공");
                            String responseData = response.body().string();
                            Log.i("tag", "서버 응답: " + responseData);

                            try {
                                JSONObject jsonResponse = new JSONObject(responseData);
                                boolean success = jsonResponse.getBoolean("success");

                                if (success) {
                                    Intent resultIntent = new Intent();
                                    setResult(RESULT_OK, resultIntent);
                                    btn_idserch.performClick();
                                } else {
                                    Toast.makeText(getApplicationContext(), "변경 실패", Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "응답 처리 중 오류 발생", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }
}