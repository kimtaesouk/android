package com.example.newproject.Chat.Drawer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.example.newproject.R;
import com.example.newproject.singup.NetworkStatus;
import com.google.gson.Gson;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChattingOptionActivity extends AppCompatActivity {

    RecyclerView rv_chat_image, rv_chat_user_list;
    ParticipantsAdapter participantsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting_option);

        rv_chat_image = findViewById(R.id.rv_chat_image);
        rv_chat_user_list = findViewById(R.id.rv_chat_user_list);


        rv_chat_user_list.setLayoutManager(new LinearLayoutManager(this));
        Intent intent = getIntent();
        String roomname = intent.getStringExtra("roomname");
        String my_pid = intent.getStringExtra("my_pid");
        ArrayList<String> friend_pids = intent.getStringArrayListExtra("friend_pids");
        String chattingroom_pid = intent.getStringExtra("chattingroom_pid");

        getData(my_pid , friend_pids);
    }

    private void getData(String my_pid, ArrayList<String> friend_pids) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
            Log.e("ChattingActivity", "네트워크 연결을 확인하세요.");
            return;
        }

        // get방식 파라미터 추가
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Get_Participants.php").newBuilder();
        urlBuilder.addQueryParameter("v", "1.0"); // 예시
        String url = urlBuilder.build().toString();

        // Convert friend_pids to JSON string
        String friendPidsJson = new Gson().toJson(friend_pids);

        // POST 파라미터 추가
        RequestBody formBody = new FormBody.Builder()
                .add("pid", my_pid)
                .add("friend_pids", friendPidsJson) // Corrected key to match PHP code
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
                runOnUiThread(() -> Log.e("ChattingActivity", "네트워크 요청 실패"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    try {
                        if (!response.isSuccessful()) {
                            Log.e("ChattingActivity", "네트워크 문제 발생");
                        } else {
                            Log.i("ChattingActivity", "응답 성공");
                            final String responseData = response.body().string();
                            Log.i("ChattingActivity", "서버 응답: " + responseData);

                            List<Participants> ParticipantsList = new ArrayList<>();
                            try {
                                JSONObject jsonResponse = new JSONObject(responseData);

                                boolean success = jsonResponse.getBoolean("success");
                                if (success) {
                                    JSONObject data = jsonResponse.getJSONObject("data");
                                    JSONObject user = data.getJSONObject("user");
                                    JSONArray friends = data.getJSONArray("friends");

                                    // 사용자 정보 처리
                                    String userName = user.getString("name");
                                    String userPid = user.getString("pid");
                                    ParticipantsList.add(new Participants(userName, userPid));

                                    // 친구 목록 처리
                                    for (int i = 0; i < friends.length(); i++) {
                                        JSONObject friend = friends.getJSONObject(i);
                                        String friendName = friend.getString("name");
                                        String friendPid = friend.getString("pid");
                                        // 친구 정보 사용
                                        System.out.println(userPid + userName + "/" + friendPid + friendName);
                                        ParticipantsList.add(new Participants(friendName, friendPid));
                                    }
                                    participantsAdapter = new ParticipantsAdapter(ParticipantsList);
                                    rv_chat_user_list.setAdapter(participantsAdapter);
                                } else {
                                    String message = jsonResponse.getString("message");
                                    Log.e("ChattingActivity", "오류 메시지: " + message);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e("ChattingActivity", "응답 처리 중 오류가 발생했습니다.");
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
