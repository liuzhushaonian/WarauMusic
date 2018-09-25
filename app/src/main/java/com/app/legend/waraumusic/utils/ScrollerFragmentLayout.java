package com.app.legend.waraumusic.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class ScrollerFragmentLayout extends FrameLayout{

    private float mPosY=0,mCurPosY=0;

    private boolean con=false;

    private GestureDetector gestureDetector;

    public ScrollerFragmentLayout(@NonNull Context context) {
        super(context);
    }

    public ScrollerFragmentLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

    }

    public ScrollerFragmentLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ScrollerFragmentLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public void setGestureDetector(GestureDetector gestureDetector) {
        this.gestureDetector = gestureDetector;
    }



    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (gestureDetector!=null){
            return gestureDetector.onTouchEvent(ev);
        }

        return super.onInterceptTouchEvent(ev);

    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
