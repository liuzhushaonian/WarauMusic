package com.app.legend.waraumusic.fragment;


import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.transition.Fade;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.activity.MainActivity;
import com.app.legend.waraumusic.adapter.PlayListAdapter;
import com.app.legend.waraumusic.bean.PlayList;
import com.app.legend.waraumusic.fragment.interfaces.PlayListItemClickListener;
import com.app.legend.waraumusic.presenter.MainPlayListFragmentPresenter;
import com.app.legend.waraumusic.presenter.interfaces.IMainPlayListFragment;
import com.app.legend.waraumusic.utils.Database;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainPlayListFragment extends BasePresenterFragment<IMainPlayListFragment,MainPlayListFragmentPresenter>
        implements IMainPlayListFragment{

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private PlayListAdapter adapter;
    private TextView infos;
    private FloatingActionButton floatingActionButton;

    public MainPlayListFragment() {
        // Required empty public constructor
    }

    @Override
    protected MainPlayListFragmentPresenter createPresenter() {
        return new MainPlayListFragmentPresenter(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view=inflater.inflate(R.layout.fragment_main_play_list, container, false);
        getComponent(view);
        initList();
        event();
        getData();

        return view;
    }

    @Override
    void autoSetColor() {
        if (floatingActionButton!=null){

            floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(getThemeColor()));

        }
    }

    private void getComponent(View view){

        recyclerView=view.findViewById(R.id.play_list_recycler_view);

        infos=view.findViewById(R.id.infos);

        floatingActionButton=view.findViewById(R.id.addBtn);

    }

    private void initList(){
        linearLayoutManager=new LinearLayoutManager(getContext());
        adapter=new PlayListAdapter();
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        adapter.setListener(new PlayListItemClickListener() {
            @Override
            public void click(int position, PlayList list) {
                openList(list);
            }

            @Override
            public void clickMenu(int position, PlayList playList, View view) {
                PopupMenu popupMenu=setPopupMenu(view,R.menu.play_list_menu);
                popupMenu.setOnMenuItemClickListener(item -> {

                    menuClick(item,position,playList);

                    return true;
                });

                popupMenu.show();
            }
        });

    }


    public void getData(){

        presenter.getData();

    }


    @Override
    public void setData(List<PlayList> playLists) {

        if (playLists==null||playLists.isEmpty()){
            infos.setVisibility(View.VISIBLE);
        }else {
            infos.setVisibility(View.GONE);
        }

        adapter.setPlayListList(playLists);
    }

    @Override
    public void playAll(List<MediaSessionCompat.QueueItem> queueItemList) {
        play(0,queueItemList);
    }


    private void openList(PlayList playList){

        PlayListInfoFragment fragment=new PlayListInfoFragment();

        fragment.setEnterTransition(new Fade());

        setExitTransition(new Fade());

        Bundle bundle=new Bundle();

        bundle.putParcelable(PlayListInfoFragment.TAG,playList);

        fragment.setArguments(bundle);

        ((MainActivity) Objects.requireNonNull(getActivity())).addFragment(fragment);

    }

    private void menuClick(MenuItem item,int position,PlayList playList){

        switch (item.getItemId()){

            case R.id.play_list_play:

                presenter.playAllPlayListMusic(playList);

                break;

            case R.id.play_list_rename:

                rename(playList);

                break;

            case R.id.play_list_delete:

                presenter.deleteList(playList,position);

                adapter.removeItem(position);


                break;


        }

    }

    private void rename(PlayList playList){

        AlertDialog.Builder builder=new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

        EditText editText=new EditText(getActivity());

        editText.setText(playList.getName());

        editText.setSelected(true);

        builder.setPositiveButton("修改",(dialog, which) -> {

            String name=editText.getText().toString();

            if (TextUtils.isEmpty(name)){

                Toast.makeText(getActivity(), "名字不可以为空", Toast.LENGTH_SHORT).show();

                return;
            }

            int s=Database.getDefault().renameList(playList,name);

            if (s>0){

                getData();//刷新

                Toast.makeText(getActivity(), "修改成功", Toast.LENGTH_SHORT).show();
            }else {

                Toast.makeText(getActivity(), "修改失败", Toast.LENGTH_SHORT).show();
            }


        }).setNegativeButton("取消",(dialog, which) -> {

            builder.create().cancel();

        }).setView(editText).setTitle("请输入名字").show();

    }

    private void event(){

        floatingActionButton.setOnClickListener(v -> {

            newList();

        });

    }

}
