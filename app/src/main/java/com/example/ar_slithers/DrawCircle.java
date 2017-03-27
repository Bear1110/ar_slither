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

    public static ArrayList<SnakeInfo> otherSnakes = new ArrayList<>();
    private static int[] color = {Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE};

    private int r = 120, colorNo = 0;
    private Sensors sensors;
    private Thread thread;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public DrawCircle(Context context, TextView info, SensorManager sensor) {
        super(context);
        mPaint.setTextSize(30);
        sensors = new Sensors(info, sensor);
        otherSnakes.add(new SnakeInfo(colorNo));
        colorNo++;
        thread = new Thread(SlowDown);
        thread.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(Color.YELLOW);
        canvas.drawText(otherSnakes.get(0).x+" "+otherSnakes.get(0).sensorX, 200, 800, mPaint);

        for (SnakeInfo snake: otherSnakes) {
            mPaint.setColor(color[snake.colorNo]);
            canvas.drawCircle(snake.x, snake.y, r, mPaint);
            canvas.drawCircle(snake.x-75, snake.y, r, mPaint);
            canvas.drawCircle(snake.x+75, snake.y, r, mPaint);
        }

        invalidate();
    }

    private Runnable SlowDown = new Runnable() {
        @Override
        public void run() {
            while (true) {
                sensors.setScreenSize(getWidth(), getHeight());
                if (PaintBoard.other.size() > otherSnakes.size()) {
                    otherSnakes.add(new SnakeInfo(colorNo));
                    colorNo++;
                    if (colorNo > 3) colorNo = 0;
                }

                try {
                    for (int i=0; i<5; i++) {
                        for (SnakeInfo snake: otherSnakes) {
                            snake.x += (snake.sensorX-snake.x)*(i+1)/5;
                            snake.y += (snake.sensorY-snake.y)*(i+1)/5;
                        }
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}