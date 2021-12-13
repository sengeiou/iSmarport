package com.szip.sportwatch;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mediatek.wearable.WearableManager;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.szip.sportwatch.BLE.EXCDController;
import com.szip.sportwatch.Broadcat.UtilBroadcat;
import com.szip.sportwatch.Activity.LoginActivity;
import com.szip.sportwatch.DB.LoadDataUtil;
import com.szip.sportwatch.DB.SaveDataUtil;
import com.szip.sportwatch.DB.dbModel.SportWatchAppFunctionConfigDTO;
import com.szip.sportwatch.Model.HttpBean.WeatherBean;
import com.szip.sportwatch.Model.UserInfo;
import com.szip.sportwatch.Notification.IgnoreList;
import com.szip.sportwatch.Notification.MyNotificationReceiver;
import com.szip.sportwatch.Notification.NotificationView;
import com.szip.sportwatch.Service.MainService;
import com.szip.sportwatch.Util.FileUtil;
import com.szip.sportwatch.Util.HttpMessgeUtil;
import com.szip.sportwatch.Util.LogUtil;
import com.szip.sportwatch.Util.MathUitl;
import com.szip.sportwatch.Util.MusicUtil;
import com.szip.sportwatch.Util.ProgressHudModel;
import com.szip.sportwatch.Util.TopExceptionHandler;
import com.tencent.bugly.crashreport.CrashReport;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/**
 * Created by Administrator on 2019/11/28.
 */

public class MyApplication extends Application{

    private SharedPreferences sharedPreferences;
    private int mFinalCount;
    static public String FILE = "sportWatch";

    private UserInfo userInfo;
    private String city;
    private String deviceNum;
    private ArrayList<WeatherBean.Condition> weatherModel;

    private static MyApplication mInstance;
    private boolean camerable;//能否使用照相机
    private boolean heartSwitch;//能否使用照相机

    private int updownTime;
    private Thread updownDataThread;//上传数据的线程

    public static MyApplication getInstance(){
        return mInstance;
    }


    private SportWatchAppFunctionConfigDTO sportWatchAppFunctionConfigDTO;
    private boolean isFirst = true;
    private String BtMac;

    private BluetoothAdapter btAdapt;

    private boolean isNewVersion = false;

    private String privatePath;

