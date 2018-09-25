package com.app.legend.waraumusic.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;

import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.activity.MainActivity;
import com.app.legend.waraumusic.service.play.PlayManager;
import com.app.legend.waraumusic.utils.Conf;
import com.app.legend.waraumusic.utils.ImageLoader;
import com.app.legend.waraumusic.utils.LongIdUtils;
import com.app.legend.waraumusic.utils.Mp3Util;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_SEEK_TO;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_STOP;
import static com.app.legend.waraumusic.utils.Conf.ADD_MUSIC;
import static com.app.legend.waraumusic.utils.Conf.REMOVE_MUSIC;
import static com.app.legend.waraumusic.utils.Conf.REPEAT_MODE_SHUFFLE;
import static com.app.legend.waraumusic.utils.Conf.UPDATE_LIST;
import static com.app.legend.waraumusic.utils.Conf.UPDATE_LIST_AND_PLAY;

public class PlayService extends MediaBrowserServiceCompat implements AudioManager.OnAudioFocusChangeListener {

    private MediaSessionCompat mediaSession;
    private static final String MY_MEDIA_ROOT_ID = "media_root_id";
    private PlayManager playManager;
    public final static String PLAY = "play_music";
    public final static String PREVIOUS = "previous_music";
    public final static String NEXT = "next_music";
    public final static String PAUSE = "pause_music";
    public static final String MODE="play_mode";

    private PlayingReceiver playingReceiver;

    private AudioEarPhoneReceiver mNoisyReceiver;

    int repeat = Conf.REPEAT_MODE_NONE;

    private int[] modes = new int[]{
            Conf.REPEAT_MODE_NONE,Conf.REPEAT_MODE_ALL,Conf.REPEAT_MODE_ONE,Conf.REPEAT_MODE_SHUFFLE
    };


    public PlayService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ComponentName mbr = new ComponentName(getPackageName(), MediaButtonReceiver.class.getName());

//        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);

//        PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        mediaSession = new MediaSessionCompat(this, getPackageName(), mbr, null);

        Intent intent=new Intent(this,MainActivity.class);

        PendingIntent pi = PendingIntent.getActivity(this, 99 /*request code*/,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mediaSession.setSessionActivity(pi);


//        mediaSession.setMediaButtonReceiver(mPendingIntent);

        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mediaSession.setCallback(callback, new Handler(Looper.getMainLooper()));


        PlaybackStateCompat.Builder builder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY |
                PlaybackStateCompat.ACTION_PLAY_PAUSE | ACTION_SKIP_TO_NEXT |
                ACTION_SKIP_TO_PREVIOUS |
                ACTION_STOP |
                ACTION_PLAY_FROM_MEDIA_ID |
                ACTION_PLAY_FROM_SEARCH |
                ACTION_SKIP_TO_QUEUE_ITEM |
                ACTION_SEEK_TO);

        mediaSession.setPlaybackState(builder.build());

        mediaSession.setActive(true);

        setSessionToken(mediaSession.getSessionToken());

        playManager = new PlayManager(this, new PlayListener());

        register();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mediaSession.setActive(false);
        mediaSession.release();

        playManager.release();

