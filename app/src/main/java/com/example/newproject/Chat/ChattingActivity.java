package com.example.newproject.Chat;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newproject.Chat.ChatListRecy.Chatting;
import com.example.newproject.Chat.ChatListRecy.ChattingAdapter;
import com.example.newproject.Chat.Socket.SocketService;
import com.example.newproject.Main.Main_Activity;
import com.example.newproject.R;
import com.example.newproject.singup.NetworkStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class ChattingActivity extends AppCompatActivity {
    // 친구의 이름을 표시할 TextView
    TextView tv_friend_name;
    // 채팅 내용을 입력할 EditText
    EditText et_talk;
    ImageButton ib_send_talk, ib_back, ib_room_option, ib_add_file, ib_clear_file, ib_chat_camara;
    LinearLayout ll_friend_add_or_block, ll_block_clear, ll_block, ll_add_friend, ll_add_file;
    // 친구의 이름과 친구의 PID (개인 식별자)
    String friend_pid, my_pid, chattingroom_pid, roomname;
    RecyclerView rv_chat_list;
    ChattingAdapter chattingAdapter;
    ArrayList<Chatting> chatList = new ArrayList<>();
    ArrayList<String> friend_pids = new ArrayList<>();
    HashMap<String, String> pidNameMap = new HashMap<>(); // PID를 키로 하고 이름을 값으로 하는 HashMap
    ArrayList<String> clientList = new ArrayList<>();
    int reader = 0;
    private int keyboardHeight = 0; // 키보드 높이를 저장할 변수
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int CAMERA_INTENT_REQUEST_CODE = 101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);
        // XML 레이아웃에서 UI 요소를 연결
        tv_friend_name = findViewById(R.id.tv_friend_name); // 친구의 이름을 표시하는 TextView
        et_talk = findViewById(R.id.et_talk);               // 채팅 메시지를 입력하는 EditText
        ib_send_talk = findViewById(R.id.ib_send_talk);// 채팅 입력후 전송 버튼 et_talk 작성 시작하면 보이게
        ll_friend_add_or_block = findViewById(R.id.ll_friend_add_or_block);
        ll_block_clear = findViewById(R.id.ll_block_clear);
        ll_block = findViewById(R.id.ll_block);
        ib_back = findViewById(R.id.ib_back);
        ll_add_friend = findViewById(R.id.ll_add_friend);
        rv_chat_list = findViewById(R.id.rv_chat_list);
        ib_room_option = findViewById(R.id.ib_room_option);
        ib_add_file = findViewById(R.id.ib_add_file);
        ll_add_file = findViewById(R.id.ll_add_file);
        ib_clear_file = findViewById(R.id.ib_clear_file);
        ib_chat_camara = findViewById(R.id.ib_chat_camara);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);  // RecyclerView를 항상 하단에 붙게 설정
        rv_chat_list.setLayoutManager(layoutManager);

        // 이전 액티비티에서 전달된 Intent를 받아옴
        Intent intent = getIntent();
        // 먼저 배열 형태로 데이터를 시도
        my_pid = intent.getStringExtra("my_pid"); // 내 PID를 받아옴
        chattingroom_pid = intent.getStringExtra("room_pid");
        roomname = intent.getStringExtra("roomname");
        System.out.println("chattinaActivity chattingroom_pid : " + chattingroom_pid );
        friend_pids = intent.getStringArrayListExtra("friend_pid");
        System.out.println("chattinaActivity friend_pids수 : " + friend_pids.size());
        if(friend_pids.size() == 1 || chattingroom_pid == null){
            // friend_pids 리스트를 JSON 배열 형태의 문자열로 변환하여 전달
            JSONArray jsonArray = new JSONArray(friend_pids);
            String friendPidsString = jsonArray.toString();
            getData(my_pid, friendPidsString);
        } else {
            tv_friend_name.setText(roomname);
            setData4(chattingroom_pid, my_pid, () -> getData2(chattingroom_pid));
        }
        // BroadcastReceiver 등록
        IntentFilter filter = new IntentFilter("com.example.NewProject.NEW_MESSAGE");
        registerReceiver(messageReceiver, filter);
