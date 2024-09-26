package com.example.newproject.Chat.ChatListRecy;

import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.newproject.Chat.DetailProfile_to_Chat;
import com.example.newproject.R;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChattingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // 채팅 리스트 및 컨텍스트, 사용자 ID
    private List<Object> chatListWithDates; // 날짜가 포함된 새로운 리스트
    private Context context;
    private String pid, roompid;

    // 뷰 타입 정의
    private static final int VIEW_TYPE_DATE = 0;
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    // 생성자
    public ChattingAdapter(List<Chatting> ChattingList, Context context, String pid, String roompid) {
        this.context = context;
        this.pid = pid;
        this.chatListWithDates = generateChatListWithDates(ChattingList); // 날짜가 포함된 리스트 생성
        this.roompid = roompid;
    }

    // 날짜가 포함된 새로운 리스트를 생성하는 메서드
    private List<Object> generateChatListWithDates(List<Chatting> chattingList) {
        List<Object> chatListWithDates = new ArrayList<>();
        String lastDate = "";

        for (Chatting chatting : chattingList) {
            String currentDate = chatting.getCreate().split(" ")[0];
            if (!currentDate.equals(lastDate)) {
                chatListWithDates.add(currentDate); // 날짜를 리스트에 추가
                lastDate = currentDate;
            }
            chatListWithDates.add(chatting); // 메시지 추가
        }
        return chatListWithDates;
    }

    @Override
    public int getItemCount() {
        return chatListWithDates.size(); // 전체 아이템 수 반환
    }

    @Override
    public int getItemViewType(int position) {
        // 아이템이 날짜인지 메시지인지에 따라 뷰 타입을 결정
        if (chatListWithDates.get(position) instanceof String) {
            return VIEW_TYPE_DATE;
        } else {
            Chatting chatting = (Chatting) chatListWithDates.get(position);
            return chatting.getSender_pid().equals(pid) ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        // 뷰 타입에 따라 ViewHolder를 생성
        if (viewType == VIEW_TYPE_DATE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_date, parent, false);
            return new DateViewHolder(view);
        } else if (viewType == VIEW_TYPE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_mine, parent, false);
            return new SentViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_other, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);

        // 날짜를 표시하는 아이템인 경우
        if (viewType == VIEW_TYPE_DATE) {
            DateViewHolder dateHolder = (DateViewHolder) holder;
            String dateString = (String) chatListWithDates.get(position);
            dateHolder.tv_chat_date.setText(formatDate(dateString));
        }
        // 내가 보낸 메시지를 표시하는 아이템인 경우
        else if (viewType == VIEW_TYPE_SENT) {
            SentViewHolder sentHolder = (SentViewHolder) holder;

            if (chatListWithDates.get(position) instanceof Chatting) {
                Chatting chatting = (Chatting) chatListWithDates.get(position);

                // 이미지 메시지 처리
                if (chatting.getImagePath() != null && !chatting.getImagePath().equals("null")) {
                    sentHolder.cv_sent_image.setVisibility(View.VISIBLE);
                    sentHolder.ll_sent_msg.setVisibility(View.GONE);
                    sentHolder.iv_sent_image.setVisibility(View.VISIBLE); // 이미지가 있을 때만 보임
                    sentHolder.tv_sent_msg.setVisibility(View.GONE); // 이미지가 있을 경우 텍스트 숨김
                    Glide.with(context).load(chatting.getImagePath()).into(sentHolder.iv_sent_image);
                } else {
                    sentHolder.cv_sent_image.setVisibility(View.GONE);
                    sentHolder.ll_sent_msg.setVisibility(View.VISIBLE);
                    sentHolder.iv_sent_image.setVisibility(View.GONE); // 이미지가 없을 때는 감춤
                    sentHolder.tv_sent_msg.setVisibility(View.VISIBLE); // 이미지가 없을 경우 텍스트 표시
                    sentHolder.tv_sent_msg.setText(chatting.msg);
                }

                // 메시지 리더 카운트
                if (chatting.count <= 0) {
                    sentHolder.tv_sent_count.setText(" ");
                } else {
                    sentHolder.tv_sent_count.setText(String.valueOf(chatting.count));
                }

                // 시간 표시
                String dateTimeString = chatting.create;
                SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
                SimpleDateFormat targetFormat = new SimpleDateFormat("a hh:mm", Locale.KOREA);
                try {
                    Date date = originalFormat.parse(dateTimeString);
                    String formattedTime = targetFormat.format(date);
                    sentHolder.tv_sent_time.setText(formattedTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                    sentHolder.tv_sent_time.setText("");
                }
            }
        }
        // 다른 사람이 보낸 메시지를 표시하는 아이템인 경우
        else if (viewType == VIEW_TYPE_RECEIVED) {
            ReceivedViewHolder receivedHolder = (ReceivedViewHolder) holder;

            if (chatListWithDates.get(position) instanceof Chatting) {
                Chatting chatting = (Chatting) chatListWithDates.get(position);

                // 이미지 메시지 처리
                if (chatting.getImagePath() != null && !chatting.getImagePath().equals("null")) {
                    receivedHolder.cv_other_image.setVisibility(View.VISIBLE);
                    receivedHolder.ll_other_msg.setVisibility(View.GONE);
                    receivedHolder.iv_other_image.setVisibility(View.VISIBLE); // 이미지가 있을 때만 보임
                    receivedHolder.tv_other_msg.setVisibility(View.GONE); // 이미지가 있을 경우 텍스트 숨김
                    Glide.with(context).load(chatting.getImagePath()).into(receivedHolder.iv_other_image);
                } else  {
                    receivedHolder.cv_other_image.setVisibility(View.GONE);
                    receivedHolder.ll_other_msg.setVisibility(View.VISIBLE);
                    receivedHolder.iv_other_image.setVisibility(View.GONE); // 이미지가 없을 때는 감춤
                    receivedHolder.tv_other_msg.setVisibility(View.VISIBLE); // 이미지가 없을 경우 텍스트 표시
                    receivedHolder.tv_other_msg.setText(chatting.msg);
                }

                // 메시지 리더 카운트
                if (chatting.count <= 0) {
                    receivedHolder.tv_other_reder.setText(" ");
                } else {
                    receivedHolder.tv_other_reder.setText(String.valueOf(chatting.count));
                }

                // 시간 표시
                String dateTimeString = chatting.create;
                SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat targetFormat = new SimpleDateFormat("a hh:mm");
                try {
                    Date date = originalFormat.parse(dateTimeString);
                    String formattedTime = targetFormat.format(date);
                    receivedHolder.tv_other_time.setText(formattedTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                    receivedHolder.tv_other_time.setText("");
                }

                // 사용자 이름
                receivedHolder.tv_other_name.setText(chatting.sender_name);

                // 프로필 이미지 클릭 시 프로필 상세로 이동
                receivedHolder.iv_profile.setOnClickListener(v -> {
                    Intent intent = new Intent(context, DetailProfile_to_Chat.class);
                    intent.putExtra("mypid", pid);
                    intent.putExtra("friend_pid", chatting.sender_pid);
                    intent.putExtra("roompid", roompid);
                    context.startActivity(intent);
                });
            }
        }
    }
    // 날짜 포맷팅 메서드
    private String formatDate(String dateStr) {
        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy년 MM월 dd일 E요일", Locale.KOREAN);
        try {
            Date date = originalFormat.parse(dateStr);
            return targetFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateStr;
        }
    }

    // 날짜를 표시하는 ViewHolder 클래스
    public static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView tv_chat_date;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_chat_date = itemView.findViewById(R.id.tv_chat_date);
        }
    }

    // 내가 보낸 메시지를 표시하는 ViewHolder 클래스
    public static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView tv_sent_msg, tv_sent_time, tv_sent_count;
        ImageView iv_sent_image;  // 이미지 추가
        LinearLayout ll_sent_msg;

        CardView cv_sent_image;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_sent_msg = itemView.findViewById(R.id.tv_sent_msg);
            tv_sent_time = itemView.findViewById(R.id.tv_sent_time);
            tv_sent_count = itemView.findViewById(R.id.tv_sent_count);
            iv_sent_image = itemView.findViewById(R.id.iv_sent_image);  // 이미지뷰 추가
            ll_sent_msg = itemView.findViewById(R.id.ll_sent_msg);
            cv_sent_image = itemView.findViewById(R.id.cv_sent_image);

        }
    }

    // 다른 사람이 보낸 메시지를 표시하는 ViewHolder 클래스
    public static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView tv_other_time, tv_other_msg, tv_other_name, tv_other_reder;
        ImageView iv_profile, iv_other_image;  // 이미지 추가
        LinearLayout ll_other_msg;
        CardView cv_other_image;

        public ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_other_msg = itemView.findViewById(R.id.tv_other_msg);
            tv_other_time = itemView.findViewById(R.id.tv_other_time);
            tv_other_name = itemView.findViewById(R.id.tv_other_name);
            tv_other_reder = itemView.findViewById(R.id.tv_other_count);
            iv_profile = itemView.findViewById(R.id.iv_profile);
            iv_other_image = itemView.findViewById(R.id.iv_other_image);  // 이미지뷰 추가
            ll_other_msg = itemView.findViewById(R.id.ll_other_msg);
            cv_other_image = itemView.findViewById(R.id.cv_other_image);
        }
    }
}

