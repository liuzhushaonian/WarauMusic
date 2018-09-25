package com.app.legend.waraumusic.fragment;



import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.activity.MainActivity;
import com.app.legend.waraumusic.adapter.MusicAdapter;
import com.app.legend.waraumusic.adapter.PlayAlbumAdapter;
import com.app.legend.waraumusic.bean.Music;
import com.app.legend.waraumusic.presenter.PlayingViewFragmentPresenter;
import com.app.legend.waraumusic.presenter.interfaces.IPlayingViewFragment;
import com.app.legend.waraumusic.utils.Conf;
import com.app.legend.waraumusic.utils.ImageLoader;
import com.app.legend.waraumusic.utils.Mp3Util;
import com.app.legend.waraumusic.utils.RoundProgressBar;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayingViewFragment extends BasePresenterFragment<IPlayingViewFragment, PlayingViewFragmentPresenter>
        implements IPlayingViewFragment {

    private ImageView playBtn;
    private LinearLayout playBar;
    private ViewPager viewPager;
    private PlayAlbumAdapter albumAdapter;
    private TextView song, info;
    private ImageView previewAlbum;
    private int w;
    private RoundProgressBar roundProgressBar;
    private Toolbar toolbar;
    private MediaMetadataCompat mediaMetadataCompat;
    int pre_position = -1;//记录当前position，也为记录上次的position，判断滑动是向左还是向右
    boolean isScroll = true;
    private ImageView mode, previous, play, next, list;
    private SeekBar progress;
    private TextView playingTime, time;
    private int[] modes = new int[]{
            Conf.REPEAT_MODE_NONE,Conf.REPEAT_MODE_ALL,Conf.REPEAT_MODE_ONE,Conf.REPEAT_MODE_SHUFFLE
    };

    int repeat = 0;
    private MusicAdapter adapter;

    public PlayingViewFragment() {
        // Required empty public constructor


    }

    @Override
    protected PlayingViewFragmentPresenter createPresenter() {
        return new PlayingViewFragmentPresenter(this);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_playing_view, container, false);


        getComponent(view);

        initViewPager();

        initPlayingList();

        scrollPager();

        event();


        w = getResources().getDimensionPixelSize(R.dimen.bottom_play_bar);

        initToolbar();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        autoSetColor();
//        resumeView();

    }

    private void getComponent(View view) {

        playBtn = view.findViewById(R.id.play_bar_button);
        playBar = view.findViewById(R.id.linearLayout);
        viewPager = view.findViewById(R.id.play_pager);
        song = view.findViewById(R.id.song_name);
        info = view.findViewById(R.id.song_artist);
        previewAlbum = view.findViewById(R.id.small_album);
        roundProgressBar = view.findViewById(R.id.playing_bar_progress);
        toolbar = view.findViewById(R.id.toolbar);
        mode = view.findViewById(R.id.mode);
        previous = view.findViewById(R.id.pre);
        play = view.findViewById(R.id.play);
        next = view.findViewById(R.id.next);
        list = view.findViewById(R.id.list);
        progress = view.findViewById(R.id.seekBar);
        playingTime = view.findViewById(R.id.playing_time);
        time = view.findViewById(R.id.time);

    }


    /**
     * 实例化播放列表
     */
    private void initPlayingList(){

        adapter=new MusicAdapter(MusicAdapter.LIST_MUSIC);
        adapter.setPlayingListClickListener((position,id) -> {

            mediaControllerCompat.getTransportControls().skipToQueueItem(id);

        });

    }

    private void initToolbar() {

//        ViewGroup.LayoutParams layoutParams=toolbar.getLayoutParams();
//
////        int height=getResources().get(R.attr.actionBarSize)
//
//        layoutParams.height=layoutParams.height+getStatusBarHeight();
//
//        toolbar.setLayoutParams(layoutParams);

        int bottom=getResources().getDimensionPixelSize(R.dimen.half_de);

        toolbar.setPadding(0, getStatusBarHeight(), 0, bottom);
        toolbar.setTitle("");


    }


    private void initViewPager() {

        reDrawPager();

        albumAdapter = new PlayAlbumAdapter(getChildFragmentManager());

        viewPager.setAdapter(albumAdapter);

    }


    @Override
    protected void onConnect() {
        super.onConnect();

        resumeView();//恢复数据

    }

    @Override
    protected void onPlaybackStateChanged(PlaybackStateCompat state) {
        super.onPlaybackStateChanged(state);

        Bundle bundle=state.getExtras();

        if (bundle!=null&&bundle.getInt("mode",-1)==11){//仅仅更新播放模式

            resumeMode();

            return;
        }


        switch (state.getState()) {

            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_PAUSED:

                playBtn.setImageResource(R.drawable.ic_play_arrow_black_16dp);
                play.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                break;

            case PlaybackStateCompat.STATE_PLAYING:

                playBtn.setImageResource(R.drawable.ic_pause_black_16dp);
                play.setImageResource(R.drawable.ic_pause_black_24dp);


                break;
        }

        if (trackSeek){//如果正在拖拽，则不需要自动设置进度
            return;
        }

        if (mediaControllerCompat.getMetadata()==null){
            return;
        }

        int progress = (int) (state.getPosition() / (mediaControllerCompat.getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION) / 500));

        roundProgressBar.setProgress(progress);

        this.progress.setProgress(progress);

        playingTime.setText(Mp3Util.formatTime(state.getPosition()));

        if (this.mediaMetadataCompat==null){

            resumeView();//恢复数据

        }


    }

    RequestOptions options=new RequestOptions().dontAnimate();

    RequestListener<Drawable> drawableRequestListener=new RequestListener<Drawable>() {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

            previewAlbum.setVisibility(View.GONE);

            return true;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            return false;
        }
    };


    //歌曲改变
    @Override
    protected void onMetadataChanged(MediaMetadataCompat metadata) {
        super.onMetadataChanged(metadata);

        if (metadata == null) {
            return;
        }

        this.mediaMetadataCompat = metadata;



        if (previewAlbum.getVisibility()==View.GONE){
            previewAlbum.setVisibility(View.VISIBLE);
        }

//        ImageLoader.getImageLoader(getContext()).setListImage(previewAlbum, w, w, metadata, ImageLoader.PLAY);



        setInfo(metadata);



        autoChange(metadata);

    }


    /**
     * 播放队列改变
     *
     * @param queue 播放队列
     */
    @Override
    protected void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
        super.onQueueChanged(queue);

        presenter.getPlayingListData(queue);

        presenter.getCurrentPositionAndUpdate(mediaControllerCompat.getMetadata(),queue);//还需要改变position

    }

    @Override
    void autoSetColor() {
        if (toolbar!=null){

            toolbar.setBackgroundColor(getThemeColor());
            roundProgressBar.setRoundProgressColor(getThemeColor());
            playBtn.setImageTintList(ColorStateList.valueOf(getThemeColor()));

            progress.setProgressTintList(ColorStateList.valueOf(getThemeColor()));

            progress.setThumbTintList(ColorStateList.valueOf(getThemeColor()));


        }
    }

    /**
     * 改变播放队列
     * @param metadataCompatList
     */
    @Override
    public void setPlayingData(List<MediaMetadataCompat> metadataCompatList) {

        albumAdapter.setMetadataCompatList(metadataCompatList);



    }

    boolean isBtn = false;

    @Override
    public void setPosition(int position) {

        isScroll = false;

        if (isBtn) {//如果是手动点击上一曲或下一曲，则不需要直接跳转，而是带着滑动

            viewPager.setCurrentItem(position, true);

            isBtn = false;//恢复变量，避免之后失效

        } else {

            viewPager.setCurrentItem(position, false);
        }

//        viewPager.setCurrentItem(position,false);


        pre_position = position;
    }

    @Override
    public void setPlayingList(List<Music> musicList) {

        adapter.setData(musicList);

    }


    //ViewPager滑动事件
    //上一首或下一首
    private void scrollPager() {

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            int p=-1;

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                isScroll = true;

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);

                if (adapter.getItemCount()==1){
                    return;
                }

                //神一般的操作，在此实现对滑动后是否换页判断，是则判断并播放上一曲或下一曲
                //state有三种状态1，2，0
                //手指按下去是1，手指抬起是2，处于空闲则为0；所以在Viewpager滑动结束时，state为0，此时可以去做判断
                isScroll = true;

                if (state==0) {

                    if (!isBtn) {//需要判断是否是手动点击

                        p=viewPager.getCurrentItem();

                        if (p > pre_position) {
                            //向左,下一曲

                            mediaControllerCompat.getTransportControls().skipToNext();

                        } else if (p < pre_position) {
                            //向右，上一曲

                            mediaControllerCompat.getTransportControls().skipToPrevious();

                        }

                        pre_position=p;//备份
                    }

                }


            }

        });
    }

    /**
     * 自动判断当前播放歌曲所在位置
     *
     * @param metadataCompat 正在播放的歌曲
     */
    private void autoChange(MediaMetadataCompat metadataCompat) {

//        Log.d("me--->>>",metadataCompat.getDescription().getTitle()+"--size->>"+mediaControllerCompat.getQueue());

        presenter.getCurrentPositionAndUpdate(metadataCompat, mediaControllerCompat.getQueue());

    }

    private void reDrawPager() {

        ViewGroup.LayoutParams layoutParams = viewPager.getLayoutParams();

        layoutParams.height = getResources().getDisplayMetrics().widthPixels;

        viewPager.setLayoutParams(layoutParams);

    }

    boolean trackSeek = false;

    private void event() {

        //进度条滑动事件
        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            long seek_progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (mediaControllerCompat.getMetadata()==null){
                    return;
                }

                if (trackSeek) {



                    long time = mediaControllerCompat.getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION);

                    seek_progress = (progress * (time / 500));

                    playingTime.setText(Mp3Util.formatTime(seek_progress));

                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                trackSeek = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                if (mediaControllerCompat.getMetadata()==null){
                    return;
                }

                trackSeek = false;

                mediaControllerCompat.getTransportControls().seekTo(seek_progress);

                seek_progress = 0;//恢复，避免影响之后的播放
            }
        });


        //播放条点击事件，上滑整个播放界面
        playBar.setOnClickListener(v -> {

            ((MainActivity) (Objects.requireNonNull(getActivity()))).autoScrollToTop();

        });


        //播放按钮点击事件
        playBtn.setOnClickListener(v -> mediaControllerCompat.getTransportControls().play());


        //重复模式
        mode.setOnClickListener(v -> {

            repeat++;

            if (repeat > 3) {
                repeat = 0;
            }

            mediaControllerCompat.getTransportControls().setRepeatMode(modes[repeat]);

            switch (modes[repeat]) {

                case Conf.REPEAT_MODE_NONE:

                    mode.setImageResource(R.drawable.ic_no_repeat_black_24dp);

                    break;

                case Conf.REPEAT_MODE_ALL:
                    mode.setImageResource(R.drawable.ic_repeat_black_24dp);

                    break;
                case Conf.REPEAT_MODE_ONE:
                    mode.setImageResource(R.drawable.ic_repeat_one_black_24dp);

                    break;

                case Conf.REPEAT_MODE_SHUFFLE:

                    mode.setImageResource(R.drawable.ic_shuffle_black_24dp);

                    break;


            }


        });

        //上一曲
        previous.setOnClickListener(v -> {

            isBtn = true;

            mediaControllerCompat.getTransportControls().skipToPrevious();

        });


        //播放or暂停
        play.setOnClickListener(v -> {

            mediaControllerCompat.getTransportControls().play();
        });


        //下一曲
        next.setOnClickListener(v -> {

            isBtn = true;

            mediaControllerCompat.getTransportControls().skipToNext();

        });

        //播放列表
        list.setOnClickListener(v -> {

            openBottomMenu();

        });


    }


    private void openBottomMenu(){

        View view= LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_dialog_content_view,null,false);

        RecyclerView recyclerView=view.findViewById(R.id.bottom_list);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext());

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
//        adapter.setMusicPositionList(PlayHelper.create().getCurrentMusicList());

        BottomSheetDialog bottomSheetDialog=new BottomSheetDialog(Objects.requireNonNull(getActivity()));

        bottomSheetDialog.setContentView(view);

