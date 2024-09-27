package com.example.newproject.Chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newproject.Chat.Socket.SocketService;
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

public class DetailProfile_to_Chat extends AppCompatActivity {
    TextView tv_friend_name;
    ImageButton ib_back, ib_enter_add, ib_enter_block, ib_option, ib_enter_notblock;
    String mypid, friend_pid, roompid;
    String status; // status를 클래스 필드로 선언
    LinearLayout ll_notblock , ll_isblock, ll_add_friend, ll_video_talk, ll_voice_talk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_profile_to_chat);

        tv_friend_name = findViewById(R.id.tv_friend_name);
        ib_back = findViewById(R.id.ib_back);
        ll_notblock = findViewById(R.id.ll_notblock);
        ll_isblock = findViewById(R.id.ll_isblock);
        ll_add_friend = findViewById(R.id.ll_add_friend);
        ll_video_talk = findViewById(R.id.ll_video_talk);
        ll_voice_talk = findViewById(R.id.ll_voice_talk);
        ib_option = findViewById(R.id.ib_option);
        ib_enter_add = findViewById(R.id.ib_enter_add);
        ib_enter_block = findViewById(R.id.ib_enter_block);
        ib_enter_notblock = findViewById(R.id.ib_enter_notblock);

        Intent intent = getIntent();
        mypid = intent.getStringExtra("mypid");
        friend_pid = intent.getStringExtra("friend_pid");
        roompid = intent.getStringExtra("roompid");

        getData(mypid, friend_pid); // 서버로부터 데이터를 가져옴

        ib_option.setOnClickListener(v -> showPopupMenu(ib_option, friend_pid, status)); // 클릭 시 상태를 반영
        ib_back.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            setResult(RESULT_OK, resultIntent);  // RESULT_OK와 함께 전달

            finish();  // 액티비티 종료
        });
        ib_enter_add.setOnClickListener(v -> handleAction(friend_pid, "return"));
        ib_enter_block.setOnClickListener(v -> handleAction(friend_pid, "block"));
        ib_enter_notblock.setOnClickListener(v -> handleAction(friend_pid, "unblock"));
    }

    private void showPopupMenu(View view, String friend_pid, String status) {
        PopupMenu popupMenu = new PopupMenu(getApplicationContext(), view);
        MenuInflater inflater = popupMenu.getMenuInflater();

        System.out.println(status);

        if (status != null && status.equals("isFriend")) {
            inflater.inflate(R.menu.isfriend_friend_popup_menu, popupMenu.getMenu());
        } else if (status != null && status.equals("isHide")) {
            inflater.inflate(R.menu.hide_friend_popup_menu, popupMenu.getMenu());
        } else {
            // 기본 메뉴를 설정 (혹시 status가 null일 경우)
//            inflater.inflate(R.menu.default_friend_popup_menu, popupMenu.getMenu());
        }

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    handleAction(friend_pid, "delete");
                    return true;
                case R.id.action_unblock:
                    handleAction(friend_pid, "unblock");
                    return true;
                case R.id.action_block:
                    handleAction(friend_pid, "block");
                    return true;
                case R.id.action_return:
                    handleAction(friend_pid, "return");
                    return true;
                case R.id.action_hide:
                    handleAction(friend_pid, "hide");
                    return true;
                default:
                    return false;
            }
        });
    }

    private void handleAction(String friend_pid, String action) {
        String state;
        if ("delete".equals(action)) {
            state = "delete";
        } else if ("unblock".equals(action)) {
            state = "unblock";
            Intent serviceIntent = new Intent(this, SocketService.class);
            serviceIntent.setAction("UnBlock");
            serviceIntent.putExtra("roompid", roompid); // 방 ID
            serviceIntent.putExtra("mypid", mypid);             // 내 ID
            serviceIntent.putExtra("message", "UnBlock");           // 차단해제 메시지
            startService(serviceIntent);
        } else if ("block".equals(action)) {
            state = "isBlock";
            Intent serviceIntent = new Intent(this, SocketService.class);
            serviceIntent.setAction("IsBlock");
            serviceIntent.putExtra("roompid", roompid); // 방 ID
            serviceIntent.putExtra("mypid", mypid);             // 내 ID
            serviceIntent.putExtra("message", "IsBlock");           // 차단 메시지
            startService(serviceIntent);
        } else if ("return".equals(action)) {
            state = "isReturn";
        }else if ("hide".equals(action)) {
            state = "isHide";
        }  else {
            return;
        }

        setData(friend_pid, mypid, state);
    }

    private void setData(String friend_pid, String my_pid, String state) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
            Toast.makeText(getApplicationContext(), "네트워크 연결을 확인하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Set_friends_state.php").newBuilder();
        urlBuilder.addQueryParameter("v", "1.0"); // 예시
        String url = urlBuilder.build().toString();

        RequestBody formBody = new FormBody.Builder()
                .add("my_pid", my_pid)
                .add("f_pid", friend_pid)
                .add("state", state)
                .build();

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
                            Toast.makeText(getApplicationContext(), "네트워크 문제 발생", Toast.LENGTH_SHORT).show();
                        } else {
                            String responseData = response.body().string();
                            JSONObject jsonResponse = new JSONObject(responseData);
                            boolean success = jsonResponse.getBoolean("success");
                            if (success) {
                                Toast.makeText(getApplicationContext(), "상태 변경 완료", Toast.LENGTH_SHORT).show();
                                getData(my_pid, friend_pid);
                            } else {
                                Toast.makeText(getApplicationContext(), "변경 실패", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private void getData(String my_pid, String friend_pid) {
        int networkStatus = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if (networkStatus == NetworkStatus.TYPE_NOT_CONNECTED) {
            Log.e("ChattingActivity", "네트워크 연결을 확인하세요.");
            return;
        }

        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Get_frdstate.php").newBuilder();
        urlBuilder.addQueryParameter("v", "1.0");
        String url = urlBuilder.build().toString();

        RequestBody formBody = new FormBody.Builder()
                .add("pid", my_pid)
                .add("friend_pid", friend_pid)
                .build();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Log.e("DetailProfile_to_Chat", "네트워크 요청 실패"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    try {
                        if (!response.isSuccessful()) {
                            Log.e("DetailProfile_to_Chat", "네트워크 문제 발생");
                        } else {
                            final String responseData = response.body().string();
                            try {
                                JSONObject jsonResponse = new JSONObject(responseData);
                                boolean success = jsonResponse.getBoolean("success");
                                if (success) {
                                    // 서버 응답으로 받은 status 값을 클래스 필드에 저장
                                    status = jsonResponse.getString("friend");
                                    String name = jsonResponse.getString("name");

                                    tv_friend_name.setText(name);

                                    if (status.equals("isBlock")) {
                                        ll_notblock.setVisibility(View.VISIBLE);

                                        ib_option.setVisibility(View.GONE);
                                        ll_isblock.setVisibility(View.GONE);
                                        ll_add_friend.setVisibility(View.GONE);
                                        ll_video_talk.setVisibility(View.GONE);
                                        ll_voice_talk.setVisibility(View.GONE);
                                    } else if (status.equals("notFriend")) {
                                        ll_isblock.setVisibility(View.VISIBLE);
                                        ll_add_friend.setVisibility(View.VISIBLE);

                                        ib_option.setVisibility(View.GONE);
                                        ll_video_talk.setVisibility(View.GONE);
                                        ll_voice_talk.setVisibility(View.GONE);
                                        ll_notblock.setVisibility(View.GONE);
                                    } else if (status.equals("isFriend") || status.equals("isHide")) {
                                        ll_video_talk.setVisibility(View.VISIBLE);
                                        ll_voice_talk.setVisibility(View.VISIBLE);
                                        ib_option.setVisibility(View.VISIBLE);

                                        ll_notblock.setVisibility(View.GONE);
                                        ll_isblock.setVisibility(View.GONE);
                                        ll_add_friend.setVisibility(View.GONE);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e("DetailProfile_to_Chat", "응답 처리 중 오류가 발생했습니다.");
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
