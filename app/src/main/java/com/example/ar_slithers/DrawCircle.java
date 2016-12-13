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
import android.icu.math.BigDecimal;
import android.view.View;
import android.widget.TextView;

public class DrawCircle extends View implements SensorEventListener {
    private float x = 400,y = 400;
    private int r = 25;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private SensorManager gsensorManager;
    private Sensor gsensor;
    private TextView info;

    public DrawCircle(Context context, TextView info, SensorManager sensor) {
        super(context);
        mPaint.setColor(Color.RED);
        //抓g sensor的資料
        gsensorManager = sensor;
        gsensor = gsensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gsensorManager.registerListener(this, gsensor, SensorManager.SENSOR_DELAY_GAME);

        this.info = info;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(x, y, r, mPaint);
        invalidate();
    }

    @SuppressLint("NewApi")
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            BigDecimal xvalue= new BigDecimal(event.values[0]);
            xvalue=xvalue.setScale(2, BigDecimal.ROUND_HALF_UP);
            x = (float) (xvalue.doubleValue())*180 + 400;

            BigDecimal yvalue= new BigDecimal(event.values[2]);
            yvalue=yvalue.setScale(2, BigDecimal.ROUND_HALF_UP);
            y = (-event.values[2])*180 + 400;

            info.setText("x: "+event.values[0]+" y: "+event.values[1]+" z: "+event.values[2]);
            //z += arg0.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
