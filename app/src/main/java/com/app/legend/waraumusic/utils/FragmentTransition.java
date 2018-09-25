package com.app.legend.waraumusic.utils;

import android.content.Context;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.TransitionSet;

import android.util.AttributeSet;

public class FragmentTransition extends TransitionSet {

    public FragmentTransition() {
        init();
    }

    /**
     * This constructor allows us to use this transition in XML
     */
    public FragmentTransition(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrdering(ORDERING_TOGETHER);
        addTransition(new ChangeBounds()).
                addTransition(new ChangeTransform()).
                addTransition(new ChangeImageTransform());
    }

}
