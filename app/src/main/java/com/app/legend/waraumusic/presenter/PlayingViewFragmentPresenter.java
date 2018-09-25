package com.app.legend.waraumusic.presenter;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import com.app.legend.waraumusic.bean.Music;
import com.app.legend.waraumusic.presenter.interfaces.IPlayingViewFragment;
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

public class PlayingViewFragmentPresenter extends BasePresenter<IPlayingViewFragment> {

    private IPlayingViewFragment fragment;

    public PlayingViewFragmentPresenter(IPlayingViewFragment fragment) {
        attachView(fragment);
        this.fragment=getView();
    }

    /**
     * 处理获取到的播放列表，将之转为MediaMetadataCompat
     * @param queueItemList
     */
    public void getPlayingListData(List<MediaSessionCompat.QueueItem> queueItemList){

        Observable
                .create((ObservableOnSubscribe<List<MediaMetadataCompat>>) e -> {

                    List<MediaMetadataCompat> mediaMetadataCompats=new ArrayList<>();

                    for (MediaSessionCompat.QueueItem item:queueItemList){

                        mediaMetadataCompats.add(Mp3Util.newInstance().getMediaMetadataCompatById(item.getDescription().getMediaId()));

                    }

                    e.onNext(mediaMetadataCompats);
                    e.onComplete();

                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<MediaMetadataCompat>>() {

                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable=d;
                    }

                    @Override
                    public void onNext(List<MediaMetadataCompat> metadataCompatList) {

                        fragment.setPlayingData(metadataCompatList);

                        List<Music> musicList=new ArrayList<>();

                        for (MediaMetadataCompat metadataCompat:metadataCompatList){

                            Music music=new Music();

                            music.setMediaMetadataCompat(metadataCompat);

                            musicList.add(music);

                        }

                        fragment.setPlayingList(musicList);

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

    /**
     * 根据当前播放的音乐获取它的具体position并通知界面及时更新
     * @param metadataCompat 当前播放的音乐
     * @param metadataCompatList 当前音乐列表
     */
    public void getCurrentPositionAndUpdate(MediaMetadataCompat metadataCompat,List<MediaSessionCompat.QueueItem> metadataCompatList){


        Observable
                .create((ObservableOnSubscribe<Integer>) e -> {

                    for (int i=0;i<metadataCompatList.size();i++){

                        MediaSessionCompat.QueueItem queueItem=metadataCompatList.get(i);

                        assert queueItem.getDescription().getMediaId() != null;
                        if (queueItem.getQueueId()==metadataCompat.getLong("uniqueId")){



                            e.onNext(i);
                            e.onComplete();

                        }

                    }

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

                        Log.d("iiiii------>>>",integer+"");

                        fragment.setPosition(integer);
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
