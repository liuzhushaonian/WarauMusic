package com.app.legend.waraumusic.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.media.MediaMetadataCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.app.legend.waraumusic.utils.Conf;
import com.app.legend.waraumusic.utils.ImageLoader;
import com.app.legend.waraumusic.utils.JsonParseUtils;
import com.app.legend.waraumusic.utils.Mp3Util;
import com.app.legend.waraumusic.utils.NetUtils;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * 在此获取所有歌曲的封面、歌手以及台词
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class GetAlbumAndLrcIntentService extends IntentService {



    public GetAlbumAndLrcIntentService() {
        super("GetAlbumAndLrcIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            Glide.get(this).clearDiskCache();



            List<MediaMetadataCompat> metadataCompatList= Mp3Util.newInstance().getAllList();

            for (MediaMetadataCompat m:metadataCompatList){

                getData(m);//放入线程池内获取数据

            }

        }
    }

    private void getData(MediaMetadataCompat metadataCompat){

//        Observable
//                .create((ObservableOnSubscribe<Integer>) e -> {

                    String title=metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE);//获取歌曲名字

                    String artist=metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);//获取歌手名字

                    String album=metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);//获取专辑名字

                    long album_id=metadataCompat.getLong(Conf.ALBUM_ID);

                    long artist_id=metadataCompat.getLong(Conf.ARTIST_ID);

                    getAlbumBook(title,album_id);

                    getArtistBook(artist,artist_id);

                    getLrc(title);



//                })
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<Integer>() {
//
//                    Disposable disposable;
//
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        disposable=d;
//                    }
//
//                    @Override
//                    public void onNext(Integer integer) {
//
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        if (!disposable.isDisposed()){
//                            disposable.dispose();
//                        }
//                    }
//                });


    }

    //传入歌名
    private void getAlbumBook(String name,long id){

        String path= Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Conf.FILE_PATH+"/"+Conf.ALBUM_PATH+"/"+id;

        File file=new File(path);


//        String u= ImageLoader.getUrl(id);//查找本地数据库


        if (!file.exists()){//文件不存在，开启网络查找

            if (!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
            }

            String url="?type=search&s="+name;//搜索单曲

            String json= NetUtils.getJson(url);

            String book= JsonParseUtils.getAlbumBook(json);

            String p=Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Conf.FILE_PATH+"/"+Conf.ALBUM_PATH;

            NetUtils.getBitmap(book,id,p);//保存到本地

            //保存图片


        }


    }

    private void getArtistBook(String name,long id){

        String path=Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Conf.FILE_PATH+"/"+Conf.ARTIST_PATH+"/"+id;

        File file=new File(path);

        if (!file.exists()) {

            if (!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
            }

            String url = "?type=search&s=" + name + "&search_type=100";//搜索歌手

            String json = NetUtils.getJson(url);

            String book = JsonParseUtils.getArtistBook(json);

            String p=Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Conf.FILE_PATH+"/"+Conf.ARTIST_PATH;

            NetUtils.getBitmap(book,id,p);//保存图片

            //处理图片


        }

    }

    private void getLrc(String name){

        String path=Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Conf.FILE_PATH+"/"+Conf.LRC_PATH+"/"+name+".lrc";

        File file=new File(path);

        if (!file.exists()) {

            if (!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
            }

            String url = "?type=search&s=" + name;//首先获取歌曲的id

            String json = NetUtils.getJson(url);

            long id=JsonParseUtils.getSongId(json);

            if (id==-1){
                return;
            }

            String url2="/?type=lyric&id="+id;//用获取到的id来获取歌词

            String json2=NetUtils.getJson(url2);

            String[] lrcs = JsonParseUtils.getLrc(json2);

            for (int i=0;i<lrcs.length;i++){

                if (i==0){

                    String lrc1=Environment.getExternalStorageDirectory()
                            .getAbsolutePath()+"/"+Conf.FILE_PATH+"/"+Conf.LRC_PATH+"/"+name+".lrc";

                    String l1=lrcs[i];

                    if (l1==null){
                        return;
                    }

                    try {
                        FileOutputStream outputStream=new FileOutputStream(lrc1);

                        outputStream.write(l1.getBytes());

                        outputStream.flush();

                        outputStream.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }else {



                    String l2=lrcs[i];

                    if (l2==null){
                        return;
                    }

                    if (TextUtils.isEmpty(l2)||l2.equals("null")){

                        return;

                    }

                    //翻译歌词名称格式：t-xxx.lrc xxx为歌曲名字
                    String lrc2=Environment.getExternalStorageDirectory()
                            .getAbsolutePath()+"/"+Conf.FILE_PATH+"/"+Conf.LRC_PATH+"/"+"t-"+name+".lrc";


                    try {
                        FileOutputStream fileOutputStream=new FileOutputStream(lrc2);

                        fileOutputStream.write(l2.getBytes());

                        fileOutputStream.flush();

                        fileOutputStream.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            }

            //处理歌词

        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d("des----->>>","任务结束");

    }
}
