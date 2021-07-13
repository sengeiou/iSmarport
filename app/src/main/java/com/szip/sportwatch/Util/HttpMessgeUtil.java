package com.szip.sportwatch.Util;


import android.content.Context;

import com.szip.sportwatch.BuildConfig;
import com.szip.sportwatch.DB.SaveDataUtil;
import com.szip.sportwatch.Interface.HttpCallbackWithBase;
import com.szip.sportwatch.Interface.HttpCallbackWithLogin;
import com.szip.sportwatch.Interface.HttpCallbackWithUserInfo;
import com.szip.sportwatch.Model.HttpBean.AvatarBean;
import com.szip.sportwatch.Model.HttpBean.BaseApi;
import com.szip.sportwatch.Model.HttpBean.BindBean;
import com.szip.sportwatch.Model.HttpBean.CheckUpdateBean;
import com.szip.sportwatch.Model.HttpBean.CheckVerificationBean;
import com.szip.sportwatch.Model.HttpBean.DeviceConfigBean;
import com.szip.sportwatch.Model.HttpBean.DialBean;
import com.szip.sportwatch.Model.HttpBean.DownloadDataBean;
import com.szip.sportwatch.Model.HttpBean.LoginBean;
import com.szip.sportwatch.Model.HttpBean.UserInfoBean;
import com.szip.sportwatch.Model.HttpBean.WeatherBean;
import com.szip.sportwatch.Model.UserInfo;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.GenericsCallback;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.szip.sportwatch.MyApplication.FILE;

public class HttpMessgeUtil {

    private static HttpMessgeUtil mInstance;
    private String url = BuildConfig.SERVER_URL;
    private String token = "null";
    private String language = "zh-CN";
    private String time;

    private Context mContext;

    private HttpCallbackWithBase httpCallbackWithBase;
    private HttpCallbackWithLogin httpCallbackWithLogin;

    private int GET_VERIFICATION = 100;
    public static int UPDOWN_LOG = 101;
    public static int UPDOWN_DATA = 102;
    public static int UPDOWN_AVATAR = 103;
    public static int UPDATA_USERINFO = 104;

