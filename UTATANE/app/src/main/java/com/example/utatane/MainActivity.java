package com.example.utatane;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import androidx.preference.PreferenceManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    // Global定義
    Globals globals;

    // Timer定義
    Timer mTimer = null;
    Timer nTimer = null;
    Timer sTimer = null;
    Handler mHandler = new Handler();

    // Dialog定義
    private AlertDialog nDialog;

    // バイブレーション定義＆パターン定義
    private Vibrator vib;
    private long pattern1[] = {100, 500, 100, 500};
    private long pattern2[] = {50, 100, 50, 100};

    // 今日の日付格納用変数
    private int NowDay;

    // MainLayout用View変数
    View main;

    // BackGroundColor仮変数
    Drawable temp;

    // ViewPager最大ページ数設定変数
    private static final int NUM_PAGES = 3;

    // ViewPager定義
    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;

    // 共有プリファレンス
    public SharedPreferences pref;
    public SharedPreferences.Editor e;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        //pref.edit().clear().apply();

        // バイブレーション初期設定格納
        vib = (Vibrator)getSystemService(VIBRATOR_SERVICE);

        // Global変数初期値設定
        globals = (Globals) getApplication();
        globals.GBackGroundColor = ResourcesCompat.getDrawable(getResources(), R.drawable.background, null);
        temp = ResourcesCompat.getDrawable(getResources(), R.drawable.background, null);
        globals.NotiFlg = false;
        globals.NotiCnt = 0;
        globals.AverageData = new float[10][2];
        globals.NotiTimeCnt = new int[24];
        globals.p = pref.getInt("Notip",0);
        Arrays.fill(globals.NotiTimeCnt,0);
        globals.AverageData[0][0] = 999.0f;

        // Date関数から現在の日にちを取得
        Date d = new Date(System.currentTimeMillis());
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        int firstNowDay = c.get(Calendar.DAY_OF_MONTH);

        // アプリを最後に使用した日付と現在の日付を比較、違う場合は一日の通知回数変数をリセット
        if(pref.getInt("Day",-1) != firstNowDay){
            pref.edit().remove("NotiTimeCnt").apply();
        }

        // 保存値を配列に再挿入
        String NotiStr = pref.getString("NotiTimeCnt","NULL");
        String AveDataXStr = pref.getString("AverageDataX","NULL");
        String AveDataYStr = pref.getString("AverageDataY","NULL");

        String[] Notitemp = NotiStr.split("/",-1);
        String[] ADXtemp = AveDataXStr.split("/",-1);
        String[] ADYtemp = AveDataYStr.split("/",-1);

        if(!NotiStr.equals("NULL")){
            for(int i = 0;i < 23; i++){
                globals.NotiTimeCnt[i] = Integer.parseInt(Notitemp[i]);
            }
        }

        if(!AveDataXStr.equals("NULL")){
            for(int i = 0;i < globals.AverageData.length; i++){
                globals.AverageData[i][0] = (float) Double.parseDouble(ADXtemp[i]);

                if (globals.AverageData[i][0] == 999.0f) break;
            }
        }

        if(!AveDataYStr.equals("NULL")){
            for(int i = 0;i < globals.AverageData.length; i++){
                globals.AverageData[i][1] = (float) Double.parseDouble(ADYtemp[i]);

                if (globals.AverageData[i][0] == 999.0f) break;
            }
        }

        // Layoutを格納
        viewPager = findViewById(R.id.pager);
        main = findViewById(R.id.mainlayout);
        main.setBackground(globals.GBackGroundColor);
        NowDay = c.get(Calendar.DAY_OF_MONTH);

        // ViewPager設定
        pagerAdapter = new ScreenSlidePagerAdapter(this);
        pagerAdapter.notifyItemRemoved(0);
        pagerAdapter.createFragment(0);
        viewPager.setAdapter(pagerAdapter);

        BackGroundTime();
    }

    protected void onStop(){
        super.onStop();

        // NotiTimeCnt,AverageData,NowDay,globals.pを共有プリファレンスに保存する
        String NTCstr = "";
        String ADXstr = "";
        String ADYstr = "";
        for (int i = 0; i < 24; i++){
            NTCstr = NTCstr + globals.NotiTimeCnt[i] + "/";
        }

        for (int i = 0; i < globals.AverageData.length; i++) {
            ADXstr = ADXstr + globals.AverageData[i][0] + "/";

            if (globals.AverageData[i][0] == 999.0f) break;
        }

        for (int i = 0; i < globals.AverageData.length; i++) {
            ADYstr = ADYstr + globals.AverageData[i][1] + "/";

            if (globals.AverageData[i][0] == 999.0f) break;
        }

        e = pref.edit();

        e.putString("NotiTimeCnt", NTCstr);
        e.putString("AverageDataX", ADXstr);
        e.putString("AverageDataY", ADYstr);
        e.putInt("Day",NowDay);
        e.putInt("Notip",globals.p);

        e.apply();
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {

        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = new fragment();
                    break;
                case 1:
                    fragment = new fragment2();
                    break;
                case 2:
                    fragment = new fragment3();
                    break;
            }
            return fragment;
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }

    // BackGroundColor変更用＆通知用Timer
    public void BackGroundTime() {
        mTimer = new Timer(false);
        TimerTask task = new TimerTask() {
            public void run() {
                mHandler.post(new Runnable() {
                    public void run() {
                        if(temp != globals.GBackGroundColor) {
                            main.setBackground(globals.GBackGroundColor);
                            temp = globals.GBackGroundColor;
                        }
                        if(globals.NotiFlg){
                            globals.NotiFlg = false;
                            Notification();
                        }
                    }
                });
            }
        };
        mTimer.schedule(task, 0, 1);
    }

    // 通知
    public void Notification(){

        // バイブレーション秒数設定＆実行
        int vibTime = 0;

        switch (globals.NotiCnt){
            case 8:
                // lv1
                vib.vibrate(1000);
                break;
            case 16:
                // lv2
                vib.vibrate(pattern1, 1);
                vibTime = 3600;
                break;
            case 24:
                // lv3
                vib.vibrate(pattern1, 1);
                vibTime = 4800;
                break;
            case 32:
                // lv4
                vib.vibrate(pattern2, 1);
                vibTime = 6000;
                break;
            case 40:
                // lv5
                vib.vibrate(pattern2, 1);
                vibTime = 10000;
                break;
            default:
                vib.vibrate(1000);
                break;
        }

        sTimer = new Timer(false);
        TimerTask tas = new TimerTask() {
            @Override
            public void run() {
                vib.cancel();
            }
        };

        if(vibTime != 0) {
            sTimer.schedule(tas, vibTime);
        }

        // 通知
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("姿勢が悪いです！！");
        builder.setMessage("姿勢が悪くなっていました。背筋と首を真っ直ぐにし、姿勢を正してください。");
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                globals.NotiCnt = 0;
            }
        });

        // 10秒経過時、通知の自動消去
        nDialog = builder.create();
        nTimer = new Timer(false);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                nDialog.dismiss();
            }
        };
        nDialog.show();
        nTimer.schedule(task, 10000);
    }
}
