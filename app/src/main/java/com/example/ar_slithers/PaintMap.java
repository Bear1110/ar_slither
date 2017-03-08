package com.example.ar_slithers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;

public class PaintMap extends View {
    public static ArrayList<double[]> other_point = new ArrayList<double[]>();

    public PaintMap(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        double[] center = {getWidth()/2,getHeight()/2};

        Paint paint = new Paint();

        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        canvas.drawCircle((float)center[0], (float)center[1], 3, paint);
        paint.setTextSize(40);

        for(int i = 0 ; i < other_point.size() ; i ++){
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            float x = (float) other_point.get(i)[0]+(float)center[0];
            float y = (float) -(other_point.get(i)[1]+(float)center[1]);
            canvas.drawCircle(x,y, 3, paint);
        }
        invalidate();

    }
}