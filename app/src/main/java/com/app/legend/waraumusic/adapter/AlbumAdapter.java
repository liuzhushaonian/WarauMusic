package com.app.legend.waraumusic.adapter;

import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.bean.Album;
import com.app.legend.waraumusic.fragment.interfaces.OnAlbumItemClickListener;
import com.app.legend.waraumusic.utils.ImageLoader;
import com.app.legend.waraumusic.utils.WarauApp;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

/**
 *
 * Created by legend on 2018/2/6.
 */

public class AlbumAdapter extends BaseAdapter<AlbumAdapter.ViewHolder> {


    List<Album> albumList;
    private OnAlbumItemClickListener listener;

    public void setListener(OnAlbumItemClickListener listener) {
        this.listener = listener;
    }

    public void setAlbumList(List<Album> albumList) {
        this.albumList = albumList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.album_list_item, parent, false);
        ViewHolder viewHolder=new ViewHolder(view);

        viewHolder.view.setOnClickListener(v -> {
            int position=viewHolder.getAdapterPosition();

            if (listener!=null){

                Album album=albumList.get(position);


                listener.click(v,position,album);
            }

        });

        viewHolder.button.setOnClickListener(v -> {

            if (listener!=null) {

                int position = viewHolder.getAdapterPosition();

                Album album=albumList.get(position);
                listener.clickMenu(v,position,album);
            }

        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        super.onBindViewHolder(holder, position);


        if (this.albumList!=null){

            Album album=albumList.get(position);

            holder.artist.setText(album.getArtist());

            holder.name.setText(album.getAlbum_name());

            RequestOptions options=new RequestOptions().placeholder(R.drawable.ic_audiotrack_black_100dp).dontAnimate();

            Glide.with(WarauApp.getContext()).load(ImageLoader.getUrl(album.getId())).apply(options).into(holder.book);

//            ViewCompat.setTransitionName(holder.book, "trans"+position);

//            ImageLoader.getImageLoader(holder.view.getContext())
//            .setAlbumListImage(holder.book,holder.width,holder.width,album.getId(),ImageLoader.ALBUM);


        }


    }

    @Override
    public int getItemCount() {

        if (this.albumList!=null){
            return this.albumList.size();
        }

        return 0;
    }

    static class ViewHolder extends BaseAdapter.ViewHolder{

        View view;
        TextView name,artist;
        ImageView book,button;
        int width=0;

        public ViewHolder(View itemView) {
            super(itemView);
            view=itemView;
            name=itemView.findViewById(R.id.album_name);
            artist=itemView.findViewById(R.id.album_info);
            book=itemView.findViewById(R.id.album_book);
            button=itemView.findViewById(R.id.album_button);

            reDraw();
        }

        private void reDraw(){

            ViewGroup.LayoutParams layoutParams=view.getLayoutParams();
            int space= WarauApp.getContext().getResources().getDimensionPixelSize(R.dimen.album_space);
            int width=WarauApp.getContext().getResources().getDisplayMetrics().widthPixels;
            int defaultSpace=WarauApp.getContext().getResources().getDimensionPixelSize(R.dimen.press_space);
            int itemWidth=(width-space)/2;
            this.width=itemWidth;
            int itemHeight=itemWidth+defaultSpace;

            layoutParams.width=itemWidth;
            layoutParams.height=itemHeight;

            view.setLayoutParams(layoutParams);

            ViewGroup.LayoutParams imageParams=book.getLayoutParams();

            imageParams.height=itemWidth;

            book.setLayoutParams(imageParams);


        }
    }



}
