package com.szip.sportwatch.Interface;

import com.szip.sportwatch.DB.dbModel.AnimalHeatData;
import com.szip.sportwatch.DB.dbModel.BloodOxygenData;
import com.szip.sportwatch.DB.dbModel.HeartData;
import com.szip.sportwatch.DB.dbModel.SleepData;
import com.szip.sportwatch.DB.dbModel.SportData;
import com.szip.sportwatch.DB.dbModel.StepData;
import com.szip.sportwatch.Model.BleStepModel;

import java.util.ArrayList;

/**
 * Created by Hqs on 2018/1/12
 * 设备回传上来的信息
 */
public interface IDataResponse {

    /**
     * 接收完成计步数据
     */
    void onSaveStepDatas(ArrayList<BleStepModel> datas);

    /**
     * 接收完成总计步数据
     */
    void onSaveDayStepDatas(ArrayList<StepData> datas);

    /**
     * 接收完成心率数据
     */
    void onSaveHeartDatas(ArrayList<HeartData> datas);

    /**
     * 接收完成体温数据
     */
    void onSaveTempDatas(ArrayList<AnimalHeatData> datas);


    /**
     * 接收完成睡眠数据
     */
    void onSaveSleepDatas(ArrayList<SleepData> datas);

    /**
     * 接收完成跑步数据
     */
    void onSaveRunDatas(ArrayList<SportData> datas);

    /**
     * 接收完成血氧数据
     */
    void onSaveBloodOxygenDatas(ArrayList<BloodOxygenData> datas);

    /**
     * 解析完业务数据索引
     */
    void onGetDataIndex(String deviceNum, ArrayList<Integer> dataIndex);

    /**
     * 远程拍照
     * */
    void onCamera(int flag);

    /**
     * 寻找手机
     * */
    void findPhone(int flag);

    /**
     * 更新个人信息
     * */
    void updateUserInfo();
}
