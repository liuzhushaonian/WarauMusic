package com.app.legend.waraumusic.service.play;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.app.legend.waraumusic.service.PlayInfoListener;

import java.io.IOException;

/**
 * 播放助手，只负责播放暂停等mediaplayer的业务
 */
public class PlayManager {

    private MediaPlayer mediaPlayer;

    private Context context;

    private MediaMetadataCompat mediaMetadataCompat;//当前播放的音乐

    private int state;

    private PlayInfoListener playInfoListener;

    public MediaMetadataCompat getMediaMetadataCompat() {
        return mediaMetadataCompat;
    }

    public PlayManager(Context context, PlayInfoListener playInfoListener) {
        this.context = context.getApplicationContext();

        this.playInfoListener = playInfoListener;

    }


    private void initPlayer() {

        if (this.mediaPlayer == null) {

            mediaPlayer = new MediaPlayer();

            //准备完成
            mediaPlayer.setOnPreparedListener(mp -> {

                mp.start();

                setNewState(PlaybackStateCompat.STATE_PLAYING);

            });

            //播放完成
            mediaPlayer.setOnCompletionListener(mp -> {
                playInfoListener.onComplete();

            });

        }


    }


    public void release() {

        if (mediaPlayer != null) {

            mediaPlayer.stop();
            mediaPlayer.release();
        }

    }

    /**
     * 播放MetaData
     *
     * @param metadata MetaData
     */
    public void playMetaData(MediaMetadataCompat metadata) {

        if (metadata == null) {
            return;
        }

        if (this.mediaMetadataCompat != null && metadata.equals(this.mediaMetadataCompat)) {

            if (isPlaying()) {//不等于null且并未在播放，则继续播放

                onPause();

                return;//返回，不做后面的处理
            } else {

                onStart();

                return;

            }

        }

        this.mediaMetadataCompat = metadata;//保存当前需要播放的data

        initPlayer();//初始化播放器

        String url = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);

        try {

            mediaPlayer.stop();

            mediaPlayer.reset();

            mediaPlayer.setDataSource(url);

            mediaPlayer.prepareAsync();


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void onStart() {

        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {

            mediaPlayer.start();

            setNewState(PlaybackStateCompat.STATE_PLAYING);
        }

    }

    public void onPause() {

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {

            mediaPlayer.pause();



//            Log.d("send------>>>","send the pause");
        }

        setNewState(PlaybackStateCompat.STATE_PAUSED);

    }


    public void onStop() {

        if (mediaPlayer != null) {

            mediaPlayer.stop();

            setNewState(PlaybackStateCompat.STATE_PAUSED);

        }

    }

    public void onSeek(long position) {

        if (mediaPlayer != null) {

            mediaPlayer.seekTo((int) position);

            setNewState(state);

        }

    }

    public void setVolume(float volume) {

        if (mediaPlayer != null) {

            mediaPlayer.setVolume(volume, volume);

        }
    }


    public boolean isPlaying() {

        return mediaPlayer != null && mediaPlayer.isPlaying();

    }


    private void setNewState(@PlaybackStateCompat.State int newPlayerState) {

        this.state = newPlayerState;

        long position = mediaPlayer == null ? 0 : mediaPlayer.getCurrentPosition();

        PlaybackStateCompat.Builder builder = new PlaybackStateCompat.Builder();

        builder.setState(state, position, 1.0f, SystemClock.currentThreadTimeMillis());

//        playInfoListener.setPlayState(builder.build());

        this.playInfoListener.setPlayState(builder.build());

        this.playInfoListener.showNotification(builder.build());


        /**
         * 开线程不断发送播放状态，主要是当前播放事件
         */
        new Thread(){
            @Override
            public void run() {
                super.run();

                while (isPlaying()){//循环条件是是否在播放，如果不是，则线程会正常结束

                    try {
                        sleep(500);

                        handler.sendEmptyMessage(20);//切换到主线程然后发送

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }
        }.start();



    }


    //主线程发送播放进度，避免线程错乱导致崩溃
    private Handler handler=new Handler(Looper.getMainLooper()){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){

                case 20:

                    long position = mediaPlayer == null ? 0 : mediaPlayer.getCurrentPosition();

                    PlaybackStateCompat.Builder builder = new PlaybackStateCompat.Builder();

                    builder.setState(state, position, 1.0f, SystemClock.currentThreadTimeMillis());


                    playInfoListener.setPlayState(builder.build());


                    break;

            }

        }
    };



}
