package com.szip.sportwatch.Fragment.ReportFragment.sport;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.szip.sportwatch.Fragment.BaseFragment;
import com.szip.sportwatch.DB.dbModel.SportData;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.DateUtil;
import com.szip.sportwatch.Util.IMapUtil;
import com.szip.sportwatch.Util.MapUtilGaodeImp;
import com.szip.sportwatch.Util.MapUtilGoogleImp;
import com.szip.sportwatch.Util.MathUitl;
import com.szip.sportwatch.View.MyRelativeLayout;
import com.szip.sportwatch.View.MyScrollView;
import com.szip.sportwatch.View.SportReportView;
import com.szip.sportwatch.View.SportSpeedView;
import java.util.Locale;

public class RunFragment extends BaseFragment implements OnMapReadyCallback {

    private MyScrollView myScrollView;
    private RelativeLayout bgRl;
    private MyRelativeLayout mapRl;
    private MapView mapView;
    private com.amap.api.maps.MapView gaodeView;
    private IMapUtil iMapUtil;
    private View mapBackView;

    private TextView dataTv,timeTv,distanceTv,unitTv,kcalTv,sportTimeTv,stepTv,averageTv1, averageTv2,averageTv3,averageTv4,averageTv5;
    private SportReportView tableView1, tableView2,tableView3, tableView4;
    private SportData sportData;
    private SportSpeedView sportSpeed;

    private String[] heartArray = new String[0];
    private String[] strideArray = new String[0];
    private String[] speedArray = new String[0];
    private String[] speedPerHourArray = new String[0];
    private String[] altitudeArray = new String[0];

    public RunFragment(SportData sportData) {
        this.sportData = sportData;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_run;
    }

    @Override
    protected void afterOnCreated(Bundle savedInstanceState) {
        initView(savedInstanceState);
        initData();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (iMapUtil!=null)
            iMapUtil.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (iMapUtil!=null)
            iMapUtil.onDestroy();
    }

    private void initView(Bundle savedInstanceState) {
        myScrollView = getView().findViewById(R.id.scrollId);
        bgRl = getView().findViewById(R.id.bgRl);
        mapRl = getView().findViewById(R.id.mapRl);
        gaodeView = getView().findViewById(R.id.gaodeMap);
        mapView = getView().findViewById(R.id.googleMap);
        mapBackView = getView().findViewById(R.id.mapBackView);
        mapBackView.getBackground().setAlpha(0);

        timeTv = getView().findViewById(R.id.timeTv);
        sportTimeTv = getView().findViewById(R.id.sportTimeTv);
        distanceTv = getView().findViewById(R.id.distanceTv);
        unitTv = getView().findViewById(R.id.unitTv);
        kcalTv = getView().findViewById(R.id.kcalTv);
        stepTv = getView().findViewById(R.id.stepTv);
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

        if(sportData.type==6){
            ((TextView)getView().findViewById(R.id.sportIdTv)).setText(R.string.training);
            bgRl.setBackground(getView().getResources().getDrawable(R.drawable.sport_bg_purple));
            ((ImageView)getView().findViewById(R.id.bgIv)).setImageResource(R.mipmap.sport_bg_trainrun);
        }
        if (sportData.latArray!=null&&!sportData.latArray.equals("")){
            if (getResources().getConfiguration().locale.getCountry().equals("CN")) {
                mapView.setVisibility(View.GONE);
                iMapUtil = new MapUtilGaodeImp(gaodeView);
                iMapUtil.onCreate(savedInstanceState);
                makeLine();
            } else {
                gaodeView.setVisibility(View.GONE);
                mapView.onCreate(getArguments());
                mapView.onResume();
                try {
                    MapsInitializer.initialize(getActivity());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
                if (ConnectionResult.SUCCESS != errorCode) {
                    GooglePlayServicesUtil.getErrorDialog(errorCode, getActivity(), 0).show();
                } else {
                    mapView.getMapAsync(this);
                }
            }
            myScrollView.setOnScrollListener(listener);
            mapRl.setScrollView(myScrollView,gaodeView,mapView);
        }else {
            getView().findViewById(R.id.mapViewtop).setVisibility(View.GONE);
        }
    }

    private void initData() {
        heartArray = sportData.getHeartArray().split(",");
        strideArray = sportData.getStrideArray().split(",");
        speedArray = sportData.getSpeedArray().split(",");
        speedPerHourArray = sportData.getSpeedPerHourArray().split(",");
        altitudeArray = sportData.getAltitudeArray().split(",");
        stepTv.setText(sportData.step+"");
        timeTv.setText(DateUtil.getStringDateFromSecond(sportData.time,"MM/dd HH:mm:ss"));
        sportTimeTv.setText(String.format(Locale.ENGLISH,"%02d:%02d:%02d",sportData.sportTime/3600,
                sportData.sportTime%3600/60,sportData.sportTime%3600%60));
        if (MyApplication.getInstance().getUserInfo().getUnit()==0){
            distanceTv.setText(String.format(Locale.ENGLISH,"%.2f",sportData.distance/1000f));
            unitTv.setText("km");
        } else{
            distanceTv.setText(String.format(Locale.ENGLISH,"%.2f", MathUitl.metric2Miles(sportData.distance*10)));
            unitTv.setText("mile");
        }
        kcalTv.setText(String.format(Locale.ENGLISH,"%.1f",((sportData.calorie+55)/100)/10f));
        stepTv.setText(String.format(Locale.ENGLISH,"%d", sportData.step));
        averageTv1.setText(sportData.heart+"");
        averageTv2.setText(sportData.stride+"");
        averageTv3.setText(String.format(Locale.ENGLISH,"%.1f",sportData.speedPerHour/10f));
        averageTv4.setText(sportData.height+"");
        averageTv5.setText(String.format(Locale.ENGLISH,"%02d'%02d''",sportData.speed/60,sportData.speed%60));
        tableView1.addData(heartArray);
        tableView2.addData(strideArray);
        tableView3.addData(speedPerHourArray);
        tableView4.addData(altitudeArray);
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

    private void makeLine(){
        String[] lats = sportData.latArray.split(",");
        String[] lngs = sportData.lngArray.split(",");
//        String[] lats = {"22541163","220","110"};
//        String[] lngs = {"113949629","203","550"};
        iMapUtil.setLatlng(lats,lngs);
        iMapUtil.moveCamera();
        iMapUtil.addMarker();
        iMapUtil.addPolyline();
    }

    private MyScrollView.OnScrollListener listener = new MyScrollView.OnScrollListener() {
        @Override
        public void onScroll(int scrollY) {
            int alpha = 0;
            alpha = scrollY/(bgRl.getTop()/255);
            if (alpha>255)
                alpha = 255;
            mapBackView.getBackground().setAlpha(alpha);
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        iMapUtil = new MapUtilGoogleImp(googleMap);
        makeLine();
    }
}
