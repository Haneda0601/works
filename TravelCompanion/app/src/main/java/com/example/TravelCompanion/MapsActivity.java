package com.example.TravelCompanion;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback , LocationListener,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    //???????????????????????????????????????

    private GoogleMap mMap;
    private LatLng location;
    private ArrayList<Marker> Marker = new ArrayList<Marker>();
    private Marker albumMarker;

    private ArrayList<GroupData> gtest;

    private float results[] = new float[1];

    protected static final String TAG = "location-updates-sample";

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    private final static String LOCATION_KEY = "location-key";
    private final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_CHECK_SETTINGS = 10;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private Boolean mRequestingLocationUpdates = false;
    private String mLastUpdateTime;

    private double oldtempx = 0.0;
    private double oldtempy = 0.0;

    private Timer sTimer = null;
    Handler mHandler = new Handler();

    public FirestoreModule fsm;
    public String sessionId;
    public String data;

    private AlertDialog arrivalDialog;


    SharedPreferences pref;
    SharedPreferences.Editor e;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        results[0] = 999.0f;

        data = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        Intent i = getIntent();
        sessionId = i.getStringExtra("sessionId");
        System.out.println("TEEEEEEEEEST:"+sessionId);
        Globalval.sessionId = sessionId;

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        e = pref.edit();
        e.putString("AcitvityState","MAP");
        e.putString("sessionId",sessionId);
        e.commit();


        fsm = new FirestoreModule(this, sessionId);

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if(sessionId.equals(data)){
            findViewById(R.id.changeButton).setVisibility(View.INVISIBLE);
        }

        findViewById(R.id.checkButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<GroupData>gd = fsm.getLocs(sessionId);

                        Intent intent = new Intent(MapsActivity.this,CheckGroups.class);
                        intent.putExtra("person",gd);
                        startActivity(intent);
                        //stopUpdatesButtonHandler();
                    }
                }
        );

        findViewById(R.id.startButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MapsActivity.this,TimeLine.class);
                        intent.putExtra("sessionId",sessionId);
                        startActivityForResult(intent,1);
                    }
                }
        );

        findViewById(R.id.changeButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MapsActivity.this);
                        Gson gson = new Gson();
                        // ?????????????????????json??????????????????
                        String userSettingString = prefs.getString("GD", "");
                        // json???????????? ???UserSetting??????????????????????????????????????????
                        GroupData gd = gson.fromJson(userSettingString, GroupData.class);
                        String s = "??????????????????";
                        if(gd.invisible.equals("0")){
                            s += "??????";
                        }else{
                            s += "?????????";
                        }
                        new AlertDialog.Builder(MapsActivity.this)
                                .setTitle("?????????????????????????????????(?????????????????????????????????????????????)")
                                .setMessage(s)
                                .setPositiveButton("????????????", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // ?????????????????????????????????
                                        String temp;
                                        if(gd.invisible.equals("0")){
                                            temp = "1";
                                        }else{
                                            temp = "0";
                                        }
                                        gd.invisible = temp;
                                        Map<String,Object>dts = new HashMap<>();
                                        dts.put("invFlag",temp);
                                        fsm.submitData("OnetimeGPS/"+sessionId+"/groups/"+data,dts);
                                        gson.toJson(gd);
                                        e.putString("GD", gson.toJson(gd)).commit();
                                        e.commit();

                                    }
                                })
                                .setNegativeButton("???????????????", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }).show();
                    }
                }
        );


        updateValuesFromBundle(savedInstanceState);
        buildGoogleApiClient();

        //????????????????????????????????????
        startUpdatesButtonHandler();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Globalval.Map = mMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // ????????????????????????????????????
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng longpushLocation) {
                if(data.equals(sessionId)) {

                    LatLng newlocation = new LatLng(longpushLocation.latitude, longpushLocation.longitude);

                    Globalval.LongLoc = longpushLocation;
                    Globalval.NewLoc = newlocation;

                    Globalval.sessionId = sessionId;


                    //Dialog?????????
                    DialogDestination dialog = new DialogDestination();
                    dialog.show(getSupportFragmentManager(), "Dialog");
                }else{
                    Toast.makeText(getApplicationContext(), "????????????????????????", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            public boolean onMarkerClick(Marker marker) {
                if(marker.equals(albumMarker)){
                    new AlertDialog.Builder(MapsActivity.this)
                            .setTitle("??????")
                            .setMessage("????????????????????????????????????")
                            .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    albumMarker.remove();
                                }
                            })
                            .setNegativeButton("?????????", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                }
                return false;
            }
        });

        /*// ???????????????????????????
        location = new LatLng(35.68, 139.76);
        // marker ??????
        mMap.addMarker(new MarkerOptions().position(location).title("Tokyo"));
        // camera ??????
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10));

        // ?????????????????????????????????????????????
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng tapLocation) {
                // tap??????????????????????????????
                location = new LatLng(tapLocation.latitude, tapLocation.longitude);
                String str = String.format(Locale.US, "%f, %f", tapLocation.latitude, tapLocation.longitude);
                mMap.addMarker(new MarkerOptions().position(location).title(str).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14));
            }
        });


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            public boolean onMarkerClick(Marker marker) {
                /// ????????????????????????????????????
                marker.remove();
                /// ????????????????????????????????????false????????????
                /// ???????????????????????????????????????????????????????????????????????????
                /// onClick????????????????????????????????????????????????
                return false;
            }
        });*/

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

        /*/// ????????????
        LatLng loc = new LatLng(35.09062222222222, 136.87594444444446);
        /// ???????????????????????????????????????????????????????????????
        mMap.addMarker(new MarkerOptions().position(loc).title("?????????????????????"));
        /// ??????????????????????????????
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        /// ????????????????????????
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 17));*/
    }

    public void ReceiptGPS(double x, double y, String name) {
        location = new LatLng(x, y);
        String str = String.format(Locale.US, "%s", name);

        if (name.equals("??????")) {
            Marker.add(mMap.addMarker(new MarkerOptions().position(location).title(str).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));
        } else if (name.equals("?????????")) {
            System.out.println("222222222222222222222222222222222Location:" + Globalval.LongLoc + "," + Globalval.NewLoc);

            Marker.add(mMap.addMarker(new MarkerOptions().position(location).title(str).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))));
            Location.distanceBetween(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), x, y, results);

            double eatime = results[0] / 4;
            String hour = String.valueOf((int) Math.floor(eatime / 3600));
            String min = String.valueOf((int) Math.floor((eatime % 3600) / 60));
            ((TextView) findViewById(R.id.eatime)).setText("???????????????" + hour + "??????" + min + "???");

        } else {
            Marker.add(mMap.addMarker(new MarkerOptions().position(location).title(str).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));
        }

        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14));

    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
            }

            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
            //updateUI();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void startUpdatesButtonHandler() {
        //clearUI();
        if (!isPlayServicesAvailable(this)) return;
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
        } else {
            return;
        }

        if (Build.VERSION.SDK_INT < 23) {
            startLocationUpdates();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                showRationaleDialog();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
    }

    public void stopUpdatesButtonHandler() {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            stopLocationUpdates();
        }
    }

    private void startLocationUpdates() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        // ?????????????????????????????????????????????????????????????????????????????????????????????
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // ??????????????????????????????????????????????????????????????????
                        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, MapsActivity.this);
                            Log.d("update", "UPPPPPPPPPPPPPPPPPPPPPPPPPP");
                            LocationSendTime();
                            System.out.println("11111111111111111111111111Location:" + Globalval.LongLoc + "," + Globalval.NewLoc);


                            mMap.setMyLocationEnabled(true);
                        }
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // ????????????????????????????????????????????????????????????????????????
                        try {
                            status.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    /*private void clearUI() {
        latitudeText.setText("");
        longitudeText.setText("");
        lastUpdateTimeText.setText("");
    }

    private void updateUI() {
        if (mCurrentLocation == null) return;
        latitudeText.setText(String.valueOf(mCurrentLocation.getLatitude()));
        longitudeText.setText(String.valueOf(mCurrentLocation.getLongitude()));
        lastUpdateTimeText.setText(mLastUpdateTime);
    }*/

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        mRequestingLocationUpdates = false;
                        Toast.makeText(MapsActivity.this, "??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                    } else {
                        showRationaleDialog();
                    }
                }
                break;
            }
        }
    }

    private void showRationaleDialog() {
        new AlertDialog.Builder(this)
                .setPositiveButton("????????????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MapsActivity.this,
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                    }
                })
                .setNegativeButton("?????????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MapsActivity.this, "?????????????????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                        mRequestingLocationUpdates = false;
                    }
                })
                .setCancelable(false)
                .setMessage("??????????????????????????????????????????????????????????????????????????????")
                .show();
    }

    public static boolean isPlayServicesAvailable(Context context) {
        // Google Play Service APK???????????????????????????????????????
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().getErrorDialog((Activity) context, resultCode, 2).show();
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                break;
            case 1:
                if(resultCode == RESULT_OK) {
                    double x = data.getDoubleExtra("x", 0.0);
                    double y = data.getDoubleExtra("y", 0.0);

                    if (x != 0.0) {
                        System.out.println(x + ":" + y);
                        LatLng loc = new LatLng(y, x);
                        albumMarker = mMap.addMarker(new MarkerOptions().position(loc).title("????????????").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                    }
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        isPlayServicesAvailable(this);

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        stopLocationUpdates();
        mGoogleApiClient.disconnect();

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            //updateUI();
        }

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        //updateUI();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void LocationSendTime() {
        sTimer = new Timer(false);
        TimerTask task = new TimerTask() {
            public void run() {
                mHandler.post(new Runnable() {
                    public void run() {

                        if(mCurrentLocation == null){
                            return;
                        }

                        if (oldtempx != mCurrentLocation.getLatitude() || oldtempy != mCurrentLocation.getLongitude()) {
                            //Log.d("Location:", "x " + mCurrentLocation.getLatitude() + "y" + mCurrentLocation.getLongitude());

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MapsActivity.this);
                            Gson gson = new Gson();
                            // ?????????????????????json??????????????????
                            String userSettingString = prefs.getString("GD", "");
                            // json???????????? ???UserSetting??????????????????????????????????????????
                            GroupData gd = gson.fromJson(userSettingString, GroupData.class);

                            gd.x = mCurrentLocation.getLatitude();
                            gd.y = mCurrentLocation.getLongitude();

                            e.putString("GD", gson.toJson(gd)).commit();
                            e.commit();

                            fsm.updateLoc(sessionId, data, gd);

                            if (Marker != null) {
                                for (Marker i : Marker) {
                                    i.remove();
                                }
                                Marker.clear();
                            }

                            gtest = fsm.getLocs(data);
                            if(gtest!=null) {
                                for (GroupData i : gtest) {
                                    if (!gd.groupName.equals(i.groupName) &&
                                            (gd.invisible.equals("0") && i.invisible.equals("0") ||
                                                    gd.invisible.equals("1") && (i.groupName.equals("??????") || i.groupName.equals("?????????")))){
                                            ReceiptGPS(i.x, i.y, i.groupName);
                                    }else if(!gd.groupName.equals(i.groupName) && gd.groupName.equals("??????")){
                                        ReceiptGPS(i.x, i.y, i.groupName);
                                    }
                                }
                            }

                            if(results[0] <= 100.0f){
                                Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                                fsm.goal(sessionId,data,gd);
                            }

                            oldtempx = mCurrentLocation.getLatitude();
                            oldtempy = mCurrentLocation.getLongitude();

                            //?????????????????????
                            if(fsm.getState().equals("1")){

                                if( arrivalDialog != null && arrivalDialog.isShowing())return;

                                AlertDialog.Builder dialog = new AlertDialog.Builder(MapsActivity.this)
                                        .setTitle("??????????????????????????????")
                                        .setMessage("OK???????????????????????????????????????????????????")
                                        .setCancelable(false)
                                        .setIcon(R.drawable.destination_icon)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                e.putString("AcitvityState","NULL");
                                                e.putString("sessionId","NULL");
                                                e.commit();
                                                fsm.deleteGroups(data);
                                                if(data.equals(sessionId)){
                                                    fsm.deleteGroups("goal");
                                                }
                                                Intent intent = new Intent(MapsActivity.this,MainActivity.class);
                                                startActivity(intent);
                                            }
                                        });
                                arrivalDialog = dialog.create();
                                arrivalDialog.show();

                            }
                        }

                    }
                });
            }
        };
        sTimer.schedule(task, 0, 1000);
    }


}
