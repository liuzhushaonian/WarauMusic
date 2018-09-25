package com.app.legend.waraumusic.presenter.interfaces;

import android.graphics.Bitmap;

import com.app.legend.waraumusic.bean.Music;
import com.app.legend.waraumusic.bean.PlayList;

import java.util.List;

public interface IPlayListFragment {

    void setData(Music music);


    void setImage(String bitmap);

    void deleteCallback(int position);

}
