package com.example.newproject.Chat.Socket;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient extends AsyncTask<Void, Void, Void> {
    private String serverIP = "172.30.1.48"; // 서버 IP를 적절히 변경하세요.
    private int serverPort = 4040; // 사용하는 포트로 변경하세요.
    private String roompid;
    private String mypid;
    private String roomname;
    private Callback callback;
    private boolean isRunning = true;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    public interface Callback {
        void onMessageReceived(String message);
    }

    public SocketClient(String roompid, String mypid, String roomname, Callback callback) {
        this.roompid = roompid;
        this.mypid = mypid;
        this.callback = callback;
        this.roomname = roomname;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            socket = new Socket(serverIP, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 서버에 소켓 열림 메시지를 보냄
            out.println(mypid + "/" + roompid + "/" + " " + "/" + "socket_open");

            Log.d("SocketClient", "Connected to server: " + serverIP + ":" + serverPort);

            // 서버로부터 메시지 수신 대기
            String receivedMessage;
            while (isRunning && (receivedMessage = in.readLine()) != null) {
                Log.d("SocketClient", "Message received: " + receivedMessage);
                if (callback != null) {
                    callback.onMessageReceived(receivedMessage);
                }
            }
        } catch (IOException e) {
            Log.e("SocketClient", "IOException: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }

    // 메시지를 소켓을 통해 전송하는 메서드
    public void sendMessage(final String message) {
        new Thread(() -> {
            if (out != null && !socket.isClosed()) {
                synchronized (out) {
                    out.println(message);
                    Log.d("SocketClient", "Message sent: " + message);
                }
            } else {
                Log.e("SocketClient", "Socket is not connected or output stream is null");
            }
        }).start();
    }

    // 소켓 연결을 중단하고 자원을 정리하는 메서드
    public void stopSocket(final String message) {
        isRunning = false;
        sendMessage(message); // 서버에 종료 메시지 전송 (예: "socket_close")
        closeResources();     // 자원 정리
    }

    // 소켓과 스트림을 닫는 메서드
    private void closeResources() {
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
