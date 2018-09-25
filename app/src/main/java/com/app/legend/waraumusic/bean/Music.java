package com.app.legend.waraumusic.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.media.MediaMetadataCompat;

/**
 * music实例，封装各种music的信息
 * Created by legend on 2018/1/25.
 */

public class Music implements Parcelable{


    private int select=-1;

    private MediaMetadataCompat mediaMetadataCompat;

    public Music() {
    }

    protected Music(Parcel in) {
        select = in.readInt();
        mediaMetadataCompat = in.readParcelable(MediaMetadataCompat.class.getClassLoader());
    }

    public static final Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel in) {
            return new Music(in);
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };

    public int getSelect() {
        return select;
    }

    public void setSelect(int select) {
        this.select = select;
    }

    public MediaMetadataCompat getMediaMetadataCompat() {
        return mediaMetadataCompat;
    }

    public void setMediaMetadataCompat(MediaMetadataCompat mediaMetadataCompat) {
        this.mediaMetadataCompat = mediaMetadataCompat;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(select);
        dest.writeParcelable(mediaMetadataCompat, flags);
    }
}
