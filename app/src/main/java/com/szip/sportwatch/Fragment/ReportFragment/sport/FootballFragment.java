package com.szip.sportwatch.Fragment.ReportFragment.sport;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.szip.sportwatch.Fragment.BaseFragment;
import com.szip.sportwatch.DB.dbModel.SportData;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.DateUtil;
import com.szip.sportwatch.View.SportReportView;

import java.util.Locale;

public class FootballFragment extends BaseFragment {

    private TextView timeTv,sportTimeTv,kcalTv,averageTv1;
    private SportReportView tableView1;
    private SportData sportData;

    private String[] heartArray = new String[0];

    public FootballFragment(SportData sportData) {
        this.sportData = sportData;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_football;
    }

    @Override
    protected void afterOnCreated(Bundle savedInstanceState) {
        initView();
        initData();
    }

    private void initView() {
        timeTv = getView().findViewById(R.id.timeTv);
        sportTimeTv = getView().findViewById(R.id.sportTimeTv);
        kcalTv = getView().findViewById(R.id.kcalTv);
        averageTv1 = getView().findViewById(R.id.averageTv1);
        tableView1 = getView().findViewById(R.id.tableView1);
    }

    private void initData() {
        heartArray = sportData.getHeartArray().split(",");
        timeTv.setText(DateUtil.getStringDateFromSecond(sportData.time,"MM/dd HH:mm:ss"));
        sportTimeTv.setText(String.format(Locale.ENGLISH,"%02d:%02d:%02d",sportData.sportTime/3600,
                sportData.sportTime%3600/60,sportData.sportTime%3600%60));
        kcalTv.setText(String.format(Locale.ENGLISH,"%.1f",((sportData.calorie+55)/100)/10f));
        averageTv1.setText(sportData.heart+"");
        tableView1.addData(heartArray);

        if (sportData.heart==0)
            getView().findViewById(R.id.heartLl).setVisibility(View.GONE);
    }

}
