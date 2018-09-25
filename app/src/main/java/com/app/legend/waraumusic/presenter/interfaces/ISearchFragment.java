package com.app.legend.waraumusic.presenter.interfaces;

import com.app.legend.waraumusic.bean.Album;
import com.app.legend.waraumusic.bean.Artist;
import com.app.legend.waraumusic.bean.Music;

import java.util.List;

/**
 *
 * Created by legend on 2018/2/14.
 */

public interface ISearchFragment {

    void setData(List<String> list);
    void queryDataByFragment(String data);
    void setMusicData(List<Music> musicList);

    void setArtistData(List<Artist> artistList);

    void setAlbumData(List<Album> albumList);

    void showInfo();

    void hideInfo();
}
