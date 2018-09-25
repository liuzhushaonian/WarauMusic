package com.app.legend.waraumusic.utils;

import android.os.Environment;
import android.support.v4.media.MediaMetadataCompat;
import android.text.TextUtils;
import android.util.Log;
import com.app.legend.waraumusic.bean.Lrc;
import com.app.legend.waraumusic.bean.Music;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 歌词管理器，负责管理，解析歌词
 *
 * 歌词获取流程：
 * 首先以歌曲名字获取本地歌词以及本地翻译歌词，如果存在，则获取并解析，返回解析结果
 * 如果本地不存在，则开启网络查找，保存在本地
 * 再次开启解析，并返回结果
 *
 * Created by legend on 2018/3/6.
 */

public class LyricManager {

    private static final String LRC_PATH = Environment.getExternalStorageDirectory().
            getAbsolutePath() + "/" +Conf.FILE_PATH+"/"+Conf.LRC_PATH;//歌词存放路径

    private static volatile LyricManager manager;

    public static LyricManager getManager() {

        if (manager == null) {
            synchronized (LyricManager.class) {
                manager = new LyricManager();
                File file = new File(LRC_PATH);
                if (!file.exists()) {
                    file.mkdirs();
                }
            }
        }
        return manager;
    }


    //获取本地歌词
    //必须在线程内执行
    //返回结果或许为null
    public String[] getLrc(MediaMetadataCompat metadataCompat){

        String title=metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE);

        String[] lrcs=getLocalLrc(title);

