package com.app.legend.waraumusic.fragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.activity.MainActivity;
import com.app.legend.waraumusic.adapter.MusicAdapter;
import com.app.legend.waraumusic.bean.Music;
import com.app.legend.waraumusic.bean.PlayList;
import com.app.legend.waraumusic.interfaces.TranslucentListener;
import com.app.legend.waraumusic.presenter.PlayListInfoFragmentPresenter;
import com.app.legend.waraumusic.presenter.interfaces.IPlayListFragment;
import com.app.legend.waraumusic.utils.Conf;
import com.app.legend.waraumusic.utils.MyNestedScrollView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayListInfoFragment extends BasePresenterFragment<IPlayListFragment,PlayListInfoFragmentPresenter>
        implements IPlayListFragment,TranslucentListener{

    private PlayList play;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private MusicAdapter adapter;
    private Toolbar toolbar;
    private ImageView imageView;
    private MyNestedScrollView myNestedScrollView;

    public static final String TAG="play_list";

    public PlayListInfoFragment() {
        // Required empty public constructor
    }

    @Override
    protected PlayListInfoFragmentPresenter createPresenter() {
        return new PlayListInfoFragmentPresenter(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view=inflater.inflate(R.layout.fragment_play_list, container, false);

        getComponent(view);

        getPlayList();

        reDraw();

        initList();

        initToolbar();

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
        recyclerView=view.findViewById(R.id.list_music);
        toolbar=view.findViewById(R.id.play_list_info_toolbar);
        imageView=view.findViewById(R.id.list_image);
        myNestedScrollView=view.findViewById(R.id.play_list_my);
    }

    private void getPlayList(){
        Bundle bundle=getArguments();

        if (bundle!=null) {

            this.play = bundle.getParcelable(TAG);
        }

    }


    private void initList(){
        linearLayoutManager=new LinearLayoutManager(getContext());
        adapter=new MusicAdapter(MusicAdapter.LIST_MUSIC);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);


        adapter.setOnClickListener(this::play);


        adapter.setMenuClickListener((view, position, metadataCompat) -> {

            PopupMenu popupMenu=setPopupMenu(view,R.menu.play_list_music_menu);

            popupMenu.setOnMenuItemClickListener(item -> {

                menuClick(item,position,metadataCompat);
                return true;
            });

            popupMenu.show();
        });

    }

    private void initToolbar(){
        toolbar.setPadding(0,getStatusBarHeight(),0,0);
        toolbar.setTitle(play.getName());
        toolbar.setNavigationIcon(R.drawable.round_arrow_back_24px);
        toolbar.setNavigationOnClickListener(v -> {
            ((MainActivity) Objects.requireNonNull(getActivity())).removeFragment(this);

        });

        toolbar.setBackgroundColor(getResources().getColor(R.color.colorBlue));
        toolbar.getBackground().setAlpha(16);
    }


    private void getData(){

        presenter.getData(this.play);

    }


    @Override
    public void setData(Music musicList) {
        adapter.insertData(musicList);
    }

    @Override
    public void setImage(String bitmap) {

        imageView.setVisibility(View.VISIBLE);

        Glide.with(this).load(bitmap).into(imageView);
    }

    @Override
    public void deleteCallback(int position) {
//        getData();

        adapter.deleteMusic(position);

    }


    private void menuClick(MenuItem item, int position, MediaMetadataCompat metadataCompat){

        switch (item.getItemId()){

            case R.id.music_play:

                playOne(metadataCompat);

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
            case R.id.list_delete:

                presenter.deleteMusic(metadataCompat,this.play,position);

                break;


        }

    }

    private void reDraw(){

        int h= (int) (getResources().getDisplayMetrics().widthPixels*0.8);

        LinearLayout.LayoutParams layoutParams= (LinearLayout.LayoutParams) imageView.getLayoutParams();
        layoutParams.height=h;

        imageView.setLayoutParams(layoutParams);

        myNestedScrollView.setTranslucentListener(this,h);
    }


    @Override
    public void onTranslucent(int alpha) {
        toolbar.getBackground().setAlpha(alpha);
    }
}
