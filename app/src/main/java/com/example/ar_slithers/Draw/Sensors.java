package com.example.ar_slithers.Draw;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import com.example.ar_slithers.PaintBoard;

@SuppressLint("NewApi")
public class Sensors implements SensorEventListener {

    private static final float ALPHA = 0.25f;

    private float width, height;
    private float degree = 0, adjustDegree = 0;

    private SensorManager sensorManager;
    private Sensor gsensor, msensor;
    private TextView info;
    public static float radians = 0;

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

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerValues = lowPass(event.values.clone(), accelerometerValues);

            for (SnakeInfo snake: DrawCircle.otherSnakes) {
                for (BodyInfo aBody: snake.drawBody) {
                    aBody.Y = -accelerometerValues[2]*130 + height;
                }
//                snake.Y = -accelerometerValues[2]*130 + height;
            }
        }

        if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
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

        // apply "Low-Pass Filter" method
        // adjustDegree: azimuth change when phone up-down, so use pitch to adjust
        degree = degree + 0.25f * (azimuth - degree);
        adjustDegree = adjustDegree + 0.25f * (pitch - adjustDegree);

        for (SnakeInfo snake: DrawCircle.otherSnakes) {
            for (BodyInfo aBody: snake.drawBody) {
                float angle = aBody.degree - degree;
                double radian = Math.toRadians(angle);
                aBody.sensorX = (float) Math.sin(radian/2) * 1500 * 2 + (-adjustDegree)*2.5f + width/2;
            }
//            float angle = snake.degree - degree;
//            double radian = Math.toRadians(angle);
//            snake.sensorX = (float) Math.sin(radian/2) * 1500 * 2 + (-adjustDegree)*2.5f + width/2;
        }

        if (!DrawCircle.otherSnakes.isEmpty()) {
            info.setText(DrawCircle.otherSnakes.get(0).drawBody.get(0).degree + " " + azimuth);
        }
        //借我算 面相角度
        radians = (float) (values[0]-1.5);
    }

    // get other players' degree comparison with user, and calculate the distance
    private void getOtherDegree() {
        double xDistance = 30, yDistance = 40;
        for (SnakeInfo snake: DrawCircle.otherSnakes) {
            for (int i=0; i<snake.drawBody.size(); i++) {

                xDistance = snake.getBodyPos()[i][0] - PaintBoard.target[0];
                yDistance = snake.getBodyPos()[i][1] - PaintBoard.target[1];

                float otherDegree = (float) Math.toDegrees( Math.atan(yDistance/xDistance) );
                if (xDistance < 0) { otherDegree = - (90 + otherDegree); }
                else { otherDegree = 90 - otherDegree; }
                snake.drawBody.get(i).degree = otherDegree;
                snake.drawBody.get(i).setDistance(xDistance, yDistance);
            }

//            if (!PaintBoard.other.isEmpty()) {
//                xDistance = PaintBoard.other.get(i)[0] - PaintBoard.target[0];
//                yDistance = PaintBoard.other.get(i)[1] - PaintBoard.target[1];
//            }
//
//            float otherDegree = (float) Math.toDegrees( Math.atan(yDistance/xDistance) );
//            if (xDistance < 0) { otherDegree = - (90 + otherDegree); }
//            else { otherDegree = 90 - otherDegree; }
//            DrawCircle.otherSnakes.get(i).degree = otherDegree;
//            DrawCircle.otherSnakes.get(i).setDistance(xDistance, yDistance);
        }
    }

    //讓感測器取得的資料以緩慢速度趨向 apply "Low-Pass Filter" method
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
