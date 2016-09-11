package com.exetree.drainsms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by m.petrauskas on 2016.08.04.
 */
public class SMSBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; i++) {
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                final String phoneNumber = msgs[i].getOriginatingAddress();
                final String messageReceived = msgs[i].getOriginatingAddress() + ":" + msgs[i].getMessageBody();
                new Thread() {
                    @Override
                    public void run() {
                        String answer;
                        try {
                            answer = netMessage("sms=" + URLEncoder.encode(messageReceived, "utf-8"));
                        } catch (Exception e) {
                            e.printStackTrace();
                            answer = "ERROR :" + e.getMessage();
                        }
                        SmsManager.getDefault().sendTextMessage(phoneNumber, null, answer, null, null);
                    }
                }.start();
            }
        }
    }

    private String netMessage(String data) {
        URL url;
        HttpURLConnection connection;
        InputStream inputStream = null;
        String result = null;
        try {
            url = new URL("http://www.exetree.eu/test.php?" + data);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-type", "application/json");
            connection.setRequestMethod("GET");


            inputStream = new BufferedInputStream(connection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            result = sb.toString();
            JSONObject jObject = new JSONObject(result);
            String aJsonString = jObject.getString("number");
            return aJsonString;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (Exception squish) {
            }
        }
    }
}
