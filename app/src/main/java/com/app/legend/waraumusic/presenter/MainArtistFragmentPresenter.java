package com.app.legend.waraumusic.presenter;

import android.support.v4.media.session.MediaSessionCompat;

import com.app.legend.waraumusic.bean.Artist;
import com.app.legend.waraumusic.bean.Music;
import com.app.legend.waraumusic.presenter.interfaces.IMainArtistFragment;
import com.app.legend.waraumusic.utils.Conf;
import com.app.legend.waraumusic.utils.Mp3Util;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainArtistFragmentPresenter extends BasePresenter<IMainArtistFragment> {

    private IMainArtistFragment fragment;

    public MainArtistFragmentPresenter(IMainArtistFragment fragment) {
        attachView(fragment);
        this.fragment=getView();
    }

    public void getData(){

        Observable
                .create((ObservableOnSubscribe<List<Artist>>) e -> {

                    List<Artist> artists= Mp3Util.newInstance().getArtistSet();

                    e.onNext(artists);

                    e.onComplete();


                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Artist>>() {

                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable=d;
                    }

                    @Override
                    public void onNext(List<Artist> artists) {
                        fragment.setData(artists);
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


    public void playArtistMusic(Artist artist){

        Observable
                .create((ObservableOnSubscribe<List<MediaSessionCompat.QueueItem>>) e -> {

                    List<Music> musicList=Mp3Util.newInstance().getArtistMusic(artist);

                    List<MediaSessionCompat.QueueItem> queueItemList=new ArrayList<>();

                    for (Music music:musicList){

                        long id=music.getMediaMetadataCompat().getLong(Conf.UNIQUE_ID);

                        MediaSessionCompat.QueueItem item=new MediaSessionCompat.QueueItem(music.getMediaMetadataCompat().getDescription(),id);

                        queueItemList.add(item);

                    }

                    e.onNext(queueItemList);

                    e.onComplete();


                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<MediaSessionCompat.QueueItem>>() {

                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable=d;
                    }

                    @Override
                    public void onNext(List<MediaSessionCompat.QueueItem> queueItemList) {
                        fragment.playArtistMusic(queueItemList);
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

    public void addList(Artist artist){

        Observable
                .create((ObservableOnSubscribe<List<Integer>>) e -> {

                    List<Integer> integerList=Mp3Util.newInstance().getArtistMusicId(artist);

                    e.onNext(integerList);

                    e.onComplete();

                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Integer>>() {

                    Disposable disposable;
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable=d;

                    }

                    @Override
                    public void onNext(List<Integer> integers) {
                        fragment.addListMusic(integers);
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
