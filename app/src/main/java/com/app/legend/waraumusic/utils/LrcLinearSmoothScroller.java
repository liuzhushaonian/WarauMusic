package com.app.legend.waraumusic.utils;

import android.content.Context;
import android.support.v7.widget.LinearSmoothScroller;
import android.util.DisplayMetrics;

public class LrcLinearSmoothScroller extends LinearSmoothScroller {

    public LrcLinearSmoothScroller(Context context) {
        super(context);
    }

    @Override
    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {

        return 1.0f;

//            return 1.0f;
//        return super.calculateSpeedPerPixel(displayMetrics);
    }


}
