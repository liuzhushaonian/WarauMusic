package com.app.legend.waraumusic.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.app.legend.waraumusic.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by legend on 2017/9/13.
 */

public class ImageLoader {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXNUM_POOL_SIZE = CPU_COUNT * 4 + 1;
    private static final long KEEP_ALIVE = 10L;

    private LruCache<String, Bitmap> lruCache;

    private static ImageLoader imageLoader;

    private static String CACHE_PATH = "";//文件缓存

    static Context context;

    public static final int SMALL = 0x00100;

    public static final int ALBUM = 0x00300;

    public static final int BIG = 0x00600;

    public static final int PLAY=0x21000;

    private boolean isScroll=false;

    public boolean isScroll() {
        return isScroll;
    }

    public void setScroll(boolean scroll) {
        isScroll = scroll;
    }

    private ImageLoader() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;

        lruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {

                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };
    }


    //单例模式
    public static ImageLoader getImageLoader(Context con) {
        if (imageLoader == null) {
            imageLoader = new ImageLoader();
            context = con;
            CACHE_PATH = context.getFilesDir().getAbsolutePath();
        }

        return imageLoader;
    }


    private static final ThreadFactory mThreadFactory = new ThreadFactory() {
        private final AtomicInteger count = new AtomicInteger(1);

        @Override
        public Thread newThread(@NonNull Runnable runnable) {
            return new Thread(runnable, "ImageLoader#" + count.getAndIncrement());
        }
    };


    private static final Executor ThreadPool = new ThreadPoolExecutor(
            CORE_POOL_SIZE, MAXNUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>(), mThreadFactory);

    //设置image
    public void setImage(String url, ImageView imageView, int reqWidth, int reqHeight) {

//        bindImage(imageView, reqWidth, reqHeight, url);

    }

    //加载本地image
    public void setImage(int res, ImageView imageView, int reqWidth, int reqHeight) {

        bindDrawableImage(res, imageView, reqWidth, reqHeight);
    }

    //加载drawable里的资源
    private void bindDrawableImage(final int res, final ImageView imageView, final int reqWidth, final int reqHeight) {
        Observable
                .create(new ObservableOnSubscribe<Bitmap>() {
                    @Override
                    public void subscribe(ObservableEmitter<Bitmap> e) throws Exception {

                        imageView.setTag(res);


                        BitmapFactory.Options options = new BitmapFactory.Options();

                        options.inJustDecodeBounds = true;

                        BitmapFactory.decodeResource(context.getResources(), res, options);

                        options.inSampleSize = reSize(options, reqWidth, reqHeight);

                        options.inJustDecodeBounds = false;

                        options.inPreferredConfig = Bitmap.Config.RGB_565;

                        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), res, options);

                        e.onNext(bitmap);

                        e.onComplete();

                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Bitmap>() {

                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        if (imageView != null) {
                            imageView.setImageBitmap(bitmap);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        if (!disposable.isDisposed()) {
                            disposable.dispose();
                        }
                    }
                });

    }


    //清除缓存
    public void clean() {
        lruCache.evictAll();

        File file = new File(CACHE_PATH);

        if (file != null && file.exists() && file.isDirectory()) {

            for (File file1 : file.listFiles()) {
                file1.delete();
            }
        }
    }


    /**
     * 根据URL获取网络图片
     *
     * @param url
     * @return
     */
    private Bitmap getBitmapFromNet(String url, int reqWidth, int reqHeight) {


        Request.Builder builder1 = new Request.Builder().url(url).method("GET", null);

        Request request = builder1.build();

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS);

        OkHttpClient okHttpClient = builder.build();

        Call call = okHttpClient.newCall(request);

        InputStream inputStream = null;

        Bitmap bitmap = null;

        try {
            Response response = call.execute();

            inputStream = response.body().byteStream();

//            bitmap=writeToDisk(inputStream,url,reqWidth,reqHeight);

            BitmapFactory.Options options = new BitmapFactory.Options();

            options.inPreferredConfig = Bitmap.Config.RGB_565;


            options.inSampleSize = 2;

            bitmap = BitmapFactory.decodeStream(inputStream, null, options);


            Log.d("internetSize--------->", bitmap.getByteCount() + "");


        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
//        return Bitmap.createScaledBitmap(bitmap,bitmap.getWidth()/2,bitmap.getHeight()/2,false);

    }


    //真·设置image的地方
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {


//            super.handleMessage(msg);
            switch (msg.what) {

                case SMALL:

                    setSmallImage((LoaderResult) msg.obj);

                    break;


                case ALBUM:

                    setAlbumImage((LoaderResult) msg.obj);


                    break;

                case BIG:

                    setPagerImage((LoaderResult) msg.obj);

                    break;


                case PLAY:

                    setPlayImage((LoaderResult) msg.obj);

                    break;

            }


        }
    };


    private void setSmallImage(LoaderResult loaderResult) {

        String url = loaderResult.url;


        ImageView imageView = loaderResult.imageView;

        Bitmap bitmap = loaderResult.bitmap;

//            Bitmap bitmap=Bitmap.createScaledBitmap(b,b.getWidth()/2,b.getHeight()/2,false);


        if (null != bitmap&&imageView.getTag().equals(getMd5(url))) {

//                bitmap=Bitmap.createScaledBitmap(bitmap,loaderResult.width,loaderResult.height,false);

            imageView.setImageBitmap(bitmap);

            //双重缓存
            cacheInMemory(bitmap, url, loaderResult.width, loaderResult.height);
            cacheImageInDisk(bitmap, url, loaderResult.width, loaderResult.height);

        }else {//没有图片


            imageView.setImageResource(R.drawable.ic_music_note_black_24dp);

        }

    }

    private void setPlayImage(LoaderResult loaderResult){

        String url = loaderResult.url;


        ImageView imageView = loaderResult.imageView;

        Bitmap bitmap = loaderResult.bitmap;

        if (null != bitmap&&imageView.getTag().equals(getMd5(url))) {

            imageView.setVisibility(View.VISIBLE);

            imageView.setImageBitmap(bitmap);

            //双重缓存
            cacheInMemory(bitmap, url, loaderResult.width, loaderResult.height);
            cacheImageInDisk(bitmap, url, loaderResult.width, loaderResult.height);

        }else {//没有图片


//            imageView.setImageResource(R.drawable.ic_music_note_black_24dp);

            imageView.setVisibility(View.GONE);

        }


    }

    private void setPagerImage(LoaderResult loaderResult){

        String url = loaderResult.url;


        ImageView imageView = loaderResult.imageView;

        Bitmap bitmap = loaderResult.bitmap;

        if (null != bitmap) {

//            imageView.setVisibility(View.VISIBLE);


            imageView.setScaleType(ImageView.ScaleType.CENTER);
            imageView.setImageBitmap(bitmap);

            //双重缓存
            cacheInMemory(bitmap, url, loaderResult.width, loaderResult.height);
            cacheImageInDisk(bitmap, url, loaderResult.width, loaderResult.height);

        }else {//没有图片


            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageResource(R.drawable.ic_audiotrack_black_100dp);

//            imageView.setVisibility(View.GONE);

        }


    }

    private void setAlbumImage(LoaderResult loaderResult){

        String url = loaderResult.url;


        ImageView imageView = loaderResult.imageView;

        Bitmap bitmap = loaderResult.bitmap;

        if (null != bitmap&&imageView.getTag().equals(getMd5(url))) {

//            imageView.setVisibility(View.VISIBLE);

            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageBitmap(bitmap);

            //双重缓存
            cacheInMemory(bitmap, url, loaderResult.width, loaderResult.height);
            cacheImageInDisk(bitmap, url, loaderResult.width, loaderResult.height);

        }else {//没有图片


            imageView.setScaleType(ImageView.ScaleType.CENTER);
            imageView.setImageResource(R.drawable.ic_audiotrack_black_100dp);

//            imageView.setVisibility(View.GONE);

        }


    }




    private static class LoaderResult {
        private ImageView imageView;
        private Bitmap bitmap;
        private String url;
        int width, height;


        public LoaderResult(ImageView imageView, Bitmap bitmap, String url, int reqWidth, int reqHeight) {
            this.imageView = imageView;
            this.bitmap = bitmap;
            this.url = url;
            this.width = reqWidth;
            this.height = reqHeight;

        }
    }

    //开线程执行放置image
