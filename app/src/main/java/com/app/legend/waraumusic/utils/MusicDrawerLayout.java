package com.app.legend.waraumusic.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.app.legend.waraumusic.R;

public class MusicDrawerLayout extends DrawerLayout {

    private ViewDragHelper viewDragHelper;
    private View dragView;

    public MusicDrawerLayout(@NonNull Context context) {
        super(context);
        viewDragHelper=ViewDragHelper.create(this,1.0f,callback);
    }

    public MusicDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        viewDragHelper=ViewDragHelper.create(this,1.0f,callback);
    }

    public MusicDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        viewDragHelper=ViewDragHelper.create(this,1.0f,callback);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {


        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        viewDragHelper.processTouchEvent(event);

        return true;
    }

    private ViewDragHelper.Callback callback=new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {


            return child==dragView;
        }



        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
//            final int topBound =getPaddingTop();
//            final int bottomBound = getHeight()- dragView.getHeight();
//            final int newTop =Math.min(Math.max(top, topBound), bottomBound);
//            return newTop;

            return top;
        }

        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
            return getMeasuredHeight()-child.getMeasuredHeight();
        }

        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
            return getMeasuredWidth()-child.getMeasuredWidth();
        }

        @Override
        public void onEdgeTouched(int edgeFlags, int pointerId) {
            super.onEdgeTouched(edgeFlags, pointerId);



        }

        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);


        }


    };

    public void setDragView(View dragView) {
        this.dragView = dragView;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        this.dragView=findViewById(R.id.playing_contain);

    }

}
