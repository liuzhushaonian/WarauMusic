package com.app.legend.waraumusic.presenter.interfaces;

import android.support.v4.media.session.MediaSessionCompat;

import java.util.List;

public interface IMainActivity {

    void playAllMusic(List<MediaSessionCompat.QueueItem> queueItemList);

    void startGetInfos();

}
