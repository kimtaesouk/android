package com.example.newproject.InfoFriend;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newproject.FriendsListRecy.Friends;
import com.example.newproject.FriendsListRecy.FriendsAdapter;
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

public class InfoFriendFragment extends Fragment {
    private View view;


    ImageButton ib_back;

    RecyclerView friend_list;

    InfoFriendsAdapter friendsAdapter;
    ArrayList<Friends> friendsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle arguments = getArguments();

        if (arguments != null) {
            String pid = arguments.getString("pid");
            String status = arguments.getString("status");
            System.out.println(pid);
            if (status.equals("hide")){
                view = inflater.inflate(R.layout.fragment_friend_hide, container, false);
                friend_list = view.findViewById(R.id.rc_hide_friendslist);
                set_RecyclerView(status, pid);

            } else if (status.equals("block")) {
                view = inflater.inflate(R.layout.fragment_friend_block, container, false);
                friend_list = view.findViewById(R.id.rc_block_friendslist);
                set_RecyclerView(status, pid);
            }

            ib_back = view.findViewById(R.id.ib_back);
            ib_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                }
            });

        }
        return view;
    }

    private void set_RecyclerView(String f_status,String pid){

        friend_list.setLayoutManager(new LinearLayoutManager(getActivity()));
        friendsAdapter = new InfoFriendsAdapter(friendsList, pid, getActivity(), f_status);
        friend_list.setAdapter(friendsAdapter);
        getData(pid, f_status);
    }

    private void getData(String pid, String f_status) {
        int status = NetworkStatus.getConnectivityStatus(getActivity().getApplicationContext());
        if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
            Toast.makeText(getActivity().getApplicationContext(), "네트워크 연결을 확인하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // get방식 파라미터 추가
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Get_infofrdList.php").newBuilder();
        urlBuilder.addQueryParameter("v", "1.0"); // 예시
        String url = urlBuilder.build().toString();

        // POST 파라미터 추가
        RequestBody formBody = new FormBody.Builder()
                .add("pid", pid)
                .add("status", f_status)
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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (!response.isSuccessful()) {
                                // 응답 실패
                                Log.i("tag", "응답 실패");
                                Toast.makeText(getActivity().getApplicationContext(), "네트워크 문제 발생", Toast.LENGTH_SHORT).show();
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

                                        friendsAdapter.notifyDataSetChanged();

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
            }
        });
    }

}
