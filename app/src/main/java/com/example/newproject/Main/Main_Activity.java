package com.example.newproject.Main;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.newproject.Chat.Socket.SocketService;
import com.example.newproject.ChattingRoomListRecy.ChattingRoom;
import com.example.newproject.R;
import com.example.newproject.fragment.ChatFragment;
import com.example.newproject.fragment.HomeFragment;
import com.example.newproject.fragment.SettingFragment;
import com.example.newproject.singup.NetworkStatus;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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

public class Main_Activity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;

    Fragment fragment_home, fragment_chat, fragment_setting;
    String pid;

    private ArrayList<String> chatroomsList = new ArrayList<>();
    private static final String SELECTED_ITEM_ID = "selected_item_id";
    private static final String CURRENT_FRAGMENT_TAG = "current_fragment";
    private static final String SHARED_PREFS_NAME = "fragmentPrefs";
    private static final String LAST_FRAGMENT_TAG = "lastFragmentTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        pid = intent.getStringExtra("pid");

        startSocketService();


        fragment_home = new HomeFragment();
        fragment_chat = new ChatFragment();
        fragment_setting = new SettingFragment();

        Bundle bundle = new Bundle();
        bundle.putString("pid", pid);
        fragment_home.setArguments(bundle);
        fragment_chat.setArguments(bundle);
        fragment_setting.setArguments(bundle);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                String fragmentTag = null;

                switch (item.getItemId()) {
                    case R.id.home:
                        Log.i(TAG, "home 들어옴");
                        selectedFragment = fragment_home;
                        fragmentTag = "HOME_FRAGMENT";
                        break;
                    case R.id.chat:
                        Log.i(TAG, "chat 들어옴");
                        selectedFragment = fragment_chat;
                        fragmentTag = "CHAT_FRAGMENT";
                        break;
                    case R.id.profile:
                        Log.i(TAG, "profile 들어옴");
                        selectedFragment = fragment_setting;
                        fragmentTag = "PROFILE_FRAGMENT";
                        break;
                }

                if (selectedFragment != null && fragmentTag != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment, fragmentTag).commitNowAllowingStateLoss();
                    saveLastFragmentTag(fragmentTag);  // 현재 프래그먼트 태그 저장
                }

                return true;
            }
        });



        // 상태 복원
        if (savedInstanceState != null) {
            int selectedItemId = savedInstanceState.getInt(SELECTED_ITEM_ID);
            bottomNavigationView.setSelectedItemId(selectedItemId);

            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment currentFragment = fragmentManager.findFragmentByTag(savedInstanceState.getString(CURRENT_FRAGMENT_TAG));
            if (currentFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, currentFragment).commitNowAllowingStateLoss();
            }
        } else {
            // SharedPreferences에서 마지막 프래그먼트 태그 가져오기
            String lastFragmentTag = getLastFragmentTag();
            if (lastFragmentTag != null) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment lastFragment = fragmentManager.findFragmentByTag(lastFragmentTag);
                if (lastFragment == null) {
                    switch (lastFragmentTag) {
                        case "HOME_FRAGMENT":
                            lastFragment = fragment_home;
                            bottomNavigationView.setSelectedItemId(R.id.home);
                            break;
                        case "CHAT_FRAGMENT":
                            lastFragment = fragment_chat;
                            bottomNavigationView.setSelectedItemId(R.id.chat);
                            break;
                        case "PROFILE_FRAGMENT":
                            lastFragment = fragment_setting;
                            bottomNavigationView.setSelectedItemId(R.id.profile);
                            break;
                    }
                }
                if (lastFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, lastFragment, lastFragmentTag).commitNowAllowingStateLoss();
                }
            } else {
                // 초기 화면 설정
                bottomNavigationView.setSelectedItemId(R.id.home);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment_home, "HOME_FRAGMENT").commitNowAllowingStateLoss();
            }
        }
    }


    private void startSocketService() {
        IntentFilter filter = new IntentFilter("com.example.NewProject.NEW_MESSAGE");
        registerReceiver(messageReceiver, filter);
        // Intent를 통해 SocketService 시작
        Intent serviceIntent = new Intent(this, SocketService.class);
        serviceIntent.setAction("SOCKET_OPEN");
        serviceIntent.putExtra("mypid", pid); // 유저 PID (필요에 따라 수정)
        startService(serviceIntent); // 서비스 시작
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            System.out.println("onReceive");
            if ("com.example.NewProject.NEW_MESSAGE".equals(action)) {
                String msg = intent.getStringExtra("message").trim();
                // 메시지를 RecyclerView에 추가하는 로직
                System.out.println("BroadcastReceiver : " + msg);
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();

        // SharedPreferences에서 프래그먼트 열림 여부 확인
        SharedPreferences prefs = getSharedPreferences("fragmentPrefs", MODE_PRIVATE);
        boolean shouldOpenChatFragment = prefs.getBoolean("shouldOpenChatFragment", false);

        if (shouldOpenChatFragment) {
            // 프래그먼트를 열어야 할 때만
            openChatFragment();

            // 다시 프래그먼트를 열지 않도록 플래그 초기화
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("shouldOpenChatFragment", false);
            editor.apply();
        }
    }


    private void openChatFragment() {
        // 기존의 ChatFragment 인스턴스를 가져옴
        FragmentManager fragmentManager = getSupportFragmentManager();
        ChatFragment chatFragment = (ChatFragment) fragmentManager.findFragmentByTag("CHAT_FRAGMENT");

        if (chatFragment == null) {
            // 기존 인스턴스가 없으면 새로 생성하고, Bundle로 pid 전달
            chatFragment = new ChatFragment();
            Bundle bundle = new Bundle();
            bundle.putString("pid", pid); // 전달할 pid 값을 Bundle에 추가
            chatFragment.setArguments(bundle);
        }

        // ChatFragment를 열기 위한 메서드
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, chatFragment, "CHAT_FRAGMENT") // fragment_container에 ChatFragment를 교체하여 표시
                .commit();
        bottomNavigationView.setSelectedItemId(R.id.chat); // R.id.chat는 BottomNavigationView의 chat 탭 ID
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // 새로 전달된 Intent를 저장

        // Intent에서 프래그먼트를 열어야 하는지 확인
        if (intent != null && intent.hasExtra("openFragment")) {
            String openFragment = intent.getStringExtra("openFragment");
            if ("ChatFragment".equals(openFragment)) {
//                openChatFragment(); // ChatFragment를 표시하는 메서드 호출
            }
        }
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_ITEM_ID, bottomNavigationView.getSelectedItemId());
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment != null) {
            outState.putString(CURRENT_FRAGMENT_TAG, currentFragment.getTag());
        }
    }

    private void saveLastFragmentTag(String fragmentTag) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LAST_FRAGMENT_TAG, fragmentTag);
        editor.apply();
    }

    private String getLastFragmentTag() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getString(LAST_FRAGMENT_TAG, null);
    }

    public void changeFragment(Fragment fragment, Bundle bundle) {
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null); // 백스택에 추가하여 백 버튼으로 돌아갈 수 있게 함
        fragmentTransaction.commit();
    }

