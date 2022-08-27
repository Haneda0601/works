package com.example.TravelCompanion;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

public class JoinRoom extends AppCompatActivity {

    //セッションに参加するための画面(生徒用)

    private ArrayList data = new ArrayList<String>();
    private String[] teamList;
    private FirestoreModule fsm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_room);
        data.add("");
    }


    public void addMemberData(View v){
        String name = ((EditText)findViewById(R.id.inputMemberName)).getText().toString();
        data.add(name);
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);
        ((ListView)findViewById(R.id.teamData)).setAdapter(adapter);
    }
    public void Back(View v){startActivity(new Intent(JoinRoom.this, MainActivity.class));}
    public void Central(View v){
        String name = ((EditText)findViewById(R.id.inputGroupName)).getText().toString();
        data.set(0,name);
        teamList = new String[data.size()];
        System.out.println(data.size());
        for(int i=0;i<data.size();i++) teamList[i]=String.valueOf(data.get(i));
        //TODO teamListあげる teamList[0]=チーム名が入る

        //QRコードをスキャン
        new IntentIntegrator(JoinRoom.this).initiateScan();
    }
    //QRコード読み取り後の処理
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            Log.d("readQR", result.getContents());
            String myUUID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            //TODO FireBaseに登録
            fsm = new FirestoreModule(JoinRoom.this,result.getContents());
            ArrayList<String> s = new ArrayList<String>();
            Boolean f = false;
            for(String i:teamList){
                if(!f){
                    f = true;
                    continue;
                }
                s.add(i);
            }

            System.out.println(s);
            fsm.joinSession(result.getContents(),myUUID,teamList[0],s);

            //Shared Priferences
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(JoinRoom.this);
            SharedPreferences.Editor e = pref.edit();
            GroupData gd = new GroupData(teamList[0],0,0,s,"0","0");
            Gson gson = new Gson();
            gson.toJson(gd);
            e.putString("GD", gson.toJson(gd)).commit();
            e.commit();

            //Intent
            Intent intent = new Intent(JoinRoom.this, Loading.class);
            intent.putExtra("sessionId",result.getContents());
            startActivity(intent);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}