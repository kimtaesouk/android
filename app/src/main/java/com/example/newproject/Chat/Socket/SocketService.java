package com.example.newproject.Chat.Socket;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.newproject.Chat.ChattingActivity;
import com.example.newproject.R;

import java.util.HashMap;

public class SocketService extends Service {
    private SocketClient socketClient;
    private String currentRoomId;  // 현재 방 ID를 저장
    private HashMap<String, Boolean> roomNotificationStatus = new HashMap<>();
    private NotificationManager mNotificationManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            String roompid = intent.getStringExtra("roompid");
            String roomname = intent.getStringExtra("roomname");
            String mypid = intent.getStringExtra("mypid");

            switch (action) {
                case "SOCKET_OPEN":
                    initializeSocketClient(mypid);
                    break;

                case "JOIN_ROOM":
                    if (roompid != null && !roompid.equals(currentRoomId)) {
                        joinRoom(roompid, roomname, mypid);
                    }
                    break;

                case "EXIT_ROOM":
                    if (roompid != null && roompid.equals(currentRoomId)) {
                        exitRoom(roompid, roomname, mypid);
                        currentRoomId = null;  // currentRoomId 초기화
                    }
                    break;

                case "SEND_MESSAGE":
                    String message = intent.getStringExtra("message");
                    sendMessage(message);
                    break;
                case "IsBlock":
                    Log.d("SocketService", "User is blocked in room: " + roompid);
                    sendMessage(mypid + "/" + roompid + "/" + roomname + "/차단");
                    break;  // 차단 처리 후 break 추가

                case "UnBlock":
                    Log.d("SocketService", "User is unblocked in room: " + roompid);
                    sendMessage(mypid + "/" + roompid + "/" + roomname + "/차단해제");
                    break;  // 차단 해제 처리 후 break 추가

                default:
                    Log.d("SocketService", "Unknown action: " + action);
                    break;
            }
        }

        return START_STICKY;
    }

    // 소켓 초기화 (한 번만 설정)
    private void initializeSocketClient(String mypid) {
        if (socketClient == null) {
            socketClient = new SocketClient(mypid, new SocketClient.Callback() {
                @Override
                public void onMessageReceived(final String message) {
                    Log.d("SocketService", "Message received: " + message);
                    handleIncomingMessage(message, mypid);
                }
            });
            socketClient.execute();  // 소켓 연결 시작
            Log.d("SocketService", "Socket initialized for user: " + mypid);
        } else {
            Log.d("SocketService", "Socket is already initialized");
        }
    }

    // 새로운 방에 참여 (소켓 연결 유지)
    private void joinRoom(String roompid, String roomname, String mypid) {
        Log.d("SocketService", "Joining room: " + roompid);
        if (socketClient != null) {
            socketClient.sendMessage(mypid + "/" + roompid + "/" + roomname + "/입장");

            // 해당 방에 대한 알림을 받지 않도록 설정
            roomNotificationStatus.put(roompid, false);

        }
    }
    private void exitRoom(String roompid, String roomname, String mypid) {
        Log.d("SocketService", "Exiting room: " + roompid);
        if (socketClient != null) {
            socketClient.sendMessage(mypid + "/" + roompid + "/" + roomname + "/퇴장");
            // 해당 방에 대한 알림을 다시 활성화
            roomNotificationStatus.put(roompid, true);
        }
    }
    // 메시지 전송
    public void sendMessage(String message) {
        if (socketClient != null) {
            socketClient.sendMessage(message);
        }
    }
    // 메시지 도착 시 호출되는 메서드
    // 메시지 처리 로직을 메서드로 분리
    private void handleIncomingMessage(String message, String mypid) {
        String[] parts = message.split(":");
        String senderId = parts[0].trim();
        String roomId = parts[1].trim();
        String roomname = parts[2].trim();
        String msg = parts.length > 3 ? parts[3].trim() : "";
        String clients = parts.length > 4 ? parts[4].trim() : "";
        int notificationId = roomId.hashCode();

        // 메시지 브로드캐스트 (항상 실행)
        Intent broadcastIntent = new Intent("com.example.NewProject.NEW_MESSAGE");
        broadcastIntent.putExtra("message", message);
        sendBroadcast(broadcastIntent);

        // 방의 알림 상태를 처음 설정하는 경우 기본값을 true로 설정
        roomNotificationStatus.putIfAbsent(roomId, true);

        // 입장 및 퇴장 메시지에 따라 알림 상태 업데이트
        if (msg.equals("입장")) {
            if (senderId.equals(mypid)) {  // 자신이 입장한 경우에만 알림 상태 변경
                roomNotificationStatus.put(roomId, false);  // 입장 시 알림 비활성화
                Log.d("SocketService", "User has entered room " + roomId + ", disabling notifications for this room.");
            }
        } else if (msg.equals("퇴장")) {
            if (senderId.equals(mypid)) {  // 자신이 퇴장한 경우에만 알림 상태 변경
                roomNotificationStatus.put(roomId, true);   // 퇴장 시 알림 활성화
                Log.d("SocketService", "User has exited room " + roomId + ", enabling notifications for this room.");
            }
        }

        // 알림 상태에 따라 알림 표시 여부 결정
        boolean isRoomNotificationEnabled = roomNotificationStatus.get(roomId);
        if (isRoomNotificationEnabled && !msg.equals("입장") && !msg.equals("퇴장")) {
            // 알림 설정 (항상 실행)
            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(roomId, "ChatApp", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.enableLights(true);
                notificationChannel.enableVibration(true);
                mNotificationManager.createNotificationChannel(notificationChannel);
            }

            Intent notificationIntent = new Intent(SocketService.this, ChattingActivity.class);
            notificationIntent.putExtra("mypid", mypid);  // 수정: 자신의 PID 사용
            notificationIntent.putExtra("name", roomname);
            notificationIntent.putExtra("friendpid", senderId);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(SocketService.this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(SocketService.this, roomId)
                    .setContentTitle(roomname)
                    .setContentText(msg)
                    .setSmallIcon(R.drawable.ic_stat_article)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[]{1000, 1000})
                    .setLights(Color.RED, 3000, 3000)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

            Log.d("SocketService", "Sending notification for room " + roomId);
            mNotificationManager.notify(notificationId, notifyBuilder.build());
        }
    }





    @Override
    public void onDestroy() {
        super.onDestroy();
        if (socketClient != null) {
            socketClient.stopSocket("CLOSE_SOCKET");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
