package com.example.newproject.fragment;

import static android.companion.CompanionDeviceManager.RESULT_OK;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newproject.AddFriend.Ch_Activity;
import com.example.newproject.FriendsListRecy.Friends;
import com.example.newproject.FriendsListRecy.FriendsAdapter;
import com.example.newproject.ItemTouchHelperCallback;
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

public class HomeFragment extends Fragment {

    static int REQUEST_CODE_ADD_FRIEND = 2001 ;

    View view;

    ImageButton ib_add_friend;

    TextView tv_home_myname, tv_fr_count;

    RecyclerView rv_friend_list;

    String pid;

    FriendsAdapter friendsAdapter;
    ArrayList<Friends> friendsList = new ArrayList<>();

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);


        Bundle arguments = getArguments();
        if (arguments != null) {
            pid = arguments.getString("pid");
            getData(pid);
        }

        ib_add_friend = view.findViewById(R.id.ib_add_friend);
        tv_home_myname = view.findViewById(R.id.tv_home_myname);
        tv_fr_count = view.findViewById(R.id.tv_fr_count);
        rv_friend_list = view.findViewById(R.id.rv_friend_list);

        friendsAdapter = new FriendsAdapter(friendsList, pid, getActivity(), new FriendsAdapter.FriendsDataListener() {
            @Override
            public void onDataChanged(int size) {
                // Update friend count TextView
                tv_fr_count.setText(size + " 명");
            }
        });
        rv_friend_list.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv_friend_list.setAdapter(friendsAdapter);

        // ItemTouchHelper 연결
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(friendsAdapter, getActivity()));
        itemTouchHelper.attachToRecyclerView(rv_friend_list);



        ib_add_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Ch_Activity.class);
                intent.putExtra("pid", pid);
                startActivityForResult(intent, REQUEST_CODE_ADD_FRIEND);
            }
        });
        return view;
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
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Get_frdlist.php").newBuilder();
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
                                        String name = user.getString("name");
                                        String pfIm = user.isNull("pf_im") ? null : user.getString("pf_im");
                                        String friendsCount = user.getString("friends_count");

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

                                        // UI에 데이터 설정
                                        tv_home_myname.setText(name);
                                        tv_fr_count.setText(friendsCount + " 명");
                                        // 프로필 이미지와 친구 목록은 필요에 따라 추가로 설정

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_FRIEND && resultCode == RESULT_OK) {
            // 친구 추가 후 RecyclerView를 새로 고칩니다.
            getData(pid);
            friendsAdapter.notifyDataSetChanged();
        }
    }
}