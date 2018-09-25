package com.app.legend.waraumusic.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 *
 * Created by legend on 2018/2/12.
 */

public class PlayList implements Parcelable{

    private String name;

    private int id;

    private int length;



    public PlayList() {
    }


    protected PlayList(Parcel in) {
        name = in.readString();
        id = in.readInt();
        length = in.readInt();
    }

    public static final Creator<PlayList> CREATOR = new Creator<PlayList>() {
        @Override
        public PlayList createFromParcel(Parcel in) {
            return new PlayList(in);
        }

        @Override
        public PlayList[] newArray(int size) {
            return new PlayList[size];
        }
    };

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(id);
        dest.writeInt(length);
    }
}