    public static HttpMessgeUtil getInstance()
    {
        if (mInstance == null)
        {
            synchronized (HttpMessgeUtil.class)
            {
                if (mInstance == null)
                {
                    mInstance = new HttpMessgeUtil();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context){
        mContext = context;
        language = context.getResources().getConfiguration().locale.getLanguage()+"-"+
                context.getResources().getConfiguration().locale.getCountry();
        time = DateUtil.getGMTWithString();
    }


    public void setToken(String token){
        this.token = token;
    }

    public void setHttpCallbackWithBase(HttpCallbackWithBase httpCallbackWithBase) {
        this.httpCallbackWithBase = httpCallbackWithBase;
    }

    public void setHttpCallbackWithLogin(HttpCallbackWithLogin httpCallbackWithLogin) {
        this.httpCallbackWithLogin = httpCallbackWithLogin;
    }

    /**
     * 注册接口
     * @param type                   注册类型1：手机注册 2：邮箱注册
     * @param areaCode               区号
     * @param phoneNumber            手机
     * @param email                  邮箱
     * @param verifyCode             验证码
     * @param password               密码
     * */
    private void _postRegister(String type,String areaCode,String phoneNumber,String email,String verifyCode,String password,
                               String phoneId,String phoneSystem) throws IOException {
        String url = this.url+"user/signUp";
        OkHttpUtils
                .jpost()
                .url(url)
                .addHeader("Accept-Language",language)
                .addHeader("Time-Diff",time)
                .addParams("type",type)
                .addParams("areaCode",areaCode)
                .addParams("phoneNumber",phoneNumber)
                .addParams("email",email)
                .addParams("verifyCode",verifyCode)
                .addParams("password",password)
                .addParams("phoneId",phoneId)
                .addParams("phoneSystem",phoneSystem)
                .build()
                .execute(baseApiGenericsCallback,new TokenInterceptor());
    }

    /**
     * 获取验证码接口
     * @param type                  验证码类型1：手机 2：邮箱
     * @param areaCode              区号
     * @param phoneNumber           手机号码
     * @param email                 邮箱
     * */
    private void _getVerificationCode(String type,String areaCode,String phoneNumber,String email)throws IOException{
        final String url = this.url+"user/sendVerifyCode";
        OkHttpUtils
                .jpost()
                .id(GET_VERIFICATION)
                .url(url)
                .addHeader("Time-Diff",time)
                .addHeader("Accept-Language",language)
                .addParams("type",type)
                .addParams("areaCode",areaCode)
                .addParams("phoneNumber",phoneNumber)
                .addParams("email",email)
                .build()
                .execute(baseApiGenericsCallback,new TokenInterceptor());
    }

    /**
     * 验证验证码接口
     * @param type                  验证码类型1：手机 2：邮箱
     * @param areaCode              区号
     * @param phoneNumber           手机号码
     * @param email                 邮箱
     * @param verifyCode            验证码
     * */
    private void _postCheckVerifyCode(String type,String areaCode,String phoneNumber,String email,String verifyCode)throws IOException{
        final String url = this.url+"user/checkVerifyCode";
        OkHttpUtils
                .jpost()
                .url(url)
                .addHeader("Accept-Language",language)
                .addParams("type",type)
                .addParams("areaCode",areaCode)
                .addParams("phoneNumber",phoneNumber)
                .addParams("email",email)
                .addParams("verifyCode",verifyCode)
                .build()
                .execute(verificationBeanGenericsCallback,new TokenInterceptor());
    }

    /**
     * 登录接口
     * @param type                   注册类型1：手机注册 2：邮箱注册
     * @param areaCode               区号
     * @param phoneNumber            手机
     * @param email                  邮箱
     * @param password               密码
     * */
    private void _postLogin(String type,String areaCode, String phoneNumber, String email, String password,String phoneId,String phoneSystem)throws IOException{
        String url = this.url+"v2/user/login";
        OkHttpUtils
                .jpost()
                .url(url)
                .addHeader("Time-Diff",time)
                .addHeader("Accept-Language",language)
                .addParams("type",type)
                .addParams("areaCode",areaCode)
                .addParams("phoneNumber",phoneNumber)
                .addParams("email",email)
                .addParams("password",password)
                .addParams("phoneId",phoneId)
                .addParams("phoneSystem",phoneSystem)
                .build()
                .execute(loginBeanGenericsCallback,new TokenInterceptor());
    }

    /**
     * 忘记密码接口
     * @param type               找回类型1：手机  2：邮箱
     * @param areaCode           区号
     * @param phoneNumber        手机号码
     * @param email              邮箱
     * @param verifyCode         验证码
     * @param password           密码
     * */
    private void _postForgotPassword(String type,String areaCode,String phoneNumber,String email,String verifyCode,
                                     String password)throws IOException{
        String url = this.url+"user/retrievePassword";
        OkHttpUtils
                .jpost()
                .url(url)
                .addHeader("Time-Diff",time)
                .addHeader("Accept-Language",language)
                .addParams("type",type)
                .addParams("areaCode",areaCode)
                .addParams("phoneNumber",phoneNumber)
                .addParams("email",email)
                .addParams("verifyCode",verifyCode)
                .addParams("password",password)
                .build()
                .execute(baseApiGenericsCallback,new TokenInterceptor());
    }

    /**
     * 发送意见反馈接口
     * */
    private void _postSendFeedback(String content)throws IOException{
        String url = this.url+"user/uploadFeedback";
        OkHttpUtils
                .jpost()
                .url(url)
                .addHeader("Time-Diff",time)
                .addHeader("token",token)
                .addHeader("Accept-Language",language)
                .addParams("content",content)
                .build()
                .execute(baseApiGenericsCallback,new TokenInterceptor());
    }

    /**
     * 获取个人信息
     * */
    private void _getForGetInfo(GenericsCallback<UserInfoBean> callback)throws IOException{
        String url = this.url+"v2/user/getUserInfo";
        OkHttpUtils
                .get()
                .url(url)
                .addHeader("Time-Diff",time)
                .addHeader("token",token)
                .addHeader("Accept-Language",language)
                .build()
                .execute(callback, new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        return chain.proceed(chain.request());
                    }
                });
    }


    /**
     * 设置用户信息
     * @param name             名字
     * @param sex              性别
     * @param birthday         生日
     * @param height           身高
     * @param weight           体重
     * */
    private void _postForSetUserInfo(String name,String sex,String birthday,String height,String weight,
                                     String heightBritish,String weightBritish,GenericsCallback<BaseApi> callback)throws IOException{
        String url = this.url+"v2/user/updateUserInfo";
        OkHttpUtils
                .jpost()
                .url(url)
                .id(UPDATA_USERINFO)
                .addHeader("Time-Diff",time)
                .addHeader("token",token)
                .addHeader("Accept-Language",language)
                .addParams("userName",name)
                .addParams("lastName","")
                .addParams("firstName","")
                .addParams("sex",sex)
                .addParams("birthday",birthday)
                .addParams("nation","")
                .addParams("height",height)
                .addParams("weight",weight)
                .addParams("heightBritish",heightBritish)
                .addParams("weightBritish",weightBritish)
                .addParams("blood","")
                .build()
                .execute(callback,new TokenInterceptor());
    }

    private void _postForSetStepsPlan(String stepsPlan,int id,GenericsCallback<BaseApi> callback)throws IOException{
        String url = this.url+"user/updateStepsPlan";
        OkHttpUtils
                .jpost()
                .url(url)
                .id(id)
                .addHeader("Time-Diff",time)
                .addHeader("token",token)
                .addHeader("Accept-Language",language)
                .addParams("stepsPlan",stepsPlan)
                .build()
                .execute(callback,new TokenInterceptor());
    }

    private void _postForSetSleepPlan(String sleepPlan,int id)throws IOException{
        String url = this.url+"user/updateSleepPlan";
        OkHttpUtils
                .jpost()
                .url(url)
                .id(id)
                .addHeader("Time-Diff",time)
                .addHeader("token",token)
                .addHeader("Accept-Language",language)
                .addParams("sleepPlan",sleepPlan)
                .build()
                .execute(baseApiGenericsCallback,new TokenInterceptor());
    }

    private void _postForSetUnit(String unit,String temp,GenericsCallback<BaseApi>callback)throws IOException{
        String url = this.url+"v2/user/setUnit";
        OkHttpUtils
                .jpost()
                .url(url)
                .addHeader("Time-Diff",time)
                .addHeader("token",token)
                .addHeader("Accept-Language",language)
                .addParams("unit",unit)
                .addParams("tempUnit",temp)
                .build()
                .execute(callback,new TokenInterceptor());
    }

    private void _postForCheckUpdate(String var ,GenericsCallback<CheckUpdateBean> callback)throws IOException{
        String url = this.url+"comm/checkUpdate";
        OkHttpUtils
                .get()
                .url(url)
                .addHeader("Time-Diff",time)
                .addHeader("token",token)
                .addHeader("Accept-Language",language)
                .addParams("phoneSystem","android")
                .addParams("currentVersion",var)
                .build()
                .execute(callback,new TokenInterceptor());
    }

    /**
     * 上传头像
     * */
    private void _postUpdownAvatar(File avatar, GenericsCallback<AvatarBean> callback)throws IOException{
        String url = this.url+"user/setProfilePicture";
        OkHttpUtils
                .fpost()
                .url(url)
                .id(UPDOWN_AVATAR)
                .addHeader("token",token)
                .addHeader("Accept-Language",language)
                .addFile("file","iSmarport_6.jpg",avatar)
                .build()
                .execute(callback,new TokenInterceptor());
    }


    /**
     * 绑定设备
     * */
    private void _getBindDevice(String deviceCode,String product, GenericsCallback<BindBean> callback)throws IOException{
        String url = this.url+"device/bindDevice";
        OkHttpUtils
                .get()
                .url(url)
                .addHeader("Time-Diff",time)
                .addHeader("token",token)
                .addHeader("Accept-Language",language)
                .addParams("deviceCode",deviceCode)
                .addParams("product",product)
                .build()
                .execute(callback,new TokenInterceptor());
    }

    /**
     * 天气预报
     * */
    private void _getWeather(String lat,String lon,GenericsCallback<WeatherBean> callback)throws IOException{
        String url = this.url+"comm/weather";
        OkHttpUtils
                .get()
                .url(url)
                .addHeader("Time-Diff",time)
                .addHeader("token",token)
                .addHeader("Accept-Language",language)
                .addParams("lat",lat)
                .addParams("lon",lon)
                .build()
                .execute(callback,new TokenInterceptor());
    }


    /**
     * 解绑设备
     * */
    private void _getUnbindDevice()throws IOException{
        String url = this.url+"device/unbindDevice";
        OkHttpUtils
                .get()
                .url(url)
                .addHeader("Time-Diff",time)
                .addHeader("token",token)
                .addHeader("Accept-Language",language)
                .build()
                .execute(baseApiGenericsCallback,new TokenInterceptor());
    }


    /**
     * 上传log数据
     * */
    private void _postAppCrashLog(String appName,String appVersion,String systemInfo,String stackTrace)throws IOException{
        String url = this.url+"appCrashLog/upload";
        OkHttpUtils
                .jpost()
                .url(url)
                .id(UPDOWN_LOG)
                .addParams("appName",appName)
                .addParams("appVersion",appVersion)
                .addParams("systemInfo",systemInfo)
                .addParams("stackTrace",stackTrace)
                .build()
                .execute(baseApiGenericsCallback,new TokenInterceptor());
    }

    /**
     * 上传数据到服务器
     * */
    private void _postForUpdownReportData(String data)throws IOException{
        String url = this.url+"data/upload";
        OkHttpUtils
                .listpost()
                .url(url)
                .id(UPDOWN_DATA)
                .addHeader("Time-Diff",time)
                .addHeader("token",token)
                .addHeader("Accept-Language",language)
                .addParams("data",data)
                .build()
                .execute(baseApiGenericsCallback,new TokenInterceptor());
    }

    /**
     * 获取服务器上的数据
     * */
    private void _getForDownloadReportData(String time, String size)throws IOException{
        String url = this.url+"data/get";
        OkHttpUtils
                .get()
                .url(url)
                .addHeader("Time-Diff",time)
                .addHeader("token",token)
                .addHeader("Accept-Language",language)
                .addParams("time",time)
                .addParams("size",size)
                .build()
                .execute(reportDataBeanGenericsCallback,new TokenInterceptor());
    }

    private void _getDeviceConfig(GenericsCallback<DeviceConfigBean> callback)throws IOException{
        String url = this.url+"comm/getAppFunctionConfigs";
        OkHttpUtils
                .get()
                .url(url)
                .addHeader("Time-Diff",time)
                .addHeader("Accept-Language",language)
                .build()
                .execute(callback,new TokenInterceptor());
    }

    private void _getDialList(String watchPlateGroupId,GenericsCallback<DialBean> callback)throws IOException{
        String url = this.url+"device/watchPlate";
        OkHttpUtils
                .get()
                .url(url)
                .addHeader("Time-Diff",time)
                .addHeader("Accept-Language",language)
                .addHeader("token",token)
                .addParams("pageNum","1")
                .addParams("pageSize","30")
                .addParams("watchPlateGroupId",watchPlateGroupId)
                .build()
                .execute(callback,new TokenInterceptor());
    }

    public void getDeviceConfig(GenericsCallback<DeviceConfigBean> callback)throws IOException{
        _getDeviceConfig(callback);
    }

    public void postAppCrashLog(String appName,String appVersion,String systemInfo,String stackTrace)throws IOException{
        _postAppCrashLog(appName,appVersion,systemInfo,stackTrace);
    }


    /**
     * 提供给用户的方法
     * */
    public void postRegister(String type,String areaCode,String phoneNumber,String email,String verifyCode,String password,
                             String phoneId,String phoneSystem) throws IOException{
        _postRegister(type,areaCode,phoneNumber,email,verifyCode,password, phoneId, phoneSystem);
    }

    public void getVerificationCode(String type,String areaCode,String phoneNumber,String email)throws IOException{

        _getVerificationCode(type,areaCode,phoneNumber,email);
    }

    public void postCheckVerifyCode(String type,String areaCode,String phoneNumber,String email,String verifyCode)throws IOException{
        _postCheckVerifyCode(type,areaCode,phoneNumber,email,verifyCode);
    }

    public void postLogin(String type, String areaCode,String phoneNumber, String email, String password,String phoneId,String phoneSystem)throws IOException{
        _postLogin(type,areaCode,phoneNumber,email,password,phoneId,phoneSystem);
    }

    public void postForgotPassword(String type,String areaCode,String phoneNumber,String email,String verifyCode,
                                   String password)throws IOException{
        _postForgotPassword(type,areaCode,phoneNumber,email,verifyCode,password);
    }

    public void postForSetUserInfo(String name,String sex,String birthday,String height,String weight,
                                   String heightBritish,String weightBritish,GenericsCallback<BaseApi> callback)throws IOException{
        _postForSetUserInfo(name,sex,birthday,height,weight,heightBritish, weightBritish,callback);
    }
    public void postForSetUserInfo1(UserInfo info,GenericsCallback<BaseApi>callback)throws IOException{
        _postForSetUserInfo(info.getUserName(),info.getSex()+"",info.getBirthday(),info.getHeight()+"",info.getWeight()+"",
                info.getHeightBritish()+"", info.getWeightBritish()+"",callback);
        _postForSetStepsPlan(info.getStepsPlan()+"",0,callback);
        _postForSetUnit(info.getUnit()+"",info.getTempUnit()+"",callback);
    }
    public void getForGetInfo(GenericsCallback<UserInfoBean> callback)throws IOException{
        _getForGetInfo(callback);
    }

    public void postForSetStepsPlan(String stepsPlan,int id)throws IOException{
        _postForSetStepsPlan(stepsPlan,id,baseApiGenericsCallback);
    }

    public void postForSetSleepPlan(String sleepPlan,int id)throws IOException{
        _postForSetSleepPlan(sleepPlan,id);
    }

    public void postForSetUnit(String unit,String temp)throws IOException{
        _postForSetUnit(unit,temp,baseApiGenericsCallback);
    }

    public void getBindDevice(String deviceCode,String product,GenericsCallback<BindBean> callback)throws IOException{
        _getBindDevice(deviceCode,product,callback);
    }

    public void getUnbindDevice()throws IOException{
        _getUnbindDevice();
    }

    public void postForUpdownReportData(String data)throws IOException{
        _postForUpdownReportData(data);
    }

    public void getForDownloadReportData(String time, String size)throws IOException{
        _getForDownloadReportData(time,size);
    }

    public void postUpdownAvatar(File avatar,GenericsCallback<AvatarBean> callback)throws IOException{
        _postUpdownAvatar(avatar,callback);
    }

    public void postSendFeedback(String content)throws IOException{
        _postSendFeedback(content);
    }

    public void getWeather(String lat,String lon,GenericsCallback<WeatherBean> callback)throws IOException{
        _getWeather(lat,lon,callback);
    }

    public void postForCheckUpdate(String var ,GenericsCallback<CheckUpdateBean> callback)throws IOException{
        _postForCheckUpdate(var,callback);
    }

    public void getDialList(String watchPlateGroupId ,GenericsCallback<DialBean> callback)throws IOException{
        _getDialList(watchPlateGroupId,callback);
    }
    /**
     * 接口回调
     **/
    private GenericsCallback<BaseApi> baseApiGenericsCallback = new GenericsCallback<BaseApi>(new JsonGenericsSerializator()) {
        @Override
        public void onError(Call call, Exception e, int id) {
        }

        @Override
        public void onResponse(BaseApi response, int id) {
            if (response.getCode() == 200){
                if (id == UPDOWN_DATA){
                    MathUitl.saveLastTime(mContext.getSharedPreferences(FILE,MODE_PRIVATE));
                }else if (id != GET_VERIFICATION){//如果不是获取验证码的回调，则返回回调数据
                    if (httpCallbackWithBase != null)
                        httpCallbackWithBase.onCallback(response,id);
                }
            }else {
                ProgressHudModel.newInstance().diss();
                MathUitl.showToast(mContext,response.getMessage());
            }
        }
    };

    private GenericsCallback<CheckVerificationBean> verificationBeanGenericsCallback = new GenericsCallback<CheckVerificationBean>(new JsonGenericsSerializator()) {
        @Override
        public void onError(Call call, Exception e, int id) {

        }

        @Override
        public void onResponse(CheckVerificationBean response, int id) {
            if (response.getCode() == 200){
                if (response.getData().isValid()){
                    if (httpCallbackWithBase!=null)
                        httpCallbackWithBase.onCallback(new BaseApi(),0);
                }
            }else {
                ProgressHudModel.newInstance().diss();
                MathUitl.showToast(mContext,response.getMessage());
            }
        }
    };

    private GenericsCallback<LoginBean> loginBeanGenericsCallback = new GenericsCallback<LoginBean>(new JsonGenericsSerializator()) {
        @Override
        public void onError(Call call, Exception e, int id) {

        }

        @Override
        public void onResponse(LoginBean response, int id) {
            if (response.getCode() == 200){
                if (httpCallbackWithLogin!=null)
                    httpCallbackWithLogin.onLogin(response);
            }else {
                ProgressHudModel.newInstance().diss();
                MathUitl.showToast(mContext,response.getMessage());
            }
        }
    };

    private GenericsCallback<DownloadDataBean> reportDataBeanGenericsCallback = new GenericsCallback<DownloadDataBean>(new JsonGenericsSerializator()) {
        @Override
        public void onError(Call call, Exception e, int id) {
        }

        @Override
        public void onResponse(DownloadDataBean response, int id) {
            if (response.getCode() == 200){
                SaveDataUtil saveDataUtil = SaveDataUtil.newInstance();
                if (response.getData().getBloodOxygenData().size()!=0)
                    saveDataUtil.saveBloodOxygenDataListData(response.getData().getBloodOxygenData());

                if (response.getData().getBloodPressureDataList().size()!=0)
                    saveDataUtil.saveBloodPressureDataListData(response.getData().getBloodPressureDataList());

                if (response.getData().getHeartDataList().size()!=0)
                    saveDataUtil.saveHeartDataListData(response.getData().getHeartDataList(),false);

                if (response.getData().getSleepDataList().size()!=0)
                    saveDataUtil.saveSleepDataListData(response.getData().getSleepDataList());

                if (response.getData().getStepDataList().size()!=0)
                    saveDataUtil.saveStepDataListDataFromWeb(response.getData().getStepDataList());

                if (response.getData().getEcgDataList().size()!=0)
                    saveDataUtil.saveEcgDataListData(response.getData().getEcgDataList());

                if (response.getData().getAnimalHeatDataList().size()!=0)
                    saveDataUtil.saveAnimalHeatDataListData(response.getData().getAnimalHeatDataList());

                if (response.getData().getSportDataList().size()!=0)
                    saveDataUtil.saveSportDataListData(response.getData().getSportDataList());

                MathUitl.saveLastTime(mContext.getSharedPreferences(FILE,MODE_PRIVATE));
            }else {
                ProgressHudModel.newInstance().diss();
                MathUitl.showToast(mContext,response.getMessage());
            }
        }
    };



}
