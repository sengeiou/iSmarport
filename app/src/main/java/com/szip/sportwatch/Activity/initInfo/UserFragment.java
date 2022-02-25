package com.szip.sportwatch.Activity.initInfo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.szip.sportwatch.Activity.main.MainActivity;
import com.szip.sportwatch.Activity.userInfo.IUserInfoPresenter;
import com.szip.sportwatch.Activity.userInfo.IUserInfoView;
import com.szip.sportwatch.Activity.userInfo.UserInfoActivity;
import com.szip.sportwatch.Activity.userInfo.UserInfoPresenterImpl;
import com.szip.sportwatch.BLE.BleClient;
import com.szip.sportwatch.BLE.EXCDController;
import com.szip.sportwatch.Fragment.BaseFragment;
import com.szip.sportwatch.Model.UserInfo;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Service.MainService;
import com.szip.sportwatch.Util.HttpMessgeUtil;
import com.szip.sportwatch.Util.MathUitl;
import com.szip.sportwatch.Util.ProgressHudModel;
import com.szip.sportwatch.View.CharacterPickerWindow;
import com.yalantis.ucrop.UCrop;

public class UserFragment extends BaseFragment implements View.OnClickListener, IUserInfoView {

    private TextView sexTv,heightTv,weightTv,birthdayTv;

    private CharacterPickerWindow window;

    private UserInfo userInfo;

    private IUserInfoPresenter iUserInfoPresenter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_user;
    }

    @Override
    protected void afterOnCreated(Bundle savedInstanceState) {
        iUserInfoPresenter = new UserInfoPresenterImpl(getActivity().getApplicationContext(),this);
        initView();
        initEvent();
    }

    /**
     * 初始化视图
     * */
    private void initView() {
        userInfo = MyApplication.getInstance().getUserInfo();
        window = new CharacterPickerWindow(getActivity());
        sexTv = getView().findViewById(R.id.sexTv);
        heightTv = getView().findViewById(R.id.heightTv);
        weightTv = getView().findViewById(R.id.weightTv);
        birthdayTv = getView().findViewById(R.id.birthdayTv);
    }

    /**
     * 初始化事件监听
     * */
    private void initEvent() {
        getView().findViewById(R.id.sexRl).setOnClickListener(this);
        getView().findViewById(R.id.heightRl).setOnClickListener(this);
        getView().findViewById(R.id.weightRl).setOnClickListener(this);
        getView().findViewById(R.id.birthdayRl).setOnClickListener(this);
        getView().findViewById(R.id.nextBtn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sexRl:
                iUserInfoPresenter.getSex(window,userInfo);
                window.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                break;
            case R.id.heightRl:
                iUserInfoPresenter.getHeight(window,userInfo);
                window.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                break;
            case R.id.weightRl:
                iUserInfoPresenter.getWeight(window,userInfo);
                window.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                break;
            case R.id.birthdayRl:
                iUserInfoPresenter.getBirthday(window,userInfo);
                window.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                break;
            case R.id.nextBtn:
                if(sexTv.getText().toString().equals("")||heightTv.getText().toString().equals("")||
                        weightTv.getText().toString().equals("")||birthdayTv.getText().toString().equals("")){
                    showToast(getString(R.string.finishInfo));
                }else {
                    ProgressHudModel.newInstance().show(getActivity(),getString(R.string.waitting));
                    iUserInfoPresenter.saveUserInfo(userInfo);
                }
                break;
        }
    }

    @Override
    public void setSex(String sexStr, int sex) {
        sexTv.setText(sexStr);
        userInfo.setSex(sex);
    }

    @Override
    public void setHeight(String heightStr, int height, int unit) {
        heightTv.setText(heightStr);
        if (unit == 0)
            userInfo.setHeight(height);
        else
            userInfo.setHeightBritish(height);
    }

    @Override
    public void setWeight(String weightStr, int weight, int unit) {
        weightTv.setText(weightStr);
        if (unit == 0)
            userInfo.setWeight(weight);
        else
            userInfo.setWeightBritish(weight);
    }

    @Override
    public void setBirthday(String birthday) {
        birthdayTv.setText(birthday);
        userInfo.setBirthday(birthday);
    }

    @Override
    public void saveSuccess(boolean isSeccuss) {
        ProgressHudModel.newInstance().diss();
        if (isSeccuss){
            showToast(getString(R.string.saved));
            MathUitl.saveInfoData(getActivity(),userInfo).commit();
            MathUitl.saveStringData(getActivity(),"token", HttpMessgeUtil.getInstance().getToken()).commit();
            Intent intentmain = new Intent(getActivity(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentmain);
        }else {
            showToast(getString(R.string.httpError));
        }
    }

    @Override
    public void getPhotoPath(Intent intent, boolean isCamera) {

    }

    @Override
    public void getCropPhoto(UCrop uCrop) {

    }

    @Override
    public void setPhoto(String pictureUrl) {

    }
}
