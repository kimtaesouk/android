package com.example.newproject.Chat.AddChatroom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newproject.FriendDetail.FriendDetailActivity;
import com.example.newproject.FriendsListRecy.Friends;
import com.example.newproject.ItemTouchHelperListener;
import com.example.newproject.R;

import java.util.ArrayList;

public class AddChatroomFriendsAdapter2 extends RecyclerView.Adapter<AddChatroomFriendsAdapter2.AddChatroomFriendViewHolder>  {

    private ArrayList<Friends> friendsList;
    private Context context;


    private String pid;

    public AddChatroomFriendsAdapter2(ArrayList<Friends> friendsList, String pid, Context context) {
        this.friendsList = friendsList;
        this.context = context;
        this.pid = pid;
    }

    @NonNull
    @Override
    public AddChatroomFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_list4, parent, false);
        return new AddChatroomFriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddChatroomFriendViewHolder holder, int position) {
        Friends friend = friendsList.get(position);
        holder.tv_list_fdname.setText(friend.getName());
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }




    // 왼쪽 버튼 클릭 시 처리


    // UI 스레드에서 실행
    private void runOnUiThread(Runnable runnable) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(runnable);
        }
    }

    public class AddChatroomFriendViewHolder extends RecyclerView.ViewHolder {

        TextView tv_list_fdname;

        public AddChatroomFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_list_fdname = itemView.findViewById(R.id.tv_list_fdname);
        }
    }
}
