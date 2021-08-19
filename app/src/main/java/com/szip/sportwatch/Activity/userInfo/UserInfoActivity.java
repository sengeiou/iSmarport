package com.szip.sportwatch.Activity.userInfo;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.szip.sportwatch.Activity.BaseActivity;
import com.szip.sportwatch.BLE.BleClient;
import com.szip.sportwatch.Interface.HttpCallbackWithBase;
import com.szip.sportwatch.Model.HttpBean.AvatarBean;
import com.szip.sportwatch.Model.HttpBean.BaseApi;
import com.szip.sportwatch.Model.UserInfo;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Service.MainService;
import com.szip.sportwatch.Util.DateUtil;
import com.szip.sportwatch.Util.FileUtil;
import com.szip.sportwatch.Util.HttpMessgeUtil;
import com.szip.sportwatch.Util.JsonGenericsSerializator;
import com.szip.sportwatch.Util.MathUitl;
import com.szip.sportwatch.Util.ProgressHudModel;
import com.szip.sportwatch.Util.StatusBarCompat;
import com.szip.sportwatch.View.CharacterPickerWindow;
import com.szip.sportwatch.View.MyAlerDialog;
import com.szip.sportwatch.View.character.OnOptionChangedListener;
import com.szip.sportwatch.BLE.EXCDController;
import com.yalantis.ucrop.UCrop;
import com.zhy.http.okhttp.callback.GenericsCallback;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import okhttp3.Call;

public class UserInfoActivity extends BaseActivity implements View.OnClickListener,IUserInfoView{

    /**
     * 头像、昵称、性别、身高、体重、生日
     * */
    private ImageView pictureIv;
    private TextView userNameTv,sexTv,heightTv,weightTv,birthdayTv;
    private Context mContext;

    private UserInfo userInfo;
    private MyApplication app;

    private final int IMAGE_CAPTURE = 0;
    private final int IMAGE_MEDIA = 1;

    /**
     * 数据选择框
     * */
    private CharacterPickerWindow window;

