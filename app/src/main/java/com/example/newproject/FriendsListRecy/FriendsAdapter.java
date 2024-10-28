package com.example.newproject.FriendsListRecy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newproject.FriendDetail.FriendDetailActivity;
import com.example.newproject.ItemTouchHelperListener;
import com.example.newproject.R;
import com.example.newproject.singup.NetworkStatus;

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

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> implements ItemTouchHelperListener {

    private ArrayList<Friends> friendsList;
    private Context context;

    private FriendsDataListener dataListener;

    private String pid;

    public interface FriendsDataListener {
        void onDataChanged(int size);
    }

    public FriendsAdapter(ArrayList<Friends> friendsList, String pid, Context context, FriendsDataListener dataListener) {
        this.friendsList = friendsList;
        this.context = context;
        this.pid = pid;
        this.dataListener = dataListener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_list, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friends friend = friendsList.get(position);
        holder.tv_list_fdname.setText(friend.getName());

        holder.itemView.setOnClickListener(v -> {

            // 여기서 원하는 동작을 수행할 수 있습니다.
            // 예를 들어, 클릭한 친구의 정보를 포함한 새로운 액티비티를 시작할 수 있습니다.
            Intent intent = new Intent(context, FriendDetailActivity.class);
            intent.putExtra("my_pid" , pid);
            intent.putExtra("friend_name", friend.getName());
            intent.putExtra("friend_pid", friend.getPid());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    @Override
    public boolean onItemMove(int from_position, int to_position) {
        return false;
    }

    @Override
    public void onItemSwipe(int position) {
        // 스와이프 동작 구현
    }

    // 왼쪽 버튼 클릭 시 처리
    @Override
    public void onBlockClick(int position, RecyclerView.ViewHolder viewHolder) {
        String state = "isBlock";
        setData(position, pid, state);
        // 필요한 로직 추가
    }

    // 오른쪽 버튼 클릭 시 처리
    @Override
    public void onHideClick(int position, RecyclerView.ViewHolder viewHolder) {
        String state = "isHide";
        setData(position, pid, state);
        // 필요한 로직 추가
    }

    public void notifyDataChanged() {
        if (dataListener != null) {
            dataListener.onDataChanged(friendsList.size());
        }
        notifyDataSetChanged();
    }

    // 서버에 데이터 전송
    private void setData(int position, String my_pid, String state) {
        Friends friend = friendsList.get(position);
        String f_pid = friend.getPid();
        int status = NetworkStatus.getConnectivityStatus(context);
        if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
            Toast.makeText(context, "네트워크 연결을 확인하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // GET 방식 파라미터 추가
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Set_friends_state.php").newBuilder();
        urlBuilder.addQueryParameter("v", "1.0"); // 예시
        String url = urlBuilder.build().toString();

        // POST 방식 파라미터 추가
        RequestBody formBody = new FormBody.Builder()
                .add("my_pid", my_pid)
                .add("f_pid", f_pid)
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
                runOnUiThread(() -> Toast.makeText(context, "네트워크 요청 실패", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    try {
                        if (!response.isSuccessful()) {
                            // 응답 실패
                            Log.i("tag", "응답 실패");
                            Toast.makeText(context, "네트워크 문제 발생", Toast.LENGTH_SHORT).show();
                        } else {
                            // 응답 성공
                            Log.i("tag", "응답 성공");
                            String responseData = response.body().string();
                            Log.i("tag", "서버 응답: " + responseData);

                            try {
                                JSONObject jsonResponse = new JSONObject(responseData);
                                boolean success = jsonResponse.getBoolean("success");

                                if (success) {
                                    friendsList.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(context, "상태 변경 완료", Toast.LENGTH_SHORT).show();
                                    notifyDataChanged();
                                } else {
                                    Toast.makeText(context, "변경 실패", Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(context, "응답 처리 중 오류 발생", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    // UI 스레드에서 실행
    private void runOnUiThread(Runnable runnable) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(runnable);
        }
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder {

        TextView tv_list_fdname;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_list_fdname = itemView.findViewById(R.id.tv_list_fdname);
        }
    }
}
