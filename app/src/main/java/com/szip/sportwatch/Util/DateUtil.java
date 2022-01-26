package com.szip.sportwatch.Util;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import java.util.TimeZone;

/**
 * 处理时间的工具类
 * */
public class DateUtil {

    public static ArrayList<String> getMonthList() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            list.add(String.format(Locale.ENGLISH,"%02d", i));
        }
        return list;
    }

    public static ArrayList<String> getDayList(int year, int month) {
        int day = 0;
        ArrayList<String> list = new ArrayList<>();
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                day = 31;
                break;
            case 4:
            case 6:
            case 9:
            case 11:
                day = 30;
                break;
            case 2:
                day = year % 4 == 0 ? 29 : 28;
            default:
                break;
        }

        for (int i = 1; i <= day; i++) {
            list.add(String.format(Locale.ENGLISH,"%02d", i));
        }
        return list;


    }

    public static ArrayList<String> getYearList() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i <= Integer.valueOf(getCurrentYear()) - 1930; i++) {
            list.add(String.format(Locale.ENGLISH,"%4d", i + 1930));
        }
        return list;
    }

    public static ArrayList<String> getStature() {
        ArrayList<String> list = new ArrayList<>();

        for (int i = 0; i < 179; i++) {
            list.add(String.format(Locale.ENGLISH,"%d", i + 50));
        }

        return list;
    }

    public static ArrayList<String> getStatureWithBritish() {

        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 70; i++) {
            list.add(String.format(Locale.ENGLISH,"%d", i + 20));
        }
        return list;
    }

    public static ArrayList<String> getWeight() {

        ArrayList<String> list1 = new ArrayList<>();


        for (int i = 0; i < 199; i++) {
            list1.add(String.format(Locale.ENGLISH,"%d", i + 30));
        }
        return list1;
    }

    public static ArrayList<String> getWeightWithBritish() {

        ArrayList<String> list2 = new ArrayList<>();

        for (int i = 0; i < 437; i++) {
            list2.add(String.format(Locale.ENGLISH,"%d", 67 + i));
        }

        return list2;
    }

    public static String getCurrentYear() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy",Locale.ENGLISH);
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR,-14);
        date.setTime(calendar.getTimeInMillis());
        return sdf.format(date);
    }

    //出生日期字符串转化成Date对象
    public static Date parse(String strDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH);
        return sdf.parse(strDate);
    }

    //由出生日期获得年龄
    public static int getAge(Date birthDay) throws Exception {
        Calendar cal = Calendar.getInstance();

        if (cal.before(birthDay)) {
            throw new IllegalArgumentException(
                    "The birthDay is before Now.It's unbelievable!");
        }
        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH);
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTime(birthDay);

        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH);
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);

        int age = yearNow - yearBirth;

        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth) {
                if (dayOfMonthNow < dayOfMonthBirth) age--;
            } else {
                age--;
            }
        }
        return age;
    }

    /**
     * 时间戳转换成字符窜
     *
     * @param milSecond
     * @return
     */
    public static String getDateToString(int milSecond) {
        long time = ((long) milSecond) * 60 * 60 * 24 * 1000;
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH);
        return format.format(date);
    }


    /**
     * 格式化时间显示
     * */
    public static Spannable initText(String text,boolean flag){
        if (flag){
            Spannable span = new SpannableString(text);
            int i = text.indexOf('h');
            if (i>=0){
                span.setSpan(new RelativeSizeSpan(1.5f), 0, i, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            i = text.indexOf("min");
            if (i>=0){
                span.setSpan(new RelativeSizeSpan(1.5f), i-2, i, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return span;
        }else {
            Spannable span = new SpannableString(text);
            span.setSpan(new RelativeSizeSpan(1.5f), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return span;
        }
    }

    public static int getGMT() {
        TimeZone tz = TimeZone.getDefault();
        int offsetMinutes = tz.getOffset(System.currentTimeMillis()) / 60000;
        return offsetMinutes;
    }

    public static String getGMTWithString(){
        TimeZone tz = TimeZone.getDefault();
        int offsetMinutes = tz.getOffset(System.currentTimeMillis()) / 60000;
        return String.format(Locale.ENGLISH,"%d",offsetMinutes);
    }


    /**
     * 获取当前日期00点的时间戳
     * @param dateStr 日期
     * @return 返回这一天0点到24点的时间戳
     * */
    public static long getTimeScopeForDay(String dateStr,String tag){
        SimpleDateFormat dateFormat = new SimpleDateFormat(tag,Locale.ENGLISH);
        Date date = new Date();
        try {
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        return calendar.getTimeInMillis()/1000;
    }

    /**
     * 获取当前日期00点的时间戳
     * @param dateStr 日期
     * @return 返回这一天0点到24点的时间戳
     * */
    public static long getTimeScopeForDay(long time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time*1000);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        return calendar.getTimeInMillis()/1000;
    }

    /**
     * 获取当前日期00点的时间戳
     * @param dateStr 日期
     * @return 返回这一天0点到24点的时间戳
     * */
    public static long getSleepTimeScopeForDay(long time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time*1000);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        return hour>=21?(calendar.getTimeInMillis()/1000+24*60*60):(calendar.getTimeInMillis()/1000);
    }

    public static long getTimeScope(String dateStr,String tag){
        SimpleDateFormat dateFormat = new SimpleDateFormat(tag,Locale.ENGLISH);
        Date date = new Date();
        try {
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getTimeInMillis()/1000;
    }

    /**
     * 拿今天的时间戳
     * */
    public static long getTimeOfToday(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);

        return calendar.getTimeInMillis()/1000;
    }

    /**
     * 时间转为分钟
     * @param time 时间
     * */
    public static int getMinue(String time){
        int index = time.indexOf(':');
        return Integer.valueOf(time.substring(0,index))*60+Integer.valueOf(time.substring(index+1));
    }

    /**
     * 时间戳（到秒）转换成字符窜
     *
     * @param milSecond
     * @return
     */
    public static String getStringDateFromSecond(long milSecond,String formatStyle) {
        long time = milSecond * 1000;
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat(formatStyle,Locale.ENGLISH);
        return format.format(date);
    }

    /**
     * 时间戳（到秒）转换成字符窜
     *
     * @param milSecond
     * @return
     */
    public static String getStringDateFromSecond1(long milSecond,String formatStyle) {
        long time = milSecond * 1000;
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat(formatStyle);
        return format.format(date);
    }

    /**
     * 判断这段时间的睡眠数据是属于哪一天
     *
     * @param data 详情睡眠数据
     * @return
     */
    public static String getSleepDate(String data) {
        String datas[] = data.split("\\|");
        if (getMinue(datas[1])>1260){
            long time = getTimeScopeForDay(datas[0],"yyyy-MM-dd");
            return getStringDateFromSecond(time+24*60*60,"yyyy-M-d");
        } else {
            return datas[0];
        }
    }


    /**
     * byte[]转变为16进制String字符, 每个字节2位, 不足补0
     */
    public static String byteToHexString(byte[] bytes) {
        String result = null;
        String hex = null;
        if (bytes != null && bytes.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(bytes.length);
            for (byte byteChar : bytes) {
                hex = Integer.toHexString(byteChar & 0xFF);
                if (hex.length() == 1) {
                    hex = '0' + hex;
                }
                stringBuilder.append(hex.toUpperCase());
            }
            result = stringBuilder.toString();
        }
        return result;
    }

    public static int[] getNowDate() {
        int gmt = DateUtil.getGMT();
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String date = mFormat.format(new Date(System.currentTimeMillis()));
        Log.d("DATA******","date = "+date);
        String[] dateArray = date.split("-");
        int[] time = new int[dateArray.length + 2];
        for (int i = 0; i < dateArray.length; i++) {
            time[i] = Integer.valueOf(dateArray[i]);
        }
        int gmtData = (int) (gmt/60f*10);
        time[6] = gmtData>=0?1:0;
        time[7] = (byte)gmtData<0?gmtData*-1:gmtData;
        return time;

    }

}

