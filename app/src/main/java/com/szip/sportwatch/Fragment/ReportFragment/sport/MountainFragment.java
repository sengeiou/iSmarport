package com.szip.sportwatch.Fragment.ReportFragment.sport;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.szip.sportwatch.Fragment.BaseFragment;
import com.szip.sportwatch.DB.dbModel.SportData;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.DateUtil;
import com.szip.sportwatch.Util.MathUitl;
import com.szip.sportwatch.View.SportReportView;
import com.szip.sportwatch.View.SportSpeedView;

import java.util.Locale;

public class MountainFragment extends BaseFragment {

    private TextView timeTv,dataTv,sportTimeTv,kcalTv,distanceTv,unitTv,averageTv1,averageTv2,averageTv3,averageTv4,averageTv5;
    private SportReportView tableView1,tableView2,tableView3,tableView4;
    private SportSpeedView sportSpeed;
    private SportData sportData;

    private String[] heartArray = new String[0];
    private String[] altitudeArray = new String[0];
    private String[] strideArray = new String[0];
    private String[] speedPerHourArray = new String[0];
    private String[] speedArray = new String[0];

    public MountainFragment(SportData sportData) {
        this.sportData = sportData;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_mountain;
    }

    @Override
    protected void afterOnCreated(Bundle savedInstanceState) {
        initView();
        initData();
    }

    private void initView() {
        timeTv = getView().findViewById(R.id.timeTv);
        dataTv = getView().findViewById(R.id.dataTv);
        distanceTv = getView().findViewById(R.id.distanceTv);
        unitTv = getView().findViewById(R.id.unitTv);
        sportTimeTv = getView().findViewById(R.id.sportTimeTv);
        kcalTv = getView().findViewById(R.id.kcalTv);
        averageTv1 = getView().findViewById(R.id.averageTv1);
        averageTv2 = getView().findViewById(R.id.averageTv2);
        averageTv3 = getView().findViewById(R.id.averageTv3);
        averageTv4 = getView().findViewById(R.id.averageTv4);
        averageTv5 = getView().findViewById(R.id.averageTv5);
        tableView1 = getView().findViewById(R.id.tableView1);
        tableView2 = getView().findViewById(R.id.tableView2);
        tableView3 = getView().findViewById(R.id.tableView3);
        tableView4 = getView().findViewById(R.id.tableView4);
        sportSpeed = getView().findViewById(R.id.sportSpeed);
    }

    private void initData() {
        heartArray = sportData.getHeartArray().split(",");
        altitudeArray = sportData.getAltitudeArray().split(",");
        strideArray = sportData.getStrideArray().split(",");
        speedPerHourArray = sportData.getSpeedPerHourArray().split(",");
        speedArray = sportData.getSpeedArray().split(",");
        timeTv.setText(DateUtil.getStringDateFromSecond(sportData.time,"MM/dd HH:mm:ss"));
        dataTv.setText(sportData.step+"");
        if (MyApplication.getInstance().getUserInfo().getUnit()==0){
            distanceTv.setText(String.format(Locale.ENGLISH,"%.2f",sportData.distance/1000f));
            unitTv.setText("km");
        } else{
            distanceTv.setText(String.format(Locale.ENGLISH,"%.2f", MathUitl.metric2Miles(sportData.distance*10)));
            unitTv.setText("mile");
        }
        sportTimeTv.setText(String.format(Locale.ENGLISH,"%02d:%02d:%02d",sportData.sportTime/3600,
                sportData.sportTime%3600/60,sportData.sportTime%3600%60));
        kcalTv.setText(String.format(Locale.ENGLISH,"%.1f",((sportData.calorie+55)/100)/10f));
        averageTv1.setText(sportData.heart+"");
        averageTv2.setText(sportData.height+"");
        averageTv3.setText(sportData.stride+"");
        averageTv4.setText(String.format(Locale.ENGLISH,"%02d'%02d''",sportData.speed/60,sportData.speed%60));
        averageTv5.setText(String.format(Locale.ENGLISH,"%.1f",sportData.speedPerHour/10f));
        tableView1.addData(heartArray);
        tableView2.addData(altitudeArray);
        tableView3.addData(strideArray);
        tableView4.addData(speedPerHourArray);
        sportSpeed.addData(speedArray);


        if(sportData.heart==0){
            getView().findViewById(R.id.heartLl).setVisibility(View.GONE);
        }
        if(sportData.height==0){
            getView().findViewById(R.id.altitudeLl).setVisibility(View.GONE);
        }
        if(sportData.stride==0){
            getView().findViewById(R.id.strideLl).setVisibility(View.GONE);
        }
        if(sportData.speed==0){
            getView().findViewById(R.id.speedLl).setVisibility(View.GONE);
        }
        if(sportData.speedPerHour==0){
            getView().findViewById(R.id.speedPerHourLl).setVisibility(View.GONE);
        }
    }
}
