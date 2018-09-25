package com.app.legend.waraumusic.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.bean.PlayList;
import com.app.legend.waraumusic.fragment.interfaces.PlayListItemClickListener;

import java.util.List;


/**
 *
 * Created by legend on 2018/2/12.
 */

public class PlayListAdapter extends BaseAdapter<PlayListAdapter.ViewHolder>{

    private List<PlayList> playListList;

    PlayListItemClickListener listener;

    public void setListener(PlayListItemClickListener listener) {
        this.listener = listener;
    }

    public void setPlayListList(List<PlayList> playListList) {
        this.playListList = playListList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());

        View view=inflater.inflate(R.layout.play_list_item,parent,false);

        ViewHolder viewHolder=new ViewHolder(view);

        viewHolder.view.setOnClickListener(v -> {
            int position=viewHolder.getAdapterPosition();

            if (listener!=null){

                PlayList list=playListList.get(position);

                listener.click(position,list);
            }

        });

        viewHolder.button.setOnClickListener(v -> {
            int position=viewHolder.getAdapterPosition();

            if (listener!=null){

                PlayList playList=playListList.get(position);

                listener.clickMenu(position,playList,v);
            }

        });




        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if (playListList!=null){

            PlayList playList=playListList.get(position);

//            String[] strings=playList.getSongs().split(";");

            holder.name.setText(playList.getName());

            String info=playList.getLength()+"首歌曲";

            holder.info.setText(info);

        }

        super.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        if (playListList!=null){
            return playListList.size();
        }

        return super.getItemCount();
    }

    static class ViewHolder extends BaseAdapter.ViewHolder{

        View view;
        ImageView book,button;
        TextView name,info;


        public ViewHolder(View itemView) {
            super(itemView);
            view=itemView;
            book=itemView.findViewById(R.id.play_list_album);
            button=itemView.findViewById(R.id.play_list_button);
            name=itemView.findViewById(R.id.play_list_name);
            info=itemView.findViewById(R.id.play_list_info);
        }
    }


    public void removeItem(int position){

        this.playListList.remove(position);

        notifyItemRemoved(position);

    }
}
