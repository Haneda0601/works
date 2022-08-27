package com.example.TravelCompanion;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CheckGroups extends AppCompatActivity {

    FirestoreModule fsm;
    Map<String,String> mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_groups);

        fsm = new FirestoreModule(this,Globalval.sessionId);

        findViewById(R.id.buttonRet).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
        );


        Intent it = getIntent();
        ArrayList<GroupData>gd = (ArrayList<GroupData>) it.getSerializableExtra("person");
        ArrayList<String>data = new ArrayList<String>();

        mp = new HashMap<String,String>();
        for(GroupData i:gd){
            String state;

            String a = new String();
            String b = new String();
            if(!(i.groupName.equals("目的地"))){
                if(i.state.equals("0")){
                    state = "未到着";
                }else{
                    state = "到着";
                }
                a+=i.groupName+" "+state;
                data.add(a);
                if(!(i.groupName.equals("先生"))){
                    for(String s:i.member){
                        b += s + "\n";
                    }
                    mp.put(i.groupName,b);
                }
            }
        }

        ListView listView = findViewById(R.id.lv);

        // simple_list_item_1 は、 もともと用意されている定義済みのレイアウトファイルのID
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                String[] temp = data.get(arg2).split(" ");
                if(!temp[0].equals("先生")) {
                    new AlertDialog.Builder(CheckGroups.this)
                            .setTitle("班員一覧")
                            .setMessage(mp.get(temp[0]))
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                }
            }

        });

    }


}