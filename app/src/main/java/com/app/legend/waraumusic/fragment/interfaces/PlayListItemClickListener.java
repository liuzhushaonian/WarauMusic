package com.app.legend.waraumusic.fragment.interfaces;

import android.view.View;

import com.app.legend.waraumusic.bean.PlayList;

public interface PlayListItemClickListener {

    void click(int position, PlayList list);

    void clickMenu(int position, PlayList playList, View view);
}
