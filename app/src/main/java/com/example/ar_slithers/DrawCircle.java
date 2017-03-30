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

    private static final int RATE = 100;
    private static final int DELAY = 300/RATE; // delay longer => move slower

    private int colorNo = 0;
    private Sensors sensors;
    private static Thread threadSlow;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public DrawCircle(Context context, TextView info, SensorManager sensor) {
        super(context);
        mPaint.setTextSize(40);
        sensors = new Sensors(info, sensor);
        //otherSnakes.add(new SnakeInfo(colorNo));
        colorNo++;
        if (threadSlow == null) {
            threadSlow = new Thread(SlowDown);
            threadSlow.start();
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(Color.YELLOW);

        if (!otherSnakes.isEmpty()) {
            canvas.drawText(otherSnakes.get(0).X+"", 100, getHeight()-80, mPaint);
            canvas.drawText(otherSnakes.get(0).Y+" "+PaintBoard.other.size(), 100, getHeight()-30, mPaint);
        }

        for (SnakeInfo snake: otherSnakes) {
            mPaint.setColor(color[snake.colorNo]);
            canvas.drawCircle(snake.X, snake.Y, snake.getRadius(), mPaint);
            canvas.drawCircle(snake.X-75, snake.Y, snake.getRadius(), mPaint);
            canvas.drawCircle(snake.X+75, snake.Y, snake.getRadius(), mPaint);
            canvas.drawText(snake.getDistance()+"", snake.X-75, snake.Y-snake.getRadius(), mPaint);
        }

        invalidate();
    }

    private Runnable SlowDown = new Runnable() {
        @Override
        public void run() {
            while (true) {
                sensors.setScreenSize(getWidth(), getHeight());
                // If add new player, otherSnakes must refresh.
                if (PaintBoard.other.size() > otherSnakes.size()) {
                    otherSnakes.add(new SnakeInfo(colorNo));
                    colorNo++;
                    if (colorNo > 3) colorNo = 0;
                }

                // Move smoother.
                try {
                    for (int i=0; i<RATE; i++) {
                        for (SnakeInfo snake: otherSnakes) {
                            snake.X += (snake.sensorX-snake.X)*(i+1)/RATE;
                        }
                        Thread.sleep(DELAY);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

}