package com.example.ar_slithers;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.TextView;

public class DrawCircle extends View {

    private float x = getWidth(),y = getWidth();
    private int r = 120;
    private Sensors sensors;
    private Thread thread;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public DrawCircle(Context context, TextView info, SensorManager sensor) {
        super(context);
        mPaint.setColor(Color.RED);
        mPaint.setTextSize(30);
        sensors = new Sensors(getWidth(), getHeight(), info, sensor);
        thread = new Thread(SlowDown);
        thread.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.drawText(x+" "+y, 200, 500, mPaint);
        canvas.drawCircle(x, y, r, mPaint);
        canvas.drawCircle(x-75, y, r, mPaint);
        canvas.drawCircle(x+75, y, r, mPaint);
        invalidate();
    }

    private Runnable SlowDown = new Runnable() {
        @Override
        public void run() {
            while (true) {
                float newX = sensors.x;
                float newY = sensors.y;
                try {
                    for (int i=0; i<5; i++) {
                        x += (newX-x)*(i+1)/5;
                        y += (newY-y)*(i+1)/5;
                        Thread.sleep(20);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}