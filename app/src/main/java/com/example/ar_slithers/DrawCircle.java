package com.example.ar_slithers;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.math.BigDecimal;
import android.icu.text.DecimalFormat;
import android.os.Build;
import android.speech.tts.Voice;
import android.view.View;
import android.widget.TextView;

@SuppressLint("NewApi")
public class DrawCircle extends View implements SensorEventListener {

    private float x = getWidth(),y = getWidth();
    private int r = 120;
    private static float otherDegree;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private SensorManager sensorManager;
    private Sensor gsensor, msensor;
    private TextView info;

    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];

    public DrawCircle(Context context, TextView info, SensorManager sensor) {
        super(context);
        mPaint.setColor(Color.RED);
        //抓 g sensor 的資料
        sensorManager = sensor;
        gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, gsensor, SensorManager.SENSOR_DELAY_UI);

        //抓 magnet sensor 的資料
        msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, msensor, 300);

        this.info = info;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(x, y, r, mPaint);
        canvas.drawCircle(x-75, y, r, mPaint);
        canvas.drawCircle(x+75, y, r, mPaint);
        invalidate();
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        DecimalFormat df = new DecimalFormat("#.#");

        getOtherDegree();

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            float xFormer = Float.valueOf(df.format(event.values[0]));
            float xLatter = event.values[0] - xFormer;
            x = xFormer*140 + xLatter/10 + 400;

            float yFormer = Float.valueOf(df.format(event.values[2]));
            float yLatter = event.values[2] - yFormer;
            y = (-yFormer)*140 - yLatter/10 + getHeight()/2;

            accelerometerValues = event.values;

            // swap y and z
            float temp = accelerometerValues[1];
            accelerometerValues[1] = accelerometerValues[2];
            accelerometerValues[2] = temp;

            //info.setText("x: "+x+" y: "+y/*+" z: "+event.values[2]*/);
        }

        String xMagnetic, yMagnetic, zMagnetic;

        if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){ // m sensor
            xMagnetic = df.format(event.values[0]);
            yMagnetic = df.format(event.values[1]);
            zMagnetic = df.format(event.values[2]);

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

        double degree = Math.toDegrees(values[0]);
        degree -= 90;
        if (degree < 0) degree += 360;

        double angle = otherDegree - degree;
        double radian = Math.toRadians(angle);
        x = - (float) Math.sin(radian/2) * 400 * 2 + getWidth()/2;
        info.setText(otherDegree + " " + degree + "");
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
}