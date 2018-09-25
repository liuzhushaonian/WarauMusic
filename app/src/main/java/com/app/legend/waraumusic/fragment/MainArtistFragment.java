package com.app.legend.waraumusic.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.activity.MainActivity;
import com.app.legend.waraumusic.adapter.ArtistAdapter;
import com.app.legend.waraumusic.bean.Artist;
import com.app.legend.waraumusic.fragment.interfaces.OnArtistItemClickListener;
import com.app.legend.waraumusic.presenter.MainArtistFragmentPresenter;
import com.app.legend.waraumusic.presenter.interfaces.IMainArtistFragment;

import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainArtistFragment extends BasePresenterFragment<IMainArtistFragment,MainArtistFragmentPresenter>
        implements IMainArtistFragment{

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ArtistAdapter adapter;
    private LinearLayout linearLayout;
    

    public MainArtistFragment() {
        // Required empty public constructor
    }

    @Override
    protected MainArtistFragmentPresenter createPresenter() {
        return new MainArtistFragmentPresenter(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view=inflater.inflate(R.layout.fragment_main_artist, container, false);

        getComponent(view);

        initList();

        getData();

        return view;
    }

    @Override
    void autoSetColor() {

    }

    private void getComponent(View view){

        recyclerView=view.findViewById(R.id.artist_list);
        linearLayout=view.findViewById(R.id.artist_null_info);

    }


    private void initList(){

        linearLayoutManager=new LinearLayoutManager(getContext());
        adapter=new ArtistAdapter(ArtistAdapter.NORMAL);

        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setAdapter(adapter);


        adapter.setListener(new OnArtistItemClickListener() {
            @Override
            public void click(int position, Artist artist) {

                openArtist(artist);
            }

            @Override
            public void clickMenu(int position, Artist artist, View view) {

                PopupMenu popupMenu=setPopupMenu(view,R.menu.artist_menu);

                popupMenu.setOnMenuItemClickListener(item -> {

                    menuItemClick(item,position,artist);

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
    public void setData(List<Artist> artists) {
        adapter.setArtists(artists);
    }

    @Override
    public void playArtistMusic(List<MediaSessionCompat.QueueItem> queueItemList) {
        play(0,queueItemList);
    }

    @Override
    public void addListMusic(List<Integer> integers) {
        addListMusicToList(integers);
    }


    private void openArtist(Artist artist){

        ArtistInfoFragment fragment=new ArtistInfoFragment();

        fragment.setEnterTransition(new Fade());

        setExitTransition(new Fade());

        Bundle bundle=new Bundle();

        bundle.putParcelable(ArtistInfoFragment.TAG,artist);

        fragment.setArguments(bundle);

        ((MainActivity) Objects.requireNonNull(getActivity())).addFragment(fragment);

    }

    private void menuItemClick(MenuItem item,int position,Artist artist){

        switch (item.getItemId()){

            case R.id.artist_open:

                openArtist(artist);

                break;

            case R.id.artist_play:

                presenter.playArtistMusic(artist);

                break;

            case R.id.artist_add_to_list:

                presenter.addList(artist);

                break;

//            case R.id.artist_delete:
//                break;

        }

    }

}
