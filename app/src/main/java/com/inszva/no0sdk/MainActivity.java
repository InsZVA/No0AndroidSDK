package com.inszva.no0sdk;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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

}
