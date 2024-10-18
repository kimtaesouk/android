package com.example.newproject.Chat.Socket;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient extends AsyncTask<Void, Void, Void> {
    private String serverIP = "192.168.0.13"; // 서버 IP를 적절히 변경하세요.
    private int serverPort = 8080; // 사용하는 포트로 변경하세요.
    private String roompid;
    private String mypid;
    private String roomname;
    private Callback callback;
    private boolean isRunning = true;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    private static final Object lock = new Object(); // 동기화를 위한 락 객체

    public interface Callback {
        void onMessageReceived(String message);
    }

    public SocketClient(String mypid ,Callback callback) {
        this.roompid = roompid;
        this.mypid = mypid;
        this.roomname = roomname;
        this.callback = callback;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            // 이미 소켓이 열려있으면 다시 열지 않음
            if (socket == null || socket.isClosed()) {
                socket = new Socket(serverIP, serverPort);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // 서버에 소켓 열림 메시지를 보냄
                sendMessage(mypid + "|" + roompid + "|" + roomname + "|" + "socket_open");
                Log.d("SocketClient", "Connected to server: " + serverIP + ":" + serverPort);

                // 서버로부터 메시지 수신 대기
                String receivedMessage;
                while (isRunning && (receivedMessage = in.readLine()) != null) {
                    Log.d("SocketClient", "Message received: " + receivedMessage);
                    if (callback != null) {
                        callback.onMessageReceived(receivedMessage);
                    }
                }
            }
        } catch (IOException e) {
            Log.e("SocketClient", "IOException: " + e.getMessage());
        } finally {
            closeResources(); // 예외 발생 시 자원 해제
        }
        return null;
    }

    // 메시지를 소켓을 통해 전송하는 메서드
    public void sendMessage(final String message) {
        new Thread(() -> {
            synchronized (lock) {
                if (out != null && socket != null && !socket.isClosed()) {
                    String msg = message.replace("\n", "").replace("\r", "");
                    out.println(msg);
                    Log.d("SocketClient", "Message sent: " + message);
                } else {
                    Log.e("SocketClient", "Socket is not connected or output stream is null");
                }
            }
        }).start();
    }

    // 소켓 연결을 중단하고 자원을 정리하는 메서드
    public void stopSocket(final String message) {
        isRunning = false; // 루프 중단
        if (message != null) {
            sendMessage(message); // 서버에 종료 메시지 전송
        }
        closeResources(); // 자원 해제
    }

    // 소켓과 스트림을 닫는 메서드
    private void closeResources() {
        synchronized (lock) {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                Log.d("SocketClient", "Socket and resources closed");
            } catch (IOException e) {
                Log.e("SocketClient", "IOException on close: " + e.getMessage());
            }
        }
    }

    // 소켓 연결 상태 확인 메서드
    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }
}
