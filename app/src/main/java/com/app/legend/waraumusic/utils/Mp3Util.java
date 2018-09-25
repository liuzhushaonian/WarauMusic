package com.app.legend.waraumusic.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import com.app.legend.waraumusic.bean.Album;
import com.app.legend.waraumusic.bean.Artist;
import com.app.legend.waraumusic.bean.Music;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 *
 * Created by legend on 2018/1/25.
 */

public class Mp3Util {

    private static volatile Mp3Util mp3Util;

    private Map<String,MediaMetadataCompat> metadataCompatMap=new HashMap<>();//存储所有音乐信息

    private void getMp3InfoArrayList(Context context){
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Audio.Media.DURATION + ">=18000", null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        assert cursor != null;
        for (int i = 0; i<cursor.getCount(); i++){
            cursor.moveToNext();

            String uri=cursor.getString(cursor.getColumnIndex((MediaStore.Audio.Media.DATA)));
            long _id=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            String title=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artist=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String albums=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            long duration=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            int isMusic=cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
            int albumsId=cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
            long artistId=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));

            long uniqueId=LongIdUtils.getRandomId();


            if (isMusic!=0){

                MediaMetadataCompat.Builder builder=new MediaMetadataCompat.Builder();

                builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION,duration);
                builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE,title);
                builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM,albums);
                builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST,artist);
