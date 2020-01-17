package com.szip.sportwatch.Util;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.PowerManager;
import android.os.StatFs;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.widget.Toast;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.szip.sportwatch.DB.dbModel.BloodOxygenData;
import com.szip.sportwatch.DB.dbModel.BloodOxygenData_Table;
import com.szip.sportwatch.DB.dbModel.BloodPressureData;
import com.szip.sportwatch.DB.dbModel.BloodPressureData_Table;
import com.szip.sportwatch.DB.dbModel.EcgData;
import com.szip.sportwatch.DB.dbModel.EcgData_Table;
import com.szip.sportwatch.DB.dbModel.HeartData;
import com.szip.sportwatch.DB.dbModel.HeartData_Table;
import com.szip.sportwatch.DB.dbModel.SleepData;
import com.szip.sportwatch.DB.dbModel.SleepData_Table;
import com.szip.sportwatch.DB.dbModel.SportData;
import com.szip.sportwatch.DB.dbModel.SportData_Table;
import com.szip.sportwatch.DB.dbModel.StepData;
import com.szip.sportwatch.DB.dbModel.StepData_Table;
import com.szip.sportwatch.Model.UserInfo;
import com.szip.sportwatch.Notification.AppList;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.szip.sportwatch.MyApplication.FILE;

/**
 * Created by Administrator on 2019/1/28.
 */

/**
 * 处理数据的工具类
 * */
public class MathUitl {

