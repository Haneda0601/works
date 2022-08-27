package com.example.TravelCompanion;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import java.util.Timer;
import java.util.TimerTask;

public class Loading extends AppCompatActivity {

    //生徒がQRを読み取り、待機する部屋

    FirestoreModule fsm;
    String sessionId;
    Handler mHandler = new Handler();
    Handler handler2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        Intent i = getIntent();
        sessionId = i.getStringExtra("sessionId");


        //セッションの開始まで待機
        fsm = new FirestoreModule(this,sessionId);
        Timer sTimer = new Timer(false);
        TimerTask task = new TimerTask() {
            public void run() {
                mHandler.post(new Runnable() {
                    public void run() {
                        String s = fsm.getState();
                        if(s.equals("0")){
                            handler2 = new Handler();

// もしくはLooperでメインスレッドを指定して生成
                            handler2 = new Handler(Looper.getMainLooper());

                            handler2.post(new Runnable() {
                                @Override
                                public void run() {
                                    // ここに処理
                                    Intent intent = new Intent(Loading.this, MapsActivity.class);
                                    intent.putExtra("sessionId",sessionId);
                                    startActivity(intent);
                                    sTimer.cancel();
                                }
                            });
                        }
                    }
                });
            }
        };
        sTimer.schedule(task, 0, 1000);

    }


}