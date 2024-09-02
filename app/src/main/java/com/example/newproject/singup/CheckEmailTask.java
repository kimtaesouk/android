package com.example.newproject.singup;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class CheckEmailTask extends AsyncTask<String, Void, String> {

    private Context mContext;

    public CheckEmailTask(Context context) {
        mContext = context;
    }


    @Override
    protected String doInBackground(String... params) {
        String email = params[0];
        String response = "";
        try {
            URL url = new URL("http://49.247.32.169/NewProject/Signup_check.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream out = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            String postData = "email=" + email;
            writer.write(postData);
            writer.flush();
            writer.close();
            out.close();
            System.out.println(email);

            conn.connect();

            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                response += line;
            }
            reader.close();
            in.close();
            conn.disconnect();



        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(response);
        return response;
    }

    public void onPostExecute(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            boolean success = jsonObject.getBoolean("success");
            if (!success) {

            } else {
                // 중복된 이메일이 있는 경우
                Toast.makeText(mContext, "중복된 이메일이 있습니다.", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



}
