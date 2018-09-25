package com.app.legend.waraumusic.activity;


import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.adapter.MainPagerAdapter;
import com.app.legend.waraumusic.bean.Album;
import com.app.legend.waraumusic.bean.Artist;
import com.app.legend.waraumusic.bean.PlayList;
import com.app.legend.waraumusic.fragment.AlbumInfoFragment;
import com.app.legend.waraumusic.fragment.ArtistInfoFragment;
import com.app.legend.waraumusic.fragment.MainAlbumFragment;
import com.app.legend.waraumusic.fragment.MainArtistFragment;
import com.app.legend.waraumusic.fragment.MainMusicFragment;
import com.app.legend.waraumusic.fragment.MainPlayListFragment;
import com.app.legend.waraumusic.fragment.PlayingViewFragment;
import com.app.legend.waraumusic.presenter.MainPresenter;
import com.app.legend.waraumusic.presenter.interfaces.IMainActivity;
import com.app.legend.waraumusic.service.GetAlbumAndLrcIntentService;
import com.app.legend.waraumusic.service.PlayService;
import com.app.legend.waraumusic.utils.Database;
import com.app.legend.waraumusic.utils.PlayingDragView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BasePresenterActivity<IMainActivity, MainPresenter> implements IMainActivity {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ViewPager viewPager;
    private List<Fragment> fragmentList;
    private MainPagerAdapter adapter;
    private TabLayout tabLayout;
    private FrameLayout playingContainer;
    private PlayingViewFragment playingViewFragment;
    private PlayingDragView parent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        startService();

        initUi();
    }


    @Override
    protected void onResume() {
        super.onResume();

        autoSetColor();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    void autoSetColor() {
        if (toolbar != null) {

            toolbar.setBackgroundColor(getThemeColor());

        }

        if (tabLayout != null) {

            tabLayout.setTabTextColors(getResources().getColor(R.color.colorGrey), getThemeColor());
            tabLayout.setSelectedTabIndicatorColor(getThemeColor());
        }
    }

    @Override
    protected MainPresenter createPresenter() {
        return new MainPresenter(this);
    }

    /**
     * 获取控件
     */
    private void getComponent() {

        toolbar = findViewById(R.id.main_toolbar);
        drawerLayout = findViewById(R.id.drawLayout);
        navigationView = findViewById(R.id.left_menu);
        tabLayout = findViewById(R.id.main_tabLayout);

        viewPager = findViewById(R.id.main_view_pager);

        playingContainer = findViewById(R.id.playing_contain);

        parent = findViewById(R.id.playing_pager);
    }

    /**
     * 初始化toolbar
     */
    private void initToolbar() {

        toolbar.setTitle("音乐");
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);

        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

    }


    /**
     * 初始化UI
     */
    private void initUi() {

        getComponent();

        reDraw();

        initPlayingView();

        initToolbar();

        initPager();

        initTabLayout();

        initDrawLayout();

//        playingViewScrollerEvent();

    }

    /**
     * 初始化viewPager
     */
    private void initPager() {

        if (this.fragmentList == null) {

            this.fragmentList = new ArrayList<>();

        }

        MainMusicFragment musicFragment = new MainMusicFragment();

        MainAlbumFragment albumFragment = new MainAlbumFragment();

        MainArtistFragment artistFragment = new MainArtistFragment();

        MainPlayListFragment playListFragment = new MainPlayListFragment();

        this.fragmentList.add(musicFragment);
        this.fragmentList.add(albumFragment);
        this.fragmentList.add(artistFragment);
        this.fragmentList.add(playListFragment);


        this.adapter = new MainPagerAdapter(getSupportFragmentManager());

        this.adapter.setFragmentList(this.fragmentList);

        viewPager.setOffscreenPageLimit(4);

        viewPager.setAdapter(adapter);


        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));


    }

    /**
     * 初始化tabLayout
     */
    private void initTabLayout() {

        if (tabLayout.getTabCount() > 0) {
            return;
        }

        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.music)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.album)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.artist)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.list)));


        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

    }

    /**
     * 初始化侧滑菜单
     */
    private void initDrawLayout() {

        navigationView.setNavigationItemSelectedListener(item -> {


            switch (item.getItemId()) {

                case R.id.about_app:
                    presenter.showAbout(MainActivity.this);


                    break;

                case R.id.change_color:

                    openColorActivity();

                    break;

//                case R.id.setting:
//
//
//
//                    break;

                case R.id.exit:

                    exitApp();

                    break;

            }

            drawerLayout.closeDrawer(GravityCompat.START);

            return true;

        });

    }


    /**
     * 重新设置PlayingContainer的高度
     */
    private void reDraw() {

        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        int play_bar_height = getResources().getDimensionPixelSize(R.dimen.fu_bottom_play_bar);

        int bottomHeight = getResources().getDimensionPixelSize(R.dimen.bottom_play_bar);

        FrameLayout.LayoutParams parentParams = (FrameLayout.LayoutParams) parent.getLayoutParams();

        parentParams.height = screenHeight * 2 + bottomHeight;

        parentParams.topMargin = play_bar_height;

        parent.setLayoutParams(parentParams);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) playingContainer.getLayoutParams();

        layoutParams.height = screenHeight + bottomHeight;

