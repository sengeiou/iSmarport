package com.szip.sportwatch.Activity.main;

import androidx.fragment.app.FragmentTabHost;

public interface IMainPrisenter {

    //检查蓝牙状态
    void checkBluetoochState();
    //检查应用更新
    void checkUpdata();
    //检查GPS状态
    void checkGPSState();
    //初始化工具栏
    void initHost(FragmentTabHost fragmentTabHost);
    //蓝牙重连
    void initBle();
    void setViewDestory();
}
