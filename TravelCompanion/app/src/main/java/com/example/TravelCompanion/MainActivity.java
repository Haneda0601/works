package com.example.TravelCompanion;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    //起動時の画面

    SharedPreferences pref;
    SharedPreferences.Editor e;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Globalval.LongLoc = null;
        Globalval.NewLoc = null;

        System.out.println("Location:" + Globalval.LongLoc + "," + Globalval.NewLoc);

        //もしプレファレンスにGUIDが登録されているならMaps画面に遷移
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String userSettingString = prefs.getString("AcitvityState", "NULL");
        if(!(userSettingString.equals("NULL"))){
            Intent intent = new Intent(this,MapsActivity.class);
            String s = prefs.getString("sessionId","NULL");
            System.out.println("XXXXXXXXXXXX:"+s);
            intent.putExtra("sessionId",s);
            startActivity(intent);
        }
    }
    public void MakeRoom(View v){startActivity(new Intent(MainActivity.this, MakeRoom.class));}

    public void JoinRoom(View v){startActivity(new Intent(MainActivity.this, JoinRoom.class));}

    public void Profile(View v){startActivity(new Intent(MainActivity.this, Profile.class));}
}