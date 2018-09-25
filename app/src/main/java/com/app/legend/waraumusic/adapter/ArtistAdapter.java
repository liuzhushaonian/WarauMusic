package com.app.legend.waraumusic.adapter;


import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.bean.Artist;
import com.app.legend.waraumusic.fragment.interfaces.OnArtistItemClickListener;
import com.app.legend.waraumusic.utils.ImageLoader;
import com.app.legend.waraumusic.utils.WarauApp;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.List;


/**
 *
 * Created by legend on 2018/2/6.
 */

public class ArtistAdapter extends BaseAdapter<ArtistAdapter.ViewHolder> {

    private List<Artist> artists;
    private int type=0;
    public static final int NORMAL=0x00100;
    public static final int SEARCH=0x00300;
    private int w=0;

    private OnArtistItemClickListener listener;

    public void setListener(OnArtistItemClickListener listener) {
        this.listener = listener;
    }

    public ArtistAdapter(int type) {
        this.type = type;

        w= WarauApp.getContext().getResources().getDimensionPixelSize(R.dimen.press_space);
    }

    public void setArtists(List<Artist> artists) {
        this.artists = artists;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.artist_list_item, parent, false);

        ViewHolder viewHolder=new ViewHolder(view);

        viewHolder.view.setOnClickListener(v -> {
            int position=viewHolder.getAdapterPosition();

            if (listener!=null){

                Artist artist=artists.get(position);

                listener.click(position,artist);
            }

        });

        viewHolder.button.setOnClickListener(v -> {
            int position=viewHolder.getAdapterPosition();

            if (listener!=null){

                Artist artist=artists.get(position);

                listener.clickMenu(position,artist,v);
            }


        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (artists!=null){
            Artist artist=artists.get(position);
            holder.name.setText(artist.getName());
//            holder.info.setText(artist.getAlbum());
            holder.family_name.setText(getFirst(artist.getName()));
            if (type==SEARCH){
                holder.button.setVisibility(View.GONE);
            }

            RequestListener<Drawable> requestListener=new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                    holder.pic.setVisibility(View.GONE);

                    return true;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    return false;
                }
            };

            holder.pic.setVisibility(View.VISIBLE);

            Glide.with(holder.view).load(ImageLoader.getArtist(artist.getId())).listener(requestListener).into(holder.pic);

        }

        super.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        if (artists!=null){
            return artists.size();
        }
        return super.getItemCount();
    }

    static class ViewHolder extends BaseAdapter.ViewHolder{

        TextView family_name,name,info;
        ImageView button;
        View view;
        ImageView pic;

        public ViewHolder(View itemView) {
            super(itemView);
            view=itemView;
            family_name=itemView.findViewById(R.id.artist_list_album);
            name=itemView.findViewById(R.id.artist_list_song_name);
            pic=itemView.findViewById(R.id.artist_item_pic);
//            info=itemView.findViewById(R.id.artist_list_info);

            button=itemView.findViewById(R.id.artist_list_button);
        }
    }

    private String getFirst(String name){

        String f=name.substring(0,1);

        return f;
    }



}
