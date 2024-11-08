package com.example.newproject.Chat.Socket;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// SocketClient 클래스는 비동기적으로 서버와의 소켓 통신을 관리합니다.
public class SocketClient extends AsyncTask<Void, Void, Void> {
    private String serverIP = "172.30.1.19"; // 서버 IP 주소
    private int serverPort = 8080; // 서버 포트 번호
    private String roompid; // 방 ID
    private String mypid; // 사용자 ID
    private String roomname; // 방 이름
    private Callback callback; // 메시지 수신 시 호출되는 콜백 인터페이스
    private boolean isRunning = true; // 소켓 통신이 실행 중인지 여부
    private PrintWriter out; // 서버로 메시지를 전송하기 위한 PrintWriter
    private BufferedReader in; // 서버로부터 메시지를 수신하기 위한 BufferedReader
    private Socket socket; // 서버와의 소켓 연결
    private static final Object lock = new Object(); // 동기화를 위한 락 객체

    // 메시지 수신 시 호출되는 콜백 인터페이스 정의
    public interface Callback {
        void onMessageReceived(String message);
    }

    // SocketClient 생성자
    public SocketClient(String mypid, Callback callback) {
        this.roompid = roompid; // 방 ID 설정
        this.mypid = mypid; // 사용자 ID 설정
        this.roomname = roomname; // 방 이름 설정
        this.callback = callback; // 콜백 설정
    }

    // 백그라운드에서 소켓 연결을 설정하고 메시지를 수신하는 메서드
    @Override
    protected Void doInBackground(Void... voids) {
        try {
            // 이미 소켓이 열려있으면 다시 열지 않음
            if (socket == null || socket.isClosed()) {
                socket = new Socket(serverIP, serverPort); // 서버에 소켓 연결
                out = new PrintWriter(socket.getOutputStream(), true); // 출력 스트림 초기화
                in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // 입력 스트림 초기화

                // 서버에 소켓 열림 메시지를 보냄
                sendMessage(mypid + "|" + roompid + "|" + roomname + "|" + "socket_open");
                Log.d("SocketClient", "Connected to server: " + serverIP + ":" + serverPort);

                // 서버로부터 메시지 수신 대기
                String receivedMessage;
                while (isRunning && (receivedMessage = in.readLine()) != null) {
                    Log.d("SocketClient", "Message received: " + receivedMessage);
                    if (callback != null) {
                        callback.onMessageReceived(receivedMessage); // 메시지 수신 시 콜백 호출
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
                    String msg = message.replace("\n", "").replace("\r", ""); // 메시지에서 줄바꿈 제거
                    out.println(msg); // 메시지 전송
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
                    out.close(); // 출력 스트림 닫기
                }
                if (in != null) {
                    in.close(); // 입력 스트림 닫기
                }
                if (socket != null && !socket.isClosed()) {
                    socket.close(); // 소켓 닫기
                }
                Log.d("SocketClient", "Socket and resources closed");
            } catch (IOException e) {
                Log.e("SocketClient", "IOException on close: " + e.getMessage());
            }
        }
    }

    // 소켓 연결 상태 확인 메서드
    public boolean isConnected() {
        return socket != null && !socket.isClosed(); // 소켓이 열려 있는지 여부 반환
    }
}
