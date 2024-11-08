package com.example.newproject.Chat.Drawer;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newproject.R;
import com.example.newproject.singup.NetworkStatus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.ParticipantsViewHolder>{
    private List<Participants> userList;
    private ArrayList<String> friendsList;
    private Activity activity; // Context 대신 Activity로 변경
    private String my_pid;

    public ParticipantsAdapter(List<Participants> userList, ArrayList<String> friendsList, String my_pid, Activity activity) {
        this.userList = userList;
        this.friendsList = friendsList;
        this.activity = activity; // Context 대신 Activity로 저장
        this.my_pid = my_pid;
    }


    @NonNull
    @Override
    public ParticipantsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_list, parent, false);
        return new ParticipantsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantsViewHolder holder, int position) {
        Participants user = userList.get(position);
        holder.tv_list_fdname.setText(user.getName());
        if (!friendsList.contains(user.getPid()) && !my_pid.equals(user.getPid())) {
            holder.iv_question_im.setVisibility(View.VISIBLE);
            holder.ib_add_friend.setVisibility(View.VISIBLE);

            holder.ib_add_friend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setData(my_pid, user.getPid(), holder);
                }
            });
        } else {
            holder.iv_question_im.setVisibility(View.GONE);
            holder.ib_add_friend.setVisibility(View.GONE);
        }
    }

    private void setData(String my_pid, String f_pid, ParticipantsViewHolder holder) {
        int status = NetworkStatus.getConnectivityStatus(activity);
        if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
            Toast.makeText(activity, "네트워크 연결을 확인하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Set_friend.php").newBuilder();
        urlBuilder.addQueryParameter("v", "1.0");
        String url = urlBuilder.build().toString();

        RequestBody formBody = new FormBody.Builder()
                .add("my_pid", my_pid)
                .add("f_pid", f_pid)
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
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, "네트워크 요청 실패", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (!response.isSuccessful()) {
                                Log.i("tag", "응답 실패");
                                Toast.makeText(activity, "네트워크 문제 발생", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.i("tag", "응답 성공");
                                final String responseData = response.body().string();
                                Log.i("tag", "서버 응답: " + responseData);

                                try {
                                    JSONObject jsonResponse = new JSONObject(responseData);
                                    boolean success = jsonResponse.getBoolean("success");

                                    if (success) {
                                        holder.iv_question_im.setVisibility(View.GONE);
                                        holder.ib_add_friend.setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(activity, "친구추가 실패", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(activity, "응답 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
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
    public int getItemCount() {
        return userList.size();
    }

    static class ParticipantsViewHolder extends RecyclerView.ViewHolder {
        TextView tv_list_fdname;
        ImageView iv_question_im;
        ImageButton ib_add_friend;
        ParticipantsViewHolder(View itemView) {
            super(itemView);
            tv_list_fdname = itemView.findViewById(R.id.tv_list_fdname);
            iv_question_im = itemView.findViewById(R.id.iv_question_im);
            ib_add_friend = itemView.findViewById(R.id.ib_add_friend);

        }
    }
}

