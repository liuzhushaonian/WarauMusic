package com.app.legend.waraumusic.presenter;

import android.support.v4.media.session.MediaSessionCompat;
import android.widget.Toast;

import com.app.legend.waraumusic.bean.Music;
import com.app.legend.waraumusic.bean.PlayList;
import com.app.legend.waraumusic.presenter.interfaces.IMainPlayListFragment;
import com.app.legend.waraumusic.utils.Conf;
import com.app.legend.waraumusic.utils.Database;
import com.app.legend.waraumusic.utils.Mp3Util;
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

public class MainPlayListFragmentPresenter extends BasePresenter<IMainPlayListFragment> {

    private IMainPlayListFragment fragment;

    public MainPlayListFragmentPresenter(IMainPlayListFragment fragment) {
        attachView(fragment);
        this.fragment=getView();
    }

    public void getData(){

        Observable
                .create((ObservableOnSubscribe<List<PlayList>>) e -> {

                    List<PlayList> playListList= Database.getDefault().getAllPlayLists();

                    e.onNext(playListList);
                    e.onComplete();

                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<PlayList>>() {

                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable=d;
                    }

                    @Override
                    public void onNext(List<PlayList> playLists) {
                        fragment.setData(playLists);
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

    public void playAllPlayListMusic(PlayList playList){

        Observable
                .create((ObservableOnSubscribe<List<MediaSessionCompat.QueueItem>>) e -> {

                    List<Integer> integerList= Database.getDefault().getListMusic(playList);

                    List<MediaSessionCompat.QueueItem> queueItemList=new ArrayList<>();

                    for (Integer integer:integerList){

                        Music music= Mp3Util.newInstance().getMusicById(integer);

                        if (music!=null){

                            long id=music.getMediaMetadataCompat().getLong(Conf.UNIQUE_ID);

                            MediaSessionCompat.QueueItem queueItem=new MediaSessionCompat.QueueItem(music.getMediaMetadataCompat().getDescription(),id);

                            queueItemList.add(queueItem);
                        }

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
                        fragment.playAll(queueItemList);
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

    public void deleteList(PlayList playList,int position){

        Observable
                .create((ObservableOnSubscribe<Integer>) e -> {

                    List<Integer> integerList=Database.getDefault().getListMusic(playList);

                    Database.getDefault().deletePlayList(playList);

                    for (int i=0;i<integerList.size();i++) {

                        Database.getDefault().deleteMusicFromList(playList, integerList.get(i));
                    }

                    e.onNext(1);

                    e.onComplete();

                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {

                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable=d;
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Toast.makeText(WarauApp.getContext(), "删除成功", Toast.LENGTH_SHORT).show();
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
