package com.app.legend.waraumusic.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class LongIdUtils {

    public static long getRandomId(){

        long now = System.currentTimeMillis();
        //获取4位年份数字
        SimpleDateFormat dateFormat=new SimpleDateFormat("MMddhhmmssSS");

        String time=dateFormat.format(now);

        //获取时间戳
        //获取三位随机数
        int ran=(int) ((Math.random()*9+1)*10000);
        //要是一段时间内的数据连过大会有重复的情况，所以做以下修改


        return Long.parseLong(time+ran);

    }


}
