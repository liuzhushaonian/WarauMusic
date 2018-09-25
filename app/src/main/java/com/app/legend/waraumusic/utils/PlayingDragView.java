package com.app.legend.waraumusic.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import com.app.legend.waraumusic.R;

public class PlayingDragView extends FrameLayout {


    private ViewDragHelper viewDragHelper;
    private View dragView;
    private int screenHeight;
    private int playBarHeight;
    private boolean lock = false;
    private int slideHeight=0;
    private PositionInfo info;
    private DrawerLayout drawerLayout;
    private boolean canScroll=true;

    public static final String SCROLL="scroll_the_view";

    public void setDrawerLayout(DrawerLayout drawerLayout) {
        this.drawerLayout = drawerLayout;
    }

    public PlayingDragView(@NonNull Context context) {
        super(context);

        initValues();
        viewDragHelper = ViewDragHelper.create(this, 1.0f, callback);

        registerReceiver();

    }

    public PlayingDragView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initValues();
        viewDragHelper = ViewDragHelper.create(this, 1.0f, callback);
        registerReceiver();
    }

    public PlayingDragView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initValues();
        viewDragHelper = ViewDragHelper.create(this, 1.0f, callback);
        registerReceiver();
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (canScroll) {//允许滑动

            return viewDragHelper.shouldInterceptTouchEvent(ev);
        }

        //不允许滑动
        return super.onInterceptTouchEvent(ev);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                if (!lock){//并未被锁住，则不消费事件

                    return false;
                }

                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:


                break;

        }

        viewDragHelper.processTouchEvent(event);


        return true;
    }

    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {

            return child == dragView;

        }


        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
            return getMeasuredHeight() - child.getMeasuredHeight();
        }

        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
            return getMeasuredWidth() - child.getMeasuredWidth();
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            return 0;
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            final int topBound = getPaddingTop();
            final int bottomBound = getHeight() - dragView.getHeight();

            slideHeight = Math.min(Math.max(top, topBound), bottomBound);//计算已滑动高度

            if (top > screenHeight) {
                top = screenHeight;
            } else if (top < 0) {
                top = 0;
            }
            return top;


        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {

            if (yvel>500){//只看速度向下

                viewDragHelper.settleCapturedViewAt(0,screenHeight);

                lock=false;
                canScroll=true;



            }else if (yvel<-500){//只看速度向上



                viewDragHelper.settleCapturedViewAt(0,0);
                lock=true;




            }else if (Math.abs(yvel)<=500){//速度不足

                if (slideHeight>screenHeight/2){//向下

                    viewDragHelper.settleCapturedViewAt(0,screenHeight);
                    lock=false;
                    canScroll=true;

                }else {//向上

                    viewDragHelper.settleCapturedViewAt(0,0);
                    lock=true;


                }


            }

            lockDraw(lock);

            invalidate();

        }

        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);

            if (info!=null){

                info.setTop((int) changedView.getY());

            }

        }
    };

    public void setDragView(View dragView) {
        this.dragView = dragView;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        this.dragView = findViewById(R.id.playing_contain);


    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (dragView != null) {

            dragView.layout(0,info.getTop(),getMeasuredWidth(),getMeasuredHeight());

        }

    }


    /**
     * 初始化各项数值
     */
    private void initValues() {

        screenHeight = getResources().getDisplayMetrics().heightPixels;
        playBarHeight = getResources().getDimensionPixelSize(R.dimen.bottom_play_bar);
        info=new PositionInfo();
        info.setTop(screenHeight);

    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (viewDragHelper.continueSettling(true)){

            invalidate();

        }
    }

    /**
     * 提供外部自动向上滑动
     */
    public void startScrollToTop(){

        viewDragHelper.smoothSlideViewTo(dragView,0,0);//向上滑动到顶部
        invalidate();
        lock=true;//切换锁住，捕捉事件
        lockDraw(true);

    }

    /**
     * 提供外部自动向下滑动到底部
     * 例如，返回键
     */
    public void startScrollToBottom(){

        viewDragHelper.smoothSlideViewTo(dragView,0,screenHeight);//向上滑动到顶部
        invalidate();
        lock=false;//切换锁住，捕捉事件

        lockDraw(false);

    }

    public boolean isLock() {
        return lock;
    }

    private void lockDraw(boolean lock){

        if (this.drawerLayout==null){
            return;
        }

        if (lock){

            this.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        }else {

            this.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }

    }

    class PositionInfo{


        int top;


        PositionInfo() {
        }

        public PositionInfo(int top) {
            this.top = top;
        }

        public int getTop() {
            return top;
        }

        public void setTop(int top) {
            this.top = top;
        }
    }

    class ScrollReceiver extends BroadcastReceiver{


        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent==null){
                return;
            }

            String action=intent.getAction();

            if (action==null){
                return;
            }

            switch (action){

                case SCROLL:


                    canScroll=intent.getBooleanExtra(Conf.SCROLL,true);


                    break;


            }


        }
    }

    private void registerReceiver(){

        ScrollReceiver receiver=new ScrollReceiver();

        IntentFilter filter=new IntentFilter(SCROLL);

        WarauApp.getContext().registerReceiver(receiver,filter);

    }

}
