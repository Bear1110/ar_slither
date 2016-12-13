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
    public static int[] target =new int[2];
    public static ArrayList<int[]> other = new ArrayList<int[]>();

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
        //畫其他人的點
        for(int i = 0 ; i < other.size() ; i ++){
            int x = other.get(i)[0]-target[0];
            int y = other.get(i)[1]-target[1];
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            canvas.drawCircle(center[0]+x, center[1]+y, 5, paint);
        }
        invalidate();
    }
}
