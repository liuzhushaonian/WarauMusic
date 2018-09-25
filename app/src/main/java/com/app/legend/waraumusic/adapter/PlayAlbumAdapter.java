package com.app.legend.waraumusic.adapter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.media.MediaMetadataCompat;

import com.app.legend.waraumusic.fragment.AlbumPagerFragment;

import java.util.List;

public class PlayAlbumAdapter extends FragmentStatePagerAdapter {

    private List<MediaMetadataCompat> metadataCompatList;


    public void setMetadataCompatList(List<MediaMetadataCompat> metadataCompatList) {
        this.metadataCompatList = metadataCompatList;

        notifyDataSetChanged();

    }

    public PlayAlbumAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        if (this.metadataCompatList!=null){

            AlbumPagerFragment fragment=new AlbumPagerFragment();

            MediaMetadataCompat mediaMetadataCompat=this.metadataCompatList.get(position);

            Bundle bundle=new Bundle();

            bundle.putParcelable("album",mediaMetadataCompat);

            fragment.setArguments(bundle);

            return fragment;

        }


        return null;
    }

    @Override
    public int getCount() {

        if (this.metadataCompatList!=null){
            return this.metadataCompatList.size();
        }

        return 0;
    }


    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }
}
