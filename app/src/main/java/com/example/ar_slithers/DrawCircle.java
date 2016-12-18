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
import android.view.View;
import android.widget.TextView;

//import java.lang.Math;

@SuppressLint("NewApi")
public class DrawCircle extends View implements SensorEventListener {

    private float x = 400,y = 400;
    private int r = 150;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private SensorManager sensorManager;
    private Sensor gsensor, msensor;
    private TextView info;

    float[] accelerometerValues = new float[3];
    float[] magneticFieldValues = new float[3];

    public DrawCircle(Context context, TextView info, SensorManager sensor) {
        super(context);
        mPaint.setColor(Color.RED);
        //��g sensor�����
        sensorManager = sensor;
        gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, gsensor, SensorManager.SENSOR_DELAY_GAME);

        //抓 magnet sensor 的資料
        msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, msensor, SensorManager.SENSOR_DELAY_GAME);

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





        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            BigDecimal xvalue= new BigDecimal(event.values[0]);
            //xvalue=xvalue.setScale(1, BigDecimal.ROUND_HALF_UP);
            x = (float) (xvalue.doubleValue())*140 + 400;
            BigDecimal yvalue= new BigDecimal(event.values[2]);
            //yvalue=yvalue.setScale(1, BigDecimal.ROUND_HALF_UP);
            y = (-event.values[2])*140 + 400;

            accelerometerValues = event.values;

            info.setText("x: "+event.values[0]+" y: "+event.values[1]+" z: "+event.values[2]);
            //z += arg0.values[2];
        }

        String x, y, z;

        if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){ // m sensor
            x = df.format(event.values[0]);
            y = df.format(event.values[1]);
            z = df.format(event.values[2]);

            //存入矩陣
            magneticFieldValues = event.values;
        }

        calculateOrientation();

    }

    private  void calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];

        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);

        values[0]=(float)Math.toDegrees(values[0]);
        //BigDecimal value= new BigDecimal(values[0]);
        //x = (float) Math.sin(value.doubleValue()) * 150 + 400;
        x = -values[0] * 10 + 400;

    }
}