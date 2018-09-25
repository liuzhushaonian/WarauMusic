package com.app.legend.waraumusic.fragment.interfaces;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import java.util.List;

public interface IMusicOnClickListener {

    void click(int position, List<MediaSessionCompat.QueueItem> metadataCompatList);

}
