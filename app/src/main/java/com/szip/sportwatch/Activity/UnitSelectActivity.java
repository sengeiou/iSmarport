package com.szip.sportwatch.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import com.szip.sportwatch.BLE.BleClient;
import com.szip.sportwatch.Model.HttpBean.BaseApi;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Service.MainService;
import com.szip.sportwatch.Util.HttpMessgeUtil;
import com.szip.sportwatch.Util.JsonGenericsSerializator;
import com.szip.sportwatch.Util.LogUtil;
import com.szip.sportwatch.Util.MathUitl;
import com.szip.sportwatch.Util.ProgressHudModel;
import com.szip.sportwatch.Util.StatusBarCompat;
import com.szip.sportwatch.BLE.EXCDController;
import com.zhy.http.okhttp.callback.GenericsCallback;

import java.io.IOException;

import okhttp3.Call;

import static com.szip.sportwatch.Util.HttpMessgeUtil.UPDATA_USERINFO;

public class UnitSelectActivity extends BaseActivity implements View.OnClickListener{

    private MyApplication app;
    private RadioGroup unitRg,tempRg;

    private int unit;//单位制式
    private int temp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_unit_select);
        app = (MyApplication) getApplicationContext();
        initView();
        initEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initView() {
        StatusBarCompat.translucentStatusBar(UnitSelectActivity.this,true);
        setAndroidNativeLightStatusBar(this,true);
        setTitleText(getString(R.string.unit));
        unitRg = findViewById(R.id.unitRg);
        tempRg = findViewById(R.id.tempRg);
        unit = app.getUserInfo().getUnit();
        if (app.getUserInfo().getUnit()==0){
            unitRg.check(R.id.metricRb);
        }else {
            unitRg.check(R.id.britishRb);
        }

        temp = app.getUserInfo().getTempUnit();
        if (app.getUserInfo().getTempUnit()==0){
            tempRg.check(R.id.cRb);
        }else {
            tempRg.check(R.id.fRb);
        }
    }

    private void initEvent() {
        findViewById(R.id.backIv).setOnClickListener(this);
        findViewById(R.id.rightIv).setOnClickListener(this);
        findViewById(R.id.metricTv).setOnClickListener(this);
        findViewById(R.id.britishTv).setOnClickListener(this);
        findViewById(R.id.metricRb).setOnClickListener(this);
        findViewById(R.id.britishRb).setOnClickListener(this);
        findViewById(R.id.cTv).setOnClickListener(this);
        findViewById(R.id.cRb).setOnClickListener(this);
        findViewById(R.id.fTv).setOnClickListener(this);
        findViewById(R.id.fRb).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.backIv:
                finish();
                break;
            case R.id.metricTv:
            case R.id.metricRb:
                unitRg.check(R.id.metricRb);
                unit = 0;
                break;
            case R.id.britishTv:
            case R.id.britishRb:
                unitRg.check(R.id.britishRb);
                unit = 1;
                break;
            case R.id.cTv:
            case R.id.cRb:
                tempRg.check(R.id.cRb);
                temp = 0;
                break;
            case R.id.fTv:
            case R.id.fRb:
                tempRg.check(R.id.fRb);
                temp = 1;
                break;
            case R.id.rightIv:
                LogUtil.getInstance().logd("DATA******","UNIT = "+unit+" ;unit = "+app.getUserInfo().getUnit());
                if (unit==app.getUserInfo().getUnit() && temp == app.getUserInfo().getTempUnit()){
                    showToast(getString(R.string.saved));
                    finish();
                }else {//如果制式发生变化，则清空原来的数据
                    ProgressHudModel.newInstance().show(UnitSelectActivity.this,getString(R.string.waitting),getString(R.string.httpError),
                            3000);
                    try {
                        HttpMessgeUtil.getInstance().postForSetUnit(unit+"",temp+"",callback);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }


    private GenericsCallback<BaseApi> callback = new GenericsCallback<BaseApi>(new JsonGenericsSerializator()) {
        @Override
        public void onError(Call call, Exception e, int id) {

        }

        @Override
        public void onResponse(BaseApi response, int id) {
            ProgressHudModel.newInstance().diss();
            if (response.getCode()==200){
                if (id==UPDATA_USERINFO)
                    finish();
                else {
                    showToast(getString(R.string.saved));
                    app.getUserInfo().setUnit(unit);
                    app.getUserInfo().setTempUnit(temp);
                    MathUitl.saveIntData(UnitSelectActivity.this,"unit1",unit).commit();
                    MathUitl.saveIntData(UnitSelectActivity.this,"temp",temp).commit();
                    if (MainService.getInstance().getState()!=3){
                        showToast(getString(R.string.syceError));
                    }else {
                        if(app.isMtk())
                            EXCDController.getInstance().writeForSetUnit(app.getUserInfo());
                        else
                            BleClient.getInstance().writeForSetUnit();
                    }
                }
            }else {
                showToast(response.getMessage());
            }

        }
    };

}