//        view.setBackgroundColor(getResources().getColor(R.color.colorBlue));
//        bottomSheetDialog.getDelegate()
//                .findViewById(android.support.design.R.id.design_bottom_sheet)
//                .setBackgroundColor(getResources().getColor(R.color.colorTransparent));

        View v=bottomSheetDialog.getDelegate()
                .findViewById(android.support.design.R.id.design_bottom_sheet);
        if (v!=null) {
            v.setBackground(getResources().getDrawable(R.drawable.shape_bottom, getActivity().getTheme()));

        }

        bottomSheetDialog.show();

    }


    //恢复
    private void resumeView(){

        if (mediaControllerCompat!=null&&mediaControllerCompat.getMetadata()!=null){
            this.mediaMetadataCompat=mediaControllerCompat.getMetadata();

            presenter.getPlayingListData(mediaControllerCompat.getQueue());

            presenter.getCurrentPositionAndUpdate(mediaControllerCompat.getMetadata(),mediaControllerCompat.getQueue());//还需要改变position

            setInfo(mediaMetadataCompat);

        }

    }


    /**
     * 将歌曲信息设置在UI上
     * @param metadataCompat 正在播放的歌曲
     */
    private void setInfo(MediaMetadataCompat metadataCompat){

        if (getActivity()==null){//判断是否在Activity上
            return;
        }

        if (metadataCompat!=null){

            if (toolbar != null) {

                long id=metadataCompat.getLong(Conf.ALBUM_ID);

                Glide.with(this)
                        .load(ImageLoader.getUrl(id))
                        .apply(options).listener(drawableRequestListener)
                        .into(previewAlbum);

                time.setText(Mp3Util.formatTime(metadataCompat.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)));

                int progress = (int) (mediaControllerCompat.getPlaybackState().getPosition() /
                        (mediaControllerCompat.getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION) / 500));

//                Log.d("pro----->>>",""+progress);

                playingTime.setText(Mp3Util.formatTime(progress));

                roundProgressBar.setProgress(progress);

                this.progress.setProgress(progress);

                song.setText(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE));

                info.setText(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));

                String info=metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)+" | "
                        +metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);

                toolbar.setTitle(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE));//添加当前播放歌曲的信息
                toolbar.setSubtitle(info);


                resumeMode();

            }

        }

    }

    private void resumeMode(){

        if (mediaControllerCompat==null){
            return;
        }

        int repeatMode=mediaControllerCompat.getRepeatMode();
        int shuffle=mediaControllerCompat.getShuffleMode();

        if (shuffle==PlaybackStateCompat.SHUFFLE_MODE_ALL){//优先判断是否为随机模式，如果不是，则进入其他判断

            mode.setImageResource(R.drawable.ic_shuffle_black_24dp);

            repeat = 3;

        }else {

            switch (repeatMode) {

                case PlaybackStateCompat.REPEAT_MODE_NONE:

                    mode.setImageResource(R.drawable.ic_no_repeat_black_24dp);

                    repeat = 0;

                    break;

                case PlaybackStateCompat.REPEAT_MODE_ALL:
                    mode.setImageResource(R.drawable.ic_repeat_black_24dp);

                    repeat = 1;

                    break;
                case PlaybackStateCompat.REPEAT_MODE_ONE:
                    mode.setImageResource(R.drawable.ic_repeat_one_black_24dp);

                    repeat = 2;

                    break;
//                        case Conf.REPEAT_MODE_SHUFFLE:
//
//                            mode.setImageResource(R.drawable.ic_shuffle_black_24dp);
//
//                            repeat = 3;
//
//                            break;

            }

        }

    }

}
