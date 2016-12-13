package com.example.ar_slithers;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Random;
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
    // json 相關
    private JSONObject ClientData = new JSONObject();
    private JSONObject transfer = null;

    public player[] player = new player[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // id 對應區
        mLat_input = (EditText)findViewById(R.id.Lat_input);
        mLng_input = (EditText)findViewById(R.id.Lng_input);
        mTitle = (TextView)findViewById(R.id.Title);
        for(int i=0,temp=0; i<4; i++) {
            temp = getResources().getIdentifier("P"+(i+1)+"_lat", "id", getPackageName());
            latView[i] = (TextView)findViewById(temp);
            temp = getResources().getIdentifier("P"+(i+1)+"_lng", "id", getPackageName());
            lngView[i] = (TextView)findViewById(temp);
        }
        // 按鈕事件宣告
        Button cameraBtn = (Button)findViewById(R.id.camera);
        cameraBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 切換畫面到CameraActivity
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, CameraActivity.class);
                startActivity(intent);
            }
        });

        Button sensorBtn = (Button)findViewById(R.id.sensor);
        sensorBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 切換畫面到gyroscope
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SensorActivity.class);
                startActivity(intent);
            }
        });

        Button GPS_Btn = (Button)findViewById(R.id.GPS);
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
        //連線
        thread = new Thread(Connection); // 賦予執行緒工作
        thread.start();
    }

    public void submit_click(){
        Toast toast = Toast.makeText(MainActivity.this,
                "Lat:"+mLat_input.getText().toString()+"\nLng:"+mLng_input.getText().toString(),
                Toast.LENGTH_SHORT);
        toast.show();
        data_in_json();
        // post Request
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new postRequest());

        ExecutorService exe = Executors.newSingleThreadExecutor();
        exe.execute(new simulateMotion());
        Random ran = new Random();
        View test = (View) findViewById(R.id.paint_board);
        PaintBoard.target[0]= ran.nextInt(test.getWidth()); //本人座標
        PaintBoard.target[1]= ran.nextInt(test.getHeight());
        for( int i = 0 ; i < 5 ; i ++){
            int[] temp = {ran.nextInt(test.getWidth()),ran.nextInt(test.getHeight())};
            PaintBoard.other.add(temp); //  其他人座標
//          PaintBoard.other.clear();
        }

    }

    public void data_in_json(){ // 資料轉成 json
        try{
            ClientData.put("lat", mLat_input.getText());
            ClientData.put("lng", mLng_input.getText());
        }catch (Exception e){e.printStackTrace();}
    }

    private void updateData(String ServerData){
        Gson gson = new Gson();
        try {
            transfer =new JSONObject(ServerData);
            player = gson.fromJson(transfer.getString("Data"), player[].class);
        } catch (JSONException e) {e.printStackTrace();}
        for(int i=0; i<4; i++){
            if( player[i]!=null ){
                latView[i].setText(player[i].Lat);
                lngView[i].setText(player[i].Lng);
            }else
                latView[i].setText("此位置尚未加入");
        }
    }

    private Runnable Connection = new Runnable(){
        int serverPort = 12345;
        String serverIp = "192.168.0.106";  // 預設是  輸入 伺服器名稱
        String ServerData = "";
        public void run() {
            try {
                clientSocket = new Socket(serverIp, serverPort);
                DataInputStream input = new DataInputStream( clientSocket.getInputStream() );
                while ( true ){
                    ServerData = input.readUTF();
                    break;
                }
                handler.post(new Runnable() {
                    public void run() {
                        updateData(ServerData);
                        int temp = 999;
                        for(int i = 0 ; i<4 ;i++){ // 找到他是P幾
                            if(player[i]!=null && player[i].ip.equals(clientSocket.getLocalSocketAddress().toString()))
                                temp = player[i].id;
                        }
                        mTitle.setText("Input Location(You are P"+temp+")");
                    }
                });
            }catch (UnknownHostException e1){e1.printStackTrace();   //這兩個是連線錯誤的時候會跑的地方
            }catch (final IOException e1){e1.printStackTrace();}

        }
    };

    class postRequest implements Runnable {
        String ServerData = "";
        public void run() {
            try{
                DataInputStream input = new DataInputStream( clientSocket.getInputStream() );
                DataOutputStream output = new DataOutputStream( clientSocket.getOutputStream() );
                Log.w("test", ClientData.toString());
                // 傳東西給server
                output.writeUTF( ClientData.toString() );
                output.flush();   // 清空緩衝區域 將東西強制送出
                ServerData = input.readUTF();
            }
            catch ( IOException e ){e.printStackTrace();}
            handler.post(new Runnable() {
                public void run() {
                    updateData(ServerData);
                }
            });
        }
    }
    class simulateMotion implements Runnable {
        private Handler handler = new Handler();
        public void run() {
            while(true){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Random ran = new Random();
                PaintBoard.target[0]+=-20+ran.nextInt(40);
                PaintBoard.target[1]+=-20+ran.nextInt(40);
                handler.post(new Runnable() {@Override
                public void run() {
                    mLat_input.setText(PaintBoard.target[0]+"");
                    mLng_input.setText(PaintBoard.target[1]+"");
                }
                });

            }
        }
    }
}
