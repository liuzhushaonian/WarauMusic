package com.app.legend.waraumusic.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.legend.waraumusic.R;
import com.app.legend.waraumusic.adapter.SearchHistoryAdapter;
import com.app.legend.waraumusic.bean.Album;
import com.app.legend.waraumusic.bean.Artist;
import com.app.legend.waraumusic.fragment.SearchFragment;
import com.app.legend.waraumusic.fragment.interfaces.OnHistoryItemClickListener;
import com.app.legend.waraumusic.presenter.SearchActivityPresenter;
import com.app.legend.waraumusic.presenter.interfaces.ISearchActivity;
import com.app.legend.waraumusic.utils.Database;

import java.util.List;

import io.reactivex.disposables.Disposable;

public class SearchActivity extends BasePresenterActivity<ISearchActivity,SearchActivityPresenter> implements ISearchActivity{

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private Toolbar toolbar;
    private SearchHistoryAdapter adapter;
    private SearchFragment fragment;
    private LinearLayout fragment_container;
    private SearchView searchView;
    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);


        getComponent();
        searchEvent();
        initToolbar();
        initList();
        getHistoryData();

    }

    @Override
    protected void onResume() {
        super.onResume();

        autoSetColor();
    }

    @Override
    void autoSetColor() {
        if (toolbar!=null){

            toolbar.setBackgroundColor(getThemeColor());
        }
    }

    private void getComponent() {
        recyclerView = findViewById(R.id.search_history);
        toolbar =findViewById(R.id.search_toolbar);
        fragment_container=findViewById(R.id.search_fragment_container);
        searchView=findViewById(R.id.search_view);

    }

    @Override
    protected SearchActivityPresenter createPresenter() {
        return new SearchActivityPresenter(this);
    }

    private void initToolbar(){

        toolbar.setTitle("");

        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> {

            finish();
        });
    }

    /**
     * 搜索事件
     */
    private void searchEvent(){


        searchView.setIconifiedByDefault(true);
        searchView.setIconified(false);
        int id = searchView.getResources().getIdentifier("search_src_text", "id", getApplicationContext().getPackageName());

        this.textView=searchView.findViewById(id);
        this.textView.setFocusable(true);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//                query(query);
//                Log.d("query--->>",query+"");
                insertQuery(query);
                closeSoftKeybord(textView);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                query(newText);
                return true;
            }
        });

    }

    //关闭输入法
    private void closeSoftKeybord(View view){
        InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    private void initList(){
        linearLayoutManager=new LinearLayoutManager(this);
        adapter=new SearchHistoryAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter.setListener(new OnHistoryItemClickListener() {
            @Override
            public void click(int position, String s) {

                query(s);


//                queryDataByFragment(s);
            }

            @Override
            public void clearClick(int position, String s) {
                Database.getDefault().deleteHistory(s);

                adapter.removeItem(position);
            }

            @Override
            public void clickLast() {//清除全部
                Database.getDefault().deleteAllHistory();

                adapter.clearAll();
            }
        });

    }

    private void getHistoryData(){
        presenter.getHistory();
    }


    @Override
    public void setData(List<String> list) {
        adapter.setHistoryList(list);
    }

    /**
     * 交给fragment搜索
     * @param data 关键字
     */
    @Override
    public void queryDataByFragment(String data) {
        SearchFragment fragment= (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment);
        fragment.queryData(data);
    }

    /**
     * 需要保存为历史
     * @param string
     */
    private void insertQuery(String string){
        if (TextUtils.isEmpty(string)){

            hideFragment();
            return;
        }

        Database.getDefault().addHistory(string);

        showFragment();



    }

    //不需要保存为历史
    private void query(String s){
        if (TextUtils.isEmpty(s)){
            hideFragment();
            return;
        }
        showFragment();
//        presenter.queryData(s);
        queryDataByFragment(s);
    }

    private void showFragment(){
        recyclerView.setVisibility(View.GONE);
        fragment_container.setVisibility(View.VISIBLE);
    }

    private void hideFragment(){
        recyclerView.setVisibility(View.VISIBLE);
        fragment_container.setVisibility(View.GONE);

        getHistoryData();//每次显示都重新获取一次数据
    }


    public void startActivityForArtist(Artist artist){

        Intent intent=new Intent(SearchActivity.this,MainActivity.class);
        intent.putExtra("artist",artist);
        startActivity(intent);

    }

    public void startActivityForAlbum(Album album){
        Intent intent=new Intent(SearchActivity.this,MainActivity.class);
        intent.putExtra("album",album);
        startActivity(intent);
    }

}
