package com.example.newproject.fragment;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newproject.InfoChange_Ac.BirthChange_Activity;
import com.example.newproject.InfoChange_Ac.IdChange_Activity;
import com.example.newproject.InfoChange_Ac.NameChange_Activity;
import com.example.newproject.InfoChange_Ac.PwChange_Activity1;
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

public class ProfileFragment extends Fragment {
    private View view;
    ImageButton ib_back;

    TextView tv_pf_name, tv_pf_email, tv_pf_birth, tv_pf_id;

    ImageView iv_profile_im;

    LinearLayout ll_pf_name, ll_pf_birth, ll_pf_id, ll_pf_pw;

    private ActivityResultLauncher<Intent> nameChangeLauncher, birthChangeLauncher, idChangeLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        ib_back = view.findViewById(R.id.ib_back);
        tv_pf_name = view.findViewById(R.id.tv_pf_name);
        tv_pf_email = view.findViewById(R.id.tv_pf_email);
        tv_pf_birth = view.findViewById(R.id.tv_pf_birth);
        tv_pf_id = view.findViewById(R.id.tv_pf_id);
        iv_profile_im = view.findViewById(R.id.iv_profile_im);
        ll_pf_name = view.findViewById(R.id.ll_pf_name);
        ll_pf_birth = view.findViewById(R.id.ll_pf_birth);
        ll_pf_id = view.findViewById(R.id.ll_pf_id);
        ll_pf_pw = view.findViewById(R.id.ll_pf_pw);

        nameChangeLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        String newName = result.getData().getStringExtra("new_name");
                        if (newName != null) {
                            tv_pf_name.setText(newName);
                        }
                    }
                }
        );

        birthChangeLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        String newBirth = result.getData().getStringExtra("new_birth");
                        if (newBirth != null) {
                            tv_pf_birth.setText(newBirth);
                        }
                    }
                }
        );
        idChangeLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        String newId = result.getData().getStringExtra("new_id");
                        if (newId != null) {
                            tv_pf_id.setText(newId);
                        }
                    }
                }
        );


        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
            }
        });

        Bundle arguments = getArguments();
        if (arguments != null) {
            String pid = arguments.getString("pid");
            System.out.println(pid);
            setData(pid);

            ll_pf_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), NameChange_Activity.class);
                    intent.putExtra("pid" , pid);
                    intent.putExtra("name" , tv_pf_name.getText().toString());
                    nameChangeLauncher.launch(intent);
                }
            });

            ll_pf_birth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), BirthChange_Activity.class);
                    intent.putExtra("pid" , pid);
                    intent.putExtra("birth" , tv_pf_birth.getText().toString());
                    birthChangeLauncher.launch(intent);
                }
            });
            ll_pf_id.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), IdChange_Activity.class);
                    intent.putExtra("pid" , pid);
                    intent.putExtra("id" , tv_pf_id.getText().toString());
                    idChangeLauncher.launch(intent);
                }
            });
            ll_pf_pw.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), PwChange_Activity1.class);
                    intent.putExtra("pid" , pid);
                    intent.putExtra("email" , tv_pf_email.getText().toString());
                    idChangeLauncher.launch(intent);
                }
            });
        }





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
                                    String email = jsonResponse.getString("email");
                                    String birth = jsonResponse.getString("birth");
                                    String pf_im = jsonResponse.getString("pf_im");
                                    String id = jsonResponse.getString("id");

                                    tv_pf_name.setText(name);
                                    tv_pf_email.setText(email);
                                    tv_pf_birth.setText(birth);
                                    tv_pf_id.setText(id);
                                    if(pf_im.equals(null)){
                                        iv_profile_im.setImageResource(R.drawable.user);
                                    }



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
