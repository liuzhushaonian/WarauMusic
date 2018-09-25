package com.app.legend.waraumusic.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.presenter.BasePresenter;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class BasePresenterFragment<V,T extends BasePresenter<V>> extends BaseFragment {

    protected T presenter;

    public BasePresenterFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter=createPresenter();

        presenter.attachView((V) this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }

    protected abstract T createPresenter();


}
