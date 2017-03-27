package com.example.ar_slithers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.DecimalFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.e6_slithers.R;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private TextView gyroinformation, gsensorinformation, magneticinformation, orientationinformation;
    private SensorManager sensorManager;
    private Sensor gyrosensor, gsensor, msensor;

    float[] accelerometerValues = new float[3];
    float[] magneticFieldValues = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        gyroinformation = (TextView)findViewById(R.id.gyroinfo);
        gsensorinformation = (TextView)findViewById(R.id.gsensorinfo);
        magneticinformation = (TextView)findViewById(R.id.magninfo);
        orientationinformation = (TextView)findViewById(R.id.orientation);

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        //抓陀螺儀的資料
        gyrosensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, gyrosensor, SensorManager.SENSOR_DELAY_GAME);

        //抓 g sensor 的資料
        gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, gsensor, SensorManager.SENSOR_DELAY_NORMAL);

        //抓 magnet sensor 的資料
        msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, msensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onSensorChanged(SensorEvent event) {
        String x, y, z;

        float[] values = new float[3];
        float[] rotationMatrix = new float[9];

        DecimalFormat df = new DecimalFormat("#.###");

		/*if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){ // 陀螺儀
			x = df.format(event.values[0]);
            y = df.format(event.values[1]);
            z = df.format(event.values[2]);
			gyroshowInfo("事件：" + " x:" + x + "\ny:" + y  + "\nz:" + z);
		}*/

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){ // g sensor
            x = df.format(event.values[0]);
            y = df.format(event.values[1]);
            z = df.format(event.values[2]);
            gShowInfo("x:" + x + "\ny:" + y  + "\nz:" + z);

            //存入矩陣
            accelerometerValues = event.values;
            float temp = accelerometerValues[1];
            accelerometerValues[1] = accelerometerValues[2];
            accelerometerValues[2] = temp;
        }

        if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){ // m sensor
            x = df.format(event.values[0]);
            y = df.format(event.values[1]);
            z = df.format(event.values[2]);
            mShowInfo("x:" + x + "\ny:" + y  + "\nz:" + z);

            //存入矩陣
            magneticFieldValues = event.values;
            float temp = magneticFieldValues[1];
            magneticFieldValues[1] = magneticFieldValues[2];
            magneticFieldValues[2] = temp;
        }

        calculateOrientation();
    }

    private  void calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];

        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);

        float[] degree = {(float)Math.toDegrees(values[0]),
                (float)Math.toDegrees(values[1]), (float)Math.toDegrees(values[2])};

        orienShowInfo("azimuth方位角:" + degree[0] + "\npitch傾斜角:" + degree[1]  + "\nroll旋轉角:" + degree[2]);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void gyroShowInfo(String info){
        gyroinformation.setText(info);
    }

    private void gShowInfo(String info){
        gsensorinformation.setText(info);
    }

    private void mShowInfo(String info){
        magneticinformation.setText(info);
    }

    private void orienShowInfo(String info){
        orientationinformation.setText(info);
    }
}
