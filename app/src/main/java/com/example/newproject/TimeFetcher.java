package com.example.newproject;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.IOException;

public class TimeFetcher {
    private static final String TIME_API_URL = "http://worldtimeapi.org/api/timezone/Etc/UTC";

    public interface TimeCallback {
        void onTimeReceived(String currentTime);
        void onError(String error);
    }

    public void getCurrentTimeFromInternet(TimeCallback callback) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(TIME_API_URL)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Failed to get time: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        String dateTime = jsonObject.getString("datetime");
                        // "yyyy-MM-dd HH:mm:ss" 형식으로 변환
                        String formattedTime = dateTime.substring(0, 19).replace("T", " ");
                        callback.onTimeReceived(formattedTime);
                    } catch (Exception e) {
                        callback.onError("Error parsing time: " + e.getMessage());
                    }
                } else {
                    callback.onError("Failed to get time");
                }
            }
        });
    }
}