//    private void getRpid(String pid) {
//        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
//        if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
//            Log.e("ChattingActivity", "네트워크 연결을 확인하세요.");
//            return;
//        }
//
//        // get방식 파라미터 추가
//        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Get_chatroomlist.php").newBuilder();
//        urlBuilder.addQueryParameter("v", "1.0"); // 예시
//        String url = urlBuilder.build().toString();
//
//        // POST 파라미터 추가
//        RequestBody formBody = new FormBody.Builder()
//                .add("pid", pid)
//                .build();
//
//        // 요청 만들기
//        OkHttpClient client = new OkHttpClient();
//        Request request = new Request.Builder()
//                .url(url)
//                .post(formBody)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                e.printStackTrace();
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.e("ChattingActivity", "네트워크 요청 실패");
//                    }
//                });
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            if (!response.isSuccessful()) {
//                                // 응답 실패
//                                Log.e("ChattingActivity", "네트워크 문제 발생");
//                            } else {
//                                // 응답 성공
//                                Log.i("ChattingActivity", "응답 성공");
//                                final String responseData = response.body().string();
//                                Log.i("ChattingActivity", "서버 응답: " + responseData); // 응답 데이터 로그 기록
//
//                                try {
//                                    JSONObject jsonResponse = new JSONObject(responseData);
//                                    boolean success = jsonResponse.getBoolean("success");
//                                    if (success){
//                                        JSONArray roomsArray = jsonResponse.getJSONArray("rooms");
//                                        ArrayList<String> roomPids = new ArrayList<>();
//                                        ArrayList<String> roomNames = new ArrayList<>();
//                                        for (int i = 0; i < roomsArray.length(); i++) {
//                                            JSONObject roomObject = roomsArray.getJSONObject(i);
//
//                                            String roomPid = roomObject.getString("pid");
//                                            String roomName = roomObject.getString("roomname");
//
//                                            roomPids.add(roomPid);
//                                            roomNames.add(roomName);
//                                            // 여기서 각 방 정보를 RecyclerView 등에 추가하여 화면에 표시할 수 있습니다.
//                                        }
//                                        startSocketService(roomPids, roomNames);
//                                        System.out.println(roomPids);
//                                    }else{
//                                        Log.e("ChattingActivity", "친구추가 실패");
//                                    }
//
//
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                    Log.e("ChattingActivity", "응답 처리 중 오류가 발생했습니다.");
//                                }
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//            }
//        });
//    }

}
