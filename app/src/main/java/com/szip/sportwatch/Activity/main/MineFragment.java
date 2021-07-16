package com.szip.sportwatch.Activity.main;

import android.Manifest;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mediatek.wearable.WearableManager;
import com.szip.sportwatch.Activity.help.FaqActivity;
import com.szip.sportwatch.BLE.BleClient;
import com.szip.sportwatch.Activity.AboutActivity;
import com.szip.sportwatch.Activity.help.BluetoochCallActivity;
import com.szip.sportwatch.Activity.LoginActivity;
import com.szip.sportwatch.Activity.NotificationAppListActivity;
import com.szip.sportwatch.Activity.SeachingActivity;
import com.szip.sportwatch.Activity.dial.SelectDialActivity;
import com.szip.sportwatch.Activity.UnitSelectActivity;
import com.szip.sportwatch.Activity.userInfo.UserInfoActivity;
import com.szip.sportwatch.DB.SaveDataUtil;
import com.szip.sportwatch.Fragment.BaseFragment;
import com.szip.sportwatch.Interface.HttpCallbackWithBase;
import com.szip.sportwatch.Model.EvenBusModel.ConnectState;
import com.szip.sportwatch.Model.EvenBusModel.PlanModel;
import com.szip.sportwatch.Model.HttpBean.BaseApi;
import com.szip.sportwatch.Model.UserInfo;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Service.MainService;
import com.szip.sportwatch.Util.HttpMessgeUtil;
import com.szip.sportwatch.Util.MathUitl;
import com.szip.sportwatch.Util.ProgressHudModel;
import com.szip.sportwatch.View.CharacterPickerWindow;
import com.szip.sportwatch.View.CircularImageView;
import com.szip.sportwatch.View.MyAlerDialog;
import com.szip.sportwatch.View.character.OnOptionChangedListener;
import com.szip.sportwatch.BLE.EXCDController;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import static android.content.Context.MODE_PRIVATE;
import static com.szip.sportwatch.MyApplication.FILE;

/**
 * Created by Administrator on 2019/12/1.
 */

public class MineFragment extends BaseFragment implements View.OnClickListener,HttpCallbackWithBase{
    private SharedPreferences sharedPreferencesp;
    private UserInfo userInfo;
    private MyApplication app;

    private CircularImageView pictureIv;
    private TextView userNameTv,stateTv,stepPlanTv,sleepPlanTv,deviceTv;

    private Switch blePhotoSwitch,heartSwitch;

    private CharacterPickerWindow window,window1;

    private int STEPFLAG = 1,SLEEPFLAG = 2;
    private int stepPlan = 0,sleepPlan = 0;
    private boolean unbind = false;
    private boolean isUpdatePlan = false;

    private View updateView;

    /**
     * 下拉菜单实例
     * */
    private PopupWindow mPop;

