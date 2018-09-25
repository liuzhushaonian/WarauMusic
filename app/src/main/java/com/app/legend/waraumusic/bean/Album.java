package com.app.legend.waraumusic.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.media.MediaMetadataCompat;

/**
 *
 * Created by legend on 2018/2/6.
 */

public class Album implements Parcelable{
    private long id;
    private String album_name;
    private long artist_id;


    public Album() {
    }


    protected Album(Parcel in) {
        id = in.readLong();
        album_name = in.readString();
        artist_id = in.readLong();
        artist = in.readString();
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    public long getId() {
        return id;
    }

    public long getArtist_id() {
        return artist_id;
    }

    public void setArtist_id(long artist_id) {
        this.artist_id = artist_id;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    private String artist;



    public void setId(long id) {
        this.id = id;
    }

    public String getAlbum_name() {
        return album_name;
    }

    public void setAlbum_name(String album_name) {
        this.album_name = album_name;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(album_name);
        dest.writeLong(artist_id);
        dest.writeString(artist);
    }
}