    public void setBtMac(final String btMac) {
        if (BtMac==null||!btMac.split(":")[0].equals(BtMac.split(":")[0])){
            String[] buff = btMac.split(":");
            BtMac = String.format("%02X:%02X:%02X:%02X:%02X:%02X",Integer.valueOf(buff[0],16),Integer.valueOf(buff[1],16),
                    Integer.valueOf(buff[2],16),Integer.valueOf(buff[3],16),Integer.valueOf(buff[4],16)
                    ,Integer.valueOf(buff[5],16));
            Log.d("DATA******","MAC = "+BtMac);
        }
        if (BtMac.equals("00:00:00:00:00:00"))
            return;
        if (btAdapt == null)
            btAdapt = BluetoothAdapter.getDefaultAdapter();
        try {
            if (BtMac!=null) {
                BluetoothDevice btDev = btAdapt.getRemoteDevice(BtMac);
                Boolean returnValue = false;
                if (btDev.getBondState() == BluetoothDevice.BOND_NONE) {
                    //利用反射方法调用BluetoothDevice.createBond(BluetoothDevice remoteDevice);
                    Method createBondMethod = BluetoothDevice.class
                            .getMethod("createBond");
                    Log.d("DATA******", "开始配对");
                    returnValue = (Boolean) createBondMethod.invoke(btDev);
                }
            }
        }catch (IllegalArgumentException e){
            Log.e("DATA******",e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("Heal","onCreate");

        mInstance = this;
        FlowManager.init(this);

        /**
         * 注册广播
         * */
        UtilBroadcat broadcat = new UtilBroadcat(getApplicationContext());
        broadcat.onRegister();

        /**
         * 把log上传到云端
         * */
        CrashReport.initCrashReport(getApplicationContext(), "937b55f499", true);

        /**
         * 把测试版的log导出到本地，上线的渠道版不需要导出
         * */
        if(BuildConfig.FLAVORS.equals(""))
            LogUtil.getInstance().init(this);

        /**
         * 初始化音乐控制器
         * */
        MusicUtil.getSingle().init(getApplicationContext());

        //初始化文件存储
        privatePath = getExternalFilesDir(null).getPath()+"/";
        FileUtil.getInstance().initFile(this);
        //注册网络回调
        HttpMessgeUtil.getInstance().init(this);
        //初始化通知栏
        NotificationView.getInstance().init(this);

        if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            NotificationManager manager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            //只在Android O之上需要渠道，这里的第一个参数要和下面的channelId一样
            NotificationChannel notificationChannel = new NotificationChannel("0103", "iSmarport", NotificationManager.IMPORTANCE_HIGH);
            //如果这里用IMPORTANCE_NOENE就需要在系统的设置里面开启渠道，通知才能正常弹出
            notificationChannel.setShowBadge(false);
            manager.createNotificationChannel(notificationChannel);
        }
        /**
         * 拿去本地缓存的数据
         * */
        if (sharedPreferences == null)
            sharedPreferences = getSharedPreferences(FILE,MODE_PRIVATE);
        sportWatchAppFunctionConfigDTO = LoadDataUtil.newInstance().getDeviceConfig(sharedPreferences.getString("deviceName",null));
        //获取上次退出之后剩余的倒计时上传时间
        updownTime = sharedPreferences.getInt("updownTime",3600);
        //获取手机缓存的远程拍照状态
        camerable = sharedPreferences.getBoolean("camera",false);
        heartSwitch = sharedPreferences.getBoolean("heartSwitch",false);
        //获取手机缓存的自动更新信息
        isNewVersion = sharedPreferences.getBoolean("version",false);
        if (sharedPreferences.getBoolean("first",true)){
            initIgnoreList();
            sharedPreferences.edit().putBoolean("first",false).commit();
        }



        registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                mFinalCount++;
                //如果mFinalCount ==1，说明是从后台到前台
                Log.e("onActivityStarted", mFinalCount + "");
                if (mFinalCount == 1) {
                    //说明从后台回到了前台
                    Log.i("DATA******", " 返回到了 前台");
                    if(isFirst){
                        isFirst = false;
                    }else {
                        if (isMtk()&&WearableManager.getInstance().getConnectState()==WearableManager.STATE_CONNECTED){
                            EXCDController.getInstance().writeForEnableSend(1);
                        }
                    }
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                mFinalCount--;
                //如果mFinalCount ==0，说明是前台到后台

                Log.i("onActivityStopped", mFinalCount + "");
                if (mFinalCount == 0) {
                    //说明从前台回到了后台
                    Log.i("DATA******", " 切换到了 后台");
                    if (isMtk()&&WearableManager.getInstance().getConnectState()==WearableManager.STATE_CONNECTED){
                        EXCDController.getInstance().writeForEnableSend(0);
                    }
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

        String packageName = getPackageName();
        String strListener = Settings.Secure.getString(this.getContentResolver(),
                "enabled_notification_listeners");
        if (strListener != null
                && strListener
                .contains(packageName)) {
            ComponentName localComponentName = new ComponentName(this, MyNotificationReceiver.class);
            PackageManager localPackageManager = this.getPackageManager();
            localPackageManager.setComponentEnabledSetting(localComponentName, 2, 1);
            localPackageManager.setComponentEnabledSetting(localComponentName, 1, 1);
        }

       startUpdownThread();
    }

    public String getPrivatePath() {
        return privatePath;
    }

    /**
     * 倒计时累计一个小时就上传一次数据到云端
     * */
    private void startUpdownThread(){

        updownDataThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(1000);
                        updownTime--;
                        if (updownTime == 0)
                            break;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (userInfo!=null&&userInfo.getDeviceCode()!=null){
                    try {
                        String datas = MathUitl.getStringWithJson(getSharedPreferences(FILE,MODE_PRIVATE));
                        HttpMessgeUtil.getInstance().postForUpdownReportData(datas);
                        updownTime = 3600;
                        startUpdownThread();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        updownDataThread.start();
    }

    private void initIgnoreList() {
        HashSet<String> exclusionList = IgnoreList.getInstance().getExclusionList();
        List<PackageInfo> packagelist = getPackageManager().getInstalledPackages(0);

        for (PackageInfo packageInfo : packagelist) {
            if (packageInfo != null) {
                // Whether this package should be exclude;
                if (exclusionList.contains(packageInfo.packageName)) {
                    continue;
                }
                // Add app name
                String appName = packageInfo.packageName;
                // Add to package list
                if (MathUitl.isSystemApp(packageInfo.applicationInfo)) {
                    IgnoreList.getInstance().addIgnoreItem(appName);
                }else {
                    if (!(appName.equals("com.tencent.mm")||appName.equals("com.tencent.mobileqq")))
                        IgnoreList.getInstance().addIgnoreItem(appName);
                }
            }
        }
        IgnoreList.getInstance().saveIgnoreList();
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public boolean isCamerable() {
        return camerable;
    }

    public void setCamerable(boolean camerable) {
        this.camerable = camerable;
        if (sharedPreferences == null)
            sharedPreferences = getSharedPreferences(FILE,MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("camera",camerable).commit();
    }

    public boolean isHeartSwitch() {
        return heartSwitch;
    }

    public void setHeartSwitch(boolean heartSwitch) {
        this.heartSwitch = heartSwitch;
        if (sharedPreferences == null)
            sharedPreferences = getSharedPreferences(FILE,MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("heartSwitch",heartSwitch).commit();
    }

    public int getUpdownTime() {
        return updownTime;
    }

    public ArrayList<WeatherBean.Condition> getWeatherModel() {
        if (weatherModel==null){
            String weather = sharedPreferences.getString("weatherList",null);
            if (weather == null)
                return null;
            else {
                Gson gson = new Gson();
                ArrayList<WeatherBean.Condition> bean = gson.fromJson(weather, new TypeToken<ArrayList<WeatherBean.Condition>>(){}.getType());
                return bean;
            }
        }else
            return weatherModel;
    }

    public String getCity() {
        return sharedPreferences.getString("city",null);
    }

    public float getElevation(){
        return sharedPreferences.getFloat("elevation",1f);
    }

    public void setWeatherModel(WeatherBean weatherBean) {
        try {
            Gson gson=new Gson();
            this.weatherModel = weatherBean.getData().getForecasts();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            JSONArray array = null;
            array = new JSONArray(gson.toJson(weatherModel));
            editor.putString("weatherList",array.toString());
            editor.putString("city",weatherBean.getData().getLocation().getCity());
            editor.putFloat("elevation",weatherBean.getData().getLocation().getElevation());
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setDeviceNum(String deviceNum) {
        Log.i("DATA******","devicenum = "+deviceNum);
        this.deviceNum = deviceNum;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("deviceNum",deviceNum);
        editor.commit();
    }

    public String getDeviceNum() {
        if (deviceNum==null){
            deviceNum = sharedPreferences.getString("deviceNum",null);
            if (deviceNum==null)
                return "0";
            else
                return deviceNum;
        }else
            return deviceNum;
    }

    public void setDeviceConfig(String deviceName) {
        if(deviceName!=null){
            deviceName = deviceName.indexOf("_LE")>=0?deviceName.substring(0,deviceName.length()-3): deviceName;
            Log.d("DATA******", "deviceName = " + deviceName);
            sharedPreferences.edit().putString("deviceName",deviceName).commit();
            sportWatchAppFunctionConfigDTO = LoadDataUtil.newInstance().getDeviceConfig(deviceName);
        }
    }

    public boolean isMtk() {
        return  sportWatchAppFunctionConfigDTO==null?false:sportWatchAppFunctionConfigDTO.getUseMtkConnect()==1;
    }

    public boolean isCircle() {
        return sportWatchAppFunctionConfigDTO==null?false:sportWatchAppFunctionConfigDTO.getScreenType()==0;
    }

    public String getDialGroupId() {
        return sportWatchAppFunctionConfigDTO==null?"0":Integer.toString(sportWatchAppFunctionConfigDTO.getWatchPlateGroupId());
    }

    public String getFaceType() {
        if (sportWatchAppFunctionConfigDTO==null)
            return "320*385";
        else {
            String type = sportWatchAppFunctionConfigDTO.getScreen();
            if (type!=null&&type.indexOf("*")>=0){
                int index = type.indexOf("*");
                type = type.substring(index-3,index+4);
                return type;
            }else {
                return "320*385";
            }
        }
    }

    public boolean checkFaceType(int width,int height){
        String[] faceStr = getFaceType().split("\\*");
        int faceWidth = Integer.valueOf(faceStr[0]);
        int faceHeight = Integer.valueOf(faceStr[1]);
        if (width!=faceWidth||height!=faceHeight)
            return false;
        return true;
    }

    public boolean isNewVersion() {
        return isNewVersion;
    }

    public void setNewVersion(boolean newVersion) {
        isNewVersion = newVersion;
        if (sharedPreferences!=null)
            sharedPreferences.edit().putBoolean("version",newVersion).commit();
    }

    public void setDialUrl(String url){
        sharedPreferences.edit().putString("dialUrl",url).commit();
    }

    public String getDiadUrl(){
        return sharedPreferences.getString("dialUrl","");
    }

    public boolean getSportVisiable(){
        if (deviceNum==null){
            deviceNum = sharedPreferences.getString("deviceNum",null);
            if (deviceNum==null)
                return true;
        }

        return LoadDataUtil.newInstance().getSportConfig(Integer.valueOf(deviceNum));
    }



    public void tokenTimeOut(){
        SharedPreferences sharedPreferences ;
        ProgressHudModel.newInstance().diss();

        sharedPreferences = getSharedPreferences(FILE,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token",null);
        editor.commit();
        SaveDataUtil.newInstance().clearDB();
        if (MainService.getInstance()!=null)
            MainService.getInstance().stopConnect();
        MathUitl.showToast(this,getString(R.string.tokenTimeOut));
        Intent intentmain=new Intent(this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentmain);
    }
}
