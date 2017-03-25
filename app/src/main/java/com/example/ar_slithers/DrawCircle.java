package com.example.ar_slithers;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class DrawCircle extends View {

    private float[] initial = {getWidth(), getHeight()};
    private float[] initial2 = {getWidth(), getHeight(), 2};
    private ArrayList<float[]> snakesPos = new ArrayList<>();

    private int r = 120;
    private Sensors sensors;
    private Thread thread;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public DrawCircle(Context context, TextView info, SensorManager sensor) {
        super(context);
        mPaint.setColor(Color.RED);
        mPaint.setTextSize(30);
        sensors = new Sensors(info, sensor);
        thread = new Thread(SlowDown);
        thread.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.drawText(x+" "+y+" "+getHeight(), 200, 800, mPaint);
        for (float[] pos: snakesPos) {
            canvas.drawCircle(pos[0], pos[1], r, mPaint);
            canvas.drawCircle(pos[0]-75, pos[1], r, mPaint);
            canvas.drawCircle(pos[0]+75, pos[1], r, mPaint);
        }

        invalidate();
    }

    private Runnable SlowDown = new Runnable() {
        @Override
        public void run() {
            while (true) {
                sensors.setScreenSize(getWidth(), getHeight());
                if (PaintBoard.other.size() > snakesPos.size()) {
                    snakesPos.add(initial);
                    Sensors.otherPos.add(initial2);
                }

                try {
                    for (int i=0; i<5; i++) {

                        for (int j=0; j<snakesPos.size(); j++) {
                            float newX = Sensors.otherPos.get(j)[0];
                            float newY = Sensors.otherPos.get(j)[1];
                            snakesPos.get(j)[0] += (newX-snakesPos.get(j)[0])*(i+1)/5;
                            snakesPos.get(j)[1] += (newY-snakesPos.get(j)[1])*(i+1)/5;
                        }
                        Thread.sleep(20);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}