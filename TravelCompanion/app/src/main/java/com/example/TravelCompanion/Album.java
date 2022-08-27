package com.example.TravelCompanion;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Album extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);


    }
    public void Back(View v) {
        startActivity(new Intent(Album.this, MainActivity.class));
    }
    public void TakePhoto(View v){

    }
    public void Upload(View v){

    }
}