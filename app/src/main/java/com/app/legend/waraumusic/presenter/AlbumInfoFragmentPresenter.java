package com.app.legend.waraumusic.presenter;

import android.util.Log;

import com.app.legend.waraumusic.bean.Album;
import com.app.legend.waraumusic.bean.Music;
import com.app.legend.waraumusic.presenter.interfaces.IAlbumInfoFragment;
import com.app.legend.waraumusic.utils.Mp3Util;
import java.util.List;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AlbumInfoFragmentPresenter extends BasePresenter<IAlbumInfoFragment> {

    private IAlbumInfoFragment fragment;

    public AlbumInfoFragmentPresenter(IAlbumInfoFragment fragment) {
        attachView(fragment);

        this.fragment=getView();
    }

    public void getData(Album album){

        if (album==null){


            return;
        }

        Observable
                .create((ObservableOnSubscribe<List<Music>>) e -> {

                    List<Music> musicList= Mp3Util.newInstance().getAlbumMusicList(album);



                    e.onNext(musicList);
                    e.onComplete();

                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Music>>() {

                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable=d;
                    }

                    @Override
                    public void onNext(List<Music> music) {
                        fragment.setData(music);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        if (!disposable.isDisposed()){
                            disposable.dispose();
                        }
                    }
                });

    }

}
