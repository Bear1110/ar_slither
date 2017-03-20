package com.example.ar_slithers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.DecimalFormat;
import android.view.View;
import android.widget.TextView;

@SuppressLint("NewApi")
public class Sensors implements SensorEventListener {

    public float x, y;
    private float width, height;
    private static float otherDegree;
    private float degree, preDegree = 0;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private SensorManager sensorManager;
    private Sensor gsensor, msensor;
    private TextView info;

    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];

    public Sensors(TextView info, SensorManager sensor) {
        mPaint.setColor(Color.RED);
        //抓 g sensor 的資料
        sensorManager = sensor;
        gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, gsensor, 100);

        //抓 magnet sensor 的資料
        msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, msensor, 100);

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
            x = xFormer*140 + xLatter/10 + width/2;

            float yFormer = Float.valueOf(df.format(event.values[2]));
            float yLatter = event.values[2] - yFormer;
            y = (-yFormer)*140 - yLatter/10 + height;

            accelerometerValues = event.values;

            // swap y and z
            float temp = accelerometerValues[1];
            accelerometerValues[1] = accelerometerValues[2];
            accelerometerValues[2] = temp;

            //info.setText("x: "+x+" y: "+y/*+" z: "+event.values[2]*/);
        }

        if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){ // m sensor

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
        if (degree < -180) degree += 360;
        if (Math.abs(preDegree-degree) < 2) degree = preDegree;
        else preDegree = degree;

        float angle = otherDegree - degree;
        double radian = Math.toRadians(angle);
        x = - (float) Math.sin(radian/2) * 1000 * 2 + width/2;
        info.setText(otherDegree + " " + degree + " " + width +" "+height);
    }

    private void getOtherDegree() {
        double xDistance = 3, yDistance = 4;
        if (!PaintBoard.other.isEmpty()) {
            xDistance = PaintBoard.other.get(0)[0] - PaintBoard.target[0];
            yDistance = PaintBoard.other.get(0)[1] - PaintBoard.target[1];
        }
        otherDegree = (float) Math.toDegrees( Math.atan(yDistance/xDistance) );
        if (xDistance < 0) otherDegree += 180;
    }

    public void setScreenSize(float width, float height) {
        this.width = width;
        this.height = height;
    }
}
