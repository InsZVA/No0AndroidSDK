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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by InsZVA on 2017/7/10.
 */

public class Session {
    private int userId = -1;
    private Handler handler;
    static private Session instance = null;

    private LoginCallback loginCallback = null;
    private GetItemListCallback  getItemListCallback = null;
    private SetStopCallback setStopCallback = null;
    private SetButtonCallback setButtonCallback = null;
    private GetPaymentCallback getPaymentCallback = null;
    private CreatePaymentCallback createPaymentCallback = null;

    static final private String API_ROOT = "http://10.180.177.247:2017/";
    static final private int MSG_NO0_SDK = 0x45786145;
    static final private int MSG_LOGIN = 0;
    static final private int MSG_ITEM_LIST = 1;
    static final private int MSG_SET_STOP = 2;
    static final private int MSG_SET_BUTTON = 3;
    static final private int MSG_GET_PAYMENT = 4;
    static final private int MSG_CREATE_PAYMENT = 5;

    class NotLoginException extends Exception {
        NotLoginException() {
            super("Not yet login.");
        }
    }

    class ButtonArrayException extends Exception {
        ButtonArrayException() {
            super("Button Array illigal");
        }
    }

    class NullItemPairsException extends Exception {
        NullItemPairsException() {super("Null Item pairs!"); }
    }

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
                        if (msg.arg2 != 0) {
                            userId = msg.arg2;
                        }
                        if (loginCallback != null) {
                            loginCallback.callback(msg.arg2 != 0, null);
                        }
                        break;
                    case MSG_ITEM_LIST:
                        if (msg.arg2 != 0) {
                            if (getItemListCallback != null) {
                                getItemListCallback.callback(false, (String)msg.obj, null);
                            }
                        } else if (getItemListCallback != null) {
                            getItemListCallback.callback(true, null, (List<Item>)msg.obj);
                        }
                        break;
                    case MSG_SET_STOP:
                        if (msg.arg2 != 0) {
                            if (setStopCallback != null) {
                                setStopCallback.callback(false, (String) msg.obj);
                            }
                        }
                        else if (setStopCallback != null)
                            setStopCallback.callback(true, null);
                        break;
                    case MSG_SET_BUTTON:
                        if (msg.arg2 != 0) {
                            if (setButtonCallback != null) {
                                setButtonCallback.callback(false, (String) msg.obj);
                            }
                        }
                        else if (setButtonCallback != null)
                            setButtonCallback.callback(true, null);
                        break;
                    case MSG_GET_PAYMENT:
                        if (msg.arg2 != 0) {
                            if (getPaymentCallback != null) {
                                getPaymentCallback.callback(false, (String)msg.obj, null);
                            }
                        }
                        else if (getPaymentCallback != null) {
                            getPaymentCallback.callback(true, null, (List<Payment>)msg.obj);
                        }
                        break;
                    case MSG_CREATE_PAYMENT:
                        if (msg.arg2 != 0) {
                            if (createPaymentCallback != null) {
                                createPaymentCallback.callback(false, (String)msg.obj);
                            }
                        } else if (createPaymentCallback != null) {
                            createPaymentCallback.callback(true, null);
                        }
                }
            }
        };
    }

    public interface LoginCallback {
        void callback(boolean success, String reason);
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
        void callback(boolean success, String reason, List<Item> list);
    }

    private String getResponseString(HttpURLConnection httpURLConnection) {
        BufferedReader in;
        String inputLine;
        StringBuffer response = new StringBuffer();

        try {
            in = new BufferedReader(
                    new InputStreamReader(httpURLConnection.getInputStream()));
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response.toString();
    }

    public void getItemList(GetItemListCallback callback) {
        getItemListCallback = callback;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection httpURLConnection = RequestBuilder.newRequest(API_ROOT + "item", "GET", null);
                    int code = httpURLConnection.getResponseCode();
                    if (code != 200) {
                        Message msg = Message.obtain();
                        msg.arg1 = MSG_ITEM_LIST;
                        msg.arg2 = code;
                        msg.obj = getResponseString(httpURLConnection);
                        msg.what = MSG_NO0_SDK;
                        handler.sendMessage(msg);
                        return;
                    }

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
                                    item.itemId = jsonReader.nextLong();
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
                    msg.arg2 = 0;
                    msg.obj = itemList;
                    msg.what = MSG_NO0_SDK;
                    handler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public interface SetStopCallback {
        void callback(boolean success, String reason);
    }

    public void setStop(final String pin, SetStopCallback callback) throws NotLoginException {
        if (userId == -1)
            throw new NotLoginException();
        setStopCallback = callback;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection httpURLConnection = RequestBuilder.newRequest(API_ROOT + "user/" + String.valueOf(userId) + "/stop", "PUT", ("pin=" + pin).getBytes());
                    int code = httpURLConnection.getResponseCode();
                    System.out.print(code);
                    if (code != 200) {
                        Message msg = Message.obtain();
                        msg.arg1 = MSG_SET_STOP;
                        msg.arg2 = code;
                        msg.obj = getResponseString(httpURLConnection);
                        msg.what = MSG_NO0_SDK;
                        handler.sendMessage(msg);
                        return;
                    }
                    Message msg = Message.obtain();
                    msg.arg1 = MSG_SET_STOP;
                    msg.arg2 = 0;
                    msg.obj = getResponseString(httpURLConnection);
                    msg.what = MSG_NO0_SDK;
                    handler.sendMessage(msg);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public interface SetButtonCallback {
        void callback(boolean success, String reason);
    }

    public void setButton(final int []buttonItemIds, SetButtonCallback callback) throws ButtonArrayException {
        if (buttonItemIds.length != 6) {
            throw new ButtonArrayException();
        }
        setButtonCallback = callback;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < 6; i++) {
                        if (i != 0)
                            stringBuilder.append("&");
                        stringBuilder.append("items=");
                        stringBuilder.append(String.valueOf(buttonItemIds[i]));
                    }
                    HttpURLConnection httpURLConnection = RequestBuilder.newRequest(API_ROOT + "user/" + String.valueOf(userId) + "/button",
                            "PUT", stringBuilder.toString().getBytes());
                    int code = httpURLConnection.getResponseCode();
                    if (code != 200) {
                        Message msg = Message.obtain();
                        msg.arg1 = MSG_SET_BUTTON;
                        msg.arg2 = code;
                        msg.obj = getResponseString(httpURLConnection);
                        msg.what = MSG_SET_BUTTON;
                        handler.sendMessage(msg);
                        return;
                    }
                    Message msg = Message.obtain();
                    msg.arg1 = MSG_SET_BUTTON;
                    msg.arg2 = 0;
                    msg.obj = getResponseString(httpURLConnection);
                    msg.what = MSG_NO0_SDK;
                    handler.sendMessage(msg);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public interface GetPaymentCallback {
        void callback(boolean success, String reason, List<Payment> list);
    }

    public void getRecentPayments(final int num, GetPaymentCallback callback) {
        getPaymentCallback = callback;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection httpURLConnection = RequestBuilder.newRequest(API_ROOT + "user/" +
                            String.valueOf(userId) + "/payment?num=" + String.valueOf(num) , "GET", null);
                    int code = httpURLConnection.getResponseCode();
                    if (code != 200) {
                        Message msg = Message.obtain();
                        msg.arg1 = MSG_GET_PAYMENT;
                        msg.arg2 = code;
                        msg.obj = getResponseString(httpURLConnection);
                        msg.what = MSG_NO0_SDK;
                        handler.sendMessage(msg);
                        return;
                    }

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(httpURLConnection.getInputStream()));
                    JsonReader jsonReader = new JsonReader(in);
                    jsonReader.beginArray();
                    List<Payment> paymentList = new ArrayList<>();

                    while (jsonReader.hasNext()) {
                        jsonReader.beginObject();
                        Payment payment = new Payment();
                        while (jsonReader.hasNext()) {
                            String key = jsonReader.nextName();
                            switch (key) {
                                case "payment_id":
                                    payment.paymentId = jsonReader.nextLong();
                                    break;
                                case "payment_number":
                                    payment.paymentNumber = jsonReader.nextString();
                                    break;
                                case "payment_item_id":
                                    jsonReader.beginArray();
                                    List<ItemPair> list = new ArrayList<>();
                                    while(jsonReader.hasNext()) {
                                        jsonReader.beginObject();
                                        ItemPair itemPair = new ItemPair();
                                        while (jsonReader.hasNext()) {
                                            String keyItem = jsonReader.nextName();
                                            switch (keyItem) {
                                                case "item_id":
                                                    itemPair.itemId = jsonReader.nextLong();
                                                    break;
                                                case "item_num":
                                                    itemPair.itemNum = jsonReader.nextLong();
                                                    break;
                                                default:
                                                    jsonReader.skipValue();
                                            }
                                        }
                                        jsonReader.endObject();
                                        list.add(itemPair);
                                    }
                                    jsonReader.endArray();
                                    payment.paymentItems = list;
                                    break;
                                case "payment_user_id":
                                    payment.paymentUserId = jsonReader.nextLong();
                                    break;
                                case "payment_uav_id":
                                    payment.paymentUavId = jsonReader.nextLong();
                                    break;
                                case "payment_price":
                                    payment.paymentPrice = jsonReader.nextDouble();
                                    break;
                                case "payment_time":
                                    payment.paymentTime = jsonReader.nextLong();
                                    break;
                                default:
                                    jsonReader.skipValue();
                            }
                        }
                        jsonReader.endObject();
                        paymentList.add(payment);
                    }
                    jsonReader.endArray();
                    in.close();

                    Message msg = Message.obtain();
                    msg.arg1 = MSG_GET_PAYMENT;
                    msg.arg2 = 0;
                    msg.obj = paymentList;
                    msg.what = MSG_NO0_SDK;
                    handler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public interface CreatePaymentCallback {
        void callback(boolean success, String reason);
    }

    public void createPayment(final List<ItemPair> list, CreatePaymentCallback callback) throws NullItemPairsException, NotLoginException {
        if (userId == -1)
            throw new NotLoginException();
        if (list == null || list.size() == 0) {
            throw new NullItemPairsException();
        }
        createPaymentCallback = callback;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < list.size(); i++) {
                        if (i != 0)
                            stringBuilder.append("&");
                        stringBuilder.append("items=");
                        stringBuilder.append(String.valueOf(list.get(i).itemId));
                        stringBuilder.append("&nums=");
                        stringBuilder.append(String.valueOf(list.get(i).itemNum));
                    }
                    HttpURLConnection httpURLConnection = RequestBuilder.newRequest(API_ROOT + "user/" + String.valueOf(userId) + "/payment",
                            "POST", stringBuilder.toString().getBytes());
                    int code = httpURLConnection.getResponseCode();
                    if (code != 200) {
                        Message msg = Message.obtain();
                        msg.arg1 = MSG_CREATE_PAYMENT;
                        msg.arg2 = code;
                        msg.obj = getResponseString(httpURLConnection);
                        msg.what = MSG_CREATE_PAYMENT;
                        handler.sendMessage(msg);
                        return;
                    }
                    Message msg = Message.obtain();
                    msg.arg1 = MSG_CREATE_PAYMENT;
                    msg.arg2 = 0;
                    msg.obj = getResponseString(httpURLConnection);
                    msg.what = MSG_NO0_SDK;
                    handler.sendMessage(msg);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
