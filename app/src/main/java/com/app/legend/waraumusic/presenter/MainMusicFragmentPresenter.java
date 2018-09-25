package com.app.legend.waraumusic.presenter;

import android.support.v4.media.MediaMetadataCompat;

import com.app.legend.waraumusic.bean.Music;
import com.app.legend.waraumusic.presenter.interfaces.IMainMusicFragment;
import com.app.legend.waraumusic.utils.WarauApp;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainMusicFragmentPresenter extends BasePresenter<IMainMusicFragment> {

    private IMainMusicFragment fragment;


    public MainMusicFragmentPresenter(IMainMusicFragment fragment) {


        attachView(fragment);

        this.fragment=getView();


    }

    /**
     * 获取所有音乐
     */
    public void getAllMusic(){

        Observable
                .create((ObservableOnSubscribe<List<Music>>) e -> {

                    List<MediaMetadataCompat> music=mp3Util.getAllList();

                    List<Music> musicList=new ArrayList<>();

                    for (MediaMetadataCompat compat:music){

                        Music music1=new Music();

                        music1.setMediaMetadataCompat(compat);

                        musicList.add(music1);

                    }

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
