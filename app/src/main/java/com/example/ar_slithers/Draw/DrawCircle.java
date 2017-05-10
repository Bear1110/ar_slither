package com.example.ar_slithers.Draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.TextView;

import com.example.ar_slithers.PaintBoard;

import java.util.ArrayList;

public class DrawCircle extends View {

    public static ArrayList<SnakeInfo> otherSnakes = new ArrayList<>();
    private static int[] color = {Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE};

    private static final int RATE = 100;
    private static final int DELAY = 400/RATE; // delay longer => move slower
    private static float density;

    private Sensors sensors;
    private static Thread threadSlow;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public DrawCircle(Context context, float density, TextView info, SensorManager sensor) {
        super(context);
        this.density = density;
        mPaint.setTextSize(40);
        sensors = new Sensors(info, sensor);
//        double[][] b = {{120.0, 25.0}};
//        DrawCircle.otherSnakes.add(new SnakeInfo(b, 0));
        if (threadSlow == null) {
            threadSlow = new Thread(SlowDown);
            threadSlow.start();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(Color.YELLOW);

        // just for test
        if (!otherSnakes.isEmpty()) {
            canvas.drawText(otherSnakes.get(0).getBodyPos()[0][0]+" ", 100, getHeight()-330, mPaint);
            canvas.drawText(otherSnakes.get(0).getBodyPos()[1][0]+" ", 100, getHeight()-280, mPaint);
            canvas.drawText(otherSnakes.get(0).getBodyPos()[2][0]+" ", 100, getHeight()-230, mPaint);
            canvas.drawText(otherSnakes.get(0).getBodyPos()[3][0]+" ", 100, getHeight()-180, mPaint);
            canvas.drawText(otherSnakes.get(0).getBodyPos()[4][0]+" ", 100, getHeight()-130, mPaint);
            canvas.drawText(otherSnakes.get(0).getBodyPos()[5][0]+" ", 100, getHeight()-80, mPaint);
            canvas.drawText(otherSnakes.get(0).getBodyPos()[6][0]+" ", 100, getHeight()-30, mPaint);
        }

        for (SnakeInfo snake: otherSnakes) {
            mPaint.setColor(Color.BLACK);
            //canvas.drawCircle(snake.drawBody.get(0).X + snake.drawBody.get(0).getRadius() * density, snake.drawBody.get(0).Y + snake.drawBody.get(0).getRadius() * density, snake.drawBody.get(0).getRadius() * (float) 0.2 * density, mPaint);
            canvas.drawCircle((float)(snake.drawBody.get(0).X+snake.drawBody.get(0).getRadius()*0.2*density), (float)(snake.drawBody.get(0).Y+snake.drawBody.get(0).getRadius()*0.2*density), (float)(snake.drawBody.get(0).getRadius()*0.2*density), mPaint);
            canvas.drawCircle((float)(snake.drawBody.get(0).X+snake.drawBody.get(0).getRadius()*0.6*density), (float)(snake.drawBody.get(0).Y+snake.drawBody.get(0).getRadius()*0.8*density), (float)(snake.drawBody.get(0).getRadius()*0.1*density), mPaint);
            canvas.drawCircle((float)(snake.drawBody.get(0).X+snake.drawBody.get(0).getRadius()*0.6*density), (float)(snake.drawBody.get(0).Y+snake.drawBody.get(0).getRadius()*0.4*density), (float)(snake.drawBody.get(0).getRadius()*0.25*density), mPaint);
            canvas.drawCircle((float)(snake.drawBody.get(0).X+snake.drawBody.get(0).getRadius()*0.25*density), (float)(snake.drawBody.get(0).Y+snake.drawBody.get(0).getRadius()*0.6*density), (float)(snake.drawBody.get(0).getRadius()*0.15*density), mPaint);
            mPaint.setColor(color[snake.colorNo]);
            for (BodyInfo aBody: snake.drawBody) {
                canvas.drawCircle(aBody.X, aBody.Y, aBody.getRadius() * density, mPaint);
                canvas.drawText((int)aBody.getRadius()+"", aBody.X-aBody.getRadius()*2, aBody.Y-aBody.getRadius()*2, mPaint);
            }
//            mPaint.setColor(color[snake.colorNo]);
//            canvas.drawCircle(snake.X, snake.Y, snake.getRadius(), mPaint);
//            canvas.drawCircle(snake.X-snake.getRadius()/2, snake.Y, snake.getRadius(), mPaint);
//            canvas.drawCircle(snake.X+snake.getRadius()/2, snake.Y, snake.getRadius(), mPaint);
//            canvas.drawText(snake.getDistance()+" "+snake.getDensity(), snake.X-snake.getRadius()/2, snake.Y-snake.getRadius(), mPaint);
        }

        invalidate();
    }

    private Runnable SlowDown = new Runnable() {
        @Override
        public void run() {
            while (true) {
                sensors.setScreenSize(getWidth(), getHeight());

                // Move smoother.
                try {
                    for (int i=0; i<RATE; i++) {
                        for (SnakeInfo snake: otherSnakes) {
                            for (BodyInfo aBody: snake.drawBody) {
                                aBody.X += (aBody.sensorX-aBody.X)*(i+1)/RATE;
                            }
                        }
                        Thread.sleep(DELAY);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void stop() {
        threadSlow = null;
    }

}