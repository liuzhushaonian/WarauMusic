package com.app.legend.waraumusic.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 *
 * 网络工具，获取json数据
 */
public class NetUtils {

    private static final String API="https://api.imjad.cn/cloudmusic/";

    public static final int SEARCH=10;

    public static final int ALBUM=20;


    public static String getJson(String url){

        String result="";

        url=API+url;

        try {

            Request.Builder builder=new Request.Builder().url(url).method("GET",null);

            Request request=builder.build();

            File sdcard= WarauApp.getContext().getExternalCacheDir();

            int cacheSize=100*1024*1024;

            OkHttpClient.Builder builder1= null;
            builder1 = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20,TimeUnit.SECONDS);

            if (sdcard!=null){
                builder1.cache(new Cache(sdcard.getAbsoluteFile(),cacheSize));
            }

            OkHttpClient okHttpClient=builder1.build();

            Call call=okHttpClient.newCall(request);

            Response response= null;

            try {
                response = call.execute();
                result=response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }catch (Exception e){
            e.printStackTrace();
        }


        return result;
    }


    /**
     * 下载图片
     * @param url 图片链接
     * @param name 图片名字
     * @param path 图片存放路径
     */
    public static void getBitmap(String url,long name,String path){

        if (TextUtils.isEmpty(url)){
            return;
        }


        try {

            Request.Builder builder=new Request.Builder().url(url).method("GET",null);

            Request request=builder.build();

            File sdcard= WarauApp.getContext().getExternalCacheDir();

            int cacheSize=100*1024*1024;

            OkHttpClient.Builder builder1= null;
            builder1 = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20,TimeUnit.SECONDS);

            if (sdcard!=null){
                builder1.cache(new Cache(sdcard.getAbsoluteFile(),cacheSize));
            }

            OkHttpClient okHttpClient=builder1.build();

            Call call=okHttpClient.newCall(request);

            Response response= null;

            try {
                response = call.execute();

                String p=path+"/"+name;//拼接成路径

                File file=new File(p);

                FileOutputStream fileOutputStream=new FileOutputStream(file);

                FileChannel fileChannel=fileOutputStream.getChannel();

                ByteBuffer byteBuffer=ByteBuffer.allocate(1024);

                byte[] bytes=response.body().bytes();

                for (byte b:bytes){

                    if (byteBuffer.hasRemaining()){

                        byteBuffer.put(b);

                    }else {

                        byteBuffer.flip();

                        fileChannel.write(byteBuffer);//写入文件

                        byteBuffer.compact();

                        byteBuffer.put(b);//放入buffer

                    }

                }

                byteBuffer.flip();

                fileChannel.write(byteBuffer);//写入文件

                byteBuffer.clear();

                fileChannel.close();

                fileOutputStream.close();



            } catch (IOException e) {
                e.printStackTrace();
            }


        }catch (Exception e){
            e.printStackTrace();
        }

    }


    public static int getAPNType(){

        int netType = -1;
        ConnectivityManager connMgr = (ConnectivityManager) WarauApp.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connMgr==null){
            return netType;
        }

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();


        if(networkInfo==null){
            return netType;
        }
        int nType = networkInfo.getType();
        if(nType==ConnectivityManager.TYPE_MOBILE){

            if(networkInfo.getExtraInfo().toLowerCase().equals("cmnet")){
                netType = 0;
            }
            else{
                netType = 1;
            }
        } else if(nType== ConnectivityManager.TYPE_WIFI){//重点获取WiFi以及无网络状态
            netType = 2;
        }
        return netType;
    }

}
