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
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class ChattingActivity extends AppCompatActivity implements ImageAlbumAdapter.OnImageClickListener {
    TextView tv_friend_name; // 친구의 이름을 표시할 TextView
    EditText et_talk; // 채팅 내용을 입력할 EditText
    ImageButton ib_send_talk, ib_back, ib_room_option, ib_add_file, ib_clear_file, ib_chat_camara, ib_chat_album;
    LinearLayout ll_friend_add_or_block, ll_block_clear, ll_block, ll_add_friend, ll_add_file, ll_image_album;
    // 친구의 이름과 친구의 PID (개인 식별자)
    ProgressBar progressBar;
    FrameLayout progressOverlay;
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
        progressBar = findViewById(R.id.progress_bar);
        progressOverlay = findViewById(R.id.progress_overlay);

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
            System.out.println("friend_pids.size()가 1 ");
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
            // 선택된 이미지 리스트를 초기화
            ImageAlbumAdapter adapter = (ImageAlbumAdapter) rv_image_album.getAdapter();
            if (adapter != null) {
                adapter.clearSelectedImages();
            }
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

    private void scrollToBottom() {
        rv_chat_list.post(() -> {
            if (chattingAdapter != null && chattingAdapter.getItemCount() > 0) {
                // 마지막 아이템의 포지션
                int lastPosition = chattingAdapter.getItemCount() - 1;

                // 스크롤을 정확한 위치로 설정
                LinearLayoutManager layoutManager = (LinearLayoutManager) rv_chat_list.getLayoutManager();
                if (layoutManager != null) {
                    // 강제로 마지막 위치로 이동
                    layoutManager.scrollToPositionWithOffset(lastPosition, 0);
                }
            }
        });
    }


    private void loadGalleryImages() {
        ArrayList<Uri> imageUris = new ArrayList<>();

        // ContentResolver를 사용하여 이미지 URI를 쿼리
        String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME};
        Uri imagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = getContentResolver().query(
                imagesUri,                   // 조회할 URI (갤러리의 이미지 데이터베이스)
                projection,                  // 반환할 컬럼 (ID와 이미지 이름)
                null,                        // WHERE 절 (필터링 조건이 없으므로 전체를 조회)
                null,                        // WHERE 절에서 사용할 파라미터 값
                MediaStore.Images.Media.DATE_ADDED + " DESC" // 정렬 조건 (가장 최근에 추가된 이미지부터)
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
    // ProgressBar를 보이는 함수
    private void showProgressBar(FrameLayout progressOverlay) {
        progressOverlay.setVisibility(View.VISIBLE); // 오버레이 표시
        progressBar.setVisibility(View.VISIBLE);     // ProgressBar 표시
    }

    // ProgressBar를 숨기는 함수
    private void hideProgressBar(FrameLayout progressOverlay) {
        progressOverlay.setVisibility(View.GONE);    // 오버레이 숨김
        progressBar.setVisibility(View.GONE);        // ProgressBar 숨김
    }

    private void displayImagesInRecyclerView(ArrayList<Uri> imageUris) {
        // RecyclerView 레이아웃 설정 (가로 스크롤)
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rv_image_album.setLayoutManager(horizontalLayoutManager);

        // 어댑터 설정
        ImageAlbumAdapter adapter = new ImageAlbumAdapter(imageUris, this, ChattingActivity.this);
        rv_image_album.setAdapter(adapter);

        // 이미지가 포함된 LinearLayout을 보이게 설정
        ll_add_file.setVisibility(View.GONE);
        ll_image_album.setVisibility(View.VISIBLE);
        et_talk.setVisibility(View.GONE);
    }

    @Override
    public void onImageClick(ArrayList<Uri> selectedImages) {

        // 선택된 이미지가 없을 경우 전송 버튼을 숨김
        if (selectedImages.size() == 0) {
            ib_send_talk.setVisibility(View.GONE);  // 전송 버튼 숨기기
        } else {
            // 선택된 이미지가 있을 경우 전송 버튼을 보이게 함
            ib_send_talk.setVisibility(View.VISIBLE);  // 전송 버튼 보이기
            // 전송 버튼에 대한 클릭 리스너 설정
            ib_send_talk.setOnClickListener(v -> {
                // ProgressBar를 표시 (이미지 전송 중임을 사용자에게 알림)
                showProgressBar(progressOverlay);

                // 선택된 모든 이미지를 서버로 전송 (이미지 전송과 함께 메시지 내용도 전송)
                sendChatMessageWithImages(et_talk.getText().toString(), selectedImages);

                // 이미지 앨범과 관련된 UI 요소들 숨김
                ll_image_album.setVisibility(View.GONE);  // 이미지 앨범 뷰 숨기기
                ib_clear_file.setVisibility(View.GONE);  // 파일 초기화 버튼 숨기기
                ib_add_file.setVisibility(View.VISIBLE);  // 파일 추가 버튼 다시 보이기
                et_talk.setVisibility(View.VISIBLE);  // 채팅 입력창 다시 보이기
                ib_send_talk.setVisibility(View.GONE);  // 전송 버튼 숨기기

                // 전송이 완료되면 ProgressBar를 숨김 (UI 스레드에서 실행)
                runOnUiThread(() -> hideProgressBar(progressOverlay));
            });
        }
    }
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_INTENT_REQUEST_CODE);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_INTENT_REQUEST_CODE && resultCode == RESULT_OK) {
            // 카메라로 찍은 이미지를 가져옵니다.
            Bundle extras = data.getExtras();
            capturedImage = (Bitmap) extras.get("data");

            // 이미지 URI를 확인합니다.
            Uri imageUri = data.getData();
            List<Uri> imageUris = new ArrayList<>();

            // URI가 없으면 Bitmap을 임시 파일로 변환하고 URI로 처리
            if (imageUri == null) {
                File imageFile = saveImageToFile(capturedImage);
                imageUri = Uri.fromFile(imageFile);
            }

            imageUris.add(imageUri);

            // 선택한 이미지를 전송
            sendChatMessageWithImages(et_talk.getText().toString(), imageUris);
        }else if (requestCode == 123) {
            if (friend_pids.size() == 1 || chattingroom_pid == null) {
                JSONArray jsonArray = new JSONArray(friend_pids);
                String friendPidsString = jsonArray.toString();
                getData(my_pid, friendPidsString);
            }
        }
    }

    private void sendChatMessageWithImages(String message, List<Uri> selectedImages) {
        // 선택된 이미지를 Uri 순서대로 처리
        for (Uri imageUri : selectedImages) {
            try {
                String tempPid = "temp_" + System.currentTimeMillis(); // 임시로 고유한 PID 생성
                // Uri에서 바로 파일로 변환
                File imageFile = uriToFile(imageUri);
                // 선택한 이미지의 순서대로 RecyclerView에 추가
                addImageToRecyclerView(imageFile, tempPid);
                //아파치 서버로 전송
                uploadMessageAndImageToServer("사진을 보냈습니다.", imageFile, tempPid);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 메시지가 있을 경우 추가로 처리
        if (!message.isEmpty()) {
            onSendTalk(message, my_pid, friend_pids); // 메시지 추가
        }
    }

    private File inputStreamToFile(InputStream inputStream, String fileName) throws IOException {
        File file = new File(getCacheDir(), fileName);
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return file;
    }


    private File uriToFile(Uri uri) throws IOException {
        // ContentResolver를 사용 Uri로부터 파일의 이름을 가져옴
        String fileName = getFileName(uri); // 파일 이름을 얻는 메소드 (따로 구현되어야 함)
        // 캐시 디렉토리 내에 파일 이름을 사용 새로운 파일을 생성
        File file = new File(getCacheDir(), fileName);
        // try-with-resources 구문을 사용 InputStream과 OutputStream을 열어, 자동으로 닫히도록 설정
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(file)) {
            // 파일을 임시로 읽을 버퍼 변수
            byte[] buffer = new byte[1024];
            int length;
            // InputStream에서 데이터를 읽어들인 후, OutputStream을 통해 파일에 기록
            // 더 이상 읽을 데이터가 없으면 루프가 종료됨
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
        // 파일로 변환된 결과를 반환
        return file;
    }

    private String getFileName(Uri uri) {
        String result = null;

        // Uri가 content 스키마를 사용할 때 처리
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    // DISPLAY_NAME 컬럼이 있는지 확인
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }

        // 만약 DISPLAY_NAME을 찾지 못하면 경로에서 이름을 추출
        if (result == null) {
            result = uri.getPath();
            int cut = result != null ? result.lastIndexOf('/') : -1;
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }

        return result;
    }

    // 리사이클러뷰에 이미지와 함께 데이터를 추가하는 메서드
    private void addImageToRecyclerView(File imageFile, String tempPid) {
        if (imageFile != null) {
            // 새로운 채팅 메시지 객체를 생성
            Chatting chatMessage = new Chatting(tempPid, chattingroom_pid, my_pid, "name", "사진을 보냈습니다.", reader, getCurrentTime(), 1);

            // 이미지 경로를 추가
            chatMessage.setImagePath(imageFile.getAbsolutePath());

            // UI 스레드에서 RecyclerView에 추가
            runOnUiThread(() -> {
                chatList.add(chatMessage);
                initChattingAdapter();
                chattingAdapter.notifyItemInserted(chatList.size() - 1);
                scrollToBottom();
                hideProgressBar(progressOverlay);
            });
        }
    }

    private void initChattingAdapter() {
        chattingAdapter = new ChattingAdapter(
                ChattingActivity.this,
                chatList,
                getApplicationContext(),
                my_pid,
                chattingroom_pid,
                (chatPid, option) -> onChatDeleted(chatPid, option)  // 삭제 콜백 처리
        );
        rv_chat_list.setAdapter(chattingAdapter);
    }
    // 채팅 삭제 시 처리 로직
    private void onChatDeleted(String chatPid, String option) {
        // chatPid로 해당 메시지의 위치를 찾음
        int position = -1;
        for (int i = 0; i < chatList.size(); i++) {
            if (chatList.get(i).getPid().equals(chatPid)) {
                position = i;
                break;
            }
        }

        // 메시지를 찾은 경우 처리
        if (position != -1) {
            if (option.equals("all")) {
                // 메시지 내용을 "삭제된 메시지입니다"로 변경
                chatList.get(position).setMsg("삭제된 메시지입니다");

                // 특정 아이템만 UI를 갱신
                getData2(chattingroom_pid);
            } else {
                // 메시지를 리스트에서 제거하고 UI에서 갱신
                chatList.remove(position);
                chattingAdapter.notifyItemRemoved(position);
                chattingAdapter.notifyItemRangeChanged(position, chatList.size());

                getData2(chattingroom_pid);
            }
            sendMessage(my_pid + "|" + chattingroom_pid + "|" + roomname + "|" + option);
        } else {
            // 만약 chatPid로 메시지를 찾지 못했을 때 처리
            Log.e("ChattingActivity", "해당 메시지를 찾을 수 없습니다: " + chatPid);
        }
    }



    private void addOtherImageToRecyclerView(File imageFile, String senderid, String sendername) {
        if (imageFile != null) {
            // 새로운 채팅 메시지 객체를 생성합니다.
            Chatting chatMessage = new Chatting("0", chattingroom_pid, senderid, sendername, "사진을 보냈습니다.", reader, getCurrentTime(), 1);

            // 이미지 경로를 추가합니다.
            chatMessage.setImagePath(imageFile.getAbsolutePath());

            // 채팅 리스트에 새 메시지를 추가합니다.
            chatList.add(chatMessage);

            initChattingAdapter();
            chattingAdapter.notifyItemInserted(chatList.size() - 1);

            // 어댑터에 데이터가 추가되었음을 알립니다.

            // 리사이클러뷰를 가장 마지막 항목으로 스크롤합니다.
            scrollToBottom();
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
                System.out.println("ChattingActivity msg : " + msg);
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
                    parts = message.split("\\|");
                    String senderId = parts[0].trim();
                    String roomId = parts[1].trim();
                    String msg = parts.length > 3 ? parts[3].trim() : "";
                    System.out.println("addMessageToRecyclerView msg : " + msg);
                    // 클라이언트 리스트 (콤마로 구분된 문자열)
                    String clients = parts.length > 4 ? parts[4].trim() : "";
                    clientList = new ArrayList<>(List.of(clients.split(",")));
                    // my_pid와 일치하는 항목을 제외
                    // 클라이언트 수를 기반으로 reader 수 계산
                    reader =  friend_pids.size() + 1  - clientList.size() ;
                    System.out.println("addMessageToRecyclerView reader : " + reader );
                    System.out.println("addMessageToRecyclerView clientList : " + clientList.size() );
                    String senderName = pidNameMap.getOrDefault(senderId, "Unknown");
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
                            // ProgressBar 표시
                            showProgressBar(progressOverlay);
                            // 현재 보이는 첫 번째 아이템의 위치와 오프셋 저장
                            LinearLayoutManager layoutManager = (LinearLayoutManager) rv_chat_list.getLayoutManager();
                            int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                            View firstVisibleView = layoutManager.findViewByPosition(firstVisiblePosition);
                            int offset = (firstVisibleView != null) ? firstVisibleView.getTop() : 0;

                            // 어댑터 갱신
                            initChattingAdapter();
                            chattingAdapter.notifyItemRangeChanged(0, chatList.size());

                            // 어댑터 갱신 후 저장된 위치로 스크롤을 복원
                            rv_chat_list.post(() -> {
                                layoutManager.scrollToPositionWithOffset(firstVisiblePosition, offset);

                                // ProgressBar 숨기기
                                hideProgressBar(progressOverlay);
                            });
                        } else if (msg.equals("퇴장")) {
                            System.out.println("addMessageToRecyclerView clientList 퇴장 : " + clientList.size() );
                            // 일반 메시지 처리
                        } else if (msg.equals("only") || msg.equals("all") ) {
                            getData2(chattingroom_pid);
                        } else if (msg.startsWith("http://49.247.32.169/NewProject/uploads/")) {
                            System.out.println("msg : " + msg);
                            String imageUrl = msg.trim();  // 이미지 경로 (HTTP URL)
                            // 메시지를 받은 순서대로 저장하기 위한 큐를 선언
                            List<String> imageQueue = new ArrayList<>();
                            imageQueue.add(imageUrl);  // 받은 이미지 URL을 큐에 추가
                            // ProgressBar 표시
                            runOnUiThread(() -> showProgressBar(findViewById(R.id.progress_overlay)));
                            // 새 스레드로 이미지를 처리
                            new Thread(() -> {
                                try {
                                    // 큐에서 이미지 URL을 꺼내옴
                                    for (String queuedUrl : imageQueue) {
                                        URL url = new URL(queuedUrl);
                                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                        connection.setDoInput(true);
                                        connection.connect();
                                        // InputStream으로부터 이미지 읽기
                                        InputStream input = connection.getInputStream();
                                        // InputStream을 파일로 저장
                                        File imageFile = inputStreamToFile(input, "downloaded_image.jpg");
                                        // UI 스레드에서 RecyclerView에 이미지 추가
                                        runOnUiThread(() -> {
                                            // 기존 메서드 사용 (File 타입을 넘김)
                                            addOtherImageToRecyclerView(imageFile, senderId, senderName);
                                        });

                                        input.close();  // InputStream 닫기
                                    }
                                    // 큐 처리 후 비우기
                                    imageQueue.clear();

                                } catch (IOException e) {
                                    Log.e("ChattingActivity", "Error fetching image: " + e.getMessage());
                                } finally {
                                    // ProgressBar 숨기기
                                    runOnUiThread(() -> hideProgressBar(findViewById(R.id.progress_overlay)));
                                }
                            }).start();
                        } else {
                            // pidNameMap에서 senderId에 해당하는 이름을 찾음
                            // 새로운 채팅 메시지 생성 및 추가
                            Chatting chatMessage = new Chatting("0", chattingroom_pid, senderId, senderName, msg, reader, getCurrentTime(), 1);
                            chatList.add(chatMessage);
                            // 어댑터 갱신 및 RecyclerView에 메시지 추가
                            initChattingAdapter();
                            chattingAdapter.notifyItemInserted(chatList.size() - 1);
                            // RecyclerView를 최신 메시지 위치로 스크롤
                            scrollToBottom();
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
        // 임시 PID를 생성 (리스트에 메시지를 추가할 때 사용할 임시 PID)
        String tempPid = "temp_" + System.currentTimeMillis(); // 임시로 고유한 PID 생성

        // 새로운 채팅 메시지 생성 (임시 PID 사용)
        Chatting chatMessage = new Chatting(tempPid, chattingroom_pid, my_pid, "name", msg, reader, getCurrentTime(), 1);

        // UI 스레드에서 데이터를 추가하고 RecyclerView 갱신
        runOnUiThread(() -> {
            // 리스트에 메시지 추가
            chatList.add(chatMessage);
            initChattingAdapter();
            // 메시지를 리스트에 추가
            chattingAdapter.notifyItemInserted(chatList.size() - 1);
            // RecyclerView를 최신 메시지 위치로 스크롤
            scrollToBottom();
        });
        // 메시지를 서버로 전송하는 로직
        if (!chatList.isEmpty()) {
            sendMessage(my_pid + "|" + chattingroom_pid + "|" + roomname + "|" + msg);
            uploadMessageAndImageToServer(msg, null, tempPid); // 임시 PID를 함께 전달
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
    private void uploadMessageAndImageToServer(String message, File imageFile, String tempPid) {
        System.out.println("isBlocked : " + String.valueOf(isBlocked));
        String friendPidsString = TextUtils.join(",", friend_pids);
        OkHttpClient client = new OkHttpClient();

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("message", message)
                .addFormDataPart("my_pid", my_pid)
                .addFormDataPart("friend_pids", friendPidsString)
                .addFormDataPart("isBlocked", String.valueOf(isBlocked))
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
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d("ChattingActivity", "Server Response: " + responseData);
                    runOnUiThread(() -> {
                        try {
                            JSONObject jsonResponse = new JSONObject(responseData);
                            boolean success = jsonResponse.getBoolean("success");
                            if (success) {
                                String chatting_pid = jsonResponse.getString("chatting_pid");

                                // 임시 PID를 사용하여 리스트에서 해당 메시지를 찾아 실제 PID로 업데이트
                                for (Chatting chat : chatList) {
                                    if (chat.getPid().equals(tempPid)) {
                                        chat.setPid(chatting_pid); // 실제 PID로 업데이트
                                        chattingAdapter.notifyDataSetChanged(); // 리스트 갱신
                                        break;
                                    }
                                }

                                String imagePathsArray = jsonResponse.getString("image_path");
                                if (!imagePathsArray.equals("null")) {
                                    sendMessage(my_pid + "|" + chattingroom_pid + "|" + roomname + "|" + imagePathsArray);
                                }
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
                                initChattingAdapter();
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
                                    chattingAdapter.notifyItemInserted(chatList.size() - 1);
                                    scrollToBottom();
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
