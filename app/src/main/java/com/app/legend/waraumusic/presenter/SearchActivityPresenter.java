package com.app.legend.waraumusic.presenter;

import com.app.legend.waraumusic.presenter.interfaces.ISearchActivity;
import com.app.legend.waraumusic.utils.Database;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SearchActivityPresenter extends BasePresenter<ISearchActivity> {

    private ISearchActivity activity;

    public SearchActivityPresenter(ISearchActivity activity) {
        attachView(activity);

        this.activity=getView();
    }

    /**
     * 获取历史记录
     */
    public void getHistory(){

        Observable
                .create((ObservableOnSubscribe<List<String>>) e -> {

                    List<String> his=Database.getDefault().getHistory();

                    e.onNext(his);
                    e.onComplete();

                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<String>>() {

                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable=d;
                    }

                    @Override
                    public void onNext(List<String> strings) {

                        activity.setData(strings);
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
