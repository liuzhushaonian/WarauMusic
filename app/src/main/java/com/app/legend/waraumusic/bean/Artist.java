package com.app.legend.waraumusic.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 *
 * Created by legend on 2018/2/6.
 */

public class Artist implements Parcelable{

    private long id;
    private String name;
    private String album;

    public Artist() {
    }

    protected Artist(Parcel in) {
        id = in.readLong();
        name = in.readString();
        album = in.readString();
    }

    public static final Creator<Artist> CREATOR = new Creator<Artist>() {
        @Override
        public Artist createFromParcel(Parcel in) {
            return new Artist(in);
        }

        @Override
        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(album);
    }
}
