package com.example.newproject.Chat.AddChatroom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newproject.FriendsListRecy.Friends;
import com.example.newproject.ItemTouchHelperListener;
import com.example.newproject.R;

import java.util.ArrayList;

public class AddChatroomFriendsAdapter extends RecyclerView.Adapter<AddChatroomFriendsAdapter.AddChatroomFriendViewHolder> implements ItemTouchHelperListener {

    private ArrayList<Friends> friendsList;
    private ArrayList<Friends> checkfriendsList; // 체크된 친구 리스트
    private Context context;
    private AddChatroomFriendsAdapter.FriendsDataListener dataListener;
    private String pid;

    public interface FriendsDataListener {
        void onDataChanged(ArrayList<Friends> size, int adapterPosition);
    }

    public AddChatroomFriendsAdapter(ArrayList<Friends> friendsList, String pid, Context context, AddChatroomFriendsAdapter.FriendsDataListener dataListener) {
        this.friendsList = friendsList;
        this.context = context;
        this.pid = pid;
        this.dataListener = dataListener;
        this.checkfriendsList = new ArrayList<>(); // checkfriendsList 초기화
    }

    @NonNull
    @Override
    public AddChatroomFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_list3, parent, false);
        return new AddChatroomFriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddChatroomFriendViewHolder holder, int position) {
        Friends friend = friendsList.get(position);
        holder.tv_list_fdname.setText(friend.getName());

        // 현재 아이템에 대한 체크박스 상태 설정
        boolean isChecked = checkfriendsList.contains(friend);
        holder.cb_add_chattingroom.setOnCheckedChangeListener(null); // 기존 리스너 제거
        holder.cb_add_chattingroom.setChecked(isChecked); // 체크박스 상태 설정

        holder.cb_add_chattingroom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    Friends currentFriend = friendsList.get(adapterPosition);

                    if (isChecked) {
                        if (!checkfriendsList.contains(currentFriend)) {
                            checkfriendsList.add(currentFriend);
                            notifyDataChanged(adapterPosition);
                        }
                    } else {
                        checkfriendsList.remove(currentFriend);
                        notifyDataChanged(adapterPosition);
                    }

                    // 여기서는 notifyItemChanged(adapterPosition);을 사용할 필요는 없음
                    // 모든 상태 변경은 이미 리스너 안에서 수행됨
                }
            }
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
    public void onItemSwipe(int position) {}

    @Override
    public void onBlockClick(int position, RecyclerView.ViewHolder viewHolder) {}

    @Override
    public void onHideClick(int position, RecyclerView.ViewHolder viewHolder) {}

    public void notifyDataChanged(int adapterPosition) {
        if (dataListener != null) {
            dataListener.onDataChanged(checkfriendsList, adapterPosition);
        }
        notifyDataSetChanged();
    }

    public class AddChatroomFriendViewHolder extends RecyclerView.ViewHolder {
        TextView tv_list_fdname;
        CheckBox cb_add_chattingroom;

        public AddChatroomFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_list_fdname = itemView.findViewById(R.id.tv_list_fdname);
            cb_add_chattingroom = itemView.findViewById(R.id.cb_add_chattingroom);
        }
    }
}