// SocketService 시작
        Intent serviceIntent = new Intent(this, SocketService.class);
        serviceIntent.putExtra("roompid", chattingroom_pid); // 방 ID
        serviceIntent.putExtra("roomname", roomname);        // 방 이름
        serviceIntent.putExtra("mypid", my_pid);             // 내 ID
        serviceIntent.putExtra("message", "입장");           // 입장 메시지
// 서비스가 실행 중이 아니면 시작
        if (!isMyServiceRunning(SocketService.class)) {
            startService(serviceIntent);
        }
        if(friend_pids.size() == 1 || chattingroom_pid == null){
            String friendPidsString = TextUtils.join(",", friend_pids);
            getData(my_pid, friendPidsString);
        } else {
            tv_friend_name.setText(roomname);
            setData4(chattingroom_pid, my_pid, () -> getData2(chattingroom_pid));
            // 새 채팅방에 접속 시 서비스 연결
            connectToSocketService();
        }


        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent(ChattingActivity.this, Main_Activity.class);
                resultIntent.putExtra("openFragment", "ChatFragment"); // ChatFragment를 표시할 것임을 명시
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                // ChattingActivity에서 Main_Activity로 돌아오기 전에
                SharedPreferences prefs = getSharedPreferences("fragmentPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("shouldOpenChatFragment", true);
                editor.apply();
                startActivity(resultIntent);
                try {
                    // 메시지 전송
                    sendMessage(my_pid + "/" + chattingroom_pid + "/" + roomname + "/" + "퇴장");
                    // BroadcastReceiver 해제
                    unregisterReceiver(messageReceiver);
                    setData4(chattingroom_pid, my_pid, () -> {
                        // setData4 완료 후 Activity 종료
                        finish();
                    });
                } catch (IllegalArgumentException e) {
                    Log.e("ChattingActivity", "Receiver not registered", e);
                }
            }
        });

        ib_add_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_talk.hasFocus()) {
                    et_talk.clearFocus();
                    hideKeyboard();
                }

                // 애니메이션을 적용하여 부드럽게 레이아웃 크기 변경
                ll_add_file.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animateLayoutChange(); // 레이아웃 변경 애니메이션
                    }
                }, 300); // 키보드가 사라진 후 잠시 대기한 후 애니메이션 적용

                ib_add_file.setVisibility(View.GONE);
                ll_add_file.setVisibility(View.VISIBLE);
                ib_clear_file.setVisibility(View.VISIBLE);
            }
        });


        ib_clear_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_add_file.setVisibility(View.GONE);
                ib_clear_file.setVisibility(View.GONE);
                ib_add_file.setVisibility(View.VISIBLE);
            }
        });
        ib_room_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext() , ChattingOptionActivity.class);
                startActivity(intent1);
            }
        });

        ib_chat_camara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 카메라 권한이 있는지 확인
                if (ContextCompat.checkSelfPermission(ChattingActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    // 권한이 없으면 권한 요청
                    ActivityCompat.requestPermissions(ChattingActivity.this,
                            new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                } else {
                    // 권한이 있으면 카메라로 이동
                    openCamera();
                }
            }
        });
        ArrayList<String> finalFriend_pids = friend_pids;
        // 키보드가 나타날 때 레이아웃 크기 변화 감지
        final View rootLayout = findViewById(R.id.ll_chat); // 최상위 레이아웃
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootLayout.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootLayout.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                if (keypadHeight > screenHeight * 0.15) {
                    // 키보드가 나타난 경우
                    scrollToBottom();
                }
            }
        });

        // EditText에 포커스가 있을 때도 자동으로 최신 메시지로 스크롤

        // EditText에 TextWatcher를 추가하여 텍스트가 변경될 때마다 처리
        et_talk.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

                    scrollToBottom();
                    ll_add_file.setVisibility(View.GONE);
                    ib_clear_file.setVisibility(View.GONE);

                    ib_add_file.setVisibility(View.VISIBLE);
                }
            }
        });
        et_talk.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 텍스트가 변경되기 전에 호출됨

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 텍스트가 변경되는 중에 호출됨
                if(et_talk.getText().toString().isEmpty()){
                    // et_talk이 비면 ib_send_talk다시 안보이게
                    ib_send_talk.setVisibility(View.GONE);
                }else if(!et_talk.getText().toString().isEmpty()){
                    // et_talk이 입력되면 버튼 보이게
                    ib_send_talk.setVisibility(View.VISIBLE);
                    ib_send_talk.setOnClickListener(v -> onSendTalk(et_talk.getText().toString(), my_pid, finalFriend_pids));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 텍스트가 변경된 후에 호출됨
                int maxWidth = 275; // 텍스트가 표시될 최대 너비를 275dp로 설정
                // dp 단위를 실제 픽셀 값으로 변환
                int editTextWidth = (int) (maxWidth * getResources().getDisplayMetrics().density);

                String text = s.toString(); // 현재 입력된 텍스트를 문자열로 변환
                String[] lines = text.split("\n"); // 텍스트를 줄바꿈 기준으로 나눔
                StringBuilder newText = new StringBuilder(); // 새로운 텍스트를 구성할 StringBuilder

                // 각 줄에 대해 텍스트가 최대 너비를 초과하는지 확인
                for (String line : lines) {
                    while (et_talk.getPaint().measureText(line) > editTextWidth) {
                        // 텍스트가 최대 너비를 초과하면 줄을 분할
                        int breakIndex = et_talk.getPaint().breakText(line, true, editTextWidth, null);
                        newText.append(line.substring(0, breakIndex)).append("\n"); // 줄바꿈을 추가하여 새 텍스트 구성
                        line = line.substring(breakIndex); // 남은 텍스트로 줄을 다시 설정
                    }
                    newText.append(line).append("\n"); // 마지막 줄 추가
                }

                // 텍스트가 변경되었을 경우 EditText를 업데이트
                if (!newText.toString().equals(text)) {
                    et_talk.removeTextChangedListener(this); // 무한 루프를 방지하기 위해 리스너 제거
                    et_talk.setText(newText.toString().trim()); // 새로운 텍스트로 설정
                    et_talk.setSelection(et_talk.getText().length()); // 커서를 텍스트의 끝으로 이동
                    et_talk.addTextChangedListener(this); // 리스너 다시 추가
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 돌아왔을 때 데이터를 다시 가져오고 UI를 새로고침하는 로직을 추가합니다.
        if (friend_pids.size() == 1 || chattingroom_pid == null) {
            String friendPidsString = TextUtils.join(",", friend_pids);
            getData(my_pid, friendPidsString); // 데이터를 다시 가져옴
        } else {
            tv_friend_name.setText(roomname);
            setData4(chattingroom_pid, my_pid, () -> getData2(chattingroom_pid)); // 데이터를 새로고침
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        // BroadcastReceiver 등록
        IntentFilter filter = new IntentFilter("com.example.NewProject.NEW_MESSAGE");
        registerReceiver(messageReceiver, filter);

        // 새 채팅방에 접속 시 서비스 연결
        connectToSocketService();
    }

    // 카메라 앱 실행 메서드
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_INTENT_REQUEST_CODE);
        }
    }

    // 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용되면 카메라 실행
                openCamera();
            } else {
                // 권한이 거부되면 사용자에게 메시지 표시
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void animateLayoutChange() {
        ll_add_file.animate()
                .translationY(0)  // 위로 올라오는 애니메이션
                .alpha(1.0f)  // 투명도 적용 (페이드 인)
                .setDuration(300)  // 300ms 동안 애니메이션 실행
                .start();
    }


    // 키보드를 숨기는 메서드
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // 키보드 높이에 맞게 ll_add_file 레이아웃을 조정
    private void adjustLayoutForKeyboard() {
        if (keyboardHeight > 0) { // 키보드가 열려 있을 때만 조정
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ll_add_file.getLayoutParams();
            params.height = keyboardHeight; // 키보드 높이만큼 설정
            ll_add_file.setLayoutParams(params);
        }
    }

    private void scrollToBottom() {
        if (chattingAdapter != null && chattingAdapter.getItemCount() > 0) {
            rv_chat_list.scrollToPosition(chattingAdapter.getItemCount() - 1);
        }
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }
    private void connectToSocketService() {
        Intent serviceIntent = new Intent(this, SocketService.class);
        serviceIntent.setAction("JOIN_ROOM");
        serviceIntent.putExtra("roompid", chattingroom_pid); // 방 ID
        serviceIntent.putExtra("roomname", roomname);        // 방 이름
        serviceIntent.putExtra("mypid", my_pid);             // 내 ID
        serviceIntent.putExtra("message", "입장");

        // 서비스가 실행 중인지 확인하고, 실행 중이면 서비스 재시작 없이 명령만 보냄
        if (!isMyServiceRunning(SocketService.class)) {
            // 서비스가 실행 중이 아니면 처음 실행
            startService(serviceIntent);
            Log.d("ChattingActivity", "Service started with room PID: " + chattingroom_pid);
        } else {
            // 서비스가 이미 실행 중이면 브로드캐스트로 새로운 방 입장 명령 전송
            sendBroadcast(serviceIntent);
            Log.d("ChattingActivity", "Service already running, sent JOIN_ROOM broadcast.");
        }
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
                addMessageToRecyclerView(msg);
            }
        }
    };

    private void addMessageToRecyclerView(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String[] parts = new String[0];
                try {
                    // 메시지 파싱
                    parts = message.split(":");
                    String senderId = parts[0].trim();
                    String roomId = parts[1].trim();
                    String msg = parts.length > 3 ? parts[3].trim() : "";
                    // 클라이언트 리스트 (콤마로 구분된 문자열)
                    String clients = parts.length > 4 ? parts[4].trim() : "";

                    clientList = new ArrayList<>(List.of(clients.split(",")));

                    // my_pid와 일치하는 항목을 제외


                    // 클라이언트 수를 기반으로 reader 수 계산
                    reader =  friend_pids.size() + 1  - clientList.size() ;
                    System.out.println("addMessageToRecyclerView reader : " + reader );
                    System.out.println("addMessageToRecyclerView clientList : " + clientList.size() );

                    // 현재 채팅방에 해당하는 메시지인지 확인
                    if (roomId.equals(chattingroom_pid) && !senderId.equals(my_pid)) {
                        // 입장 메시지 처리
                        if (msg.equals("입장")) {
                            System.out.println("message senderId : " + senderId);
                            System.out.println("addMessageToRecyclerView 입장 friend_pids수 : " + friend_pids.size());

                            // 모든 메시지의 reader_count 업데이트
                            for (Chatting chat : chatList) {
                                if (chat.getCount() != 0) {
                                    chat.setCount(reader);
                                }
                            }

                            // 어댑터 갱신 및 RecyclerView 새로고침
                            chattingAdapter = new ChattingAdapter(chatList, getApplicationContext(), my_pid);
                            rv_chat_list.setAdapter(chattingAdapter);
                            chattingAdapter.notifyDataSetChanged();

                            // 퇴장 메시지 처리
                        } else if (msg.equals("퇴장")) {
                            System.out.println("addMessageToRecyclerView clientList 퇴장 : " + clientList.size() );
                            // 일반 메시지 처리
                        } else {
                            // pidNameMap에서 senderId에 해당하는 이름을 찾음
                            String senderName = pidNameMap.getOrDefault(senderId, "Unknown");
                            System.out.println("addMessageToRecyclerView" + msg);

//                            num = friend_pids.size() + 1;

                            // 새로운 채팅 메시지 생성 및 추가
                            Chatting chatMessage = new Chatting("0", chattingroom_pid, senderId, senderName, msg, reader, getCurrentTime(), 1);
                            chatList.add(chatMessage);

                            // 어댑터 갱신 및 RecyclerView에 메시지 추가
                            chattingAdapter = new ChattingAdapter(chatList, getApplicationContext(), my_pid);
                            rv_chat_list.setAdapter(chattingAdapter);
                            chattingAdapter.notifyItemInserted(chatList.size() - 1);

                            // RecyclerView를 최신 메시지 위치로 스크롤
                            rv_chat_list.post(() -> rv_chat_list.scrollToPosition(chatList.size() - 1));
                        }
                    }
                } catch (NumberFormatException e) {
                    Log.e("Chatting_Activity", "Invalid number format in message: " + parts[4]);
                }
            }
        });
    }

    private String getCurrentTime() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return now.format(formatter);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return sdf.format(new Date());
        }
    }


    private void getData(String my_pid, String friend_pid) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
            Log.e("ChattingActivity", "네트워크 연결을 확인하세요.");
            return;
        }

        System.out.println("getData 에서 넘길 friend_pid :" + friend_pid);

        // get방식 파라미터 추가
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Get_chatroompid.php").newBuilder();
        urlBuilder.addQueryParameter("v", "1.0"); // 예시
        String url = urlBuilder.build().toString();

        // POST 파라미터 추가
        RequestBody formBody = new FormBody.Builder()
                .add("pid", my_pid)
                .add("friend_pid", String.valueOf(friend_pids))
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
                        Log.e("ChattingActivity", "네트워크 요청 실패");
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
                                Log.e("ChattingActivity", "네트워크 문제 발생");
                            } else {
                                // 응답 성공
                                Log.i("ChattingActivity", "응답 성공");
                                final String responseData = response.body().string();
                                Log.i("ChattingActivity", "서버 응답: " + responseData); // 응답 데이터 로그 기록

                                try {
                                    JSONObject jsonResponse = new JSONObject(responseData);
                                    String success = jsonResponse.getString("success");
                                    roomname = jsonResponse.getString("roomname");
                                    chattingroom_pid = jsonResponse.getString("chatting_room_pid");
                                    System.out.println("getData 에서 받아온 chattingroom_pid : " + chattingroom_pid);
                                    tv_friend_name.setText(roomname);
                                    setData4(chattingroom_pid, my_pid, () -> getData2(chattingroom_pid));

                                    if (success.equals("isBlock")){
                                        ll_friend_add_or_block.setVisibility(View.VISIBLE);

                                        ll_block_clear.setVisibility(View.VISIBLE);
                                        ll_block_clear.setOnClickListener(v -> onUnblockClick(friend_pid));

                                        ll_block.setVisibility(View.GONE);

                                        ll_add_friend.setVisibility(View.GONE);

                                        et_talk.setText("차단 친구와는 대화가 불가능합니다.");
                                        et_talk.setEnabled(false); // et_talk를 비활성화 (회색으로 표시되고 클릭 불가)
                                        et_talk.setFocusable(false); // et_talk를 포커스 불가능하게 설정
                                        et_talk.setFocusableInTouchMode(false); // 터치로도 포커스할 수 없게 설정
                                    } else if (success.equals("true")) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ll_friend_add_or_block.setVisibility(View.GONE);

                                                et_talk.setText("");
                                                et_talk.setEnabled(true); // et_talk를 활성화
                                                et_talk.setFocusable(true); // et_talk를 포커스 가능하게 설정
                                                et_talk.setFocusableInTouchMode(true); // 터치로도 포커스할 수 있게 설정
                                            }
                                        });
                                    }
                                    else if (success.equals("false")) {
                                        ll_friend_add_or_block.setVisibility(View.VISIBLE);

                                        ll_block_clear.setVisibility(View.GONE);

                                        ll_block.setVisibility(View.VISIBLE);
                                        ll_block.setOnClickListener(v -> onIsblockClick(friend_pid));

                                        ll_add_friend.setVisibility(View.VISIBLE);
                                        ll_add_friend.setOnClickListener(v -> onAddFriendClick(friend_pid));

                                        et_talk.setText("");
                                        et_talk.setEnabled(true); // et_talk를 활성화
                                        et_talk.setFocusable(true); // et_talk를 포커스 가능하게 설정
                                        et_talk.setFocusableInTouchMode(true); // 터치로도 포커스할 수 있게 설정
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.e("ChattingActivity", "응답 처리 중 오류가 발생했습니다.");
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

    private void onAddFriendClick(String f_pid) {
        // 친구 추가 버튼 클릭 시 실행될 코드
        setData(my_pid, friend_pids.get(0));
    }

    private void onUnblockClick(String f_pid) {
        // 차단 해제 버튼 클릭 시 실행될 코드
        setData2(friend_pids.get(0), my_pid, "unblock");
    }

    private void onIsblockClick(String f_pid) {
        // 차단 버튼 클릭 시 실행될 코드
        setData2(friend_pids.get(0), my_pid, "isBlock");
    }

    private void sendMessage(String message) {
        System.out.println("sendMessage 클릭");
        Intent serviceIntent = new Intent(getApplicationContext(), SocketService.class);
        serviceIntent.setAction("SEND_MESSAGE");
        serviceIntent.putExtra("roompid", chattingroom_pid);
        serviceIntent.putExtra("roomname", roomname);
        serviceIntent.putExtra("mypid", my_pid);
        serviceIntent.putExtra("message", message);
        startService(serviceIntent);
    }

    private void onSendTalk(String msg, String my_pid, ArrayList<String> friend_pids) {
        System.out.println("onSendTalk friend_pids : " + friend_pids.size());
//        num = friend_pids.size() + 1;
        System.out.println(clientList.size());
        System.out.println(reader);
        // 새로운 메시지 생성
        Chatting chatMessage = new Chatting("0", chattingroom_pid, my_pid, "name", msg, reader, getCurrentTime(), 1);

        // UI 스레드에서 데이터를 추가하고 RecyclerView 갱신
        runOnUiThread(() -> {
            // 리스트에 메시지 추가
            // 리스트에 메시지 추가
            chatList.add(chatMessage);

            chattingAdapter = new ChattingAdapter(chatList, getApplicationContext(), my_pid);
            rv_chat_list.setAdapter(chattingAdapter);

            // 메시지를 리스트에 추가
            chattingAdapter.notifyItemInserted(chatList.size() - 1);

            // RecyclerView를 최신 메시지 위치로 스크롤
            rv_chat_list.post(() -> {
                rv_chat_list.scrollToPosition(chatList.size() - 1);
            });

        });
        // 메시지를 서버로 전송하는 로직
        if (!chatList.isEmpty()) {
            sendMessage(my_pid + "/" + chattingroom_pid + "/" + roomname + "/" + msg);
            setData3(friend_pids, my_pid, msg);
            et_talk.setText(""); // 입력창 초기화
        }
    }
    //친구등록이 없다가 처음 친구로 추가 될때
    private void setData(String my_pid, String f_pid) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
            Log.e("ChattingActivity", "네트워크 연결을 확인하세요.");
            return;
        }
        // get방식 파라미터 추가
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Set_friend.php").newBuilder();
        urlBuilder.addQueryParameter("v", "1.0"); // 예시
        String url = urlBuilder.build().toString();
        // POST 파라미터 추가
        RequestBody formBody = new FormBody.Builder()
                .add("my_pid", my_pid)
                .add("f_pid", friend_pids.get(0))
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
                        Log.e("ChattingActivity", "네트워크 요청 실패");
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
                                Log.e("ChattingActivity", "네트워크 문제 발생");
                            } else {
                                // 응답 성공
                                Log.i("ChattingActivity", "응답 성공");
                                final String responseData = response.body().string();
                                Log.i("ChattingActivity", "서버 응답: " + responseData); // 응답 데이터 로그 기록

                                try {
                                    JSONObject jsonResponse = new JSONObject(responseData);
                                    boolean success = jsonResponse.getBoolean("success");
                                    if (success){
                                        getData(my_pid, friend_pid);
                                    }else{
                                        Log.e("ChattingActivity", "친구추가 실패");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.e("ChattingActivity", "응답 처리 중 오류가 발생했습니다.");
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
            Log.e("ChattingActivity", "네트워크 연결을 확인하세요.");
            return;
        }
        // GET 방식 파라미터 추가
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Set_friends_state.php").newBuilder();
        urlBuilder.addQueryParameter("v", "1.0"); // 예시
        String url = urlBuilder.build().toString();

        System.out.println(my_pid + String.valueOf(friend_pids) + state);

        // POST 방식 파라미터 추가
        RequestBody formBody = new FormBody.Builder()
                .add("my_pid", my_pid)
                .add("f_pid", friend_pids.get(0))
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
                runOnUiThread(() -> Log.e("ChattingActivity", "네트워크 요청 실패"));
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    try {
                        if (!response.isSuccessful()) {
                            // 응답 실패
                            Log.e("ChattingActivity", "네트워크 문제 발생");
                        } else {
                            // 응답 성공
                            Log.i("ChattingActivity", "응답 성공");
                            String responseData = response.body().string();
                            Log.i("ChattingActivity", "서버 응답: " + responseData);

                            try {
                                JSONObject jsonResponse = new JSONObject(responseData);
                                boolean success = jsonResponse.getBoolean("success");

                                if (success) {
                                    getData(my_pid, friend_pid);
                                } else {
                                    Log.e("ChattingActivity", "변경 실패");
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e("ChattingActivity", "응답 처리 중 오류 발생");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }
    //ib_send_talk클릭했을때 my_pid, 친구pid , massage
    private void setData3(ArrayList<String> friend_pids, String my_pid, String message) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
            Log.e("ChattingActivity", "네트워크 연결을 확인하세요.");
            return;
        }

        // GET 방식 파라미터 추가
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Set_talk.php").newBuilder();
        urlBuilder.addQueryParameter("v", "1.0"); // 예시
        String url = urlBuilder.build().toString();

        // ArrayList<String> friend_pids를 콤마로 구분된 문자열로 변환
        String friendPidsString = TextUtils.join(",", friend_pids);

        System.out.println("setData3 friend_pids수 : " + friend_pids.size());

        // POST 방식 파라미터 추가
        RequestBody formBody = new FormBody.Builder()
                .add("my_pid", my_pid)
                .add("friend_pids", friendPidsString)
                .add("message", message)
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
                            // 응답 실패
                            Log.e("ChattingActivity", "네트워크 문제 발생");
                        } else {
                            // 응답 성공
                            Log.i("ChattingActivity", "응답 성공");
                            String responseData = response.body().string();
                            Log.i("ChattingActivity", "서버 응답: " + responseData);

                            try {
                                JSONObject jsonResponse = new JSONObject(responseData);
                                boolean success = jsonResponse.getBoolean("success");
                                if (success) {
                                    System.out.println(success);
                                } else {
                                    Log.e("ChattingActivity", "변경 실패");
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e("ChattingActivity", "응답 처리 중 오류 발생");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }
    private void getData2(String room_pid) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
            Log.e("ChattingActivity", "네트워크 연결을 확인하세요.");
            return;
        }
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Get_chatlist.php").newBuilder();
        urlBuilder.addQueryParameter("v", "1.0");
        String url = urlBuilder.build().toString();

        RequestBody formBody = new FormBody.Builder()
                .add("room_pid", room_pid)
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
                runOnUiThread(() -> Log.e("ChattingActivity", "네트워크 요청 실패"));
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // 네트워크 작업은 비동기적으로 처리하고 UI 업데이트는 runOnUiThread() 내에서 실행
                final String responseData = response.body().string();
                runOnUiThread(() -> {
                    try {
                        if (!response.isSuccessful()) {
                            Log.e("ChattingActivity", "응답 실패");
                        } else {
                            Log.i("ChattingActivity", "응답 성공");
                            JSONObject jsonResponse = new JSONObject(responseData);
                            boolean success = jsonResponse.getBoolean("success");
                            if (success) {
                                JSONArray roomsArray = jsonResponse.getJSONArray("rooms");
                                // chatList 초기화 후 데이터 추가
                                chatList.clear();
                                for (int i = 0; i < roomsArray.length(); i++) {
                                    JSONObject roomObject = roomsArray.getJSONObject(i);
                                    String chatPid = roomObject.getString("pid");
                                    String room_pid = roomObject.getString("room_pid");
                                    String sender_pid = roomObject.getString("sender_pid");
                                    String sender_name = roomObject.getString("sender_name");
                                    String msg = roomObject.getString("msg");
                                    String createTime = roomObject.getString("create");
                                    int count = roomObject.getInt("count");
                                    int status = roomObject.getInt("status");
                                    chatList.add(new Chatting(chatPid, room_pid, sender_pid, sender_name, msg, count, createTime, status));
                                }
                                // 이름 매핑
                                JSONObject namesObject = jsonResponse.getJSONObject("names");
                                Iterator<String> keys = namesObject.keys();
                                while (keys.hasNext()) {
                                    String key = keys.next();
                                    String name = namesObject.getString(key);
                                    pidNameMap.put(key, name); // HashMap에 저장
                                }
                                // 어댑터 초기화 또는 갱신
                                if (chattingAdapter == null) {
                                    chattingAdapter = new ChattingAdapter(chatList, getApplicationContext(), my_pid);
                                    rv_chat_list.setAdapter(chattingAdapter);
                                } else {
                                    chattingAdapter.notifyDataSetChanged();
                                }
                                // RecyclerView를 최신 메시지 위치로 스크롤
                                rv_chat_list.post(() -> rv_chat_list.scrollToPosition(chatList.size() - 1));
                                // 사용자 입장 메시지 전송
                                sendMessage(my_pid + "/" + room_pid + "/" + roomname + "/" + "입장");
                            } else {
                                Log.e("ChattingActivity", "데이터 로드 실패");
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("ChattingActivity", "응답 처리 중 오류 발생");
                    }
                });
            }
        });
    }
    private void setData4(String room_pid, String reader_pid, Runnable callback) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
            Log.e("ChattingActivity", "네트워크 연결을 확인하세요.");
            return;
        }
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Update_Chatting_Reader.php").newBuilder();
        urlBuilder.addQueryParameter("v", "1.0");
        String url = urlBuilder.build().toString();
        RequestBody formBody = new FormBody.Builder()
                .add("room_pid", room_pid)
                .add("pid", reader_pid)
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
                runOnUiThread(() -> Log.e("ChattingActivity", "네트워크 요청 실패"));
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    try {
                        if (!response.isSuccessful()) {
                            Log.e("ChattingActivity", "응답 실패");
                        } else {
                            Log.i("ChattingActivity", "응답 성공");
                            final String responseData = response.body().string();
                            Log.i("ChattingActivity setData4", "서버 응답: " + responseData);

                            try {
                                JSONObject jsonResponse = new JSONObject(responseData);
                                boolean success = jsonResponse.getBoolean("success");
                                if (success){
                                    // setData4가 성공하면 콜백 실행
                                    if (callback != null) {
                                        callback.run();
                                    }
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
