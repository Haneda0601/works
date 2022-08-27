package com.example.TravelCompanion;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.File;


public class TimeLine2 extends AppCompatActivity {

    //アルバムに写真を投稿する機能を担う画面

    private static final int READ_REQUEST_CODE = 42;

    public FirestoreModule fsm;
    public MemberAdapter adapter;

    private StorageReference imageRef;
    private ProgressDialog progress;

    private ImageView imageView;
    private File galleryFile;
    private String imagePath,sessionId,data;
    private Uri uri;
    private double resX=-400,resY=-400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline2);

        Intent i = getIntent();
        sessionId = i.getStringExtra("sessionId");
        data = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);


        fsm = new FirestoreModule(TimeLine2.this, sessionId);
        progress = new ProgressDialog(TimeLine2.this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(TimeLine2.this);
        Gson gson = new Gson();
        // 保存されているjson文字列を取得
        String userSettingString = prefs.getString("GD", "");
        // json文字列を 「UserSettingクラス」のインスタンスに変換
        GroupData gd = gson.fromJson(userSettingString, GroupData.class);


        findViewById(R.id.button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
        );

        findViewById(R.id.button2).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fsm.uploadPicture(sessionId, data,gd.groupName);
                    }
                }
        );

        findViewById(R.id.button4).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(TimeLine2.this, MapsActivity2.class);
                        startActivityForResult(intent,1);
                    }
                }
        );



        findViewById(R.id.button3).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (fsm.dts != null && resX!=-400 && resY!=-400) {
                            EditText et = findViewById(R.id.et);
                            fsm.dts.put("text", et.getText().toString());
                            fsm.dts.put("x",resX);
                            fsm.dts.put("y",resY);
                            int res = fsm.submitData("OnetimeGPS/" + sessionId + "/images/" + fsm.newPicName, fsm.dts);
                            // 画像アップロード用パス決定
                            ContentResolver contentResolver = TimeLine2.this.getContentResolver();
                            Cursor cursor = contentResolver.query(uri, null, null, null, null);
                            cursor.moveToFirst();
// とりあえず固定( 2 => _display_name )
                            String name = cursor.getString(2);
                            Log.i("lightbox", "FileName: " + name);

//この カーソルの詳細
                            String[] columnName = cursor.getColumnNames();
                            for (int i = 0; i < columnName.length; i++) {
                                Log.i("lightbox", String.format("%d : %s : %s", i, columnName[i], cursor.getString(i)));
                            }

                            //String uploadImagePath = String.format("image/%s",name);
                            String uploadImagePath = String.format("image/%s.png", fsm.newPicName);
                            imageRef = fsm.storageRef.child(uploadImagePath);

// アップロード中の表示
                            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            progress.setMessage("画像をアップロードしています");
                            progress.show();

// 画像アップロード
                            UploadTask uploadTask = imageRef.putFile(uri);
                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    progress.dismiss();

                                    Log.i("lightbox", "アップロードに成功しました");
                                    long size = taskSnapshot.getMetadata().getSizeBytes();
                                    Log.i("lightbox", String.format("サイズ : %d", size));
                                    new AlertDialog.Builder(TimeLine2.this)
                                            .setTitle("投稿完了")
                                            .setMessage("投稿しました！！")
                                            .setPositiveButton("戻る", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // クリックしたときの処理
                                                    finish();
                                                }
                                            }).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progress.dismiss();

                                    Log.i("lightbox", "アップロードに失敗しました");
                                }
                            });

                            fsm.dts = null;
                        } else if(fsm.dts == null) {
                            Toast toast = Toast.makeText(TimeLine2.this, "写真が未設定です", Toast.LENGTH_LONG);
                            toast.show();
                        }else{
                            Toast toast = Toast.makeText(TimeLine2.this, "写真の位置情報が未設定です", Toast.LENGTH_LONG);
                            toast.show();
                        }

                    }
                }
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

// ギャラリーからの戻り
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            uri = null;
            if (data != null) {
// Uri が戻されます
                uri = data.getData();
                Log.i("lightbox", "Uri: " + uri.toString());

                ImageView iv = findViewById(R.id.iv);
                iv.setImageURI(uri);

                //File file = new File(String.valueOf(uri));

                /*
                try {
                    ExifInterface exif = new ExifInterface()
                    System.out.println(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
                } catch (IOException e) {
                    e.printStackTrace();
                }*/

            }

        }else{
            resX = data.getDoubleExtra("x",0.0);
            resY = data.getDoubleExtra("y",0.0);
            TextView tv = findViewById(R.id.text);
            tv.setText("設定完了");
        }
    }
}
