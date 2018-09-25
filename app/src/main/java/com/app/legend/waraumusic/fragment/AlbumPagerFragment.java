package com.app.legend.waraumusic.fragment;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.adapter.LrcAdapter;
import com.app.legend.waraumusic.bean.Lrc;
import com.app.legend.waraumusic.interfaces.LrcItemClickListener;
import com.app.legend.waraumusic.presenter.AlbumPagerPresenter;
import com.app.legend.waraumusic.presenter.interfaces.IAlbumPagerFragment;
import com.app.legend.waraumusic.utils.Conf;
import com.app.legend.waraumusic.utils.ImageLoader;
import com.app.legend.waraumusic.utils.LrcLinearLayoutManager;
import com.app.legend.waraumusic.utils.LrcLinearSmoothScroller;
import com.app.legend.waraumusic.utils.LrcRecyclerView;
import com.app.legend.waraumusic.utils.Mp3Util;
import com.app.legend.waraumusic.utils.PlayingDragView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;
import java.util.Objects;

/**
 * 歌曲封面展示与歌词展示页面
 * A simple {@link Fragment} subclass.
 */
public class AlbumPagerFragment extends BasePresenterFragment<IAlbumPagerFragment,AlbumPagerPresenter>
        implements IAlbumPagerFragment{

    private ImageView imageView,lrc_controller;
    private MediaMetadataCompat metadataCompat;
    public boolean autoScroll = true;

    private LrcRecyclerView recyclerView;
    private LrcLinearLayoutManager linearLayoutManager;
    private LrcAdapter adapter;
    private LrcLinearSmoothScroller linearSmoothScroller;

    private LinearLayout center_view;
    private Lrc lrc;
    private ImageView play_button;
    private TextView time_text;
//    private Button expand_button;
    private SeekBar textSizeSeekBar;
    private FrameLayout lrcView;

    private TextView info;



    public AlbumPagerFragment() {
        // Required empty public constructor
    }

    @Override
    protected AlbumPagerPresenter createPresenter() {
        return new AlbumPagerPresenter(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view=inflater.inflate(R.layout.fragment_album_pager, container, false);

        getComponent(view);

        initAlbum();

        initLrcList();

        initClick();

        changeTextSize();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        autoSetColor();
    }

    @Override
    void autoSetColor() {
        if (lrc_controller!=null){
            lrc_controller.setImageTintList(ColorStateList.valueOf(getThemeColor()));

        }

        if (adapter!=null){
            adapter.setColor(getThemeColor());
        }
    }

    private void getComponent(View view){

        imageView=view.findViewById(R.id.album_pager);

        recyclerView = view.findViewById(R.id.lrc_list);
        center_view = view.findViewById(R.id.center_view);
        play_button = view.findViewById(R.id.lrc_playing_button);
        time_text = view.findViewById(R.id.lrc_time);
//        expand_button=view.findViewById(R.id.lrc_expand_button);
        textSizeSeekBar=view.findViewById(R.id.text_size_seek_bar);
        lrcView=view.findViewById(R.id.lrc_view);
        info=view.findViewById(R.id.null_lrc_info);
        lrc_controller=view.findViewById(R.id.lrc_controller);

    }

    private void initAlbum(){

        Bundle bundle=getArguments();

        if (bundle!=null) {

            this.metadataCompat=bundle.getParcelable("album");

            if (this.metadataCompat!=null){

                int w=getResources().getDisplayMetrics().widthPixels;

                long id=metadataCompat.getLong(Conf.ALBUM_ID);

                RequestOptions options=new RequestOptions().placeholder(R.drawable.ic_music_note_black_150dp);

                Glide.with(this).load(ImageLoader.getUrl(id)).apply(options).into(imageView);

//                ImageLoader.getImageLoader(getContext()).setListImage(this.imageView,w,w,this.metadataCompat,ImageLoader.BIG);

            }


        }
    }

    private void initLrcList(){

        adapter=new LrcAdapter(this);

        adapter.setListener(view -> {

            lrcView.setVisibility(View.GONE);

            imageView.setVisibility(View.VISIBLE);

            onScrolledToTop();

        });

        linearLayoutManager=new LrcLinearLayoutManager(getContext());
        linearLayoutManager.setSpeedSlow();

        linearSmoothScroller=new LrcLinearSmoothScroller(getContext()){

            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }

            @Nullable
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return linearLayoutManager.computeScrollVectorForPosition(targetPosition);
            }

        };


        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setFragment(this);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!autoScroll){

                    if (center_view.getVisibility()==View.GONE){
                        showCenterLine(center_view);
                    }

                    if (textSizeSeekBar.getVisibility()==View.VISIBLE){

                        hideCenterLine(textSizeSeekBar);
                    }


                    getCenterItem();
                }else {

                    if (center_view.getVisibility()==View.VISIBLE){

                        hideCenterLine(center_view);
                    }

                }

                if (!recyclerView.canScrollVertically(-1)){

                    onScrolledToTop();

                }else {

                    onScrolledDown();

                }


            }
        });

    }

    //其他
    public void onScrolledDown() {

        Intent intent=new Intent(PlayingDragView.SCROLL);

        intent.putExtra(Conf.SCROLL,false);

        Objects.requireNonNull(getActivity()).sendBroadcast(intent);

    }

    public void onScrolledToTop() {

        //滚动到顶部，可以向下滑动

        Intent intent=new Intent(PlayingDragView.SCROLL);

        intent.putExtra(Conf.SCROLL,true);

        Objects.requireNonNull(getActivity()).sendBroadcast(intent);

    }


    public void cancelTheCenter(){

        handler.sendEmptyMessage(1);
        handler.sendEmptyMessage(0);

    }


    public void setCurrentItemToCenter(int index) {

        if (!autoScroll){//如果不是自动滚动则返回
            return;
        }

        //当前可见的第一项和最后一项
        int firstItem = linearLayoutManager.findFirstVisibleItemPosition();
        int lastItem = linearLayoutManager.findLastVisibleItemPosition();


        int count = lastItem - firstItem;

        if (count > 0) {

            int half = count / 2;

            int top = index - half;

            if (top >= 0) {
                autoScroll = true;
                linearSmoothScroller.setTargetPosition(top);
                linearLayoutManager.startSmoothScroll(linearSmoothScroller);
//                recyclerView.smoothScrollToPosition(top);


            }

        }

    }

    public void getCenterItem() {


        //当前可见的第一项和最后一项
        int firstItem = linearLayoutManager.findFirstVisibleItemPosition();
        int lastItem = linearLayoutManager.findLastVisibleItemPosition();

        for (int i = firstItem; i <= lastItem; i++) {

            View view = linearLayoutManager.findViewByPosition(i);
            if (view != null) {
                if (ifCenter(view)) {
                    adapter.setCenter(i);
                }
            }
        }
    }

    private boolean ifCenter(View view) {

        int half_screen = getResources().getDisplayMetrics().widthPixels / 2;
        int top_center = half_screen - view.getHeight();
        int ruler = view.getTop();
        return (half_screen > ruler && ruler >= top_center);
    }


    /**
     * 在中心轴设置歌词对应的时间
     * @param lrc
     */
    public void showTime(Lrc lrc) {

        this.lrc = lrc;
        long t = lrc.getTime();
        if (t >= 0) {
            String time = Mp3Util.formatTime(lrc.getTime());

            time_text.setText(time);

        } else {
            time_text.setText("");
        }
    }


    /**
     * 点击事件
     */
    private void initClick() {

        //播放按钮点击事件
        play_button.setOnClickListener(v -> {
            if (this.lrc == null) {
                return;
            }

            if (this.lrc.getTime() >= 0) {


                //播放当前进度

                mediaControllerCompat.getTransportControls().seekTo(lrc.getTime());


                cancelTheCenter();//取消显示
                hideCenterLine(center_view);//取消中间线
            }

        });

        //扩展按钮点击事件
//        expand_button.setOnClickListener(this::showDialog);

        //封面图点击事件
        imageView.setOnClickListener(v -> {
//            presenter.getLrc(music);//获取歌词

            getLrcData();

            lrcView.setVisibility(View.VISIBLE);

            onScrolledDown();//禁止滑动

//            imageView.setVisibility(View.GONE);


//            setScroll(false);//恢复滑动
        });


        info.setOnClickListener(v -> {

            lrcView.setVisibility(View.GONE);

            imageView.setVisibility(View.VISIBLE);

        });

        lrc_controller.setOnClickListener(v -> {

            PopupMenu popupMenu=new PopupMenu(getActivity(),v);

            popupMenu.inflate(R.menu.lrc_menu);

            popupMenu.setOnMenuItemClickListener(item -> {

                switch (item.getItemId()){

                    case R.id.lrc_size:

                        setProgress(adapter.getTextSize());

                        showCenterLine(this.textSizeSeekBar);

                        break;


                    case R.id.fix_lrc://修正歌词



                        break;


                    case R.id.close_fan://关闭翻译歌词

                        adapter.cleanTLrcList();

                        break;

                }


                return true;
            });

            popupMenu.show();

        });

    }

    private void getLrcData(){

        if (this.metadataCompat!=null) {
            presenter.getLrcData(this.metadataCompat);
        }

    }



    Handler handler=new Handler(Looper.getMainLooper()){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:

                    adapter.setCenter(-1);

                    break;


                case 1:

                    hideCenterLine(center_view);

                    break;

            }

        }
    };


    private void showCenterLine(View view){

        if (view==null){
            return;
        }

        ObjectAnimator alpha= ObjectAnimator.ofFloat(view,"alpha",0,1).setDuration(1000);

        alpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationEnd(animation);

                view.setVisibility(View.VISIBLE);

            }
        });

        alpha.start();

    }


    private void hideCenterLine(View view){

        if (view==null){
            return;
        }

        ObjectAnimator alpha= ObjectAnimator.ofFloat(view,"alpha",1,0).setDuration(1000);

        alpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.GONE);
            }
        });

        alpha.start();

    }

    //改变歌词大小
    private void changeTextSize(){

        float defaultSize=getResources().getDimension(R.dimen.lrc_text_size);

        textSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    float size=progress+defaultSize;
//                    presenter.saveTextSize(getActivity(),size);


                    //保存
                    adapter.setTextSize(size);

                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                hideCenterLine(textSizeSeekBar);

            }
        });

    }


    //设置歌词字体大小进度
    private void setProgress(float progress){

        float d=getResources().getDimension(R.dimen.lrc_text_size);

        int p= (int) (progress-d);

        if (p<0){
            p=0;
        }

        this.textSizeSeekBar.setProgress(p);
    }


    @Override
    public void setLLrc(List<Lrc> lLrc) {

        if (lLrc==null||lLrc.isEmpty()){
            //无歌词，不展示，直接填写信息
            info.setVisibility(View.VISIBLE);

            info.setOnClickListener(v -> {

                lrcView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
//                setScroll(true);

                if (textSizeSeekBar.getVisibility()==View.VISIBLE){
                    hideCenterLine(textSizeSeekBar);
                }

                onScrolledToTop();

            });

        }else {
            info.setVisibility(View.GONE);
            adapter.setLrcList(lLrc);
        }

    }

    @Override
    public void setTLrc(List<Lrc> lrcs) {

        if (lrcs!=null&&!lrcs.isEmpty()){

            adapter.setTlrcList(lrcs);

        }


    }

    @Override
    protected void onPlaybackStateChanged(PlaybackStateCompat state) {
        super.onPlaybackStateChanged(state);

        long currentTime=state.getPosition();

        long time=this.metadataCompat.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);

        if (adapter!=null){

            adapter.changeIndex(currentTime,time);

        }

    }

    private void showFixMenu(){

        AlertDialog.Builder builder=new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

        String[] strings=new String[]{"歌词错误，手动查询","这是首纯音乐啊！"};

        builder.setTitle("歌词出什么问题了吗").setItems(strings,(dialog, which) -> {

            switch (which){

                case 0:



                    break;


                case 1:



                    break;

            }


        }).setPositiveButton("取消",(dialog, which) -> {

            builder.create().cancel();

        }).show();


    }

}
