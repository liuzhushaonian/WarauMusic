package com.app.legend.waraumusic.presenter;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.presenter.interfaces.IMainActivity;
import com.app.legend.waraumusic.utils.Conf;
import com.app.legend.waraumusic.utils.Mp3Util;
import com.app.legend.waraumusic.utils.NetUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainPresenter extends BasePresenter<IMainActivity> {

    private IMainActivity activity;

    public MainPresenter(IMainActivity activity) {
        attachView(activity);
        this.activity=getView();
    }


    public void playAllMusic(){

        Observable
                .create((ObservableOnSubscribe<List<MediaSessionCompat.QueueItem>>) e -> {
                    List<MediaSessionCompat.QueueItem> queueItemList=new ArrayList<>();

                    List<MediaMetadataCompat> mediaMetadataCompats=Mp3Util.newInstance().getAllList();

                    for (MediaMetadataCompat compat:mediaMetadataCompats){

                        long id=compat.getLong(Conf.UNIQUE_ID);

                        MediaSessionCompat.QueueItem item=new MediaSessionCompat.QueueItem(compat.getDescription(),id);

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

                        activity.playAllMusic(queueItemList);
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

    public void showAbout(Activity activity){

        AlertDialog.Builder builder=new AlertDialog.Builder(activity);


        PackageInfo pi = null;
        try {
            pi = activity.getPackageManager().getPackageInfo(activity.getPackageName(),0);

            String versionName = pi.versionName;
//            String versioncode = pi.versionCode;

            String info="当前版本:"+versionName;

            builder.setTitle("关于").setMessage(activity.getString(R.string.about_content)+info).show();

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


    }



    public void getNetWork(Activity a){

        int state= NetUtils.getAPNType();

        if (state==-1){

            Toast.makeText(a, "未连接网络", Toast.LENGTH_SHORT).show();

            return;
        }

        AlertDialog.Builder builder1=new AlertDialog.Builder(a);


        if (state!=2){//非WiFi状态

            AlertDialog.Builder builder=new AlertDialog.Builder(a);

            builder.setTitle("警告").setMessage("当前网络为移动网络，将会耗费大量流量").setPositiveButton("流量足够,干！",(dialog, which) -> {

                activity.startGetInfos();


            }).setNegativeButton("取消",(dialog, which) -> {

                builder.create().cancel();

            }).show();

        }else {

            activity.startGetInfos();

        }

    }

    


}
