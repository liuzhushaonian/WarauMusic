package com.app.legend.waraumusic.utils;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.app.legend.waraumusic.R;


/**
 *
 * Created by legend on 2018/2/6.
 */

public class AlbumItemSpace extends RecyclerView.ItemDecoration{

    public static final int PAGER=0x000100;
    public static final int INFO=0x000200;

    int type=-1;

    public AlbumItemSpace(int type) {
        this.type=type;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//        super.getItemOffsets(outRect, view, parent, state);

        int defaultSpace=WarauApp.getContext().getResources().getDimensionPixelSize(R.dimen.default_space);

        int topSpace=WarauApp.getContext().getResources().getDimensionPixelSize(R.dimen.album_top);

        int other=WarauApp.getContext().getResources().getDimensionPixelSize(R.dimen.album_other);

        int position=parent.getChildAdapterPosition(view);

        switch (type){
            case PAGER:
                if (position==0||position==1){

                    outRect.top=topSpace;
                }
//        else if (position==parent.getAdapter().getItemCount()||position==parent.getAdapter().getItemCount()-1){
//            outRect.bottom=topSpace;
//        }

                else {
                    outRect.top=other;
                }

                if (position%2==0) {
                    outRect.left = defaultSpace;
                } else {
//            outRect.left=other;
                    outRect.right=defaultSpace;
                }

                break;
            case INFO:

                if (position!=0){
                    outRect.left=other;
                }


                break;
        }



    }
}
