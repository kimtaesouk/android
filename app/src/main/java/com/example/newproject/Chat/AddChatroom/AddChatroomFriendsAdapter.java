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
    private String pid,chattingroom_pid;
    private ArrayList<String> friend_pids = new ArrayList<>();

    public interface FriendsDataListener {
        void onDataChanged(ArrayList<Friends> size, int adapterPosition);
    }

    public AddChatroomFriendsAdapter(ArrayList<Friends> friendsList, String pid, ArrayList<String> friend_pids, String chattingroom_pid, Context context, FriendsDataListener dataListener) {
        this.friendsList = friendsList;
        this.context = context;
        this.pid = pid;
        this.dataListener = dataListener;
        this.checkfriendsList = new ArrayList<>(); // checkfriendsList 초기화
        this.chattingroom_pid = chattingroom_pid;
        this.friend_pids = friend_pids;
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

        // friend_pids에 현재 친구의 pid가 있는지 확인
        boolean isNonClickable = friend_pids.contains(friend.getPid());
        holder.cb_add_chattingroom.setEnabled(!isNonClickable); // 터치 불가 설정

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
