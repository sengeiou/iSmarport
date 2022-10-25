package com.szip.sportwatch.DB;

import android.content.Context;
import android.util.Log;

import com.mediatek.wearable.WearableManager;
import com.necer.utils.CalendarUtil;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;
import com.szip.sportwatch.DB.dbModel.AnimalHeatData;
import com.szip.sportwatch.DB.dbModel.AnimalHeatData_Table;
import com.szip.sportwatch.DB.dbModel.BloodOxygenData;
import com.szip.sportwatch.DB.dbModel.BloodOxygenData_Table;
import com.szip.sportwatch.DB.dbModel.BloodPressureData;
import com.szip.sportwatch.DB.dbModel.BloodPressureData_Table;
import com.szip.sportwatch.DB.dbModel.EcgData;
import com.szip.sportwatch.DB.dbModel.EcgData_Table;
import com.szip.sportwatch.DB.dbModel.HealthyConfig;
import com.szip.sportwatch.DB.dbModel.HeartData;
import com.szip.sportwatch.DB.dbModel.HeartData_Table;
import com.szip.sportwatch.DB.dbModel.NotificationData;
import com.szip.sportwatch.DB.dbModel.NotificationData_Table;
import com.szip.sportwatch.DB.dbModel.ScheduleData;
import com.szip.sportwatch.DB.dbModel.ScheduleData_Table;
import com.szip.sportwatch.DB.dbModel.SleepData;
import com.szip.sportwatch.DB.dbModel.SleepData_Table;
import com.szip.sportwatch.DB.dbModel.SportData;
import com.szip.sportwatch.DB.dbModel.SportData_Table;
import com.szip.sportwatch.DB.dbModel.SportWatchAppFunctionConfigDTO;
import com.szip.sportwatch.DB.dbModel.SportWatchAppFunctionConfigDTO_Table;
import com.szip.sportwatch.DB.dbModel.StepData;
import com.szip.sportwatch.DB.dbModel.StepData_Table;
import com.szip.sportwatch.Model.EvenBusModel.ConnectState;
import com.szip.sportwatch.Model.EvenBusModel.UpdateSchedule;
import com.szip.sportwatch.Util.DateUtil;
import com.szip.sportwatch.Util.LogUtil;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 2019/12/22.
 */

public class SaveDataUtil {
    private static SaveDataUtil saveDataUtil;
    private SaveDataUtil(){

    }

    public static SaveDataUtil newInstance(){                     // 单例模式，双重锁
        if( saveDataUtil == null ){
            synchronized (SaveDataUtil.class){
                if( saveDataUtil == null ){
                    saveDataUtil = new SaveDataUtil();
                }
            }
        }
        return saveDataUtil ;
    }


