package com.szip.sportwatch.DB;

import android.util.Log;

import com.necer.utils.CalendarUtil;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.szip.sportwatch.DB.dbModel.AnimalHeatData;
import com.szip.sportwatch.DB.dbModel.AnimalHeatData_Table;
import com.szip.sportwatch.DB.dbModel.BloodOxygenData;
import com.szip.sportwatch.DB.dbModel.BloodOxygenData_Table;
import com.szip.sportwatch.DB.dbModel.BloodPressureData;
import com.szip.sportwatch.DB.dbModel.BloodPressureData_Table;
import com.szip.sportwatch.DB.dbModel.EcgData;
import com.szip.sportwatch.DB.dbModel.EcgData_Table;
import com.szip.sportwatch.DB.dbModel.HealthyConfig;
import com.szip.sportwatch.DB.dbModel.HealthyConfig_Table;
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
import com.szip.sportwatch.Model.DrawDataBean;
import com.szip.sportwatch.Model.HealthyDataModel;
import com.szip.sportwatch.Model.ReportDataBean;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.DateUtil;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Administrator on 2019/12/22.
 */

public class LoadDataUtil {
    private static LoadDataUtil loadDataUtil;

    private LoadDataUtil(){
    }

    public static LoadDataUtil newInstance(){                     // 单例模式，双重锁
        if( loadDataUtil == null ){
            synchronized (SaveDataUtil.class){
                if( loadDataUtil == null ){
                    loadDataUtil = new LoadDataUtil();
                }
            }
        }
        return loadDataUtil ;
    }

    /**
     * 取计步日报告
     * */
    public ReportDataBean getStepWithDay(long time){
        ReportDataBean reportDataBean = new ReportDataBean();
        ArrayList<DrawDataBean> drawData = new ArrayList<>();
        StepData sqlData = SQLite.select()
                .from(StepData.class)
                .where(StepData_Table.time.is(time))
                .querySingle();
        if (sqlData!=null){
            int sql[] = new int[24];
            if (sqlData.dataForHour!=null){
                String[] sqlStr = sqlData.dataForHour.split(",");
                for (int i = 0;i<sqlStr.length;i++){
                    String[] indexStr = sqlStr[i].split(":");
                    sql[Integer.valueOf(indexStr[0])] = Integer.valueOf(indexStr[1]);
                }
                for (int i = 0;i<sql.length;i++){
                    if (sql[i]!=0)
                        drawData.add(new DrawDataBean(sql[i],0,0));
                    else
                        drawData.add(new DrawDataBean(0,0,0));
                }
            }else {
                for (int i = 0;i<sql.length;i++){
                    drawData.add(new DrawDataBean(0,0,0));
                }
            }

            reportDataBean.setDrawDataBeans(drawData);
            reportDataBean.setValue(sqlData.steps);
            reportDataBean.setValue1(sqlData.distance);
            reportDataBean.setValue2(sqlData.calorie);
            Log.d("DATA******","steps = "+sqlData.steps+" ;hour = "+sqlData.dataForHour);
        }else {
            for (int i = 0;i<24;i++){
                drawData.add(new DrawDataBean(0,0,0));
            }
            reportDataBean.setDrawDataBeans(drawData);
            reportDataBean.setValue(0);
            reportDataBean.setValue1(0);
            reportDataBean.setValue2(0);
        }

        return reportDataBean;
    }

    /**
     * 取计步周报告
     * */
    public ReportDataBean getStepWithWeek(long time){
        ReportDataBean reportDataBean = new ReportDataBean();
        ArrayList<DrawDataBean> drawData = new ArrayList<>();
        for (int i = 0;i<7;i++){
            StepData sqlData = SQLite.select()
                    .from(StepData.class)
                    .where(StepData_Table.time.is(time-(6-i)*24*60*60))
                    .querySingle();
            if (sqlData!=null)
                drawData.add(new DrawDataBean(sqlData.steps,0,0));
            else
                drawData.add(new DrawDataBean(0,0,0));
        }

        reportDataBean.setDrawDataBeans(drawData);
        reportDataBean.setValue(getSum(drawData)[0]);
        reportDataBean.setValue1(getAverage(drawData)[0]);
        return reportDataBean;
    }

    /**
     * 取计步月报告
     * */
    public ReportDataBean getStepWithMonth(long time,int plan){
        ReportDataBean reportDataBean = new ReportDataBean();
        ArrayList<DrawDataBean> drawData = new ArrayList<>();
        for (int i = 0;i<28;i++){
            StepData sqlData = SQLite.select()
                    .from(StepData.class)
                    .where(StepData_Table.time.is(time-(27-i)*24*60*60))
                    .querySingle();
            if (sqlData!=null)
                drawData.add(new DrawDataBean(sqlData.steps,0,0));
            else
                drawData.add(new DrawDataBean(0,0,0));
        }

        reportDataBean.setDrawDataBeans(drawData);
        reportDataBean.setValue(getSum(drawData)[0]);
        reportDataBean.setValue1(getReach(drawData,plan));
        return reportDataBean;
    }

