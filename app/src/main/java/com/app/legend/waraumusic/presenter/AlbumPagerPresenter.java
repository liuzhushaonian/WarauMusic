package com.app.legend.waraumusic.presenter;

import android.support.v4.media.MediaMetadataCompat;
import android.text.TextUtils;
import android.util.Log;

import com.app.legend.waraumusic.bean.Lrc;
import com.app.legend.waraumusic.presenter.interfaces.IAlbumPagerFragment;
import com.app.legend.waraumusic.utils.LyricManager;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AlbumPagerPresenter extends BasePresenter<IAlbumPagerFragment> {

    private IAlbumPagerFragment fragment;
    private static final int L=11;

    private static final int T=22;

    public AlbumPagerPresenter(IAlbumPagerFragment fragment) {


        attachView(fragment);

        this.fragment = getView();

    }


    public void getLrcData(MediaMetadataCompat metadataCompat){

        Observable
                .create((ObservableOnSubscribe<String[]>) e -> {

                    String[] lrc= LyricManager.getManager().getLrc(metadataCompat);//获取歌词

                    if (lrc!=null){

                        e.onNext(lrc);
                    }

                    e.onComplete();


                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String[]>() {

                    Disposable disposable;
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable=d;
                    }

                    @Override
                    public void onNext(String[] lrcs) {

                        for (int i=0;i<lrcs.length;i++){

                            String l=lrcs[i];

                            if (i==0){

                                if (l!=null){

                                    getLrc(l,L);

                                }

                            }

                            if (i==1){//翻译歌词

                                if (l!=null&&!TextUtils.isEmpty(l)&&!l.equals("null")){

                                    getLrc(l,T);

                                }

                            }


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


    private void getLrc(String lrc,int type){

        Observable
                .create((ObservableOnSubscribe<List<Lrc>>) e -> {

                    List<Lrc> lrcList=LyricManager.getManager().parseLrc(lrc);

                    if (lrcList!=null){

                        e.onNext(lrcList);

                    }

                    e.onComplete();


                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Lrc>>() {

                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable=d;
                    }

                    @Override
                    public void onNext(List<Lrc> lrcs) {

                        switch (type){

                            case L:

                                fragment.setLLrc(lrcs);

                                break;

                            case T:

                                fragment.setTLrc(lrcs);

                                break;


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



}