//    private void bindImage(final String url,final ImageView imageView,final int reqWidth,final int reqHeight) {
//
//
//        //先查找内存缓存以及本地缓存，如果有则设置并返回，没有则开启网络查找
//
//
//        Observable.create(new ObservableOnSubscribe<Bitmap>() {
//            @Override
//            public void subscribe(ObservableEmitter<Bitmap> e) throws Exception {
//
//                Bitmap bitmap = null;
//
//                imageView.setTag(url);
//
//                bitmap = getBitmapFromMemory(url, reqWidth, reqHeight);
//
//                if (bitmap == null) {
//                    bitmap = getBitmapFromDisk(url, reqWidth, reqHeight);
//
////                    Log.d("info1-------------->",bitmap.getByteCount()+"");
//                }
//
//                if (bitmap == null) {
//
////                    Log.d("info2-------->","it is null!");
//                    bitmap = getBitmapFromNet(url, reqWidth, reqHeight);
//
//                    if (bitmap != null) {//写入缓存，本地&内存
//                        cacheInMemory(bitmap, url);
//                        cacheImageInDisk(bitmap, url);
//                    }
//                }
//
//                e.onNext(bitmap);
//
//
//            }
//        })
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<Bitmap>() {
//                    @Override
//                    public void accept(Bitmap bitmap) throws Exception {
//
//                        if (null != bitmap && imageView.getTag().equals(url)) {
//                            imageView.setImageBitmap(bitmap);
//                        }
//                    }
//                });
//
//    }

//        bitmap=getBitmapFromMemory(url,reqWidth,reqHeight);
//
//        if (null!=bitmap&&imageView.getTag().equals(url)){
//
//            imageView.setImageBitmap(bitmap);
//            return;
//        }
//
////        if (isScroll){
////            return;
////        }
//
//        bitmap=getBitmapFromDisk(url,reqWidth,reqHeight);
//
//        if (null!=bitmap&&imageView.getTag().equals(url)){
//
//            Log.d("size------->",(bitmap.getByteCount())+"");
//
//            imageView.setImageBitmap(bitmap);
//            return;
//        }
//
//
//
//
//

    private void bindImage(final ImageView imageView, final int reqWidth, final int reqHeight, final String url, int type) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                Bitmap bitmap = null;

                imageView.setTag(getMd5(url));

                bitmap = getBitmapFromMemory(url, reqWidth, reqHeight);

                if (bitmap == null) {

                    bitmap = getBitmapFromDisk(url, reqWidth, reqHeight);

                }

                if (bitmap == null) {

                    bitmap = getBitmapFromLocal(url, reqWidth, reqHeight);

                }

//                if (null != bitmap) {

                LoaderResult loaderResult = new LoaderResult(imageView, bitmap, url, reqWidth, reqHeight);

                handler.obtainMessage(type, loaderResult).sendToTarget();

//                }
            }
        };

        ThreadPool.execute(runnable);//放入线程池

    }


    /**
     * 本地缓存
     *
     * @param bitmap
     * @param url    MD5加密命名
     */
    private void cacheImageInDisk(Bitmap bitmap, String url, int w, int h) {
        String name = getMd5(url) + h + "just" + w;

        try {


            File file = new File(CACHE_PATH, name);


            File parentFile = file.getParentFile();

            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }

            bitmap.compress(Bitmap.CompressFormat.WEBP, 100, new FileOutputStream(file));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * 从本地读取缓存
     *
     * @param url
     * @return
     */
    private Bitmap getBitmapFromDisk(String url, int reqWidth, int reqHeight) {
        Bitmap bitmap = null;
        String name = getMd5(url) + reqHeight + "just" + reqWidth;

        File file = new File(CACHE_PATH).getAbsoluteFile();

        String file_path = file + "/" + name;

        try {

            BitmapFactory.Options options = new BitmapFactory.Options();


//        options.inSampleSize=2;
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            options.inJustDecodeBounds = true;

            bitmap = BitmapFactory.decodeFile(file_path, options);

            options.inSampleSize = reSize(options, reqWidth, reqHeight);

            options.inJustDecodeBounds = false;

            bitmap = BitmapFactory.decodeFile(file_path, options);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != bitmap) {
                //还需要进行内存缓存
                cacheInMemory(bitmap, url, reqWidth, reqHeight);
            }

            return bitmap;
        }
    }

    /**
     * 写入内存缓存
     *
     * @param bitmap
     * @param url
     */
    private void cacheInMemory(Bitmap bitmap, String url, int w, int h) {
        String name = getMd5(url) + h + "just" + w;

        lruCache.put(name, bitmap);
    }

    /**
     * 读取内存缓存
     *
     * @param url
     * @return
     */
    private Bitmap getBitmapFromMemory(String url, int reqWidth, int reqHeight) {
        String name = getMd5(url) + reqHeight + "just" + reqWidth;

        Bitmap bitmap = lruCache.get(name);

        return bitmap;
    }


    //md5加密改名
    private String getMd5(String plainText) {

        if (plainText==null){
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            //32位加密
            return buf.toString();
            // 16位的加密
            //return buf.toString().substring(8, 24);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

    }


    private int reSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int size = 1;

        int width = options.outWidth;

        int height = options.outHeight;


        if (height > reqHeight / 2 || width > reqWidth / 2) {
            int halfHeight = height / 2;

            int halfWidth = width / 2;

            while ((halfHeight / size) >= reqHeight &&
                    (halfWidth / size) >= reqWidth) {
                size *= 2;
            }
        }

        return size;
    }


    private static String getAlbumArt(long album_id) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[]{"album_art"};
        Cursor cursor = WarauApp.getContext().getContentResolver().query(
                Uri.parse(mUriAlbums + "/" + album_id), projection, null, null, null);
        String albums_art = null;
        if (cursor.getCount() > 0 && cursor.getColumnCount() > 0) {
            cursor.moveToNext();
            albums_art = cursor.getString(0);
        }
        cursor.close();
        cursor = null;


        if (albums_art==null){//数据库并没有图片，从本地文件获取

            String path= Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Conf.FILE_PATH+"/"+Conf.ALBUM_PATH+"/"+album_id;

            albums_art=path;

        }


        return albums_art;
    }

    public static String getUrl(long id){

        return getAlbumArt(id);

    }


    // 取Bitmap
    private Bitmap getBitmapFromLocal(String url, int width, int height) {

        Bitmap bitmap = BitmapFactory.decodeFile(url);

        if (bitmap != null) {

            cacheImageInDisk(bitmap, url, width, height);//先缓存到本地，避免本地缓存图片格式被破坏

            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);//再按照规格压缩图片

            cacheInMemory(bitmap, url, width, height);//最后把压缩好的图一并缓存到内存里
        }

        return bitmap;

    }

    /**
     * 设置列表图片
     */
    public void setListImage(ImageView imageView, int w, int h, MediaMetadataCompat metadataCompat,int type) {

        long id = metadataCompat.getLong("albumId");

        String url = getAlbumArt(id);

        bindImage(imageView, w, h, url,type);

    }

    /**
     * 设置album列表图片
     *
     */
    public void setAlbumListImage(ImageView imageView, int w, int h, long albumId, int type){


        String url=getAlbumArt(albumId);

        bindImage(imageView, w, h, url,type);

    }



    public Bitmap getBitmap(long id){

        String url=getAlbumArt(id);

        return BitmapFactory.decodeFile(url);

    }

    public static String getArtist(long id){

        return Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Conf.FILE_PATH+"/"+Conf.ARTIST_PATH+"/"+id;

    }

}
