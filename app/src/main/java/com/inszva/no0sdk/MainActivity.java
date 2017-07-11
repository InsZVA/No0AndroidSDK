package com.inszva.no0sdk;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle=intent.getExtras();
            show(bundle.getString("data"));
        }
    }

    private Session session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        session = Session.getInstance();
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putInt("userId", 1);
        intent.putExtras(bundle);
        intent.setClass(this, PullService.class);
        startService(intent);

        BroadcastReceiver receiver=new MyReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction("com.inszva.no0sdk.PullService");
        MainActivity.this.registerReceiver(receiver,filter);

        session.login(1, new Session.LoginCallback() {
            @Override
            public void callback(boolean success, String reason) {
                ((TextView)findViewById(R.id.textView)).setText("login success");
                session.getItemList(new Session.GetItemListCallback() {
                    @Override
                    public void callback(boolean success, String reason, List<Item> list) {
                        ((TextView)findViewById(R.id.textView)).setText(list.get(0).itemName);
                        try {
                            session.setStop("1321411", new Session.SetStopCallback() {
                                @Override
                                public void callback(boolean success, String reason) {
                                    ((TextView)findViewById(R.id.textView)).setText("setStop");
                                    try {
                                        session.setButton(new int[]{1,1,1,1,1,1}, new Session.SetButtonCallback() {
                                            @Override
                                            public void callback(boolean success, String reason) {
                                                ((TextView)findViewById(R.id.textView)).setText("setButton");
                                                List<ItemPair> list = new ArrayList<ItemPair>();
                                                ItemPair itemPair = new ItemPair();
                                                itemPair.itemNum = 1L;
                                                itemPair.itemId = 1L;
                                                list.add(itemPair);
                                                try {
                                                    session.createPayment(list, new Session.CreatePaymentCallback() {
                                                        @Override
                                                        public void callback(boolean success, String reason) {
                                                            ((TextView)findViewById(R.id.textView)).setText("create");
                                                            session.getRecentPayments(10, new Session.GetPaymentCallback() {
                                                                @Override
                                                                public void callback(boolean success, String reason, List<Payment> list) {
                                                                    ((TextView)findViewById(R.id.textView)).setText(list.get(0).paymentNumber);
                                                                }
                                                            });
                                                        }
                                                    });
                                                } catch (Session.NullItemPairsException e) {
                                                    e.printStackTrace();
                                                } catch (Session.NotLoginException e) {
                                                    e.printStackTrace();
                                                }

                                            }
                                        });
                                    } catch (Session.ButtonArrayException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (Session.NotLoginException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        });

    }

    private int s2meteor = 0;

    private void show(String data) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(android.R.drawable.sym_def_app_icon);
        builder.setTicker("S2Meteor");
        builder.setContentTitle(data);
        builder.setContentText("点击进入app查看详细内容");
        builder.setWhen(System.currentTimeMillis());
        Notification note = builder.build();
        NotificationManager mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent=new Intent(this,MainActivity.class);
        PendingIntent pendingIntent= PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);//点击后的意图
        note.defaults = Notification.DEFAULT_ALL;
        mgr.notify(s2meteor++, note);
    }
}
