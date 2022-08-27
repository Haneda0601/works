package com.example.TravelCompanion;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.AndroidRuntimeException;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;

public class MakeRoom extends AppCompatActivity {

    //ルーム作成の画面(先生用)

    private NumberPicker[] numPicker = new NumberPicker[4];
    private final Handler handler = new Handler(Looper.getMainLooper());

    private String data;//guid
    private FirestoreModule fsm;

     /* スレッドUI操作用ハンドラ */
    private Handler mHandler = new Handler();
    /* テキストオブジェクト */
    private Runnable updateText;
    private ArrayAdapter<String> adapter;
    private String myUUID;

    SharedPreferences pref;
    SharedPreferences.Editor e;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_room);
        myUUID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        fsm = new FirestoreModule(MakeRoom.this,myUUID);
        fsm.mDocRef.delete();
        //myUUID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        data = myUUID;
        pref = PreferenceManager.getDefaultSharedPreferences(MakeRoom.this);
        e =  pref.edit();
        makeQR();


        //どの班が参加したかlistviewに表示、定期的に更新するための処理
        updateText = new Runnable() {
            public void run() {
                if(fsm!=null) {
                    ArrayList<String> s = fsm.getGroups(data);
                    if (s == null) {
                        s = new ArrayList<String>();
                    }
                    System.out.println(s);
                    adapter = new ArrayAdapter<String>(MakeRoom.this, android.R.layout.simple_list_item_1, s);
                    ListView listView = findViewById(R.id.list);
                    listView.setAdapter(adapter);
                    mHandler.removeCallbacks(updateText);
                    mHandler.postDelayed(updateText, 5000);
                }
            }
        };
        mHandler.postDelayed(updateText, 5000);
        handler.post(updateText);

    }

    public void makeQR(){
        int size = 500;//px
        try {
            //QRコードをBitmapで作成
            Bitmap bitmap = (new BarcodeEncoder()).encodeBitmap(data, BarcodeFormat.QR_CODE, size, size);
            //作成したQRコードを画面上に配置
            ImageView imageViewQrCode = (ImageView) findViewById(R.id.displayQR);
            imageViewQrCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            throw new AndroidRuntimeException("Barcode Error.", e);
        }
    }

    public void createList(ArrayList<String>GroupList){
        if(GroupList!=null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, GroupList);
            ((ListView) findViewById(R.id.list)).setAdapter(adapter);
        }
    }

    public void Back(View v){
        startActivity(new Intent(MakeRoom.this, MainActivity.class));
    }


    //セッション開始
    public void Next(View v){
        fsm.makeSession(myUUID);
        fsm.joinSession(myUUID,myUUID,"先生",new ArrayList<String>());
        
        Intent intent = new Intent(MakeRoom.this, MapsActivity.class);
        intent.putExtra("sessionId",myUUID);
        startActivity(intent);

        //Shared Priferences

        GroupData gd = new GroupData("先生",0,0,new ArrayList<String>());
        Gson gson = new Gson();
        gson.toJson(gd);
        e.putString("GD", gson.toJson(gd)).commit();
        e.commit();

        fsm = null;



    }

}