    /**
     * 批量保存计步
     * */
    public void saveStepDataListData(List<StepData> stepDataList){
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<StepData>() {
                            @Override
                            public void processModel(StepData stepData, DatabaseWrapper wrapper) {
                                StepData sqlData = SQLite.select()
                                        .from(StepData.class)
                                        .where(StepData_Table.time.is(stepData.time))
                                        .querySingle();
                                if (sqlData == null) {//为null则代表数据库没有保存
                                    stepData.save();
                                } else {//不为null则代表数据库存在，进行更新
                                   sqlData.calorie = stepData.calorie;
                                   sqlData.distance = stepData.distance;
                                   sqlData.steps = stepData.steps;
                                   sqlData.update();
                                }
                            }
                        }).addAll(stepDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
                    @Override
                    public void onSuccess(Transaction transaction) {
                        LogUtil.getInstance().logd("DATA******","计步数据保存成功");
                        EventBus.getDefault().post(new ConnectState());
                    }
                }).build().execute();
    }


    /**
     * 批量保存计步数据（线上数据）
     * */
    public void saveStepDataListDataFromWeb(List<StepData> stepDataList){
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<StepData>() {
                            @Override
                            public void processModel(StepData stepData, DatabaseWrapper wrapper) {
                                StepData sqlData = SQLite.select()
                                        .from(StepData.class)
                                        .where(StepData_Table.time.is(stepData.time))
                                        .querySingle();
                                if (sqlData == null){//为null则代表数据库没有保存
                                    stepData.save();
                                }
                                else {//不为null则代表数据库存在，进行更新
                                    sqlData.calorie = stepData.calorie;
                                    sqlData.distance = stepData.distance;
                                    sqlData.steps = stepData.steps;
                                    if (sqlData.dataForHour == null)
                                        sqlData.dataForHour = stepData.dataForHour;
                                    sqlData.update();
                                }
                            }
                        }).addAll(stepDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                LogUtil.getInstance().logd("DATA******","计步数据保存成功");
                EventBus.getDefault().post(new ConnectState());
            }
        }).build().execute();
    }

    /**
     * 批量保存详情计步
     * */
    public void saveStepInfoDataListData(List<StepData> stepDataList){
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<StepData>() {
                            @Override
                            public void processModel(StepData stepData, DatabaseWrapper wrapper) {
                                StepData sqlData = SQLite.select()
                                        .from(StepData.class)
                                        .where(StepData_Table.time.is(stepData.time))
                                        .querySingle();

                                if (sqlData == null){//为null则代表数据库没有保存
                                    stepData.save();
                                }
                                else {//不为null则代表数据库存在，进行更新
                                    if (sqlData.dataForHour != null&&
                                            !sqlData.dataForHour.equals(stepData.dataForHour)){
                                        LogUtil.getInstance().logd("DATA******","STEP D = "+sqlData.dataForHour);
                                        int sql[] = new int[24];
                                        String[] sqlStr = (sqlData.dataForHour == null)?new String[0]:(sqlData.dataForHour.split(","));
                                        int step[] = new int[24];
                                        String[] stepStr = stepData.dataForHour.split(",");
                                        for (int i = 0;i<sqlStr.length;i++){
                                            sql[Integer.valueOf(sqlStr[i].split(":")[0])] = Integer.valueOf(sqlStr[i].split(":")[1]);
                                        }
                                        for (int i = 0;i<stepStr.length;i++){
                                            step[Integer.valueOf(stepStr[i].split(":")[0])] = Integer.valueOf(stepStr[i].split(":")[1]);
                                        }
                                        StringBuffer stepString = new StringBuffer();
                                        for (int i = 0;i<24;i++){
                                            if (sql[i]+step[i]!=0){
                                                stepString.append(String.format(Locale.ENGLISH,",%02d:%d",i,sql[i]+step[i]));
                                            }
                                        }
                                        sqlData.dataForHour = stepString.toString().substring(1);
                                    }else
                                        sqlData.dataForHour = stepData.dataForHour;
                                    sqlData.update();
                                }
                            }
                        }).addAll(stepDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                LogUtil.getInstance().logd("DATA******","计步详情数据保存成功");
                EventBus.getDefault().post(new ConnectState());
            }
        }).build().execute();
    }

    /**
     * 批量保存详情计步(用来保存2523的计步数据，2523的总计步与详情计步是放在一条协议里面的)
     * */
    public void saveStepInfoDataListData1(List<StepData> stepDataList){
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<StepData>() {
                            @Override
                            public void processModel(StepData stepData, DatabaseWrapper wrapper) {
                                StepData sqlData = SQLite.select()
                                        .from(StepData.class)
                                        .where(StepData_Table.time.is(stepData.time))
                                        .querySingle();
                                if (sqlData == null){//为null则代表数据库没有保存
                                    stepData.save();
                                } else {//不为null则代表数据库存在，进行更新
                                    LogUtil.getInstance().logd("DATA******","sql = "+sqlData.dataForHour+" ;step = "+stepData.dataForHour);
                                    int sql[] = new int[24];
                                    String[] sqlStr = (sqlData.dataForHour == null)?new String[0]:(sqlData.dataForHour.split(","));
                                    int step[] = new int[24];
                                    String[] stepStr = stepData.dataForHour.split(",");
                                    for (int i = 0;i<sqlStr.length;i++){
                                        sql[Integer.valueOf(sqlStr[i].substring(0,2))] = Integer.valueOf(sqlStr[i].substring(3));
                                    }
                                    for (int i = 0;i<stepStr.length;i++){
                                        step[Integer.valueOf(stepStr[i].substring(0,2))] = Integer.valueOf(stepStr[i].substring(3));
                                    }
                                    StringBuffer stepString = new StringBuffer();
                                    for (int i = 0;i<24;i++){
                                        if (sql[i]+step[i]!=0){
                                            stepString.append(String.format(Locale.ENGLISH,",%02d:%d",i,sql[i]+step[i]));
                                        }
                                    }
                                    sqlData.dataForHour = stepString.toString().substring(1);
                                    sqlData.steps += stepData.steps;
                                    sqlData.distance += stepData.distance;
                                    sqlData.calorie += stepData.calorie;
                                    sqlData.update();
                                }
                            }
                        }).addAll(stepDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {
                        LogUtil.getInstance().logd("DATA******",error.getMessage());
                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                LogUtil.getInstance().logd("DATA******","计步详情数据保存成功");
                EventBus.getDefault().post(new ConnectState());
            }
        }).build().execute();
    }

    /**
     * 批量保存睡眠
     * */
    public void saveSleepDataListData(List<SleepData> sleepDataList){
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<SleepData>() {
                            @Override
                            public void processModel(SleepData sleepData, DatabaseWrapper wrapper) {
                                SleepData sqlData = SQLite.select()
                                        .from(SleepData.class)
                                        .where(SleepData_Table.time.is(sleepData.time))
                                        .querySingle();
                                if (sqlData == null){//为null则代表数据库没有保存
                                    sleepData.save();
                                } else {//不为null则代表数据库存在，进行更新
                                    sqlData.deepTime = sleepData.deepTime;
                                    sqlData.lightTime = sleepData.lightTime;
                                    sqlData.dataForHour = sleepData.dataForHour;
                                    sqlData.update();
                                }
                            }
                        }).addAll(sleepDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                LogUtil.getInstance().logd("DATA******","睡眠数据保存成功");
            }
        }).build().execute();
    }


    /**
     * 批量保存设备配置
     * */
    public void saveConfigListData(List<SportWatchAppFunctionConfigDTO> sportWatchAppFunctionConfigDTOS){

        SQLite.delete()
        .from(SportWatchAppFunctionConfigDTO.class)
        .execute();

        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<SportWatchAppFunctionConfigDTO>() {
                            @Override
                            public void processModel(SportWatchAppFunctionConfigDTO sportWatchAppFunctionConfigDTO, DatabaseWrapper wrapper) {
                                sportWatchAppFunctionConfigDTO.save();
                            }
                        }).addAll(sportWatchAppFunctionConfigDTOS).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
            }
        }).build().execute();
    }

    /**
     * 批量保存设备健康配置
     * */
    public void saveHealthyConfigListData(List<HealthyConfig> healthyConfigs){

        SQLite.delete()
                .from(HealthyConfig.class)
                .execute();

        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<HealthyConfig>() {
                            @Override
                            public void processModel(HealthyConfig healthyConfig, DatabaseWrapper wrapper) {
                                healthyConfig.save();
                            }
                        }).addAll(healthyConfigs).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
            }
        }).build().execute();
    }

    /**
     * 批量保存详情睡眠
     * */
    public void saveSleepInfoDataListData(List<SleepData> sleepDataList){
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<SleepData>() {
                            @Override
                            public void processModel(SleepData sleepData, DatabaseWrapper wrapper) {
                                SleepData sqlData = SQLite.select()
                                        .from(SleepData.class)
                                        .where(SleepData_Table.time.is(sleepData.time))
                                        .querySingle();
                                if (sqlData == null){//为null则代表数据库没有保存
                                    sleepData.save();
                                } else {//不为null则代表数据库存在，进行更新
                                    sqlData.dataForHour = sleepData.dataForHour;
                                    sqlData.update();
                                }
                            }
                        }).addAll(sleepDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                LogUtil.getInstance().logd("DATA******","睡眠详情保存成功");
                EventBus.getDefault().post(new ConnectState());
            }
        }).build().execute();
    }


    /**
     * 批量保存心率
     * @param isAdd   判断该条数据是当天需要往上累加的数据还是服务器返回的需要替代的数据
     * */
    public void saveHeartDataListData(List<HeartData> heartDataList, final boolean isAdd){
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<HeartData>() {
                            @Override
                            public void processModel(HeartData heartData, DatabaseWrapper wrapper) {
                                HeartData sqlData = SQLite.select()
                                        .from(HeartData.class)
                                        .where(HeartData_Table.time.is(heartData.time))
                                        .querySingle();
                                if (sqlData == null){//为null则代表数据库没有保存
                                    heartData.save();
                                } else {//不为null则代表数据库存在，进行更新
                                    if (isAdd){
                                        String heartStr = sqlData.heartArray+","+heartData.heartArray;
                                        String []heartArray = heartStr.split(",");
                                        int heartSum = 0;
                                        int sum = 0;
                                        for (int i = 0;i<heartArray.length;i++){
                                            if (!heartArray[i].equals("0")){
                                                heartSum+=Integer.valueOf(heartArray[i]);
                                                sum++;
                                            }
                                        }
                                        sqlData.averageHeart = heartSum/sum;
                                        sqlData.heartArray = heartStr;
                                        sqlData.update();
                                    }else {
                                        if (sqlData.getHeartArray().length()<heartData.getHeartArray().length()){
                                            sqlData.averageHeart = heartData.averageHeart;
                                            sqlData.heartArray = heartData.heartArray;
                                            sqlData.update();
                                        }
                                    }

                                }
                            }
                        }).addAll(heartDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                LogUtil.getInstance().logd("DATA******","心率数据保存成功");
                EventBus.getDefault().post(new ConnectState());
            }
        }).build().execute();
    }


    /**
     * 批量保存血压
     * */
    public void saveBloodPressureDataListData(List<BloodPressureData> bloodPressureDataList){
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<BloodPressureData>() {
                            @Override
                            public void processModel(BloodPressureData bloodPressureData, DatabaseWrapper wrapper) {
                                BloodPressureData sqlData = SQLite.select()
                                        .from(BloodPressureData.class)
                                        .where(BloodPressureData_Table.time.is(bloodPressureData.time))
                                        .querySingle();
                                if (sqlData == null&&bloodPressureData.dbpDate!=0){//为null则代表数据库没有保存
                                    bloodPressureData.save();
                                }
                            }
                        }).addAll(bloodPressureDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                LogUtil.getInstance().logd("DATA******","血压数据保存成功");
                EventBus.getDefault().post(new ConnectState());
            }
        }).build().execute();
    }

    /**
     * 批量保存血氧
     * */
    public void saveBloodOxygenDataListData(List<BloodOxygenData> bloodOxygenDataList){
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<BloodOxygenData>() {
                            @Override
                            public void processModel(BloodOxygenData bloodOxygenData, DatabaseWrapper wrapper) {
                                BloodOxygenData sqlData = SQLite.select()
                                        .from(BloodOxygenData.class)
                                        .where(BloodOxygenData_Table.time.is(bloodOxygenData.time))
                                        .querySingle();
                                if (sqlData == null&&bloodOxygenData.bloodOxygenData!=0){//为null则代表数据库没有保存
                                    bloodOxygenData.save();
                                }
                            }
                        }).addAll(bloodOxygenDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                LogUtil.getInstance().logd("DATA******","血氧数据保存成功");
                EventBus.getDefault().post(new ConnectState());
            }
        }).build().execute();
    }

    /**
     * 批量保存体温
     * */
    public void saveAnimalHeatDataListData(List<AnimalHeatData> animalHeatDataList){
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<AnimalHeatData>() {
                            @Override
                            public void processModel(AnimalHeatData animalHeatData, DatabaseWrapper wrapper) {
                                AnimalHeatData sqlData = SQLite.select()
                                        .from(AnimalHeatData.class)
                                        .where(AnimalHeatData_Table.time.is(animalHeatData.time))
                                        .querySingle();
                                if (sqlData == null&&animalHeatData.tempData!=0){//为null则代表数据库没有保存
                                    animalHeatData.save();
                                }
                            }
                        }).addAll(animalHeatDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                LogUtil.getInstance().logd("DATA******","体温数据保存成功");
                EventBus.getDefault().post(new ConnectState());
            }
        }).build().execute();
    }

    /**
     * 批量保存ecg
     * */
    public void saveEcgDataListData(List<EcgData> ecgDataList){
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<EcgData>() {
                            @Override
                            public void processModel(EcgData ecgData, DatabaseWrapper wrapper) {
                                EcgData sqlData = SQLite.select()
                                        .from(EcgData.class)
                                        .where(EcgData_Table.time.is(ecgData.time))
                                        .querySingle();
                                if (sqlData == null){//为null则代表数据库没有保存
                                    ecgData.save();
                                }
                            }
                        }).addAll(ecgDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                LogUtil.getInstance().logd("DATA******","ECG数据保存成功");
                EventBus.getDefault().post(new ConnectState());
            }
        }).build().execute();
    }

    /**
     * 批量保存sport
     * */
    public void saveSportDataListData(List<SportData> sportDataList){
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<SportData>() {
                            @Override
                            public void processModel(SportData sportData, DatabaseWrapper wrapper) {
                                SportData sqlData = SQLite.select()
                                        .from(SportData.class)
                                        .where(SportData_Table.time.is(sportData.time))
                                        .querySingle();
                                if (sqlData == null){//为null则代表数据库没有保存
                                    sportData.save();
                                }
                            }
                        }).addAll(sportDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                LogUtil.getInstance().logd("DATA******","多运动数据保存成功");
            }
        }).build().execute();
    }

    /**
     * 保存sport
     * */
    public void saveSportData(SportData sportData){
        SportData sqlData = SQLite.select()
                .from(SportData.class)
                .where(SportData_Table.time.is(sportData.time))
                .querySingle();
        if (sqlData == null){//为null则代表数据库没有保存
            sportData.save();
            LogUtil.getInstance().logd("DATA******","sport数据保存成功 time = "+sportData.time+" ;distance = "+sportData.distance+" ;caloria = "+sportData.calorie+
                    " ;speed = "+sportData.speed+" ;sportTime = "+sportData.sportTime+" type = "+sportData.type);
        }
    }

    /**
     * 批量保存计划表数据
     * */
    public void saveScheduleListData(List<ScheduleData> scheduleDataList){
        SQLite.delete()
                .from(ScheduleData.class)
                .execute();
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<ScheduleData>() {
                            @Override
                            public void processModel(ScheduleData scheduleData, DatabaseWrapper wrapper) {
                                ScheduleData sqlData = SQLite.select()
                                        .from(ScheduleData.class)
                                        .where(ScheduleData_Table.time.is(scheduleData.time))
                                        .querySingle();
                                if (sqlData == null){//为null则代表数据库没有保存
                                    scheduleData.save();
                                }
                            }
                        }).addAll(scheduleDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                LogUtil.getInstance().logd("DATA******","计划表数据保存成功");
                EventBus.getDefault().post(new UpdateSchedule(new ArrayList<ScheduleData>()));
            }
        }).build().execute();
    }

    public void saveNotificationList(final List<NotificationData> notificationDataList){
        SQLite.delete().from(NotificationData.class).execute();
        FlowManager.getDatabase(AppDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<NotificationData>() {
                            @Override
                            public void processModel(NotificationData notificationData, DatabaseWrapper wrapper) {
                                NotificationData sqlData = SQLite.select()
                                        .from(NotificationData.class)
                                        .where(NotificationData_Table.packageName.is(notificationData.packageName))
                                        .querySingle();
                                if (sqlData!=null){
                                    sqlData.name = notificationData.name;
                                    sqlData.update();
                                }else
                                    notificationData.save();
                            }
                        }).addAll(notificationDataList).build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
            }
        }).build().execute();
    }

    /**
     * 清除数据库
     * */
    public void clearDB(){
//        SQLite.delete()
//                .from(BloodOxygenData.class)
//                .execute();
//        SQLite.delete()
//                .from(BloodPressureData.class)
//                .execute();
//        SQLite.delete()
//                .from(EcgData.class)
//                .execute();
//        SQLite.delete()
//                .from(HeartData.class)
//                .execute();
//        SQLite.delete()
//                .from(SleepData.class)
//                .execute();
//        SQLite.delete()
//                .from(StepData.class)
//                .execute();
//        SQLite.delete()
//                .from(SportData.class)
//                .execute();
//        SQLite.delete()
//                .from(AnimalHeatData.class)
//                .execute();
    }
}
