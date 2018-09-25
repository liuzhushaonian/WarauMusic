package com.app.legend.waraumusic.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.presenter.BasePresenter;
import com.app.legend.waraumusic.utils.Conf;
import com.app.legend.waraumusic.utils.SlideHelper;


public abstract class BasePresenterActivity<V,T extends BasePresenter<V>> extends BaseActivity {


    protected T presenter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter=createPresenter();
        presenter.attachView((V) this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        presenter.detachView();
    }

    protected abstract T createPresenter();




}
