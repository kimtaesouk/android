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
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.icu.text.SimpleDateFormat;
import android.media.ExifInterface;
import android.net.Uri;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
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
import com.example.newproject.Chat.Image.ImageAlbumAdapter;
import com.example.newproject.Chat.Socket.SocketService;
import com.example.newproject.Main.Main_Activity;
import com.example.newproject.R;
import com.example.newproject.singup.NetworkStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class ChattingActivity extends AppCompatActivity {
    TextView tv_friend_name; // 친구의 이름을 표시할 TextView
    EditText et_talk; // 채팅 내용을 입력할 EditText
    ImageButton ib_send_talk, ib_back, ib_room_option, ib_add_file, ib_clear_file, ib_chat_camara, ib_chat_album;
    LinearLayout ll_friend_add_or_block, ll_block_clear, ll_block, ll_add_friend, ll_add_file, ll_image_album;
    // 친구의 이름과 친구의 PID (개인 식별자)
    String friend_pid, my_pid, chattingroom_pid, roomname;
    RecyclerView rv_chat_list, rv_image_album;
    ChattingAdapter chattingAdapter;
    ArrayList<Chatting> chatList = new ArrayList<>();
    ArrayList<String> friend_pids = new ArrayList<>();
    HashMap<String, String> pidNameMap = new HashMap<>(); // PID를 키로 하고 이름을 값으로 하는 HashMap
    ArrayList<String> clientList = new ArrayList<>();
    private boolean isBlocked = false;  // 기본값은 차단되지 않은 상태로 설정
    int reader = 0;
    private int keyboardHeight = 0; // 키보드 높이를 저장할 변수
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int CAMERA_INTENT_REQUEST_CODE = 101;
    private static final int GALLERY_PERMISSION_REQUEST_CODE = 200;
    private static final int GALLERY_INTENT_REQUEST_CODE = 201;
    private int currentPage = 1;  // 현재 페이지
    private final int PAGE_SIZE = 25;  // 한 번에 로드할 항목의 개수
    private boolean isLoading = false;  // 데이터 로드 중인지 여부
    private boolean isFirstLoad = true;
    private Bitmap capturedImage = null;  // 카메라로 찍은 이미지를 저장하는 변수
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        // XML 레이아웃에서 UI 요소를 연결
        tv_friend_name = findViewById(R.id.tv_friend_name);
        et_talk = findViewById(R.id.et_talk);
        ib_send_talk = findViewById(R.id.ib_send_talk);
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
        ib_chat_album = findViewById(R.id.ib_chat_album);

        rv_image_album = findViewById(R.id.rv_image_album);
        ll_image_album = findViewById(R.id.ll_image_album);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        rv_chat_list.setLayoutManager(layoutManager);
        rv_chat_list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) rv_chat_list.getLayoutManager();
                if (layoutManager != null && layoutManager.findFirstVisibleItemPosition() == 0 && dy < 0 && !isLoading) {
                    isLoading = true;
                    getData2(chattingroom_pid);
                }
            }
        });
        // 이전 액티비티에서 전달된 Intent를 받아옴
        Intent intent = getIntent();
        my_pid = intent.getStringExtra("my_pid");
        chattingroom_pid = intent.getStringExtra("room_pid");
        roomname = intent.getStringExtra("roomname");
        friend_pids = intent.getStringArrayListExtra("friend_pid");

        if (friend_pids.size() == 1 || chattingroom_pid == null) {
            JSONArray jsonArray = new JSONArray(friend_pids);
            String friendPidsString = jsonArray.toString();
            getData(my_pid, friendPidsString);
        } else {
            tv_friend_name.setText(roomname);
            setData4(chattingroom_pid, my_pid, () -> getData2(chattingroom_pid));
        }

        IntentFilter filter = new IntentFilter("com.example.NewProject.NEW_MESSAGE");
        registerReceiver(messageReceiver, filter);
        connectToSocketService();
        sendMessage(my_pid + "|" + chattingroom_pid + "|" + roomname + "|" + "입장");

        ib_back.setOnClickListener(v -> {
            Intent resultIntent = new Intent(ChattingActivity.this, Main_Activity.class);
            resultIntent.putExtra("openFragment", "ChatFragment");
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            SharedPreferences prefs = getSharedPreferences("fragmentPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("shouldOpenChatFragment", true);
            editor.apply();
            startActivity(resultIntent);
            try {
                sendMessage(my_pid + "|" + chattingroom_pid + "|" + roomname + "|" + "퇴장");
                unregisterReceiver(messageReceiver);
                setData4(chattingroom_pid, my_pid, () -> finish());
            } catch (IllegalArgumentException e) {
                Log.e("ChattingActivity", "Receiver not registered", e);
            }
        });

        ib_add_file.setOnClickListener(v -> {
            if (et_talk.hasFocus()) {
                et_talk.clearFocus();
                hideKeyboard();
            }
            ll_add_file.postDelayed(this::animateLayoutChange, 300);
            ib_add_file.setVisibility(View.GONE);
            ll_add_file.setVisibility(View.VISIBLE);
            ib_clear_file.setVisibility(View.VISIBLE);
        });

        ib_clear_file.setOnClickListener(v -> {
            ll_add_file.setVisibility(View.GONE);
            ib_clear_file.setVisibility(View.GONE);
            ll_image_album.setVisibility(View.GONE);
            ib_add_file.setVisibility(View.VISIBLE);
            et_talk.setVisibility(View.VISIBLE);

        });

        ib_chat_camara.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(ChattingActivity.this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ChattingActivity.this,
                        new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                openCamera();
            }
        });

        et_talk.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                scrollToBottom();
                ll_add_file.setVisibility(View.GONE);
                ib_clear_file.setVisibility(View.GONE);
                ib_add_file.setVisibility(View.VISIBLE);
            }
        });

        et_talk.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (et_talk.getText().toString().isEmpty()) {
                    ib_send_talk.setVisibility(View.GONE);
                } else {
                    ib_send_talk.setVisibility(View.VISIBLE);
                    ib_send_talk.setOnClickListener(v -> onSendTalk(et_talk.getText().toString(), my_pid, friend_pids));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ib_chat_album.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(ChattingActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ChattingActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION_REQUEST_CODE);
            } else {
                // 갤러리에서 이미지 로드 및 RecyclerView에 표시
                loadGalleryImages();
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        // BroadcastReceiver 등록
        IntentFilter filter = new IntentFilter("com.example.NewProject.NEW_MESSAGE");
        registerReceiver(messageReceiver, filter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent serviceIntent = new Intent(this, SocketService.class);
        serviceIntent.setAction("EXIT_ROOM");
        serviceIntent.putExtra("roompid", chattingroom_pid); // 방 ID
        serviceIntent.putExtra("roomname", roomname);        // 방 이름
        serviceIntent.putExtra("mypid", my_pid);             // 내 ID
        serviceIntent.putExtra("message", "퇴장");
    }

    private void loadGalleryImages() {
        ArrayList<Uri> imageUris = new ArrayList<>();

        // ContentResolver를 사용하여 이미지 URI를 쿼리
        String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME};
        Uri imagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = getContentResolver().query(
                imagesUri,
                projection,
                null,
                null,
                MediaStore.Images.Media.DATE_ADDED + " DESC"  // 최근 추가된 이미지부터 가져오기
        );

        if (cursor != null) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                // 이미지 URI 리스트에 추가
                imageUris.add(contentUri);
            }
            cursor.close();
        }

        // RecyclerView에 이미지 URI 리스트 표시
        displayImagesInRecyclerView(imageUris);
    }

    private void displayImagesInRecyclerView(ArrayList<Uri> imageUris) {
        // RecyclerView 레이아웃 설정 (가로 스크롤)
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rv_image_album.setLayoutManager(horizontalLayoutManager);

        // 어댑터 설정
        ImageAlbumAdapter adapter = new ImageAlbumAdapter(imageUris, this);
        rv_image_album.setAdapter(adapter);

        // 이미지가 포함된 LinearLayout을 보이게 설정
        ll_add_file.setVisibility(View.GONE);
        ll_image_album.setVisibility(View.VISIBLE);
        et_talk.setVisibility(View.GONE);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_INTENT_REQUEST_CODE);
        }
    }
    // EXIF 데이터를 확인하고 필요시 이미지를 회전시키는 함수
    private Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {
        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23) {
            ei = new ExifInterface(input);
        } else {
            ei = new ExifInterface(selectedImage.getPath());
        }

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    // 이미지를 회전시키는 함수
    private Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_INTENT_REQUEST_CODE && resultCode == RESULT_OK) {
            // Intent에서 Bitmap 데이터를 가져옵니다.
            Bundle extras = data.getExtras();
            capturedImage = (Bitmap) extras.get("data");

            // 이미지 URI를 확인합니다.
            Uri imageUri = data.getData();

            // URI가 없으면 Bitmap만을 처리합니다.
            if (imageUri != null) {
                try {
                    // EXIF 데이터를 확인하고 이미지를 회전시킵니다.
                    Bitmap rotatedImage = rotateImageIfRequired(this, capturedImage, imageUri);
                    sendChatMessageWithImage(et_talk.getText().toString(), rotatedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "이미지 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // URI가 없을 경우, 그냥 capturedImage 사용
                sendChatMessageWithImage(et_talk.getText().toString(), capturedImage);
            }
        }
        if (requestCode == 123) {
            // 데이터 새로고침 로직
            System.out.println("onActivityResult");
            if (friend_pids.size() == 1 || chattingroom_pid == null) {
                JSONArray jsonArray = new JSONArray(friend_pids);
                String friendPidsString = jsonArray.toString();
                getData(my_pid, friendPidsString);
            }
        }
    }

    private void sendChatMessageWithImage(String message, Bitmap imageBitmap) {
        File imageFile = null;
        String encodedImage = null;

        if (imageBitmap != null) {
            // 이미지를 Base64로 인코딩합니다.
            encodedImage = encodeImageToBase64(imageBitmap);

            // 이미지 파일을 생성합니다.
            imageFile = saveImageToFile(imageBitmap);

            // 리사이클러뷰에 이미지를 바로 추가합니다.
            addImageToRecyclerView(imageBitmap, imageFile);
        }

        // 메시지와 이미지를 서버로 전송합니다.
        uploadMessageAndImageToServer("[사진]", imageFile);

        // Base64로 인코딩한 이미지를 포함한 메시지를 서버로 전송합니다.
        sendMessage(my_pid + "|" + chattingroom_pid + "|" + roomname + "|" + encodedImage);

        // 메시지가 있을 경우 추가로 처리합니다.
        if (!message.isEmpty()) {
            onSendTalk(message, my_pid, friend_pids); // 메시지 추가
        }
    }
    private String encodeImageToBase64(Bitmap imageBitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        // Base64로 인코딩
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
    private Bitmap decodeBase64ToBitmap(String base64String) {
        try {
            // Base64 문자열에서 줄바꿈이나 공백 제거
            base64String = base64String.replace("\n", "").replace("\r", "");

            // Base64로부터 바이트 배열을 디코딩
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);

            // 바이트 배열을 비트맵으로 변환
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) {
            Log.e("ChattingActivity", "Base64 decoding failed", e);
            return null;
        }
    }

    // 리사이클러뷰에 이미지와 함께 데이터를 추가하는 메서드
    private void addImageToRecyclerView(Bitmap imageBitmap, File imageFile) {
        if (imageFile != null) {
            // 새로운 채팅 메시지 객체를 생성합니다.
            Chatting chatMessage = new Chatting("0", chattingroom_pid, my_pid, "name", "[사진]", reader, getCurrentTime(), 1);

            // 이미지 경로를 추가합니다.
            chatMessage.setImagePath(imageFile.getAbsolutePath());

            // 채팅 리스트에 새 메시지를 추가합니다.
            chatList.add(chatMessage);

            chattingAdapter = new ChattingAdapter(this, chatList, getApplicationContext(), my_pid, chattingroom_pid);
            rv_chat_list.setAdapter(chattingAdapter);
            chattingAdapter.notifyItemInserted(chatList.size() - 1);

            // 어댑터에 데이터가 추가되었음을 알립니다.

            // 리사이클러뷰를 가장 마지막 항목으로 스크롤합니다.
            rv_chat_list.scrollToPosition(chatList.size() - 1);
        }
    }
    private void addOtherImageToRecyclerView(File imageFile, String senderid, String sendername) {
        if (imageFile != null) {
            // 새로운 채팅 메시지 객체를 생성합니다.
            Chatting chatMessage = new Chatting("0", chattingroom_pid, senderid, sendername, "[사진]", reader, getCurrentTime(), 1);

            // 이미지 경로를 추가합니다.
            chatMessage.setImagePath(imageFile.getAbsolutePath());

            // 채팅 리스트에 새 메시지를 추가합니다.
            chatList.add(chatMessage);

            chattingAdapter = new ChattingAdapter(this, chatList, getApplicationContext(), my_pid, chattingroom_pid);
            rv_chat_list.setAdapter(chattingAdapter);
            chattingAdapter.notifyItemInserted(chatList.size() - 1);

            // 어댑터에 데이터가 추가되었음을 알립니다.

            // 리사이클러뷰를 가장 마지막 항목으로 스크롤합니다.
            rv_chat_list.scrollToPosition(chatList.size() - 1);
        }
    }


    private File saveImageToFile(Bitmap bitmap) {
        File imageFile = null;
        try {
            File storageDir = getExternalFilesDir(null);
            imageFile = new File(storageDir, "captured_image_" + System.currentTimeMillis() + ".jpg");

            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageFile;
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
        }else if (requestCode == GALLERY_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadGalleryImages();
            } else {
                Toast.makeText(this, "갤러리 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void animateLayoutChange() {
        ll_add_file.animate()
                .translationY(0)  // 위로 올라오는 애니메이션
                .alpha(1.0f)  // 투명도 적용 (페이드 인)
                .setDuration(500)  // 300ms 동안 애니메이션 실행
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
            if (isBlocked) {
                // 친구가 차단된 상태라면 메시지를 받지 않음
                Log.i("ChattingActivity", "메시지를 받지 않음 - 차단된 상태");
                return;  // 메시지 처리를 중단
            }

            String action = intent.getAction();
            if ("com.example.NewProject.NEW_MESSAGE".equals(action)) {
                String msg = intent.getStringExtra("message").trim();
                addMessageToRecyclerView(msg);  // 차단 상태가 아니라면 메시지를 추가
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
                            chattingAdapter = new ChattingAdapter(ChattingActivity.this, chatList, getApplicationContext(), my_pid, chattingroom_pid);
                            rv_chat_list.setAdapter(chattingAdapter);
                            chattingAdapter.notifyDataSetChanged();

                            // 퇴장 메시지 처리
                        } else if (msg.equals("퇴장")) {
                            System.out.println("addMessageToRecyclerView clientList 퇴장 : " + clientList.size() );
                            // 일반 메시지 처리
                        } else if (msg.startsWith("/9j/")) {
                            String senderName = pidNameMap.getOrDefault(senderId, "Unknown");
                            String base64String = msg;

                            Bitmap decodedImage = decodeBase64ToBitmap(base64String);
                            if (decodedImage != null) {
                                // 파일로 저장 (원한다면)
                                File imageFile = saveImageToFile(decodedImage);
                                if (imageFile != null) {
                                    addOtherImageToRecyclerView(imageFile, senderId, senderName);
                                } else {
                                    Log.e("ChattingActivity", "Failed to save image to file.");
                                }
                            } else {
                                Log.e("ChattingActivity", "Failed to decode Base64 image.");
                            }
                        } else {
                            // pidNameMap에서 senderId에 해당하는 이름을 찾음
                            String senderName = pidNameMap.getOrDefault(senderId, "Unknown");
                            System.out.println("addMessageToRecyclerView" + msg);

//                            num = friend_pids.size() + 1;

                            // 새로운 채팅 메시지 생성 및 추가
                            Chatting chatMessage = new Chatting("0", chattingroom_pid, senderId, senderName, msg, reader, getCurrentTime(), 1);
                            chatList.add(chatMessage);

                            // 어댑터 갱신 및 RecyclerView에 메시지 추가
                            chattingAdapter = new ChattingAdapter(ChattingActivity.this, chatList, getApplicationContext(), my_pid, chattingroom_pid);
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
                                    String status = jsonResponse.getString("success");
                                    roomname = jsonResponse.getString("roomname");
                                    chattingroom_pid = jsonResponse.getString("chatting_room_pid");
                                    System.out.println("getData 에서 받아온 chattingroom_pid : " + chattingroom_pid);
                                    tv_friend_name.setText(roomname);
                                    setData4(chattingroom_pid, my_pid, () -> getData2(chattingroom_pid));

                                    System.out.println("서버에서 가져온 isBlodk : " + status );

                                    if (status.equals("isBlock")){
                                        isBlocked = true;  // 차단 상태를 true로 설정
                                        ll_friend_add_or_block.setVisibility(View.VISIBLE);

                                        ll_block_clear.setVisibility(View.VISIBLE);
                                        ll_block_clear.setOnClickListener(v -> onUnblockClick(friend_pid));

                                        ll_block.setVisibility(View.GONE);

                                        ll_add_friend.setVisibility(View.GONE);

                                        et_talk.setText("차단 친구와는 대화가 불가능합니다.");
                                        et_talk.setEnabled(false); // et_talk를 비활성화 (회색으로 표시되고 클릭 불가)
                                        et_talk.setFocusable(false); // et_talk를 포커스 불가능하게 설정
                                        et_talk.setFocusableInTouchMode(false); // 터치로도 포커스할 수 없게 설정
                                        ib_send_talk.setVisibility(View.GONE);
                                    } else if (status.equals("true")) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                isBlocked = false;  // 차단 상태를 true로 설정
                                                ll_friend_add_or_block.setVisibility(View.GONE);

                                                et_talk.setText("");
                                                et_talk.setEnabled(true); // et_talk를 활성화
                                                et_talk.setFocusable(true); // et_talk를 포커스 가능하게 설정
                                                et_talk.setFocusableInTouchMode(true); // 터치로도 포커스할 수 있게 설정
                                            }
                                        });
                                    }
                                    else if (status.equals("false")) {
                                        isBlocked = false;
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
        Intent serviceIntent = new Intent(this, SocketService.class);
        serviceIntent.setAction("UnBlock");
        serviceIntent.putExtra("roompid", chattingroom_pid); // 방 ID
        serviceIntent.putExtra("roomname", roomname);        // 방 이름
        serviceIntent.putExtra("mypid", my_pid);             // 내 ID
        serviceIntent.putExtra("message", "UnBlock");
        startService(serviceIntent);
    }

    private void onIsblockClick(String f_pid) {
        // 차단 버튼 클릭 시 실행될 코드
        setData2(friend_pids.get(0), my_pid, "isBlock");
        Intent serviceIntent = new Intent(this, SocketService.class);
        serviceIntent.setAction("IsBlock");
        serviceIntent.putExtra("roompid", chattingroom_pid); // 방 ID
        serviceIntent.putExtra("roomname", roomname);        // 방 이름
        serviceIntent.putExtra("mypid", my_pid);             // 내 ID
        serviceIntent.putExtra("message", "IsBlock");           // 입장 메시지
        startService(serviceIntent);
    }

    private void sendMessage(String message) {
        Intent serviceIntent = new Intent(getApplicationContext(), SocketService.class);
        serviceIntent.setAction("SEND_MESSAGE");
        serviceIntent.putExtra("roompid", chattingroom_pid);
        serviceIntent.putExtra("roomname", roomname);
        serviceIntent.putExtra("mypid", my_pid);
        serviceIntent.putExtra("message", message);
        startService(serviceIntent);
    }

    private void onSendTalk(String msg, String my_pid, ArrayList<String> friend_pids) {
        // 새로운 메시지 생성
        Chatting chatMessage = new Chatting("0", chattingroom_pid, my_pid, "name", msg, reader, getCurrentTime(), 1);

        // UI 스레드에서 데이터를 추가하고 RecyclerView 갱신
        runOnUiThread(() -> {
            // 리스트에 메시지 추가
            // 리스트에 메시지 추가
            chatList.add(chatMessage);

            chattingAdapter = new ChattingAdapter(this, chatList, getApplicationContext(), my_pid, chattingroom_pid);
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
            sendMessage(my_pid + "|" + chattingroom_pid + "|" + roomname + "|" + msg);
            uploadMessageAndImageToServer (msg, null);
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
    private void uploadMessageAndImageToServer(String message, File imageFile) {
        System.out.println("uploadMessageAndImageToServer");
        String friendPidsString = TextUtils.join(",", friend_pids);

        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("message", message)
                .addFormDataPart("my_pid", my_pid)
                .addFormDataPart("friend_pids", friendPidsString)
                .addFormDataPart("isBlocked" , String.valueOf(isBlocked))
                .addFormDataPart("chattingroom_pid", chattingroom_pid);

        if (imageFile != null) {
            builder.addFormDataPart("image", imageFile.getName(),
                    RequestBody.create(imageFile, okhttp3.MediaType.parse("image/jpeg")));
        }

        RequestBody requestBody = builder.build();
        Request request = new Request.Builder()
                .url("http://49.247.32.169/NewProject/Set_talk.php")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ChattingActivity.this, "메시지 전송 실패", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 서버로부터 받은 응답 처리
                if (response.isSuccessful()) {
                    // 서버 응답 내용을 문자열로 변환
                    String responseData = response.body().string();

                    // 서버 응답을 로그로 출력하여 확인
                    Log.d("ChattingActivity", "Server Response: " + responseData);

                    runOnUiThread(() -> {
                        // 서버에서 받은 응답을 처리
                        try {
                            // 서버 응답이 JSON 형태라면 JSONObject로 파싱
                            JSONObject jsonResponse = new JSONObject(responseData);

                            boolean success = jsonResponse.getBoolean("success");
                            String message = jsonResponse.getString("message");

                            // 응답에 따라 처리
                            if (success) {
                            } else {
                                Toast.makeText(ChattingActivity.this, "메시지 전송 실패: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ChattingActivity.this, "서버 응답 처리 중 오류 발생", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(ChattingActivity.this, "서버 오류: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void getData2(String room_pid) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
            Log.e("ChattingActivity", "네트워크 연결을 확인하세요.");
            return;
        }

        // 현재 보이는 첫 번째 아이템의 위치와 오프셋을 저장
        LinearLayoutManager layoutManager = (LinearLayoutManager) rv_chat_list.getLayoutManager();
        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
        View firstVisibleItemView = layoutManager.findViewByPosition(firstVisibleItemPosition);
        int offset = (firstVisibleItemView != null) ? firstVisibleItemView.getTop() : 0;

        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Get_chatlist.php").newBuilder();
        urlBuilder.addQueryParameter("v", "1.0");
        String url = urlBuilder.build().toString();

        RequestBody formBody = new FormBody.Builder()
                .add("my_pid", my_pid)
                .add("room_pid", room_pid)
                .add("page", String.valueOf(currentPage))
                .add("page_size", String.valueOf(PAGE_SIZE))
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
                isLoading = false;
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseData = response.body().string();
                runOnUiThread(() -> {
                    try {
                        if (!response.isSuccessful()) {
                            Log.e("ChattingActivity", "응답 실패");
                        } else {
                            JSONObject jsonResponse = new JSONObject(responseData);
                            boolean success = jsonResponse.getBoolean("success");

                            if (success) {
                                JSONArray roomsArray = jsonResponse.getJSONArray("rooms");

                                int newItemsCount = roomsArray.length();
                                List<Chatting> newItems = new ArrayList<>();

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

                                    // 이미지 URL 받기
                                    String imageUrl = roomObject.optString("image_url", null);

                                    // Chatting 객체에 이미지 URL을 포함하여 추가
                                    Chatting chatItem = new Chatting(chatPid, room_pid, sender_pid, sender_name, msg, count, createTime, status);
                                    chatItem.setImagePath(imageUrl);  // 이미지 URL 설정
                                    newItems.add(chatItem);
                                }

                                // 새로운 데이터 추가 후 어댑터 갱신
                                chatList.addAll(0, newItems);

                                // chattingAdapter 설정 및 RecyclerView에 적용
                                chattingAdapter = new ChattingAdapter(ChattingActivity.this, chatList, getApplicationContext(), my_pid, chattingroom_pid);
                                rv_chat_list.setAdapter(chattingAdapter);

                                // JSONObject에서 "names" 객체를 가져와 pidNameMap에 추가
                                JSONObject namesObject = jsonResponse.getJSONObject("names");
                                Iterator<String> keys = namesObject.keys();
                                while (keys.hasNext()) {
                                    String key = keys.next();
                                    String name = namesObject.getString(key);
                                    pidNameMap.put(key, name);
                                }

                                // 처음 로드될 때만 가장 아래로 스크롤
                                if (isFirstLoad) {
                                    // 스크롤을 맨 아래로 확실하게 이동하기 위해 약간의 지연을 둡니다.
                                    rv_chat_list.postDelayed(() -> {
                                        rv_chat_list.scrollToPosition(chatList.size() + 1);
                                    }, 100);  // 100ms 정도의 지연을 줍니다.
                                    isFirstLoad = false;  // 이후로는 자동 스크롤하지 않도록 설정
                                } else {
                                    // 기존 위치로 복원
                                    layoutManager.scrollToPositionWithOffset(firstVisibleItemPosition + newItemsCount, offset);
                                }

                                // 다음 페이지 로드를 위해 페이지 번호 증가
                                currentPage++;
                                isLoading = false;
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
