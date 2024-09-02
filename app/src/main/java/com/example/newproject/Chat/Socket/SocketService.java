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

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.newproject.Chat.ChattingActivity;
import com.example.newproject.R;

import java.util.HashMap;

public class SocketService extends Service {
    private SocketClient socketClient;

    // Channel을 생성 및 전달해 줄 수 있는 Manager 생성
    private NotificationManager mNotificationManager;
    // Notivication에 대한 ID 생성
    private HashMap<String, Boolean> roomNotificationStatus = new HashMap<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // SocketClient 초기화 및 연결 설정
        System.out.println("넘어는옴" );
        String roompid = intent.getStringExtra("roompid");
        String roomname = intent.getStringExtra("roomname");
        String mypid = intent.getStringExtra("mypid");
        socketClient = new SocketClient(roompid, mypid, roomname, new SocketClient.Callback() {
            @Override
            public void onMessageReceived(final String message) {
                // 메시지 수신 처리
                String[] parts = message.split(":");
                String senderId = parts[0].trim();
                String roomId = parts[1].trim();
                String roomname = parts[2].trim();
                String msg = parts.length > 3 ? parts[3].trim() : "";
                int num = Integer.parseInt(parts[4].trim());
                int notificationId = roomId.hashCode();

                System.out.println(message);
                Intent broadcastIntent = new Intent("com.example.chatapp.NEW_MESSAGE");
                broadcastIntent.putExtra("message", message);
                sendBroadcast(broadcastIntent);

//                notification manager 생성
                mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                // 기기(device)의 SDK 버전 확인 ( SDK 26 버전 이상인지 - VERSION_CODES.O = 26)
                if(Build.VERSION.SDK_INT
                        >= Build.VERSION_CODES.O){
                    //Channel 정의 생성자( construct 이용 )
                    NotificationChannel notificationChannel = new NotificationChannel(roomId,"ChatApp", NotificationManager.IMPORTANCE_HIGH);
                    //Channel에 대한 기본 설정
                    notificationChannel.enableLights(true);
                    notificationChannel.enableVibration(true);
//                    notificationChannel.setDescription("Notification from Mascot");
                    // Manager을 이용하여 Channel 생성
                    mNotificationManager.createNotificationChannel(notificationChannel);
                }
                // 알림 탭 시 열릴 액티비티를 위한 Intent 생성
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
                        .setPriority(NotificationCompat.PRIORITY_HIGH) // 중요도 설정
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 }) // 진동 패턴
                        .setLights(Color.RED, 3000, 3000) // LED 색상 및 지속 시간
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)); // 기본 알림 소리
                roomNotificationStatus.putIfAbsent(roomId, true);
                roomNotificationStatus.put(roomId, true);
                // 입장 메시지 처리
                if (msg.equals("입장")) {
                    if (mNotificationManager != null) {
                        mNotificationManager.cancel(notificationId); // roomId의 hashCode를 사용하여 알림 ID를 생성
                    }
                    roomNotificationStatus.put(roomId, false);
                    System.out.println("입장 처리 완료");
                    System.out.println(roomNotificationStatus.size());
                    notifyBuilder.setAutoCancel(true);
                }
                // 퇴장 메시지 처리
                if (msg.equals("퇴장")) {
                    roomNotificationStatus.put(roomId, true);
                    System.out.println("퇴장 처리 완료");
                    System.out.println(roomNotificationStatus.size());
                }
                 //알림 전송 조건 확인 및 전송
                if (Boolean.TRUE.equals(roomNotificationStatus.get(roomId)) && !msg.equals("퇴장") && !msg.equals("입장")) {
                   mNotificationManager.notify(Integer.parseInt(roomId), notifyBuilder.build());
                   startForeground(notificationId, notifyBuilder.build());
                   System.out.println("알림 처리 완료");
                   System.out.println(roomNotificationStatus.get(roomId));
                }

            }
        });
        socketClient.execute();

        if (intent != null) {
            String action = intent.getAction();
            if (action != null && action.equals("SEND_MESSAGE")) {
                String message = intent.getStringExtra("message");
                sendMessage(message);
            }
        }
        return START_STICKY;
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

    // 메시지 전송 메소드 추가 (예시)
    public void sendMessage(String message) {
        if (socketClient != null) {
            socketClient.sendMessage(message);
        }
    }
}