    /**
     * 格式化字符串显示样式
     * */
    public static Spannable initText(String text, int flag,String split,String split1){
        if (flag == 0){
            Spannable span = new SpannableString(text);
            if (split!=null){
                int i = text.indexOf(split);
                int m = text.indexOf(split1);
                span.setSpan(new RelativeSizeSpan(1.5f), i+2, m, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }else {
                int i = text.indexOf("steps");
                span.setSpan(new RelativeSizeSpan(2f), 0, i, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return span;
        }else if (flag == 1){
            Spannable span = new SpannableString(text);
            if (split!=null){
                if (split1!=null){
                    int i = text.indexOf(split);
                    int m = text.indexOf(split1);
                    span.setSpan(new RelativeSizeSpan(1.5f), i+2, m, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }else {
                    int m = text.indexOf(':');
                    int i = text.indexOf('h');
                    if (i>=0){
                        span.setSpan(new RelativeSizeSpan(2f), m, i, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    i = text.indexOf("min");
                    if (i>=0){
                        span.setSpan(new RelativeSizeSpan(2f), i-2, i, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }else {
                int i = text.indexOf('h');
                if (i>=0){
                    span.setSpan(new RelativeSizeSpan(2f), 0, i, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                i = text.indexOf("min");
                if (i>=0){
                    span.setSpan(new RelativeSizeSpan(2f), i-2, i, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            return span;
        }else if (flag == 2){
            Spannable span = new SpannableString(text);
            if (split!=null){
                if (split1!=null){
                    int i = text.indexOf(split);
                    int m = text.indexOf(split1);
                    span.setSpan(new RelativeSizeSpan(1.5f), i+2, m, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }else {
                    int i = text.indexOf(split);
                    span.setSpan(new RelativeSizeSpan(1.5f), i+2,span.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }else {
                int i = text.indexOf("bpm");
                span.setSpan(new RelativeSizeSpan(2f), 0, i, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return span;
        }else if (flag == 3){
            Spannable span = new SpannableString(text);
            if (split!=null){
                int i = text.indexOf(split);
                span.setSpan(new RelativeSizeSpan(1.5f), i+2,span.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }else {
                int i = text.indexOf("mmHg");
                span.setSpan(new RelativeSizeSpan(2f), 0, i, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return span;
        }else{
            Spannable span = new SpannableString(text);
            if (split!=null){
                int i = text.indexOf(split);
                span.setSpan(new RelativeSizeSpan(1.5f), i+2,span.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }else {
                span.setSpan(new RelativeSizeSpan(2f), 0, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return span;
        }
    }

    /**
     * Returns whether the mobile phone screen is locked.
     *
     * @param context
     * @return Return true, if screen is locked, otherwise, return false.
     */
    public static boolean isScreenLocked(Context context) {
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        Boolean isScreenLocked = km.inKeyguardRestrictedInputMode();

        return isScreenLocked;
    }

    /**
     * Returns whether the mobile phone screen is currently on.
     *
     * @param context
     * @return Return true, if screen is on, otherwise, return false.
     */
    public static boolean isScreenOn(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        Boolean isScreenOn = pm.isScreenOn();
        return isScreenOn;
    }

    /**
     * Returns whether the application is system application.
     *
     * @param appInfo
     * @return Return true, if the application is system application, otherwise,
     *         return false.
     */
    public static boolean isSystemApp(ApplicationInfo appInfo) {
        boolean isSystemApp = false;
        if (((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
                || ((appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0)) {
            isSystemApp = true;
        }

        // Log.i(LOG_TAG, "isSystemApp(), packageInfo.packageName=" +
        // appInfo.packageName
        // + ", isSystemApp=" + isSystemApp);
        return isSystemApp;
    }

    public static String getKeyFromValue(CharSequence charSequence) {
        Map<Object, Object> appList = AppList.getInstance().getAppList();
        Set<?> set = appList.entrySet();
        Iterator<?> it = set.iterator();
        String key = null;
        while (it.hasNext()) {
            @SuppressWarnings("rawtypes")
            Map.Entry entry = (Map.Entry) it.next();
            if (entry.getValue() != null && entry.getValue().equals(charSequence)) {
                key = entry.getKey().toString();
                break;
            }
        }
        return key;
    }

    /**
     * Array转换成Stirng
     * */
    static public String ArrayToString(ArrayList<String> repeatList){
        StringBuilder repeatString = new StringBuilder();
        if (repeatList.contains("1")){
            repeatString.append("1,");
        }
        if (repeatList.contains("2")){
            repeatString.append("2,");
        }
        if (repeatList.contains("3")){
            repeatString.append("3,");
        }
        if (repeatList.contains("4")){
            repeatString.append("4,");
        }
        if (repeatList.contains("5")){
            repeatString.append("5,");
        }
        if (repeatList.contains("6")){
            repeatString.append("6,");
        }
        if (repeatList.contains("7")){
            repeatString.append("7,");
        }
        if (repeatString.length()>0)
            return repeatString.substring(0,repeatString.length()-1);
        else
            return "";
    }

    private static ArrayList<Long> longs = new ArrayList<>();//异常数据所在的时间戳列表



    /**
     * 判断邮箱是否合法
     * */
    public static boolean isEmail(String strEmail) {
        String strPattern = "^[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";
        if (TextUtils.isEmpty(strPattern)) {
            return false;
        } else {
            return strEmail.matches(strPattern);
        }
    }

    public static void showToast(Context context,String msg){
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
    }

    /**
     * 公制转英制
     * */
    public static int [] metric2British(int height,int weight){
        int data[] = new int[2];
        data[0] = (int)(height * 0.3937008);
        data[1] = (int)(weight * 2.2046226);
        return data;
    }

    /**
     * dip 转换成px
     *
     * @param dip
     * @return
     */
    public static int dipToPx(float dip,Context context)
    {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5f * (dip >= 0 ? 1 : -1));
    }


    public static ArrayList<String> getStepPlanList(){
        ArrayList<String> list  = new ArrayList<>();
        for (int i = 1;i<=35;i++){
            list.add(String.format("%d",i*1000));
        }
        return list;
    }

    public static ArrayList<String> getSleepPlanList(){
        ArrayList<String> list  = new ArrayList<>();
        for (int i = 300;i<=900;i+=30){
            list.add(String.format("%.1f",(float)i/60));
        }
        return list;
    }

    /**
     * 统计日计步数据,日详情计步格式
     * str = hour1:step,hour2:step,....
     * 一天为24小时，hour代表的是第几个小时，step代表该小时里生成的总步数
     * */
    public static StepData mathStepDataForDay(ArrayList<String> steps){
        int hour[] = new int[24];
        String data[] = new String[0];
        for (int i = 0;i<steps.size();i++){
            data = steps.get(i).split("\\|");
            hour[Integer.valueOf(data[1].substring(0,data[1].indexOf(':')))] += Integer.valueOf(data[3]);
        }
        StringBuffer stepString = new StringBuffer();
        for (int i = 0;i<hour.length;i++){
            if (hour[i]!=0){
                stepString.append(String.format(",%02d:%d",i,hour[i]));
            }
        }
        String step = stepString.toString();
        Log.d("SZIP******","详情计步数据 = "+"time = "+DateUtil.getTimeScopeForDay(data[0],"yyyy-MM-dd")+"str = "+step.substring(1));
        return new StepData(DateUtil.getTimeScopeForDay(data[0],"yyyy-MM-dd"),0,0,
                0,step.equals("")?null:step.substring(1));
    }

    /**
     * 统计日睡眠数据,日详情睡眠格式
     * str = startTime,"time:model"...,sleepTime
     * startTime代表开始睡眠的时间，"time:model"代表状态组，time为该状态持续时间，model微睡眠状态，sleepTime为总睡眠时间
     * */
    public static SleepData mathSleepDataForDay(ArrayList<String> sleeps,String date){
        String data[];
        StringBuffer sleepString = new StringBuffer();
        for (int i = 0;i<sleeps.size()-1;i++){
            data = sleeps.get(i).split("\\|");
            if (i == 0){//第一条数据，代表睡眠起始时间
                sleepString.append(DateUtil.getMinue(data[1])+"");//初始化startTime
                sleepString.append(String.format(",%d:",DateUtil.getMinue(sleeps.get(i+1).split("\\|")[1])
                        -DateUtil.getMinue(data[1]))+data[2]);
            }else {
                sleepString.append(String.format(",%d:",DateUtil.getMinue(sleeps.get(i+1).split("\\|")[1])
                        -DateUtil.getMinue(data[1]))+data[2]);
            }
        }
        Log.d("SZIP******","详情睡眠数据 = "+"time = "+DateUtil.getTimeScopeForDay(date,"yyyy-MM-dd")+"str = "+sleepString.toString());
        return new SleepData(DateUtil.getTimeScopeForDay(date,"yyyy-MM-dd"),0,0,
                sleepString.toString().equals("")?null:sleepString.toString());
    }

    /**
     * 统计心率数据
     * */
    public static HeartData mathHeartDataForDay(ArrayList<String> hearts){
        int heart = 0;
        int sum = 0;
        StringBuffer heartStr = new StringBuffer();
        String data[];
        for (int i = 0;i<hearts.size();i++){
            data = hearts.get(i).split("\\|");
            if (Integer.valueOf(data[1])!=0){
                heart+=Integer.valueOf(data[1]);
                sum++;
                heartStr.append(","+data[1]);
            }
        }
        Log.d("SZIP******","心率数据 = "+"time = "+DateUtil.getTimeScopeForDay(hearts.get(0).split(" ")[0],"yyyy-MM-dd")
                +"heart = "+(sum==0?0:heart/sum)+" ;heartStr = "+heartStr.toString().substring(1));
        return new HeartData(DateUtil.getTimeScopeForDay(hearts.get(0).split(" ")[0],"yyyy-MM-dd"),sum==0?0:heart/sum,
                heartStr.toString().substring(1));
    }

    private static long stepLast,sleepLast,heartLast,bloodPressureLast,bloodOxygenLast,ecgLast,sportLast;
    /**
     * 把手表的数据换成json格式字符串用于上传到服务器
     * */
    public static String getStringWithJson(SharedPreferences sharedPreferences){

        stepLast = sharedPreferences.getLong("stepLast",0);
        sleepLast = sharedPreferences.getLong("sleepLast",0);
        heartLast = sharedPreferences.getLong("heartLast",0);
        bloodPressureLast = sharedPreferences.getLong("bloodPressureLast",0);
        bloodOxygenLast = sharedPreferences.getLong("bloodOxygenLast",0);
        ecgLast = sharedPreferences.getLong("ecgLast",0);
        sportLast = sharedPreferences.getLong("sportLast",0);

        List<StepData> stepDataList = SQLite.select()
                .from(StepData.class)
                .where(StepData_Table.time.greaterThanOrEq(stepLast))
                .queryList();
        if (stepDataList.size()!=0)
            stepLast = stepDataList.get(stepDataList.size()-1).time;

        List<SleepData> sleepDataList = SQLite.select()
                .from(SleepData.class)
                .where(SleepData_Table.time.greaterThanOrEq(sleepLast))
                .queryList();
        if (sleepDataList.size()!=0)
            sleepLast = sleepDataList.get(sleepDataList.size()-1).time;

        List<HeartData> heartDataList = SQLite.select()
                .from(HeartData.class)
                .where(HeartData_Table.time.greaterThanOrEq(heartLast))
                .queryList();
        if (heartDataList.size()!=0)
            heartLast = heartDataList.get(heartDataList.size()-1).time;

        List<BloodPressureData> bloodPressureDataList = SQLite.select()
                .from(BloodPressureData.class)
                .where(BloodPressureData_Table.time.greaterThan(bloodPressureLast))
                .queryList();
        if (bloodPressureDataList.size()!=0)
            bloodPressureLast = bloodPressureDataList.get(bloodPressureDataList.size()-1).time;

        List<BloodOxygenData> bloodOxygenDataList = SQLite.select()
                .from(BloodOxygenData.class)
                .where(BloodOxygenData_Table.time.greaterThan(bloodOxygenLast))
                .queryList();
        if (bloodOxygenDataList.size()!=0)
            bloodOxygenLast = bloodOxygenDataList.get(bloodOxygenDataList.size()-1).time;

        List<EcgData> ecgDataList = SQLite.select()
                .from(EcgData.class)
                .where(EcgData_Table.time.greaterThan(ecgLast))
                .queryList();
        if (ecgDataList.size()!=0)
            ecgLast = ecgDataList.get(ecgDataList.size()-1).time;

        List<SportData> sportDataList = SQLite.select()
                .from(SportData.class)
                .where(SportData_Table.time.greaterThan(sportLast))
                .queryList();
        if (sportDataList.size()!=0)
            sportLast = sportDataList.get(sportDataList.size()-1).time;


        JSONArray array = new JSONArray();
        JSONObject data = new JSONObject();

        /**
         * 遍历数据库里面的数据
         * */
        try {
            for (int i = 0;i<bloodOxygenDataList.size();i++){
                JSONObject object = new JSONObject();
                object.put("time",bloodOxygenDataList.get(i).time);
                object.put("bloodOxygenData",bloodOxygenDataList.get(i).bloodOxygenData);
                array.put(object);
            }
            data.put("bloodOxygenDataList",array);

            array = new JSONArray();
            for (int i = 0;i<bloodPressureDataList.size();i++){
                JSONObject object = new JSONObject();
                object.put("time",bloodPressureDataList.get(i).time);
                object.put("sbpDate",bloodPressureDataList.get(i).sbpDate);
                object.put("dbpDate",bloodPressureDataList.get(i).dbpDate);
                array.put(object);
            }
            data.put("bloodPressureDataList",array);

            array = new JSONArray();
            for (int i = 0;i<ecgDataList.size();i++){
                JSONObject object = new JSONObject();
                object.put("time",ecgDataList.get(i).time);
                object.put("heart",ecgDataList.get(i).heart);
                array.put(object);
            }
            data.put("ecgDataList",array);

            array = new JSONArray();
            for (int i = 0;i<heartDataList.size();i++){
                JSONObject object = new JSONObject();
                object.put("time",heartDataList.get(i).time);
                object.put("averageHeart",heartDataList.get(i).averageHeart);
                object.put("heartArray",heartDataList.get(i).heartArray);
                array.put(object);
            }
            data.put("heartDataList",array);

            array = new JSONArray();
            for (int i = 0;i<sleepDataList.size();i++){
                JSONObject object = new JSONObject();
                object.put("time",sleepDataList.get(i).time);
                object.put("deepTime",sleepDataList.get(i).deepTime);
                object.put("lightTime",sleepDataList.get(i).lightTime);
                object.put("dataForHour",sleepDataList.get(i).dataForHour);
                array.put(object);
            }
            data.put("sleepDataList",array);

            array = new JSONArray();
            for (int i = 0;i<sportDataList.size();i++){
                JSONObject object = new JSONObject();
                object.put("time",sportDataList.get(i).time);
                object.put("sportTime",sportDataList.get(i).sportTime);
                object.put("distance",sportDataList.get(i).distance);
                object.put("calorie",sportDataList.get(i).calorie);
                object.put("speed",sportDataList.get(i).speed);
                object.put("type",sportDataList.get(i).type);
                array.put(object);
            }
            data.put("sportDataList",array);

            array = new JSONArray();
            for (int i = 0;i<stepDataList.size();i++){
                JSONObject object = new JSONObject();
                object.put("time",stepDataList.get(i).time);
                object.put("steps",stepDataList.get(i).steps);
                object.put("distance",stepDataList.get(i).distance);
                object.put("calorie",stepDataList.get(i).calorie);
                object.put("dataForHour",stepDataList.get(i).dataForHour);
                array.put(object);
            }
            data.put("stepDataList",array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("TOKENSZIP******","array = "+data.toString());
        return data.toString();
    }

    public static void saveLastTime(SharedPreferences sharedPreferences){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (stepLast!=0)
            editor.putLong("stepLast",stepLast);
        if (sleepLast!=0)
            editor.putLong("sleepLast",sleepLast);
        if (heartLast!=0)
            editor.putLong("heartLast",heartLast);
        if (bloodPressureLast!=0)
            editor.putLong("bloodPressureLast",bloodPressureLast);
        if (bloodOxygenLast!=0)
            editor.putLong("bloodOxygenLast",bloodOxygenLast);
        if (ecgLast!=0)
            editor.putLong("ecgLast",ecgLast);
        if (sportLast!=0)
            editor.putLong("sportLast",sportLast);
        editor.commit();

    }

    public static SharedPreferences.Editor saveInfoData(Context context, UserInfo info){
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILE,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("birthday",info.getBirthday());
        editor.putString("userName",info.getUserName());
        editor.putString("height",info.getHeight());
        editor.putString("weight",info.getWeight());
        editor.putString("unit",info.getUnit());
        editor.putInt("sex",info.getSex());
        editor.putInt("stepsPlan",info.getStepsPlan());
        editor.putInt("sleepPlan",info.getSleepPlan());
        editor.putString("deviceCode",info.getDeviceCode());
        return editor;
    }

    public static UserInfo loadInfoData(SharedPreferences sharedPreferences){
        UserInfo info = new UserInfo();
        info.setBirthday(sharedPreferences.getString("birthday",""));
        info.setUserName(sharedPreferences.getString("userName","ipt"));
        info.setHeight(sharedPreferences.getString("height","0"));
        info.setWeight(sharedPreferences.getString("weight","0"));
        info.setUnit(sharedPreferences.getString("unit","metric"));
        info.setSex(sharedPreferences.getInt("sex",1));
        info.setStepsPlan(sharedPreferences.getInt("stepsPlan",6000));
        info.setSleepPlan(sharedPreferences.getInt("sleepPlan",480));
        info.setDeviceCode(sharedPreferences.getString("deviceCode",null));
        return info;
    }
}
