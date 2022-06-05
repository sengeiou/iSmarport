package com.szip.sportwatch.Util;


import android.content.Context;
import android.os.Handler;

import com.szip.sportwatch.BuildConfig;
import com.szip.sportwatch.DB.SaveDataUtil;
import com.szip.sportwatch.Model.HttpBean.AvatarBean;
import com.szip.sportwatch.Model.HttpBean.BaseApi;
import com.szip.sportwatch.Model.HttpBean.BindBean;
import com.szip.sportwatch.Model.HttpBean.CheckUpdateBean;
import com.szip.sportwatch.Model.HttpBean.CheckVerificationBean;
import com.szip.sportwatch.Model.HttpBean.DeviceConfigBean;
import com.szip.sportwatch.Model.HttpBean.DialBean;
import com.szip.sportwatch.Model.HttpBean.DownloadDataBean;
import com.szip.sportwatch.Model.HttpBean.FaqBean;
import com.szip.sportwatch.Model.HttpBean.FaqListBean;
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

    public static int GET_VERIFICATION = 100;
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

    public String getToken() {
        return token;
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
                               String phoneId,String phoneSystem,GenericsCallback<BaseApi> callback) throws IOException {
        String url = this.url+"user/signUp";
        OkHttpUtils
                .jpost()
                .url(url)
                .addHeader("project",BuildConfig.APP_NAME)
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
                .execute(callback,new TokenInterceptor());
    }

    /**
     * 获取验证码接口
     * @param type                  验证码类型1：手机 2：邮箱
     * @param areaCode              区号
     * @param phoneNumber           手机号码
     * @param email                 邮箱
     * */
    private void _getVerificationCode(String type,String areaCode,String phoneNumber,String email,GenericsCallback<BaseApi> callback)throws IOException{
        final String url = this.url+"user/sendVerifyCode";
        OkHttpUtils
                .jpost()
                .id(GET_VERIFICATION)
                .url(url)
                .addHeader("project",BuildConfig.APP_NAME)
                .addHeader("Time-Diff",time)
                .addHeader("Accept-Language",language)
                .addParams("type",type)
                .addParams("areaCode",areaCode)
                .addParams("phoneNumber",phoneNumber)
                .addParams("email",email)
                .build()
                .execute(callback,new TokenInterceptor());
    }

    /**
     * 验证验证码接口
     * @param type                  验证码类型1：手机 2：邮箱
     * @param areaCode              区号
     * @param phoneNumber           手机号码
     * @param email                 邮箱
     * @param verifyCode            验证码
     * */
    private void _postCheckVerifyCode(String type,String areaCode,String phoneNumber,String email,String verifyCode,GenericsCallback<CheckVerificationBean> callback)throws IOException{
        final String url = this.url+"user/checkVerifyCode";
        OkHttpUtils
                .jpost()
                .url(url)
                .addHeader("project",BuildConfig.APP_NAME)
                .addHeader("Accept-Language",language)
                .addParams("type",type)
                .addParams("areaCode",areaCode)
                .addParams("phoneNumber",phoneNumber)
                .addParams("email",email)
                .addParams("verifyCode",verifyCode)
                .build()
                .execute(callback,new TokenInterceptor());
    }

    /**
     * 登录接口
     * @param type                   注册类型1：手机注册 2：邮箱注册
     * @param areaCode               区号
     * @param phoneNumber            手机
     * @param email                  邮箱
     * @param password               密码
     * */
    private void _postLogin(String type,String areaCode, String phoneNumber, String email, String password,String phoneId,String phoneSystem,GenericsCallback<LoginBean> callback)throws IOException{
        String url = this.url+"v2/user/login";
        OkHttpUtils
                .jpost()
                .url(url)
                .addHeader("project",BuildConfig.APP_NAME)
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
                .execute(callback,new TokenInterceptor());
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
                                     String password,GenericsCallback<BaseApi> callback)throws IOException{
        String url = this.url+"user/retrievePassword";
        OkHttpUtils
                .jpost()
                .url(url)
                .addHeader("project",BuildConfig.APP_NAME)
                .addHeader("Time-Diff",time)
                .addHeader("Accept-Language",language)
                .addParams("type",type)
                .addParams("areaCode",areaCode)
                .addParams("phoneNumber",phoneNumber)
                .addParams("email",email)
                .addParams("verifyCode",verifyCode)
                .addParams("password",password)
                .build()
                .execute(callback,new TokenInterceptor());
    }

    /**
     * 发送意见反馈接口
     * */
    private void _postSendFeedback(String content,GenericsCallback<BaseApi> callback)throws IOException{
        String url = this.url+"user/uploadFeedback";
        OkHttpUtils
                .jpost()
                .url(url)
                .addHeader("project",BuildConfig.APP_NAME)
                .addHeader("Time-Diff",time)
                .addHeader("token",token)
                .addHeader("Accept-Language",language)
                .addParams("content",content)
                .build()
                .execute(callback,new TokenInterceptor());
    }

    /**
     * 获取个人信息
     * */
    private void _getForGetInfo(GenericsCallback<UserInfoBean> callback)throws IOException{
        String url = this.url+"v2/user/getUserInfo";
        OkHttpUtils
                .get()
                .url(url)
                .addHeader("project",BuildConfig.APP_NAME)
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
                .addHeader("project",BuildConfig.APP_NAME)
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
                .addHeader("project",BuildConfig.APP_NAME)
                .id(id)
                .addHeader("Time-Diff",time)
                .addHeader("token",token)
                .addHeader("Accept-Language",language)
                .addParams("stepsPlan",stepsPlan)
                .build()
                .execute(callback,new TokenInterceptor());
    }

    private void _postForSetSleepPlan(String sleepPlan,int id,GenericsCallback<BaseApi> callback)throws IOException{
        String url = this.url+"user/updateSleepPlan";
        OkHttpUtils
                .jpost()
                .url(url)
                .addHeader("project",BuildConfig.APP_NAME)
                .id(id)
                .addHeader("Time-Diff",time)
                .addHeader("token",token)
                .addHeader("Accept-Language",language)
                .addParams("sleepPlan",sleepPlan)
                .build()
                .execute(callback,new TokenInterceptor());
    }

    private void _postForSetUnit(String unit,String temp,GenericsCallback<BaseApi>callback)throws IOException{
        String url = this.url+"v2/user/setUnit";
        OkHttpUtils
                .jpost()
                .url(url)
                .addHeader("project",BuildConfig.APP_NAME)
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
                .addHeader("project",BuildConfig.APP_NAME)
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
    private void _postUploadAvatar(File avatar, GenericsCallback<AvatarBean> callback)throws IOException{
        String url = this.url+"user/setProfilePicture";
        OkHttpUtils
                .fpost()
                .url(url)
                .addHeader("project",BuildConfig.APP_NAME)
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
                .addHeader("project",BuildConfig.APP_NAME)
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
                .addHeader("project",BuildConfig.APP_NAME)
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
                .addHeader("project",BuildConfig.APP_NAME)
                .addHeader("Time-Diff",time)
                .addHeader("token",token)
                .addHeader("Accept-Language",language)
                .build()
                .execute(baseApiGenericsCallback,new TokenInterceptor());
    }


    /**
     * 上传log数据
     * */
    private void _postAppCrashLog(String appName,String appVersion,String systemInfo,String stackTrace,GenericsCallback<BaseApi> callback)throws IOException{
        String url = this.url+"appCrashLog/upload";
        OkHttpUtils
                .jpost()
                .url(url)
                .addHeader("project",BuildConfig.APP_NAME)
                .id(UPDOWN_LOG)
                .addParams("appName",appName)
                .addParams("appVersion",appVersion)
                .addParams("systemInfo",systemInfo)
                .addParams("stackTrace",stackTrace)
                .build()
                .execute(callback,new TokenInterceptor());
    }

    /**
     * 上传数据到服务器
     * */
    private void _postForUpdownReportData(String data,GenericsCallback<BaseApi> callback)throws IOException{
        String url = this.url+"data/upload";
        OkHttpUtils
                .listpost()
                .url(url)
                .addHeader("project",BuildConfig.APP_NAME)
                .id(UPDOWN_DATA)
                .addHeader("Time-Diff",time)
                .addHeader("token",token)
                .addHeader("Accept-Language",language)
                .addParams("data",data)
                .build()
                .execute(callback,new TokenInterceptor());
    }

    /**
     * 获取服务器上的数据
     * */
    private void _getForDownloadReportData(String time, String size)throws IOException{
        String url = this.url+"data/get";
        OkHttpUtils
                .get()
                .url(url)
                .addHeader("project",BuildConfig.APP_NAME)
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
                .addHeader("project",BuildConfig.APP_NAME)
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
                .addHeader("project",BuildConfig.APP_NAME)
                .addHeader("Time-Diff",time)
                .addHeader("Accept-Language",language)
                .addHeader("token",token)
                .addParams("pageNum","1")
                .addParams("pageSize","30")
                .addParams("watchPlateGroupId",watchPlateGroupId)
                .build()
                .execute(callback,new TokenInterceptor());
    }

    private void _getFaqList(GenericsCallback<FaqListBean> callback)throws IOException{
        String url = this.url+"comm/getQuestionAndAnswers";
        OkHttpUtils
                .get()
                .url(url)
                .addHeader("project",BuildConfig.APP_NAME)
                .addHeader("Time-Diff",time)
                .addHeader("Accept-Language",language)
                .addParams("pageNum","1")
                .addParams("pageSize","30")
                .build()
                .execute(callback,new TokenInterceptor());
    }

    private void _getFaqContent(String id,GenericsCallback<FaqBean> callback)throws IOException{
        String url = this.url+"comm/getQuestionAndAnswerDetail";
        OkHttpUtils
                .get()
                .url(url)
                .addHeader("project",BuildConfig.APP_NAME)
                .addHeader("Time-Diff",time)
                .addHeader("Accept-Language",language)
                .addParams("id",id)
                .build()
                .execute(callback,new TokenInterceptor());
    }

    public void getFaq(String id,GenericsCallback<FaqBean> callback)throws IOException{
        _getFaqContent(id,callback);
    }

    public void getFaqList(GenericsCallback<FaqListBean> callback)throws IOException{
        _getFaqList(callback);
    }

    public void getDeviceConfig(GenericsCallback<DeviceConfigBean> callback)throws IOException{
        _getDeviceConfig(callback);
    }

    public void postAppCrashLog(String appName,String appVersion,String systemInfo,String stackTrace,GenericsCallback<BaseApi> callback)throws IOException{
        _postAppCrashLog(appName,appVersion,systemInfo,stackTrace,callback);
    }


    /**
     * 提供给用户的方法
     * */
    public void postRegister(String type,String areaCode,String phoneNumber,String email,String verifyCode,String password,
                             String phoneId,String phoneSystem,GenericsCallback<BaseApi> callback) throws IOException{
        _postRegister(type,areaCode,phoneNumber,email,verifyCode,password, phoneId, phoneSystem,callback);
    }

    public void getVerificationCode(String type,String areaCode,String phoneNumber,String email,GenericsCallback<BaseApi> callback)throws IOException{

        _getVerificationCode(type,areaCode,phoneNumber,email,callback);
    }

    public void postCheckVerifyCode(String type,String areaCode,String phoneNumber,String email,String verifyCode,GenericsCallback<CheckVerificationBean> callback)throws IOException{
        _postCheckVerifyCode(type,areaCode,phoneNumber,email,verifyCode,callback);
    }

    public void postLogin(String type, String areaCode,String phoneNumber, String email, String password,String phoneId,String phoneSystem
            ,GenericsCallback<LoginBean> callback)throws IOException{
        _postLogin(type,areaCode,phoneNumber,email,password,phoneId,phoneSystem,callback);
    }

    public void postForgotPassword(String type,String areaCode,String phoneNumber,String email,String verifyCode,
                                   String password,GenericsCallback<BaseApi> callback)throws IOException{
        _postForgotPassword(type,areaCode,phoneNumber,email,verifyCode,password,callback);
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

    public void postForSetStepsPlan(String stepsPlan,int id,GenericsCallback<BaseApi> callback)throws IOException{
        _postForSetStepsPlan(stepsPlan,id,callback);
    }

    public void postForSetStepsPlan(String stepsPlan,int id)throws IOException{
        _postForSetStepsPlan(stepsPlan,id,baseApiGenericsCallback);
    }

    public void postForSetSleepPlan(String sleepPlan,int id,GenericsCallback<BaseApi> callback)throws IOException{
        _postForSetSleepPlan(sleepPlan,id,callback);
    }

    public void postForSetUnit(String unit,String temp,GenericsCallback<BaseApi> callback)throws IOException{
        _postForSetUnit(unit,temp,callback);
    }

    public void getBindDevice(String deviceCode,String product,GenericsCallback<BindBean> callback)throws IOException{
        _getBindDevice(deviceCode,product,callback);
    }

    public void getUnbindDevice()throws IOException{
        _getUnbindDevice();
    }

    public void postForUpdownReportData(String data)throws IOException{
        _postForUpdownReportData(data,baseApiGenericsCallback);
    }

    public void getForDownloadReportData(String time, String size)throws IOException{
        _getForDownloadReportData(time,size);
    }

    public void postUpdownAvatar(File avatar,GenericsCallback<AvatarBean> callback)throws IOException{
        _postUploadAvatar(avatar,callback);
    }

    public void postSendFeedback(String content,GenericsCallback<BaseApi> callback)throws IOException{
        _postSendFeedback(content,callback);
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
                if (id == UPDOWN_DATA)
                    MathUitl.saveLastTime(mContext.getSharedPreferences(FILE,MODE_PRIVATE));
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


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MathUitl.saveLastTime(mContext.getSharedPreferences(FILE,MODE_PRIVATE));
                    }
                },10000);

            }else {
                ProgressHudModel.newInstance().diss();
                MathUitl.showToast(mContext,response.getMessage());
            }
        }
    };



}
