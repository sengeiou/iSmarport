
package com.szip.sportwatch.BLE;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.text.format.DateFormat;

import com.mediatek.wearable.Controller;
import com.szip.sportwatch.Activity.camera.CameraActivity;
import com.szip.sportwatch.Interface.OnCameraListener;
import com.szip.sportwatch.Interface.ReceiveDataCallback;
import com.szip.sportwatch.Model.EvenBusModel.PlanModel;
import com.szip.sportwatch.Model.EvenBusModel.UnitModel;
import com.szip.sportwatch.Model.EvenBusModel.UpdateDialView;
import com.szip.sportwatch.Model.EvenBusModel.UpdateReport;
import com.szip.sportwatch.Model.HttpBean.WeatherBean;
import com.szip.sportwatch.Model.UserInfo;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.Util.DateUtil;
import com.szip.sportwatch.Util.LogUtil;
import com.szip.sportwatch.Util.MathUitl;
import com.szip.sportwatch.Util.MusicUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class EXCDController extends Controller {

//    private String cmdHead = "KCT_PEDOMETER kct_pedometer ";
    private String cmdHead = "ZNSD_WATCH znsd_watch ";

    private static final String sControllerTag = "EXCDController";

    private static final String TAG = "DATA******";

    private static EXCDController mInstance;

    private Context mContext = MyApplication.getInstance().getApplicationContext();

    private ReceiveDataCallback receiveDataCallback;

    public static final String EXTRA_DATA = "EXTRA_DATA";

    private ArrayList<String> sportList = new ArrayList<>();//运动数据索引链表，长度为0则代表无索引数据
    private ArrayList<String> steps;
    private ArrayList<String> sleeps;
    private ArrayList<String> ecgs;
    private ArrayList<String> sports;

    private OnCameraListener onCameraListener;


    private EXCDController() {
        super(sControllerTag, CMD_9);
    }

    public static EXCDController getInstance() {
        if (mInstance != null) {
            return mInstance;
        } else {
            mInstance = new EXCDController();
        }
        return mInstance;
    }

    public void setReceiveDataCallback(ReceiveDataCallback receiveDataCallback) {
        this.receiveDataCallback = receiveDataCallback;
    }

    public void setOnCameraListener(OnCameraListener onCameraListener) {
        this.onCameraListener = onCameraListener;
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void tearDown() {
        super.tearDown();
    }

    @Override
    public void onConnectionStateChange(int state) {
        super.onConnectionStateChange(state);
    }

    @Override
    public void send(String cmd, byte[] dataBuffer, boolean response, boolean progress, int priority) {
        try {
            super.send(cmd, dataBuffer, response, progress, priority);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(byte[] dataBuffer) {
        super.onReceive(dataBuffer);
        String command = new String(dataBuffer);
        String[] commands = command.split(",");

        Log.i(TAG, "onReceive(), command :" + command);

        if (commands[0].contains("GET")){//GET指令
            if (commands[1].equals("0")){//心跳包
                MyApplication.getInstance().setBtMac(commands[13]);
                String step[] = commands[5].split("\\|");
                String sleep[] = commands[6].split("\\|");
                String heart = commands[7];
                String bloodPressure = commands[15];
                String bloodOxygen = commands[16];
                String ecg = commands[22];
                String animalHeat = null;
                if(commands.length>23)
                    animalHeat = commands[23];
                LogUtil.getInstance().logd("DATA******","animal = "+animalHeat);
                if (receiveDataCallback !=null)
                    receiveDataCallback.checkVersion(!step[0].equals("0"),!step[1].equals("0"),
                            !sleep[0].equals("0"),!sleep[1].equals("0"),!heart.equals("0"),
                            !bloodPressure.equals("0"),!bloodOxygen.equals("0"),!ecg.equals("0"),(animalHeat==null)?false:!animalHeat.equals("0"),commands[17]);
            }else if (commands[1].equals("10")){//同步计步数据
                if (commands.length>2){//有数据
                    String datas[] = new String[commands.length-2];
                    System.arraycopy(commands,2,datas,0,datas.length);
                    if (receiveDataCallback !=null)
                        receiveDataCallback.getStepsForDay(datas);
                    writeForRET("GET,"+commands[1]);
                }
            }else if (commands[1].equals("11")){//同步计步详情数据
                if (commands.length>4){//有数据
                    if (steps == null){
                        steps = new ArrayList<>();
                        steps.add(command);
                    }else {
                        if (!steps.contains(command))
                            steps.add(command);
                    }
                    if (commands[2].equals(commands[3])){//接收结束
                        StringBuffer str = new StringBuffer();
                        for (int i = 0;i<steps.size();i++){
                            String strs[] = steps.get(i).split(",");
                            str.append(steps.get(i).substring(strs[0].length()+strs[1].length()+strs[2].length()+
                                    strs[3].length()+4));
                        }
                        LogUtil.getInstance().logd("DATA******","STEP str = "+str);
                        if (receiveDataCallback !=null)
                            receiveDataCallback.getSteps(str.toString().split(","));
                        steps = null;
                    }
                    writeForRET("GET,"+commands[1]+","+commands[2]+","+commands[3]);
                }
            }else if (commands[1].equals("12")){//接收睡眠数据
                if (commands.length>2){//有数据
                    String datas[] = new String[commands.length-2];
                    System.arraycopy(commands,2,datas,0,datas.length);
                    if (receiveDataCallback !=null)
                        receiveDataCallback.getSleepForDay(datas);
                    writeForRET("GET,"+commands[1]);
                }
            }else if (commands[1].equals("13")){//接收睡眠详情数据
                if (commands.length>4){//有数据
                    if (sleeps == null){
                        sleeps = new ArrayList<>();
                        sleeps.add(command);
                    }else {
                        if (!sleeps.contains(command))
                            sleeps.add(command);
                    }
                    if (commands[2].equals(commands[3])){//接收结束
                        StringBuffer str = new StringBuffer();
                        for (int i = 0;i<sleeps.size();i++){
                            String strs[] = sleeps.get(i).split(",");
                            str.append(sleeps.get(i).substring(strs[0].length()+strs[1].length()+strs[2].length()+
                                    strs[3].length()+4));
                        }
                        if (receiveDataCallback !=null)
                            receiveDataCallback.getSleep(str.toString().split(","));
                        sleeps = null;
                    }
                    writeForRET("GET,"+commands[1]+","+commands[2]+","+commands[3]);
                }
            }else if (commands[1].equals("14")){//接受心率数据
                if (commands.length>2){//有数据
                    String datas[] = new String[commands.length-2];
                    System.arraycopy(commands,2,datas,0,datas.length);
                    if (receiveDataCallback !=null)
                        receiveDataCallback.getHeart(datas);
                    writeForRET("GET,"+commands[1]);
                }
            }else if (commands[1].equals("17")){
                for (int i = 2;i<commands.length;i++){//把收到的索引统计起来
                    sportList.add(commands[i]);
                }
                if (sportList.size()!=0){//如果索引不等于0，则拿第一个数据，拿完即刻删掉
                    writeForSport(sportList.get(0));
                    sportList.remove(0);
                }else {
                    EventBus.getDefault().post(new UpdateReport());
                }
            }else if (commands[1].equals("18")){
                writeForRET("GET,"+commands[1]+","+commands[2]+","+commands[3]+","+commands[4]);
                if (commands.length>6){//有数据
                    if (sports == null){
                        sports = new ArrayList<>();
                        sports.add(command);
                    }else {
                        if (!sports.contains(command))
                            sports.add(command);
                    }
                    if (commands[3].equals(commands[4])){//接收结束
                        StringBuffer str = new StringBuffer();
                        for (int i = 0;i<sports.size();i++){
                            String strs[] = sports.get(i).split(",");
                            str.append(sports.get(i).substring(strs[0].length()+strs[1].length()+strs[2].length()+
                                    strs[3].length()+strs[4].length()+5));
                        }
                        if (receiveDataCallback !=null)
                            receiveDataCallback.getSport(str.toString().split(","));
                        if (sportList.size()!=0){
                            writeForSport(sportList.get(0));
                            sportList.remove(0);
                        }else {
                            EventBus.getDefault().post(new UpdateReport());
                        }
                        sports = null;
                    }
                }else {
                    if (sportList.size()!=0){
                        writeForSport(sportList.get(0));
                        sportList.remove(0);
                    }else {
                        EventBus.getDefault().post(new UpdateReport());
                    }
                }
            }else if (commands[1].equals("20")){//接受心电数据
                if (commands.length>4){//有数据
                    if (ecgs == null){
                        ecgs = new ArrayList<>();
                        ecgs.add(command);
                    }else {
                        if (!ecgs.contains(command))
                            ecgs.add(command);
                    }
                    if (commands[2].equals(commands[3])){//接收结束
                        StringBuffer str = new StringBuffer();
                        for (int i = 0;i<ecgs.size();i++){
                            String strs[] = ecgs.get(i).split(",");
                            str.append(ecgs.get(i).substring(strs[0].length()+strs[1].length()+strs[2].length()+
                                    strs[3].length()+5));
                        }
                        if (receiveDataCallback !=null)
                            receiveDataCallback.getEcg(str.toString().split("#"));
                        ecgs = null;
                    }
                    writeForRET("GET,"+commands[1]+","+commands[2]+","+commands[3]);
                }
            }else if (commands[1].equals("51")){//接受血压数据
                if (commands.length>2){//有数据
                    String datas[] = new String[commands.length-2];
                    System.arraycopy(commands,2,datas,0,datas.length);
                    if (receiveDataCallback !=null)
                        receiveDataCallback.getBloodPressure(datas);
                    writeForRET("GET,"+commands[1]);
                }
            }else if (commands[1].equals("52")){//接受血氧数据
                if (commands.length>2){//有数据
                    String datas[] = new String[commands.length-2];
                    System.arraycopy(commands,2,datas,0,datas.length);
                    if (receiveDataCallback !=null)
                        receiveDataCallback.getBloodOxygen(datas);
                    writeForRET("GET,"+commands[1]);
                }
            }else if (commands[1].equals("80")){//接受血氧数据
                if (commands.length>2){//有数据
                    String datas[] = new String[commands.length-2];
                    System.arraycopy(commands,2,datas,0,datas.length);
                    if (receiveDataCallback !=null)
                        receiveDataCallback.getAnimalHeat(datas);
                    writeForRET("GET,"+commands[1]);
                }
            }else if (commands[1].equals("81")){//初始化缓存完毕
                EventBus.getDefault().post(new UpdateDialView(3));
            }
        }else if (commands[0].contains("SET")){//SET指令
            if (commands[1].equals("10")){//设置计步目标
                UserInfo userInfo =((MyApplication)mContext.getApplicationContext()).getUserInfo();
                if (userInfo!=null&&commands[2]!=null) {
                    String [] datas = commands[2].split("\\|");
                    userInfo.setStepsPlan(Integer.valueOf(datas[0]));
                    EventBus.getDefault().post(new PlanModel(Integer.valueOf(datas[0])));
                }
                writeForRET("SET,"+commands[1]);
            }else if (commands[1].equals("14")){//操作相机指令
                if (((MyApplication)mContext.getApplicationContext()).isCamerable())
                    if (commands[2].equals("1")){//打开相机
                        Intent intent1=new Intent(mContext, CameraActivity.class);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent1);
                    }else if (commands[2].equals("0")){//关闭相机
                        if (onCameraListener!=null)
                            onCameraListener.onCamera(0);
                    }else {//拍照
                        if (onCameraListener!=null)
                            onCameraListener.onCamera(1);
                    }
                writeForRET("SET,"+commands[1]+","+commands[2]);
            }else if (commands[1].equals("35")){//同步单位制式
                UserInfo userInfo =((MyApplication)mContext.getApplicationContext()).getUserInfo();
                if (userInfo!=null&&commands[2]!=null) {
                    String [] datas = commands[2].split("\\|");
                    userInfo.setUnit(Integer.valueOf(datas[0]));
                    userInfo.setTempUnit(Integer.valueOf(datas[1]));
                    EventBus.getDefault().post(new UnitModel());
                }
                writeForRET("SET,"+commands[1]);
            }else if (commands[1].equals("40")){//找手机
                if (commands[2].equals("1")){//开始找手机
                   if (receiveDataCallback !=null)
                       receiveDataCallback.findPhone(1);
                }else {
                    if (receiveDataCallback !=null)
                        receiveDataCallback.findPhone(0);
                }
            }else if (commands[1].equals("43")){//收到音乐播放指令，转换成music可识别的指令
                if (commands[2].equals("4")){//暂停
                    MusicUtil.getSingle().controlMusic(127);
                }else if (commands[2].equals("3")){//播放
                    MusicUtil.getSingle().controlMusic(126);
                }else if (commands[2].equals("7")){//上一曲
                    MusicUtil.getSingle().controlMusic(88);
                }else if (commands[2].equals("8")){//下一曲
                    MusicUtil.getSingle().controlMusic(87);
                }else if (commands[2].equals("5")){//音量升
                    MusicUtil.getSingle().setVoice(1);
                }else if (commands[2].equals("6")){//音量降
                    MusicUtil.getSingle().setVoice(-1);
                }
                if (commands[2].equals("2")){
                    writeForRET("SET,"+commands[1]+",0");
                }else
                    writeForRET("SET,"+commands[1]+",1");
            }
        }else if (commands[0].contains("SEND")){
            if (commands[1].equals("10")){//同步计步数据
                if (commands.length>2){//有数据
                    String datas[] = new String[commands.length-2];
                    System.arraycopy(commands,2,datas,0,datas.length);
                    if (receiveDataCallback !=null)
                        receiveDataCallback.getStepsForDay(datas);
                    writeForRET("GET,"+commands[1]);
                }
            }else if (commands[1].equals("12")){//接受心率数据
                if (commands.length>2){//有数据
                    String datas[] = new String[commands.length-2];
                    System.arraycopy(commands,2,datas,0,datas.length);
                    if (receiveDataCallback !=null)
                        receiveDataCallback.getHeart(datas);
                    writeForRET("GET,"+14);
                }
            }else if (commands[1].equals("14")){//接受心率数据
                if (commands.length>2){//有数据
                    String datas[] = new String[commands.length-2];
                    System.arraycopy(commands,2,datas,0,datas.length);
                    if (receiveDataCallback !=null)
                        receiveDataCallback.getHeart(datas);
                    writeForRET("GET,"+14);
                }
            }else if (commands[1].equals("20")){//接受心电数据
                if (commands.length>4){//有数据
                    if (ecgs == null){
                        ecgs = new ArrayList<>();
                        ecgs.add(command);
                    }else {
                        ecgs.add(command);
                    }
                    if (commands[2].equals(commands[3])){//接收结束
                        StringBuffer str = new StringBuffer();
                        for (int i = 0;i<ecgs.size();i++){
                            String strs[] = ecgs.get(i).split(",");
                            str.append(ecgs.get(i).substring(strs[0].length()+strs[1].length()+strs[2].length()+
                                    strs[3].length()+5));
                        }
                        if (receiveDataCallback !=null)
                            receiveDataCallback.getEcg(str.toString().split("#"));
                        ecgs = null;
                    }
                    writeForRET("GET,"+commands[1]+","+commands[2]+","+commands[3]);
                }
            }else if (commands[1].equals("51")){//接受血压数据
                if (commands.length>2){//有数据
                    String datas[] = new String[commands.length-2];
                    System.arraycopy(commands,2,datas,0,datas.length);
                    if (receiveDataCallback !=null)
                        receiveDataCallback.getBloodPressure(datas);
                    writeForRET("GET,"+commands[1]);
                }
            }else if (commands[1].equals("52")){//接受血氧数据
                if (commands.length>2){//有数据
                    String datas[] = new String[commands.length-2];
                    System.arraycopy(commands,2,datas,0,datas.length);
                    if (receiveDataCallback !=null)
                        receiveDataCallback.getBloodOxygen(datas);
                    writeForRET("GET,"+commands[1]);
                }
            }else if (commands[1].equals("80")){//接受血氧数据
                if (commands.length>2){//有数据
                    String datas[] = new String[commands.length-2];
                    System.arraycopy(commands,2,datas,0,datas.length);
                    if (receiveDataCallback !=null)
                        receiveDataCallback.getAnimalHeat(datas);
                    writeForRET("GET,"+commands[1]);
                }
            }

        }else if (commands[0].contains("RET")){//RET指令
            if(commands[2].equals("81")){
                EventBus.getDefault().post(new UpdateDialView(Integer.valueOf(commands[3])));
            }
        }
    }


    /**
     * 应用指令集
     * */

    //使能SEND指令
    public void writeForEnableSend(int state){
        String str = "SET,15,"+state;
        byte[] datas = new byte[0];
        try {
            datas = str.getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.send(cmdHead+"0 0 8 ",datas,true,false,0);
    }

    //心跳包
    public void writeForCheckVersion(){
        String str = "GET,0";
        byte[] datas = new byte[0];
        try {
            datas = str.getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.send(cmdHead+"0 0 5 ",datas,true,false,0);
    }

    //同步语言
    public void writeForSetLanuage(String lanuage){
        String str = "SET,44,"+lanuage;
        LogUtil.getInstance().logd("lanuage******","str = "+str);
        byte[] datas = new byte[0];
        try {
            datas = str.getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.send(cmdHead+"0 0 12 ",datas,true,false,0);
    }

    //日均计步数据
    public void writeForGetDaySteps(){
        String str = "GET,10";
        byte[] datas = new byte[0];
        try {
            datas = str.getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.send(cmdHead+"0 0 6 ",datas,true,false,0);
    }
    //日详情计步数据
    public void writeForGetSteps(){
        String str = "GET,11";
        byte[] datas = new byte[0];
        try {
            datas = str.getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.send(cmdHead+"0 0 6 ",datas,true,false,0);
    }

    //日均睡眠数据
    public void writeForGetDaySleep(){
        String str = "GET,12";
        byte[] datas = new byte[0];
        try {
            datas = str.getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.send(cmdHead+"0 0 6 ",datas,true,false,0);
    }

    //日详情睡眠
    public void writeForGetSleep(){
        String str = "GET,13";
        byte[] datas = new byte[0];
        try {
            datas = str.getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.send(cmdHead+"0 0 6 ",datas,true,false,0);
    }

    //获取心率
    public void writeForGetHeart(){
        String str = "GET,14";
        byte[] datas = new byte[0];
        try {
            datas = str.getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.send(cmdHead+"0 0 6 ",datas,true,false,0);
    }

    //获取运动数据索引
    public void writeForGetSportPosition(){
        String str = "GET,17";
        byte[] datas = new byte[0];
        try {
            datas = str.getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.send(cmdHead+"0 0 8 ",datas,true,false,0);
    }

    //获取运动数据
    public void writeForGetSportData(int index){
        String str = String.format(Locale.ENGLISH,"GET,18,%d",index);
        byte[] datas = new byte[0];
        try {
            datas = str.getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.send(cmdHead+"0 0 8 ",datas,true,false,0);
    }

    //获取血压数据
    public void writeForGetBloodPressure(){
        String str = "GET,51";
        byte[] datas = new byte[0];
        try {
            datas = str.getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.send(cmdHead+"0 0 6 ",datas,true,false,0);
    }

    //获取血氧数据
    public void writeForGetBloodOxygen(){
        String str = "GET,52";
        byte[] datas = new byte[0];
        try {
            datas = str.getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.send(cmdHead+"0 0 6 ",datas,true,false,0);
    }

    //获取ecg数据
    public void writeForGetEcg(){
        String str = "GET,20";
        byte[] datas = new byte[0];
        try {
            datas = str.getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.send(cmdHead+"0 0 6 ",datas,true,false,0);
    }

    //获取体温数据
    public void writeForGetAnimalHeat(){
        String str = "GET,80";
        byte[] datas = new byte[0];
        try {
            datas = str.getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.send(cmdHead+"0 0 6 ",datas,true,false,0);
    }

    //设置时间
    public void writeForSetDate(){
        int gmt = DateUtil.getGMT();
        boolean is24Hour = DateFormat.is24HourFormat(MyApplication.getInstance());
        String str = "SET,45,"+(is24Hour?"1|":"0|")+(gmt>0?"+":"-")+String.format(Locale.ENGLISH,"%04.1f|%d",Math.abs(gmt)/60f, Calendar.getInstance().getTimeInMillis()/1000);
        LogUtil.getInstance().logd("DATA******","DATA STR = "+str);
        byte[] datas = new byte[0];
        try {
            datas = str.getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.send(cmdHead+String.format(Locale.ENGLISH,"0 0 %d ",str.length()),datas,true,false,0);
    }

    public void writeForSetUnit(UserInfo info){
        String str = "SET,35,"+(info.getUnit()+"")+(","+info.getTempUnit());
        byte[] datas = new byte[0];
        try {
            datas = str.getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.send(cmdHead+"0 0 10 ",datas,true,false,0);
    }

    //设置个人信息
    public void writeForSetInfo(UserInfo info){
        int height;
        int weight;
        if(info.getUnit()==1){
            height = info.getHeightBritish()!=0?MathUitl.inch2Cm(info.getHeightBritish()):66;
            weight = info.getWeightBritish()!=0?MathUitl.pound2Kg(info.getWeightBritish()):132;
        }else {
            height = info.getHeight()!=0?info.getHeight():170;
            weight = info.getWeight()!=0?info.getWeight():60;
        }
        String str = "SET,10,"+String.format(Locale.ENGLISH,"%d|%d|%d|%d",info.getStepsPlan(),info.getSex(),height,weight);

        LogUtil.getInstance().logd("DATA******","STR = "+str);
        byte[] datas = new byte[0];
        try {
            datas = str.getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.send(cmdHead+String.format(Locale.ENGLISH,"0 0 %d ",str.length()),datas,true,false,0);
    }

    //找手表
    public void writeForFindDevice(){
        String str = "SET,40,1";
        byte[] datas = new byte[0];
        try {
            datas = str.getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.send(cmdHead+"0 0 8 ",datas,true,false,0);
    }

    //获取运动指引
    public void writeForSportIndex(){
        String str = "GET,17";
        byte[] datas = new byte[0];
        try {
            datas = str.getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.send(cmdHead+"0 0 6 ",datas,true,false,0);
    }

    //获取运动数据
    public void writeForSport(String index){
        String str = "GET,18,"+index;
        byte[] datas = new byte[0];
        try {
            datas = str.getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.send(cmdHead+String.format("0 0 %d ",str.length()),datas,true,false,0);
    }

    //同步天气
    public void writeForUpdateWeather(ArrayList<WeatherBean.Condition> weatherModel,String city){

        String str;
        if (weatherModel!=null){
            str = "WEATHER;"+city+";0,"+DateUtil.getStringDateFromSecond
                    (Calendar.getInstance().getTimeInMillis()/1000,"yyyy-MM-dd")+","+((int)weatherModel.get(0).getLow())+
                    ","+((int)weatherModel.get(0).getHigh())+","+weatherModel.get(0).getCode()+"|1,"+DateUtil.getStringDateFromSecond
                    (Calendar.getInstance().getTimeInMillis()/1000+24*60*60,"yyyy-MM-dd")+","+((int)weatherModel.get(1).getLow())+
                    ","+((int)weatherModel.get(1).getHigh())+","+weatherModel.get(1).getCode()+"|2,"+DateUtil.getStringDateFromSecond
                    (Calendar.getInstance().getTimeInMillis()/1000+24*60*60*2,"yyyy-MM-dd")+","+((int)weatherModel.get(2).getLow())+
                    ","+((int)weatherModel.get(2).getHigh())+","+weatherModel.get(2).getCode()+"|";

            byte[] datas;

            datas = str.getBytes();

            LogUtil.getInstance().logd("DATA******","DATA = "+new String(datas));
            this.send(cmdHead+String.format(Locale.ENGLISH,"0 0 %d ",datas.length),datas,true,false,0);
        }
    }

     //初始化底层缓存，以便接收图片
    public void initDialInfo(){
        String str = "GET,81";
        byte[] datas = new byte[0];
        try {
            datas = str.getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.send(cmdHead+"0 0 6 ",datas,true,false,0);
    }

    //发送图片
    public void writeForSendImage(byte[] image,int index,int num,int clock,int clockStye){
        String str = String.format("SET,81,%d;%d;%d;%d;%d;",clockStye,clock,image.length,num,index);
        byte[] datas = new byte[0];
        try {
            datas = str.getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] newDatas = new byte[datas.length+image.length+1];
        System.arraycopy(datas,0,newDatas,0,datas.length);
        System.arraycopy(image,0,newDatas,datas.length,image.length);
        newDatas[newDatas.length-1] = 59;
        LogUtil.getInstance().logd("DATA******","SEND IMAGE = "+str);
        this.send(cmdHead+String.format(Locale.ENGLISH,"0 0 %d ",newDatas.length),newDatas,true,false,0);

    }

    private   String getCnASCII(String cnStr) {
        StringBuffer strBuf = new StringBuffer();
        // 将字符串转换成字节序列
        byte[] bGBK = cnStr.getBytes();
        for (int i = 0; i < bGBK.length; i++) {
            // 将每个字符转换成ASCII码
            strBuf.append(Integer.toHexString(bGBK[i] & 0xff) + " ");
        }
        return strBuf.toString();
    }

    //回复指令
    public void writeForRET(String tag){
        String str = "RET,"+tag;
        byte[] datas = new byte[0];
        try {
            datas = str.getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.send(cmdHead+String.format(Locale.ENGLISH,"0 0 %d ",str.length()),datas,true,false,0);
    }
}
