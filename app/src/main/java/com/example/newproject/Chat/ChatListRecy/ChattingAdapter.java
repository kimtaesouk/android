package com.example.newproject.Chat.ChatListRecy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newproject.Chat.DetailProfile_to_Chat;
import com.example.newproject.R;
import com.example.newproject.singup.NetworkStatus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChattingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // 채팅 리스트 및 컨텍스트, 사용자 ID
    private List<Object> chatListWithDates; // 날짜가 포함된 새로운 리스트
    private Context context;
    private Activity activity;  // Activity 추가
    private String pid, roompid;
    private OnChatUpdateListener chatUpdateListener;

    // 뷰 타입 정의
    private static final int VIEW_TYPE_DATE = 0;
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    // 생성자
    public ChattingAdapter(Activity activity, List<Chatting> ChattingList, Context context,
                           String pid, String roompid, OnChatUpdateListener listener) {
        this.activity = activity;
        this.context = context;
        this.pid = pid;
        this.chatListWithDates = generateChatListWithDates(ChattingList);
        this.roompid = roompid;
        this.chatUpdateListener = listener;
    }
    public interface OnChatUpdateListener {
        void onChatDeleted(String chatPid, String option);
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

                sentHolder.ll_sent_msg.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        // PopupMenu 생성 및 설정
                        PopupMenu popup = new PopupMenu(context, v);
                        popup.inflate(R.menu.chatting_sent_menu);  // 메뉴 리소스 설정

                        // 메뉴 항목 클릭 리스너
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(android.view.MenuItem item) {
                                if (item.getItemId() == R.id.action_sent_chat_copy) {
                                    // 메시지 텍스트를 클립보드에 복사
                                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                    android.content.ClipData clip = android.content.ClipData.newPlainText("복사된 메시지", chatting.msg);
                                    clipboard.setPrimaryClip(clip);

                                    // 복사 완료 토스트 메시지 표시
                                    android.widget.Toast.makeText(context, "메시지가 복사되었습니다", android.widget.Toast.LENGTH_SHORT).show();
                                    return true;
                                } else if (item.getItemId() == R.id.action_sent_chat_delet) {
                                    int chat_count = chatting.count;
                                    if (chat_count == 0 || chatting.msg.equals("삭제된 메시지입니다")) {
                                        // activity를 직접 사용하여 다이얼로그 생성
                                        popupmeun(chatting.count , "only" , chatting);
                                    } else {
                                        // chat_count가 0이 아닐 때 실행할 코드
                                        PopupMenu popup2 = new PopupMenu(context, v);
                                        popup2.inflate(R.menu.chatting_delet_menu);  // 메뉴 리소스 설정
                                        popup2.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                            @Override
                                            public boolean onMenuItemClick(MenuItem item) {
                                                if (item.getItemId() == R.id.action_all_client_delet){
                                                    // activity를 직접 사용하여 다이얼로그 생성
                                                    popupmeun(chatting.count , "all" , chatting);
                                                } else if (item.getItemId() == R.id.action_only_client_delet) {
                                                    // activity를 직접 사용하여 다이얼로그 생성
                                                    popupmeun(chatting.count , "only" , chatting);

                                                }
                                                return false;
                                            }
                                        });
                                        popup2.show();
                                        return true;
                                    }
                                }

                                return false;
                            }
                        });

                        popup.show();  // 메뉴 표시
                        return true;
                    }
                });

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
                // ll_other_msg를 길게 클릭했을 때 메뉴를 표시
                receivedHolder.ll_other_msg.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        // PopupMenu 생성 및 설정
                        PopupMenu popup = new PopupMenu(context, v);
                        popup.inflate(R.menu.chatting_other_menu);  // 메뉴 리소스 설정

                        // 메뉴 항목 클릭 리스너
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(android.view.MenuItem item) {
                                if (item.getItemId() == R.id.action_other_chat_copy) {
                                    // 메시지 텍스트를 클립보드에 복사
                                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                    android.content.ClipData clip = android.content.ClipData.newPlainText("복사된 메시지", chatting.msg);
                                    clipboard.setPrimaryClip(clip);

                                    // 복사 완료 토스트 메시지 표시
                                    android.widget.Toast.makeText(context, "메시지가 복사되었습니다", android.widget.Toast.LENGTH_SHORT).show();
                                    return true;
                                }
                                return false;
                            }
                        });

                        popup.show();  // 메뉴 표시
                        return true;
                    }
                });

                // 프로필 이미지 클릭 시 프로필 상세로 이동
                receivedHolder.iv_profile.setOnClickListener(v -> {
                    Intent intent = new Intent(context, DetailProfile_to_Chat.class);
                    intent.putExtra("mypid", pid);
                    intent.putExtra("friend_pid", chatting.sender_pid);
                    intent.putExtra("roompid", roompid);
                    activity.startActivityForResult(intent, 123);  // startActivityForResult() 호출
                });
            }
        }
    }
    private  void popupmeun(int count, String option, Chatting chatting){

        if (!activity.isFinishing()) {
            // AlertDialog를 사용하여 "예/아니오" 선택 제공
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            if (count == 0 || option.equals("only") ){
                builder.setTitle("이 기기에서 삭제");
                builder.setMessage("현재 사용 중인 기기에서만 삭제 되며\n상대방의 채팅방에서는 삭제되지 않습니다.");
            }else{
                builder.setTitle("모든 대화 상대에게서 삭제");
                builder.setMessage("선택한 메시지를 모든 대화 상대의 \n채팅방 화면에서 삭제 합니다.");

            }

            // "예" 버튼 설정
            builder.setPositiveButton("삭제", (dialog, which) -> {
                // "예"를 선택했을 때 실행할 코드
                setUpadeChat(chatting.pid, chatting.sender_pid, option);
            });

            // "아니오" 버튼 설정
            builder.setNegativeButton("취소", (dialog, which) -> {
                dialog.dismiss();  // 다이얼로그 닫기
            });

            // 다이얼로그 표시
            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }
    private void setUpadeChat(String chat_pid, String my_pid, String option) {
        int status = NetworkStatus.getConnectivityStatus(activity);
        if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
            Log.e("ChattingActivity", "네트워크 연결을 확인하세요.");
            return;
        }
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://49.247.32.169/NewProject/Update_Chatting_msg.php").newBuilder();
        urlBuilder.addQueryParameter("v", "1.0");
        String url = urlBuilder.build().toString();
        RequestBody formBody = new FormBody.Builder()
                .add("chat_pid", chat_pid)
                .add("my_pid", my_pid)
                .add("delete_option", option)
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
                activity.runOnUiThread(() -> Log.e("ChattingActivity", "네트워크 요청 실패"));
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                activity.runOnUiThread(() -> {
                    try {
                        if (!response.isSuccessful()) {
                            Log.e("ChattingActivity", "응답 실패");
                        } else {
                            Log.i("ChattingActivity", "응답 성공");
                            final String responseData = response.body().string();
                            Log.i("ChattingActivity setData4", "서버 응답: " + responseData);

                            try {
                                JSONObject jsonResponse = new JSONObject(responseData);
                                boolean success = jsonResponse.getBoolean("success");
                                if (success){
                                    if (chatUpdateListener != null) {
                                        chatUpdateListener.onChatDeleted(chat_pid, option);
                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e("ChattingActivity", "응답 처리 중 오류가 발생했습니다.");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
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

