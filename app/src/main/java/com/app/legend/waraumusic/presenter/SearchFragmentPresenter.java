package com.app.legend.waraumusic.presenter;

import com.app.legend.waraumusic.bean.Album;
import com.app.legend.waraumusic.bean.Artist;
import com.app.legend.waraumusic.bean.Music;
import com.app.legend.waraumusic.presenter.interfaces.ISearchFragment;
import com.app.legend.waraumusic.utils.Mp3Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 *
 * Created by legend on 2018/2/15.
 */

public class SearchFragmentPresenter extends BasePresenter<ISearchFragment> {
    private ISearchFragment fragment;

    public SearchFragmentPresenter(ISearchFragment fragment) {
        attachView(fragment);

        this.fragment=getView();
    }

    public void getData(String string){
        getAllData(string);
    }



    private void getAllData(String string){
        
        Observable
                .create((ObservableOnSubscribe<Map>) e -> {


                    List<Music> musicList= Mp3Util.newInstance().getSearchMusic(string);

                    List<Album> albumList=Mp3Util.newInstance().getSearchAlbum(string);

                    List<Artist> artistList=Mp3Util.newInstance().getSearchArtist(string);

                    Map<String,List> map=new HashMap<>();

                    map.put("music",musicList);
                    map.put("album",albumList);
                    map.put("artist",artistList);

                    e.onNext(map);

                    e.onComplete();

                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Map>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Map map) {

                        showOrHide(map);

                        List<Music> musicList= (List<Music>) map.get("music");

                        List<Album> albumList= (List<Album>) map.get("album");

                        List<Artist> artistList= (List<Artist>) map.get("artist");




                        fragment.setMusicData(musicList);
                        fragment.setAlbumData(albumList);

                        fragment.setArtistData(artistList);

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
        
        
    }



    private void setMusicData(List<Music> musicList){
        fragment.setMusicData(musicList);
    }

    private void setArtistData(List<Artist> artistList){
        fragment.setArtistData(artistList);
    }


    private void setAlbumData(List<Album> albumList){
        fragment.setAlbumData(albumList);
    }

    private void showInfo(){
        fragment.showInfo();
    }

    private void hideInfo(){
        fragment.hideInfo();
    }

    private void showOrHide(Map map){

        List<Music> musicList= (List<Music>) map.get("music");

        List<Album> albumList= (List<Album>) map.get("album");

        List<Artist> artistList= (List<Artist>) map.get("artist");

        if (musicList==null&&albumList==null&&artistList==null){

            showInfo();


        }else if (musicList!=null&&musicList.isEmpty()&&albumList!=null&&albumList.isEmpty()&&artistList!=null&&artistList.isEmpty()){
            showInfo();
        }else {

            hideInfo();
        }

    }


}
