package com.example.newproject.InfoFriend;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newproject.FriendsListRecy.Friends;
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

public class InfoFriendsAdapter extends RecyclerView.Adapter<InfoFriendsAdapter.FriendViewHolder>  {

    private ArrayList<Friends> friendsList;
    private Context context;

    private FriendsDataListener dataListener;

    private String pid;

    private String status;

    public interface FriendsDataListener {
        void onDataChanged(int size);
    }

    public InfoFriendsAdapter(ArrayList<Friends> friendsList, String pid, Context context, String status) {
        this.friendsList = friendsList;
        this.context = context;
        this.pid = pid;
        this.status = status;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_list2, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friends friend = friendsList.get(position);
        holder.tv_list_fdname.setText(friend.getName());

        holder.btn_manage_friend.setOnClickListener(v -> showPopupMenu(holder.btn_manage_friend, position, status));
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    private void showPopupMenu(View view, int position, String status) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        if(status.equals("hide")){
            inflater.inflate(R.menu.hide_friend_popup_menu, popupMenu.getMenu());
        } else if (status.equals("block")) {
            inflater.inflate(R.menu.block_friend_popup_menu, popupMenu.getMenu());
        }

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    // Handle delete action
                    handleAction(position, "delete");
                    return true;
                case R.id.action_unblock:
                    // Handle unblock action
                    handleAction(position, "unblock");
                    return true;
                case R.id.action_block:
                    handleAction(position, "block");
                    return true;
                case R.id.action_return:
                    handleAction(position, "return");
                    return true;
                default:
                    return false;

            }
        });
    }

    private void handleAction(int position, String action) {
        String state;
        if ("delete".equals(action)) {
            state = "delete";
        } else if ("unblock".equals(action)) {
            state = "unblock";
        } else if("block".equals(action)){
            state = "isBlock";
        }else if("return".equals(action)){
            state = "isReturn";
        }else{
            return;
        }

        setData(position, pid, state);
    }


    // 왼쪽 버튼 클릭 시 처리

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

        Button btn_manage_friend;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_list_fdname = itemView.findViewById(R.id.tv_list_fdname);
            btn_manage_friend = itemView.findViewById(R.id.btn_manage_friend);
        }
    }
}
