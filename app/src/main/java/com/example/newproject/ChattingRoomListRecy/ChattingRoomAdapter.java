package com.example.newproject.ChattingRoomListRecy;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newproject.Chat.ChatListRecy.Chatting;
import com.example.newproject.Chat.ChattingActivity;
import com.example.newproject.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChattingRoomAdapter extends RecyclerView.Adapter<ChattingRoomAdapter.ChattingRoomViewHolder>{
    private ArrayList<ChattingRoom> ChattingRoomList;
    private Context context;
    String pid;
    public ChattingRoomAdapter(ArrayList<ChattingRoom> ChattingRoomList, Context context, String pid) {
        this.ChattingRoomList = ChattingRoomList;
        this.context = context;
        this.pid = pid;
    }

    @NonNull
    @Override
    public ChattingRoomAdapter.ChattingRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chattingroom_list, parent, false);
        return new ChattingRoomAdapter.ChattingRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChattingRoomAdapter.ChattingRoomViewHolder holder, int position) {

        try {
            holder.tv_list_frdname.setText(ChattingRoomList.get(position).roomname);
            holder.tv_list_chat.setText(ChattingRoomList.get(position).last_msg);
            String dateTimeString = ChattingRoomList.get(position).create;
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
            SimpleDateFormat targetFormat = new SimpleDateFormat("a hh:mm", Locale.KOREA);

            Date date = originalFormat.parse(dateTimeString);
            String formattedTime = targetFormat.format(date);
            holder.tv_list_time.setText(formattedTime);

            int count = ChattingRoomList.get(position).count;
            if (count != 0) {
                holder.tv_list_count.setText(String.valueOf(count));
            } else if (count == 0) {
                holder.tv_list_count.setVisibility(View.GONE);
            }

            System.out.println(dateTimeString);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int currentPosition = holder.getAdapterPosition(); // Get the current position
                    if (currentPosition != RecyclerView.NO_POSITION) { // Ensure the position is valid
                        try {
                            // Participants 문자열을 JSONArray로 파싱
                            JSONArray participantsArray = new JSONArray(ChattingRoomList.get(currentPosition).Participants);

                            // 참가자 목록에서 pid와 일치하지 않는 항목만 새로운 JSONArray에 추가
                            ArrayList<String> friend_pid = new ArrayList<>();
                            for (int i = 0; i < participantsArray.length(); i++) {
                                String participant = participantsArray.getString(i);
                                if (!participant.equals(pid)) {
                                    friend_pid.add(participant);
                                }
                            }
                            System.out.println(friend_pid);

                            // friend_pid should now contain the desired participant's pid as a plain string
                            Intent intent = new Intent(context, ChattingActivity.class);
                            intent.putExtra("friend_pid", friend_pid);
                            intent.putExtra("roomname", ChattingRoomList.get(currentPosition).roomname);
                            intent.putExtra("my_pid", pid);
                            intent.putExtra("room_pid", ChattingRoomList.get(currentPosition).pid);

                            // Start the ChattingActivity
                            context.startActivity(intent);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("ChattingRoomAdapter", "Failed to parse participants JSON");
                        }
                    }
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
            holder.tv_list_time.setText("");
        }
    }





    @Override
    public int getItemCount() {
        return ChattingRoomList.size();
    }

    public class ChattingRoomViewHolder extends RecyclerView.ViewHolder {
        TextView tv_list_frdname, tv_list_chat, tv_list_time, tv_list_count;
        public ChattingRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_list_frdname = itemView.findViewById(R.id.tv_list_frdname);
            tv_list_chat = itemView.findViewById(R.id.tv_list_chat);
            tv_list_time = itemView.findViewById(R.id.tv_list_time);
            tv_list_count = itemView.findViewById(R.id.tv_list_count);
        }
    }
}
