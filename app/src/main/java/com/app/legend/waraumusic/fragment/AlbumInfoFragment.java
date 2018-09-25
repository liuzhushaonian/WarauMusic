package com.app.legend.waraumusic.fragment;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.app.legend.waraumusic.adapter.MusicAdapter;
import com.app.legend.waraumusic.bean.Album;
import com.app.legend.waraumusic.bean.Music;
import com.app.legend.waraumusic.interfaces.TranslucentListener;
import com.app.legend.waraumusic.presenter.AlbumInfoFragmentPresenter;
import com.app.legend.waraumusic.presenter.interfaces.IAlbumInfoFragment;
import com.app.legend.waraumusic.utils.Conf;
import com.app.legend.waraumusic.utils.ImageLoader;
import com.app.legend.waraumusic.utils.MyNestedScrollView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 展示album详细内容
 * A simple {@link Fragment} subclass.
 */
public class AlbumInfoFragment extends BasePresenterFragment<IAlbumInfoFragment,AlbumInfoFragmentPresenter>
        implements TranslucentListener,IAlbumInfoFragment {

    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    MusicAdapter adapter;
    private Toolbar toolbar;
    private Album album;
    private ImageView bg;
    private TextView album_name,album_info;
    private MyNestedScrollView nestedScrollView;
    public static final String TAG="album";



    public AlbumInfoFragment() {
        // Required empty public constructor
    }

    @Override
    protected AlbumInfoFragmentPresenter createPresenter() {
        return new AlbumInfoFragmentPresenter(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view=inflater.inflate(R.layout.fragment_album_info, container, false);

        getComponent(view);

        getAlbum();

        initBook();

        initToolbar();

        initList();

        presenter.getData(album);

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

        recyclerView=view.findViewById(R.id.album_info_recycler_view);
        toolbar=view.findViewById(R.id.album_info_toolbar);

        nestedScrollView=view.findViewById(R.id.nested_scroll_view);

        bg=view.findViewById(R.id.bg);

    }

    private void getAlbum(){

        Bundle bundle=getArguments();
        if (bundle != null) {
            this.album=bundle.getParcelable(TAG);
        }

    }

    /**
     * 设置头部背景图，取自album
     */
    private void initBook(){

        if (this.album==null){
            return;
        }

        long id=this.album.getId();

        reDraw();

        bg.setVisibility(View.VISIBLE);
        bg.setScaleType(ImageView.ScaleType.CENTER_CROP);

        RequestListener<Drawable> listener=new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                bg.setScaleType(ImageView.ScaleType.CENTER);
                bg.setImageResource(R.drawable.ic_music_note_black_150dp);

                return true;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        };

        RequestOptions options=new RequestOptions().dontAnimate();

        Glide.with(this).load(ImageLoader.getUrl(id)).apply(options).listener(listener).into(bg);

    }


    private void reDraw(){
        LinearLayout.LayoutParams layoutParams= (LinearLayout.LayoutParams) bg.getLayoutParams();
        int h= (int) (getResources().getDisplayMetrics().widthPixels*0.8);
        layoutParams.height=h;
        bg.setLayoutParams(layoutParams);
        nestedScrollView.setTranslucentListener(this,h);
    }


    @Override
    public void onTranslucent(int alpha) {
        if (toolbar!=null){
            toolbar.getBackground().setAlpha(alpha);
        }
    }


    private void initToolbar(){

        toolbar.setPadding(0,getStatusBarHeight(),0,0);

        toolbar.setTitle(album.getAlbum_name());
        toolbar.setNavigationIcon(R.drawable.round_arrow_back_24px);
        toolbar.setNavigationOnClickListener(v -> {

            ((MainActivity) Objects.requireNonNull(getActivity())).removeFragment(this);

        });

        toolbar.setBackgroundColor(getResources().getColor(R.color.colorBlue));
        toolbar.getBackground().setAlpha(16);

    }


    private void initList(){

        adapter=new MusicAdapter(MusicAdapter.ALBUM_MUSIC);

        linearLayoutManager=new LinearLayoutManager(getContext());

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);

        adapter.setOnClickListener(this::play);

        adapter.setMenuClickListener((view, position, metadataCompat) -> {

            PopupMenu popupMenu=setPopupMenu(view,R.menu.album_info_music_menu);

            popupMenu.setOnMenuItemClickListener(item -> {

                menuClick(item,position,metadataCompat);

                return true;
            });

            popupMenu.show();

        });

    }

    @Override
    public void setData(List<Music> musicList) {


        adapter.setData(musicList);

    }



    private void menuClick(MenuItem item, int position, MediaMetadataCompat metadataCompat){


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



        }

    }

}
