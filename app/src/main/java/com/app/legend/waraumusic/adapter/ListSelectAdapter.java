package com.app.legend.waraumusic.adapter;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.bean.PlayList;
import com.app.legend.waraumusic.interfaces.SelectItemClickListener;

import java.util.List;

public class ListSelectAdapter extends BaseAdapter<ListSelectAdapter.ViewHolder> {


    private List<PlayList> playLists;
    private SelectItemClickListener listener;

    public void setListener(SelectItemClickListener listener) {
        this.listener = listener;
    }

    public void setPlayLists(List<PlayList> playLists) {
        this.playLists = playLists;

        PlayList list=new PlayList();

        list.setName("新建列表");

        playLists.add(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item,parent,false);

        ViewHolder viewHolder=new ViewHolder(view);

        viewHolder.view.setOnClickListener(v -> {

            if (listener!=null){

                int position=viewHolder.getAdapterPosition();

                PlayList playList=playLists.get(position);

                if (position==playLists.size()-1){

                    Log.d("ddd------>>>","sssss");
                    listener.clickLast(playList.getName());

                }else {
                    listener.click(position,playList);

                }

            }

        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);


        if (playLists==null){
            return;
        }

        PlayList playList = playLists.get(position);

        holder.textView.setText(playList.getName());


    }

    @Override
    public int getItemCount() {

        if (this.playLists!=null){
            return playLists.size();
        }

        return super.getItemCount();
    }

    static class ViewHolder extends BaseAdapter.ViewHolder{

        TextView textView;

        View view;

        public ViewHolder(View itemView) {
            super(itemView);

            view=itemView;

            textView=itemView.findViewById(R.id.list_name);

        }
    }
}
