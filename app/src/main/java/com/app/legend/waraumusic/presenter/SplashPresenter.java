package com.app.legend.waraumusic.presenter;

import com.app.legend.waraumusic.presenter.interfaces.ISplashActivity;

public class SplashPresenter extends BasePresenter<ISplashActivity> {

    private ISplashActivity activity;

    public SplashPresenter(ISplashActivity activity) {
        attachView(activity);
        this.activity=getView();
    }
}