    /**
     * 取计步年报告
     * */
    public ReportDataBean getStepWithYear(long time,int plan){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time*1000);
        calendar.add(Calendar.MONTH,-11);
        calendar.set(Calendar.DAY_OF_MONTH,1);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);

        ReportDataBean reportDataBean = new ReportDataBean();
        ArrayList<DrawDataBean> drawData = new ArrayList<>();
        for (int i = 0;i<12;i++){
            int stepSum = 0;
            long startMonth = calendar.getTimeInMillis()/1000;//年报告的起始月份的第一天第一秒
            calendar.add(Calendar.MONTH,1);
            long endMonth = calendar.getTimeInMillis()/1000-1;//下一个月的第一天-1秒则是上个月的最后一秒
            List<StepData> list = SQLite.select()
                    .from(StepData.class)
                    .where(StepData_Table.time.lessThanOrEq(endMonth),
                            StepData_Table.time.greaterThanOrEq(startMonth))
                    .queryList();
            for (int j = 0;j<list.size();j++){
                stepSum+=list.get(j).steps;
            }
            drawData.add(new DrawDataBean(stepSum,0,0));
        }

        reportDataBean.setDrawDataBeans(drawData);
        reportDataBean.setValue(getSum(drawData)[0]);
        reportDataBean.setValue1(getReach(drawData,plan));
        return reportDataBean;
    }

    /**
     * 取睡眠日报告
     * */
    public ReportDataBean getSleepWithDay(long time){
        ReportDataBean reportDataBean = null;

        SleepData sleepData = SQLite.select()
                .from(SleepData.class)
                .where(SleepData_Table.time.is(time))
                .querySingle();
        if (sleepData!=null){
            reportDataBean = new ReportDataBean();
            if (sleepData.dataForHour!=null&&!sleepData.dataForHour.equals("")){
                String sleepStr[] = sleepData.dataForHour.split(",");
                ArrayList<DrawDataBean> drawData = new ArrayList<>();
                for (int i = 1;i<sleepStr.length;i++){
                    String str[] = sleepStr[i].split(":");
                    drawData.add(new DrawDataBean(Integer.valueOf(str[1]),Integer.valueOf(str[0]),0));
                }
                reportDataBean.setDrawDataBeans(drawData);
                reportDataBean.setValue(DateUtil.getMinue(sleepStr[0]));
            }
            reportDataBean.setValue1(sleepData.deepTime);
            reportDataBean.setValue2(sleepData.lightTime);
        }

        return reportDataBean;
    }

    /**
     * 取睡眠周报告
     * */
    public ReportDataBean getSleepWithWeek(long time){
        ReportDataBean reportDataBean = new ReportDataBean();
        ArrayList<DrawDataBean> drawData = new ArrayList<>();
        for (int i = 0;i<7;i++){
            SleepData sqlData = SQLite.select()
                    .from(SleepData.class)
                    .where(SleepData_Table.time.is(time-(6-i)*24*60*60))
                    .querySingle();
            if (sqlData!=null)
                drawData.add(new DrawDataBean(sqlData.deepTime+sqlData.lightTime,sqlData.lightTime,0));
            else
                drawData.add(new DrawDataBean(0,0,0));
        }

        reportDataBean.setDrawDataBeans(drawData);
        reportDataBean.setValue(getSum(drawData)[0]);
        reportDataBean.setValue1(getAverage(drawData)[0]);
        return reportDataBean;
    }

    /**
     * 取睡眠月报告
     * */
    public ReportDataBean getSleepWithMonth(long time){
        ReportDataBean reportDataBean = new ReportDataBean();
        ArrayList<DrawDataBean> drawData = new ArrayList<>();
        for (int i = 0;i<28;i++){
            SleepData sqlData = SQLite.select()
                    .from(SleepData.class)
                    .where(SleepData_Table.time.is(time-(27-i)*24*60*60))
                    .querySingle();
            if (sqlData!=null)
                drawData.add(new DrawDataBean(sqlData.deepTime+sqlData.lightTime,sqlData.lightTime,0));
            else
                drawData.add(new DrawDataBean(0,0,0));
        }

        reportDataBean.setDrawDataBeans(drawData);
        reportDataBean.setValue(getSum(drawData)[0]);
        reportDataBean.setValue1(getAverage(drawData)[0]);
        return reportDataBean;
    }

    /**
     * 取睡眠年报告
     * */
    public ReportDataBean getSleepWithYear(long time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time*1000);
        calendar.add(Calendar.MONTH,-11);
        calendar.set(Calendar.DAY_OF_MONTH,1);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);

        ReportDataBean reportDataBean = new ReportDataBean();
        ArrayList<DrawDataBean> drawData = new ArrayList<>();
        for (int i = 0;i<12;i++){
            int deepSleep = 0;
            int lightSleep = 0;
            long startMonth = calendar.getTimeInMillis()/1000;//年报告的起始月份的第一天第一秒
            calendar.add(Calendar.MONTH,1);
            long endMonth = calendar.getTimeInMillis()/1000-1;//下一个月的第一天-1秒则是上个月的最后一秒
            List<SleepData> list = SQLite.select()
                    .from(SleepData.class)
                    .where(SleepData_Table.time.lessThanOrEq(endMonth),
                            SleepData_Table.time.greaterThanOrEq(startMonth))
                    .queryList();
            for (int j = 0;j<list.size();j++){
                deepSleep+=list.get(j).deepTime;
                lightSleep+=list.get(j).lightTime;
            }
            drawData.add(new DrawDataBean((deepSleep+lightSleep),lightSleep,0));
        }
        reportDataBean.setDrawDataBeans(drawData);
        reportDataBean.setValue(getSum(drawData)[0]);
        reportDataBean.setValue1(getAverage(drawData)[0]);
        return reportDataBean;
    }


    /**
     * 取心率日报告
     * */
    public ReportDataBean getHeartWithDay(long time){
        Log.d("DATA******","获取心率报告");
        //绘图数据-40传入控件
        ReportDataBean reportDataBean = null;

        HeartData sqlData = SQLite.select()
                .from(HeartData.class)
                .where(HeartData_Table.time.is(time))
                .querySingle();

        if (sqlData!=null){
            reportDataBean = new ReportDataBean();
            ArrayList<DrawDataBean> drawData = new ArrayList<>();
            String heartArray[] = sqlData.heartArray.split(",");
            for (int i = 0;i<heartArray.length;i++){
                if (Integer.valueOf(heartArray[i])!=0)
                drawData.add(new DrawDataBean(Integer.valueOf(heartArray[i])-40<0?0:(Integer.valueOf(heartArray[i])-40),0,0));
            }
            Log.d("DATA******","heart = "+sqlData.heartArray);
            reportDataBean.setDrawDataBeans(drawData);
            reportDataBean.setValue(sqlData.averageHeart);
            int data[];
            data = getMaxMin(drawData);
            reportDataBean.setValue1(data[0]);
            reportDataBean.setValue2(data[1]);
        }
        return reportDataBean;
    }

    /**
     * 取心率周报告
     * */
    public ReportDataBean getHeartWithWeek(long time){
        ReportDataBean reportDataBean = new ReportDataBean();
        ArrayList<DrawDataBean> drawData = new ArrayList<>();
        for (int i = 0;i<7;i++){
            HeartData sqlData = SQLite.select()
                    .from(HeartData.class)
                    .where(HeartData_Table.time.is(time-(6-i)*24*60*60))
                    .querySingle();
            if (sqlData!=null)
                drawData.add(new DrawDataBean(sqlData.averageHeart-40,0,0));
            else
                drawData.add(new DrawDataBean(0,0,0));
        }
        reportDataBean.setDrawDataBeans(drawData);
        reportDataBean.setValue(getAverage(drawData)[0]);
        int data[];
        data = getMaxMin(drawData);
        reportDataBean.setValue1(data[0]);
        reportDataBean.setValue2(data[1]);
        return reportDataBean;
    }

    /**
     * 取心率月报告
     * */
    public ReportDataBean getHeartWithMonth(long time){
        ReportDataBean reportDataBean = new ReportDataBean();
        ArrayList<DrawDataBean> drawData = new ArrayList<>();
        for (int i = 0;i<28;i++){
            HeartData sqlData = SQLite.select()
                    .from(HeartData.class)
                    .where(HeartData_Table.time.is(time-(27-i)*24*60*60))
                    .querySingle();
            if (sqlData!=null)
                drawData.add(new DrawDataBean(sqlData.averageHeart-40,0,0));
            else
                drawData.add(new DrawDataBean(0,0,0));
        }
        reportDataBean.setDrawDataBeans(drawData);
        reportDataBean.setValue(getAverage(drawData)[0]);
        int data[];
        data = getMaxMin(drawData);
        reportDataBean.setValue1(data[0]);
        reportDataBean.setValue2(data[1]);
        return reportDataBean;
    }

    /**
     * 取心率年报告
     * */
    public ReportDataBean getHeartWithYear(long time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time*1000);
        calendar.add(Calendar.MONTH,-11);
        calendar.set(Calendar.DAY_OF_MONTH,1);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);

        ReportDataBean reportDataBean = new ReportDataBean();
        ArrayList<DrawDataBean> drawData = new ArrayList<>();
        for (int i = 0;i<12;i++){
            int heartSum = 0;
            int sum = 0;
            long startMonth = calendar.getTimeInMillis()/1000;//年报告的起始月份的第一天第一秒
            calendar.add(Calendar.MONTH,1);
            long endMonth = calendar.getTimeInMillis()/1000-1;//下一个月的第一天-1秒则是上个月的最后一秒
            List<HeartData> list = SQLite.select()
                    .from(HeartData.class)
                    .where(HeartData_Table.time.lessThanOrEq(endMonth),
                            HeartData_Table.time.greaterThanOrEq(startMonth))
                    .queryList();
            for (int j = 0;j<list.size();j++){
                Log.d("DATA******","time = "+ DateUtil.getStringDateFromSecond(list.get(j).time,"yyyy-MM-dd"));
                    heartSum+=list.get(j).averageHeart;
                    sum++;
            }
            drawData.add(new DrawDataBean(sum==0?0:heartSum/sum-40,0,0));
        }

        reportDataBean.setDrawDataBeans(drawData);
        reportDataBean.setValue(getAverage(drawData)[0]);
        int data[];
        data = getMaxMin(drawData);
        reportDataBean.setValue1(data[0]);
        reportDataBean.setValue2(data[1]);
        return reportDataBean;
    }


    /**
     * 取血压日报告
     * */
    public ReportDataBean getBloodPressureWithDay(long time){
        //绘图数据-45传入控件
        ReportDataBean reportDataBean =new ReportDataBean();

        ArrayList<DrawDataBean> drawData = new ArrayList<>();
        List<BloodPressureData> list = SQLite.select()
                .from(BloodPressureData.class)
                .where(BloodPressureData_Table.time.lessThan(time+24*60*60-1),
                        BloodPressureData_Table.time.greaterThanOrEq(time))
//                .orderBy(OrderBy.fromString(BloodPressureData_Table.time+OrderBy.DESCENDING))
                .queryList();
        for (int i = 0;i<list.size();i++){
            Log.d("DATA******","SBP = "+list.get(i).sbpDate+" ;DBP = "+list.get(i).dbpDate);
            drawData.add(new DrawDataBean(list.get(i).sbpDate-45,
                    list.get(i).dbpDate-45,list.get(i).time));
        }

        reportDataBean.setDrawDataBeans(drawData);
        return reportDataBean;
    }

    /**
     * 取血压周报告
     * */
    public ReportDataBean getBloodPressureWithWeek(long time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time*1000);
        calendar.add(Calendar.DAY_OF_WEEK,-6);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        long startTime = calendar.getTimeInMillis()/1000;//周报告的起始天数的第一秒

        List<BloodPressureData> list = SQLite.select()
                .from(BloodPressureData.class)
                .where(BloodPressureData_Table.time.lessThanOrEq(time+24*60*60-1),
                        BloodPressureData_Table.time.greaterThanOrEq(startTime))
                .queryList();

        ReportDataBean reportDataBean = new ReportDataBean();
        ArrayList<DrawDataBean> drawData = new ArrayList<>();
        for (int i = 0;i<7;i++){
            int sbp = 0;
            int dbp = 0;
            int sum = 0;
            long startMonth = calendar.getTimeInMillis()/1000;//周报告的起始天数的第一秒
            calendar.add(Calendar.DAY_OF_WEEK,1);
            long endMonth = calendar.getTimeInMillis()/1000-1;//下一天减一秒就是上一天的最后一秒
            for (int j = 0;j<list.size();j++){
                if (list.get(j).time<=endMonth&&list.get(j).time>=startMonth){
                    sbp+=list.get(j).sbpDate;
                    dbp+=list.get(j).dbpDate;
                    sum++;
                }
            }
            drawData.add(new DrawDataBean(sum==0?0:sbp/sum-45,sum==0?0:dbp/sum-45,0));
        }

        reportDataBean.setDrawDataBeans(drawData);
        int data[];
        data = getAverage(drawData);
        reportDataBean.setValue(data[0]);
        reportDataBean.setValue1(data[1]);
        return reportDataBean;
    }

    /**
     * 取血压月报告
     * */
    public ReportDataBean getBloodPressureWithMonth(long time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time*1000);
        calendar.add(Calendar.DAY_OF_MONTH,-27);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        long startTime = calendar.getTimeInMillis()/1000;//月报告的起始天数的第一秒

        List<BloodPressureData> list = SQLite.select()
                .from(BloodPressureData.class)
                .where(BloodPressureData_Table.time.lessThanOrEq(time+24*60*60-1),
                        BloodPressureData_Table.time.greaterThanOrEq(startTime))
                .queryList();

        ReportDataBean reportDataBean = new ReportDataBean();
        ArrayList<DrawDataBean> drawData = new ArrayList<>();
        for (int i = 0;i<28;i++){
            int sbp = 0;
            int dbp = 0;
            int sum = 0;
            long startMonth = calendar.getTimeInMillis()/1000;//月报告的起始天数的第一秒
            calendar.add(Calendar.DAY_OF_MONTH,1);
            long endMonth = calendar.getTimeInMillis()/1000-1;//下一天减一秒就是上一天的最后一秒
            for (int j = 0;j<list.size();j++){
                if (list.get(j).time<=endMonth&&list.get(j).time>=startMonth){
                    sbp+=list.get(j).sbpDate;
                    dbp+=list.get(j).dbpDate;
                    sum++;
                }
            }
            drawData.add(new DrawDataBean(sum==0?0:sbp/sum-45,sum==0?0:dbp/sum-45,0));
        }

        reportDataBean.setDrawDataBeans(drawData);
        int data[];
        data = getAverage(drawData);
        reportDataBean.setValue(data[0]);
        reportDataBean.setValue1(data[1]);
        return reportDataBean;
    }

    /**
     * 取血压年报告
     * */
    public ReportDataBean getBloodPressureWithYear(long time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time*1000);
        calendar.add(Calendar.MONTH,-11);
        calendar.set(Calendar.DAY_OF_MONTH,1);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        long startTime = calendar.getTimeInMillis()/1000;//年报告的起始月份的第一天第一秒



        ReportDataBean reportDataBean = new ReportDataBean();
        ArrayList<DrawDataBean> drawData = new ArrayList<>();
        for (int i = 0;i<12;i++){
            int sbp = 0;
            int dbp = 0;
            int sum = 0;
            long startMonth = calendar.getTimeInMillis()/1000;//年报告的起始月份的第一天第一秒
            calendar.add(Calendar.MONTH,1);
            long endMonth = calendar.getTimeInMillis()/1000-1;//下一个月的第一天-1秒则是上个月的最后一秒
            List<BloodPressureData> list = SQLite.select()
                    .from(BloodPressureData.class)
                    .where(BloodPressureData_Table.time.lessThanOrEq(endMonth),
                            BloodPressureData_Table.time.greaterThanOrEq(startMonth))
                    .queryList();
            for (int j = 0;j<list.size();j++){

                    sbp+=list.get(j).sbpDate;
                    dbp+=list.get(j).dbpDate;
                    sum++;

            }
            drawData.add(new DrawDataBean(sum==0?0:sbp/sum-45,sum==0?0:dbp/sum-45,0));
        }

        reportDataBean.setDrawDataBeans(drawData);
        int data[];
        data = getAverage(drawData);
        reportDataBean.setValue(data[0]);
        reportDataBean.setValue1(data[1]);
        return reportDataBean;
    }

    /**
     * 取血氧日报告
     * */
    public ReportDataBean getBloodOxygenWithDay(long time){
        //绘图数据-70传入控件
        ReportDataBean reportDataBean = new ReportDataBean();
        ArrayList<DrawDataBean> drawData = new ArrayList<>();
        List<BloodOxygenData> list = SQLite.select()
                .from(BloodOxygenData.class)
                .where(BloodOxygenData_Table.time.lessThan(time+24*60*60-1),
                        BloodOxygenData_Table.time.greaterThanOrEq(time))
//                .orderBy(OrderBy.fromString(BloodPressureData_Table.time+OrderBy.DESCENDING))
                .queryList();
        for (int i = 0;i<list.size();i++){
            drawData.add(new DrawDataBean(list.get(i).bloodOxygenData -70, 0,list.get(i).time));
        }

        reportDataBean.setDrawDataBeans(drawData);
        return reportDataBean;
    }

    /**
     * 取血氧周报告
     * */
    public ReportDataBean getBloodOxygenWithWeek(long time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time*1000);
        calendar.add(Calendar.DAY_OF_WEEK,-6);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        long startTime = calendar.getTimeInMillis()/1000;//月报告的起始天数的第一秒

        List<BloodOxygenData> list = SQLite.select()
                .from(BloodOxygenData.class)
                .where(BloodOxygenData_Table.time.lessThanOrEq(time+24*60*60-1),
                        BloodOxygenData_Table.time.greaterThanOrEq(startTime))
                .queryList();

        ReportDataBean reportDataBean = new ReportDataBean();
        ArrayList<DrawDataBean> drawData = new ArrayList<>();
        for (int i = 0;i<7;i++){
            int value = 0;
            int sum = 0;
            long startMonth = calendar.getTimeInMillis()/1000;//月报告的起始天数的第一秒
            calendar.add(Calendar.DAY_OF_WEEK,1);
            long endMonth = calendar.getTimeInMillis()/1000-1;//下一天减一秒就是上一天的最后一秒
            for (int j = 0;j<list.size();j++){
                if (list.get(j).time<=endMonth&&list.get(j).time>=startMonth){
                    value+=list.get(j).bloodOxygenData;
                    sum++;
                }
            }
            drawData.add(new DrawDataBean(sum==0?0:value/sum-70,0,0));
        }

        reportDataBean.setDrawDataBeans(drawData);
        reportDataBean.setValue(getAverage(drawData)[0]);
        reportDataBean.setValue1(getReach(drawData,24));
        return reportDataBean;
    }

    /**
     * 取血氧月报告
     * */
    public ReportDataBean getBloodOxygenWithMonth(long time){

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time*1000);
        calendar.add(Calendar.DAY_OF_MONTH,-27);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        long startTime = calendar.getTimeInMillis()/1000;//月报告的起始天数的第一秒

        List<BloodOxygenData> list = SQLite.select()
                .from(BloodOxygenData.class)
                .where(BloodOxygenData_Table.time.lessThanOrEq(time+24*60*60-1),
                        BloodOxygenData_Table.time.greaterThanOrEq(startTime))
                .queryList();

        ReportDataBean reportDataBean = new ReportDataBean();
        ArrayList<DrawDataBean> drawData = new ArrayList<>();
        for (int i = 0;i<28;i++){
            int value = 0;
            int sum = 0;
            long startMonth = calendar.getTimeInMillis()/1000;//月报告的起始天数的第一秒
            calendar.add(Calendar.DAY_OF_MONTH,1);
            long endMonth = calendar.getTimeInMillis()/1000-1;//下一天减一秒就是上一天的最后一秒
            for (int j = 0;j<list.size();j++){
                if (list.get(j).time<=endMonth&&list.get(j).time>=startMonth){
                    value+=list.get(j).bloodOxygenData;
                    sum++;
                }
            }
            drawData.add(new DrawDataBean(sum==0?0:value/sum-70,0,0));
        }

        reportDataBean.setDrawDataBeans(drawData);
        reportDataBean.setValue(getAverage(drawData)[0]);
        reportDataBean.setValue1(getReach(drawData,24));
        return reportDataBean;
    }

    /**
     * 取血氧年报告
     * */
    public ReportDataBean getBloodOxygenWithYear(long time){

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time*1000);
        calendar.add(Calendar.MONTH,-11);
        calendar.set(Calendar.DAY_OF_MONTH,1);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);

        ReportDataBean reportDataBean = new ReportDataBean();
        ArrayList<DrawDataBean> drawData = new ArrayList<>();
        for (int i = 0;i<12;i++){
            int value = 0;
            int sum = 0;
            long startMonth = calendar.getTimeInMillis()/1000;//年报告的起始月份的第一天第一秒
            calendar.add(Calendar.MONTH,1);
            long endMonth = calendar.getTimeInMillis()/1000-1;//下一个月的第一天-1秒则是上个月的最后一秒
            List<BloodOxygenData> list = SQLite.select()
                    .from(BloodOxygenData.class)
                    .where(BloodOxygenData_Table.time.lessThanOrEq(endMonth),
                            BloodOxygenData_Table.time.greaterThanOrEq(startMonth))
                    .queryList();
            for (int j = 0;j<list.size();j++){

                    value+=list.get(j).bloodOxygenData;
                    sum++;

            }
            drawData.add(new DrawDataBean(sum==0?0:value/sum-70,0,0));
        }

        reportDataBean.setDrawDataBeans(drawData);
        reportDataBean.setValue(getAverage(drawData)[0]);
        reportDataBean.setValue1(getReach(drawData,24));
        return reportDataBean;
    }


    /**
     * 取体温日报告
     * */
    public ReportDataBean getAnimalHeatWithDay(long time){
        //绘图数据-70传入控件
        ReportDataBean reportDataBean = new ReportDataBean();
        ArrayList<DrawDataBean> drawData = new ArrayList<>();
        List<AnimalHeatData> list = SQLite.select()
                .from(AnimalHeatData.class)
                .where(AnimalHeatData_Table.time.lessThan(time+24*60*60-1),
                        AnimalHeatData_Table.time.greaterThanOrEq(time))
                .queryList();

        for (int i = 0;i<list.size();i++){
            drawData.add(new DrawDataBean(list.get(i).tempData -340, 0,list.get(i).time));
        }

        reportDataBean.setDrawDataBeans(drawData);
        return reportDataBean;
    }

    /**
     * 取体温周报告
     * */
    public ReportDataBean getAnimalHeatWithWeek(long time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time*1000);
        calendar.add(Calendar.DAY_OF_WEEK,-6);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        long startTime = calendar.getTimeInMillis()/1000;//月报告的起始天数的第一秒

        List<AnimalHeatData> list = SQLite.select()
                .from(AnimalHeatData.class)
                .where(AnimalHeatData_Table.time.lessThanOrEq(time+24*60*60-1),
                        AnimalHeatData_Table.time.greaterThanOrEq(startTime))
                .queryList();

        ReportDataBean reportDataBean = new ReportDataBean();
        ArrayList<DrawDataBean> drawData = new ArrayList<>();
        for (int i = 0;i<7;i++){
            int value = 0;
            int sum = 0;
            long startMonth = calendar.getTimeInMillis()/1000;//月报告的起始天数的第一秒
            calendar.add(Calendar.DAY_OF_WEEK,1);
            long endMonth = calendar.getTimeInMillis()/1000-1;//下一天减一秒就是上一天的最后一秒
            for (int j = 0;j<list.size();j++){
                if (list.get(j).time<=endMonth&&list.get(j).time>=startMonth){
                    value+=list.get(j).tempData;
                    sum++;
                }
            }
            drawData.add(new DrawDataBean(sum==0?0:value/sum-340,0,0));
        }

        reportDataBean.setDrawDataBeans(drawData);
        reportDataBean.setValue(getAverage(drawData)[0]);
        reportDataBean.setValue1(getMaxMin(drawData)[0]);
        return reportDataBean;
    }

    /**
     * 取体温月报告
     * */
    public ReportDataBean getAnimalHeatWithMonth(long time){

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time*1000);
        calendar.add(Calendar.DAY_OF_MONTH,-27);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        long startTime = calendar.getTimeInMillis()/1000;//月报告的起始天数的第一秒

        List<AnimalHeatData> list = SQLite.select()
                .from(AnimalHeatData.class)
                .where(AnimalHeatData_Table.time.lessThanOrEq(time+24*60*60-1),
                        AnimalHeatData_Table.time.greaterThanOrEq(startTime))
                .queryList();

        ReportDataBean reportDataBean = new ReportDataBean();
        ArrayList<DrawDataBean> drawData = new ArrayList<>();
        for (int i = 0;i<28;i++){
            int value = 0;
            int sum = 0;
            long startMonth = calendar.getTimeInMillis()/1000;//月报告的起始天数的第一秒
            calendar.add(Calendar.DAY_OF_MONTH,1);
            long endMonth = calendar.getTimeInMillis()/1000-1;//下一天减一秒就是上一天的最后一秒
            for (int j = 0;j<list.size();j++){
                if (list.get(j).time<=endMonth&&list.get(j).time>=startMonth){
                    value+=list.get(j).tempData;
                    sum++;
                }
            }
            drawData.add(new DrawDataBean(sum==0?0:value/sum-340,0,0));
        }

        reportDataBean.setDrawDataBeans(drawData);
        reportDataBean.setValue(getAverage(drawData)[0]);
        reportDataBean.setValue1(getMaxMin(drawData)[0]);
        return reportDataBean;
    }

    /**
     * 取体温年报告
     * */
    public ReportDataBean getAnimalHeatWithYear(long time){

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time*1000);
        calendar.add(Calendar.MONTH,-11);
        calendar.set(Calendar.DAY_OF_MONTH,1);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);


        ReportDataBean reportDataBean = new ReportDataBean();
        ArrayList<DrawDataBean> drawData = new ArrayList<>();
        for (int i = 0;i<12;i++){
            int value = 0;
            int sum = 0;
            long startMonth = calendar.getTimeInMillis()/1000;//年报告的起始月份的第一天第一秒
            calendar.add(Calendar.MONTH,1);
            long endMonth = calendar.getTimeInMillis()/1000-1;//下一个月的第一天-1秒则是上个月的最后一秒
            List<AnimalHeatData> list = SQLite.select()
                    .from(AnimalHeatData.class)
                    .where(AnimalHeatData_Table.time.lessThanOrEq(endMonth),
                            AnimalHeatData_Table.time.greaterThanOrEq(startMonth)
                    )
                    .queryList();
            for (int j = 0;j<list.size();j++){
                value+=list.get(j).tempData;
                sum++;
            }
            drawData.add(new DrawDataBean(sum==0?0:value/sum-340,0,0));
        }

        reportDataBean.setDrawDataBeans(drawData);
        reportDataBean.setValue(getAverage(drawData)[0]);
        reportDataBean.setValue1(getMaxMin(drawData)[0]);
        return reportDataBean;
    }


    /**
     * 取ecg数据
     * */
    public ArrayList<DrawDataBean> getEcgDataList(long time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time*1000);
        calendar.set(Calendar.DAY_OF_MONTH,1);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        long startTime = calendar.getTimeInMillis()/1000;//月报告的起始天数的第一秒

        ArrayList<DrawDataBean> drawData = new ArrayList<>();


        List<EcgData> list = SQLite.select()
                .from(EcgData.class)
                .where(EcgData_Table.time.lessThanOrEq(time+24*60*60-1),
                        EcgData_Table.time.greaterThanOrEq(startTime))
                .orderBy(OrderBy.fromString(EcgData_Table.time+OrderBy.DESCENDING))
                .queryList();
        for (int i = 0;i<list.size();i++){
            String heartArray[] = list.get(i).heart.split(",");
            ArrayList<DrawDataBean> heart = new ArrayList<>();
            for (int j = 0;j<heartArray.length;j++){
                if (Integer.valueOf(heartArray[j])!=0)
                    heart.add(new DrawDataBean(Integer.valueOf(heartArray[j]),0,0));
            }
            int averageHeart = getAverage(heart)[0];
            int data[];
            data = getMaxMin(heart);
            drawData.add(new DrawDataBean(averageHeart,data[0],data[1],list.get(i).time));
        }

        return drawData;
    }

    /**
     * 取运动数据
     * */
    public List<SportData> getBestSportData(long time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time*1000);
        calendar.set(Calendar.DAY_OF_MONTH,1);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        long startTime = calendar.getTimeInMillis()/1000;//月报告的起始天数的第一秒

        List<SportData> list = SQLite.select()
                .from(SportData.class)
                .where(SportData_Table.time.lessThanOrEq(time+24*60*60-1),
                        SportData_Table.time.greaterThanOrEq(startTime))
                .queryList();

        return list;
    }

    /**
     * 取最佳运动数据
     * */
    public SportData getBestSportData(){

        SportData sportData = SQLite.select()
                .from(SportData.class)
                .orderBy(OrderBy.fromString(SportData_Table.sportTime+OrderBy.DESCENDING))
                .limit(0)
                .querySingle();
        if (sportData == null)
            return new SportData();
        return sportData;
    }

    /**
     * 删除运动数据
     * */
    public void removeSportData(SportData data){
        SQLite.delete()
                .from(SportData.class)
                .where(SportData_Table.id.is(data.id))
                .execute();
    }

    /**
     * 取当天最近一次的健康数据
     * */
    public HealthyDataModel getHealthyDataLast(long time){
        HealthyDataModel healthyDataModel = new HealthyDataModel();

        StepData stepData = SQLite.select()
                .from(StepData.class)
                .where(StepData_Table.time.is(time))
                .querySingle();
        if (stepData!=null){
            healthyDataModel.setStepsData(stepData.steps);
            healthyDataModel.setDistanceData(stepData.distance);
            healthyDataModel.setKcalData(stepData.calorie);
        }else {
            healthyDataModel.setStepsData(0);
            healthyDataModel.setDistanceData(0);
            healthyDataModel.setKcalData(0);
        }


        SleepData sleepData = SQLite.select()
                .from(SleepData.class)
                .where(SleepData_Table.time.is(time))
                .querySingle();
        if (sleepData!=null){
            healthyDataModel.setLightSleepData(sleepData.lightTime);
            healthyDataModel.setAllSleepData(sleepData.lightTime+sleepData.deepTime);
        }


        HeartData heartData = SQLite.select()
                .from(HeartData.class)
                .where(HeartData_Table.time.is(time))
                .querySingle();
        if (heartData!=null){
            if (heartData.getHeartArray().equals("")){
                healthyDataModel.setHeartData(0);
            }else {
                String[] hearts = heartData.getHeartArray().split(",");
                healthyDataModel.setHeartData(hearts.length==0?0:Integer.valueOf(hearts[hearts.length-1]));
            }

        }
        else
            healthyDataModel.setHeartData(0);


        BloodPressureData bloodPressureData = SQLite.select()
                .from(BloodPressureData.class)
                .where(BloodPressureData_Table.time.greaterThanOrEq(time),
                        BloodPressureData_Table.time.lessThanOrEq(time*24*60*60-1))
                .orderBy(OrderBy.fromString(BloodPressureData_Table.time+OrderBy.DESCENDING))
                .limit(0)
                .querySingle();
        if (bloodPressureData!=null){
            healthyDataModel.setSbpData(bloodPressureData.sbpDate);
            healthyDataModel.setDbpData(bloodPressureData.dbpDate);
        }else {
            healthyDataModel.setSbpData(0);
            healthyDataModel.setDbpData(0);
        }


        BloodOxygenData bloodOxygenData = SQLite.select()
                .from(BloodOxygenData.class)
                .where(BloodOxygenData_Table.time.greaterThanOrEq(time),
                        BloodOxygenData_Table.time.lessThanOrEq(time*24*60*60-1))
                .orderBy(OrderBy.fromString(BloodOxygenData_Table.time+OrderBy.DESCENDING))
                .limit(0)
                .querySingle();
        if (bloodOxygenData!=null)
            healthyDataModel.setBloodOxygenData(bloodOxygenData.bloodOxygenData);
        else
            healthyDataModel.setBloodOxygenData(0);

        AnimalHeatData animalHeatData = SQLite.select()
                .from(AnimalHeatData.class)
                .where(AnimalHeatData_Table.time.greaterThanOrEq(time),
                        AnimalHeatData_Table.time.lessThanOrEq(time*24*60*60-1))
                .orderBy(OrderBy.fromString(AnimalHeatData_Table.time+OrderBy.DESCENDING))
                .limit(0)
                .querySingle();
        if (animalHeatData!=null)
            healthyDataModel.setAnimalHeatData(animalHeatData.tempData);
        else
            healthyDataModel.setAnimalHeatData(0);

        EcgData ecgData = SQLite.select()
                .from(EcgData.class)
                .where(EcgData_Table.time.greaterThanOrEq(time),
                        EcgData_Table.time.lessThanOrEq(time*24*60*60-1))
                .orderBy(OrderBy.fromString(EcgData_Table.time+OrderBy.DESCENDING))
                .limit(0)
                .querySingle();
        if (ecgData!=null){
            String heartArray[] = ecgData.heart.split(",");
            ArrayList<DrawDataBean> heart = new ArrayList<>();
            for (int j = 0;j<heartArray.length;j++){
                if (Integer.valueOf(heartArray[j])!=0)
                    heart.add(new DrawDataBean(Integer.valueOf(heartArray[j]),0,0));
            }
            healthyDataModel.setEcgData(getAverage(heart)[0]);
        }

        else
            healthyDataModel.setEcgData(0);

        return healthyDataModel;
    }

    /**
     * 初始化日历中有数据的天数（画黄点）
     * */
    public void initCalendarPoint(String type){
        List<LocalDate> dataList = new ArrayList<>();

        List<StepData> stepDataList = new ArrayList<>();
        List<SleepData> sleepDataList = new ArrayList<>();
        List<HeartData> heartDataList = new ArrayList<>();
        List<BloodPressureData> bloodPressureDataList = new ArrayList<>();
        List<BloodOxygenData> bloodOxygenDataList = new ArrayList<>();
        List<EcgData> ecgDataList = new ArrayList<>();
        List<SportData> sportDataList = new ArrayList<>();
        List<AnimalHeatData> animalHeatDataList = new ArrayList<>();
        switch (type){
            case "step":
                stepDataList = SQLite.select()
                        .from(StepData.class)
                        .queryList();
                for (StepData stepData:stepDataList){
                    if (stepData.steps>0)
                        dataList.add(new LocalDate(DateUtil.getStringDateFromSecond(stepData.time,"yyyy-MM-dd")));
                }
                break;
            case "sleep":
                sleepDataList = SQLite.select()
                        .from(SleepData.class)
                        .queryList();

                for (SleepData sleepData:sleepDataList){
                    if ((sleepData.lightTime+sleepData.deepTime)>0)
                        dataList.add(new LocalDate(DateUtil.getStringDateFromSecond(sleepData.time,"yyyy-MM-dd")));
                }
                break;
            case "heart":
                heartDataList = SQLite.select()
                        .from(HeartData.class)
                        .queryList();
                for (HeartData heartData:heartDataList){
                    if (heartData.averageHeart>0)
                        dataList.add(new LocalDate(DateUtil.getStringDateFromSecond(heartData.time,"yyyy-MM-dd")));
                }
                break;
            case "bp":
                bloodPressureDataList = SQLite.select()
                        .from(BloodPressureData.class)
                        .queryList();
                for (BloodPressureData bloodPressureData:bloodPressureDataList){
                    LocalDate localDate = new LocalDate(DateUtil.getStringDateFromSecond(bloodPressureData.time,"yyyy-MM-dd"));
                    if (!dataList.contains(localDate))
                        dataList.add(localDate);
                }
                break;
            case "bo":
                bloodOxygenDataList = SQLite.select()
                        .from(BloodOxygenData.class)
                        .queryList();
                for (BloodOxygenData bloodOxygenData:bloodOxygenDataList){
                    LocalDate localDate = new LocalDate(DateUtil.getStringDateFromSecond(bloodOxygenData.time,"yyyy-MM-dd"));
                    if (!dataList.contains(localDate))
                        dataList.add(localDate);
                }
                break;
            case "ecg":
                ecgDataList = SQLite.select()
                        .from(EcgData.class)
                        .queryList();
                for (EcgData ecgData:ecgDataList){
                    LocalDate localDate = new LocalDate(DateUtil.getStringDateFromSecond(ecgData.time,"yyyy-MM-dd"));
                    if (!dataList.contains(localDate))
                        dataList.add(localDate);
                }
                break;
            case "sport":
                sportDataList = SQLite.select()
                        .from(SportData.class)
                        .queryList();
                for (SportData sportData:sportDataList){
                    LocalDate localDate = new LocalDate(DateUtil.getStringDateFromSecond(sportData.time,"yyyy-MM-dd"));
                    if (!dataList.contains(localDate))
                        dataList.add(localDate);
                }
                break;
            case "temp":
                animalHeatDataList = SQLite.select()
                        .from(AnimalHeatData.class)
                        .queryList();
                for (AnimalHeatData animalHeatData:animalHeatDataList){
                    LocalDate localDate = new LocalDate(DateUtil.getStringDateFromSecond(animalHeatData.time,"yyyy-MM-dd"));
                    if (!dataList.contains(localDate))
                        dataList.add(localDate);
                }
                break;
        }

        CalendarUtil.setPointList(dataList);
    }


    /**
     * 判断是否支持运动
     * */
    public boolean getSportConfig(int deviceNum){

        SportWatchAppFunctionConfigDTO sportWatchAppFunctionConfigDTO = SQLite.select()
                .from(SportWatchAppFunctionConfigDTO.class)
                .where(SportWatchAppFunctionConfigDTO_Table.identifier.is(deviceNum))
                .querySingle();

        return sportWatchAppFunctionConfigDTO==null?false:sportWatchAppFunctionConfigDTO.getMultiSport()==1;

    }

    public HealthyConfig getConfig(int deviceNum){
        Log.d("DATA******","获取ID = "+deviceNum);
        HealthyConfig config = SQLite.select()
                .from(HealthyConfig.class)
                .where(HealthyConfig_Table.identifier.is(deviceNum))
                .querySingle();
        Log.d("DATA******","获取ID = "+config);
        return config;
    }

    /**
     * 获取支持的蓝牙名
     * */
    public ArrayList<String> getBleNameConfig(){
        List<SportWatchAppFunctionConfigDTO> sportWatchAppFunctionConfigDTOs = SQLite.select()
                .from(SportWatchAppFunctionConfigDTO.class)
                .queryList();
        ArrayList<String> datas = new ArrayList<>();
        for (SportWatchAppFunctionConfigDTO sportWatchAppFunctionConfigDTO:sportWatchAppFunctionConfigDTOs){
            datas.add(sportWatchAppFunctionConfigDTO.appName);
        }
        return datas;
    }

    /**
     * 判断是否支持mtk蓝牙库
     * */
    public SportWatchAppFunctionConfigDTO getDeviceConfig(String deviceName){
        if (deviceName!=null){
            SportWatchAppFunctionConfigDTO sportWatchAppFunctionConfigDTO = SQLite.select()
                    .from(SportWatchAppFunctionConfigDTO.class)
                    .where(SportWatchAppFunctionConfigDTO_Table.appName.is(deviceName))
                    .querySingle();
            return sportWatchAppFunctionConfigDTO==null?new SportWatchAppFunctionConfigDTO():sportWatchAppFunctionConfigDTO;
        }
        return null;
    }

    /**
     * 取计划表数据
     * */
    public List<ScheduleData> getScheduleData(){
        List<ScheduleData> list = SQLite.select()
                .from(ScheduleData.class)
                .orderBy(OrderBy.fromString(ScheduleData_Table.time+OrderBy.DESCENDING))
                .queryList();
        return list;
    }

    public boolean scheduleCanAdd(long time){
        ScheduleData data = SQLite.select()
                .from(ScheduleData.class)
                .where(ScheduleData_Table.time.is(time))
                .querySingle();

        return data==null;
    }

    /**
     * 求最大值最小值
     * */
    private int[] getMaxMin(ArrayList<DrawDataBean> dataBeans){
        int data[] = new int[2];
        int min = 1000,max = 0;
        for (int i=0;i<dataBeans.size();i++){
            if (min>dataBeans.get(i).getValue()&&dataBeans.get(i).getValue()!=0)
                min = dataBeans.get(i).getValue();
            if (max<dataBeans.get(i).getValue()&&dataBeans.get(i).getValue()!=0)
                max = dataBeans.get(i).getValue();
            Log.d("DATA******","max = "+max+" ;data = "+dataBeans.get(i).getValue());
        }
        data[0] = max;
        data[1] = min==1000?0:min;
        return data;
    }

    /**
     * 计算达标率
     * */
    private int getReach(ArrayList<DrawDataBean> dataBeans,int plan){
        int sum = 0;
        int sum1 = 0;//不为0的个数
        for (int i=0;i<dataBeans.size();i++){
            if (dataBeans.get(i).getValue()!=0)
                sum1++;
            if (dataBeans.get(i).getValue()>=plan)
                sum++;
        }
        return sum1==0?0:(int)((float)sum/sum1*1000);
    }



    /**
     * 求总和
     * */
    private int[] getSum(ArrayList<DrawDataBean> dataBeans){
        int data[] = new int[2];
        int sum = 0,sum1 = 0;
        for (int i=0;i<dataBeans.size();i++){
            sum+=dataBeans.get(i).getValue();
            sum1+=dataBeans.get(i).getValue1();
        }
        data[0] = sum;
        data[1] = sum1;
        return data;
    }

    /**
     * 求平均数
     * */
    private int[] getAverage(ArrayList<DrawDataBean> dataBeans){
        int [] data = new int[2];
        int a = 0,b = 0,sum = 0,sum1 = 0;
        for (int i=0;i<dataBeans.size();i++){
            if (dataBeans.get(i).getValue()!=0){
                a++;
                sum+=dataBeans.get(i).getValue();
            }
            if (dataBeans.get(i).getValue1()!=0){
                b++;
                sum1+=dataBeans.get(i).getValue1();
            }
        }
        if (a!=0)
            data[0] = sum/a;
        if (b!=0)
            data[1] = sum1/b;

        return data;
    }

    public List<NotificationData> getNotificationList(){
        List<NotificationData> list = SQLite.select()
                .from(NotificationData.class)
                .queryList();
        return list;
    }

    public NotificationData getNotification(String packageName){
        NotificationData notificationData = SQLite.select()
                .from(NotificationData.class)
                .where(NotificationData_Table.packageName.is(packageName))
                .querySingle();
        return notificationData;
    }

    public boolean needNotify(String packageName){
       NotificationData notificationData = SQLite.select()
                .from(NotificationData.class)
               .where(NotificationData_Table.packageName.is(packageName))
               .querySingle();
       if (notificationData == null)
           return false;
        return notificationData.state;
    }

    public String getNotifyName(String packageName){
        NotificationData notificationData = SQLite.select()
                .from(NotificationData.class)
                .where(NotificationData_Table.packageName.is(packageName))
                .querySingle();
        if (notificationData == null)
            return "";
        return notificationData.name;
    }

}
