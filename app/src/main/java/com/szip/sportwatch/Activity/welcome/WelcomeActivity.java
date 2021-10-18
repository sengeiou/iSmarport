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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        welcomePresenter.setViewDestory();
    }

    @Override
    public void checkPrivacyResult(boolean comfirm) {
        if (comfirm){//隐私协议通过
            welcomePresenter.initBle(getApplicationContext());
            welcomePresenter.initDeviceConfig();
            welcomePresenter.initUserInfo(getApplicationContext());
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
