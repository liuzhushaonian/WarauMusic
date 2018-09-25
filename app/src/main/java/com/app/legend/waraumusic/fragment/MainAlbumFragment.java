package com.app.legend.waraumusic.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.activity.MainActivity;
import com.app.legend.waraumusic.adapter.AlbumAdapter;
import com.app.legend.waraumusic.bean.Album;
import com.app.legend.waraumusic.fragment.interfaces.OnAlbumItemClickListener;
import com.app.legend.waraumusic.presenter.MainAlbumFragmentPresenter;
import com.app.legend.waraumusic.presenter.interfaces.IMainAlbumFragment;
import com.app.legend.waraumusic.service.PlayService;
import com.app.legend.waraumusic.utils.AlbumItemSpace;
import com.app.legend.waraumusic.utils.FragmentTransition;
import com.app.legend.waraumusic.utils.ImageLoader;
import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Objects;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

/**
 * 主页面，专辑页面
 * A simple {@link Fragment} subclass.
 */
public class MainAlbumFragment extends BasePresenterFragment<IMainAlbumFragment,MainAlbumFragmentPresenter>
        implements IMainAlbumFragment{

    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private LinearLayout linearLayout;
    private AlbumAdapter albumAdapter;


    public MainAlbumFragment() {
        // Required empty public constructor
    }

    @Override
    protected MainAlbumFragmentPresenter createPresenter() {
        return new MainAlbumFragmentPresenter(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view=inflater.inflate(R.layout.fragment_main_album, container, false);

        getComponent(view);

        initList();

        getData();

        return view;
    }

    @Override
    void autoSetColor() {

    }

    private void getComponent(View view){

        recyclerView=view.findViewById(R.id.album_recycler_view);

    }

    private void initList(){

        layoutManager=new GridLayoutManager(getContext(),2);

        recyclerView.setLayoutManager(layoutManager);

        albumAdapter=new AlbumAdapter();

        recyclerView.setAdapter(albumAdapter);

        recyclerView.addItemDecoration(new AlbumItemSpace(AlbumItemSpace.PAGER));
        recyclerView.getItemAnimator().setChangeDuration(0);


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState==RecyclerView.SCROLL_STATE_IDLE){

                    Glide.with(getContext()).resumeRequests();
                }else {

                    Glide.with(getContext()).pauseRequests();
                }

            }
        });


        albumAdapter.setListener(new OnAlbumItemClickListener() {

            @Override
            public void click(View view, int position, Album album) {

                openAlbum(album);

            }

            @Override
            public void clickMenu(View view, int position, Album album) {

                PopupMenu popupMenu=setPopupMenu(view,R.menu.album_menu);

                popupMenu.setOnMenuItemClickListener(item -> {

                    menuClick(item,position,album);
                    return true;
                });

                popupMenu.show();
            }
        });


    }

    private void getData(){

        presenter.getData();
    }


    @Override
    public void setData(List<Album> albums) {
        albumAdapter.setAlbumList(albums);
    }

    @Override
    public void playAlbumMusic(List<MediaSessionCompat.QueueItem> queueItemList) {
        play(0,queueItemList);
    }

    @Override
    public void addToList(List<Integer> integerList) {
        addListMusicToList(integerList);
    }

    private void menuClick(MenuItem item,int position,Album album){

        switch (item.getItemId()){

            case R.id.open_album:

                openAlbum(album);

                break;
            case R.id.play_album://播放所有的album音乐

                presenter.playAlbumMusic(album);

                break;
            case R.id.add_list://将album的所有音乐都添加到列表里

                presenter.addAllMusicToList(album);

                break;
            case R.id.save_album_book://保存图片

                presenter.saveBitmap(album);

                break;


        }

    }

    private void openAlbum(Album album){

        AlbumInfoFragment fragment=new AlbumInfoFragment();

        fragment.setEnterTransition(new Fade());

        setExitTransition(new Fade());

        Bundle bundle=new Bundle();

        bundle.putParcelable(AlbumInfoFragment.TAG,album);

        fragment.setArguments(bundle);

        ((MainActivity) Objects.requireNonNull(getActivity())).addFragment(fragment);


    }



}
