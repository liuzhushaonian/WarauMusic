package com.app.legend.waraumusic.presenter.interfaces;

import android.support.v4.media.MediaMetadataCompat;

import com.app.legend.waraumusic.bean.Music;

import java.util.List;

public interface IPlayingViewFragment {

    void setPlayingData(List<MediaMetadataCompat> metadataCompatList);


    void setPosition(int position);

    void setPlayingList(List<Music> musicList);

}
