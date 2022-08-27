package com.example.utatane;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BubbleChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BubbleData;
import com.github.mikephil.charting.data.BubbleDataSet;
import com.github.mikephil.charting.data.BubbleEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBubbleDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class fragment2 extends Fragment {

    // Global変数定義
    Globals globals;

    // グラフ定義
    private BarChart bChart;
    private BubbleChart BbChart;

    // TextView定義
    private TextView LRFBtext;
    private TextView countText;

    // Timer定義
    private Timer rTimer;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout2, container, false);
    }

    public void onViewCreated(View view, Bundle sevedInstanceState) {
        super.onViewCreated(view, sevedInstanceState);

        globals = (Globals) getActivity().getApplication();
        LRFBtext = view.findViewById(R.id.trendText);
        countText = view.findViewById(R.id.countText);

        GraphChange(view);
        text();

        // 更新Button
        view.findViewById(R.id.reloadbut).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ImageButton b = (ImageButton) v;

                        b.setBackgroundResource(R.drawable.reloadicong);
                        reloadwait(b);

                        GraphChange(view);
                        text();
                    }
                }
        );
    }

    // Reload画像変更用Timer
    public void reloadwait(ImageButton but){
        rTimer = new Timer(false);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                but.setBackgroundResource(R.drawable.reloadicon);
            }
        };
        rTimer.schedule(task, 500);
    }

    public void GraphChange(View view) {

        // バブルチャートグラフ全体設定
        BbChart = view.findViewById(R.id.bubble_chart);
        BbChart.setDrawGridBackground(false);
        BbChart.setBackgroundResource(R.drawable.b);
        BbChart.setTouchEnabled(false);
        BbChart.getAxisRight().setEnabled(false);

        Legend legend = BbChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);

        Description description = BbChart.getDescription();
        description.setText("前後左右の位置傾向");
        description.setTextSize(13f);
        description.setYOffset(5f);
        description.setTextColor(Color.argb(150,0,0,0));
        description.setEnabled(true);

        // X軸用設定
        XAxis xAxis = BbChart.getXAxis();
        xAxis.setAxisMaximum(11f);
        xAxis.setAxisMinimum(-11f);
        xAxis.setTextSize(15f);
        xAxis.enableGridDashedLine(20f, 20f, 0f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // Y軸用設定
        YAxis leftAxis = BbChart.getAxisLeft();
        leftAxis.setAxisMaximum(11f);
        leftAxis.setAxisMinimum(-11f);
        leftAxis.setTextSize(15f);
        leftAxis.enableGridDashedLine(20f, 20f, 0f);
        leftAxis.setDrawZeroLine(true);

        // Dataを追加
        BbsetData();

        // 棒グラフ全体設定
        bChart = view.findViewById(R.id.bar_chart);
        bChart.setDrawGridBackground(false);
        bChart.setTouchEnabled(false);
        bChart.getAxisRight().setEnabled(false);

        Legend blegend = bChart.getLegend();
        blegend.setForm(Legend.LegendForm.CIRCLE);

        Description bdescription = bChart.getDescription();
        bdescription.setText("一日の通知時間と回数");
        bdescription.setTextSize(20f);
        bdescription.setYOffset(10f);
        bdescription.setTextColor(Color.argb(150,0,0,0));
        bdescription.setEnabled(true);

        // X軸用設定
        XAxis bxAxis = bChart.getXAxis();
        bxAxis.setAxisMaximum(23);
        bxAxis.setAxisMinimum(0);
        bxAxis.setTextSize(15f);
        bxAxis.enableGridDashedLine(20, 20, 0);
        bxAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // Y軸用設定
        YAxis bleftAxis = bChart.getAxisLeft();
        bleftAxis.setAxisMaximum(15);
        bleftAxis.setAxisMinimum(0);
        bleftAxis.setTextSize(15f);
        bleftAxis.enableGridDashedLine(20, 20, 0);
        bleftAxis.setDrawZeroLine(true);

        // Dataを追加
        BsetData();

        BbChart.animateX(2500);
        bChart.animateX(2500);
    }

    private void BbsetData() {

        BubbleDataSet set1;
        ArrayList<BubbleEntry> values = new ArrayList<>();

        // Dataを格納
        for (int i = 0; i < globals.AverageData.length; i++) {
            if (globals.AverageData[i][0] == 999.0f) break;

            values.add(new BubbleEntry(globals.AverageData[i][0], globals.AverageData[i][1], 30f));
        }

        Collections.sort(values,new EntryXComparator());

        if (BbChart.getData() != null &&
                BbChart.getData().getDataSetCount() > 0) {

            set1 = (BubbleDataSet) BbChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            BbChart.getData().notifyDataChanged();
            BbChart.notifyDataSetChanged();
        } else {
            set1 = new BubbleDataSet(values, "各位置傾向");

            set1.setDrawIcons(false);
            set1.setColor(Color.rgb(34, 139, 34));
            set1.setValueTextSize(0f);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);

            ArrayList<IBubbleDataSet> dataSets = new ArrayList<IBubbleDataSet>();
            dataSets.add(set1);

            BubbleData BbData = new BubbleData(dataSets);

            BbChart.setData(BbData);
        }
    }

    private void BsetData() {

        BarDataSet set1;
        ArrayList<BarEntry> values = new ArrayList<>();

        // Dataを格納
        for (int i = 0; i < globals.NotiTimeCnt.length; i++) {
            values.add(new BarEntry(i, globals.NotiTimeCnt[i], null, null));
        }

        if (bChart.getData() != null &&
                bChart.getData().getDataSetCount() > 0) {

            set1 = (BarDataSet) bChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            bChart.getData().notifyDataChanged();
            bChart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(values, "通知回数");

            set1.setDrawIcons(false);
            set1.setColor(Color.rgb(34, 139, 34));
            set1.setValueTextSize(0f);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);

            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);

            BarData bData = new BarData(dataSets);

            bChart.setData(bData);
        }

        int NotiCnt = 0;
        for(int i : globals.NotiTimeCnt){
            NotiCnt += i;
        }

        String str = "今日１日に、" + NotiCnt + "回通知が行われました。";
        countText.setText(str);
    }

    private void text() {

        // 出力用文字列
        String[] arrayTextLR = {"右にかなり傾いています。", "右に少し傾いています。", "正常です。", "左に少し傾いています。", "左にかなり傾いています。"};
        String[] arrayTextFB = {"後ろにかなり傾いています。", "後ろに少し傾いています。", "正常です。", "前に少し傾いています。", "前にかなり傾いています。"};

        // 基準変数:左右
        float kijunANormalRight =1.0f;
        float kijunAHighRight =5.0f;
        float kijunANormalLeft =-1.0f;
        float kijunAHighLeft =-5.0f;

        // 基準変数:前後
        float kijunBNormalBack =1.0f;
        float kijunBHighBack =2.5f;
        float kijunBNormalFront =-1.0f;
        float kijunBHighFront =-2.5f;

        int[][] LRFBcnt = new int[5][5];
        int x = 2;
        int y = 2;
        int LRAvg = 0;
        int FBAvg = 0;

        // Dataの位置を格納
        for (int i = 0; i < globals.AverageData.length; i++) {
            if (globals.AverageData[i][0] == 999.0f) break;

            if (globals.AverageData[i][0] > kijunAHighRight) {// 右にかなり傾き
                x = 0;

            } else if (globals.AverageData[i][0] > kijunANormalRight) {// 右に少し傾き
                x = 1;
            }

            if (globals.AverageData[i][0] < kijunAHighLeft) {// 左にかなり傾き
                x = 4;
            } else if (globals.AverageData[i][0] < kijunANormalLeft) { // 左に少し傾き
                x = 3;
            }

            if (globals.AverageData[i][1] > kijunBHighBack) {// 後ろにかなり傾き
                y = 4;
            } else if (globals.AverageData[i][1] > kijunBNormalBack) {// 後ろに少し傾き
                y = 3;
            }

            if (globals.AverageData[i][1] < kijunBHighFront) {// 前にかなり傾き
                y = 0;
            } else if (globals.AverageData[i][1] < kijunBNormalFront) {// 前に少し傾き
                y = 1;
            }

            LRFBcnt[y][x] = 1;
        }

        // 評価を行う
        for(int f = 0;f < 5;f++){
            for(int s = 0;s < 5;s++){
                if(LRFBcnt[f][s] == 1){
                    LRAvg += (s - 2);
                    FBAvg += (f - 2);
                }
            }
        }

        // Error対策用IF
        if(LRAvg > 2){
            LRAvg = 2;
        }else if(LRAvg < -2){
            LRAvg = -2;
        }
        if(FBAvg > 2){
            FBAvg = 2;
        }else if(FBAvg < -2){
            FBAvg = -2;
        }

        // 評価による文字の出力
        String str = "";
        if(LRAvg + 2 == 2){
            str = "あなたの左右の傾向は" + arrayTextLR[LRAvg + 2];
        }else{
            str = "あなたは" + arrayTextLR[LRAvg + 2];
        }
        if(FBAvg + 2 == 2){
            str = str + "\nあなたの左右の傾向は" + arrayTextFB[FBAvg + 2];
        }else{
            str = str + "\nあなたは" + arrayTextFB[FBAvg + 2];
        }

        LRFBtext.setText(str);

    }

}
