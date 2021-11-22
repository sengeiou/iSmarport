package com.szip.sportwatch.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.szip.sportwatch.Interface.HttpCallbackWithBase;
import com.szip.sportwatch.Model.HttpBean.BaseApi;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.HttpMessgeUtil;
import com.szip.sportwatch.Util.JsonGenericsSerializator;
import com.szip.sportwatch.Util.ProgressHudModel;
import com.szip.sportwatch.Util.StatusBarCompat;
import com.zaaach.citypicker.CityPicker;
import com.zaaach.citypicker.adapter.OnPickListener;
import com.zaaach.citypicker.model.City;
import com.zhy.http.okhttp.callback.GenericsCallback;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;

import static com.szip.sportwatch.MyApplication.FILE;
import static com.szip.sportwatch.Util.MathUitl.isEmail;

public class ForgetPasswordActivity extends BaseActivity implements View.OnClickListener{

    /**
     * 国家与地区以及国家代码
     * */
    private TextView countryTv,countryCodeTv,countryTipTv;
    /**
     * 用户名以及密码
     * */
    private EditText userEt,passwordEt;
    private TextView userTipTv,passwordTipTv;
    /**
     * 验证码相关控件
     * */
    private EditText verifyCodeEt;
    private TextView sendTv,verifyCodeTipTv;
    private Timer timer;
    private int time;


    private int flagForEt;


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 100:
                    String getCodeAgain = getString(R.string.send);
                    time--;
                    if (time <= 0){
                        timer.cancel();
                        sendTv.setEnabled(true);
                        sendTv.setText(getCodeAgain);
                        sendTv.setTextColor(getResources().getColor(R.color.rayblue));
                    }else {
                        sendTv.setText(time+"s");
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_forget_password);
        initView();
        initEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    /**
     * 初始化视图
     * */
    private void initView() {
        StatusBarCompat.translucentStatusBar(ForgetPasswordActivity.this,true);
        setAndroidNativeLightStatusBar(this,true);
        countryTv = findViewById(R.id.countryTv);
        countryCodeTv = findViewById(R.id.countryCodeTv);
        countryTipTv = findViewById(R.id.countryTipTv);

        userEt = findViewById(R.id.userEt);
        userTipTv = findViewById(R.id.userTipTv);

        passwordEt = findViewById(R.id.passwordEt);
        passwordTipTv = findViewById(R.id.passwordTipTv);

        verifyCodeEt = findViewById(R.id.verifyCodeEt);
        verifyCodeTipTv = findViewById(R.id.verifyCodeTipTv);
        sendTv = findViewById(R.id.sendTv);

        SharedPreferences sharedPreferencesp = getSharedPreferences(FILE,MODE_PRIVATE);
        countryTv.setText(sharedPreferencesp.getString("countryName",""));
        countryCodeTv.setText(sharedPreferencesp.getString("countryCode",""));
    }

