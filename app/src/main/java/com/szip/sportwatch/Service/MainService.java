package com.szip.sportwatch.Service;

import android.Manifest;
import android.app.DownloadManager;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.mediatek.ctrl.map.MapController;
import com.mediatek.wearable.WearableListener;
import com.mediatek.wearable.WearableManager;
import com.szip.sportwatch.BLE.BleClient;
import com.szip.sportwatch.BLE.NotificationController;
import com.szip.sportwatch.DB.LoadDataUtil;
import com.szip.sportwatch.DB.SaveDataUtil;
import com.szip.sportwatch.DB.dbModel.AnimalHeatData;
import com.szip.sportwatch.DB.dbModel.BloodOxygenData;
import com.szip.sportwatch.DB.dbModel.BloodPressureData;
import com.szip.sportwatch.DB.dbModel.EcgData;
import com.szip.sportwatch.DB.dbModel.HeartData;
import com.szip.sportwatch.DB.dbModel.SleepData;
import com.szip.sportwatch.DB.dbModel.SportData;
import com.szip.sportwatch.DB.dbModel.StepData;
import com.szip.sportwatch.Interface.IOtaResponse;
import com.szip.sportwatch.Interface.ReceiveDataCallback;
import com.szip.sportwatch.Model.EvenBusModel.ConnectState;
import com.szip.sportwatch.Model.SendDialModel;
import com.szip.sportwatch.Model.UpdateSportView;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.Notification.AppList;
import com.szip.sportwatch.Notification.NotificationView;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.DateUtil;
import com.szip.sportwatch.Util.FileUtil;
import com.szip.sportwatch.Util.LogUtil;
import com.szip.sportwatch.Util.MathUitl;
import com.szip.sportwatch.BLE.EXCDController;
import com.szip.sportwatch.Notification.NotificationReceiver;
import com.szip.sportwatch.Notification.NotificationService;
import com.szip.sportwatch.Notification.SmsService;
import com.szip.sportwatch.Util.MusicUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.media.AudioManager.FLAG_PLAY_SOUND;
import static android.media.AudioManager.STREAM_MUSIC;

/**
 * Created by Administrator on 2019/12/27.
 */

public class MainService extends Service {

    // Debugging
    private static final String TAG = "AppManager/MainService";

    // Global instance
    private static MainService mSevice = null;

    // Application context
    private static final Context sContext = MyApplication.getInstance()
            .getApplicationContext();

    // Flag to indicate whether main service has been start
    private static boolean mIsMainServiceActive = false;

    private boolean mIsSmsServiceActive = false;


    // Register and unregister SMS service dynamically
    private SmsService mSmsService = null;

    private NotificationService mNotificationService = null;

    private Thread connectThread;//????????????
    private boolean isThreadRun = true;
    private int reconnectTimes = 0;

    private MediaPlayer mediaPlayer;
    private int volume = 0;

    private MyApplication app;





    public int getState() {
        return app.isMtk()?WearableManager.getInstance().getConnectState(): BleClient.getInstance().getConnectState();
    }


