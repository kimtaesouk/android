package com.example.newproject.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.newproject.Chat.AddChatroom.AddChattingRoomActivity;
import com.example.newproject.ChattingRoomListRecy.ChattingRoom;
import com.example.newproject.ChattingRoomListRecy.ChattingRoomAdapter;
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

public class ChatFragment extends Fragment {

    private View view;

    private RecyclerView rv_chattingroom_list;

    ArrayList<ChattingRoom> chatroomsList = new ArrayList<>();

    ImageButton ib_add_chattingroom ;

    ChattingRoomAdapter chattingRoomAdapter;

    String pid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_chat, container, false);

        Bundle arguments = getArguments();

        if (arguments != null) {
            pid = arguments.getString("pid");
            getData(pid);

            rv_chattingroom_list = view.findViewById(R.id.rv_chattingroom_list);
            ib_add_chattingroom = view.findViewById(R.id.ib_add_chattingroom);
            ib_add_chattingroom.setOnClickListener(v -> onClick_add_chattingroom(pid));
        }

        return view;
    }

    private  void onClick_add_chattingroom(String pid){
        Intent intent = new Intent(getContext() , AddChattingRoomActivity.class);
        intent.putExtra("my_pid" , pid);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fragment가 다시 보일 때 데이터 새로 고침
        if (pid != null) {
            getData(pid);
        }
    }

    private void getData(String pid) {
        int status = NetworkStatus.getConnectivityStatus(getActivity().getApplicationContext());
        if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
            Toast.makeText(getActivity().getApplicationContext(), "네트워크 연결을 확인하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // get방식 파라미터 추가
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Get_chatroomlist.php").newBuilder();
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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity().getApplicationContext(), "네트워크 요청 실패", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // Activity가 null이 아닌지, Fragment가 아직 Activity에 추가되어 있는지 확인
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (!response.isSuccessful()) {
                                    Log.i("tag", "응답 실패");
                                    Toast.makeText(getActivity().getApplicationContext(), "네트워크 문제 발생", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.i("tag", "응답 성공");
                                    final String responseData = response.body().string();
                                    Log.i("tag", "서버 응답: " + responseData);

                                    // JSON 응답 처리
                                    try {
                                        JSONObject jsonResponse = new JSONObject(responseData);
                                        boolean success = jsonResponse.getBoolean("success");

                                        if (success) {
                                            // rooms 배열을 파싱
                                            JSONArray roomsArray = jsonResponse.getJSONArray("rooms");
                                            chatroomsList.clear();
                                            for (int i = 0; i < roomsArray.length(); i++) {
                                                JSONObject roomObject = roomsArray.getJSONObject(i);

                                                String roomPid = roomObject.getString("pid");
                                                String roomName = roomObject.getString("roomname");
                                                String participants = roomObject.getString("Participants");
                                                String createTime = roomObject.getString("create");
                                                int state = roomObject.getInt("state");
                                                String last_msg = roomObject.getString("last_msg");

                                                chatroomsList.add(new ChattingRoom(roomPid,roomName,participants,createTime,state, last_msg));
                                                // 여기서 각 방 정보를 RecyclerView 등에 추가하여 화면에 표시할 수 있습니다.
                                            }

                                            chattingRoomAdapter = new ChattingRoomAdapter(chatroomsList, getActivity(), pid);
                                            rv_chattingroom_list.setLayoutManager(new LinearLayoutManager(getActivity()));
                                            rv_chattingroom_list.setAdapter(chattingRoomAdapter);
                                        } else {
                                            Toast.makeText(getActivity().getApplicationContext(), "데이터 로드 실패 했습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Toast.makeText(getActivity().getApplicationContext(), "응답 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    Log.w("ChatFragment", "Fragment is not attached to an activity.");
                }
            }


        });
    }

}