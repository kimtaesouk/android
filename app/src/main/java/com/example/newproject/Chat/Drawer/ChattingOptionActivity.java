package com.example.newproject.Chat.Drawer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;

import com.example.newproject.Chat.ChatListRecy.Chatting;
import com.example.newproject.Chat.ChattingActivity;
import com.example.newproject.R;
import com.example.newproject.singup.NetworkStatus;
import com.google.gson.Gson;

import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChattingOptionActivity extends AppCompatActivity {

    RecyclerView rv_drawer_image, rv_chat_user_list;
    ParticipantsAdapter participantsAdapter;

    DrawerImagesAdapter drawerImagesAdapter;

    ImageButton ib_exit;

    String roomname, my_pid, chattingroom_pid;

    ArrayList<String> friend_pids = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting_option);

        rv_drawer_image = findViewById(R.id.rv_drawer_image);
        rv_chat_user_list = findViewById(R.id.rv_chat_user_list);
        ib_exit = findViewById(R.id.ib_exit);


        rv_chat_user_list.setLayoutManager(new LinearLayoutManager(this));
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rv_drawer_image.setLayoutManager(horizontalLayoutManager);
        Intent intent = getIntent();
        roomname = intent.getStringExtra("roomname");
        my_pid = intent.getStringExtra("my_pid");
        friend_pids = intent.getStringArrayListExtra("friend_pids");
        chattingroom_pid = intent.getStringExtra("chattingroom_pid");

        ib_exit.setOnClickListener( v -> ib_exitclick(my_pid , chattingroom_pid));
        getparticipantsData(my_pid , friend_pids);
        getImageData(chattingroom_pid);
    }

    private void ib_exitclick(String my_pid, String chattingroom_pid){
        updateRoomExit(my_pid, chattingroom_pid);

    }
    private void updateRoomExit(String my_pid, String chattingroom_pid) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        String friendPidsString = TextUtils.join(",", friend_pids);
        if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
            Log.e("ChattingActivity", "네트워크 연결을 확인하세요.");
            return;
        }

        // get방식 파라미터 추가
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Update_roomExit.php").newBuilder();
        urlBuilder.addQueryParameter("v", "1.0"); // 예시
        String url = urlBuilder.build().toString();

        System.out.println(friendPidsString);

        // POST 파라미터 추가
        RequestBody formBody = new FormBody.Builder()
                .add("pid", my_pid)
                .add("chattingroom_pid", chattingroom_pid) // Corrected key to match PHP code
                .add("friend_pids" ,  friendPidsString)
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
                            Log.i("ChattingActivity", "1111응답 성공");
                            final String responseData = response.body().string();
                            Log.i("ChattingActivity", "서버 응답: " + responseData);

                            try {
                                JSONObject jsonResponse = new JSONObject(responseData);

                                Intent returnIntent = new Intent();
                                setResult(Activity.RESULT_OK, returnIntent);
                                finish();
                                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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



    private void getparticipantsData(String my_pid, ArrayList<String> friend_pids) {
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

    private void getImageData(String room_pid) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
            Log.e("ChattingActivity", "네트워크 연결을 확인하세요.");
            return;
        }

        // get방식 파라미터 추가
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Get_Chatroom_Image.php").newBuilder();
        urlBuilder.addQueryParameter("v", "1.0"); // 예시
        String url = urlBuilder.build().toString();


        // POST 파라미터 추가
        RequestBody formBody = new FormBody.Builder()
                .add("room_pid", room_pid)
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

                            try {
                                JSONObject jsonResponse = new JSONObject(responseData);
                                boolean success = jsonResponse.getBoolean("success");

                                List<Drawer_Images> Images = new ArrayList<>();
                                if (success) {
                                    JSONObject data = jsonResponse.getJSONObject("data");
                                    JSONArray images = data.getJSONArray("images");


                                    for (int i = 0; i < images.length(); i++) {
                                        JSONObject friend = images.getJSONObject(i);
                                        String image_path = friend.getString("image_path");
                                        String sender_pid = friend.getString("sender_pid");
                                        // 친구 정보 사용
                                        Images.add(new Drawer_Images(image_path, sender_pid));
                                    }

                                    drawerImagesAdapter = new DrawerImagesAdapter(Images);
                                    rv_drawer_image.setAdapter(drawerImagesAdapter);
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // 특정 뷰의 경계를 가져옵니다. 예를 들어, activity_chatting_option.xml의 루트 레이아웃
        View mainView = findViewById(R.id.ll_Drawer); // main_layout은 activity_chatting_option.xml의 루트 레이아웃 ID입니다.
        Rect viewRect = new Rect();
        mainView.getGlobalVisibleRect(viewRect);

        // 터치 이벤트가 뷰의 경계 밖에서 발생했는지 확인합니다.
        if (!viewRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
            finish(); // 액티비티 종료
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }




}
