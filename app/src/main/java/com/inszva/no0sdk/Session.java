package com.inszva.no0sdk;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.JsonReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by InsZVA on 2017/7/10.
 */

public class Session {
    private int userId = -1;
    private Handler handler;
    static private Session instance = null;

    private LoginCallback loginCallback = null;
    private GetItemListCallback  getItemListCallback = null;

    static final private String API_ROOT = "http://10.180.33.38:2017/";
    static final private int MSG_NO0_SDK = 0x45786145;
    static final private int MSG_LOGIN = 0;
    static final private int MSG_ITEM_LIST = 1;

    static public Session getInstance() {
        if (instance == null)
            return instance = new Session(Looper.myLooper());
        return instance;
    }

    private Session(Looper looper) {
        handler = new Handler(looper) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what != MSG_NO0_SDK)
                    return;

                switch (msg.arg1) {
                    case MSG_LOGIN:
                        if (loginCallback != null) {
                            if (msg.arg2 != 0) {
                                userId = msg.arg2;
                            }
                            loginCallback.callback(msg.arg2 != 0);
                        }
                        break;
                    case MSG_ITEM_LIST:
                        if (getItemListCallback != null) {
                            getItemListCallback.callback((List<Item>)msg.obj);
                        }
                }
            }
        };
    }

    public interface LoginCallback {
        void callback(boolean success);
    }

    public void login(final int userId, LoginCallback callback) {
        loginCallback = callback;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Message msg = Message.obtain();
                msg.arg1 = MSG_LOGIN;
                msg.arg2 = userId;
                msg.what = MSG_NO0_SDK;
                handler.sendMessage(msg);
            }
        }).start();
    }

    public interface GetItemListCallback {
        void callback(List<Item> list);
    }

    public void getItemList(GetItemListCallback callback) {
        getItemListCallback = callback;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection httpURLConnection = RequestBuilder.newRequest(API_ROOT + "item", "GET", null);
                    httpURLConnection.getResponseCode();

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(httpURLConnection.getInputStream()));

                    JsonReader jsonReader = new JsonReader(in);
                    jsonReader.beginArray();
                    List<Item> itemList = new ArrayList<Item>();
                    while (jsonReader.hasNext()) {
                        jsonReader.beginObject();
                        Item item = new Item();
                        while (jsonReader.hasNext()) {
                            String key = jsonReader.nextName();
                            switch (key) {
                                case "item_id":
                                    item.itemId = jsonReader.nextInt();
                                    break;
                                case "item_name":
                                    item.itemName = jsonReader.nextString();
                                    break;
                                case "item_img":
                                    item.itemImage = jsonReader.nextString();
                                    break;
                                case "item_price":
                                    item.itemPrice = jsonReader.nextDouble();
                                    break;
                                case "item_description":
                                    item.itemDescription = jsonReader.nextString();
                                    break;
                                default:
                                    jsonReader.skipValue();
                            }
                        }
                        jsonReader.endObject();
                        itemList.add(item);
                    }
                    jsonReader.endArray();
                    in.close();

                    Message msg = Message.obtain();
                    msg.arg1 = MSG_ITEM_LIST;
                    msg.obj = itemList;
                    msg.what = MSG_NO0_SDK;
                    handler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
