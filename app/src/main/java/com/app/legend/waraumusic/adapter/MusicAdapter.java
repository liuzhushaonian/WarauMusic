package com.app.legend.waraumusic.adapter;


import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.bean.Music;
import com.app.legend.waraumusic.fragment.interfaces.IMusicOnClickListener;
import com.app.legend.waraumusic.fragment.interfaces.OnMusicMenuClickListener;
import com.app.legend.waraumusic.fragment.interfaces.OnPlayingListClickListener;
import com.app.legend.waraumusic.fragment.interfaces.SearchMusicClickListener;
import com.app.legend.waraumusic.utils.ImageLoader;
import com.app.legend.waraumusic.utils.LongIdUtils;
import com.app.legend.waraumusic.utils.WarauApp;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;


/**
 *音乐列表adapter
 * Created by legend on 2018/1/29.
 */

public class MusicAdapter extends BaseAdapter<MusicAdapter.ViewHolder> {

    private List<Music> allMusicList;

    private IMusicOnClickListener onClickListener;
    private OnPlayingListClickListener playingListClickListener;
    private OnMusicMenuClickListener menuClickListener;
    private int color=WarauApp.getContext().getResources().getColor(R.color.colorTeal);

    public void setColor(int color) {
        this.color = color;
        notifyDataSetChanged();
    }

    public void setMenuClickListener(OnMusicMenuClickListener menuClickListener) {
        this.menuClickListener = menuClickListener;
    }

    public void setPlayingListClickListener(OnPlayingListClickListener playingListClickListener) {
        this.playingListClickListener = playingListClickListener;
    }

    private Disposable disposable;

    public static final int ALL_MUSIC=0x0000100;
    public static final int ALBUM_MUSIC=0x0000200;
    public static final int ARTIST_MUSIC=0x0000300;
    public static final int LIST_MUSIC=0x0000400;
    public static final int SEARCH_LIST_MUSIC=0x0000500;
    public static final int BOTTOM=0x0000600;

    private int type=-1;

    private int w;

    SearchMusicClickListener searchMusicClickListener;

    public void setSearchMusicClickListener(SearchMusicClickListener searchMusicClickListener) {
        this.searchMusicClickListener = searchMusicClickListener;
    }

    public void setOnClickListener(IMusicOnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public MusicAdapter(int type) {
        this.type=type;

        w= WarauApp.getContext().getResources().getDimensionPixelSize(R.dimen.bottom_play_bar);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.music_list_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        switch (type){

            case ALL_MUSIC://全部音乐点击事件
            case ARTIST_MUSIC:
            case ALBUM_MUSIC:

                viewHolder.view.setOnClickListener(v -> {
                    int position = viewHolder.getAdapterPosition();
                    clickMusic(position);

                });

                break;


            case LIST_MUSIC://正在播放列表点击事件

                viewHolder.view.setOnClickListener(v -> {


                    int p=viewHolder.getAdapterPosition();

                    if (this.playingListClickListener!=null){

                        long id=allMusicList.get(p).getMediaMetadataCompat().getLong("uniqueId");


                        playingListClickListener.click(p,id);
                    }


                });

                break;


            case SEARCH_LIST_MUSIC://搜索点击事件

                viewHolder.view.setOnClickListener(v -> {

                    if (searchMusicClickListener!=null){

                        int p=viewHolder.getAdapterPosition();

                        Music music=allMusicList.get(p);

                        searchMusicClickListener.click(music.getMediaMetadataCompat());

                    }

                });

                break;





        }


        viewHolder.menu.setOnClickListener(v -> {

            int position = viewHolder.getAdapterPosition();
            clickMenu(position,v);

        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);

        if (allMusicList != null) {

            Music music=allMusicList.get(position);

            holder.state.setImageTintList(ColorStateList.valueOf(color));

            MediaMetadataCompat compat =music.getMediaMetadataCompat();

            holder.song.setText(compat.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            String info = compat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST) + " | " + compat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);

            holder.info.setText(info);

//            ImageLoader.getImageLoader(holder.view.getContext()).setListImage(holder.album_book,w,w,compat,ImageLoader.SMALL);

            long id=compat.getLong("albumId");

            RequestOptions options=new RequestOptions().placeholder(R.drawable.ic_music_note_black_24dp).dontAnimate();

            Glide.with(WarauApp.getContext())
                    .load(ImageLoader.getUrl(id))
                    .apply(options)
                    .into(holder.album_book);



            if (music.getSelect()>0) {

                holder.state.setVisibility(View.VISIBLE);
            }else {

                holder.state.setVisibility(View.GONE);
            }



            //局部刷新时调用
            if (!payloads.isEmpty()) {

                int s= (int) payloads.get(0);

                switch (s){

                    case 10:

                        if (music.getSelect()>0) {

                            holder.state.setVisibility(View.VISIBLE);
                        }else {

                            holder.state.setVisibility(View.GONE);
                        }

                        break;


                }

            }

            if (type==SEARCH_LIST_MUSIC){

                holder.menu.setVisibility(View.GONE);
            }


        }


    }

    @Override
    public int getItemCount() {

        if (allMusicList != null) {
            return allMusicList.size();
        }

        return 0;
    }

    static class ViewHolder extends BaseAdapter.ViewHolder {

        ImageView album_book, state, menu;
        TextView song, info;
        View view;

        private ViewHolder(View itemView) {
            super(itemView);

            view = itemView;
            album_book = itemView.findViewById(R.id.music_list_album);
            state = itemView.findViewById(R.id.music_list_play_state);
            menu = itemView.findViewById(R.id.music_list_button);

            song = itemView.findViewById(R.id.music_list_song_name);
            info = itemView.findViewById(R.id.music_list_info);

        }
    }

    public void setData(List<Music> list) {

        this.allMusicList = list;
        notifyDataSetChanged();
    }

    public void insertData(Music music){

        if (this.allMusicList==null){
            this.allMusicList=new ArrayList<>();
        }

        this.allMusicList.add(music);

        notifyItemInserted(allMusicList.size());

//        notifyDataSetChanged();

    }


    /**
     * 点击播放事件
     * @param position 播放位置
     */
    private void clickMusic(int position){

        List<MediaSessionCompat.QueueItem> queueItemList=new ArrayList<>();

        for (Music music:allMusicList){

            MediaSessionCompat.QueueItem queueItem=new MediaSessionCompat
                    .QueueItem(music.getMediaMetadataCompat().getDescription(), music.getMediaMetadataCompat().getLong("uniqueId"));

            queueItemList.add(queueItem);
        }

        if (this.onClickListener!=null){

            onClickListener.click(position,queueItemList);
        }

    }



    /**
     * 更改显示正在播放的音乐
     * @param id 传入唯一id，因为会有其他地方的列表点击播放，则需要在全部音乐处展示
     */
    public void showStatus(String id){


        for (int i=0;i<allMusicList.size();i++){

            Music music=allMusicList.get(i);

            if (music.getMediaMetadataCompat().getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID).equals(id)){

                music.setSelect(1);

                notifyItemChanged(i,10);

            }else {
                music.setSelect(-1);
                notifyItemChanged(i,10);

            }

        }

    }

    private void clickMenu(int position,View view){

        if (this.menuClickListener!=null){

            MediaMetadataCompat mediaMetadataCompat=allMusicList.get(position).getMediaMetadataCompat();

            this.menuClickListener.clickMenu(view,position,mediaMetadataCompat);

        }

    }

    public List<Music> getAllMusicList() {
        return allMusicList;
    }

    public void deleteMusic(int position){

        this.allMusicList.remove(position);

        notifyItemRemoved(position);

    }
}