package com.example.ar_slithers;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import java.util.ArrayList;

@SuppressLint("NewApi")
public class Sensors implements SensorEventListener {

    private static final float ALPHA = 0.25f;

    private float width, height;
    private float degree = 0, adjustDegree = 0;

    private SensorManager sensorManager;
    private Sensor gsensor, msensor;
    private TextView info;

    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];

    public Sensors(TextView info, SensorManager sensor) {
        //抓 g sensor 的資料
        sensorManager = sensor;
        gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, gsensor, SensorManager.SENSOR_DELAY_NORMAL);

        //抓 magnet sensor 的資料
        msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, msensor, SensorManager.SENSOR_DELAY_NORMAL);

        this.info = info;
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        getOtherDegree();

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
//            float xFormer = Float.valueOf(df.format(event.values[0]));
//            float xLatter = event.values[0] - xFormer;
//
//            float yFormer = Float.valueOf(df.format(event.values[2]));
//            float yLatter = event.values[2] - yFormer;

            accelerometerValues = lowPass(event.values.clone(), accelerometerValues);

            for (SnakeInfo snake: DrawCircle.otherSnakes) {
                snake.Y = -accelerometerValues[2]*130 + height*snake.getDensity()/2;
            }
        }

        if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            //存入矩陣
            magneticFieldValues = lowPass(event.values.clone(), magneticFieldValues);
        }

        calculateOrientation();

    }

    private void calculateOrientation() {
        float[] values = new float[3];
        float[] inR = new float[9];
        float[] outR = new float[9];

        SensorManager.getRotationMatrix(inR, null, accelerometerValues, magneticFieldValues);
        SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
        SensorManager.getOrientation(outR, values);

        float azimuth = (float)(((values[0]*180)/Math.PI));
        float pitch = (float)(((values[1]*180)/Math.PI));
        float roll = (float)(((values[2]*180)/Math.PI));

        degree = degree + 0.25f * (azimuth - degree);
        adjustDegree = adjustDegree + 0.25f * (pitch - adjustDegree);

        for (SnakeInfo snake: DrawCircle.otherSnakes) {
            float angle = snake.degree - degree;
            double radian = Math.toRadians(angle);
            snake.sensorX = (float) Math.sin(radian/2) * 1500 * 2 + (-adjustDegree)*0.5f + width/2;
        }

        if (!DrawCircle.otherSnakes.isEmpty()) {
            info.setText(DrawCircle.otherSnakes.get(0).degree + " " + azimuth);
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

    protected float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;

        for (int i=0; i<input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    public void setScreenSize(float width, float height) {
        this.width = width;
        this.height = height;
    }
}
