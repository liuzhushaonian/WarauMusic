package com.app.legend.waraumusic.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.app.legend.waraumusic.R;

/**
 *
 * Created by liuzh on 2017/2/23.
 */

public class RoundProgressBar extends View {

    private Paint paint;

    private int roundColor;

    private int roundProgressColor;
    private float roundWidth;
    private int max;
    private int progress=0;
    private int centre;
    private int radius;
    private RectF oval;


    public RoundProgressBar(Context context) {
        super(context);
//        System.out.println("构造方法1");
    }

    public RoundProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
//        System.out.println("构造方法2");
        paint=new Paint();

        TypedArray typedArray=context.obtainStyledAttributes(attrs, R.styleable.RoundProgressBar);
        roundColor=typedArray.getColor(R.styleable.RoundProgressBar_roundColor, Color.GRAY);
        roundProgressColor=typedArray.getColor(R.styleable.RoundProgressBar_roundProgressColor, Color.WHITE);
        roundWidth=typedArray.getDimension(R.styleable.RoundProgressBar_roundWidth,5);
        max=typedArray.getInteger(R.styleable.RoundProgressBar_max,100);

        typedArray.recycle();



    }

    public RoundProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        System.out.println("构造方法3");


    }

    public RoundProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        centre=getWidth()/2;
        radius= (int) (centre-roundWidth/2);


        paint.setColor(roundColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(roundWidth);
        paint.setAntiAlias(true);
        canvas.drawCircle(centre,centre,radius,paint);


        paint.setStrokeWidth(roundWidth);
        paint.setColor(roundProgressColor);
        oval=new RectF(centre-radius,centre-radius,centre+radius,centre+radius);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(oval,-90,360*progress/max,false,paint);
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;

        postInvalidate();
    }

    public void setRoundProgressColor(int roundProgressColor) {
        this.roundProgressColor = roundProgressColor;
        postInvalidate();
    }
}
