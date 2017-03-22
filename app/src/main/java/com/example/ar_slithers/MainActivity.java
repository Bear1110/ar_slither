package com.example.ar_slithers;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.e6_slithers.R;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText mLat_input, mLng_input;
    private TextView mTitle;
    private TextView latView[] = new TextView[4];
    private TextView lngView[] = new TextView[4];
    // 與連線有關的參數
    private Handler handler = new Handler();
    private Socket clientSocket;
    private Thread thread;
    // GPS相關
    private LocationManager lms;
    private Location location;
    private String bestProvider = LocationManager.GPS_PROVIDER;

    private player[] player = new player[4];
    private int id  = 999;
    private String serverIp = "140.115.204.124"; // 預設是  輸入 伺服器名稱

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText inputIp = new EditText(this);
        inputIp.setText(serverIp);
        new AlertDialog.Builder(this)
                .setTitle("Server IP")
                .setView(inputIp)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        serverIp = inputIp.getText().toString();
                        // id 對應區
                        viewIdToObject();
                        // 事件宣告
                        createEvents();
                        // 取得系統定位服務
                        LocationManager locationManager
                                = (LocationManager) (MainActivity.this.getSystemService(Context.LOCATION_SERVICE));
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
                    }
                })
                .show();
    }

    // id 對應區
    private void viewIdToObject() {
        mLat_input = (EditText) findViewById(R.id.Lat_input);
        mLng_input = (EditText) findViewById(R.id.Lng_input);
        mTitle = (TextView) findViewById(R.id.Title);
        for (int i = 0, temp = 0; i < 4; i++) {
            temp = getResources().getIdentifier("P" + (i + 1) + "_lat", "id", getPackageName());
            latView[i] = (TextView) findViewById(temp);
            temp = getResources().getIdentifier("P" + (i + 1) + "_lng", "id", getPackageName());
            lngView[i] = (TextView) findViewById(temp);
        }
    }
    // 事件宣告
    private void createEvents() {
        Button cameraBtn = (Button) findViewById(R.id.camera);
        cameraBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 切換畫面到CameraActivity
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, CameraActivity.class);
                startActivity(intent);
            }
        });

        Button sensorBtn = (Button) findViewById(R.id.sensor);
        sensorBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 切換畫面到gyroscope
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SensorActivity.class);
                startActivity(intent);
            }
        });

        Button GPS_Btn = (Button) findViewById(R.id.GPS);
        GPS_Btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 切換畫面到GPS
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, GpsActivity.class);
                startActivity(intent);
            }
        });
        // 傳送資料給server
        Button mSubmitButton = (Button) findViewById(R.id.Submit);
        mSubmitButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit_click();
            }
        });
    }

    private void locationServiceInitial() {
        lms = (LocationManager) getSystemService(LOCATION_SERVICE);
//        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            locationProvider = LocationManager.GPS_PROVIDER;
//        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//            locationProvider = LocationManager.NETWORK_PROVIDER;
//        }
         /*做法二,由Criteria物件判斷提供最準確的資訊*/
        Criteria criteria = new Criteria(); // 資訊提供者選取標準
        bestProvider = lms.getBestProvider(criteria, true); // 選擇精準度最高的提供者

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lms.requestLocationUpdates(bestProvider, 1000, 1, locationListener);//當時間超過minTime（單位：毫秒），或者位置移動超過minDistance（單位：米），就會調用listener中的方法更新GPS資訊。
        Location location = lms.getLastKnownLocation(bestProvider);
        getLocation(location);
    }

    private void getLocation(Location location) { // 將定位資訊顯示在畫面中
        DecimalFormat df = new DecimalFormat("#.######"); //精準到第幾位
        if (location != null) {
            Double longitude = location.getLongitude(); // 取得經度
            Double latitude = location.getLatitude(); // 取得緯度
            mLat_input.setText(String.valueOf(df.format(longitude)));
            mLng_input.setText(String.valueOf(df.format(latitude)));
        } else {
            Toast.makeText(this, "無法定位座標", Toast.LENGTH_LONG).show();
        }
    }

    public void submit_click() {
    }
    private void updateData(String ServerData) {
        Gson gson = new Gson();
        try {
            JSONObject transfer = new JSONObject(ServerData);
            player = gson.fromJson(transfer.getString("Data"), player[].class);
        } catch (JSONException e) {e.printStackTrace();}

        PaintBoard.other.clear();
        PaintMap.other_point.clear();
        for (int i = 0; i < 4; i++) {
            if (player[i] != null) {
                latView[i].setText(player[i].Lat);
                lngView[i].setText(player[i].Lng);
                if(!player[i].Lat.equals("")) {
                    if (player[i].id == id) {//自己
                        PaintBoard.target[0] = player[i].map[0]; //本人座標
                        PaintBoard.target[1] = player[i].map[1];
                        PaintMap.other_point.add(player[i].map);
                    } else {
//                        double[] temp = {Double.parseDouble(player[i].Lat), Double.parseDouble(player[i].Lng)};
                        double[] temp = {player[i].map[0], player[i].map[1]};
                        PaintBoard.other.add(temp); //  其他人座標
                        PaintMap.other_point.add(player[i].map);
                    }
                }
            } else
                latView[i].setText("此位置尚未加入");
        }
    }

    private Runnable Connection = new Runnable()/*傳送值用的*/ {
        int serverPort = 12345;
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
                        Thread.sleep(1000);
                        DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
                        JSONObject ClientData = new JSONObject();
                        try {
                            ClientData.put("lat", mLat_input.getText());
                            ClientData.put("lng", mLng_input.getText());
//                            ClientData.put("lat", "121.187504");
//                            ClientData.put("lng", "24.966835");
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
