package com.app.legend.waraumusic.fragment.interfaces;

import android.view.View;

import com.app.legend.waraumusic.bean.Artist;

public interface OnArtistItemClickListener {

    void click(int position, Artist artist);

    void clickMenu(int position, Artist artist, View view);

}
