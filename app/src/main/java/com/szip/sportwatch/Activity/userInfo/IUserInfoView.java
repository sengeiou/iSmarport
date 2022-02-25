package com.szip.sportwatch.Activity.userInfo;

import android.content.Intent;

import com.yalantis.ucrop.UCrop;

public interface IUserInfoView {
    void setSex(String sexStr,int sex);
    void setHeight(String heightStr,int height,int unit);
    void setWeight(String weightStr,int weight,int unit);
    void setBirthday(String birthday);
    void saveSuccess(boolean isSeccuss);
    void getPhotoPath(Intent intent,boolean isCamera);
    void getCropPhoto(UCrop uCrop);
    void setPhoto(String pictureUrl);
}