        unregister();

    }


    private void register() {

        playingReceiver = new PlayingReceiver();

        mNoisyReceiver = new AudioEarPhoneReceiver();

        IntentFilter mFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        mFilter.addAction(AudioManager.ACTION_HEADSET_PLUG);

        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(PLAY);
//        intentFilter.addAction(PAUSE);
//        intentFilter.addAction(NEXT);
//        intentFilter.addAction(PREVIOUS);
        intentFilter.addAction(MODE);

        registerReceiver(playingReceiver, intentFilter);

        registerReceiver(mNoisyReceiver, mFilter);


    }

    private void unregister() {

        if (this.playingReceiver != null) {
            unregisterReceiver(playingReceiver);
        }

        if (this.mNoisyReceiver != null) {
            unregisterReceiver(mNoisyReceiver);
        }

    }


    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {

        return new BrowserRoot(MY_MEDIA_ROOT_ID, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {


//        Mp3Util.newInstance().getAllList();

        result.detach();


//        result.detach();


    }


    /**
     * 分隔符
     * -----------------------------------------------------------------------------------------------------------
     * 播放回调
     */
    MediaSessionCompat.Callback callback = new MediaSessionCompat.Callback() {

        private List<MediaSessionCompat.QueueItem> queueItemList = new ArrayList<>();//普通序列

        private List<MediaSessionCompat.QueueItem> randomQueueItemList = new ArrayList<>();//随机序列

        private MediaMetadataCompat metadataCompat;

        private int index = -1;



        private long uniqueId = -1;

        @Override
        public void onPrepare() {
            super.onPrepare();

            if (queueItemList == null) {
                return;
            }

            String id = "";


            switch (repeat) {

                case Conf.REPEAT_MODE_SHUFFLE:

                    id = randomQueueItemList.get(index).getDescription().getMediaId();

                    uniqueId = randomQueueItemList.get(index).getQueueId();//保存唯一id

                    break;


                default:

                    id = queueItemList.get(index).getDescription().getMediaId();

                    uniqueId = queueItemList.get(index).getQueueId();//保存唯一id

                    break;

            }


            MediaMetadataCompat mediaMetadataCompat = Mp3Util.newInstance().getMediaMetadataCompatById(id);

            if (mediaMetadataCompat == null) {

                Log.w("onPrepare--->>", "the mediaMetadataCompat is null object");

                return;
            }

            this.metadataCompat = mediaMetadataCompat;

            MediaMetadataCompat[] metadataCompats=new MediaMetadataCompat[]{this.metadataCompat};

            String url=ImageLoader.getUrl(metadataCompat.getLong(Conf.ALBUM_ID));//获取专辑封面

            if (url!=null) {

                SimpleTarget<Bitmap> simpleTarget = new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                        metadataCompats[0] = new MediaMetadataCompat
                                .Builder(metadataCompats[0])
                                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, resource)
                                .build();

                        mediaSession.setMetadata(metadataCompats[0]);
                    }


                };

                Glide.with(PlayService.this).asBitmap().load(url).into(simpleTarget);

            }else {

                mediaSession.setMetadata(metadataCompats[0]);
            }



        }

        @Override
        public void onPlay() {

            if (!isReadyToPlay()) {//如果没有准备好，则不允许播放
                return;
            }

            mediaSession.setActive(true);

            if (this.metadataCompat == null) {//当前需要播放的为null，则准备好当前需要播放的data

                onPrepare();
            }

            playManager.playMetaData(this.metadataCompat);

            if (!mediaSession.isActive()) {
                mediaSession.setActive(true);
            }

        }

        @Override
        public void onPause() {

            playManager.onPause();

        }

        @Override
        public void onSkipToNext() {

            if (repeat == Conf.REPEAT_MODE_ONE) {//单曲模式，直接继续播放

                this.metadataCompat = null;
                onPlay();

                return;

            }


            if (repeat == Conf.REPEAT_MODE_ALL) {//列表循环,最后一首回到第一首

                if (index + 1 < queueItemList.size()) {
                    index++;
                } else {

                    index = 0;
                }

                this.metadataCompat = null;
                onPlay();

                return;

            }


            if (index + 1 < queueItemList.size()) {
                index++;
            } else if (repeat == Conf.REPEAT_MODE_NONE || repeat == Conf.REPEAT_MODE_SHUFFLE) {//单一模式或随机模式则不再继续播放

//                onPause();
                onStop();

                return;

            }

            this.metadataCompat = null;
            onPlay();

        }

        @Override
        public void onSkipToPrevious() {

//            super.onSkipToPrevious();

            if (repeat == Conf.REPEAT_MODE_ONE) {//单曲模式，直接继续播放

                this.metadataCompat = null;
                onPlay();

                return;

            }


            if (index > 0) {

                index--;

            } else {

                index = 0;
            }

            this.metadataCompat = null;

            onPlay();

        }

        @Override
        public void onStop() {
//            super.onStop();
            playManager.onStop();

            mediaSession.setActive(false);
        }

        @Override
        public void onSeekTo(long pos) {
//            super.onSeekTo(pos);

            playManager.onSeek(pos);

        }

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description, int index) {
//            super.onAddQueueItem(description, index);


            MediaSessionCompat.QueueItem queueItem = new MediaSessionCompat.QueueItem(description, LongIdUtils.getRandomId());

            this.queueItemList.add(index, queueItem);


        }

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {
//            super.onAddQueueItem(description);

            MediaSessionCompat.QueueItem queueItem = new MediaSessionCompat.QueueItem(description, LongIdUtils.getRandomId());

            switch (repeat) {

                case REPEAT_MODE_SHUFFLE://随机模式下添加音乐

                    this.randomQueueItemList.add(index+1,queueItem);//添加在随机列表处

                    this.queueItemList.add(queueItem);

                    mediaSession.setQueue(randomQueueItemList);//通知更新

                    break;


                default:

                    this.queueItemList.add(this.index + 1, queueItem);//添加到正在播放歌曲的后面

                    mediaSession.setQueue(queueItemList);//通知更新

                    break;

            }



        }


        @Override
        public void onRemoveQueueItem(MediaDescriptionCompat description) {
            super.onRemoveQueueItem(description);


        }

        private boolean isReadyToPlay() {

            return !(queueItemList == null || queueItemList.isEmpty());

        }

        @Override
        public void onCustomAction(String action, Bundle extras) {

            extras.setClassLoader(mediaSession.getClass().getClassLoader());

            switch (action) {

                case UPDATE_LIST:

                    this.queueItemList = extras.getParcelableArrayList("list");

                    break;


                case UPDATE_LIST_AND_PLAY://更新并播放，用于点击列表时

                    upDateListAndPlay(extras);

                    break;

                case ADD_MUSIC:

                    addMusic(extras);

                    break;


                case REMOVE_MUSIC://移除音乐

                    removeMusic(extras);

                    break;

            }

        }

        private void addMusic(Bundle bundle){

            MediaMetadataCompat mediaMetadataCompat=bundle.getParcelable("music");

            onAddQueueItem(mediaMetadataCompat.getDescription());

        }

        private void removeMusic(Bundle bundle) {


        }

        @Override
        public void onSetRepeatMode(int repeatMode) {
            super.onSetRepeatMode(repeatMode);


            if (repeat == repeatMode) {//避免重复
                return;
            }


            if (repeat == Conf.REPEAT_MODE_SHUFFLE) {//当前模式是随机模式，则改为普通模式

                toOrdinary();

            }

            repeat = repeatMode;//赋值

            if (repeatMode == Conf.REPEAT_MODE_SHUFFLE) {//如果是随机模式

                toShuffle();


            }

            switch (repeat){

                case Conf.REPEAT_MODE_NONE:

                    mediaSession.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE);

                    mediaSession.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_INVALID);

                    break;

                case Conf.REPEAT_MODE_ALL:

                    mediaSession.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL);
                    mediaSession.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_INVALID);
                    break;

                case Conf.REPEAT_MODE_ONE:

                    mediaSession.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
                    mediaSession.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_INVALID);
                    break;

                case Conf.REPEAT_MODE_SHUFFLE:

