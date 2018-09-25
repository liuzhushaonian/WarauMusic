package com.app.legend.waraumusic.presenter;

import android.graphics.Bitmap;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.Toast;

import com.app.legend.waraumusic.bean.Album;
import com.app.legend.waraumusic.bean.Music;
import com.app.legend.waraumusic.presenter.interfaces.IMainAlbumFragment;
import com.app.legend.waraumusic.utils.Conf;
import com.app.legend.waraumusic.utils.ImageLoader;
import com.app.legend.waraumusic.utils.Mp3Util;
import com.app.legend.waraumusic.utils.WarauApp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainAlbumFragmentPresenter extends BasePresenter<IMainAlbumFragment> {

    private IMainAlbumFragment fragment;

    public MainAlbumFragmentPresenter(IMainAlbumFragment fragment) {
        attachView(fragment);
        this.fragment=getView();
    }

    public void getData(){

        Observable
                .create((ObservableOnSubscribe<List<Album>>) e -> {

                    List<Album> albumList= Mp3Util.newInstance().getAllAlbumList();

                    e.onNext(albumList);

                    e.onComplete();

                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Album>>() {

                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable=d;
                    }

                    @Override
                    public void onNext(List<Album> albums) {
                        fragment.setData(albums);
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

    public void playAlbumMusic(Album album){

        Observable
                .create((ObservableOnSubscribe<List<MediaSessionCompat.QueueItem>>) e -> {

                    List<Music> musicList=Mp3Util.newInstance().getAlbumMusicList(album);

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
                        fragment.playAlbumMusic(queueItemList);
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

    public void addAllMusicToList(Album album){

        Observable
                .create((ObservableOnSubscribe<List<Integer>>) e -> {

                    List<Integer> integers=Mp3Util.newInstance().getAlbumMusicId(album);

                    e.onNext(integers);
                    e.onComplete();

                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Integer>>() {

                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable=d;
                    }

                    @Override
                    public void onNext(List<Integer> integers) {

                        fragment.addToList(integers);

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

    public void saveBitmap(Album album){

        Observable
                .create((ObservableOnSubscribe<Integer>) e -> {
                    Bitmap bitmap= ImageLoader.getImageLoader(WarauApp.getContext()).getBitmap(album.getId());

                    if (bitmap!=null){

                        String path= Environment.getExternalStorageDirectory()+"/"+Conf.FILE_PATH+"/albums";

                        saveBitmap(bitmap,path);

                        e.onNext(1);
                    }else {

                        e.onNext(-1);
                    }

                    e.onComplete();

                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        if (integer>0){

                            Toast.makeText(WarauApp.getContext(), "保存在 sdcard/"+Conf.FILE_PATH+"/albums 下了", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });


    }


    private void saveBitmap(Bitmap bitmap,String path) {
        FileOutputStream fileOutputStream = null;

        try {
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            File file1 = new File(file, SystemClock.currentThreadTimeMillis()+".jpg");

            fileOutputStream = new FileOutputStream(file1);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
