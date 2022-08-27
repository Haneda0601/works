package com.example.TravelCompanion;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.content.ContentValues.TAG;
import static java.sql.Types.NULL;

import java.io.Serializable;

public class FirestoreModule extends AppCompatActivity {

    //FireStoreと通信するためのモジュール

    public DocumentReference mDocRef;
    private int RET;
    private Activity mActivity;

    private ArrayList<GroupData> g;//グループの情報を格納しておく変数
    private ArrayList<PictureData> p;//写真の情報
    private final String state="0";
    private String retState;
    Boolean flag;
    CollectionReference cr,cr2;

    FirestoreModule(){}

    FirestoreModule(Activity activity,String uuid) {

        this.mActivity = activity;


        mDocRef = FirebaseFirestore.getInstance().document("OnetimeGPS/"+uuid);
        System.out.println("uuid:"+uuid);

        flag = false;
        retState = "5";

        //コレクションリスナー
        //https://firebase.google.com/docs/firestore/query-data/listen?hl=ja#java_2
        //班一覧を取得、GroupData型のArrayListに格納
        cr = FirebaseFirestore.getInstance().collection("OnetimeGPS/"+uuid+"/groups");

        cr.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                List<String> cities = new ArrayList<>();
                g = new ArrayList<GroupData>();
                int cnt = 0;
                for (QueryDocumentSnapshot doc : value) {
                    if (doc.get("groupName") != null) {
                        //System.out.println(uuid+" "+doc.getId()+" "+doc.get("groupName"));
                        double x,y;
                        String i="0";
                        ArrayList<String> mem;
                        if(doc.get("x") != null) {
                            x = doc.getDouble("x");
                        }else{
                            x=0;
                        }

                        if(doc.get("y") != null) {
                            y = doc.getDouble("y");
                        }else{
                            y=0;
                        }

                        if(doc.get("member") != null){
                            mem = (ArrayList<String>) doc.get("member");
                        }else{
                            mem = new ArrayList<String>();
                        }

                        if(Objects.equals(doc.get("state"), "1")){
                            i = (String)doc.get("state");
                            System.out.println(doc.getId() + "gtgtgtgtgtgtgdtgethyrhs "+doc.get("state"));
                            if(!(doc.getString("groupName").equals("目的地"))){
                                cnt++;
                            }else{
                                flag = true;
                            }
                        }else{
                            i = "0";
                        }

                        String j;
                        if(doc.get("invFlag") != null && doc.get("invFlag").equals("1")){
                            j = "1";
                        }else{
                            j = "0";
                        }
                        GroupData data = new GroupData(doc.getString("groupName"),x,y,mem,i,j);
                        g.add(data);
                        if(doc.getId() == Settings.Secure.getString(mActivity.getContentResolver(), Settings.Secure.ANDROID_ID)){
                            groupName = doc.getString("groupName");
                            //state = i;
                        }
                    }
                }
                System.out.println(value.size());
                if(cnt == value.size()-1 && flag && value.size()>1) {
                    retState = "1";
                    Map<String, Object> dataToSave = new HashMap<String, Object>();
                    dataToSave.put("state", "1");
                    submitData("OnetimeGPS/" + uuid, dataToSave);
                    System.out.println("END-END");
                }
                //Log.d(TAG, "Current cites in CA: " + g);
            }
        });

        cr2 = FirebaseFirestore.getInstance().collection("OnetimeGPS/"+uuid+"/images");

        //写真の情報を取得するリスナー、PictureData型のArrayListにデータを格納する

        cr2.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                p = new ArrayList<PictureData>();
                for (QueryDocumentSnapshot doc : value) {
                    if (doc.get("groupName") != null) {
                        double x,y;
                        String text = "";
                        if(doc.get("x") != null) {
                            x = doc.getDouble("x");
                        }else{
                            x=0;
                        }

                        if(doc.get("y") != null) {
                            y = doc.getDouble("y");
                        }else{
                            y=0;
                        }

                        if(doc.get("text") != null){
                            text = doc.getString("text");
                        }


                        //System.out.println(doc.getId());
                        PictureData data = new PictureData(doc.getId(),doc.getString("groupName"),x,y,text);
                        p.add(data);
                    }
                }
                //Log.d(TAG, "Current cites in CA: " + g);
            }
        });


        //その他のDataBase上の値用のリスナー

        mDocRef.addSnapshotListener(mActivity, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                /// データが存在する場合に処理を実行。。
                if (documentSnapshot.exists()) {
                    retState = (String) documentSnapshot.get("state");
                } else if (e != null) {
                    Log.w(TAG, "Got an exception!", e);
                    retState = "3";
                }else{
                    retState = "3";
                }
            }
        });
    }

    //データを送信するメソッド
    //path:更新するデータのパス
    //data:送信データ
    //戻り値：正常終了-0 失敗-1
    int submitData(String path, Map dataToSave) {

        DocumentReference doc = FirebaseFirestore.getInstance().document(path);

        /// データを保存するとともに，保存が完了した場合に，onCompleteメソッドを実行する。
        doc.set(dataToSave).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // 保存が成功した場合
                    RET = 0;
                } else {
                    // 保存が失敗した場合
                    RET = 1;
                }
            }
        });

        return RET;
    }

    //新しくセッションを立てるメソッド
    //guid:セッションに割り当てるid
    //戻り値:正常終了:0
    //       失敗:1
    int makeSession(String uuid) {
        /// 変数名（String型）と，値（Object型）をセットで保存するデータ構造Mapを使う。
        Map<String, Object> dataToSave = new HashMap<String, Object>();
        dataToSave.put("state", "0");

        int res = this.submitData("OnetimeGPS/" + uuid, dataToSave);

        return res;
    }


    //ゴールした班用の更新メソッド
    int goal(String uuid,String id,GroupData gd){
        Map<String, Object> dataToSave = new HashMap<String, Object>();
        dataToSave.put("state", "1");
        dataToSave.put("groupName",gd.groupName);
        dataToSave.put("x",gd.x);
        dataToSave.put("y",gd.y);
        dataToSave.put("member",gd.member);
        dataToSave.put("invFlag",gd.invisible);
        int res = this.submitData("OnetimeGPS/" + uuid + "/groups/" + id, dataToSave);
        return res;
    }


    //セッションが終了したか状態を返すメソッド
    ///戻り値：アクティブ 0
    //         非アクティブ 1
    String getState(){
        return retState;
    }


    //セッションに登録されているグループ名一覧を取得
    //uuid:セッションに割り当てられているid
    //戻り値:失敗した場合はnull
    ArrayList<String> getGroups(String uuid){

        ArrayList<String> groups = new ArrayList<String>();
        //System.out.println(uuid);

        if(!(g == null)) {
            for (GroupData i : g) {
                groups.add(i.groupName);
                //System.out.println(i.groupName);
            }
            return groups;
        }else{
            return null;
        }
    }


    //指定したguidが管理しているgroupsコレクションを全て削除する
    void deleteGroups(String guid){
        cr.document(guid).delete();
    }


    //立てられたセッションに班の情報を追加する関数
    //uuid     :セッションに割り当てられたid
    //groupId  :グループのID
    //groupName:グループの名前
    //member   :班に所属する生徒の名前
    //戻り値:正常終了:0
    //       引数guidで指定されたセッションがない場合や追加に失敗した場合:1
    int joinSession(String uuid, String groupId, String groupName, ArrayList<String> member) {
        Map<String, Object> dataToSave = new HashMap<String, Object>();
        dataToSave.put("state",state);
        dataToSave.put("groupName", groupName);
        dataToSave.put("member", member);
        dataToSave.put("x", 0.0);
        dataToSave.put("y", 0.0);
        dataToSave.put("invFlag","0");

        int res = this.submitData("OnetimeGPS/" + uuid + "/groups/" + groupId, dataToSave);

        return res;
    }

    //セッションを開始する(firestoreのフラグを立てる
    //uuid:開始するセッションのid
    //戻り値:正常終了:0
    //       失敗:1
    int startSession(String uuid) {
        Map<String, Object> dataToSave = new HashMap<String, Object>();
        dataToSave.put("state", 1);

        int res = this.submitData("OnetimeGPS/" + uuid, dataToSave);

        return res;

    }

    //位置情報を更新する
    //uuid:セッションに割り当てられているid
    //groupId:自分のグループのid
    //groupData:グループの情報を管理するクラス(詳細は後述)
    //          緯度、経度の情報のみが入ったもの
    //戻り値:正常終了:0
    //       失敗:1
    int updateLoc(String uuid, String groupId, GroupData groupData) {
        Map<String, Object> dataToSave = new HashMap<String, Object>();
        dataToSave.put("groupName", groupData.groupName);
        dataToSave.put("x", groupData.x);
        dataToSave.put("y", groupData.y);
        dataToSave.put("member",groupData.member);
        dataToSave.put("state",groupData.state);
        dataToSave.put("invFlag",groupData.invisible);
        int res = this.submitData("OnetimeGPS/" + uuid + "/groups/" + groupId, dataToSave);

        return res;
    }

    //目的地設定用のメソッド
    int updateLoc2(String uuid, String groupId, GroupData groupData) {
        Map<String, Object> dataToSave = new HashMap<String, Object>();
        dataToSave.put("groupName", groupData.groupName);
        dataToSave.put("x", groupData.x);
        dataToSave.put("y", groupData.y);
        dataToSave.put("member",groupData.member);
        dataToSave.put("state",groupData.state);
        dataToSave.put("invFlag","0");
        int res = this.submitData("OnetimeGPS/" + uuid + "/groups/" + groupId, dataToSave);

        return res;
    }

    //すべての班の位置情報を取得する
    //uuid:セッションに割り当てられているid
    //戻り値:GroupData型の配列(詳細は後述)、何もない場合はnull
    ArrayList<GroupData> getLocs(String uuid) {
        return g;
    }

    //セッションの写真の情報を取得する
    //uuid:セッションに割り当てられているid
    //戻り値：PictureData型の配列
    ArrayList<PictureData> getPicture(String uuid) {
        return p;
    }


    //これより下はアルバム機能用のプログラム
    private static final int READ_REQUEST_CODE = 42;

    public FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef;
    public ProgressDialog progress;

    public String groupName;
    public String newPicName;
    public Map<String,Object> dts = null;

    //写真をFirestore storageにアップロードする
    void uploadPicture(String uuid,String groupId,String gName){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        progress = new ProgressDialog(mActivity);

        storageRef = storage.getReferenceFromUrl("gs://practicefirestore-d7f92.appspot.com/"+uuid);
        DocumentReference mDocRef = FirebaseFirestore.getInstance().document("OnetimeGPS/"+uuid+"/groups/"+groupId);

        //写真の名前は班名+現在時刻.pngとする
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        newPicName = String.format(gName+"-%s",sf.format(cal.getTime()));

        dts = new HashMap<String, Object>();
        dts.put("groupName", gName);
        dts.put("x", 0.0);
        dts.put("y", 0.0);

        mActivity.startActivityForResult(intent, READ_REQUEST_CODE);
    }


}

