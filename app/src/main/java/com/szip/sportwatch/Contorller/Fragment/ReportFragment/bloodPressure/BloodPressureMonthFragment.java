package com.szip.sportwatch.Contorller.Fragment.ReportFragment.bloodPressure;

import android.os.Bundle;
import android.widget.TextView;

import com.szip.sportwatch.Contorller.BloodPressureReportActivity;
import com.szip.sportwatch.Contorller.Fragment.BaseFragment;
import com.szip.sportwatch.DB.LoadDataUtil;
import com.szip.sportwatch.Model.EvenBusModel.UpdateReport;
import com.szip.sportwatch.Model.ReportDataBean;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.DateUtil;
import com.szip.sportwatch.View.ReportView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by Administrator on 2019/12/18.
 */

public class BloodPressureMonthFragment extends BaseFragment{

    private ReportView reportView;
    private TextView averageSbpTv,averageDbpTv;
    private ReportDataBean reportDataBean;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_blood_pressure_month;
    }

    @Override
    protected void afterOnCreated(Bundle savedInstanceState) {
        initData();
        initView();
        updateView();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    private void updateView() {
        reportView.setReportDate(((BloodPressureReportActivity)getActivity()).reportDate);
        reportView.addData(reportDataBean.getDrawDataBeans());
        averageSbpTv.setText(reportDataBean.getValue()+45+"mmHg");
        averageDbpTv.setText(reportDataBean.getValue1()+45+"mmHg");
        if (DateUtil.getTimeOfToday()==((BloodPressureReportActivity)getActivity()).reportDate)
            ((TextView)getView().findViewById(R.id.dateTv)).setText(DateUtil.getStringDateFromSecond(
                    ((BloodPressureReportActivity)getActivity()).reportDate-27*24*60*60,"yyyy/MM/dd")+
                    "~"+getString(R.string.today));
        else
            ((TextView)getView().findViewById(R.id.dateTv)).setText(
                    DateUtil.getStringDateFromSecond(
                            ((BloodPressureReportActivity)getActivity()).reportDate-27*24*60*60,"yyyy/MM/dd")+
                            "~"
                            +DateUtil.getStringDateFromSecond(
                            ((BloodPressureReportActivity)getActivity()).reportDate,"yyyy/MM/dd"
                    ));
    }

    private void initData() {
        reportDataBean = LoadDataUtil.newInstance().getBloodPressureWithMonth(((BloodPressureReportActivity)getActivity()).reportDate);
    }

    private void initView() {
        reportView = getView().findViewById(R.id.tableView);
        reportView.setReportDate(0);
        averageSbpTv = getView().findViewById(R.id.sbpTv);
        averageDbpTv = getView().findViewById(R.id.dbpTv);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateReport(UpdateReport updateReport){
        initData();
        updateView();
    }
}
