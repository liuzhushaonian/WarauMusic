package com.app.legend.waraumusic.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

public class MainPagerAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> fragmentList;

    public void setFragmentList(List<Fragment> fragmentList) {
        this.fragmentList = fragmentList;
        notifyDataSetChanged();
    }

    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        if (this.fragmentList!=null) {

            return this.fragmentList.get(position);
        }


        return null;
    }

    @Override
    public int getCount() {

        if (this.fragmentList!=null){
            return this.fragmentList.size();
        }

        return 0;
    }
}