//        layoutParams.topMargin = screenHeight - bottomHeight;

        this.playingContainer.setLayoutParams(layoutParams);

        parent.setDrawerLayout(drawerLayout);

    }


    /**
     * 初始化播放页面
     */
    private void initPlayingView() {

        if (this.playingViewFragment != null) {
            return;
        }

        this.playingViewFragment = new PlayingViewFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.playing_contain, this.playingViewFragment);

        transaction.commit();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {


            case R.id.search:

                openSearchActivity();

                break;


            case R.id.play_all:

                presenter.playAllMusic();

                break;

            case R.id.new_list:

                newList();

                break;

            case R.id.get_album_and_lrc://一键图词

                presenter.getNetWork(this);

                break;
        }

        return true;
    }

    private void startService() {

        Intent intent = new Intent(this, PlayService.class);

        startService(intent);

    }


    public MediaControllerCompat getMediaControllerCompat() {

        return mediaControllerCompat;
    }


    /**
     * 自动向上滚动，提供给fragment使用
     */
    public void autoScrollToTop() {
        parent.startScrollToTop();

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();

        if (parent.isLock()) {//被锁住，表示在上方，需要下滑

            parent.startScrollToBottom();
        } else if (drawerLayout.isDrawerOpen(GravityCompat.START)) {

            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    //添加fragment
    public void addFragment(Fragment fragment) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.other_pager, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }

    //移除fragment
    public void removeFragment(Fragment fragment) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(fragment);
        fragmentManager.popBackStack();//模拟栈操作，将栈顶null去掉
        fragmentTransaction.commit();

    }


    public void refresh() {

        if (this.fragmentList == null) {
            return;
        }

        MainPlayListFragment fragment = (MainPlayListFragment) this.fragmentList.get(3);

        fragment.getData();

    }

    public void exitApp() {

        Intent intent = new Intent(this, PlayService.class);

        stopService(intent);

        finishAndRemoveTask();

        System.exit(0);


    }

    private void newList() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        EditText editText = new EditText(this);

        builder.setTitle("新列表名字").setView(editText).setPositiveButton("添加", (dialog, which) -> {

            String name = editText.getText().toString();

            if (TextUtils.isEmpty(name)) {
                return;
            }

            PlayList playList = new PlayList();

            playList.setName(name);

            int id = Database.getDefault().addList(playList);

            if (id != -1) {

                refresh();
            }


        }).setNegativeButton("取消", (dialog, which) -> {

            builder.create().cancel();

        }).show();

    }


    @Override
    public void playAllMusic(List<MediaSessionCompat.QueueItem> queueItemList) {
        play(0, queueItemList);
    }

    @Override
    public void startGetInfos() {

        Glide.get(this).clearMemory();

        Intent intent=new Intent(this, GetAlbumAndLrcIntentService.class);

        startService(intent);//启动Service一键图词

        Toast.makeText(this, "正在匹配一键图词，过程可能较为缓慢，但并不会影响您正常使用本APP", Toast.LENGTH_LONG).show();

    }

    private void openSearchActivity() {

        Intent intent = new Intent(this, SearchActivity.class);

        startActivity(intent);

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        searchInfo(intent);

    }

    /**
     * 打开相关页面
     *
     * @param intent
     */
    private void searchInfo(Intent intent) {
        Artist artist = intent.getParcelableExtra("artist");
        Album album = intent.getParcelableExtra("album");
        if (artist != null) {

            toArtistFragment(artist);

        } else if (album != null) {

            toAlbumFragment(album);
        }
    }


    /**
     * 打开artist页面
     *
     * @param artist
     */
    private void toArtistFragment(Artist artist) {

        ArtistInfoFragment fragment = new ArtistInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ArtistInfoFragment.TAG, artist);
        fragment.setArguments(bundle);
        addFragment(fragment);
    }

    /**
     * 打开album页面
     *
     * @param album
     */
    private void toAlbumFragment(Album album) {
        AlbumInfoFragment fragment = new AlbumInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(AlbumInfoFragment.TAG, album);
        fragment.setArguments(bundle);
        addFragment(fragment);
    }

    private void openColorActivity() {

        Intent intent = new Intent(this, ColorActivity.class);

        startActivity(intent);

    }


}
