package com.example.newproject.Chat.Socket;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient extends AsyncTask<Void, Void, Void> {
    String serverIP = "172.20.10.3"; // 실제 서버 IP로 변경하세요.
    int serverPort = 4040; // 사용하는 포트로 변경하세요.
    private String roompid;
    private String mypid;
    private String roomname;
    private Callback callback;
    private boolean isRunning = true;
    private PrintWriter out;
    private Socket socket;
    private boolean isInitialized = false; // PrintWriter 초기화 상태 플래그

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
            isInitialized = true; // PrintWriter가 초기화되었음을 표시
            out.println(mypid + "/" + roompid + "/" +  " " + "/" +"socket_open");

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (isRunning) {
                String receivedMessage = in.readLine();
                if (callback != null && receivedMessage != null) {
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

    private void closeResources() {
        try {
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            Log.e("SocketClient", "IOException on close: " + e.getMessage());
        }
    }

    public void sendMessage(final String message) {
        new Thread(() -> {
            synchronized (this) {
                try {
                    socket = new Socket(serverIP, serverPort);
                    out = new PrintWriter(socket.getOutputStream(), true);
                    if (out != null ) {
                        out.println(message);
                    } else {
                        System.out.println(isInitialized);
                        Log.e("SocketClient", "PrintWriter not initialized or socket is not connected");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }



    public void stopSocket(final String message) {
        isRunning = false;
        sendMessage(message); // Send CLOSE_MESSAGE or any final message
        closeResources(); // Close the socket and other resources immediately
    }
}
