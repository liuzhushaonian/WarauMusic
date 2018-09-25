package com.app.legend.waraumusic.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.activity.SearchActivity;
import com.app.legend.waraumusic.adapter.ArtistAdapter;
import com.app.legend.waraumusic.adapter.MusicAdapter;
import com.app.legend.waraumusic.adapter.SearchAlbumAdapter;
import com.app.legend.waraumusic.bean.Album;
import com.app.legend.waraumusic.bean.Artist;
import com.app.legend.waraumusic.bean.Music;
import com.app.legend.waraumusic.fragment.interfaces.OnArtistItemClickListener;
import com.app.legend.waraumusic.presenter.SearchFragmentPresenter;
import com.app.legend.waraumusic.presenter.interfaces.ISearchFragment;

import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends BasePresenterFragment<ISearchFragment,SearchFragmentPresenter>
        implements ISearchFragment{

    private RecyclerView musicRecycler,artistRecycler,albumRecycler;
    private MusicAdapter musicAdapter;
    private ArtistAdapter artistAdapter;
    private SearchAlbumAdapter albumAdapter;
    private LinearLayout musicList,artistList,albumList,searchInfo;
    private int show=0;


    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    protected SearchFragmentPresenter createPresenter() {
        return new SearchFragmentPresenter(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_search, container, false);
        getComponent(view);
        initList();
        return view;
    }

    @Override
    void autoSetColor() {

    }

    private void getComponent(View view){
        musicRecycler=view.findViewById(R.id.search_fragment_music_list);
        artistRecycler=view.findViewById(R.id.search_fragment_artist_list);
        albumRecycler=view.findViewById(R.id.search_fragment_album_list);
        musicList=view.findViewById(R.id.search_fragment_music);
        artistList=view.findViewById(R.id.search_fragment_artist);
        albumList=view.findViewById(R.id.search_fragment_album);
        searchInfo=view.findViewById(R.id.search_no_info);
    }

    private void initList(){
        LinearLayoutManager musicLayoutManager = new LinearLayoutManager(getContext());
        LinearLayoutManager artistLayoutManager=new LinearLayoutManager(getContext());
        LinearLayoutManager albumLayoutManager=new LinearLayoutManager(getContext());
        musicAdapter=new MusicAdapter(MusicAdapter.SEARCH_LIST_MUSIC);
        musicRecycler.setLayoutManager(musicLayoutManager);
        musicRecycler.setAdapter(musicAdapter);

        artistAdapter=new ArtistAdapter(ArtistAdapter.SEARCH);
        artistRecycler.setLayoutManager(artistLayoutManager);
        artistRecycler.setAdapter(artistAdapter);

        albumAdapter=new SearchAlbumAdapter();
        albumRecycler.setLayoutManager(albumLayoutManager);
        albumRecycler.setAdapter(albumAdapter);


        musicAdapter.setSearchMusicClickListener(this::playOne);

        artistAdapter.setListener(new OnArtistItemClickListener() {

            @Override
            public void click(int position, Artist artist) {
                ((SearchActivity) Objects.requireNonNull(getActivity())).startActivityForArtist(artist);
            }

            @Override
            public void clickMenu(int position, Artist artist, View view) {

            }
        });


        albumAdapter.setAlbumClickListener(((SearchActivity) Objects.requireNonNull(getActivity()))::startActivityForAlbum);

    }

    /**
     * 外部调用，直接查询
     * @param string 查询关键字
     */
    public void queryData(String string){
        getData(string);
    }

    private void getData(String string){
        presenter.getData(string);
    }


    @Override
    public void setMusicData(List<Music> musicList) {
        if (musicList==null||musicList.isEmpty()){
            this.musicList.setVisibility(View.GONE);
        }else {
            this.musicList.setVisibility(View.VISIBLE);
            musicAdapter.setData(musicList);
        }
    }



    @Override
    public void setArtistData(List<Artist> artistList) {
        if (artistList==null||artistList.isEmpty()){
            this.artistList.setVisibility(View.GONE);
        }else {
            this.artistList.setVisibility(View.VISIBLE);
            artistAdapter.setArtists(artistList);
        }
    }

    @Override
    public void setAlbumData(List<Album> albumList) {
        if (albumList==null||albumList.isEmpty()){
            this.albumList.setVisibility(View.GONE);
        }else {
            this.albumList.setVisibility(View.VISIBLE);
            albumAdapter.setAlbumList(albumList);
        }
    }

    @Override
    public void setData(List<String> list) {

    }

    @Override
    public void queryDataByFragment(String data) {

    }



    @Override
    public void showInfo(){
        searchInfo.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideInfo(){
        searchInfo.setVisibility(View.GONE);
    }

}
