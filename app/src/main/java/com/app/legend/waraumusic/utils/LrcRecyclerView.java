package com.app.legend.waraumusic.utils;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.app.legend.waraumusic.fragment.AlbumPagerFragment;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * Created by legend on 2018/3/14.
 */

public class LrcRecyclerView extends RecyclerView {

    private boolean auto=false;
    private Timer timer;
    private int time=0;
    private AlbumPagerFragment fragment;

    public void setFragment(AlbumPagerFragment fragment) {
        this.fragment = fragment;
    }

    public LrcRecyclerView(Context context) {
        super(context);
    }

    public LrcRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LrcRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        switch (e.getAction()){
            case MotionEvent.ACTION_DOWN:

                timer(0);

            case MotionEvent.ACTION_MOVE:

                timer(0);
            case MotionEvent.ACTION_UP:
                timer(1);
        }

        return super.onTouchEvent(e);
    }

    private void timer(int state){

        if (state==1){

            if (this.timer!=null){
                this.timer.cancel();
            }

            this.timer=new Timer();

            time=0;

            TimerTask timerTask=new TimerTask() {
                @Override
                public void run() {
                    time++;

                    if (time>=5){

                        time=0;
                        timer.cancel();
                        if (fragment!=null){

                            fragment.autoScroll=true;
                            fragment.cancelTheCenter();//取消中间高亮
                        }

                    }
                }
            };

            timer.schedule(timerTask,0,1000);
        }else {//其他情况重置time

            time=0;
            if (fragment!=null){

                fragment.autoScroll=false;
            }

        }

    }


}
