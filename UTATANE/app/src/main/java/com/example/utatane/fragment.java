package com.example.utatane;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class fragment extends Fragment implements SensorEventListener {

    // Global変数定義
    Globals globals;

    // 諸々の定義
    private LineChart mChart;
    private SensorManager mManager;
    private Context contex;
    private TextView Viewtext;

    // 傾向用変数
    static boolean b = false;
    static boolean flg = false;
    static float[] x = new float[10];
    static float[] z = new float[10];
    static float[] arrayAvex = new float[100];
    static float[] arrayAvez = new float[100];
    static int ix;
    static int c;
    static long d;
    static int cnt;
    static float avex = 0;
    static float avez = 0;
    static float calcAvex;
    static float calcAvez;
    static float showCalcAvex = 0;
    static float showCalcAvez = 0;

    // Button on/off T:OFF F:ON
    static boolean butflag = true;

    // Sensor変数
    private static final int MATRIX_SIZE = 16;
    private static final int DIMENSION = 3;
    private float[] mMagneticValues;
    private float[] mAccelerometerValues;

    private float SOAverage = 0f;
    private float STAverage = 0f;

    long SlopeOneInt = 0;
    long SlopeTwoInt = 0;

    // 判定した回数の変数:初期化対象
    long CheckCnt = 0;

    // 通知判定変数
    private int NotiRunCnt = 8;

    // 基準値変数:初期化対象
    private float SlopeOC = 0;
    private float SlopeTC = 0;

    // 動いている判定用変数
    private int GYRWaitCnt = 75;
    private int GYRCnt = 0;

    // 反応範囲設定
    private float XYZGyrJ = 2.0f;
    private float NotiSlope = 20;

    // 動いているかFLG T:動いている F:動いていない
    private boolean moveFlg = true;

    // 前後左右傾き率グラフ用Data変数
    private float[] data = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
    private float[] data2 = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};

    // Timer定義
    private Timer mTimer = null;
    private Timer sTimer = null;
    Handler mHandler = new Handler();

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.layout1, container, false);


    }
    public void onViewCreated(View view,Bundle sevedInstanceState){
       super.onViewCreated(view,sevedInstanceState);

       globals = (Globals) getActivity().getApplication();

       contex = getContext();
       Viewtext = view.findViewById(R.id.Viewtext);

       // MainButton
       view.findViewById(R.id.buttonof).setOnClickListener(
               new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       ImageButton b = (ImageButton) v;

                       if (butflag) { // ON
                           b.setBackgroundResource(R.drawable.imageb);

                           globals.GBackGroundColor = ResourcesCompat.getDrawable(getResources(), R.drawable.background2, null);

                           Viewtext.setText("測定中・・・");
                           Viewtext.setBackgroundColor(Color.argb(31, 34, 139, 34));

                           Sensor(contex);
                           TimerStart();
                           GraphChangeTime();
                           butflag = false;
                       } else { // OFF
                           b.setBackgroundResource(R.drawable.imagea);

                           globals.GBackGroundColor = ResourcesCompat.getDrawable(getResources(), R.drawable.background, null);

                           Viewtext.setText("ボタンを押して\n開始してください");
                           Viewtext.setBackgroundColor(Color.argb(31, 34, 139, 34));

                           Arrays.fill(data,0.0f);
                           Arrays.fill(data2,0.0f);

                           BsetData();
                           SensorStop();

                           sTimer.cancel();
                           sTimer = null;
                           mTimer.cancel();
                           mTimer = null;
                           butflag = true;

                           CheckCnt = 0;
                       }
                   }
               }
       );

        Graph(view);

    }

    private void Sensor(Context context){
        if (mManager == null) {
            // 初回実行時
            mManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
        // 地磁気センサー登録
        mManager.registerListener(this, mManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI);
        // 加速度センサー登録
        mManager.registerListener(this, mManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
        // ジャイロセンサー登録
        mManager.registerListener(this, mManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_UI);
    }

    private void SensorStop(){
        mManager.unregisterListener(this);
    }

    private void TimerStart(){
        mTimer = new Timer(false);
        TimerTask task = new TimerTask() {

            int i = 0;
            long[] SO = {0,0,0,0,0};
            long[] ST = {0,0,0,0,0};

            @Override
            public void run() {

                // 今の時間を取得
                Date d = new Date(System.currentTimeMillis());
                Calendar c = Calendar.getInstance();
                c.setTime(d);
                int nowHourTime = c.get(Calendar.HOUR_OF_DAY);

                // ボタンがON＆動いていない場合:TRUE
                if(!butflag && !moveFlg) {

                    // 傾きを取得＆格納
                    SO[i] = SlopeOneInt;
                    ST[i] = SlopeTwoInt;
                    i++;

                    // ５回ごとに判定を行う
                    if (i == SO.length) {

                        CheckCnt++;

                        // ここで配列内の値を合計＆平均値を算出
                        for(int a = 0; a < SO.length; a++){
                            SOAverage += SO[a];
                            STAverage += ST[a];
                        }

                        SOAverage /= SO.length;
                        STAverage /= SO.length;

                        // 判定一回目のみ基準値を格納
                        if(CheckCnt == 1){
                            SlopeOC = SOAverage;
                            SlopeTC = STAverage;
                        }

                        // 傾きの平均値と基準値を比較
                        boolean AJ = AttitudeJudge(SlopeOC, SlopeTC, SOAverage, STAverage);

                        // BA T:姿勢が悪い F:姿勢が良い
                        if(AJ){
                            // 姿勢が悪い状態が一定時間続いたら通知を行う　およそ１分続くと通知
                            if(globals.NotiCnt % NotiRunCnt == 0 && globals.NotiCnt != 0){
                                // 通知
                                if(showCalcAvex != 0 && showCalcAvez != 0) {

                                    globals.AverageData[globals.p][0] = showCalcAvex;
                                    globals.AverageData[globals.p][1] = showCalcAvez * -1;
                                    globals.p++;

                                    if(globals.p >= globals.AverageData.length){
                                        globals.p = 0;
                                    }else if(globals.AverageData[globals.AverageData.length - 1][0] == 0.0f){
                                        globals.AverageData[globals.p][0] = 999.0f;
                                    }
                                }
                                globals.NotiTimeCnt[nowHourTime]++;
                                globals.NotiFlg = true;
                            }
                            Viewtext.setText("姿勢が悪いです！");
                            Viewtext.setBackgroundColor(Color.argb(31, 228, 0, 0));
                            globals.NotiCnt++;
                        }else{
                            Viewtext.setText("正常です");
                            Viewtext.setBackgroundColor(Color.argb(31, 34, 139, 34));
                            globals.NotiCnt = 0;
                        }
                        i = 0;
                    }
                }
            }
        };
        mTimer.schedule(task, 0, 1000);
    }

    private boolean AttitudeJudge(float SlopeO, float SlopeT, float SOA, float STA){

        if((SOA < SlopeO + NotiSlope && SOA > SlopeO - NotiSlope)&&(STA < SlopeT + NotiSlope && STA > SlopeT - NotiSlope)) {
            return false;
        }
        return true;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy){}

    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){

            // 動いているか動いていないかを判断 X:0 Y:1 Z:2
            if((Math.abs(event.values[0]) < XYZGyrJ && Math.abs(event.values[1]) < XYZGyrJ && Math.abs(event.values[2]) < XYZGyrJ) && !butflag){
                GYRCnt++;
                if(GYRCnt >= GYRWaitCnt) {
                    moveFlg = false;
                }
            }else{
                moveFlg = true;
                GYRCnt = 0;
                globals.NotiCnt = 0;
            }
        }

        switch (event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                // 地磁気センサー
                mMagneticValues = event.values.clone();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                // 加速度センサー
                mAccelerometerValues = event.values.clone();
                break;
            default:
                // それ以外は無視
                return;
        }
        if(mMagneticValues != null && mAccelerometerValues != null) {

            float[] rotationMatrix = new float[MATRIX_SIZE];
            float[] inclinationMatrix = new float[MATRIX_SIZE];
            float[] remapedMatrix = new float[MATRIX_SIZE];
            float[] orientationValues = new float[DIMENSION];

            // 加速度センサーと地磁気センサーから回転行列を取得
            SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, mAccelerometerValues, mMagneticValues);
            SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remapedMatrix);
            SensorManager.getOrientation(remapedMatrix, orientationValues);

            // ラジアン値を変換し、それぞれの回転角度を取得する
            if(radianToDegrees(orientationValues[1]) <= 360 && radianToDegrees(orientationValues[1]) > 270){
                SlopeOneInt = radianToDegrees(orientationValues[1]) - 360;
            }else{
                SlopeOneInt = radianToDegrees(orientationValues[1]);
            }
            if(radianToDegrees(orientationValues[2]) <= 360 && radianToDegrees(orientationValues[2]) > 180){
                SlopeTwoInt = radianToDegrees(orientationValues[2]) - 360;
            }else{
                SlopeTwoInt = radianToDegrees(orientationValues[2]);
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (!b) {
                x[ix] = event.values[0];
                z[ix] = event.values[2];
                b = true;
                count();
            }
        }
    }
    public static void count() {
        Timer timer = new Timer(false);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                cnt++;
                ix++;
                if (ix >= x.length) { // 1サイクルまわったら0にする
                    ix = 0;
                }
                if (cnt > x.length) { // 1サイクル以降の処理
                    for (int f = 0; f < x.length; f++) {
                        avex += x[f]; // 1サイクル分の数値をすべて足す
                        avez += z[f]; // 1サイクル分の数値をすべて足す
                    }
                    arrayAvex[c] = avex / x.length; // 1サイクル分の平均値をarrayAvex[c]に格納
                    arrayAvez[c] = avez / z.length; // 1サイクル分の平均値をarrayAvez[c]に格納
                    avex = 0;
                    avez = 0;
                    cnt = 0;
                    c++;
                    d++;

                    if (flg) {
                        for (int g = 0; g < arrayAvex.length; g++) {// 平均をすべて足す
                            calcAvex += arrayAvex[g];
                            calcAvez += arrayAvez[g];
                        };
                        showCalcAvex = calcAvex / arrayAvex.length;
                        showCalcAvez = calcAvez / arrayAvez.length;
                        calcAvex = 0;
                        calcAvez = 0;
                        if ( c >=arrayAvex.length){
                            c = 0;
                        }
                    }else{
                        if (d >= x.length) {
                            for (int g = 0; g < c; g++) {// 平均をすべて足す
                                calcAvex += arrayAvex[g];
                                calcAvez += arrayAvez[g];
                            }
                            showCalcAvex = calcAvex / c;
                            showCalcAvez = calcAvez / c;
                            calcAvex = 0;
                            calcAvez = 0;
                            if (c >= arrayAvex.length) {
                                c = 0;
                                flg = true;
                            }
                        }
                    }
                }
                b = false;
                timer.cancel();
            }
        };
        timer.schedule(task, 90, 10000);
    }


    private synchronized int radianToDegrees(float angrad) {
        return (int) Math.floor(angrad >= 0 ? Math.toDegrees(angrad) : 360 + Math.toDegrees(angrad));
    }

    private void Graph(View view){
        // 折れ線グラフ全体設定
        mChart = view.findViewById(R.id.line_chart);
        mChart.setDrawGridBackground(false);
        mChart.setTouchEnabled(false);
        mChart.getAxisRight().setEnabled(false);
        mChart.getDescription().setEnabled(true);

        Legend legend = mChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);

        Description description = mChart.getDescription();
        description.setText("前後左右の傾き率");
        description.setTextSize(30f);
        description.setYOffset(10f);
        description.setTextColor(Color.argb(150,0,0,0));

        // X軸用設定
        XAxis xAxis = mChart.getXAxis();
        xAxis.setAxisMaximum(13f);
        xAxis.setAxisMinimum(0f);
        xAxis.setTextSize(15f);
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // Y軸用設定
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setAxisMaximum(100f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextSize(15f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(true);

        // Dataを追加
        BsetData();

        mChart.animateX(0);
    }

    private void BsetData() {

        LineDataSet set1;
        LineDataSet set2;
        ArrayList<Entry> values = new ArrayList<>();
        ArrayList<Entry> values2 = new ArrayList<>();

        // Dataを格納
        for (int i = 0; i < data.length; i++) {
            values.add(new Entry(i, data[i], null, null));
            values2.add(new Entry(i, data2[i], null, null));
        }

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {

            set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            set2 = (LineDataSet) mChart.getData().getDataSetByIndex(1);
            set2.setValues(values2);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            // 一つ目の折れ線グラフ
            set1 = new LineDataSet(values, "前後傾き率");

            set1.setDrawIcons(false);
            set1.setColor(Color.rgb(220, 20, 60));// 線色
            set1.setCircleColor(Color.rgb(220, 20, 60));// 点色
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(0f);
            set1.setDrawFilled(true);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);

            set1.setFillColor(Color.rgb(220, 20, 60));// グラフ内色

            // 二つ目の折れ線グラフ
            set2 = new LineDataSet(values2, "左右傾き率");

            set2.setDrawIcons(false);
            set2.setColor(Color.rgb(255, 165, 0));// 線色
            set2.setCircleColor(Color.rgb(255, 165, 0));// 点色
            set2.setLineWidth(1f);
            set2.setCircleRadius(3f);
            set2.setDrawCircleHole(false);
            set2.setValueTextSize(0f);
            set2.setDrawFilled(true);
            set2.setFormLineWidth(1f);
            set2.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set2.setFormSize(15.f);

            set2.setFillColor(Color.rgb(255, 165, 0));

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            dataSets.add(set2);

            LineData lineData = new LineData(dataSets);

            mChart.setData(lineData);
        }
        mChart.animateX(0);
    }

    // 前後左右傾き率グラフの更新Timer  0.7秒間隔
    public void GraphChangeTime(){
        sTimer = new Timer(false);
        TimerTask task = new TimerTask() {
            public void run() {
                mHandler.post(new Runnable() {
                    public void run() {
                        for (int i = 0; i < 14; i++) {
                            data[i] = data[i + 1];
                            data2[i] = data2[i + 1];
                        }

                        long angle = Math.abs(SlopeOneInt);
                        if (angle > 90) {
                            angle = 90;
                        }
                        long angle2 = Math.abs(SlopeTwoInt);
                        if (angle2 > 90) {
                            angle2 = 90;
                        }

                        data[13] = (angle * 100.0f) / 40.0f;
                        data2[13] = (angle2 * 100.0f) / 40.0f;

                        BsetData();
                    }
                });
            }
        };
        sTimer.schedule(task, 0, 700);
    }
}