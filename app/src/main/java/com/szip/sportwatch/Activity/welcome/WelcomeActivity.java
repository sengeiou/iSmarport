package com.szip.sportwatch.Activity.welcome;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.szip.sportwatch.Activity.BaseActivity;
import com.szip.sportwatch.Activity.LoginActivity;
import com.szip.sportwatch.Activity.main.MainActivity;
import com.szip.sportwatch.R;

public class WelcomeActivity extends BaseActivity implements IWelcomeView{

    /**
     * 延时线程
     * */
    private int sportWatchCode = 100;

    private boolean isConfig = false;
    private boolean isInitInfo = false;
    private boolean isInitBle = false;
    private boolean isNeedLogin = false;
    private IWelcomePresenter welcomePresenter;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);
        mContext = this;
        welcomePresenter = new WelcomePresenterImpl(this);
        welcomePresenter.checkPrivacy(this);
    }

    /**
     * 获取权限
     * */
    private void checkPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED

                    ||checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS
                                ,Manifest.permission.READ_SMS,Manifest.permission.SEND_SMS
//                                , Manifest.permission.ACCESS_FINE_LOCATION
                                , Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        sportWatchCode);
            }else {
                    welcomePresenter.initBle(getApplicationContext());
                    welcomePresenter.initDeviceConfig();
                    welcomePresenter.initUserInfo(getApplicationContext());
            }
        }else {
                welcomePresenter.initBle(getApplicationContext());
                welcomePresenter.initDeviceConfig();
                welcomePresenter.initUserInfo(getApplicationContext());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100){
            int code = grantResults[0];
            int code1 = grantResults[1];
            int code2= grantResults[2];
            int code3= grantResults[3];
            int code4= grantResults[4];
//            int code5= grantResults[5];
            if (code == PackageManager.PERMISSION_GRANTED&&code1 == PackageManager.PERMISSION_GRANTED
                    &&code2 == PackageManager.PERMISSION_GRANTED&&code3 == PackageManager.PERMISSION_GRANTED
                    &&code4 == PackageManager.PERMISSION_GRANTED
//                    &&code5 == PackageManager.PERMISSION_GRANTED
            ){
                    welcomePresenter.initBle(getApplicationContext());
                    welcomePresenter.initDeviceConfig();
                    welcomePresenter.initUserInfo(getApplicationContext());
            }else {
                WelcomeActivity.this.finish();
            }
        }
    }

    @Override
    public void checkPrivacyResult(boolean comfirm) {
        if (comfirm){//隐私协议通过
            checkPermission();
        }else {
            finish();
        }
    }


    @Override
    public void initDeviceConfigFinish() {
        isConfig = true;
        if (isInitInfo&&isInitBle){
            if (isNeedLogin){
                startActivity(new Intent(mContext, LoginActivity.class));
            }else {
                startActivity(new Intent(mContext, MainActivity.class));
            }
            finish();
        }
    }

    @Override
    public void initBleFinish() {
        isInitBle = true;
        if (isInitInfo&&isConfig){
            if (isNeedLogin){
                startActivity(new Intent(mContext, LoginActivity.class));
            }else {
                startActivity(new Intent(mContext, MainActivity.class));
            }
            finish();
        }
    }

    @Override
    public void initUserinfoFinish(boolean isNeedLogin) {
        isInitInfo = true;
        this.isNeedLogin = isNeedLogin;
        if (isInitBle&&isConfig){
            if (isNeedLogin){
                startActivity(new Intent(mContext, LoginActivity.class));
            }else {
                startActivity(new Intent(mContext, MainActivity.class));
            }
            finish();
        }
    }
}
