package com.app.legend.waraumusic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.bean.Album;
import com.app.legend.waraumusic.fragment.interfaces.OnSearchAlbumClickListener;

import java.util.List;

/**
 *
 * Created by legend on 2018/2/14.
 */

public class SearchAlbumAdapter extends BaseAdapter<SearchAlbumAdapter.ViewHolder> {

    private List<Album> albumList;

    OnSearchAlbumClickListener albumClickListener;

    public void setAlbumClickListener(OnSearchAlbumClickListener albumClickListener) {
        this.albumClickListener = albumClickListener;
    }

    public void setAlbumList(List<Album> albumList) {
        this.albumList = albumList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater=LayoutInflater.from(parent.getContext());

        View view=inflater.inflate(R.layout.search_album_list_item,parent,false);

        ViewHolder viewHolder=new ViewHolder(view);

        viewHolder.view.setOnClickListener(v -> {

            if (albumClickListener!=null) {

                int position = viewHolder.getAdapterPosition();
                Album album = albumList.get(position);

                albumClickListener.click(album);

            }
        });



        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(albumList!=null){
            Album album=albumList.get(position);
            holder.name.setText(album.getAlbum_name());
            holder.info.setText(album.getArtist());


        }else {
            super.onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        if (albumList!=null){
            return albumList.size();
        }
        return super.getItemCount();
    }

    static class ViewHolder extends BaseAdapter.ViewHolder {

        View view;
        ImageView book,button;
        TextView name,info;

        public ViewHolder(View itemView) {
            super(itemView);
            view=itemView;
            book=itemView.findViewById(R.id.search_album_list_book);
//            button=itemView.findViewById(R.id.search_album_list_button);
            name=itemView.findViewById(R.id.search_album_list_song_name);
            info=itemView.findViewById(R.id.search_album_list_info);
        }
    }



}
