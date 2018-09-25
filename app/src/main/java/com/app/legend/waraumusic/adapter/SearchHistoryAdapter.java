package com.app.legend.waraumusic.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.fragment.interfaces.OnHistoryItemClickListener;
import com.app.legend.waraumusic.utils.WarauApp;

import java.util.List;

/**
 *搜索历史adapter
 * Created by legend on 2018/2/14.
 */

public class SearchHistoryAdapter extends BaseAdapter<SearchHistoryAdapter.ViewHolder>{

    private List<String> historyList;
    private OnHistoryItemClickListener listener;

    public void setListener(OnHistoryItemClickListener listener) {
        this.listener = listener;
    }

    public void setHistoryList(List<String> historyList) {
        this.historyList = historyList;
        if (!this.historyList.isEmpty()) {
            this.historyList.add(this.historyList.size(),"清除搜索记录");
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater=LayoutInflater.from(parent.getContext());
        View view=layoutInflater.inflate(R.layout.history_list_item,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);

        viewHolder.view.setOnClickListener(v -> {

            if (listener!=null) {



                int position = viewHolder.getAdapterPosition();

                if (position==historyList.size()-1){

                    listener.clickLast();

                }else {

                    String data = historyList.get(position);

                    listener.click(position,data);
                }



            }


        });

        viewHolder.clear.setOnClickListener(v -> {
            if (listener!=null){
                int position = viewHolder.getAdapterPosition();
                String data = historyList.get(position);

                listener.clearClick(position,data);
            }


        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (historyList!=null){
            String history=historyList.get(position);
            holder.textView.setText(history);
            if (history.equals("清除搜索记录")){
                holder.imageView.setVisibility(View.GONE);
                holder.textView.setGravity(Gravity.CENTER);
                holder.clear.setVisibility(View.GONE);

            }else {
                holder.imageView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (historyList!=null){
            return historyList.size();
        }
        return super.getItemCount();
    }

    static class ViewHolder extends BaseAdapter.ViewHolder {

        View view;
        TextView textView;
        ImageView imageView,clear;

        ViewHolder(View itemView) {
            super(itemView);
            view=itemView;
            textView=itemView.findViewById(R.id.history_item);
            imageView=itemView.findViewById(R.id.item_icon);
            clear=itemView.findViewById(R.id.history_clear);
        }
    }

    public void removeItem(int position){

        if (this.historyList!=null){

            this.historyList.remove(position);

            notifyItemRemoved(position);

            if (this.historyList.size()==1){
                this.historyList.clear();

                notifyDataSetChanged();
            }

        }

    }

    public void clearAll(){
        this.historyList.clear();

        notifyDataSetChanged();
    }

}
