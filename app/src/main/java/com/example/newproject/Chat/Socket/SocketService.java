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
        // SocketClient 초기화 및 연결 설정
        if (intent != null) {
            String roompid = intent.getStringExtra("roompid");
            String roomname = intent.getStringExtra("roomname");
            String mypid = intent.getStringExtra("mypid");

            // 채팅방이 바뀔 때마다 소켓을 새로 설정
            if (roompid != null && !roompid.equals(currentRoomId)) {
                Log.d("SocketService", "Switching to a new room: " + roompid);

                // 기존 소켓이 연결되어 있으면 종료
                if (socketClient != null) {
                    Log.d("SocketService", "Stopping existing socket for room: " + currentRoomId);
                    socketClient.stopSocket("SWITCH_ROOM"); // 기존 소켓 종료
                }

                // 새로운 소켓 클라이언트로 연결 초기화
                currentRoomId = roompid;
                Log.d("SocketService", "Starting new socket for room: " + currentRoomId);
                socketClient = new SocketClient(roompid, mypid, roomname, new SocketClient.Callback() {
                    @Override
                    public void onMessageReceived(final String message) {
                        Log.d("SocketService", "Message received: " + message);
                        handleIncomingMessage(message, roompid, roomname, mypid);
                    }
                });
                socketClient.execute();  // 새로운 소켓 연결 시작
            }

            // 메시지 전송 로직
            String action = intent.getAction();
            if (action != null && action.equals("SEND_MESSAGE")) {
                String message = intent.getStringExtra("message");
                sendMessage(message);
            }
        }

        return START_STICKY;
    }


    // 메시지 처리 로직을 메서드로 분리
    private void handleIncomingMessage(String message, String roompid, String roomname, String mypid) {
        String[] parts = message.split(":");
        String senderId = parts[0].trim();
        String roomId = parts[1].trim();
        String msg = parts.length > 3 ? parts[3].trim() : "";
        int num = Integer.parseInt(parts[4].trim());
        int notificationId = roomId.hashCode();

        Intent broadcastIntent = new Intent("com.example.NewProject.NEW_MESSAGE");
        broadcastIntent.putExtra("message", message);
        sendBroadcast(broadcastIntent);

        // 알림 설정
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(roomId, "ChatApp", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }

        Intent notificationIntent = new Intent(SocketService.this, ChattingActivity.class);
        notificationIntent.putExtra("mypid", mypid);
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
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setLights(Color.RED, 3000, 3000)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        roomNotificationStatus.putIfAbsent(roomId, true);
        roomNotificationStatus.put(roomId, true);

        if (msg.equals("입장")) {
            if (mNotificationManager != null) {
                mNotificationManager.cancel(notificationId);
            }
            roomNotificationStatus.put(roomId, false);
        }

        if (msg.equals("퇴장")) {
            roomNotificationStatus.put(roomId, true);
        }

        if (Boolean.TRUE.equals(roomNotificationStatus.get(roomId)) && !msg.equals("퇴장") && !msg.equals("입장")) {
            mNotificationManager.notify(notificationId, notifyBuilder.build());
            startForeground(notificationId, notifyBuilder.build());
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

    public void sendMessage(String message) {
        if (socketClient != null) {
            socketClient.sendMessage(message);
        }
    }
}