//GroupData型
class GroupData implements Serializable{

    String groupName;//班の名前
    double x;//経度
    double y;//緯度
    ArrayList<String> member;//その班に所属する生徒の情報
    String state;//目的地に到着したか
    String invisible;//公開設定の有無

    //コンストラクタ、フィールドの初期化をする
    public GroupData(String name, double x, double y,ArrayList<String> member,String s) {
        this.groupName = name;
        this.x = x;
        this.y = y;
        this.member = member;
        this.state = s;
        this.invisible = "0";
    }

    //コンストラクタ、オーバーロード
    public GroupData(double x, double y) {
        this.x = x;
        this.y = y;
        this.groupName = String.valueOf(NULL);
    }

    public GroupData(String name, double x, double y,ArrayList<String> member) {
        this.groupName = name;
        this.x = x;
        this.y = y;
        this.member = member;
        this.state="0";
        this.invisible = "0";
    }

    public GroupData(String name, double x, double y,ArrayList<String> member,String s,String flag) {
        this.groupName = name;
        this.x = x;
        this.y = y;
        this.member = member;
        this.state = s;
        this.invisible = flag;
    }

}

//写真の情報を格納するクラス
class PictureData{
    String pictureName;//写真の名前
    String groupName; //どの班の投稿画像か
    double x,y;//写真の位置情報
    Bitmap image;//写真のビットマップ
    String text;//写真のコメント


    public PictureData(String name, String groupName,double x, double y,String text) {
        this.groupName = groupName;
        this.x = x;
        this.y = y;
        this.pictureName = name;
        this.text = text;
    }
}