    /**
     * 选项列表
     * */
    private ListView MenuItem;
    private ArrayAdapter<String> ItemAdapter;
    private List<String> ItemValue;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_mine;
    }

    @Override
    protected void afterOnCreated(Bundle savedInstanceState) {
        app = (MyApplication) getActivity().getApplicationContext();
        userInfo = app.getUserInfo();
        initView();
        initEvent();
        initWindow();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        if (app.getUserInfo().getDeviceCode()==null){
            deviceTv.setText("");
            stateTv.setText(getString(R.string.addDevice));
        } else {
            if (MainService.getInstance().getState() == WearableManager.STATE_CONNECTED){
                deviceTv.setText(userInfo.getDeviceCode());
                stateTv.setText(getString(R.string.connected));
            }else if (MainService.getInstance().getState() == WearableManager.STATE_CONNECT_LOST||
                    MainService.getInstance().getState() == WearableManager.STATE_CONNECT_FAIL){
                stateTv.setText(getString(R.string.disConnect));
            }else if (MainService.getInstance().getState() == WearableManager.STATE_CONNECTING){
                deviceTv.setText("");
                stateTv.setText(getString(R.string.connectting));
            }else {
                deviceTv.setText("");
                stateTv.setText(getString(R.string.disConnect));
            }
        }
        initData();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        HttpMessgeUtil.getInstance().setHttpCallbackWithBase(null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBleConnectStateChange(ConnectState connectBean){
        if (connectBean.getState() == WearableManager.STATE_CONNECTED){
            deviceTv.setText(userInfo.getDeviceCode());
            stateTv.setText(getString(R.string.connected));
        }else if (connectBean.getState()  == WearableManager.STATE_CONNECT_LOST||
                connectBean.getState()  == WearableManager.STATE_CONNECT_FAIL){
            deviceTv.setText("");
            if (unbind){
                unbind = false;
                ProgressHudModel.newInstance().diss();
                app.getUserInfo().setDeviceCode(null);
                MainService.getInstance().stopConnect();
                MathUitl.saveInfoData(getContext(),app.getUserInfo()).commit();
                SaveDataUtil.newInstance().clearDB();
                getActivity().startActivity(new Intent(getActivity(),SeachingActivity.class));
            }else
                stateTv.setText(getString(R.string.disConnect));
        }else if (connectBean.getState()  == WearableManager.STATE_CONNECTING){
            deviceTv.setText("");
            stateTv.setText(getString(R.string.connectting));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdatePlan(PlanModel planModel){
       if(stepPlanTv!=null){
           HttpMessgeUtil.getInstance().setHttpCallbackWithBase(MineFragment.this);
           try {
               isUpdatePlan = false;
               HttpMessgeUtil.getInstance().postForSetStepsPlan(planModel.getData()+"",STEPFLAG);
               stepPlan = Integer.valueOf(planModel.getData());
           } catch (IOException e) {
           }

       }
    }

    /**
     * 初始化下拉菜单
     * */
    private void initSelectPopup() {
        if(MainService.getInstance().getState()== WearableManager.STATE_CONNECTED){
            ItemValue= new ArrayList<>(Arrays.asList(getString(R.string.unline),getString(R.string.unBind)));
        }else {
            ItemValue= new ArrayList<>(Arrays.asList(getString(R.string.line),getString(R.string.unBind)));
        }


        MenuItem = new ListView(getActivity());
        ItemAdapter = new ArrayAdapter<>(getActivity(),R.layout.popwindow_layout2,ItemValue);
        MenuItem.setAdapter(ItemAdapter);
        MenuItem.setBackgroundResource(R.color.space);

        MenuItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position==1){
                    try {
                        HttpMessgeUtil.getInstance().setHttpCallbackWithBase(MineFragment.this);
                        String datas = MathUitl.getStringWithJson(getActivity().getSharedPreferences(FILE,MODE_PRIVATE));
                        HttpMessgeUtil.getInstance().postForUpdownReportData(datas);
                        ProgressHudModel.newInstance().show(getContext(),getString(R.string.waitting),getString(R.string.httpError)
                                ,3000);
                        if (MainService.getInstance().getState()==WearableManager.STATE_CONNECTED){
                            unbind = true;
                            MainService.getInstance().stopConnect();
                        }else {
                            unbind = false;
                            ProgressHudModel.newInstance().diss();
                            app.getUserInfo().setDeviceCode(null);
                            MainService.getInstance().stopConnect();
                            SaveDataUtil.newInstance().clearDB();
                            getActivity().startActivity(new Intent(getActivity(),SeachingActivity.class));
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    unbind = false;
                    if (MainService.getInstance().getState()== WearableManager.STATE_CONNECTED){
                        MainService.getInstance().stopConnect();
                    }else {
                        MainService.getInstance().startConnect();
                    }
                }
                mPop.dismiss();
            }
        });


        WindowManager wm = (WindowManager)getActivity()
                .getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();

        mPop = new PopupWindow(MenuItem, width/2, ActionBar.LayoutParams.WRAP_CONTENT, true);

        Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.bg_corner);
        mPop.setBackgroundDrawable(drawable);
        mPop.setFocusable(true);
        mPop.setOutsideTouchable(true);
        mPop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                // 关闭popup窗口
                mPop.dismiss();
            }
        });
    }

    /**
     * 初始化选择器
     * */
    private void initWindow() {
        //步行计划选择器
        window = new CharacterPickerWindow(getContext(),getString(R.string.stepPlan));

        final List<String> stepList = MathUitl.getStepPlanList();
        //初始化选项数据
        window.getPickerView().setPicker(stepList);
        //设置默认选中的三级项目
        window.setCurrentPositions(stepList.size()/2, 0, 0);
        //监听确定选择按钮
        window.setOnoptionsSelectListener(new OnOptionChangedListener() {
            @Override
            public void onOptionChanged(int option1, int option2, int option3) {
                try {
                    isUpdatePlan = true;
                    HttpMessgeUtil.getInstance().setHttpCallbackWithBase(MineFragment.this);
                    ProgressHudModel.newInstance().show(getContext(),getString(R.string.waitting),getString(R.string.httpError),3000);
                    HttpMessgeUtil.getInstance().postForSetStepsPlan(stepList.get(option1),STEPFLAG);
                    stepPlan = Integer.valueOf(stepList.get(option1));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        //睡眠计划选择器
        window1 = new CharacterPickerWindow(getContext(),getString(R.string.sleepPlan));

        final ArrayList<String> sleepList = MathUitl.getSleepPlanList();
        window1.getPickerView().setText("h","");
        //初始化选项数据
        window1.getPickerView().setPicker(sleepList);
        //设置默认选中的三级项目
        window1.setCurrentPositions(sleepList.size()/2, 0, 0);
        //监听确定选择按钮
        window1.setOnoptionsSelectListener(new OnOptionChangedListener() {
            @Override
            public void onOptionChanged(int option1, int option2, int option3) {
                try {
                    HttpMessgeUtil.getInstance().setHttpCallbackWithBase(MineFragment.this);
                    ProgressHudModel.newInstance().show(getContext(),getString(R.string.waitting),getString(R.string.httpError),3000);
                    HttpMessgeUtil.getInstance().postForSetSleepPlan((int)(Float.valueOf(sleepList.get(option1))*60)+"",SLEEPFLAG);
                    sleepPlan = (int)(Float.valueOf(sleepList.get(option1))*60);
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
        deviceTv = getView().findViewById(R.id.deviceTv);
        userNameTv = getView().findViewById(R.id.userNameTv);
        pictureIv = getView().findViewById(R.id.pictureIv);
        stateTv = getView().findViewById(R.id.deviceNameTv);
        stepPlanTv = getView().findViewById(R.id.stepPlanTv);
        sleepPlanTv = getView().findViewById(R.id.sleepPlanTv);
        blePhotoSwitch = getView().findViewById(R.id.blePhotoSwitch);
        heartSwitch = getView().findViewById(R.id.heartSwitch);
        updateView = getView().findViewById(R.id.updateView);
        if (app.isNewVersion()){
            updateView.setVisibility(View.VISIBLE);
        }else{
            updateView.setVisibility(View.GONE);
        }
    }

    /**
     * 初始化事件监听
     * */
    private void initEvent() {
        getView().findViewById(R.id.pictureIv).setOnClickListener(this);
        getView().findViewById(R.id.userNameTv).setOnClickListener(this);
        getView().findViewById(R.id.deviceLl).setOnClickListener(this);
        getView().findViewById(R.id.stepPlanLl).setOnClickListener(this);
        getView().findViewById(R.id.sleepPlanLl).setOnClickListener(this);
        getView().findViewById(R.id.notificationLl).setOnClickListener(this);
        getView().findViewById(R.id.findLl).setOnClickListener(this);
        getView().findViewById(R.id.blePhoneLl).setOnClickListener(this);
        getView().findViewById(R.id.unitLl).setOnClickListener(this);
        getView().findViewById(R.id.aboutLl).setOnClickListener(this);
        getView().findViewById(R.id.faceLl).setOnClickListener(this);
        getView().findViewById(R.id.logoutLl).setOnClickListener(this);
        blePhotoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if (getActivity().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
                            requestPermissions(new String[]{Manifest.permission.CAMERA,
                            }, 102);
                        }else {
                            app.setCamerable(isChecked);
                        }
                    }else {
                        app.setCamerable(isChecked);
                    }
                }else {
                    app.setCamerable(isChecked);
                }
            }
        });

        heartSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(MainService.getInstance().getState()!=3){
                    showToast(getString(R.string.lostDevice));
                    heartSwitch.setChecked(app.isHeartSwitch());
                } else {
                    app.setHeartSwitch(isChecked);
                    BleClient.getInstance().writeForSetHeartSwitch();
                }
            }
        });
    }

    /**
     * 初始化数据
     * */
    private void initData() {
        userNameTv.setText(userInfo.getUserName());
        stepPlanTv.setText(userInfo.getStepsPlan()+"");
        sleepPlanTv.setText(String.format(Locale.ENGLISH,"%.1fh",userInfo.getSleepPlan()/60f));

        if (app.getUserInfo().getAvatar()!=null)
            Glide.with(this).load(app.getUserInfo().getAvatar()).into(pictureIv);
        else
            pictureIv.setImageResource(userInfo.getSex()==1?R.mipmap.my_head_male_52:R.mipmap.my_head_female_52);

        if (app.isMtk()){
            getView().findViewById(R.id.heartSwitchLl).setVisibility(View.GONE);
        }else {
            getView().findViewById(R.id.heartSwitchLl).setVisibility(View.VISIBLE);
        }


        if (app.isCamerable())
            blePhotoSwitch.setChecked(true);
        else
            blePhotoSwitch.setChecked(false);

        if (app.isHeartSwitch())
            heartSwitch.setChecked(true);
        else
            heartSwitch.setChecked(false);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 102){
            int code = grantResults[0];
            if (!(code == PackageManager.PERMISSION_GRANTED)){
                showToast(getString(R.string.permissionErrorForCamare));
                app.setCamerable(false);
                blePhotoSwitch.setChecked(false);
            }else {
                app.setCamerable(true);
            }
        }
    }

    /**
     * 点击事件监听
     * */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.pictureIv:
            case R.id.userNameTv:
                if (app.getUserInfo().getPhoneNumber()==null&&app.getUserInfo().getEmail()==null)
                    showToast(getString(R.string.visiter));
                else
                    startActivity(new Intent(getActivity(), UserInfoActivity.class));
                break;
            case R.id.deviceLl:
                if (app.getUserInfo().getDeviceCode()==null){
                    startActivity(new Intent(getContext(),SeachingActivity.class));
                }else {
                    if (MainService.getInstance().getState()== WearableManager.STATE_CONNECTING){
                        showToast(getString(R.string.connectting));
                    }else {
                        initSelectPopup();
                        if (mPop != null && !mPop.isShowing()) {
                            mPop.showAsDropDown(getView().findViewById(R.id.device), 10, 10);
                        }
                    }
                }
                break;
            case R.id.stepPlanLl:
                window.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                break;
            case R.id.sleepPlanLl:
                window1.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                break;
            case R.id.notificationLl:
                startActivity(new Intent(getActivity(), NotificationAppListActivity.class));
                break;
            case R.id.findLl:
                if(MainService.getInstance().getState()!=3)
                    showToast(getString(R.string.lostDevice));
                else{
                    ((MainActivity)getActivity()).showMyToast(getString(R.string.sendOK));
                    if (app.isMtk())
                        EXCDController.getInstance().writeForFindDevice();
                    else
                        BleClient.getInstance().writeForFindWatch();
                }
                break;
            case R.id.blePhoneLl:
                startActivity(new Intent(getActivity(), FaqActivity.class));
                break;
            case R.id.unitLl:
                startActivity(new Intent(getActivity(), UnitSelectActivity.class));
                break;
            case R.id.aboutLl:
                startActivity(new Intent(getActivity(), AboutActivity.class));
                break;
            case R.id.faceLl:
                if(MainService.getInstance().getState()!=3)
                    showToast(getString(R.string.lostDevice));
                else{
//                    if (app.isMtk())
                        startActivity(new Intent(getActivity(), SelectDialActivity.class));
//                    else
//                        startActivity(new Intent(getActivity(), SelectDialActivity06.class));
                }
                break;
            case R.id.logoutLl:
                MyAlerDialog.getSingle().showAlerDialog(getString(R.string.tip), getString(R.string.logoutTip), null, null,
                        false, new MyAlerDialog.AlerDialogOnclickListener() {
                            @Override
                            public void onDialogTouch(boolean flag) {
                                if (flag){
                                    if (app.getUserInfo().getDeviceCode()!=null){
                                        String datas = MathUitl.getStringWithJson(getActivity().getSharedPreferences(FILE,MODE_PRIVATE));
                                        try {
                                            HttpMessgeUtil.getInstance().postForUpdownReportData(datas);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (sharedPreferencesp==null)
                                        sharedPreferencesp = getActivity().getSharedPreferences(FILE,MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferencesp.edit();
                                    editor.putString("token",null);
                                    editor.commit();
                                    SaveDataUtil.newInstance().clearDB();
                                    Intent intent = new Intent();
                                    intent.setClass(getActivity(),LoginActivity.class);
                                    startActivity(intent);
                                    getActivity().finish();
                                }
                            }
                        },getActivity());
                break;
        }
    }

    @Override
    public void onCallback(BaseApi baseApi, int id) {
        if (id == STEPFLAG){
            ProgressHudModel.newInstance().diss();
            stepPlanTv.setText(stepPlan+"");
            if(isUpdatePlan){
                app.getUserInfo().setStepsPlan(stepPlan);
                if (MainService.getInstance().getState()!=3){
                    showToast(getString(R.string.syceError));
                }else {
                    if (app.isMtk())
                        EXCDController.getInstance().writeForSetInfo(app.getUserInfo());
                    else
                        BleClient.getInstance().writeForUpdateUserInfo();
                }
            }
        }else if (id == SLEEPFLAG){
            ProgressHudModel.newInstance().diss();
            sleepPlanTv.setText(String.format(Locale.ENGLISH,"%.1fh",sleepPlan/60f));
            app.getUserInfo().setSleepPlan(sleepPlan);
        }
        MathUitl.saveInfoData(getContext(),app.getUserInfo()).commit();
    }
}
