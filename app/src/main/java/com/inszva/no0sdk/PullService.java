package com.inszva.no0sdk;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;

/**
 * Created by InsZVA on 2017/7/11.
 */

public class PullService extends Service {
    private Socket socket;
    private final String dstServIp = "10.180.177.247";
    private final int dstServPort = 2018;
    Thread thread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //show("aa");
                        socket = new Socket(dstServIp, dstServPort);
                        InputStream inputStream = socket.getInputStream();
                        OutputStream outputStream = socket.getOutputStream();
                        byte buff[] = new byte[1024];
                        byte len[] = new byte[2];

                        //login
                        int userId = intent.getIntExtra("userId", 0);
                        Log.v("Service1",String.valueOf(userId));
                        String loginStr = "{\"type\":\"login\", \"id\":\"" + String.valueOf(userId) + "\"}";
                        byte[] s2meteor = new byte[2];
                        s2meteor[0] = (byte) (loginStr.length() >> 8);
                        s2meteor[1] = (byte) loginStr.length();
                        outputStream.write(s2meteor);
                        outputStream.write(loginStr.getBytes());
                        inputStream.read(len);
                        short length = (short) ((short)len[0] << 8 | len[1]);
                        Log.v("Service1", String.valueOf(length));
                        inputStream.read(buff, 0, length);
                        Log.v("Service1", new String(buff));
                        while(true) {
                            inputStream.read(len);
                            length = (short) ((short)len[0] << 8 | len[1]);
                            Log.v("Service1", String.valueOf(length));
                            inputStream.read(buff, 0, length);
                            Log.v("Service1", new String(buff));
                            try {
                                JSONObject object = new JSONObject(new String(buff, 0, length));
                                String data = object.getString("data");
                                Intent intent=new Intent();
                                intent.putExtra("data", data);
                                intent.setAction("com.inszva.no0sdk.PullService");
                                sendBroadcast(intent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        thread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