    /**
     * 初始化事件
     * */
    private void initEvent() {
        sendTv.setOnClickListener(this);
        findViewById(R.id.countryRl).setOnClickListener(this);
        findViewById(R.id.resetBtn).setOnClickListener(this);
        findViewById(R.id.backIv).setOnClickListener(this);
        userEt.addTextChangedListener(watcher);
        userEt.setOnFocusChangeListener(focusChangeListener);
        passwordEt.addTextChangedListener(watcher);
        passwordEt.setOnFocusChangeListener(focusChangeListener);
        verifyCodeEt.addTextChangedListener(watcher);
        verifyCodeEt.setOnFocusChangeListener(focusChangeListener);
        ((CheckBox)findViewById(R.id.lawsCb)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String psd = passwordEt.getText().toString();
                if (isChecked){
                    passwordEt.setInputType(0x90);
                }else {
                    passwordEt.setInputType(0x81);
                }
                passwordEt.setSelection(psd.length());
            }
        });
    }

    /**
     * 开始倒计时
     * */
    private void startTimer(){
        try {
            if (userEt.getText().toString().contains("@"))
                HttpMessgeUtil.getInstance().getVerificationCode("2","","",
                        userEt.getText().toString(),callback);
            else
                HttpMessgeUtil.getInstance().getVerificationCode("1","00"+countryCodeTv.getText().toString().substring(1),
                        userEt.getText().toString(),"",callback);

        } catch (IOException e) {
            e.printStackTrace();
        }
        sendTv.setTextColor(getResources().getColor(R.color.gray));
        sendTv.setEnabled(false);
        time = 60;
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(100);
            }
        };
        timer = new Timer();
        timer.schedule(timerTask,1000,1000);
    }

    /**
     * 监听点击事件
     * */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.countryRl:
                CityPicker.getInstance()
                        .setFragmentManager(getSupportFragmentManager())
                        .enableAnimation(true)
                        .setAnimationStyle(R.style.CustomAnim)
                        .setLocatedCity(null)
                        .setHotCities(null)
                        .setOnPickListener(new OnPickListener() {
                            @Override
                            public void onPick(int position, City data) {
                                countryTv.setText(data == null ? "" :  data.getName());
                                countryCodeTv.setText("+"+data.getCode().substring(2));
                                countryTv.setTextColor(getResources().getColor(R.color.rayblue));
                                countryCodeTv.setTextColor(getResources().getColor(R.color.rayblue));
                                countryTipTv.setTextColor(getResources().getColor(R.color.gray));
                                SharedPreferences sharedPreferencesp = getSharedPreferences(FILE,MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferencesp.edit();
                                editor.putString("countryName",countryTv.getText().toString());
                                editor.putString("countryCode",countryCodeTv.getText().toString());
                                editor.commit();
                            }
                            @Override
                            public void onLocate() {

                            }
                        })
                        .show();
                break;
            case R.id.sendTv:
                if (countryCodeTv.getText().toString().equals("")){
                    showToast(getString(R.string.choseCountry));
                }else if (userEt.getText().toString().equals("")){
                    showToast(getString(R.string.phoneOrEmail));
                }else {
                    if (userEt.getText().toString().contains("@")){
                        if (!isEmail(userEt.getText().toString()))
                            showToast(getString(R.string.enterRightEmail));
                        else
                            startTimer();
                    } else
                        startTimer();
                }
                break;
            case R.id.resetBtn:
                if (countryCodeTv.getText().toString().equals("")) {
                    showToast(getString(R.string.choseCountry));
                }else if (userEt.getText().toString().equals("")){
                    showToast(getString(R.string.phoneOrEmail));
                }else if (verifyCodeEt.getText().toString().equals("")){
                    showToast(getString(R.string.enterVerification));
                }else if (passwordEt.getText().toString().equals("")){
                    showToast(getString(R.string.enterPassword));
                }else {
                    try {
                        if (userEt.getText().toString().contains("@")) {//邮箱
                            if (isEmail(userEt.getText().toString())){
                                ProgressHudModel.newInstance().show(ForgetPasswordActivity.this,getString(R.string.waitting)
                                        ,getString(R.string.httpError),10000);
                                HttpMessgeUtil.getInstance().postForgotPassword("2","",""
                                        ,userEt.getText().toString(), verifyCodeEt.getText().toString(),passwordEt.getText().toString(),callback);
                            }else
                                showToast(getString(R.string.enterRightEmail));

                        } else {//手机
                            ProgressHudModel.newInstance().show(ForgetPasswordActivity.this,getString(R.string.waitting)
                                    ,getString(R.string.httpError),10000);
                            HttpMessgeUtil.getInstance().postForgotPassword("1","00"+countryCodeTv.getText().toString().substring(1),
                                    userEt.getText().toString(),"", verifyCodeEt.getText().toString(),passwordEt.getText().toString(),callback);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.backIv:
                finish();
                break;
        }
    }

    /**
     * 输入框键入监听器
     * */
    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String data = s.toString();

            switch (flagForEt){
                case 0:
                    if (TextUtils.isEmpty(data)){
                        userTipTv.setTextColor(getResources().getColor(R.color.rayblue));
                    }else {
                        userTipTv.setTextColor(getResources().getColor(R.color.gray));
                    }
                    break;
                case 1:
                    if (TextUtils.isEmpty(data)){
                        passwordTipTv.setTextColor(getResources().getColor(R.color.rayblue));
                    }else {
                        passwordTipTv.setTextColor(getResources().getColor(R.color.gray));
                    }
                    break;
                case 2:
                    if (TextUtils.isEmpty(data)){
                        verifyCodeTipTv.setTextColor(getResources().getColor(R.color.rayblue));
                    }else {
                        verifyCodeTipTv.setTextColor(getResources().getColor(R.color.gray));
                    }
                    break;
            }
        }
    };

    /**
     * 输入框焦点监听
     * */
    private View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            switch (v.getId()){
                case R.id.userEt:
                    if (hasFocus){
                        flagForEt = 0;
                    }
                    break;
                case R.id.passwordEt:
                    if (hasFocus){
                        flagForEt = 1;
                    }
                    break;
                case R.id.verifyCodeEt:
                    if (hasFocus){
                        flagForEt = 2;
                    }
                    break;
            }
        }
    };

    private GenericsCallback<BaseApi> callback = new GenericsCallback<BaseApi>(new JsonGenericsSerializator()) {
        @Override
        public void onError(Call call, Exception e, int id) {

        }

        @Override
        public void onResponse(BaseApi response, int id) {
            ProgressHudModel.newInstance().diss();
            showToast(getString(R.string.resetSuccess));
            finish();
        }
    };

}
