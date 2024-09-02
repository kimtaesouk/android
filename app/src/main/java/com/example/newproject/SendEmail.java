package com.example.newproject;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.newproject.singup.GMailSender;
import com.example.newproject.singup.GPassWordSender;

import javax.mail.MessagingException;
import javax.mail.SendFailedException;


public class SendEmail {
    private static String email = "kimtsnaeun@gmail.com"; // 보내는 계정의 id
    private static String password = "hbbywraxpsjdrrjb"; // 보내는 계정의 pw
    private GMailSender gMailSender = new GMailSender(email , password);

    private GPassWordSender gPassWordSender = new GPassWordSender(email, password);

    public String emailCode = gMailSender.getEmailCode();

    String tempPassword = gPassWordSender.getTemPassword();

    public void sendSecurityCode(final Context context, final String sendTo) {
        AsyncTask<Void, Void, Boolean> sendTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    String emailCode = gMailSender.getEmailCode();
                    gMailSender.sendMail("인증번호 입니다.", "인증번호 : " + emailCode, sendTo);
                    return true;
                } catch (SendFailedException e) {
                    return false;
                } catch (MessagingException e) {
                    return false;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    Toast.makeText(context, "인증번호가 전송되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "이메일 전송에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        };

        sendTask.execute();
    }

    public void sendTempPassword(Context context, String sendTo){
        AsyncTask<Void, Void, Boolean> sendTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    String tempPassword = gPassWordSender.getTemPassword();
                    gMailSender.sendMail("임시비밀번호 입니다.", "임시비밀번호 : " + tempPassword, sendTo);
                    return true;
                } catch (SendFailedException e) {
                    return false;
                } catch (MessagingException e) {
                    return false;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    Toast.makeText(context, "임시비밀번호가 전송되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "이메일 전송에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        };

        sendTask.execute();
    }
}