//                    mediaSession.setRepeatMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);

                    mediaSession.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
                    break;

            }

//            Log.d("repeat--->>>",repeat+"");

            PlaybackStateCompat.Builder builder=new PlaybackStateCompat.Builder();

            Bundle bundle=new Bundle();

            bundle.putInt("mode",11);

            builder.setExtras(bundle);

            mediaSession.setPlaybackState(builder.build());//通知更改UI


            startNewNotification();



        }


        @Override
        public void onSkipToQueueItem(long id) {
//            super.onSkipToQueueItem(id);

            Observable
                    .create((ObservableOnSubscribe<Integer>) e -> {

                        index = getIndex(id);

                        e.onNext(9);
                        e.onComplete();
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Integer>() {

                        Disposable disposable;

                        @Override
                        public void onSubscribe(Disposable d) {
                            disposable = d;
                        }

                        @Override
                        public void onNext(Integer integer) {

                            metadataCompat = null;
                            onPlay();
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            if (!disposable.isDisposed()) {
                                disposable.dispose();
                            }
                        }
                    });
        }

        /**
         * 根据id寻找当前的index并播放
         * @param id 唯一id
         * @return 返回index
         */
        private int getIndex(long id) {

            int position = 0;

            if (repeat == Conf.REPEAT_MODE_SHUFFLE) {

                for (int i = 0; i < randomQueueItemList.size(); i++) {

                    MediaSessionCompat.QueueItem queueItem = queueItemList.get(i);

                    if (queueItem.getQueueId() == id) {

                        return i;
                    }
                }


            } else {

                for (int i = 0; i < queueItemList.size(); i++) {

                    MediaSessionCompat.QueueItem queueItem = queueItemList.get(i);

                    if (queueItem.getQueueId() == id) {

                        return i;
                    }
                }
            }

            return position;

        }


        /**
         * 获取当前的播放位置
         * 用于在随机模式下回复其他模式
         * 或者在随机模式下添加播放歌曲，方便普通列表添加歌曲到正确的位置
         * @return 返回播放位置position
         */
        private int getCurrentPosition() {


            if (this.metadataCompat != null) {//当前存在播放的歌曲

                for (int i = 0; i < queueItemList.size(); i++) {

                    MediaSessionCompat.QueueItem queueItem = queueItemList.get(i);

                    if (queueItem.getQueueId() == uniqueId) {

                        return i;
                    }
                }
            }
            return 0;
        }

        /**
         * 在普通模式切换到随机模式，将列表打乱
         * 放入线程执行
         */
        private void toShuffle() {


            Observable
                    .create((ObservableOnSubscribe<Integer>) e -> {

                        if (uniqueId != -1) {//有正在播放的歌曲

                            MediaSessionCompat.QueueItem Item = null;

                            this.randomQueueItemList.clear();//清空列表

                            for (int i = 0; i < queueItemList.size(); i++) {

                                MediaSessionCompat.QueueItem queueItem = queueItemList.get(i);

                                if (queueItem.getQueueId() == uniqueId) {

                                    Item = queueItem;
                                } else {
                                    randomQueueItemList.add(queueItem);
                                }
                            }

                            Collections.shuffle(randomQueueItemList);

                            randomQueueItemList.add(0, Item);//添加到第一位

                            index = 0;//指针改为第一位

                            e.onNext(10);
                            e.onComplete();


                        }


                        e.onNext(-1);
                        e.onComplete();

                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Integer>() {

                        Disposable disposable;

                        @Override
                        public void onSubscribe(Disposable d) {
                            disposable = d;
                        }

                        @Override
                        public void onNext(Integer integer) {

                            if (integer > 0) {

                                mediaSession.setQueue(randomQueueItemList);//通知改变列表

                            }

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            if (!disposable.isDisposed()) {
                                disposable.dispose();
                            }
                        }
                    });

        }


        /**
         * 从随机模式返回普通模式
         * 放入线程执行
         */
        private void toOrdinary() {

            Observable
                    .create((ObservableOnSubscribe<Integer>) e -> {

                        if (uniqueId != -1) {

                            index = getCurrentPosition();

                            e.onNext(10);
                            e.onComplete();

                        }

                        e.onNext(-1);
                        e.onComplete();


                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Integer>() {

                        Disposable disposable;

                        @Override
                        public void onSubscribe(Disposable d) {
                            disposable = d;
                        }

                        @Override
                        public void onNext(Integer integer) {

                            mediaSession.setQueue(queueItemList);

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            if (!disposable.isDisposed()) {
                                disposable.dispose();
                            }
                        }
                    });

        }


        /**
         * 传入列表并播放歌曲
         * @param extras bundle对象
         */
        private void upDateListAndPlay(Bundle extras) {

            Observable
                    .create((ObservableOnSubscribe<Integer>) e -> {

                        this.index = extras.getInt("index", 0);

                        List<MediaSessionCompat.QueueItem> itemList = extras.getParcelableArrayList("list");

                        if (itemList == null) {
                            return;
                        }

                        this.queueItemList.clear();

                        this.queueItemList.addAll(itemList);//全局添加

                        if (repeat == Conf.REPEAT_MODE_SHUFFLE) {

                            MediaSessionCompat.QueueItem queueItem = itemList.get(index);

                            itemList.remove(queueItem);

                            index = 0;//重置指针

                            randomQueueItemList.clear();//清除全部

                            randomQueueItemList.addAll(itemList);//添加全部，避免全局改变

                            Collections.shuffle(randomQueueItemList);

                            randomQueueItemList.add(0, queueItem);//放到首位

                            e.onNext(20);
                            e.onComplete();


                        } else {

                            e.onNext(60);
                            e.onComplete();


                        }

                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Integer>() {

                        Disposable disposable;

                        @Override
                        public void onSubscribe(Disposable d) {
                            disposable = d;
                        }

                        @Override
                        public void onNext(Integer integer) {

                            if (integer == 20) {

                                mediaSession.setQueue(randomQueueItemList);

                            } else if (integer == 60) {

                                mediaSession.setQueue(queueItemList);

                            }


                            metadataCompat = null;//清除当前播放

                            onPlay();

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            if (!disposable.isDisposed()) {
                                disposable.dispose();
                            }
                        }
                    });


        }


        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {


            KeyEvent event = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

            keyEvent(event);

            return true;
        }

        private Timer timer;
        private int open = 0;
        private int clickCount = 0;

        /**
         * 自定义按键事件
         * @param event 事件
         */
        private void keyEvent(KeyEvent event) {

            if (event == null || playManager == null) {
                return;
            }

            int keycode = event.getKeyCode();

            switch (keycode) {
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    //CMD STOP

                    onPause();

                    break;
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    //CMD TOGGLE PAUSE

                    if (event.getAction() == KeyEvent.ACTION_UP) {

                        clickCount++;
                        cancelTimer();
                        open = 0;
                        startTimer();
                    }

                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    //CMD NEXT 这里处理播放器逻辑 下一曲

                    onSkipToNext();

                    break;

                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    //CMD PREVIOUS 这里处理播放器逻辑 上一曲

                    onSkipToPrevious();

                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    //CMD PAUSE 这里处理播放器逻辑 暂停

                    onPause();

                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    //CMD PLAY 这里处理播放器逻辑 播放

                    onPlay();

                    break;
            }

        }

        private void cancelTimer() {
            if (timer != null) {
                open = 0;
                timer.cancel();
            }
        }

        private void startTimer() {
            timer = new Timer();

            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    open += 1;

                    if (open >= 10) {//过1秒没有点击

                        switch (clickCount) {
                            case 1://单击
                                handler.sendEmptyMessage(1);

                                break;
                            case 2://双击
                                handler.sendEmptyMessage(2);

                                break;
                            case 3:

                                handler.sendEmptyMessage(3);
                                break;
                        }

                        clickCount = 0;

                        timer.cancel();
                    }
                }
            };

            timer.schedule(timerTask, 0, 100);
        }

        private Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
//                super.handleMessage(msg);

                switch (msg.what) {
                    case 1:

                        if (playManager.isPlaying()) {
                            onPause();
                        } else {
                            onPlay();
                        }

                        break;
                    case 2:

                        onSkipToNext();

                        break;
                    case 3:

                        onSkipToPrevious();

                        break;
                }
            }
        };
    };

    //音频焦点
    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN://已获得音频焦点
                if (playManager != null && !playManager.isPlaying()) {

                    playManager.setVolume(1.0f);//恢复声音
                    playManager.onStart();
                }

                break;
            case AudioManager.AUDIOFOCUS_LOSS://失去音频焦点
                if (playManager != null) {
                    playManager.onPause();
                }

                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT://临时失去焦点
                if (playManager != null) {
                    playManager.onPause();
                }

                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK://临时失去焦点，但允许低音量播放
                if (playManager != null) {

                    playManager.setVolume(0.1f);

                }
                break;
        }
    }


    /**
     * 内部类更新来自PlayManager的播放信息以及更新notification
     */
    class PlayListener extends PlayInfoListener {


        @Override
        public void setPlayState(PlaybackStateCompat playState) {

            mediaSession.setPlaybackState(playState);//通知界面进行更新




        }

        //播放完成的回调
        @Override
        public void onComplete() {

//            mediaSession.setActive(false);

            callback.onSkipToNext();

        }

        @Override
        public void showNotification(PlaybackStateCompat playState) {

            switch (playState.getState()) {

                case PlaybackStateCompat.STATE_PLAYING:


                    startNewNotification();


                    break;


                case PlaybackStateCompat.STATE_NONE:
                case PlaybackStateCompat.STATE_PAUSED:

                    startNewNotification();
                    break;

            }
        }
    }


    private void startNewNotification() {


        MediaMetadataCompat metadataCompat=playManager.getMediaMetadataCompat();
        if (metadataCompat==null){
            return;
        }


        String CHANNEL_ID = "JustMusic";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence sequence = "just_music_notification_channel";
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, sequence, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setShowBadge(false);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationChannel.setSound(null, null);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Bitmap bitmap = ImageLoader
                .getImageLoader(getApplicationContext())
                .getBitmap(metadataCompat.getLong("albumId"));


        if (bitmap != null) {
            builder.setLargeIcon(bitmap);

            int defaultValue = getResources().getColor(R.color.colorBlueGrey);
//            int color=ColorUtil.getColor(bitmap,defaultValue);
            builder.setColor(defaultValue);
        }


        String info = metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST) + " | "
                + metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);



        NotificationCompat.Action pre_action = new NotificationCompat.Action(R.drawable.ic_skip_previous_black_24dp, "previous",
                MediaButtonReceiver.buildMediaButtonPendingIntent(this,PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));


        builder.addAction(pre_action);


        if (playManager.isPlaying()) {
            NotificationCompat.Action pause_action = new NotificationCompat.Action(R.drawable.ic_pause_black_24dp, "play",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(this,PlaybackStateCompat.ACTION_PAUSE));
            builder.addAction(pause_action);
        } else {
            NotificationCompat.Action play_action = new NotificationCompat.Action(R.drawable.ic_play_arrow_black_24dp, "pause",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(this,PlaybackStateCompat.ACTION_PLAY));
            builder.addAction(play_action);

        }

        NotificationCompat.Action next_action = new NotificationCompat.Action(R.drawable.ic_skip_next_black_24dp, "next",
                MediaButtonReceiver.buildMediaButtonPendingIntent(this,PlaybackStateCompat.ACTION_SKIP_TO_NEXT));
        builder.addAction(next_action);



        switch (repeat){



            case Conf.REPEAT_MODE_NONE:

                Intent t_intent = new Intent(MODE);

                PendingIntent t_pendingIntent = getPendingIntentForBroadcast(t_intent);

                NotificationCompat.Action t_action = new NotificationCompat.Action(R.drawable.ic_no_repeat_black_24dp, "mode_none",
                        t_pendingIntent);

                builder.addAction(t_action);

                break;

            case Conf.REPEAT_MODE_ALL:

                Intent o_intent = new Intent(MODE);



                PendingIntent o_pendingIntent = getPendingIntentForBroadcast(o_intent);

                NotificationCompat.Action o_action = new NotificationCompat.Action(R.drawable.ic_repeat_black_24dp, "mode_all",
                        o_pendingIntent);

                builder.addAction(o_action);

                break;
            case Conf.REPEAT_MODE_ONE:

                Intent p_intent = new Intent(MODE);



                PendingIntent p_pendingIntent = getPendingIntentForBroadcast(p_intent);

                NotificationCompat.Action p_action = new NotificationCompat.Action(R.drawable.ic_repeat_one_black_24dp, "mode_one",
                        p_pendingIntent);

                builder.addAction(p_action);

                break;
            case Conf.REPEAT_MODE_SHUFFLE:

                Intent a_intent = new Intent(MODE);

                PendingIntent a_pendingIntent = getPendingIntentForBroadcast(a_intent);

                NotificationCompat.Action a_action = new NotificationCompat.Action(R.drawable.ic_shuffle_black_24dp, "mode_shu",
                        a_pendingIntent);

                builder.addAction(a_action);

                break;


        }



        Notification notification = builder.setContentTitle(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE))
                .setContentText(info)
                .setContentIntent(mediaSession.getController().getSessionActivity())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_music_note_black_24dp)
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2,3)
                        .setMediaSession(mediaSession.getSessionToken()))
                .build();

        startForeground(233, notification);

        if (!playManager.isPlaying()) {
            stopForeground(false);
        }

    }


    private PendingIntent getPendingIntentForBroadcast(Intent intent) {

        return PendingIntent.getBroadcast(this, 0, intent, 0);
    }


    class PlayingReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action == null) {
                return;
            }

            switch (action) {

                case PAUSE:

                    callback.onPause();

                    break;

                case PLAY:

                    callback.onPlay();

                    break;
                case NEXT:

                    callback.onSkipToNext();

                    break;

                case PREVIOUS:

                    callback.onSkipToPrevious();

                    break;


                case MODE:


                    for (int mode : modes) {

                        if (mode == repeat) {//找到当前模式

                            switch (mode) {

                                case Conf.REPEAT_MODE_NONE://当前模式，需要改为下一个模式

                                    callback.onSetRepeatMode(Conf.REPEAT_MODE_ALL);

                                    break;

                                case Conf.REPEAT_MODE_ALL:

                                    callback.onSetRepeatMode(Conf.REPEAT_MODE_ONE);

                                    break;
                                case Conf.REPEAT_MODE_ONE:

                                    callback.onSetRepeatMode(Conf.REPEAT_MODE_SHUFFLE);

                                    break;
                                case Conf.REPEAT_MODE_SHUFFLE:

                                    callback.onSetRepeatMode(Conf.REPEAT_MODE_NONE);

                                    break;

                            }

                            break;
                        }

                    }


                    break;


            }

        }
    }


    //耳机拔插接收器
    public class AudioEarPhoneReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null && action.length() > 0) {
                switch (action) {
                    //来电/耳机拔出时暂停播放
                    case AudioManager.ACTION_AUDIO_BECOMING_NOISY://拔出耳机

//                        Log.d("bb--->>","耳机拔出，暂停");

                        if (playManager != null && playManager.isPlaying()) {

                            playManager.onPause();

                        }

                        break;

                    case AudioManager.ACTION_HEADSET_PLUG://插入耳机

//                        Log.d("cc--->>","耳机插入，播放");

//                        if (playManager != null && !playManager.isPlaying()) {
//
//                            playManager.onStart();
//
//                        }

                        break;
                    default:
                        break;
                }
            }
        }

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        MediaButtonReceiver.handleIntent(mediaSession, intent);


        return super.onStartCommand(intent, flags, startId);
    }
}