    private WearableListener mWearableListener = new WearableListener() {

        @Override
        public void onConnectChange(int oldState, int newState) {
            if (app.isMtk()){
                LogUtil.getInstance().logd("DATA******","STATE = "+newState);
                EventBus.getDefault().post(new ConnectState(newState));
                if (newState == WearableManager.STATE_CONNECTED){//???????????????????????????????????????
                    mSevice.startForeground(0103,NotificationView.getInstance().getNotify(true));
                    startThread();//????????????
                    reconnectTimes = 0;
                    String str = getResources().getConfiguration().locale.getLanguage();
                    LogUtil.getInstance().logd("DATA******","lau = "+str+" loc = "+getResources().getConfiguration().locale.getCountry());
                    if (str.equals("en"))
                        EXCDController.getInstance().writeForSetLanuage("en_US");
                    else if (str.equals("de"))
                        EXCDController.getInstance().writeForSetLanuage("de_DE");
                    else if (str.equals("fr"))
                        EXCDController.getInstance().writeForSetLanuage("fr_FR");
                    else if (str.equals("it"))
                        EXCDController.getInstance().writeForSetLanuage("it_IT");
                    else if (str.equals("es"))
                        EXCDController.getInstance().writeForSetLanuage("es_ES");
                    else if (str.equals("pt"))
                        EXCDController.getInstance().writeForSetLanuage("pt_PT");
                    else if (str.equals("tr"))
                        EXCDController.getInstance().writeForSetLanuage("tr_TR");
                    else if (str.equals("ru"))
                        EXCDController.getInstance().writeForSetLanuage("ru_RU");
                    else if (str.equals("ar"))
                        EXCDController.getInstance().writeForSetLanuage("ar_SA");
                    else if (str.equals("th"))
                        EXCDController.getInstance().writeForSetLanuage("th_TH");
                    else if (str.equals("zh")){
                        if (MyApplication.getInstance().getResources().getConfiguration().locale.getCountry().equals("CN"))
                            EXCDController.getInstance().writeForSetLanuage("zh_CN");
                        else
                            EXCDController.getInstance().writeForSetLanuage("zh_TW");
                    } else if (str.equals("ja"))
                        EXCDController.getInstance().writeForSetLanuage("ja_jp");
                    else if (str.equals("iw"))
                        EXCDController.getInstance().writeForSetLanuage("he_IL");
                    EXCDController.getInstance().writeForEnableSend(1);
                    EXCDController.getInstance().writeForSetDate();
                    EXCDController.getInstance().writeForSetInfo(app.getUserInfo());
                    EXCDController.getInstance().writeForSetUnit(app.getUserInfo());
                    EXCDController.getInstance().writeForCheckVersion();
                    EXCDController.getInstance().writeForUpdateWeather(app.getWeatherModel(),
                            app.getCity());
                    MusicUtil.getSingle().registerNotify();
                }else if (newState == WearableManager.STATE_CONNECT_LOST){
                    MusicUtil.getSingle().unRegisterNotify();
                    mSevice.startForeground(0103,NotificationView.getInstance().getNotify(false));
                }
            }
        }

        @Override
        public void onDeviceChange(BluetoothDevice device) {
            return;
        }

        @Override
        public void onDeviceScan(BluetoothDevice device) {
            if (getState()!=3&&getState()!=2){
                if (device.getAddress().equals(app.getUserInfo().getDeviceCode())){
                    LogUtil.getInstance().logd("DATA******","????????????="+device.getAddress());
                    WearableManager.getInstance().scanDevice(false);
                    WearableManager.getInstance().setRemoteDevice(device);
                    startConnect();
                }
            }
        }


        @Override
        public void onModeSwitch(int newMode) {
            LogUtil.getInstance().logd(TAG, "onModeSwitch newMode = " + newMode);
        }
    };





