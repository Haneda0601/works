package com.example.TravelCompanion;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class TimeLine extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    //アルバム機能のメイン画面

    public FirestoreModule fsm;
    public MemberAdapter adapter;

    private StorageReference imageRef;
    public static ProgressDialog progress;

    private ImageView imageView;
    private File galleryFile;
    private String imagePath, sessionId;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        Intent i = getIntent();
        sessionId = i.getStringExtra("sessionId");

        fsm = new FirestoreModule(TimeLine.this, sessionId);


        progress = new ProgressDialog(TimeLine.this);

        swipeRefreshLayout = findViewById(R.id.swipelayout2);
        swipeRefreshLayout.setOnRefreshListener(this);

        ActivityCompat.requestPermissions(TimeLine.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);


        Button button = findViewById(R.id.button);
        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reload();

                    }
                }
        );


        //新規投稿画面に遷移
        findViewById(R.id.button2).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(TimeLine.this, TimeLine2.class);
                        intent.putExtra("sessionId",sessionId);
                        startActivity(intent);


                    }
                }
        );

    }

    public void reload() {
        ArrayList<PictureData> p = fsm.getPicture(sessionId);

        ArrayList<PictureData> temp = new ArrayList<PictureData>();
        adapter = new MemberAdapter(TimeLine.this, R.layout.mylist, temp);

        adapter.clear();


        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setMessage("画像をダウンロードしています");
        progress.show();

        for (PictureData i : p) {
            PictureData vc;
            vc = new PictureData(i.pictureName, i.groupName, i.x, i.y, i.text);
            //adapter.add(vc);
            DownloadPicture(sessionId, i.pictureName, vc);
        }

        if (p.size() == 0) {
            progress.dismiss();
        }


        ListView lv1 = (ListView) findViewById(R.id.listView1);
        //リストビューの更新
        lv1.setAdapter(adapter);

        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                Log.d("############","Items " + adapter.getItem(arg2).groupName);

                new AlertDialog.Builder(TimeLine.this)
                        .setTitle("ピンの設定")
                        .setMessage("この写真の位置情報を地図に表示しますか？")
                        .setPositiveButton("はい", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.putExtra("x", adapter.getItem(arg2).x);
                                intent.putExtra("y",adapter.getItem(arg2).y);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        })
                        .setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
            }

        });

    }


    @Override
    public void onRefresh() {
        reload();
        swipeRefreshLayout.setRefreshing(false);
    }


    void DownloadPicture(String uuid, String name, PictureData p) {
        fsm.storageRef = fsm.storage.getReferenceFromUrl("gs://practicefirestore-d7f92.appspot.com/" + uuid + "/image");
        imageRef = fsm.storageRef.child(name + ".png");

        // ギャラリー用に内部ストレージにフォルダを作成
        String firebaseImageDir = Environment.getExternalStorageDirectory().getPath() + "/firebase";


        // ギャラリー用画像保存パス
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        //imagePath = firebaseImageDir + "/" + sf.format(cal.getTime()) + ".png";
        imagePath = firebaseImageDir + "/" + name + ".png";
        galleryFile = new File(imagePath);


        //imageRef.getBytes(1024 * 1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
        long size = 4000000;
        size = 400000000;
        imageRef.getBytes(size).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] aByte) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(aByte, 0, aByte.length);
                p.image = bitmap;
                System.out.println("P-bitmap:" + p.image);
                adapter.add(p);
            }
        });
        //progress.dismiss();
    }
}


//ArrayAdapterを継承したクラス
class MemberAdapter extends ArrayAdapter<PictureData> {
    Context context;
    int size;

    public MemberAdapter(Context context, int resource, ArrayList<PictureData> objects) {
        super(context, resource, objects);
        this.context = context;
        size = objects.size();
    }

    //リストの行が生成されるたびにListViewから呼ばれる
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.mylist, parent, false);
        }

        PictureData vc = getItem(position);

        String name = vc.groupName;
        //System.out.println(position + " " + name + " " + vc.image);

        TextView tvName = (TextView) convertView.findViewById(R.id.textView41);

        tvName.setText("投稿者：" + name);

        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView1);
        Bitmap bitmap2 = Bitmap.createScaledBitmap(vc.image, 300, 400, false);
        imageView.setImageBitmap(bitmap2);

        TextView tv = (TextView) convertView.findViewById(R.id.content);
        tv.setText(vc.text);

        vc = null;
        if (position == size) {
            TimeLine.progress.dismiss();
        }

        return convertView;
    }

}

