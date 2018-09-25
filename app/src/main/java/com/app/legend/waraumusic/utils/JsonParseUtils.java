package com.app.legend.waraumusic.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonParseUtils {

    public static String getAlbumBook(String json){

        String book="";

        try {
            JSONObject jsonObject=new JSONObject(json);
            if (jsonObject.has("result")){
                JSONObject result=jsonObject.getJSONObject("result");
                if (result.has("songs")){
                    JSONArray array=result.getJSONArray("songs");
                    if (array.length()>0) {//搜索结果大于0个
                        JSONObject one = array.getJSONObject(0);//选取第一个
                        if (one.has("al")) {//获取封面链接

                            JSONObject al=one.getJSONObject("al");

                            if (al.has("picUrl")){

                                book=al.getString("picUrl");
                            }


                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return book;

    }

    public static String getArtistBook(String json){

        String book="";


        try {
            JSONObject jsonObject=new JSONObject(json);

            if (jsonObject.has("result")){

                JSONObject result=jsonObject.getJSONObject("result");

                if (result.has("artists")){
                    JSONArray array=result.getJSONArray("artists");

                    if (array.length()>0){

                        JSONObject one=array.getJSONObject(0);

                        if (one.has("picUrl")){

                            book=one.getString("picUrl");//获取封面链接

                        }

                    }


                }


            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return book;

    }

    public static String[] getLrc(String json){

        String[] lrcs=new String[2];//原版歌词与翻译歌词

        try {
            JSONObject jsonObject=new JSONObject(json);

            if (jsonObject.has("lrc")){

                JSONObject lrc1=jsonObject.getJSONObject("lrc");

                if (lrc1.has("lyric")){

                    lrcs[0]=lrc1.getString("lyric");//获取原版歌词

                }

            }

            if (jsonObject.has("tlyric")){//有翻译歌词

                JSONObject tlrc=jsonObject.getJSONObject("tlyric");

                if (tlrc.has("lyric")){

                    lrcs[1]=tlrc.getString("lyric");//获取翻译歌词

                }

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return lrcs;

    }

    public static long getSongId(String json){

        long id=-1;

        try {
            JSONObject jsonObject=new JSONObject(json);

            if (jsonObject.has("result")){

                JSONObject result=jsonObject.getJSONObject("result");

                if (result.has("songs")){

                    JSONArray array=result.getJSONArray("songs");

                    if (array.length()>0){

                        JSONObject one=array.getJSONObject(0);

                        if (one.has("id")){

                            id=one.getLong("id");

                        }


                    }

                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return id;

    }

}
