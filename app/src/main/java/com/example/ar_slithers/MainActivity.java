package com.example.ar_slithers;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.SurfaceView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ar_slithers.Draw.DrawCircle;
import com.example.ar_slithers.Draw.SnakeInfo;
import com.example.e6_slithers.R;
import com.google.gson.Gson;
import com.microsoft.azure.mobile.MobileCenter;
import com.microsoft.azure.mobile.analytics.Analytics;
import com.microsoft.azure.mobile.crashes.Crashes;
import com.microsoft.azure.mobile.distribute.Distribute;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;

//mobile center

public class MainActivity extends AppCompatActivity {

    private TextView mTitle;
    private FrameLayout drawLayout;
    private TextView info;
    // 與連線有關的參數
    private Handler handler = new Handler();
    private Socket clientSocket;
    private Thread thread;
    // GPS相關
    private LocationManager locationManager;
    private Location location;
    private String bestProvider = LocationManager.GPS_PROVIDER;
    private final static int LOCATION_REQUEST_CODE = 3333;
    private String mLat, mLng;
    // Camera相關
    private SurfaceView cameraView;
    private CameraManager cameraManager;
    private final static int CAMERA_REQUEST_CODE = 2222;
    private OpenCamera openCam;

    private player[] player = new player[4];
    private int id  = 999;
    private String serverIp = "192.168.0.100"; // 預設是  輸入 伺服器名稱

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MobileCenter.start(getApplication(), "75774d09-5381-4c9e-9e51-8bca3b6440bb",Analytics.class, Crashes.class, Distribute.class);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText inputIp = new EditText(this);
        inputIp.setText(serverIp);
        // id 對應區
        viewIdToObject();
        // 開啟相機
        startCamera();
        new AlertDialog.Builder(this)
                .setTitle("Server IP")
                .setView(inputIp)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        serverIp = inputIp.getText().toString();
                        // 取得系統定位服務
                        locationManager= (LocationManager) (MainActivity.this.getSystemService(Context.LOCATION_SERVICE));
                        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            // 如果GPS或網路定位開啟，呼叫locationServiceInitial()更新位置
                            locationServiceInitial();
                        } else {
                            Toast.makeText(MainActivity.this, "請開啟定位服務", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)); // 開啟設定頁面
                            setContentView(R.layout.activity_gps);
                        }
                        //連線
                        thread = new Thread(Connection); // 賦予執行緒工作
                        thread.start();
                        drawCamera();
                    }
                })
                .show();
    }

    // id 對應區
    private void viewIdToObject() {
        mTitle = (TextView) findViewById(R.id.Title);
        drawLayout = (FrameLayout) findViewById(R.id.frame);
        info = (TextView)findViewById(R.id.info);
        cameraView = (SurfaceView) findViewById(R.id.cameraView);
    }

    private void locationServiceInitial() {
//        lms = (LocationManager) getSystemService(LOCATION_SERVICE);
//        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            locationProvider = LocationManager.GPS_PROVIDER;
//        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//            locationProvider = LocationManager.NETWORK_PROVIDER;
//        }
         /*做法二,由Criteria物件判斷提供最準確的資訊*/
        Criteria criteria = new Criteria(); // 資訊提供者選取標準
        bestProvider = locationManager.getBestProvider(criteria, true); // 選擇精準度最高的提供者
        if (bestProvider == null) {
            bestProvider = LocationManager.GPS_PROVIDER;
        }
        useLocation();
    }

    private void useLocation() {
        try {
            locationManager.requestLocationUpdates(bestProvider, 500, 1, locationListener);//當時間超過minTime（單位：毫秒），或者位置移動超過minDistance（單位：米），就會調用listener中的方法更新GPS資訊。
            Location location = locationManager.getLastKnownLocation(bestProvider);
            getLocation(location);
        } catch (SecurityException e) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
    }

    private void getLocation(Location location) { // 將定位資訊顯示在畫面中
        DecimalFormat df = new DecimalFormat("#.######"); //精準到第幾位
        if (location != null) {
            Double longitude = location.getLongitude(); // 取得經度
            Double latitude = location.getLatitude(); // 取得緯度
            mLat = String.valueOf(df.format(longitude));
            mLng = String.valueOf(df.format(latitude));
        } else {
            Toast.makeText(this, "無法定位座標", Toast.LENGTH_LONG).show();
        }
    }

    private void startCamera() {
        // Camera Surface View
        cameraManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
        openCam = new OpenCamera(info, this, cameraManager, cameraView);
    }

    private void drawCamera() {
        // Draw in Layout
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        DrawCircle drawCircle = new DrawCircle(this, displayMetrics.density, info,
                (SensorManager)getSystemService(Context.SENSOR_SERVICE));
        drawLayout.addView(drawCircle);
    }

    // 詢問使用者是否能取得權限
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
            }
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    openCam.openCamera();
                } else {
                    finish();
                }
            }
        }
    }

    private void updateData(String ServerData) {
        Gson gson = new Gson();
        try {
            JSONObject transfer = new JSONObject(ServerData);
            player = gson.fromJson(transfer.getString("Data"), player[].class);
        } catch (JSONException e) {e.printStackTrace();}

        PaintBoard.other.clear();
        PaintMap.other_point.clear();
        DrawCircle.otherSnakes.clear();
        for (int i = 0; i < 4; i++) {
            if (player[i] != null) {
                if(player[i].map[0] != 0) {
                    if (player[i].id == id) {//自己
                        PaintBoard.target[0] = player[i].map[0]; //本人座標
                        PaintBoard.target[1] = player[i].map[1];
                        PaintMap.other_point.add(player[i].map);
                    } else {
                        double[] temp = {player[i].map[0], player[i].map[1]};
                        PaintBoard.other.add(temp); //  其他人座標
                        PaintMap.other_point.add(player[i].map);
                        DrawCircle.otherSnakes.add(new SnakeInfo(player[i].body, i));
                    }
                }
            }
        }
    }

    private Runnable Connection = new Runnable()/*傳送值用的*/ {
        final int serverPort = 12345;
        String ServerData = "";

        public void run() {
            try {
                clientSocket = new Socket(serverIp, serverPort);
                DataInputStream input = new DataInputStream(clientSocket.getInputStream());
                ServerData = input.readUTF();

                try {
                    JSONObject transfer = new JSONObject(ServerData);
                    id = Integer.parseInt(transfer.getString("id"));
                } catch (JSONException e) {e.printStackTrace();}
                handler.post(new Runnable() {
                    public void run() {
                        updateData(ServerData);
                        mTitle.setText("Input Location(You are P" + id + ")");
                    }
                });
                while (true) {
                    try {
                        Thread.sleep(500);
                        DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
                        JSONObject ClientData = new JSONObject();
                        try {
                            ClientData.put("lat", mLat);
                            ClientData.put("lng", mLng);
                        } catch (Exception e) {e.printStackTrace();}
                        // 傳東西給server
                        output.writeUTF(ClientData.toString());
                        output.flush();   // 清空緩衝區域 將東西強制送出
                        ServerData = input.readUTF();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.post(new Runnable() {
                        public void run() {
                            updateData(ServerData);
                        }
                    });
                }
            } catch (UnknownHostException e1) {
                e1.printStackTrace();   //這兩個是連線錯誤的時候會跑的地方
            } catch (final IOException e1) {
                e1.printStackTrace();
            }
        }
    };
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) { //
            getLocation(location);
        }
        @Override
        public void onProviderDisabled(String arg0) {//
        }

        @Override
        public void onProviderEnabled(String arg0) { //
        }

        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) { //
        }
    };
}
