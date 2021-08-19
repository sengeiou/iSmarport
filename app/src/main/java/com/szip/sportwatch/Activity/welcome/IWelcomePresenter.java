package com.szip.sportwatch.Activity.welcome;

import android.content.Context;

public interface IWelcomePresenter {
    //检查隐私协议
    void checkPrivacy(Context context);
    //初始化蓝牙
    void initBle(Context context);
    //初始化设备配置
    void initDeviceConfig();
    //初始化用户信息
    void initUserInfo(Context context);

    void setViewDestory();
}
