package com.app.legend.waraumusic.fragment;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.activity.MainActivity;
import com.app.legend.waraumusic.adapter.AlbumAdapter;
import com.app.legend.waraumusic.adapter.MusicAdapter;
import com.app.legend.waraumusic.bean.Album;
import com.app.legend.waraumusic.bean.Artist;
import com.app.legend.waraumusic.bean.Music;
import com.app.legend.waraumusic.fragment.interfaces.OnAlbumItemClickListener;
import com.app.legend.waraumusic.interfaces.TranslucentListener;
import com.app.legend.waraumusic.presenter.ArtistInfoFragmentPresenter;
import com.app.legend.waraumusic.presenter.interfaces.IArtistInfoFragment;
import com.app.legend.waraumusic.utils.AlbumItemSpace;
import com.app.legend.waraumusic.utils.Conf;
import com.app.legend.waraumusic.utils.ImageLoader;
import com.app.legend.waraumusic.utils.MyNestedScrollView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 歌手详细页面，放置歌手所有专辑以及所有音乐
 * A simple {@link Fragment} subclass.
 */
public class ArtistInfoFragment extends BasePresenterFragment<IArtistInfoFragment,ArtistInfoFragmentPresenter> implements
        IArtistInfoFragment,TranslucentListener{


    private Artist artist;

    public static final String TAG="artist";

    private RecyclerView albumList,musicList;
    private LinearLayoutManager linearLayoutManager;
    private GridLayoutManager gridLayoutManager;
    private AlbumAdapter albumMusicAdapter;
    private MusicAdapter musicAdapter;
    private TextView artistName;
    private Toolbar toolbar;
    private MyNestedScrollView nestedScrollView;
    private ImageView pic;
    private FrameLayout frameLayout;

    public ArtistInfoFragment() {
        // Required empty public constructor
    }

    @Override
    protected ArtistInfoFragmentPresenter createPresenter() {
        return new ArtistInfoFragmentPresenter(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view=inflater.inflate(R.layout.fragment_artist_info, container, false);

        getComponent(view);

        getArtist();
        reDraw();
        initToolbar();
        initList();

        getData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        autoSetColor();
    }

    @Override
    void autoSetColor() {
        if (toolbar!=null){
            toolbar.setBackgroundColor(getThemeColor());
            toolbar.getBackground().setAlpha(16);
        }
    }

    private void getComponent(View view){

        albumList=view.findViewById(R.id.artist_info_album_list);
        musicList=view.findViewById(R.id.artist_info_music_list);
        artistName=view.findViewById(R.id.artist_info_name);
        toolbar=view.findViewById(R.id.artist_info_toolbar);
        nestedScrollView=view.findViewById(R.id.artist_info_netes_scroll_view);
        pic=view.findViewById(R.id.artist_pic);
        frameLayout=view.findViewById(R.id.frame_top);

    }

    private void initToolbar(){

        if (this.artist!=null) {
            toolbar.setTitle(this.artist.getName());
        }

        toolbar.setPadding(0,getStatusBarHeight(),0,0);
        toolbar.setNavigationIcon(R.drawable.round_arrow_back_24px);
        toolbar.setNavigationOnClickListener(v -> {
            ((MainActivity) Objects.requireNonNull(getActivity())).removeFragment(this);

        });

        int h=getResources().getDimensionPixelSize(R.dimen.album_info_d);

        nestedScrollView.setTranslucentListener(this,h);

        toolbar.getBackground().setAlpha(16);


    }

    private void initList(){
        linearLayoutManager=new LinearLayoutManager(getContext());
        gridLayoutManager=new GridLayoutManager(getContext(),1);
        gridLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        albumMusicAdapter=new AlbumAdapter();
        albumList.setAdapter(albumMusicAdapter);
        albumList.setLayoutManager(gridLayoutManager);
        albumList.addItemDecoration(new AlbumItemSpace(AlbumItemSpace.INFO));

        musicAdapter=new MusicAdapter(MusicAdapter.ARTIST_MUSIC);
        musicList.setAdapter(musicAdapter);
        musicList.setLayoutManager(linearLayoutManager);
        musicList.setNestedScrollingEnabled(false);


        albumMusicAdapter.setListener(new OnAlbumItemClickListener() {
            @Override
            public void click(View view, int position, Album album) {
                openAlbumFragment(album);
            }

            @Override
            public void clickMenu(View view, int position, Album album) {
                PopupMenu popupMenu=setPopupMenu(view,R.menu.album_menu);

                popupMenu.setOnMenuItemClickListener(item -> {

                    albumMenuClick(item,position,album);
                    return true;
                });


                popupMenu.show();
            }
        });

        //音乐点击，播放
        musicAdapter.setOnClickListener(this::play);

        //音乐菜单点击，展开菜单
        musicAdapter.setMenuClickListener((view, position, metadataCompat) -> {

            PopupMenu popupMenu=setPopupMenu(view,R.menu.music_menu);

            popupMenu.setOnMenuItemClickListener(item -> {

                musicMenuClick(item,position,metadataCompat);
                return true;
            });

            popupMenu.show();
        });

    }

    /**
     * 重写frameLayout的高度
     */
    private void reDraw(){

        int h= (int) (getResources().getDisplayMetrics().widthPixels*0.8);

        LinearLayout.LayoutParams layoutParams= (LinearLayout.LayoutParams) frameLayout.getLayoutParams();

        layoutParams.height=h;

        frameLayout.setLayoutParams(layoutParams);

    }


    @Override
    public void onTranslucent(int alpha) {
        toolbar.getBackground().setAlpha(alpha);
    }

    private void getArtist(){

        Bundle bundle=getArguments();

        if (bundle!=null){

            this.artist=bundle.getParcelable(TAG);

            Glide.with(this).load(ImageLoader.getArtist(this.artist.getId())).into(this.pic);

        }

    }

    private void getData(){

        presenter.getData(artist);

    }

    @Override
    public void setAlbumData(List<Album> albumList) {
        albumMusicAdapter.setAlbumList(albumList);
    }

    @Override
    public void setMusicData(List<Music> musicList) {
        musicAdapter.setData(musicList);
    }

    @Override
    public void playAlbumMusic(List<MediaSessionCompat.QueueItem> queueItemList) {
        play(0,queueItemList);
    }

    @Override
    public void addListMusic(List<Integer> integers) {
        addListMusicToList(integers);
    }


    private void albumMenuClick(MenuItem menuItem,int position,Album album){

        switch (menuItem.getItemId()){

            case R.id.open_album:

                openAlbumFragment(album);

                break;

            case R.id.play_album:

                presenter.playAllMusic(album);

                break;

            case R.id.add_list:


                presenter.addList(album);

                break;

            case R.id.save_album_book:

                presenter.saveBitmap(album);

                break;

//            case R.id.album_delete:
//                break;


        }

    }


    private void musicMenuClick(MenuItem item, int position, MediaMetadataCompat metadataCompat){

        switch (item.getItemId()){

            case R.id.music_play:

                playOne(metadataCompat);
                break;

            case R.id.music_add_to_list:

                showPlayList(metadataCompat);

                break;
            case R.id.next_play:

                addMusicToNext(metadataCompat);

                break;
            case R.id.see_artist:

                openArtistFragment(getArtist(metadataCompat));

                break;
            case R.id.see_album:

                openAlbumFragment(getAlbum(metadataCompat));
                break;


        }

    }
}
