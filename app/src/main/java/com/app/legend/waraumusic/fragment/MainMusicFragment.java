package com.app.legend.waraumusic.fragment;


import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.activity.MainActivity;
import com.app.legend.waraumusic.adapter.MusicAdapter;
import com.app.legend.waraumusic.bean.Artist;
import com.app.legend.waraumusic.bean.Music;
import com.app.legend.waraumusic.presenter.MainMusicFragmentPresenter;
import com.app.legend.waraumusic.presenter.interfaces.IMainMusicFragment;
import com.app.legend.waraumusic.utils.Conf;
import com.app.legend.waraumusic.utils.ImageLoader;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainMusicFragment extends BasePresenterFragment<IMainMusicFragment,MainMusicFragmentPresenter> implements IMainMusicFragment{


    private RecyclerView recyclerView;

    private MusicAdapter adapter;

    private LinearLayoutManager linearLayoutManager;

    private TextView musicInfo;

    public MainMusicFragment() {
        // Required empty public constructor
    }

    @Override
    protected MainMusicFragmentPresenter createPresenter() {
        return new MainMusicFragmentPresenter(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view=inflater.inflate(R.layout.fragment_main_music, container, false);

        getComponent(view);


        initList();

        getData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        autoSetColor();
    }

    private void getComponent(View view){

        recyclerView=view.findViewById(R.id.music_list);
        musicInfo=view.findViewById(R.id.music_info);
    }

    /**
     * 初始化列表
     */
    private void initList(){

        linearLayoutManager=new LinearLayoutManager(getContext());

        adapter=new MusicAdapter(MusicAdapter.ALL_MUSIC);

        adapter.setColor(getThemeColor());

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.getItemAnimator().setChangeDuration(0);

        //            Bundle bundle=new Bundle();
//
//            bundle.putParcelableArrayList("list", (ArrayList<? extends Parcelable>) metadataCompatList);
//
//            bundle.putInt("index",position);
//
//            mediaControllerCompat.getTransportControls().sendCustomAction(Conf.UPDATE_LIST_AND_PLAY,bundle);

        adapter.setOnClickListener(this::play);


        //菜单点击事件
        adapter.setMenuClickListener((view, position, metadataCompat) -> {

            PopupMenu popupMenu= setPopupMenu(view,R.menu.music_menu);

            popupMenu.setOnMenuItemClickListener(item ->{//菜单item点击事件

                clickMenuItem(item,position,metadataCompat);

                return true;
            });

            popupMenu.show();

        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState==RecyclerView.SCROLL_STATE_IDLE){

                    Glide.with(Objects.requireNonNull(getContext())).resumeRequests();
                }else {

                    Glide.with(Objects.requireNonNull(getContext())).pauseRequests();
                }

            }
        });


    }

    /**
     * 获取数据
     */
    private void getData(){

        presenter.getAllMusic();

    }

    @Override
    public void setData(List<Music> musicList) {

        if (musicList==null||musicList.isEmpty()){

            musicInfo.setVisibility(View.VISIBLE);

        }else {

            musicInfo.setVisibility(View.GONE);
            adapter.setData(musicList);
        }


    }

    @Override
    protected void onMetadataChanged(MediaMetadataCompat metadata) {
        super.onMetadataChanged(metadata);

        adapter.showStatus(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));

    }

    @Override
    void autoSetColor() {
        if (adapter!=null){
            adapter.setColor(getThemeColor());
        }
    }


    private void clickMenuItem(MenuItem item,int position,MediaMetadataCompat metadataCompat){

        switch (item.getItemId()){

            case R.id.music_play://播放

                if (adapter==null||adapter.getAllMusicList()==null){
                    return;
                }

                playOne(metadataCompat);

                break;

            case R.id.music_add_to_list://添加到列表

                showPlayList(metadataCompat);


                break;
            case R.id.next_play://添加到下一曲播放

                addMusicToNext(metadataCompat);

                break;
            case R.id.see_artist://查看歌手


                openArtistFragment(getArtist(metadataCompat));


                break;
            case R.id.see_album://查看专辑

                openAlbumFragment(getAlbum(metadataCompat));

                break;

        }

    }



}
