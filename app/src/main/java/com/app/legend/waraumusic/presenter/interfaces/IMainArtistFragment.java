package com.app.legend.waraumusic.presenter.interfaces;

import android.support.v4.media.session.MediaSessionCompat;

import com.app.legend.waraumusic.bean.Artist;

import java.util.List;

public interface IMainArtistFragment {

    void setData(List<Artist> artists);

    void playArtistMusic(List<MediaSessionCompat.QueueItem> queueItemList);

    void addListMusic(List<Integer> integers);
}
