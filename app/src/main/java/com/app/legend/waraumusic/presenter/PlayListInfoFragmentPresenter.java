package com.app.legend.waraumusic.presenter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import com.app.legend.waraumusic.bean.Music;
import com.app.legend.waraumusic.bean.PlayList;
import com.app.legend.waraumusic.presenter.interfaces.IPlayListFragment;
import com.app.legend.waraumusic.utils.Conf;
import com.app.legend.waraumusic.utils.Database;
import com.app.legend.waraumusic.utils.ImageLoader;
import com.app.legend.waraumusic.utils.Mp3Util;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PlayListInfoFragmentPresenter extends BasePresenter<IPlayListFragment> {

    private IPlayListFragment fragment;

    public PlayListInfoFragmentPresenter(IPlayListFragment fragment) {
        attachView(fragment);

        this.fragment=getView();
    }

    public void getData(PlayList playList){

        if (playList==null){
            return;
        }

        Observable
                .create((ObservableOnSubscribe<Music>) e -> {

                    List<Integer> integerList= Database.getDefault().getListMusic(playList);

                    for (Integer integer:integerList){

                        Music music= Mp3Util.newInstance().getMusicById(integer);

                        if (music!=null){

                            Log.d("music------>>>",""+music.toString());

                            e.onNext(music);
                        }

                    }

                    e.onComplete();


                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Music>() {

                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable=d;
                    }

                    @Override
                    public void onNext(Music music) {
                        fragment.setData(music);

                        if (!hasImage){
                            setBitmap(music);
                        }

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

    private boolean hasImage=false;

    private void setBitmap(Music music){

        long id=music.getMediaMetadataCompat().getLong(Conf.ALBUM_ID);

        String url= ImageLoader.getUrl(id);

        if (url!=null&&!hasImage){

            fragment.setImage(url);

            hasImage=true;

        }


    }

    public void deleteMusic(MediaMetadataCompat music,PlayList playList,int position){

        Observable
                .create((ObservableOnSubscribe<Integer>) e -> {

                    int id= Integer.parseInt(music.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));

                    int s=Database.getDefault().deleteMusicFromList(playList,id);

                    e.onNext(s);

                    e.onComplete();

                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {

                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable=d;
                    }

                    @Override
                    public void onNext(Integer integer) {

                        fragment.deleteCallback(position);
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
