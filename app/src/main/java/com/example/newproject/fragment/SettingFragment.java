package com.example.newproject.fragment;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newproject.Main.Main_Activity;
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

public class SettingFragment extends Fragment {

    private View view;
    String pid;

    TextView tv_set_name;

    LinearLayout ll_setting_profile, ll_setting_friend;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_setting, container, false);

        ll_setting_profile = view.findViewById(R.id.ll_setting_profile);
        ll_setting_friend = view.findViewById(R.id.ll_setting_friend);
        tv_set_name = view.findViewById(R.id.tv_set_name);

        Bundle arguments = getArguments();
        if (arguments != null) {
            pid = arguments.getString("pid");
            setData(pid);
        }

        ll_setting_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof Main_Activity) {
                    ProfileFragment profileFragment = new ProfileFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("pid", pid);
                    ((Main_Activity) getActivity()).changeFragment(profileFragment, bundle);
                }
            }
        });
        ll_setting_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof Main_Activity) {
                    FriendFragment friendFragment = new FriendFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("pid", pid);
                    ((Main_Activity) getActivity()).changeFragment(friendFragment, bundle);
                }
            }
        });
        return view;
    }

    private void setData(String pid) {
        int status = NetworkStatus.getConnectivityStatus(getActivity().getApplicationContext());
        if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
            Toast.makeText(getActivity().getApplicationContext(), "네트워크 연결을 확인하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // get방식 파라미터 추가
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Setting_getdata.php").newBuilder();
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
                                    String name = jsonResponse.getString("name");
                                    String pf_im = jsonResponse.getString("pf_im");
                                    System.out.println(name);
                                    System.out.println(pf_im);

                                    tv_set_name.setText(name);

                                    if (success) {
                                        // 로그인 정보 저장
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