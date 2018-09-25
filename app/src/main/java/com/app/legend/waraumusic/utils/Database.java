package com.app.legend.waraumusic.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.app.legend.waraumusic.bean.Music;
import com.app.legend.waraumusic.bean.PlayList;

import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteOpenHelper {

    private static volatile Database database;
    private static final String MUSICDATABASE="JustMusicDatabase";//数据库名称
    private static int VERSION=1;//数据库版本
    private SQLiteDatabase sqLiteDatabase;//数据库实例
    private static final String DEFAULT_TABLE="PlayList";
    private static final String ID="id";
    private static final String NAME="name";
    private static final String SONGS="songs";
    private static final String HISTORY_TABLE="history";


    private static final String DEFAULT="CREATE TABLE IF NOT EXISTS "+DEFAULT_TABLE+"(" +
            ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
            NAME+" TEXT NOT NULL UNIQUE," +
            SONGS+" TEXT DEFAULT ''" +
            ")";

    private static final String HISTORY="CREATE TABLE IF NOT EXISTS "+HISTORY_TABLE+"(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "strings TEXT NOT NULL UNIQUE" +
            ")";

    private static final String LIST_TABLE="CREATE TABLE IF NOT EXISTS play_list(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT NOT NULL" +
            ")";


    private static final String LIST_MUSIC_TABLE="CREATE TABLE IF NOT EXISTS list_music(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "list_id INTEGER," +
            "song_id INTEGER" +
            ")";

    public static Database getDefault(){

        if (database == null) {
            synchronized (Database.class) {
                database = new Database(WarauApp.getContext(), MUSICDATABASE, null, VERSION);
            }
        }

        return database;
    }


    private Database(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        sqLiteDatabase=getReadableDatabase();

        sqLiteDatabase.execSQL(HISTORY);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(DEFAULT);
        db.execSQL(LIST_TABLE);//初始化列表
        db.execSQL(LIST_MUSIC_TABLE);//初始化列表音乐表


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    /**
     * 添加历史查询记录
     * @param history 历史记录
     */
    public void addHistory(String history){

        String sql="insert into "+HISTORY_TABLE+"(strings) values('"+history+"')";

        try {

            sqLiteDatabase.execSQL(sql);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    //查询所有历史输入记录
    public List<String> getHistory(){
        List<String> stringList=new ArrayList<>();
        try {

            String sql="select * from "+HISTORY_TABLE;

            Cursor cursor=sqLiteDatabase.rawQuery(sql,null);

            if (cursor!=null){
                if (cursor.moveToFirst()){
                    do {
                        String item=cursor.getString(cursor.getColumnIndex("strings"));
                        stringList.add(item);
                    }while (cursor.moveToNext());
                }

                cursor.close();
            }


        }catch (Exception e){
            e.printStackTrace();
        }

        return stringList;

    }

    //清除历史记录
    public void deleteAllHistory(){

        try {
            String sql="delete from "+HISTORY_TABLE;
            sqLiteDatabase.execSQL(sql);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    //列表重命名
    public int renameList(PlayList playList, String newName){
        int result=0;

        try {
            String sql="update play_list set name = '"+newName+"' where id = "+playList.getId();
            sqLiteDatabase.execSQL(sql);
            result=1;
        }catch (Exception e){
            e.printStackTrace();
            result=-1;
        }

        return result;

    }

    /**
     * 获取所有列表
     * @return 返回列表集合
     */
    public List<PlayList> getAllPlayLists(){

        List<PlayList> playList=new ArrayList<>();

        String sql="select * from play_list";

        try {
            Cursor cursor=sqLiteDatabase.rawQuery(sql,null);

            if (cursor!=null){

                if (cursor.moveToFirst()){

                    do {
                        String name=cursor.getString(cursor.getColumnIndex("name"));

                        int id=cursor.getInt(cursor.getColumnIndex("id"));

                        PlayList playList1=new PlayList();

                        playList1.setId(id);
                        playList1.setName(name);

                        playList1.setLength(getPlayMusicCount(id));

                        playList.add(playList1);

                    }while (cursor.moveToNext());


                }


                cursor.close();
            }


        }catch (Exception e){

            e.printStackTrace();
        }


        return playList;

    }

    //获取列表歌曲数目
    private int getPlayMusicCount(int id){

        String sql="select list_id from list_music where list_id ="+id;

        int count=0;

        try {

           Cursor cursor= sqLiteDatabase.rawQuery(sql,null);

           if (cursor!=null){

               count=cursor.getCount();

               cursor.close();
           }

        }catch (Exception e){
            e.printStackTrace();
        }

        return count;

    }

    /**
     * 删除列表
     * @param playList 列表对象
     * @return 返回结果
     */
    public int deletePlayList(PlayList playList){

        String sql="delete from play_list where id ="+playList.getId();

        String sql2="delete from list_music where list_id="+playList.getId();

        int result=-1;

        try {

            sqLiteDatabase.execSQL(sql);//删除已定义列表

            sqLiteDatabase.execSQL(sql2);//删除列表所有数据

            result=1;
        }catch (Exception e){

            result=-1;

            e.printStackTrace();
        }


        return result;
    }

    /**
     * 添加列表
     * @param playList 列表对象
     * @return 返回结果
     */
    public int addList(PlayList playList){

        int result=-1;


        String sql="insert into play_list (name) values ('"+playList.getName()+"')";

        String sql1="select last_insert_rowid() from play_list";

        try {

            sqLiteDatabase.execSQL(sql);

            Cursor cursor=sqLiteDatabase.rawQuery(sql1,null);

            if (cursor!=null){

                if (cursor.moveToFirst()){

                    result=cursor.getInt(0);

//                    Log.d("result----->>>",""+result);

                }

                cursor.close();

            }

//

        }catch (Exception e){

            result=-1;
            e.printStackTrace();
        }

        return result;

    }


    /**
     * 添加音乐到列表里
     * @param playList 列表对象
     * @param music_id 音乐id
     */
    public void addMusicToList(PlayList playList,int music_id){

        String sql="insert into list_music (list_id,song_id)values("+playList.getId()+","+music_id+")";


        try {

            sqLiteDatabase.execSQL(sql);


        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 删除列表歌曲
     * @param playList 列表对象
     * @param music_id 歌曲id
     * @return 返回结果
     */
    public int deleteMusicFromList(PlayList playList,int music_id){

        String sql="delete from list_music where list_id="+playList.getId()+"and song_id="+music_id;

        int count=-1;

        try {

            sqLiteDatabase.execSQL(sql);
            count=1;
        }catch (Exception e){
            e.printStackTrace();
        }

        return count;
    }

    /**
     * 获取列表的歌曲id
     * @param playList 列表对象
     * @return 返回歌曲id列表
     */
    public List<Integer> getListMusic(PlayList playList){

        String sql="select song_id from list_music where list_id="+playList.getId();

        List<Integer> ids=new ArrayList<>();

        try {

            Cursor cursor=sqLiteDatabase.rawQuery(sql,null);

            if (cursor!=null){

                if (cursor.moveToFirst()){

                    do {

                        int id=cursor.getInt(cursor.getColumnIndex("song_id"));

                        ids.add(id);

                    }while (cursor.moveToNext());

                }

                cursor.close();
            }


        }catch (Exception e){
            e.printStackTrace();
        }

        return ids;

    }

    public void deleteHistory(String s){

        String sql="delete from "+HISTORY_TABLE+" where strings = '"+s+"'";

        try {

            sqLiteDatabase.execSQL(sql);

        }catch (Exception e){
            e.printStackTrace();
        }


    }

}