    private IUserInfoPresenter iUserInfoPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super. onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_user_info);
        mContext = this;
        app = (MyApplication) getApplicationContext();
        iUserInfoPresenter = new UserInfoPresenterImpl(getApplicationContext(),this);
        initView();
        initEvent();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        iUserInfoPresenter.setViewDestory();
    }

    /**
     * 初始化视图
     * */
    private void initView() {
        StatusBarCompat.translucentStatusBar(UserInfoActivity.this,true);
        setAndroidNativeLightStatusBar(this,true);
        setTitleText(getString(R.string.userInfo));
        window = new CharacterPickerWindow(UserInfoActivity.this);
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
        userInfo = (UserInfo)app.getUserInfo().clone();//软拷贝

        userNameTv.setText(userInfo.getUserName());
        sexTv.setText(userInfo.getSex()==1?getString(R.string.male):getString(R.string.female));
        if (app.getUserInfo().getAvatar()!=null)
            Glide.with(this).load(app.getUserInfo().getAvatar()).into(pictureIv);
        else
            pictureIv.setImageResource(app.getUserInfo().getSex()==1?R.mipmap.my_head_male_52: R.mipmap.my_head_female_52);
        if (userInfo.getUnit() == 0){
            heightTv.setText(userInfo.getHeight()+"cm");
            weightTv.setText(userInfo.getWeight()+"kg");
        }else {
            heightTv.setText(userInfo.getHeightBritish()+"in");
            weightTv.setText(userInfo.getWeightBritish()+"lb");
        }
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED||
                            checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA},
                                100);
                    }else {
                        iUserInfoPresenter.selectPhoto(new Dialog(mContext, R.style.transparentFrameWindowStyle),
                                getWindowManager().getDefaultDisplay().getHeight());

                    }
                }else {
                    iUserInfoPresenter.selectPhoto(new Dialog(mContext, R.style.transparentFrameWindowStyle),
                            getWindowManager().getDefaultDisplay().getHeight());

                }
                break;
            case R.id.userNameRl:
                MyAlerDialog.getSingle().showAlerDialogWithEdit(getString(R.string.userName), userInfo.getUserName(),
                        getString(R.string.enterUserName), null, null,false,
                        new MyAlerDialog.AlerDialogEditOnclickListener() {
                            @Override
                            public void onDialogEditTouch(String edit1) {
                                if (edit1.length()<=12){
                                    userNameTv.setText(edit1);
                                    userInfo.setUserName(edit1);
                                }else {
                                    showToast(getString(R.string.nameLong));
                                }

                            }
                        },mContext);
                break;
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
            case R.id.rightIv:
                ProgressHudModel.newInstance().show(mContext,getString(R.string.waitting));
                iUserInfoPresenter.saveUserInfo(userInfo);
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100){
            int code = grantResults[0];
            if (code == PackageManager.PERMISSION_GRANTED){
                iUserInfoPresenter.selectPhoto(new Dialog(mContext, R.style.transparentFrameWindowStyle),
                        getWindowManager().getDefaultDisplay().getHeight());
            }else {
                showToast(getString(R.string.permissionErrorForCamare));
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
       if (resultCode == UCrop.RESULT_ERROR){
            showToast(getString(R.string.crop_pic_failed));
        }
        switch (requestCode){
            case IMAGE_CAPTURE:{// 相机
                File file = new File(MyApplication.getInstance().getPrivatePath()+"camera.jpg");
                if (file.exists()) {
                    Uri uri;
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        uri = Uri.fromFile(file);
                    } else {
                        uri = FileProvider.getUriForFile(this, "com.szip.sportwatch.fileprovider", file);
                    }
                    iUserInfoPresenter.cropPhoto(uri);
                }
            }
                break;
            case IMAGE_MEDIA:{
                if (data!=null)
                    FileUtil.getInstance().writeUriSdcardFile(data.getData());
                File file = new File(MyApplication.getInstance().getPrivatePath()+"camera.jpg");
                if (file.exists()) {
                    Uri uri;
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        uri = Uri.fromFile(file);
                    } else {
                        uri = FileProvider.getUriForFile(this, "com.szip.sportwatch.fileprovider", file);
                    }
                    iUserInfoPresenter.cropPhoto(uri);
                }
            }
                break;
            case  UCrop.REQUEST_CROP:{
                if (data!=null){
                    iUserInfoPresenter.updownPhoto(new File(MyApplication.getInstance().getPrivatePath()+"crop.jpg"));
                }
            }
                break;
        }
    }

    @Override
    public void setSex(String sexStr, int sex) {
        sexTv.setText(sexStr);
        userInfo.setSex(sex);
        if (userInfo.getAvatar()==null){
            if (sex==0)
                pictureIv.setImageResource(R.mipmap.my_head_female_36);
            else
                pictureIv.setImageResource(R.mipmap.my_head_male_36);
        }
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
    public void saveSeccuss(boolean isSeccuss) {
        ProgressHudModel.newInstance().diss();
        if (isSeccuss){
            showToast(getString(R.string.saved));
            if (MainService.getInstance().getState()!=3){
                showToast(getString(R.string.syceError));
            }else {
                if(app.isMtk()){
                    EXCDController.getInstance().writeForSetInfo(app.getUserInfo());
                }else {
                    BleClient.getInstance().writeForUpdateUserInfo();
                }
            }
            finish();
        }else {
            showToast(getString(R.string.httpError));
        }
    }

    @Override
    public void getPhotoPath(Intent intent, boolean isCamera) {
        if (isCamera){
            startActivityForResult(intent, IMAGE_CAPTURE);
        }else {
            startActivityForResult(intent, IMAGE_MEDIA);
        }
    }

    @Override
    public void getCropPhoto(UCrop uCrop) {
        uCrop.start(this);
    }

    @Override
    public void setPhoto(String pictureUrl) {
        Glide.with(this).load(pictureUrl).into(pictureIv);
        app.getUserInfo().setAvatar(pictureUrl);
        userInfo.setAvatar(pictureUrl);
    }

}
