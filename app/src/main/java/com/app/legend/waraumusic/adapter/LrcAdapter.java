package com.app.legend.waraumusic.adapter;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.bean.Lrc;
import com.app.legend.waraumusic.fragment.AlbumPagerFragment;
import com.app.legend.waraumusic.interfaces.LrcItemClickListener;
import com.app.legend.waraumusic.utils.TextSizeUtil;
import com.app.legend.waraumusic.utils.WarauApp;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by legend on 2018/3/9.
 */

public class LrcAdapter extends BaseAdapter<LrcAdapter.ViewHolder> {


    private List<Lrc> lrcList;
    private List<Lrc> tlrcList;
    private LrcItemClickListener listener;
    private int color= WarauApp.getContext().getResources().getColor(R.color.colorCyan);
    private int defaultColor=WarauApp.getContext().getResources().getColor(R.color.colorBlack);
    private int index=0;
    private int pre_index=0;
    private AlbumPagerFragment fragment;
    private int center=-1;
    private float textSize=WarauApp.getContext().getResources().getDimension(R.dimen.lrc_text_size);

    public LrcAdapter(AlbumPagerFragment fragment) {
        this.fragment = fragment;
    }

    public void setLrcList(List<Lrc> lrcList) {

        int result=calculation();

        this.lrcList=new ArrayList<>();//重置

        for (int i=0;i<result/2;i++){

            Lrc lrc=new Lrc();
            lrc.setTime(-1);
            lrc.setContent(" ");

            this.lrcList.add(lrc);//添加空item到最前面
        }

        this.lrcList.addAll(lrcList);//添加实体item

        for (int i=0;i<result/2;i++){
            Lrc lrc=new Lrc();
            lrc.setTime(-1);
            lrc.setContent(" ");

            this.lrcList.add(lrc);//添加空item到最后面
        }

        notifyDataSetChanged();

        pre_index=index;
    }

    public void setTlrcList(List<Lrc> tlrcList) {
        this.tlrcList = tlrcList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater=LayoutInflater.from(parent.getContext());

        View view=layoutInflater.inflate(R.layout.lrc_item,parent,false);

        ViewHolder viewHolder=new ViewHolder(view);

        viewHolder.view.setOnClickListener(v -> {
            if (this.listener!=null){
                listener.click(v);
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);


        if (lrcList!=null){

            holder.textView.setTextSize(TextSizeUtil.px2sp(textSize));

            Lrc lrc=lrcList.get(position);


            if (lrc.getContent()!=null) {
                Lrc tlrc = getSameLrc(lrc);

                StringBuilder builder = new StringBuilder(lrc.getContent());

                if (tlrc != null) {

//                Log.d("tlrc--->>",tlrc.getTime()+""+tlrc.getContent());
                    builder.append("\n").append(tlrc.getContent());
                }

                holder.textView.setText(builder.toString());

            }else {
                holder.textView.setText("");
            }

//            if (index==position){
//
//                holder.textView.setTextColor(color);
//            }else {
//
//                holder.textView.setTextColor(defaultColor);
//            }

            if (index==position){
                holder.textView.setTextColor(color);
            }else if (center==position){

                holder.textView.setTextColor(color);

            }else {
                holder.textView.setTextColor(defaultColor);
            }

//            Log.d("view--height-->>",holder.view.getHeight()+"");
        }



    }

    @Override
    public int getItemCount() {

        if (lrcList!=null){
            return lrcList.size();
        }

        return super.getItemCount();
    }

    static class ViewHolder extends BaseAdapter.ViewHolder {

        View view;
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            view=itemView;
            textView=itemView.findViewById(R.id.lrc_item_text);


        }
    }

    public void setListener(LrcItemClickListener listener) {
        this.listener = listener;
    }


    private Lrc getSameLrc(Lrc lrc){

        if (this.tlrcList!=null){

            for (Lrc lrc1:tlrcList){

                if (lrc1.getTime()==lrc.getTime()){
                    return lrc1;
                }
            }
        }

        return null;
    }



    public void changeIndex(long time,long songTime){

        if (lrcList==null){
            return;
        }

        if (time< songTime){

            for (int i=0;i<lrcList.size();i++){

                if (i<lrcList.size()-1){

                    if (lrcList.get(i).getTime()!=-1&&time<lrcList.get(i).getTime()&&i==0){
                        index=i;
                    }

                    if (lrcList.get(i).getTime()!=-1&&(time>lrcList.get(i).getTime())&&time<lrcList.get(i+1).getTime()){
                        index=i;
                    }

                    if (lrcList.get(i).getTime()!=-1&&(time>lrcList.get(i).getTime())&&lrcList.get(i+1).getTime()==-1){//最后一句歌词
                        index=i;
                    }

                }

                if (lrcList.get(i).getTime()!=-1&&(i==lrcList.size()-1)&&time>lrcList.get(i).getTime()){
                    index=i;
                }
            }
        }

        if (index!=pre_index) {
            notifyDataSetChanged();
            pre_index=index;//记住上一次
            if (this.fragment!=null){
                this.fragment.setCurrentItemToCenter(index);
            }
        }
    }

    /**
     *
     * 计算应该放多少个空item
     * @return
     */
    private int calculation(){
        int half_screen_width=WarauApp.getContext().getResources().getDisplayMetrics().widthPixels/2;//获取屏幕宽度的一半
        int item_height=WarauApp.getContext().getResources().getDimensionPixelSize(R.dimen.bottom_padding);//获取item高度，8dp左右
        int result=half_screen_width/item_height-1;
        return result;
    }


    private void getHeight(View view){

        Log.d("view--height-->>>",view.getHeight()+"");

    }

    public void setCenter(int center) {
        this.center = center;
        if (this.fragment!=null&&center>=0){
            this.fragment.showTime(this.lrcList.get(center));//传入歌词时间给显示
        }
        notifyDataSetChanged();
    }

    public void setColor(int color) {
        this.color = color;
        notifyDataSetChanged();
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
        notifyDataSetChanged();
    }

    public float getTextSize() {
        return textSize;
    }


    public void cleanTLrcList(){

        if (this.tlrcList!=null){
            tlrcList.clear();
            tlrcList=null;
            notifyDataSetChanged();
        }

    }
}
