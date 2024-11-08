package com.example.newproject.Chat.AddChatroom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.newproject.Chat.ChattingActivity;
import com.example.newproject.Chat.Socket.SocketService;
import com.example.newproject.FriendsListRecy.Friends;
import com.example.newproject.R;
import com.example.newproject.singup.NetworkStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddChattingRoomActivity extends AppCompatActivity {
    Button btn_complet;
    ImageButton ib_back;
    AddChatroomFriendsAdapter addChatroomFriendsAdapter;
    AddChatroomFriendsAdapter2 addChatroomFriendsAdapter2;
    ArrayList<Friends> friendsList = new ArrayList<>();
    RecyclerView rv_friend_list, rv_add_friend_list;
    String my_pid, chattingroom_pid, roomname = null;
    ArrayList<Friends> checkfriendsList = new ArrayList<>();
    ArrayList<String> friend_pids = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chatting_room);

        rv_friend_list = findViewById(R.id.rv_friend_list);
        btn_complet = findViewById(R.id.btn_complet);
        rv_add_friend_list = findViewById(R.id.rv_add_friend_list);
        ib_back = findViewById(R.id.ib_back);

        Intent intent = getIntent();
        my_pid = intent.getStringExtra("my_pid");
        roomname = intent.getStringExtra("roomname");
        if(intent.hasExtra("chattingroom_pid") && intent.hasExtra("friend_pids")){
            chattingroom_pid = intent.getStringExtra("chattingroom_pid");
            friend_pids = intent.getStringArrayListExtra("friend_pids");

            System.out.println("AddChattingRoomActivity" + "/" + chattingroom_pid);
            System.out.println("AddChattingRoomActivity" + "/" + friend_pids);
        }
        getData(my_pid);

        addChatroomFriendsAdapter = new AddChatroomFriendsAdapter(friendsList, my_pid,friend_pids,chattingroom_pid, getApplicationContext(), new AddChatroomFriendsAdapter.FriendsDataListener() {
            @Override
            public void onDataChanged(ArrayList<Friends> checkList, int adapterPosition) {
                // Update friend count TextView
                if (checkList.size() != 0) {
                    checkfriendsList.clear(); // 기존 리스트 초기화 (중복 방지)
                    checkfriendsList.addAll(checkList); // 업데이트된 리스트 추가
                    btn_complet.setEnabled(true);
                    rv_add_friend_list.setVisibility(View.VISIBLE);
                    addChatroomFriendsAdapter2 = new AddChatroomFriendsAdapter2(checkfriendsList, my_pid, getApplicationContext());
                    rv_add_friend_list.setLayoutManager(new LinearLayoutManager(AddChattingRoomActivity.this, LinearLayoutManager.HORIZONTAL, false));
                    rv_add_friend_list.setAdapter(addChatroomFriendsAdapter2);
                    Log.i("AddChattingRoomActivity", "checkfriendsList : " + checkfriendsList);
                }else{
                    btn_complet.setEnabled(false);
                    rv_add_friend_list.setVisibility(View.GONE);
                }
            }
        });
        rv_friend_list.setLayoutManager(new LinearLayoutManager(AddChattingRoomActivity.this));
        rv_friend_list.setAdapter(addChatroomFriendsAdapter);

        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_complet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!intent.hasExtra("chattingroom_pid") && !intent.hasExtra("friend_pids")){
                    btn_complet_chatfrag();
                }else if(intent.hasExtra("chattingroom_pid") && intent.hasExtra("friend_pids")){
                    ArrayList<String> friendPids = new ArrayList<>();
                    ArrayList<String> friendNames = new ArrayList<>();

                    // checkfriendsList에서 각 친구의 pid를 추출하여 friendPids 리스트에 추가
                    for (Friends friend : checkfriendsList) {
                        friendPids.add(friend.getPid());
                        friendNames.add(friend.getName());
                        System.out.println(friend.getName());
                    }
                    setparticepants(chattingroom_pid, friendPids, friendNames);
                }
            }
        });
    }

    private void btn_complet_chatfrag(){
        ArrayList<String> friendPids = new ArrayList<>();

        // checkfriendsList에서 각 친구의 pid를 추출하여 friendPids 리스트에 추가
        for (Friends friend : checkfriendsList) {
            friendPids.add(friend.getPid());
        }

        System.out.println(friendPids);
        // Intent에 pid 리스트 추가
        Intent intent1 = new Intent(getApplicationContext(), ChattingActivity.class);
        intent1.putStringArrayListExtra("friend_pid", friendPids);
        intent1.putExtra("my_pid", my_pid);
        startActivity(intent1);
        finish();

    }
    private void setparticepants(String chattingroom_pid, ArrayList<String> friend_pids, ArrayList<String> friendNames) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        String friendPidsString = TextUtils.join(",", friend_pids);
        String friendNamesString = TextUtils.join(",", friendNames);
        if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
            Toast.makeText(getApplicationContext(), "네트워크 연결을 확인하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // get방식 파라미터 추가
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Get_addcharroom_frdlist.php").newBuilder();
        urlBuilder.addQueryParameter("v", "1.0"); // 예시
        String url = urlBuilder.build().toString();

        // POST 파라미터 추가
        RequestBody formBody = new FormBody.Builder()
                .add("chattingroom_pid", chattingroom_pid)
                .add("friend_pids", friendPidsString)
                .add("pid", my_pid)
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
                                        Intent serviceIntent = new Intent(getApplicationContext(), SocketService.class);
                                        serviceIntent.setAction("JOIN_ROOM");
                                        serviceIntent.putExtra("roompid", chattingroom_pid); // 방 ID
                                        serviceIntent.putExtra("friendNamesString", friendNamesString);        // 방 이름
                                        serviceIntent.putExtra("friendPidsString", friendPidsString);             // 내 ID
                                        serviceIntent.putExtra("message", "JOIN_ROOM");
                                        startService(serviceIntent);


                                        Intent returnIntent = new Intent();
                                        returnIntent.putExtra("friendPidsString", friendPidsString);
                                        setResult(RESULT_OK, returnIntent);
                                        finish(); // 액티비티 종료
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
    private void getData(String pid) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
            Toast.makeText(getApplicationContext(), "네트워크 연결을 확인하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // get방식 파라미터 추가
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Get_addcharroom_frdlist.php").newBuilder();
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

                                    if (success) {
                                        JSONObject user = jsonResponse.getJSONObject("user");
                                        String name = user.getString("name");
                                        String pfIm = user.isNull("pf_im") ? null : user.getString("pf_im");

                                        // JSONArray로 변경
                                        JSONArray friends = user.getJSONArray("friends");
                                        // 친구 목록 초기화
                                        friendsList.clear();
                                        for (int i = 0; i < friends.length(); i++) {
                                            JSONObject friend = friends.getJSONObject(i);
                                            String friendPid = friend.getString("pid");
                                            String friendName = friend.getString("name");
                                            String friendBirth = friend.getString("birth");
                                            String friendPfIm = friend.isNull("pf_im") ? null : friend.getString("pf_im");
                                            // 친구 정보를 리스트에 추가
                                            friendsList.add(new Friends(friendName,friendPid, friendPfIm, friendBirth));
                                        }

                                        System.out.println(friendsList.size());
                                        addChatroomFriendsAdapter.notifyDataSetChanged();
                                        // 프로필 이미지와 친구 목록은 필요에 따라 추가로 설정


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

