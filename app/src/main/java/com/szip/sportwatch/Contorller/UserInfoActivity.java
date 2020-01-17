package com.szip.sportwatch.Contorller;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.szip.sportwatch.Interface.HttpCallbackWithBase;
import com.szip.sportwatch.Model.HttpBean.BaseApi;
import com.szip.sportwatch.Model.UserInfo;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Service.MainService;
import com.szip.sportwatch.Util.DateUtil;
import com.szip.sportwatch.Util.HttpMessgeUtil;
import com.szip.sportwatch.Util.MathUitl;
import com.szip.sportwatch.Util.ProgressHudModel;
import com.szip.sportwatch.Util.StatusBarCompat;
import com.szip.sportwatch.View.CharacterPickerWindow;
import com.szip.sportwatch.View.MyAlerDialog;
import com.szip.sportwatch.View.character.OnOptionChangedListener;
import com.szip.sportwatch.BLE.EXCDController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserInfoActivity extends BaseActivity implements View.OnClickListener,HttpCallbackWithBase{

    /**
     * 头像、昵称、性别、身高、体重、生日
     * */
    private ImageView pictureIv;
    private TextView userNameTv;
    private TextView sexTv;
    private TextView heightTv;
    private TextView weightTv;
    private TextView birthdayTv;

    private Context mContext;

    private UserInfo userInfo;
    private MyApplication app;

    /**
     * 是否公制单位
     * */
    private boolean isMetric;


    /**
     * 数据选择框
     * */
    private CharacterPickerWindow window;
    private CharacterPickerWindow window1;
    private CharacterPickerWindow window2;
    private CharacterPickerWindow window3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super. onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_user_info);
        mContext = this;
        app = (MyApplication) getApplicationContext();
        initView();
        initEvent();
        initData();
        initWindow();
    }

    @Override
    protected void onResume() {
        super.onResume();
        HttpMessgeUtil.getInstance(this).setHttpCallbackWithBase(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HttpMessgeUtil.getInstance(this).setHttpCallbackWithBase(null);
    }

    /**
     * 初始化选择器
     * */
    private void initWindow() {


        //性别选择器
        window = new CharacterPickerWindow(UserInfoActivity.this,getString(R.string.height));
        final List<String> sexList =new ArrayList<>(Arrays.asList(getString(R.string.female),getString(R.string.male)));
        //初始化选项数据
        window.getPickerView().setPicker(sexList);
        //设置默认选中的三级项目
        window.setCurrentPositions(app.getUserInfo().getSex(), 0, 0);
        //监听确定选择按钮
        window.setOnoptionsSelectListener(new OnOptionChangedListener() {
            @Override
            public void onOptionChanged(int option1, int option2, int option3) {
                try {
                    sexTv.setText(sexList.get(option1));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        //身高选择器
        window1 = new CharacterPickerWindow(UserInfoActivity.this,getString(R.string.height));
        if (isMetric){
            final ArrayList<String> list2 = DateUtil.getStature();
            window1.getPickerView().setText("cm","");
            //初始化选项数据
            window1.getPickerView().setPicker(list2);
            //设置默认选中的三级项目
            window1.setCurrentPositions(list2.size()/2, 0, 0);
            //监听确定选择按钮
            window1.setOnoptionsSelectListener(new OnOptionChangedListener() {
                @Override
                public void onOptionChanged(int option1, int option2, int option3) {
                    try {
                        heightTv.setText(list2.get(option1)+"cm");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }else {
            final ArrayList<String> list2 = DateUtil.getStatureWithBritish();
            window1.getPickerView().setText("in","");
            //初始化选项数据
            window1.getPickerView().setPicker(list2);
            //设置默认选中的三级项目
            window1.setCurrentPositions(list2.size()/2, 0, 0);
            //监听确定选择按钮
            window1.setOnoptionsSelectListener(new OnOptionChangedListener() {
                @Override
                public void onOptionChanged(int option1, int option2, int option3) {
                    try {
                        heightTv.setText(list2.get(option1)+"in");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }


        //重量选择器
        window2 = new CharacterPickerWindow(UserInfoActivity.this,getString(R.string.weight));
        //初始化选项数据
        if (isMetric){
            window2.getPickerView().setText("kg","");
            final ArrayList<String> list3 = DateUtil.getWeight();
            window2.getPickerView().setPicker(list3);
            //设置默认选中的三级项目
            window2.setCurrentPositions(list3.size()/2, 0, 0);
            //监听确定选择按钮
            window2.setOnoptionsSelectListener(new OnOptionChangedListener() {
                @Override
                public void onOptionChanged(int option1, int option2, int option3) {
                    try {
                        weightTv.setText(list3.get(option1)+"kg");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }else {
            window2.getPickerView().setText("lb","");
            final ArrayList<String> list3 = DateUtil.getWeightWithBritish();
            window2.getPickerView().setPicker(list3);
            //设置默认选中的三级项目
            window2.setCurrentPositions(list3.size()/2, 0, 0);
            //监听确定选择按钮
            window2.setOnoptionsSelectListener(new OnOptionChangedListener() {
                @Override
                public void onOptionChanged(int option1, int option2, int option3) {
                    try {
                        weightTv.setText(list3.get(option1)+"lb");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }


        ArrayList<String> list1 = DateUtil.getYearList();
        //生日选择器
        window3 = new CharacterPickerWindow(UserInfoActivity.this,getString(R.string.birthday));
        //初始化选项数据
        window3.getPickerView().setPickerForDate(list1);
        //设置默认选中的三级项目
        window3.setCurrentPositions(list1.size()/2, 0, 0);
        //监听确定选择按钮
        window3.setOnoptionsSelectListener(new OnOptionChangedListener() {
            @Override
            public void onOptionChanged(int option1, int option2, int option3) {
                try {
                    birthdayTv.setText(String.format("%4d-%02d-%02d",(1930+option1),(option2+1),(option3+1)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * 初始化视图
     * */
    private void initView() {
        StatusBarCompat.translucentStatusBar(UserInfoActivity.this,true);
        ((TextView)findViewById(R.id.titleTv)).setText(getString(R.string.userInfo));
        pictureIv = findViewById(R.id.pictureIv);
        userNameTv = findViewById(R.id.userNameTv);
        sexTv = findViewById(R.id.sexTv);
        heightTv = findViewById(R.id.heightTv);
        weightTv = findViewById(R.id.weightTv);
        birthdayTv = findViewById(R.id.birthdayTv);
    }

    /**
     * 初始化事件监听
     * */
    private void initEvent() {
        findViewById(R.id.pictureRl).setOnClickListener(this);
        findViewById(R.id.userNameRl).setOnClickListener(this);
        findViewById(R.id.sexRl).setOnClickListener(this);
        findViewById(R.id.heightRl).setOnClickListener(this);
        findViewById(R.id.weightRl).setOnClickListener(this);
        findViewById(R.id.birthdayRl).setOnClickListener(this);
        findViewById(R.id.backIv).setOnClickListener(this);
        findViewById(R.id.rightIv).setOnClickListener(this);
    }

    /**
     * 初始化数据
     * */
    private void initData() {
        userInfo = app.getUserInfo();
        userNameTv.setText(userInfo.getUserName());
        sexTv.setText(userInfo.getSex()==1?getString(R.string.male):getString(R.string.female));
        isMetric = userInfo.getUnit().equals("metric");
        heightTv.setText(userInfo.getHeight());
        weightTv.setText(userInfo.getWeight());
        birthdayTv.setText(userInfo.getBirthday());
    }

    /**
     * 点击事件监听
     * */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.backIv:
                finish();
                break;
            case R.id.pictureRl:
                break;
            case R.id.userNameRl:
                MyAlerDialog.getSingle().showAlerDialogWithEdit(getString(R.string.userName), userInfo.getUserName(), getString(R.string.enterUserName),
                        null, null,false, new MyAlerDialog.AlerDialogEditOnclickListener() {
                            @Override
                            public void onDialogEditTouch(String edit1) {
                                userNameTv.setText(edit1);
                            }
                        },mContext);
                break;
            case R.id.sexRl:
                window.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                break;
            case R.id.heightRl:
                window1.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                break;
            case R.id.weightRl:
                window2.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                break;
            case R.id.birthdayRl:
                window3.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                break;
            case R.id.rightIv:
                ProgressHudModel.newInstance().show(mContext,getString(R.string.waitting),getString(R.string.httpError),
                        3000);
                try {
                    HttpMessgeUtil.getInstance(this).postForSetUserInfo(userNameTv.getText().toString(),
                            sexTv.getText().toString().equals(getString(R.string.male))?"1":"0", birthdayTv.getText().toString(),
                            heightTv.getText().toString(),weightTv.getText().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    /**
     * 上传用户信息接口返回的回调
     * */
    @Override
    public void onCallback(BaseApi baseApi, int id) {
        ProgressHudModel.newInstance().diss();
        showToast(getString(R.string.saved));
        app.getUserInfo().setUserName(userNameTv.getText().toString());
        app.getUserInfo().setSex(sexTv.getText().toString().equals(getString(R.string.male))?1:0);
        app.getUserInfo().setBirthday(birthdayTv.getText().toString());
        app.getUserInfo().setHeight(heightTv.getText().toString());
        app.getUserInfo().setWeight(weightTv.getText().toString());
        MathUitl.saveInfoData(mContext,app.getUserInfo()).commit();//退出之前更新本地缓存的userInfo
        if (MainService.getInstance().getConnectState()!=3){
            showToast(getString(R.string.syceError));
        }else {
            EXCDController.getInstance().writeForSetInfo(app.getUserInfo());
        }
        finish();
    }
}
