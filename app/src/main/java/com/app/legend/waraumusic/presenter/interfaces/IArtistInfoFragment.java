package com.app.legend.waraumusic.presenter.interfaces;

import android.support.v4.media.session.MediaSessionCompat;

import com.app.legend.waraumusic.bean.Album;
import com.app.legend.waraumusic.bean.Music;

import java.util.List;

public interface IArtistInfoFragment {

    void setAlbumData(List<Album> albumList);

    void setMusicData(List<Music> musicList);

    void playAlbumMusic(List<MediaSessionCompat.QueueItem> queueItemList);

    void addListMusic(List<Integer> integers);

}
