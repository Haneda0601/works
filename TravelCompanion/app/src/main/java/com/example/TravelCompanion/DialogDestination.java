package com.example.TravelCompanion;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DialogDestination extends DialogFragment {

    private GoogleMap mMap;
    private LatLng newlocation;
    private LatLng longlocation;
    private String id;

    Calendar calendar = Calendar.getInstance();
    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    int minute = calendar.get(Calendar.MINUTE);
    FirestoreModule fsm = new FirestoreModule();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setTitle("目的地を設定しますか？")
                .setIcon(R.drawable.destination_icon)

                //タイトル表示文字

                //肯定的なボタン（はい）が押された場合
                .setPositiveButton("はい", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                TimePickerDialog builder = new TimePickerDialog(
                                        (MapsActivity) getActivity(),
                                        new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                                if(Globalval.DestinationMarkertemp != null) {
                                                    Globalval.DestinationMarkertemp.remove();
                                                    Log.d("test","TTTTTTTTTTTTTTTTTTTTTTTTTTTTT");
                                                }

                                                Globalval.DestinationMarkertemp = Globalval.Map.addMarker(new MarkerOptions().position(Globalval.NewLoc).title("目的地").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                                                fsm.updateLoc2(Globalval.sessionId,"goal",new GroupData("目的地",Globalval.LongLoc.latitude,Globalval.LongLoc.longitude,new ArrayList<String>(),"1"));
                                                Log.d("TimeSet", String.format("%02d:%02d", hourOfDay, minute));
                                                Map<String,Object> dataToSave = new HashMap<String, Object>();
                                                dataToSave.put("time",hourOfDay+"時"+minute+"分");
                                                dataToSave.put("state","0");
                                                fsm.submitData("OnetimeGPS/" + Globalval.sessionId, dataToSave);

                                            }
                                        }, hour, minute, true
                                );
                                builder.setCancelable(true);
                                builder.setTitle("時間を設定する");
                                builder.show();
                            }
                        }
                )

                //否定的なボタン（いいえ）が押された場合
                .setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(((MapsActivity) getActivity()), "目的地が設定されませんでした", Toast.LENGTH_SHORT).show();
                            }
                        }
                )
                .create();  //これによりDialogを生成。
    }


}
