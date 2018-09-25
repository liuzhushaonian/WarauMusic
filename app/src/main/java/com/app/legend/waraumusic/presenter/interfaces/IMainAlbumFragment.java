package com.app.legend.waraumusic.presenter.interfaces;

import android.support.v4.media.session.MediaSessionCompat;

import com.app.legend.waraumusic.bean.Album;

import java.util.List;

public interface IMainAlbumFragment {

    void setData(List<Album> albums);

    void playAlbumMusic(List<MediaSessionCompat.QueueItem> queueItemList);

    void addToList(List<Integer> integerList);

}
