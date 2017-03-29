package com.example.ar_slithers;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.DecimalFormat;
import android.widget.TextView;

import java.util.ArrayList;

@SuppressLint("NewApi")
public class Sensors implements SensorEventListener {

    protected static ArrayList<Float> degreeSet = new ArrayList<>();

    private float width, height;
    private float degree, preDegree = 0;

    private SensorManager sensorManager;
    private Sensor gsensor, msensor;
    private TextView info;

    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];

    public Sensors(TextView info, SensorManager sensor) {
        //抓 g sensor 的資料
        sensorManager = sensor;
        gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, gsensor, SensorManager.SENSOR_DELAY_FASTEST);

        //抓 magnet sensor 的資料
        msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, msensor, SensorManager.SENSOR_DELAY_FASTEST);

        this.info = info;
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        DecimalFormat df = new DecimalFormat("#.#");

        getOtherDegree();

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            float xFormer = Float.valueOf(df.format(event.values[0]));
            float xLatter = event.values[0] - xFormer;

            float yFormer = Float.valueOf(df.format(event.values[2]));
            float yLatter = event.values[2] - yFormer;

            for (SnakeInfo snake: DrawCircle.otherSnakes) {
                //snake.sensorX = xFormer*140 + xLatter/10 + width/2;
                snake.sensorY = (-yFormer)*140 - yLatter/10 + height;
            }

            // swap y and z
            accelerometerValues = event.values;
            float temp = accelerometerValues[1];
            accelerometerValues[1] = accelerometerValues[2];
            accelerometerValues[2] = temp;
        }

        if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){

            //存入矩陣
            magneticFieldValues = event.values;
            float temp = magneticFieldValues[1];
            magneticFieldValues[1] = magneticFieldValues[2];
            magneticFieldValues[2] = temp;
        }

        calculateOrientation();

    }

    private void calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];

        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);

        degree = (float) Math.toDegrees(values[0]);
        degree -= 90;
        if (degree < -180) { degree += 360; }
        if (Math.abs(preDegree-degree) < 2) { degree = preDegree; }
        else { preDegree = degree; }

        degreeSet.add(degree);



        if (!DrawCircle.otherSnakes.isEmpty()) {
            info.setText(DrawCircle.otherSnakes.get(0).degree + " " + degree);
        }
    }

    private void getOtherDegree() {
        double xDistance = 3, yDistance = 4;
        for (int i=0; i<DrawCircle.otherSnakes.size(); i++) {

            if (!PaintBoard.other.isEmpty()) {
                xDistance = PaintBoard.other.get(i)[0] - PaintBoard.target[0];
                yDistance = PaintBoard.other.get(i)[1] - PaintBoard.target[1];
            }

            float otherDegree = (float) Math.toDegrees( Math.atan(yDistance/xDistance) );
            if (xDistance < 0) otherDegree += 180;
            DrawCircle.otherSnakes.get(i).degree = otherDegree;
            DrawCircle.otherSnakes.get(i).setDistance(xDistance, yDistance);
        }
    }

    public void setScreenSize(float width, float height) {
        this.width = width;
        this.height = height;
    }
}