//                builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,ImageLoader.getImageLoader(WarauApp.getContext()).getBitmap(albumsId));
                builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,uri);
                builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, String.valueOf(_id));
                builder.putLong("albumId",albumsId);//设置id，方便查找封面
                builder.putLong("uniqueId",uniqueId);//设置唯一id，方便查找position
                builder.putLong(Conf.ARTIST_ID,artistId);

                metadataCompatMap.put(String.valueOf(_id),builder.build());

            }
        }
        cursor.close();

    }

    public static Mp3Util newInstance(){

        if (mp3Util==null){
            synchronized (Mp3Util.class) {
                mp3Util = new Mp3Util();
            }
        }

        return mp3Util;
    }

    public void scanMusic(){

        if (metadataCompatMap.isEmpty()) {
            getMp3InfoArrayList(WarauApp.getContext());
        }
    }

    //service订阅
    public List<MediaBrowserCompat.MediaItem> getMediaItemList(){

        if (this.metadataCompatMap.isEmpty()){

            scanMusic();

        }

        List<MediaBrowserCompat.MediaItem> mediaItemList=new ArrayList<>();

        for (Map.Entry<String,MediaMetadataCompat> m:metadataCompatMap.entrySet()){

            MediaBrowserCompat.MediaItem mediaItem=new MediaBrowserCompat.MediaItem(m.getValue().getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);

            mediaItemList.add(mediaItem);

        }


        return mediaItemList;


    }

    public List<MediaMetadataCompat> getAllList(){

        List<MediaMetadataCompat> mediaMetadataCompats=new ArrayList<>();

        for (Map.Entry<String,MediaMetadataCompat> m:metadataCompatMap.entrySet()){

            mediaMetadataCompats.add(m.getValue());

        }

        return mediaMetadataCompats;

    }

    /**
     * 根据id获取MediaMetadataCompat对象
     * @param id
     * @return
     */
    public MediaMetadataCompat getMediaMetadataCompatById(String id){

        return metadataCompatMap.get(id);

    }

    //转换时间
    public static String formatTime(long time){


        SimpleDateFormat format=new SimpleDateFormat("mm:ss");

        format.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));

        return format.format(time);
    }

    public List<Album> getAllAlbumList(){

        Cursor cursor = WarauApp.getContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Audio.Media.DURATION + ">=18000", null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        Set<Integer> set=new HashSet<>();
        List<Album> albumList=new ArrayList<>();

        assert cursor!=null;
        for (int i=0;i<cursor.getCount();i++){
            cursor.moveToNext();
            String albums=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            int albumsId=cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
            int isMusic=cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
            String artist=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            long artistId=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
            if (isMusic!=0){

                Album album=new Album();
                album.setAlbum_name(albums);
                album.setId(albumsId);
                album.setArtist(artist);
                album.setArtist_id(artistId);
                if (!set.contains(albumsId)) {
                    albumList.add(album);
                }

                set.add(albumsId);

            }

        }

        return albumList;


    }


    //获取全部歌手
    public List<Artist> getArtistSet(){
        Cursor cursor = WarauApp.getContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Audio.Media.DURATION + ">=18000", null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        List<Artist> list=new ArrayList<>();

        Set<Long> set=new HashSet<>();

        assert cursor!=null;
        for (int i=0;i<cursor.getCount();i++){
            cursor.moveToNext();
            String artist=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            long artistId=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
            int isMusic=cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
            String albums=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            if (isMusic!=0){
                Artist artist1=new Artist();
                artist1.setId(artistId);
                artist1.setName(artist);
                artist1.setAlbum(albums);

                if (!set.contains(artistId)){
                    list.add(artist1);
                }

                set.add(artistId);
            }
        }

        return list;

    }

    /**
     * 搜索数据库获取数据
     * @param album album
     * @return 返回音乐列表
     */
    public List<Music> getAlbumMusicList(Album album){

        Cursor cursor = WarauApp.getContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Audio.Media.ALBUM_ID + "="+album.getId(), null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        List<Music> musicList=new ArrayList<>();


        if (cursor!=null){

            for (int i=0;i<cursor.getCount();i++){

                cursor.moveToNext();

                String uri=cursor.getString(cursor.getColumnIndex((MediaStore.Audio.Media.DATA)));
                long _id=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String title=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String albums=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                long duration=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                int isMusic=cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
                int albumsId=cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                long artistId=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));

                long uniqueId=LongIdUtils.getRandomId();

                if (isMusic!=0){

                    MediaMetadataCompat.Builder builder=new MediaMetadataCompat.Builder();
                    builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION,duration);
                    builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE,title);
                    builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM,albums);
                    builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST,artist);
                    builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,uri);
                    builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, String.valueOf(_id));
                    builder.putLong(Conf.ALBUM_ID,albumsId);//设置id，方便查找封面
                    builder.putLong(Conf.UNIQUE_ID,uniqueId);//设置唯一id，方便查找position
                    builder.putLong(Conf.ARTIST_ID,artistId);


                    Music music=new Music();

                    music.setMediaMetadataCompat(builder.build());

                    musicList.add(music);

                }

                System.out.println("------->>>搜索结果");


            }


        }

        return musicList;

    }

    public List<Album> getArtistAlbums(Artist artist){

        Cursor cursor = WarauApp.getContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Audio.Media.DURATION + ">=18000", null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        Set<Integer> set=new HashSet<>();
        List<Album> albumList=new ArrayList<>();

        assert cursor!=null;
        for (int i=0;i<cursor.getCount();i++){
            cursor.moveToNext();
            String albums=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            int albumsId=cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
            int isMusic=cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
            String artists=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            long artistId=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
            if (isMusic!=0&&artist.getId()==artistId){//是音乐且歌手id一致

                Album album=new Album();
                album.setAlbum_name(albums);
                album.setId(albumsId);
                album.setArtist(artists);
                album.setArtist_id(artistId);
                if (!set.contains(albumsId)) {
                    albumList.add(album);
                }

                set.add(albumsId);

            }

        }

        return albumList;





    }


    public List<Music> getArtistMusic(Artist artist){

        Cursor cursor = WarauApp.getContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Audio.Media.ARTIST_ID + "="+artist.getId(), null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        List<Music> musicList=new ArrayList<>();


        if (cursor!=null){

            for (int i=0;i<cursor.getCount();i++){

                cursor.moveToNext();

                String uri=cursor.getString(cursor.getColumnIndex((MediaStore.Audio.Media.DATA)));
                long _id=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String title=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artists=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String albums=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                long duration=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                int isMusic=cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
                int albumsId=cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                long artistId=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
                long uniqueId=LongIdUtils.getRandomId();

                if (isMusic!=0){

                    MediaMetadataCompat.Builder builder=new MediaMetadataCompat.Builder();
                    builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION,duration);
                    builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE,title);
                    builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM,albums);
                    builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST,artists);
                    builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,uri);
                    builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, String.valueOf(_id));
                    builder.putLong(Conf.ALBUM_ID,albumsId);//设置id，方便查找封面
                    builder.putLong("uniqueId",uniqueId);//设置唯一id，方便查找position
                    builder.putLong(Conf.ARTIST_ID,artistId);


                    Music music=new Music();

                    music.setMediaMetadataCompat(builder.build());

                    musicList.add(music);

                }

            }

            cursor.close();

        }

        return musicList;

    }

    public Music getMusicById(int id){


        Cursor cursor = WarauApp.getContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Audio.Media._ID + "="+id, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);


        if (cursor!=null){

            for (int i=0;i<cursor.getCount();i++){

                cursor.moveToNext();

                String uri=cursor.getString(cursor.getColumnIndex((MediaStore.Audio.Media.DATA)));
                long _id=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String title=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artists=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String albums=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                long duration=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                int isMusic=cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
                int albumsId=cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                long artistId=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
                long uniqueId=LongIdUtils.getRandomId();

                if (isMusic!=0){

                    MediaMetadataCompat.Builder builder=new MediaMetadataCompat.Builder();
                    builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION,duration);
                    builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE,title);
                    builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM,albums);
                    builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST,artists);
                    builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,uri);
                    builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, String.valueOf(_id));
                    builder.putLong(Conf.ALBUM_ID,albumsId);//设置id，方便查找封面
                    builder.putLong("uniqueId",uniqueId);//设置唯一id，方便查找position
                    builder.putLong(Conf.ARTIST_ID,artistId);

                    Music music=new Music();

                    music.setMediaMetadataCompat(builder.build());

                    return music;

                }

            }

            cursor.close();

        }

        return null;

    }

    public List<Integer> getAlbumMusicId(Album album){

        List<Integer> integerList=new ArrayList<>();

        Cursor cursor = WarauApp.getContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Audio.Media.ALBUM_ID + "="+album.getId(), null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        if (cursor!=null){

            if (cursor.moveToFirst()){

                do {

                    long id=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));

                    integerList.add((int) id);

                }while (cursor.moveToNext());

            }

            cursor.close();
        }

        return integerList;

    }

    public List<Integer> getArtistMusicId(Artist artist){

        List<Integer> integerList=new ArrayList<>();

        Cursor cursor = WarauApp.getContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Audio.Media.ARTIST_ID + "="+artist.getId(), null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        if (cursor!=null){

            if (cursor.moveToFirst()){

                do {

                    long id=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));

                    integerList.add((int) id);

                }while (cursor.moveToNext());

            }

            cursor.close();
        }

        return integerList;

    }

    /**
     * 获取搜索音乐
     * @param keyword 搜索关键字
     * @return 返回搜索结果
     */
    public List<Music> getSearchMusic(String keyword){

        if (this.metadataCompatMap==null){
            return null;
        }

        List<Music> musicList=new ArrayList<>();

        Set<String> keys=this.metadataCompatMap.keySet();

        for (String id:keys){

            MediaMetadataCompat metadataCompat=this.metadataCompatMap.get(id);

            String title=metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE);

            if (title.contains(keyword)){

                Music music=new Music();

                music.setMediaMetadataCompat(metadataCompat);

                musicList.add(music);

            }

        }

        return musicList;


    }

    public List<Album> getSearchAlbum(String keyword){

        if (this.metadataCompatMap==null){
            return null;
        }

        List<Album> albumList=new ArrayList<>();

        Set<Long> strings=new HashSet<>();

        Set<String> keys=this.metadataCompatMap.keySet();

        for (String id:keys){

            MediaMetadataCompat metadataCompat=this.metadataCompatMap.get(id);

            String name=metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);


            if (name.contains(keyword)){

                long artist_id=metadataCompat.getLong(Conf.ARTIST_ID);//根据id来获取唯一的album

                if (!strings.contains(artist_id)){
                    long album_id=metadataCompat.getLong(Conf.ALBUM_ID);


                    String artist=metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);

                    Album album=new Album();

                    album.setAlbum_name(name);

                    album.setId(album_id);

                    album.setArtist_id(artist_id);
                    album.setArtist(artist);

                    albumList.add(album);
                }

                strings.add(artist_id);

            }

        }

        return albumList;

    }


    public List<Artist> getSearchArtist(String keyword){

        if (this.metadataCompatMap==null){
            return null;
        }

        List<Artist> artistList=new ArrayList<>();
        Set<String> keys=this.metadataCompatMap.keySet();

        Set<Long> strings=new HashSet<>();

        for (String id:keys){

            MediaMetadataCompat metadataCompat=this.metadataCompatMap.get(id);

            String name=metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);

            if (name.contains(keyword)){

                long artist_id=metadataCompat.getLong(Conf.ARTIST_ID);

                if (!strings.contains(artist_id)) {


                    Artist artist = new Artist();

                    artist.setName(name);

                    artist.setId(artist_id);

                    artistList.add(artist);
                }

                strings.add(artist_id);

            }

        }

        return artistList;


    }

}
