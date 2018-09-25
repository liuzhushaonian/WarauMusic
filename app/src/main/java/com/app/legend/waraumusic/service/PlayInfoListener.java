package com.app.legend.waraumusic.service;

import android.support.v4.media.session.PlaybackStateCompat;

public abstract class PlayInfoListener {


    public abstract void setPlayState(PlaybackStateCompat playState);

    /**
     * 歌曲播放完成后进行回调
     */
    public abstract void onComplete();

    public abstract void showNotification(PlaybackStateCompat playState);


}
