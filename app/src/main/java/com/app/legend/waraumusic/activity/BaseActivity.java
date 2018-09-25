package com.app.legend.waraumusic.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.service.PlayService;
import com.app.legend.waraumusic.utils.Conf;
import com.app.legend.waraumusic.utils.Mp3Util;
import com.app.legend.waraumusic.utils.SlideHelper;

import java.util.ArrayList;
import java.util.List;

import static com.app.legend.waraumusic.utils.Conf.COLOR;

public abstract class BaseActivity extends AppCompatActivity {

    protected Toolbar toolbar;
    protected SharedPreferences sharedPreferences;
    protected SlideHelper slideHelper;
    protected Mp3Util mp3Util;


    protected MediaBrowserCompat mediaBrowserCompat;

    protected MediaControllerCompat mediaControllerCompat;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        sharedPreferences=getSharedPreferences(Conf.SHARE_NAME,MODE_PRIVATE);

        slideHelper=SlideHelper.getInstance();

        mp3Util=Mp3Util.newInstance();

        initBrowser();

    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaBrowserCompat.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mediaBrowserCompat.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        autoChangeColor(toolbar);//自动获取颜色并设置上
    }




    protected void autoChangeColor(Toolbar toolbar){

        int defaultColor=getResources().getColor(R.color.colorTeal);

        int color=sharedPreferences.getInt(COLOR,defaultColor);

        if (toolbar!=null) {
            toolbar.setBackgroundColor(color);
        }

    }

    protected void saveColor(int color){

        sharedPreferences.edit().putInt(COLOR,color).apply();

    }

    protected int getThemeColor(){

        int defaultColor=getResources().getColor(R.color.colorTeal);

        return sharedPreferences.getInt(COLOR,defaultColor);
    }

    protected void startCropImage(Uri uri, int w, int h, int code) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        //设置数据uri和类型为图片类型
        intent.setDataAndType(uri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //显示View为可裁剪的
        intent.putExtra("crop", true);
        //裁剪的宽高的比例为1:1
        intent.putExtra("aspectX", w);
        intent.putExtra("aspectY", h);
        //输出图片的宽高均为150
        intent.putExtra("outputX", w);
        intent.putExtra("outputY", h);

        //裁剪之后的数据是通过Intent返回
        intent.putExtra("return-data", false);

        intent.putExtra("outImage", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection",true);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent, code);
    }


    private void initBrowser() {

        this.mediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(this, PlayService.class),
                connectionCallback, null);

    }

    //连接回调，处理是否连接成功
    private MediaBrowserCompat.ConnectionCallback connectionCallback = new MediaBrowserCompat.ConnectionCallback() {


        @Override
        public void onConnected() {
            super.onConnected();

            if (mediaBrowserCompat.isConnected()) {

                try {
                    mediaControllerCompat = new MediaControllerCompat(BaseActivity.this,mediaBrowserCompat.getSessionToken());

//                    mediaControllerCompat.registerCallback();


                } catch (RemoteException e) {
                    e.printStackTrace();
                }


                String id = mediaBrowserCompat.getRoot();

                mediaBrowserCompat.unsubscribe(id);

                mediaBrowserCompat.subscribe(id, subscriptionCallback);

            }

        }

        @Override
        public void onConnectionFailed() {
            super.onConnectionFailed();
            Log.w("onConnection--->>", "Failed");

        }
    };

    //订阅回调，获取检索到的数据
    private MediaBrowserCompat.SubscriptionCallback subscriptionCallback = new MediaBrowserCompat.SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);


            Log.d("onChildrenLoaded---->>", children.toString());


        }
    };

    //播放列表
    protected void play(int position,List<MediaSessionCompat.QueueItem> metadataCompatList){

        Bundle bundle=new Bundle();

        bundle.putParcelableArrayList("list", (ArrayList<? extends Parcelable>) metadataCompatList);

        bundle.putInt("index",position);

        mediaControllerCompat.getTransportControls().sendCustomAction(Conf.UPDATE_LIST_AND_PLAY,bundle);

    }

    protected void saveThemeColor(int color){

        getSharedPreferences(Conf.SHARE_NAME,MODE_PRIVATE).edit().putInt(COLOR,color).apply();

    }


    abstract void autoSetColor();

}
