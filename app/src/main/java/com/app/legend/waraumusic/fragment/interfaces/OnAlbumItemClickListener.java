package com.app.legend.waraumusic.fragment.interfaces;

import android.view.View;
import android.widget.ImageView;

import com.app.legend.waraumusic.bean.Album;

public interface OnAlbumItemClickListener {

    void click(View view, int position, Album album);

    void clickMenu(View view, int position, Album album);
}
