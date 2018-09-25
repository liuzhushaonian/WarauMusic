package com.app.legend.waraumusic.presenter.interfaces;

import android.support.v4.media.session.MediaSessionCompat;

import com.app.legend.waraumusic.bean.PlayList;

import java.util.List;

public interface IMainPlayListFragment {


    void setData(List<PlayList> playLists);

    void playAll(List<MediaSessionCompat.QueueItem> queueItemList);

}
