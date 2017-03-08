package com.example.ar_slithers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
/**
 * Created by Bear on 2016/12/12.
 */
/**
 * Created by Bear on 2016/11/30.
 */

public class PaintBoard extends View {
    public static double[] target =new double[2];
    public static ArrayList<double[]> other = new ArrayList<double[]>();

    public PaintBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int[] center = {getWidth()/2,getHeight()/2};

        //畫自己的點
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        canvas.drawCircle(center[0], center[1], 5, paint);
        paint.setTextSize(40);
        canvas.drawText("("+target[0]+","+target[1]+")",center[0],center[1]+40,paint);
        canvas.drawText("N",center[0],40,paint);
        //畫其他人的點
        for(int i = 0 ; i < other.size() ; i ++){
            float x = (float) (other.get(i)[0]-target[0]);
            float y = (float) -(other.get(i)[1]-target[1]);
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            canvas.drawCircle(center[0]+x, center[1]+y, 5, paint);
            canvas.drawText("("+other.get(i)[0]+","+other.get(i)[1]+")",center[0]+x,center[1]+y+30,paint);
            canvas.drawCircle(center[0]+x, center[1]+y, 5, paint);
        }
        invalidate();
    }
}
