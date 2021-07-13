package com.szip.sportwatch.Activity.welcome;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.mediatek.leprofiles.LocalBluetoothLEManager;
import com.mediatek.wearable.WearableManager;
import com.szip.sportwatch.DB.SaveDataUtil;
import com.szip.sportwatch.DB.dbModel.HealthyConfig;
import com.szip.sportwatch.DB.dbModel.SportWatchAppFunctionConfigDTO;
import com.szip.sportwatch.Model.HttpBean.DeviceConfigBean;
import com.szip.sportwatch.Model.HttpBean.UserInfoBean;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Service.MainService;
import com.szip.sportwatch.Util.HttpMessgeUtil;
import com.szip.sportwatch.Util.JsonGenericsSerializator;
import com.szip.sportwatch.Util.LogUtil;
import com.szip.sportwatch.Util.MathUitl;
import com.szip.sportwatch.View.MyAlerDialog;
import com.zhy.http.okhttp.callback.GenericsCallback;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;

import static android.content.Context.MODE_PRIVATE;
import static com.szip.sportwatch.MyApplication.FILE;
import static com.szip.sportwatch.MyApplication.getInstance;

public class WelcomePresenterImpl implements IWelcomePresenter{

    private IWelcomeView iWelcomeView;
    private Handler handler;

    public WelcomePresenterImpl(IWelcomeView iWelcomeView) {
        this.iWelcomeView = iWelcomeView;
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void checkPrivacy(Context context) {

        final SharedPreferences sharedPreferences = context.getSharedPreferences(FILE,MODE_PRIVATE);
        if (sharedPreferences.getBoolean("isFirst",true)){
            MyAlerDialog.getSingle().showAlerDialogWithPrivacy(context.getString(R.string.privacy1), context.getString(R.string.privacyTip),
                    null, null, false, new MyAlerDialog.AlerDialogOnclickListener() {
                        @Override
                        public void onDialogTouch(boolean flag) {
                            if (flag){
                                sharedPreferences.edit().putBoolean("isFirst",false).commit();
                                if (iWelcomeView!=null)
                                    iWelcomeView.checkPrivacyResult(true);
                            }else{
                                if (iWelcomeView!=null)
                                    iWelcomeView.checkPrivacyResult(false);
                            }
                        }
                    },context);
        }else {
            if (iWelcomeView!=null)
                iWelcomeView.checkPrivacyResult(true);
        }

    }

    @Override
    public void initBle(Context context) {
        LocalBluetoothLEManager.getInstance().init(context, 511);
        boolean isSuccess = WearableManager.getInstance().init(true, context, "we had", R.xml.wearable_config);
        //切换成GATT模式
        if (WearableManager.getInstance().getWorkingMode() == WearableManager.MODE_SPP)
            WearableManager.getInstance().switchMode();
        if (!MainService.isMainServiceActive()) {
            context.startService(
                    new Intent(context, MainService.class));
        }
        if (iWelcomeView!=null)
            iWelcomeView.initBleFinish();
        LogUtil.getInstance().logd("SZIP******","初始化蓝牙");
    }

    @Override
    public void initDeviceConfig() {
        try {
            HttpMessgeUtil.getInstance().getDeviceConfig(new GenericsCallback<DeviceConfigBean>(new JsonGenericsSerializator()) {
                @Override
                public void onError(Call call, Exception e, int id) {
                    if (iWelcomeView!=null)
                        iWelcomeView.initDeviceConfigFinish();
                }

                @Override
                public void onResponse(DeviceConfigBean response, int id) {
                    if (response.getCode()==200){
                        ArrayList<HealthyConfig> data = new ArrayList<>();
                        SaveDataUtil.newInstance().saveConfigListData(response.getData());
                        for (SportWatchAppFunctionConfigDTO configDTO:response.getData()){
                            configDTO.getHealthMonitorConfig().identifier = configDTO.identifier;
                            data.add(configDTO.getHealthMonitorConfig());
                        }
                        SaveDataUtil.newInstance().saveHealthyConfigListData(data);
                        if (iWelcomeView!=null)
                            iWelcomeView.initDeviceConfigFinish();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initUserInfo(final Context context) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(FILE,MODE_PRIVATE);
        //判断登录状态
        String token = sharedPreferences.getString("token",null);
        if (token!=null){//已登录
            HttpMessgeUtil.getInstance().setToken(token);
            try {
                HttpMessgeUtil.getInstance().getForGetInfo(new GenericsCallback<UserInfoBean>(new JsonGenericsSerializator()) {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        getInstance().setUserInfo(MathUitl.loadInfoData(sharedPreferences));
                        if (iWelcomeView!=null)
                            iWelcomeView.initUserinfoFinish(false);
                    }

                    @Override
                    public void onResponse(UserInfoBean response, int id) {
                        if (response.getCode() == 200){
                            getInstance().setUserInfo(response.getData());
                            if (response.getData().getDeviceCode()!=null&&!response.getData().getDeviceCode().equals("")){
                                if (MyApplication.getInstance().getDialGroupId().equals("0")){
                                    BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                                    BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
                                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(response.getData().getDeviceCode());
                                    if (device!=null&&device.getName()!=null)
                                        MyApplication.getInstance().setDeviceConfig(device.getName().indexOf("_LE")>=0?
                                                device.getName().substring(0,device.getName().length()-3):
                                                device.getName());
                                }
                            }
                            if (iWelcomeView!=null)
                                iWelcomeView.initUserinfoFinish(false);
                        }else if (response.getCode() == 401){
                            sharedPreferences.edit().putString("token",null).commit();
                            MathUitl.showToast(context,context.getString(R.string.tokenTimeOut));
                            if (iWelcomeView!=null)
                                iWelcomeView.initUserinfoFinish(true);
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                getInstance().setUserInfo(MathUitl.loadInfoData(sharedPreferences));
                if (iWelcomeView!=null)
                    iWelcomeView.initUserinfoFinish(false);
            }
        }else {
            if (iWelcomeView!=null)
                iWelcomeView.initUserinfoFinish(true);
        }
    }
}
