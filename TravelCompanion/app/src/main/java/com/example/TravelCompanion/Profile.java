package com.example.TravelCompanion;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class Profile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        CreateList();
        EditProfile();
    }
    public void Back(View v){
        startActivity(new Intent(Profile.this, MainActivity.class));//sampleでMainActivity
    }
    public void EditProfile(){
        /* TODO りょーへーお願い
            String teamName = get
            String comment = get
            String IcomImage = get
         */
        //sample
        String teamName = "きあぬりいぶす";
        String comment = "めっちゃかっこいいよね";
        int iconImage = R.drawable.kan;

        ((TextView)findViewById(R.id.TeamName)).setText(teamName);
        ((TextView)findViewById(R.id.Comment)).setText(comment);
        ((ImageView)findViewById(R.id.iconImage)).setImageResource(iconImage);
    }
    public void CreateList(){
        // TODO ArrayList<String> GroupList = getGroup();
        ArrayList<String> GroupList = new ArrayList<String>(Arrays.asList("Apple", "Orange", "Melon"));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,GroupList);
        ((ListView)findViewById(R.id.list)).setAdapter(adapter);
    }
}