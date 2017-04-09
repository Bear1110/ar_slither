package com.example.ar_slithers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.e6_slithers.R;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private TextView gyroinformation, gsensorinformation, magneticinformation, orientationinformation;
    private SensorManager sensorManager;
    private Sensor gyrosensor, gsensor, msensor;

    private static final float ALPHA = 0.25f;

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
//        gyrosensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
//        sensorManager.registerListener(this, gyrosensor, SensorManager.SENSOR_DELAY_GAME);

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

		/*if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){ // 陀螺儀
			x = df.format(event.values[0]);
            y = df.format(event.values[1]);
            z = df.format(event.values[2]);
			gyroshowInfo("事件：" + " x:" + x + "\ny:" + y  + "\nz:" + z);
		}*/

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){ // g sensor
//            float xFormer = Float.valueOf(df.format(event.values[0]));
//            float xLatter = event.values[0] - xFormer;
//            float yFormer = Float.valueOf(df.format(event.values[2]));
//            float yLatter = event.values[2] - yFormer;

            //存入矩陣
            accelerometerValues = lowPass(event.values.clone(), accelerometerValues);
            x = String.valueOf(accelerometerValues[0]);
            y = String.valueOf(accelerometerValues[1]);
            z = String.valueOf(accelerometerValues[2]);
            gShowInfo("x:" + x + "\ny:" + y  + "\nz:" + z);

//            float temp = accelerometerValues[1];
//            accelerometerValues[1] = accelerometerValues[2];
//            accelerometerValues[2] = temp;
        }

        if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){ // m sensor
            //存入矩陣
            magneticFieldValues = lowPass(event.values.clone(), magneticFieldValues);
            x = String.valueOf(magneticFieldValues[0]);
            y = String.valueOf(magneticFieldValues[1]);
            z = String.valueOf(magneticFieldValues[2]);
            mShowInfo("x:" + x + "\ny:" + y  + "\nz:" + z);

//            float temp = magneticFieldValues[1];
//            magneticFieldValues[1] = magneticFieldValues[2];
//            magneticFieldValues[2] = temp;
        }

        calculateOrientation();
    }

    private  void calculateOrientation() {
        float[] values = new float[3];
        float[] inR = new float[9];
        float[] outR = new float[9];

        SensorManager.getRotationMatrix(inR, null, accelerometerValues, magneticFieldValues);
        //SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
        SensorManager.getOrientation(inR, values);

        float azimuth = (float)(((values[0]*180)/Math.PI));
        float pitch = (float)(((values[1]*180)/Math.PI));
        float roll = (float)(((values[2]*180)/Math.PI));

        orienShowInfo("azimuth方位角:" + azimuth + "\npitch傾斜角:" + pitch  + "\nroll旋轉角:" + roll);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;

        for (int i=0; i<input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
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