        if (lrcs[0]==null&&lrcs[1]==null){//本地不存在歌词,开启网络搜索获取

            return getNetLrc(title);


        }else{//本地存在歌词

            return lrcs;
        }

    }


    //获取网络歌词并保存在本地
    private String[] getNetLrc(String name){


        String url = "?type=search&s=" + name;//首先获取歌曲的id

        String json = NetUtils.getJson(url);

        long id=JsonParseUtils.getSongId(json);

        if (id==-1){
            return null;
        }

        String url2="/?type=lyric&id="+id;//用获取到的id来获取歌词

        String json2=NetUtils.getJson(url2);

        String[] lrcs = JsonParseUtils.getLrc(json2);

        for (int i=0;i<lrcs.length;i++){

            if (i==0){

                String l1=lrcs[0];

                if (l1!=null){

                    saveLrc(l1,name);//保存原版歌词

                }

            }

            if (i==1){
                String l2=lrcs[1];
                if (l2!=null&& !TextUtils.isEmpty(l2)&&!l2.equals("null")){//否认三连 我不是 我没有 别瞎说啊
                    String name2="t-"+name;
                    saveLrc(l2,name2);
                }
            }
        }


        return lrcs;


    }

    private void saveLrc(String lrc,String name){


        String lrc1=LRC_PATH+"/"+name+".lrc";

        try {

            FileOutputStream outputStream=new FileOutputStream(lrc1);

            outputStream.write(lrc.getBytes());

            outputStream.flush();

            outputStream.close();

        }catch (IOException e){

            e.printStackTrace();

        }



    }



    //获取本地两份歌词（或许只有一份，或许一份都没有，谁知道呢~）
    private String[] getLocalLrc(String name){

        String[] lrcs=new String[2];

        String l1=LRC_PATH+"/"+name+".lrc";

        String l2=LRC_PATH+"/t-"+name+".lrc";

        File file=new File(l1);

        File file1=new File(l2);

        if (file.exists()){

            try {
                InputStream inputStream=new FileInputStream(file);

                byte[] bytes=new byte[inputStream.available()];

                int i=inputStream.read(bytes);

                inputStream.close();

                lrcs[0]=new String(bytes);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        if (file1.exists()){

            try {
                InputStream inputStream=new FileInputStream(file1);

                byte[] bytes=new byte[inputStream.available()];

                int i=inputStream.read(bytes);

                inputStream.close();

                lrcs[1]=new String(bytes);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        return lrcs;

    }




    /**
     * 解析歌词
     *
     * @param lrc
     */
    public List<Lrc> parseLrc(String lrc) {
        String[] strings = lrc.split("\\n");

        List<Lrc> lrcList = new ArrayList<>();

        for (int i = 0; i < strings.length; i++) {
            List<Lrc> list = parseLine(strings[i]);
            if (list != null) {
                lrcList.addAll(list);
            }
        }


        Collections.sort(lrcList, new TimeComparator());

//        for (int o=0;o<lrcList.size();o++){
//            Log.d("lrc--->>>",lrcList.get(o).getTime()+"");
//        }
        return lrcList;


    }


    private List<Lrc> parseLine(String line) {

        List<Lrc> lrcList = new ArrayList<>();

//        Log.d("line---->>",line);

        if (line.startsWith("[al")) {
//            lrc.setContent(line.substring(4,line.length()-1));
//            lrc.setTime(0);
//            lrcList.add(lrc);
            return lrcList;
        } else if (line.startsWith("[ar")) {
//            lrc.setTime(0);
//            lrc.setContent();
            return lrcList;
        } else if (line.startsWith("[au")) {
            return lrcList;
        } else if (line.startsWith("[by")) {
            return lrcList;
        } else if (line.startsWith("[offset")) {
            return lrcList;
        } else if (line.startsWith("[re")) {
            return lrcList;
        } else if (line.startsWith("[ti")) {
            return lrcList;
        } else if (line.startsWith("[ve")) {
            return lrcList;
        } else {

            // 设置正则规则
            String reg = "\\[(\\d{1,2}:\\d{1,2}\\.\\d{1,2})\\]|\\[(\\d{1,2}:\\d{1,2}\\.\\d{1,3})\\]|\\[(\\d{1,2}:\\d{1,2})\\]";
            // 编译
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(line);

            if (!matcher.find()) {
                return null;
            }


            //解析开始
            String[] strings = line.split("]");

//            Log.d("size---->>",strings.length+"");

            for (int i = 0; i < strings.length; i++) {

                String content = strings[strings.length - 1];//获取最后一项
                String c = "";

                /**
                 * 歌词的形式
                 * [00:01.20][00:03.45][04:20.33]你一直在我心中
                 * 以"]"为分割，可获得    [00:01.20、[00:03.45、[04:20.33、你一直在我心中    等字符串数组
                 * 然后以"["为开头的则是时间，没有则是歌词本身
                 * 最后只需去掉"["再转为long即可
                 */
                if (!content.startsWith("[")) {
                    //表示这是个歌词
                    c = content;
                }


                String s = strings[i];
                if (s.startsWith("[")) {
                    //时间部分
                    s = s.substring(1, s.length());
//                    Log.d("time--->>",s);

                    Lrc lrc = new Lrc();
                    lrc.setTime(timeConvert(s));
                    lrc.setContent(c);

                    lrcList.add(lrc);
                }

            }

        }
        return lrcList;

    }


    private static long timeConvert(String timeString) {
        //因为给如的字符串的时间格式为XX:XX.XX,返回的long要求是以毫秒为单位
        //将字符串 XX:XX.XX 转换为 XX:XX:XX
//        timeString=timeString.replace("[","");
//        timeString=timeString.replace("]","");
        timeString = timeString.replace('.', ':');
        //将字符串 XX:XX:XX 拆分
        String[] times = timeString.split(":");

        Log.d("time---->>>",timeString);

        int t=0;

        for (int i=0;i<times.length;i++){
            if (i==0){
                t=t+Integer.valueOf(times[i])*60*1000;
            }

            if (i==1){

                t=t+Integer.valueOf(times[i])*1000;
            }

            if (i==2){
                t=t+Integer.valueOf(times[i]);
            }

        }

        return t;

    }

    private static class TimeComparator implements Comparator {


        @Override
        public int compare(Object o1, Object o2) {
            Lrc l1 = (Lrc) o1;
            Lrc l2 = (Lrc) o2;
            return Long.compare(l1.getTime(), l2.getTime());
        }
    }



}
