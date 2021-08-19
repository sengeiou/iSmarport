package com.szip.sportwatch.Activity.userInfo;

import android.app.Dialog;
import android.net.Uri;

import com.szip.sportwatch.Model.UserInfo;
import com.szip.sportwatch.View.CharacterPickerWindow;

import java.io.File;

public interface IUserInfoPresenter {
    void getSex(CharacterPickerWindow window, UserInfo userInfo);
    void getHeight(CharacterPickerWindow window, UserInfo userInfo);
    void getWeight(CharacterPickerWindow window, UserInfo userInfo);
    void getBirthday(CharacterPickerWindow window, UserInfo userInfo);
    void saveUserInfo(UserInfo userInfo);
    void selectPhoto(Dialog dialog, int y);
    void cropPhoto(Uri uri);
    void updownPhoto(File file);
    void setViewDestory();
}
