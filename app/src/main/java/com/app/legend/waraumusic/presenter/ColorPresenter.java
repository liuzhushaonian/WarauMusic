package com.app.legend.waraumusic.presenter;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;
import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.bean.Color;
import com.app.legend.waraumusic.presenter.interfaces.IColorActivity;
import com.app.legend.waraumusic.utils.WarauApp;
import java.util.ArrayList;
import java.util.List;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ColorPresenter extends BasePresenter<IColorActivity> {

    private IColorActivity activity;

    public ColorPresenter(IColorActivity activity) {

        attachView(activity);

        this.activity=getView();
    }


    public void getColorData(){

        Observable
                .create((ObservableOnSubscribe<List<Color>>) e -> {
                    List<Color> colors=getColorList();
                    e.onNext(colors);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Color>>() {
                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable=d;
                    }

                    @Override
                    public void onNext(List<Color> colors) {
                        setData(colors);
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

    private void setData(List<Color> colors){
        activity.setData(colors);
    }

    private List<Color> getColorList(){

        List<Color> colors=new ArrayList<>();

        Resources resources= WarauApp.getContext().getResources();

        XmlResourceParser xmlResourceParser=resources.getXml(R.xml.colors);

        try {
            while (xmlResourceParser.getEventType()!=XmlResourceParser.END_DOCUMENT){

                if (xmlResourceParser.getEventType()== XmlResourceParser.START_TAG){

                    String name=xmlResourceParser.getName();
                    if (name.equals("color")){

                        String n=xmlResourceParser.getAttributeValue(1);
                        int id= Integer.parseInt(xmlResourceParser.getAttributeValue(0));
                        String c=xmlResourceParser.nextText();
                        Log.d("id---->>",id+"");
                        int use=0;

                        int color_int= android.graphics.Color.parseColor(c);

                        Color color=new Color();
                        color.setColor(color_int);
                        color.setName(n);
                        color.setId(id);
                        color.setIs_use(use);

                        colors.add(color);
                    }
                }

                xmlResourceParser.next();

            }



        } catch (Exception e) {
            e.printStackTrace();
        }


        return colors;
    }

}
