package com.app.legend.waraumusic.utils;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.util.Log;

import com.app.legend.waraumusic.interfaces.TranslucentListener;


/**
 *
 * Created by liuzh on 2017/2/9.
 */

public class MyNestedScrollView extends NestedScrollView {

    TranslucentListener translucentListener;
    int color=-100;
    int height=-1;


    public MyNestedScrollView(Context context) {
        super(context);
//        System.out.println("这里是构造方法1");
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (translucentListener!=null){

            int dis=height/255;

            int speed=getScrollY()/dis;
            if (speed>255){
                speed=255;
            }

            if (speed<16){
                speed=16;
            }

            translucentListener.onTranslucent(speed);


        }


    }


    public MyNestedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public MyNestedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    public void setTranslucentListener(TranslucentListener translucentListener,int height) {
        this.translucentListener = translucentListener;
        this.height=height;
    }

    private String getColor(String hex){
        return "#"+hex;
    }



}
