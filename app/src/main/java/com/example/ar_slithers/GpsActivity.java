package com.example.ar_slithers;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.e6_slithers.R;

import java.text.DecimalFormat;

public class GpsActivity extends AppCompatActivity implements LocationListener {

    private TextView latitude_txt, longitude_txt;
    private boolean getService = false; // 是否已開啟定位服務
    private LocationManager locationManager;
    private String locationProvider;
    private final static int LOCATION_REQUEST_CODE = 3333;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        latitude_txt = (TextView) findViewById(R.id.latitude);
        longitude_txt = (TextView) findViewById(R.id.longitude);

        // 取得系統定位服務
        locationManager = (LocationManager) (this.getSystemService(Context.LOCATION_SERVICE));
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // 如果GPS或網路定位開啟，呼叫locationServiceInitial()更新位置
            getService = true; // 確認開啟定位服務
            locationServiceInitial();
        } else {
            Toast.makeText(this, "請開啟定位服務", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)); // 開啟設定頁面
            setContentView(R.layout.activity_gps);
        }

    }

    private void locationServiceInitial() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationProvider = LocationManager.GPS_PROVIDER;
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationProvider = LocationManager.NETWORK_PROVIDER;
        }
		/* 做法二,由Criteria物件判斷提供最準確的資訊
		 Criteria criteria = new Criteria(); // 資訊提供者選取標準
		 bestProvider = lms.getBestProvider(criteria, true); // 選擇精準度最高的提供者
		 lms.requestLocationUpdates(bestProvider, 60000, 1,locationListener); //當時間超過minTime（單位：毫秒），或者位置移動超過minDistance（單位：米），就會調用listener中的方法更新GPS資訊。
		*/
        useLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    useLocation();
                } else {
                    finish();
                }
                return;
            }
        }
    }

    private void useLocation() {
        try {
            locationManager.requestLocationUpdates(locationProvider, 0 , 0, GpsActivity.this); // 使用GPS定位座標
            Location location = locationManager.getLastKnownLocation(locationProvider);
            getLocation(location);
        } catch (SecurityException e) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
    }

    private void getLocation(Location location) { // 將定位資訊顯示在畫面中
        DecimalFormat df = new DecimalFormat("#.###");
        if (location != null) {
            Double longitude = location.getLongitude(); // 取得經度
            Double latitude = location.getLatitude(); // 取得緯度

            longitude_txt.setText(String.valueOf(df.format(longitude)));
            latitude_txt.setText(String.valueOf(df.format(latitude)));
        } else {
            Toast.makeText(this, "無法定位座標", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        getLocation(location);
        Log.v("map", location.toString());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (getService) {
            try {
                locationManager.removeUpdates(this); // 離開頁面時停止更新
            } catch (SecurityException e) {
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getService) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, GpsActivity.this);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, GpsActivity.this);
            } catch (SecurityException e) {
                finish();
            }
        }
    }
}
