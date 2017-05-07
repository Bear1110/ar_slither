package com.example.ar_slithers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.ar_slithers.Draw.Sensors;

import java.text.DecimalFormat;
import java.util.ArrayList;
/**
 * Created by Bear on 2016/12/12.
 */
/**
 * Created by Bear on 2016/11/30.
 */

public class PaintBoard extends View {
    public static double[] target =new double[2];
    public static item[] item={};
    public static ArrayList<double[]> other = new ArrayList<double[]>();
    public PaintBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float radians = Sensors.radians;
        super.onDraw(canvas);

        int[] center = {getWidth()/2,getHeight()/2};
        DecimalFormat df = new DecimalFormat("#.####");

        //畫自己的點
        Paint paint = new Paint();
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.YELLOW);//畫面向的方向
        canvas.drawCircle((float) (20*Math.cos(radians)+center[0]), (float) (20*Math.sin(radians)+center[1]), 5, paint);
        paint.setColor(Color.BLUE);
        canvas.drawCircle(center[0], center[1], 5, paint);
        paint.setTextSize(40);
        canvas.drawText("("+df.format(target[0])+","+df.format(target[1])+")",center[0],center[1]+40,paint);
        canvas.drawText("N",center[0],40,paint);
        //畫其他人的點

        for(int i = 0 ; i < other.size() ; i ++){
            float x = (float) (other.get(i)[0]-target[0]);
            float y = (float) -(other.get(i)[1]-target[1]);
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            canvas.drawCircle(center[0]+x, center[1]+y, 5, paint);
            canvas.drawText("("+df.format(other.get(i)[0])+","+df.format(other.get(i)[1])+")",center[0]+x,center[1]+y+30,paint);
//            canvas.drawCircle(center[0]+x, center[1]+y, 5, paint);
        }
        //畫物體
        for(int i = 0 ; i < item.length ; i ++){
            float x = (float) (item[i].map[0]-target[0]);
            float y = (float) -(item[i].map[1]-target[1]);
            paint.setColor(Color.GREEN);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            canvas.drawCircle(center[0]+x, center[1]+y, 5, paint);
        }
        invalidate();
    }
}
