package com.app.legend.waraumusic.fragment;


import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.transition.Fade;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.activity.MainActivity;
import com.app.legend.waraumusic.adapter.ListSelectAdapter;
import com.app.legend.waraumusic.bean.Album;
import com.app.legend.waraumusic.bean.Artist;
import com.app.legend.waraumusic.bean.Music;
import com.app.legend.waraumusic.bean.PlayList;
import com.app.legend.waraumusic.interfaces.SelectItemClickListener;
import com.app.legend.waraumusic.service.PlayService;
import com.app.legend.waraumusic.utils.Conf;
import com.app.legend.waraumusic.utils.Database;
import com.app.legend.waraumusic.utils.LongIdUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;
import static com.app.legend.waraumusic.utils.Conf.COLOR;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class BaseFragment extends Fragment {


    protected MediaBrowserCompat mediaBrowserCompat;

    protected MediaControllerCompat mediaControllerCompat;
    protected SharedPreferences sharedPreferences;



    public BaseFragment() {
        // Required empty public constructor



    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initBrowser();

        sharedPreferences=getContext().getSharedPreferences(Conf.SHARE_NAME,MODE_PRIVATE);

    }

    @Override
    public void onStart() {
        super.onStart();
        mediaBrowserCompat.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mediaBrowserCompat.disconnect();
    }

    protected void saveThemeColor(int color){

        sharedPreferences.edit().putInt(COLOR,color).apply();

    }

    protected int getThemeColor(){

        return sharedPreferences.getInt(COLOR,getResources().getColor(R.color.colorCP));

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

//        initBrowser();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void initBrowser() {

        this.mediaBrowserCompat = new MediaBrowserCompat(getContext(), new ComponentName(getContext(), PlayService.class),
                connectionCallback, null);

    }

    //连接回调，处理是否连接成功
    private MediaBrowserCompat.ConnectionCallback connectionCallback = new MediaBrowserCompat.ConnectionCallback() {


        @Override
        public void onConnected() {
            super.onConnected();

            if (mediaBrowserCompat.isConnected()) {

                try {
                    mediaControllerCompat = new MediaControllerCompat(getContext(),mediaBrowserCompat.getSessionToken());

                    mediaControllerCompat.registerCallback(callback);

                    BaseFragment.this.onConnect();

                } catch (RemoteException e) {
                    e.printStackTrace();
                }


                String id = mediaBrowserCompat.getRoot();

                mediaBrowserCompat.unsubscribe(id);

                mediaBrowserCompat.subscribe(id, subscriptionCallback);

            }

        }

        @Override
        public void onConnectionFailed() {
            super.onConnectionFailed();
            Log.w("onConnection--->>", "Failed");

        }
    };

    //订阅回调，获取检索到的数据
    private MediaBrowserCompat.SubscriptionCallback subscriptionCallback = new MediaBrowserCompat.SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);


        }
    };


    /**
     * 音乐状态改变回调，由service -> UI
     */
    public MediaControllerCompat.Callback callback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {//播放状态改变，比如播放、暂停、停止
            super.onPlaybackStateChanged(state);

            BaseFragment.this.onPlaybackStateChanged(state);


        }


        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {//播放歌曲改变
            super.onMetadataChanged(metadata);

            BaseFragment.this.onMetadataChanged(metadata);

        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            super.onQueueChanged(queue);

            BaseFragment.this.onQueueChanged(queue);
        }



    };


    protected void onPlaybackStateChanged(PlaybackStateCompat state){

    }


    protected void onMetadataChanged(MediaMetadataCompat metadata){


    }

    protected void onQueueChanged(List<MediaSessionCompat.QueueItem> queue){


    }

    protected void onConnect(){



    }

    protected void onSubscribe(){



    }

    protected int getStatusBarHeight(){

        try {
            Class<?> c=Class.forName("com.android.internal.R$dimen");
            Object object=c.newInstance();
            Field field=c.getField("status_bar_height");
            int x=Integer.parseInt(field.get(object).toString());
            return getResources().getDimensionPixelSize(x);

        }catch (Exception e){
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * 展示popupmenu
     * @param view 展示的view
     * @param reMenu 资源
     */
    protected PopupMenu setPopupMenu(View view,int reMenu){
        PopupMenu popupMenu=new PopupMenu(getContext(),view,0);
        MenuInflater menuInflater=popupMenu.getMenuInflater();
        menuInflater.inflate(reMenu,popupMenu.getMenu());

        return popupMenu;

    }

    protected void showPlayList(MediaMetadataCompat music){

        AlertDialog.Builder builder=new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

        List<PlayList> playLists= Database.getDefault().getAllPlayLists();

        String[] strings=new String[playLists.size()+1];

        for (int i=0;i<strings.length;i++){

            if (i<playLists.size()) {
                strings[i] = playLists.get(i).getName();
            }else {

                strings[i]="新建列表";
            }

        }

        builder.setTitle("选择列表").setItems(strings, (DialogInterface dialog, int which) -> {


            if (which<playLists.size()){

                PlayList playList=playLists.get(which);

                addMusicToList(playList,music);

                refresh();

            }else {

                addList(music);

            }

        }).show();


    }

    private void addMusicToList(PlayList playList,MediaMetadataCompat music){

        long id= Long.parseLong(music.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));

        Database.getDefault().addMusicToList(playList, (int) id);

        Toast.makeText(getActivity(), "添加成功", Toast.LENGTH_SHORT).show();
        refresh();
    }

    /**
     * 刷新
     */
    protected void refresh(){

        ((MainActivity) Objects.requireNonNull(getActivity())).refresh();
    }


    private void addList(MediaMetadataCompat metadataCompat){

        AlertDialog.Builder builder=new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

        EditText editText=new EditText(getContext());

        builder.setTitle("新列表名字").setView(editText).setPositiveButton("添加",(dialog, which) -> {

            String name=editText.getText().toString();

            if (TextUtils.isEmpty(name)){
                return;
            }

            PlayList playList=new PlayList();

            playList.setName(name);

            int id=Database.getDefault().addList(playList);

            if (id!=-1){

                playList.setId(id);

//                Log.d("id----->>>","创建成功");

                long ids= Long.parseLong(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));

                Database.getDefault().addMusicToList(playList, (int) ids);

                Toast.makeText(getContext(), "添加成功", Toast.LENGTH_SHORT).show();

                refresh();
            }


        }).setNegativeButton("取消",(dialog, which) -> {

            builder.create().cancel();

        }).show();


    }


    //播放列表
    protected void play(int position,List<MediaSessionCompat.QueueItem> metadataCompatList){

        Bundle bundle=new Bundle();

        bundle.putParcelableArrayList("list", (ArrayList<? extends Parcelable>) metadataCompatList);

        bundle.putInt("index",position);

        mediaControllerCompat.getTransportControls().sendCustomAction(Conf.UPDATE_LIST_AND_PLAY,bundle);

    }

    //播放一首歌
    protected void playOne(MediaMetadataCompat metadataCompat){

        List<MediaSessionCompat.QueueItem> itemList=new ArrayList<>();

        MediaSessionCompat.QueueItem item=new MediaSessionCompat.QueueItem(metadataCompat.getDescription(), LongIdUtils.getRandomId());

        itemList.add(item);

        play(0,itemList);

    }

    //添加歌曲到下一首播放
    protected void addMusicToNext(MediaMetadataCompat metadataCompat){

        Bundle bundle=new Bundle();

        bundle.putParcelable("music",metadataCompat);

        mediaControllerCompat.getTransportControls().sendCustomAction(Conf.ADD_MUSIC,bundle);

    }


    protected void openFragment(Fragment fragment){

        ((MainActivity) Objects.requireNonNull(getActivity())).addFragment(fragment);

    }

    protected void openAlbumFragment(Album album){

        AlbumInfoFragment fragment=new AlbumInfoFragment();

        fragment.setEnterTransition(new Fade());

        setExitTransition(new Fade());

        Bundle bundle=new Bundle();

        bundle.putParcelable(AlbumInfoFragment.TAG,album);

        fragment.setArguments(bundle);

        openFragment(fragment);

    }

    protected void openArtistFragment(Artist artist){

        ArtistInfoFragment fragment=new ArtistInfoFragment();

        fragment.setEnterTransition(new Fade());

        setExitTransition(new Fade());

        Bundle bundle=new Bundle();

        bundle.putParcelable(ArtistInfoFragment.TAG,artist);

        fragment.setArguments(bundle);

        openFragment(fragment);

    }

    protected Artist getArtist(MediaMetadataCompat metadataCompat){

        long id=metadataCompat.getLong(Conf.ARTIST_ID);

        String name=metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);

        String album=metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);

        Artist artist=new Artist();

        artist.setId(id);

        artist.setName(name);

        artist.setAlbum(album);

        return artist;

    }


    protected Album getAlbum(MediaMetadataCompat metadataCompat){

        Album album=new Album();

        long id=metadataCompat.getLong(Conf.ALBUM_ID);

        long artist_id=metadataCompat.getLong(Conf.ARTIST_ID);

        String name=metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);

        String artist=metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);

        album.setArtist_id(artist_id);

        album.setAlbum_name(name);

        album.setArtist(artist);

        album.setId(id);


        return album;


    }


    protected void addListMusicToList(List<Integer> integers){

        AlertDialog.Builder builder=new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

        List<PlayList> playLists= Database.getDefault().getAllPlayLists();

        String[] strings=new String[playLists.size()+1];

        for (int i=0;i<strings.length;i++){

            if (i<playLists.size()) {
                strings[i] = playLists.get(i).getName();
            }else {

                strings[i]="新建列表";
            }

        }

        builder.setTitle("选择列表").setItems(strings, (DialogInterface dialog, int which) -> {


            if (which<playLists.size()){

                PlayList playList=playLists.get(which);

                for (Integer integer:integers){

                    Database.getDefault().addMusicToList(playList, integer);

                }

                Toast.makeText(getContext(), "添加成功", Toast.LENGTH_SHORT).show();

                refresh();

//                addMusicToList(playList,music);

            }else {

                addList(integers);

            }

        }).show();


    }

    //添加多个音乐
    private void addList(List<Integer> integers){


        AlertDialog.Builder builder=new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

        EditText editText=new EditText(getContext());

        builder.setTitle("新列表名字").setView(editText).setPositiveButton("添加",(dialog, which) -> {

            String name=editText.getText().toString();

            if (TextUtils.isEmpty(name)){
                return;
            }

            PlayList playList=new PlayList();

            playList.setName(name);

            int id=Database.getDefault().addList(playList);

            if (id!=-1){

                playList.setId(id);

//                Log.d("id----->>>","创建成功");

                for (Integer integer:integers){

                    Database.getDefault().addMusicToList(playList, integer);

                }

                Toast.makeText(getContext(), "添加成功", Toast.LENGTH_SHORT).show();

                refresh();
            }


        }).setNegativeButton("取消",(dialog, which) -> {

            builder.create().cancel();

        }).show();


    }

    abstract void autoSetColor();

    protected void newList(){

        AlertDialog.Builder builder=new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

        EditText editText=new EditText(getContext());

        builder.setTitle("新列表名字").setView(editText).setPositiveButton("添加",(dialog, which) -> {

            String name=editText.getText().toString();

            if (TextUtils.isEmpty(name)){
                return;
            }

            PlayList playList=new PlayList();

            playList.setName(name);

            int id=Database.getDefault().addList(playList);

            if (id!=-1){

                Toast.makeText(getContext(), "添加成功", Toast.LENGTH_SHORT).show();

                refresh();
            }


        }).setNegativeButton("取消",(dialog, which) -> {

            builder.create().cancel();

        }).show();

    }


}