    /**
     * ??????????????????????????????
     * */
    private ReceiveDataCallback receiveDataCallback = new ReceiveDataCallback() {
        @Override
        public void checkVersion(boolean stepNum, boolean deltaStepNum, boolean sleepNum, boolean deltaSleepNum,
                                 boolean heart, boolean bloodPressure, boolean bloodOxygen,boolean ecg,boolean animalHeat,String deviceNum) {
            LogUtil.getInstance().logd("DATA******","???????????????step = "+stepNum+" ;stepD = "+deltaStepNum+" ;sleep = "+sleepNum+
                    " ;sleepD = "+deltaSleepNum+" ;heart = "+heart+ " ;bloodPressure = "+bloodPressure+
                    " ;bloodOxygen = "+heart+" ;ecg = "+ecg+" ;animalHeat = "+animalHeat);
            if (stepNum)
                EXCDController.getInstance().writeForGetDaySteps();
            if (deltaStepNum)
                EXCDController.getInstance().writeForGetSteps();
            if (sleepNum)
                EXCDController.getInstance().writeForGetDaySleep();
            if (deltaSleepNum)
                EXCDController.getInstance().writeForGetSleep();
            if (heart)
                EXCDController.getInstance().writeForGetHeart();
            if (bloodPressure)
                EXCDController.getInstance().writeForGetBloodPressure();
            if (bloodOxygen)
                EXCDController.getInstance().writeForGetBloodOxygen();
            if (ecg)
                EXCDController.getInstance().writeForGetEcg();
            if (animalHeat)
                EXCDController.getInstance().writeForGetAnimalHeat();

            if(app.getDeviceNum()!=deviceNum){
                app.setDeviceNum(deviceNum);
                EventBus.getDefault().post(new UpdateSportView());
            }

        }

        @Override
        public void getStepsForDay(String[] stepsForday) {
            LogUtil.getInstance().logd("DATA******","???????????????????????? = "+stepsForday.length);
            ArrayList<StepData> dataArrayList = new ArrayList<>();
            for (int i =0;i<stepsForday.length;i++){
                String datas[] = stepsForday[i].split("\\|");
                long time = DateUtil.getTimeScopeForDay(datas[0],"yyyy-MM-dd");
                int steps = Integer.valueOf(datas[1]);
                int distance = Integer.valueOf(datas[2]);
                int calorie = Integer.valueOf(datas[3])*100;
                LogUtil.getInstance().logd("DATA******","???????????? = "+"time = "+time+" ;steps = "+steps+" ;distance = "+distance+" ;calorie = "+calorie);
                dataArrayList.add(new StepData(time,steps,distance,calorie,null));
            }
            SaveDataUtil.newInstance().saveStepDataListData(dataArrayList);
        }

        @Override
        public void getSteps(String[] steps) {
            Log.d("DATA******","???????????????????????? = "+steps.length);
            ArrayList<StepData> dataArrayList = new ArrayList<>();
            ArrayList<String> list = null;//???????????????????????????????????????
            String date = null;//???????????????????????????????????????
            for (int i =0;i<steps.length;i++){//???????????????????????????????????????????????????
                if (list == null){//????????????????????????????????????????????????????????????????????????????????????
                    list = new ArrayList<>();
                    list.add(steps[i]);
                    date = steps[i].split("\\|")[0];//?????????????????????????????????
                }else {
                    if (steps[i].split("\\|")[0].equals(date)){//?????????????????????????????????????????????
                        list.add(steps[i]);
                    }else {//??????????????????????????????????????????????????????????????????list??????null,date????????????????????????
                        dataArrayList.add(MathUitl.mathStepDataForDay(list));
                        list = new ArrayList<>();
                        list.add(steps[i]);
                        date = steps[i].split("\\|")[0];
                    }
                }
            }
            if (list!=null)
                dataArrayList.add(MathUitl.mathStepDataForDay(list));
            SaveDataUtil.newInstance().saveStepInfoDataListData(dataArrayList);
        }

        @Override
        public void getSleepForDay(String[] sleepForday) {
            Log.d("DATA******","???????????????????????? = "+sleepForday.length);
            ArrayList<SleepData> dataArrayList = new ArrayList<>();
            for (int i =0;i<sleepForday.length;i++){
                String datas[] = sleepForday[i].split("\\|");
                long time = DateUtil.getTimeScopeForDay(datas[0],"yyyy-MM-dd")+24*60*60;
                int deepTime = DateUtil.getMinue(datas[1]);
                int lightTime = DateUtil.getMinue(datas[2]);
                Log.d("DATA******","???????????? = "+"time = "+time+" ;deep = "+deepTime+" ;light = "+lightTime);
                dataArrayList.add(new SleepData(time,deepTime,lightTime,null));
            }
            SaveDataUtil.newInstance().saveSleepDataListData(dataArrayList);
        }

        @Override
        public void getSleep(String[] sleep) {
            Log.d("DATA******","?????????????????????????????? = "+sleep.length);
            ArrayList<SleepData> dataArrayList = new ArrayList<>();
            ArrayList<String> list = null;//???????????????????????????????????????
            String date = null;//???????????????????????????????????????
            String sleepDate = null;
            for (int i =0;i<sleep.length;i++){//???????????????????????????????????????????????????
                sleepDate = DateUtil.getSleepDate(sleep[i]);//???????????????????????????????????????????????????
                if (list == null){//????????????????????????????????????????????????????????????????????????????????????
                    list = new ArrayList<>();
                    list.add(sleep[i]);
                    date = sleepDate;//?????????????????????????????????
                }else {
                    if (sleepDate.equals(date)){//?????????????????????????????????????????????
                        list.add(sleep[i]);
                    }else {//??????????????????????????????????????????????????????????????????list??????null,date????????????????????????
                        dataArrayList.add(MathUitl.mathSleepDataForDay(list,date));
                        list = new ArrayList<>();
                        list.add(sleep[i]);
                        date = sleepDate;
                    }
                }
            }
            if (list!=null)
                dataArrayList.add(MathUitl.mathSleepDataForDay(list,date));
            SaveDataUtil.newInstance().saveSleepInfoDataListData(dataArrayList);
        }

        @Override
        public void getHeart(String[] heart) {
            Log.d("DATA******","???????????????????????? = "+heart.length);
            ArrayList<HeartData> dataArrayList = new ArrayList<>();
            ArrayList<String> list = null;//???????????????????????????????????????
            String date = null;//???????????????????????????????????????
            for (int i =0;i<heart.length;i++){//???????????????????????????????????????????????????
                if (list == null){//????????????????????????????????????????????????????????????????????????????????????
                    list = new ArrayList<>();
                    list.add(heart[i]);
                    date = heart[i].split(" ")[0];//?????????????????????????????????
                }else {
                    if (heart[i].split(" ")[0].equals(date)){//?????????????????????????????????????????????
                        list.add(heart[i]);
                    }else {//??????????????????????????????????????????????????????????????????list??????null,date????????????????????????
                        dataArrayList.add(MathUitl.mathHeartDataForDay(list));
                        list = new ArrayList<>();
                        list.add(heart[i]);
                        date = heart[i].split(" ")[0];
                    }
                }
            }
            if (list!=null)
                dataArrayList.add(MathUitl.mathHeartDataForDay(list));
            SaveDataUtil.newInstance().saveHeartDataListData(dataArrayList,true);
        }

        @Override
        public void getBloodPressure(String[] bloodPressure) {
            Log.d("DATA******","???????????????????????? = "+bloodPressure.length);
            ArrayList<BloodPressureData> dataArrayList = new ArrayList<>();
            for (int i =0;i<bloodPressure.length;i++){
                String datas[] = bloodPressure[i].split("\\|");
                long time = DateUtil.getTimeScope(datas[0],"yyyy-MM-dd HH:mm:ss");
                int sbp = Integer.valueOf(datas[1]);
                int dbp = Integer.valueOf(datas[2]);
                Log.d("DATA******","???????????? = "+"time = "+time+" ;sbp = "+sbp+" ;dbp = "+dbp);
                dataArrayList.add(new BloodPressureData(time,sbp,dbp));
            }
            SaveDataUtil.newInstance().saveBloodPressureDataListData(dataArrayList);
        }

        @Override
        public void getBloodOxygen(String[] bloodOxygen) {
            Log.d("DATA******","???????????????????????? = "+bloodOxygen.length);
            ArrayList<BloodOxygenData> dataArrayList = new ArrayList<>();
            for (int i =0;i<bloodOxygen.length;i++){
                String datas[] = bloodOxygen[i].split("\\|");
                long time = DateUtil.getTimeScope(datas[0],"yyyy-MM-dd HH:mm:ss");
                int data = Integer.valueOf(datas[1]);
                Log.d("DATA******","???????????? = "+"time = "+time+" ;oxygen = "+data);
                dataArrayList.add(new BloodOxygenData(time,data));
            }
            SaveDataUtil.newInstance().saveBloodOxygenDataListData(dataArrayList);
        }

        @Override
        public void getAnimalHeat(String[] animalHeat) {
            Log.d("DATA******","???????????????????????? = "+animalHeat.length);
            ArrayList<AnimalHeatData> dataArrayList = new ArrayList<>();
            for (int i =0;i<animalHeat.length;i++){
                String datas[] = animalHeat[i].split("\\|");
                long time = DateUtil.getTimeScope(datas[0],"yyyy-MM-dd HH:mm:ss");
                int data = Integer.valueOf(datas[1])*10+Integer.valueOf(datas[2]);
                Log.d("DATA******","???????????? = "+"time = "+time+" ;animalHeat = "+data);
                dataArrayList.add(new AnimalHeatData(time,data));
            }
            SaveDataUtil.newInstance().saveAnimalHeatDataListData(dataArrayList);
        }

        @Override
        public void getEcg(String[] ecg) {
            Log.d("DATA******","??????ecg???????????? = "+ecg.length);
            ArrayList<EcgData> dataArrayList = new ArrayList<>();
            for (int i =0;i<ecg.length;i++){
                String datas[] = ecg[i].split("\\|");
                long time = DateUtil.getTimeScope(datas[0]+" "+datas[1],"yyyy-MM-dd HH:mm:ss");
                Log.d("DATA******","ecg?????? = "+"time = "+time+" ;heart = "+datas[2]);
                dataArrayList.add(new EcgData(time,datas[2]));
            }
            SaveDataUtil.newInstance().saveEcgDataListData(dataArrayList);
        }

        @Override
        public void getSport(String[] sport) {
            int type = Integer.valueOf(sport[0]);
            long time = DateUtil.getTimeScope(sport[1],"yyyy|MM|dd|HH|mm|ss");
            int sportTime = Integer.valueOf(sport[2]);
            int distance = Integer.valueOf(sport[3]);
            int calorie = Integer.valueOf(sport[4])*1000;
            int speed = Integer.valueOf(sport[5]);
            int heart = Integer.valueOf(sport[10]);
            int stride = Integer.valueOf(sport[7]);
            SportData sportData = new SportData(time,sportTime,distance,calorie,speed,type,heart,stride);
            if (stride!=0){
                sportData.step = (int)((sportTime/60f)*stride);
            }
            SaveDataUtil.newInstance().saveSportData(sportData);
        }


        @Override
        public void findPhone(int flag) {
            final AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            if (flag == 1){
                starVibrate(new long[]{500,500,500});
                volume  = am.getStreamVolume(STREAM_MUSIC);//???????????????????????????
                am.setStreamVolume (STREAM_MUSIC, am.getStreamMaxVolume(STREAM_MUSIC), FLAG_PLAY_SOUND);//????????????????????????
                if (mediaPlayer==null){
                    mediaPlayer = MediaPlayer.create(MainService.this, R.raw.dang_ring);
                    mediaPlayer.start();
                    mediaPlayer.setVolume(1f,1f);
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            stopVibrate();
                            am.setStreamVolume (STREAM_MUSIC, volume, FLAG_PLAY_SOUND);//???????????????????????????????????????
                            mediaPlayer = null;
                        }
                    });
                }
            }else{
                if (mediaPlayer!=null){
                    mediaPlayer.stop();
                    stopVibrate();
                    am.setStreamVolume (STREAM_MUSIC, volume, FLAG_PLAY_SOUND);//???????????????????????????????????????
                    mediaPlayer = null;
                }
            }
        }
    };


    public MainService() {
        Log.i(TAG, "MainService(), MainService in construction!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate()");
        // updateConnectionStatus(false);

        super.onCreate();
        Log.d("DATA******","service start");
        mSevice = this;
        app = MyApplication.getInstance();
        mIsMainServiceActive = true;

        Map<Object, Object> applist = AppList.getInstance().getAppList();
        if (applist.size() == 0) {
            applist.put(AppList.MAX_APP, (int) AppList.CREATE_LENTH);
            applist.put(AppList.CREATE_LENTH, AppList.BATTERYLOW_APPID);
            applist.put(AppList.CREATE_LENTH, AppList.SMSRESULT_APPID);
            AppList.getInstance().saveAppList(applist);
        }
        if (!applist.containsValue(AppList.BATTERYLOW_APPID)) {
            int max = Integer.parseInt(applist.get(AppList.MAX_APP).toString());
            applist.remove(AppList.MAX_APP);
            max = max + 1;
            applist.put(AppList.MAX_APP, max);
            applist.put(max, AppList.BATTERYLOW_APPID);
            AppList.getInstance().saveAppList(applist);
        }
        if (!applist.containsValue(AppList.SMSRESULT_APPID)) {
            int max = Integer.parseInt(applist.get(AppList.MAX_APP).toString());
            applist.remove(AppList.MAX_APP);
            max = max + 1;
            applist.put(AppList.MAX_APP, max);
            applist.put(max, AppList.SMSRESULT_APPID);
            AppList.getInstance().saveAppList(applist);
        }


        registerService();
    }

    public void startConnect(){
        if (!app.isMtk()) {//??????????????????MTK????????????????????????
            if ((getState()==WearableManager.STATE_CONNECT_FAIL||
                    getState()==WearableManager.STATE_CONNECT_LOST ||
                    getState()==WearableManager.STATE_NONE) ){//???????????????????????????????????????
                //??????????????????????????????
                Log.d("DATA******","????????????BLE");
                BleClient.getInstance().connect(app.getUserInfo().getDeviceCode());
            }
        }else {
            if ((getState()==WearableManager.STATE_CONNECT_FAIL||
                    getState()==WearableManager.STATE_CONNECT_LOST ||
                    getState()==WearableManager.STATE_NONE||
                    getState()==WearableManager.STATE_LISTEN) ){//???????????????????????????????????????
                //??????????????????????????????
                Log.d("DATA******","????????????MTK");
                WearableManager.getInstance().connect();
            }
        }
    }

    public void stopConnect(){
        if (app.isMtk()){
            WearableManager.getInstance().disconnect();
            WearableManager.getInstance().setRemoteDevice(null);
        }else {
            BleClient.getInstance().disConnect();
        }
        isThreadRun = false;
    }


    public void startThread(){
        isThreadRun = true;
        if(connectThread == null||!connectThread.isAlive()){//??????????????????
            connectThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isThreadRun){
                        if (getState()==WearableManager.STATE_CONNECT_FAIL||
                                getState()==WearableManager.STATE_CONNECT_LOST ||
                                getState()==WearableManager.STATE_NONE){
                            Log.d("DATA******","????????????");
                            if (getState()==0||getState()==5){
                                if (reconnectTimes<3){
                                    reconnectTimes++;
                                    startConnect();
                                }else {
                                    isThreadRun = false;
                                    reconnectTimes = 0;
                                }
                            }
                        }
                        try {
                            Thread.sleep(10*1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            connectThread.start();
        }
    }


    public void setReconnectTimes(int reconnectTimes) {
        this.reconnectTimes = reconnectTimes;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        WearableManager manager = WearableManager.getInstance();
        manager.removeController(MapController.getInstance(sContext));
        manager.removeController(NotificationController.getInstance());
        manager.removeController(EXCDController.getInstance());
        EXCDController.getInstance().setReceiveDataCallback(null);
        manager.unregisterWearableListener(mWearableListener);
        mIsMainServiceActive = false;
        stopNotificationService();
        mSevice = null;
        BleClient.getInstance().setiOtaResponse(null);
        stopConnect();
//        Intent intent = new Intent();
//        intent.setClass(this,MainService.class);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(intent);
//        }else {
//            startService(intent);
//        }
    }


    public void startNotificationService() {
        Log.i(TAG, "startNotificationService()");
        mNotificationService = new NotificationService();
//        NotificationController.setListener(mNotificationService);

    }

    public void stopNotificationService() {
        Log.i(TAG, "stopNotificationService()");
//        NotificationController.setListener(null);
        mNotificationService = null;
    }

    private void starVibrate(long[] pattern) {
        Vibrator vib = (Vibrator) mSevice.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(pattern, 1);
    }


    private void stopVibrate() {
        Vibrator vib = (Vibrator) mSevice.getSystemService(Service.VIBRATOR_SERVICE);
        vib.cancel();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.getInstance().logd("data******","service bind");
        return new Binder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtil.getInstance().logd("data******","service onUnbind");
        return super.onUnbind(intent);
    }

    /**
     * Return the instance of main service.
     *
     * @return main service instance
     */
    public static MainService getInstance() {
        return mSevice;
    }

    /**
     * Return whether main service is started.
     *
     * @return Return true, if main service start, otherwise, return false.
     */
    public static boolean isMainServiceActive() {
        return mIsMainServiceActive;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void registerService() {
        // regist battery low

        Log.i(TAG, "registerService()");

        WearableManager manager = WearableManager.getInstance();
        manager.addController(NotificationController.getInstance());
        manager.addController(EXCDController.getInstance());
        EXCDController.getInstance().setReceiveDataCallback(receiveDataCallback);
        manager.registerWearableListener(mWearableListener);
        // start SMS service
        if (LoadDataUtil.newInstance().needNotify("message"))
            startSmsService();
        startNotificationService();
        BleClient.getInstance().setiOtaResponse(iOtaResponse);
    }

    public boolean getSmsServiceStatus() {
        return mIsSmsServiceActive;
    }

    /**
     * Start SMS service to push new SMS.
     */
    public void startSmsService() {
        Log.i(TAG, "startSmsService()");
        // Start SMS service
        if (mSmsService == null) {
            mSmsService = new SmsService();
        }
        IntentFilter filter = new IntentFilter("com.mtk.btnotification.SMS_RECEIVED");
        registerReceiver(mSmsService, filter);

        mIsSmsServiceActive = true;
    }

    /**
     * Stop SMS service.
     */
    public void stopSmsService() {
        Log.i(TAG, "stopSmsService()");

        // Stop SMS service
        if (mSmsService != null) {
            unregisterReceiver(mSmsService);
            mSmsService = null;
        }

        mIsSmsServiceActive = false;
    }
    /**
     * Save notification service instance.
     */
    public static void setNotificationReceiver(NotificationReceiver notificationReceiver) {
    }
//
    /**
     * Clear notification service instance.
     */
    public static void clearNotificationReceiver() {
    }


    private DownloadManager downloadManager;
    private long mTaskId;

    /**
     * ????????????
     * */
    public void downloadFirmsoft(String dialUrl, String versionName) {
        if (!dialUrl.equals(app.getDiadUrl())){
            Log.i("DATA******","dialUrl = "+dialUrl+" ;path = "+(app.getPrivatePath() + versionName));
            File file = new File(app.getPrivatePath() + versionName);
            if (file.exists()){
                file.delete();
            }
            //??????????????????
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(dialUrl));
            request.setAllowedOverRoaming(true);//??????????????????????????????

            //?????????????????????????????????????????????
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);

            //sdcard???????????????download????????????????????????
            request.setDestinationInExternalFilesDir(MainService.this, "/",versionName);

            //?????????????????????????????????
            downloadManager = (DownloadManager) MainService.this.getSystemService(Context.DOWNLOAD_SERVICE);
            //????????????????????????????????????????????????long??????id???
            //?????????id???????????????????????????????????????
            mTaskId = downloadManager.enqueue(request);

            //??????????????????????????????????????????
            MainService.this.registerReceiver(receiver,
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            app.setDialUrl(dialUrl);
        }else {
            EventBus.getDefault().post(new SendDialModel(true));
        }
    }

    /**
     * ????????????
     * */
    public boolean downloadFirmsoft(String dialUrl) {
            Log.i("DATA******","dialUrl = "+dialUrl);

        String[] fileNames = dialUrl.split("/");
        String fileName = fileNames[fileNames.length-1];
        Log.i("DATA******","fileName = "+fileName);
        File file = new File(app.getPrivatePath() + fileName);
        if (file.exists()){
            return true;
        }
        //??????????????????
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(dialUrl));
        request.setAllowedOverRoaming(true);//??????????????????????????????

        //?????????????????????????????????????????????
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);

        //sdcard???????????????download????????????????????????
        request.setDestinationInExternalFilesDir(MainService.this, "/",fileName);

        //?????????????????????????????????
        downloadManager = (DownloadManager) MainService.this.getSystemService(Context.DOWNLOAD_SERVICE);
        //????????????????????????????????????????????????long??????id???
        //?????????id???????????????????????????????????????
        mTaskId = downloadManager.enqueue(request);

        //??????????????????????????????????????????
        MainService.this.registerReceiver(receiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        app.setDialUrl(dialUrl);
        return false;
    }

    //????????????????????????????????????
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                checkDownloadStatus();
            }
        }
    };

    private void checkDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(mTaskId);//?????????????????????????????????ID???????????????
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_PAUSED:
                    //Log.d("DATA******",">>>????????????");
                case DownloadManager.STATUS_PENDING:
                    //Log.d("DATA******",">>>????????????");
                case DownloadManager.STATUS_RUNNING:
                    //Log.d("DATA******",">>>????????????");
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    Log.d("DATA******",">>>????????????");
                    EventBus.getDefault().post(new SendDialModel(true));
                    break;
                case DownloadManager.STATUS_FAILED:

                    EventBus.getDefault().post(new SendDialModel(false));
                    Log.d("DATA******",">>>????????????");
                    break;
            }
        }
    }

    private int index;
    private byte fileDatas[];
    private int page;

    private IOtaResponse iOtaResponse = new IOtaResponse() {
        @Override
        public void onStartToSendFile(int type, int address) {
            Log.d("DATA******","??????????????????");
            if (type == 0||type == 1){
                InputStream in = null;
                try {
                    in = new FileInputStream(MyApplication.getInstance().getPrivatePath()+"image.bin");
                    byte[] datas =  FileUtil.getInstance().toByteArray(in);
                    in.close();
                    fileDatas = datas;
                    index = address;
                    page = 0;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }catch (IOException e) {
                    e.printStackTrace();
                }
                timer = new Timer();
                sendDataTask = new TimerTask() {
                    @Override
                    public void run() {
                        sendByte();
                    }
                };
                timer.schedule(sendDataTask,0,20);
            }else {

            }
        }

        @Override
        public void onSendProgress() {

        }

        @Override
        public void onSendSccuess() {
            Log.d("DATA******","??????????????????");
        }

        @Override
        public void onSendFail() {
            Log.d("DATA******","??????????????????");
            if(timer!=null){
                timer.cancel();
                timer = null;
            }
        }
    };

    private Timer timer;
    private TimerTask sendDataTask;

    private void sendByte(){
        byte[] newDatas;
        int len = (fileDatas.length-index- page >175)?175:(fileDatas.length-index- page);
        if (len<0)
            return;
        newDatas = new byte[len];
        System.arraycopy(fileDatas, page+index,newDatas,0,len);
        BleClient.getInstance().writeForSendOtaFile(1,null,index+page, page/175,newDatas);
        page+=175;
        if (page>=fileDatas.length-index){
            if(timer!=null){
                timer.cancel();
                timer = null;
            }
            BleClient.getInstance().writeForSendOtaFile(2,null,0,0,null);
        }
    }

}
