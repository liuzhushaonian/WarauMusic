package com.app.legend.waraumusic.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.presenter.SplashPresenter;
import com.app.legend.waraumusic.presenter.interfaces.ISplashActivity;
import com.app.legend.waraumusic.service.PlayService;
import com.app.legend.waraumusic.utils.Mp3Util;


/**
 * 首页，获取权限
 */
public class SplashActivity extends BasePresenterActivity<ISplashActivity,SplashPresenter> implements ISplashActivity {

    private static final String[] permissionStrings=
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getPermission();

    }

    @Override
    void autoSetColor() {

    }

    @Override
    protected SplashPresenter createPresenter() {
        return new SplashPresenter(this);
    }

    private void getPermission(){
        if (ContextCompat.checkSelfPermission(SplashActivity.this, permissionStrings[0])!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{permissionStrings[0]},1000);



        }else {

//            startMainActivity();



            mp3Util.scanMusic();//扫描所有音乐

            startCountDown();

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case 1000:
                if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){


                    mp3Util.scanMusic();

                    startCountDown();

//                    startMainActivity();
                }else {

                    Toast.makeText(SplashActivity.this,"无法获取权限，请赋予相关权限",Toast.LENGTH_SHORT).show();
                }

                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }


    }

    private void startCountDown() {


        new Thread() {
            @Override
            public void run() {
                super.run();

                int i = 0;

                boolean canGo=false;

                while (!canGo) {

                    try {
                        sleep(1000);
                        i += 1;

                        Message message = new Message();
                        message.what = 100;
                        message.arg1 = i;

                        handler.handleMessage(message);
                        if (i >= 2) {
                            canGo = true;
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();

    }


    private Handler handler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 100:

                    int t = msg.arg1;

                    if (t == 2) {

                        startMainActivity();
                    }

                    break;
            }

        }
    };

    private void startMainActivity(){


        Intent intent=new Intent(this,MainActivity.class);

        startActivity(intent);

        finish();

        overridePendingTransition(0,R.anim.exit);

    }



}
