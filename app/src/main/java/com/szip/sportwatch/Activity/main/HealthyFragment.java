package com.szip.sportwatch.Activity.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.szip.sportwatch.BLE.BleClient;
import com.szip.sportwatch.BLE.EXCDController;
import com.szip.sportwatch.Activity.EcgListActivity;
import com.szip.sportwatch.Activity.report.ReportActivity;
import com.szip.sportwatch.Activity.userInfo.UserInfoActivity;
import com.szip.sportwatch.DB.LoadDataUtil;
import com.szip.sportwatch.DB.dbModel.HealthyConfig;
import com.szip.sportwatch.Fragment.BaseFragment;
import com.szip.sportwatch.Interface.MyListener;
import com.szip.sportwatch.Model.EvenBusModel.ConnectState;
import com.szip.sportwatch.Model.EvenBusModel.PlanModel;
import com.szip.sportwatch.Model.HealthyDataModel;
import com.szip.sportwatch.Model.HttpBean.WeatherBean;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.DateUtil;
import com.szip.sportwatch.Util.HttpMessgeUtil;
import com.szip.sportwatch.Util.JsonGenericsSerializator;
import com.szip.sportwatch.Util.LocationUtil;
import com.szip.sportwatch.Util.LogUtil;
import com.szip.sportwatch.Util.MathUitl;
import com.szip.sportwatch.Util.ViewUtil;
import com.szip.sportwatch.View.CircularImageView;
import com.szip.sportwatch.View.ColorArcProgressBar;
import com.szip.sportwatch.View.HealthyProgressView;
import com.szip.sportwatch.View.PullToRefreshLayout;
import com.zhy.http.okhttp.callback.GenericsCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.Call;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static com.szip.sportwatch.MyApplication.FILE;

/**
 * Created by Administrator on 2019/12/1.
 */

public class HealthyFragment extends BaseFragment implements View.OnClickListener{

    private MyApplication app;
    /**
     * 计步圈
     * */
    private ColorArcProgressBar stepPb;
    /**
     * 健康游标控件
     * */
    private HealthyProgressView sleepPv;

    /**
     * 健康数据相关控件
     * */
    private TextView stepRadioTv,planStepTv,stepTv,distanceTv,kcalTv;
    private TextView sleepDataTv;
    private TextView heartDataTv;
    private TextView bloodDataTv;
    private TextView bloodODataTv;
    private TextView animalHeatDataTv;
    private TextView ecgDataTv;
    private TextView tempTv,conditionTv;
    private ImageView weatherIv;

    /**
     * 最近一次健康数据的model
     * */
    private HealthyDataModel healthyDataModel;

    private ViewUtil viewUtil;

    private Intent intent;

    private HealthyConfig healthyConfig;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_healthy;
    }

    @Override
    protected void afterOnCreated(Bundle savedInstanceState) {
        app = (MyApplication) getActivity().getApplicationContext();
        intent = new Intent(getActivity(), ReportActivity.class);
        initView();
        initEvent();
        initWeather();
    }

    private void initWeather() {
        updataWeatherView();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(FILE,MODE_PRIVATE);
        long time = sharedPreferences.getLong("weatherTime",0);
        if (Calendar.getInstance().getTimeInMillis()-time>60*60*1000){
            LogUtil.getInstance().logd("LOCATION******","开始定位");
            locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
            LocationUtil.getInstance().getLocation(locationManager,true,myListener,locationListener);
        }
    }


    private void updataWeatherView() {
        if(app.getWeatherModel()!=null&&app.getCity()!=null){
            if(app.getUserInfo().getTempUnit()==0)
                tempTv.setText(String.format(Locale.ENGLISH,"%d~%d℃",(int)app.getWeatherModel().get(0).getLow(),(int)app.getWeatherModel().get(0).getHigh()));
            else
                tempTv.setText(String.format(Locale.ENGLISH,"%d~%d℉",(int)MathUitl.c2f(app.getWeatherModel().get(0).getLow()),
                        (int)MathUitl.c2f(app.getWeatherModel().get(0).getHigh())));
            Glide.with(getActivity()).load(app.getWeatherModel().get(0).getIconUrl()).into(weatherIv);
            conditionTv.setText(app.getCity()+" "+app.getWeatherModel().get(0).getText());
            conditionTv.setSelected(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        initData();
        if (conditionTv!=null)
            conditionTv.setSelected(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }


    private void initView() {
        ((PullToRefreshLayout) getView().findViewById(R.id.refresh_view))
                .setOnRefreshListener(new MyListener());
        healthyConfig = LoadDataUtil.newInstance().getConfig(Integer.valueOf(app.getDeviceNum()));
        if (healthyConfig!=null){
            if(healthyConfig.sleep!=1){
                getView().findViewById(R.id.sleepRl).setVisibility(View.GONE);
            }else {
                getView().findViewById(R.id.sleepRl).setVisibility(View.VISIBLE);
            }

            if(healthyConfig.heartRate!=1){
                getView().findViewById(R.id.heartRl).setVisibility(View.GONE);
            }else {
                getView().findViewById(R.id.heartRl).setVisibility(View.VISIBLE);
            }

            if(healthyConfig.bloodPressure!=1){
                getView().findViewById(R.id.bloodPressureRl).setVisibility(View.GONE);
            }else {
                getView().findViewById(R.id.bloodPressureRl).setVisibility(View.VISIBLE);
            }

            if(healthyConfig.bloodOxygen!=1){
                getView().findViewById(R.id.bloodOxygenRl).setVisibility(View.GONE);
            }else {
                getView().findViewById(R.id.bloodOxygenRl).setVisibility(View.VISIBLE);
            }

            if(healthyConfig.ecg!=1){
                getView().findViewById(R.id.ecgRl).setVisibility(View.GONE);
            }else {
                getView().findViewById(R.id.ecgRl).setVisibility(View.VISIBLE);
            }

            if(healthyConfig.temperature!=1){
                getView().findViewById(R.id.animalHeatRl).setVisibility(View.GONE);
            }else {
                getView().findViewById(R.id.animalHeatRl).setVisibility(View.VISIBLE);
            }
        }

        stepPb = getView().findViewById(R.id.stepPb);
        sleepPv = getView().findViewById(R.id.sleepPv);

        stepRadioTv = getView().findViewById(R.id.stepRadioTv);
        planStepTv = getView().findViewById(R.id.planStepTv);
        stepTv = getView().findViewById(R.id.stepTv);
        distanceTv = getView().findViewById(R.id.dataTv);
        kcalTv = getView().findViewById(R.id.kcalTv);

        sleepDataTv = getView().findViewById(R.id.sleepDataTv);

        heartDataTv = getView().findViewById(R.id.heartDataTv);

        bloodDataTv = getView().findViewById(R.id.bloodDataTv);

        bloodODataTv = getView().findViewById(R.id.bloodODataTv);

        animalHeatDataTv = getView().findViewById(R.id.animalHeatDataTv);

        ecgDataTv = getView().findViewById(R.id.ecgDataTv);

        weatherIv = getView().findViewById(R.id.weatherIv);

        conditionTv = getView().findViewById(R.id.conditionTv);

        tempTv = getView().findViewById(R.id.tempTv);
    }

    private void initEvent() {
        getView().findViewById(R.id.pictureIv).setOnClickListener(this);
        getView().findViewById(R.id.weatherIv).setOnClickListener(this);
        getView().findViewById(R.id.stepRl).setOnClickListener(this);
        getView().findViewById(R.id.sleepRl).setOnClickListener(this);
        getView().findViewById(R.id.heartRl).setOnClickListener(this);
        getView().findViewById(R.id.bloodPressureRl).setOnClickListener(this);
        getView().findViewById(R.id.bloodOxygenRl).setOnClickListener(this);
        getView().findViewById(R.id.animalHeatRl).setOnClickListener(this);
        getView().findViewById(R.id.ecgRl).setOnClickListener(this);
        getView().findViewById(R.id.weatherLl).setOnClickListener(this);
    }

    @SuppressLint("StringFormatInvalid")
    private void initData() {
        viewUtil = new ViewUtil(getContext());
        healthyDataModel = LoadDataUtil.newInstance().getHealthyDataLast(DateUtil.getTimeOfToday());

        if (app.getUserInfo()!=null){
            stepPb.setMaxValues(app.getUserInfo().getStepsPlan());
            stepPb.setCurrentValues(healthyDataModel.getStepsData());
            stepTv.setText(healthyDataModel.getStepsData()+"");
            if (app.getUserInfo().getUnit()==0){
                distanceTv.setText(String.format(Locale.ENGLISH,"%.2f",(healthyDataModel.getDistanceData()+55)/100/100f));
                ((TextView)getView().findViewById(R.id.unitTv)).setText("km");
            } else{
                distanceTv.setText(String.format(Locale.ENGLISH,"%.2f",MathUitl.km2Miles(healthyDataModel.getDistanceData()/10)));
                ((TextView)getView().findViewById(R.id.unitTv)).setText("mile");
            }
            kcalTv.setText(String.format(Locale.ENGLISH,"%.1f",((healthyDataModel.getKcalData()+55)/100)/10f));
            planStepTv.setText(String.format(Locale.ENGLISH,getString(R.string.planStep),app.getUserInfo().getStepsPlan()));
            stepRadioTv.setText(String.format(Locale.ENGLISH,"%.1f%%",(healthyDataModel.getStepsData()
                    /(float)app.getUserInfo().getStepsPlan()*100)>100?100:healthyDataModel.getStepsData()
                    /(float)app.getUserInfo().getStepsPlan()*100));
        }

        if (healthyDataModel.getAllSleepData()!=0){
            sleepDataTv.setText(String.format(Locale.ENGLISH,"%dh%dmin/%dh%dmin",
                    healthyDataModel.getAllSleepData()/60,healthyDataModel.getAllSleepData()%60,
                    app.getUserInfo().getSleepPlan()/60,app.getUserInfo().getSleepPlan()%60));
        }else{
            sleepDataTv.setText(String.format(Locale.ENGLISH,"0h0min/%dh%dmin",
                    app.getUserInfo().getSleepPlan()/60,app.getUserInfo().getSleepPlan()%60));
        }
        sleepPv.setSleepData(healthyDataModel.getAllSleepData()/(float)app.getUserInfo().getSleepPlan(),
                healthyDataModel.getAllSleepData(),healthyDataModel.getLightSleepData());
        viewUtil.setSleepView(healthyDataModel.getAllSleepData(),getView().findViewById(R.id.sleepStateTv));

        if (healthyDataModel.getHeartData()!=0){
            heartDataTv.setText(healthyDataModel.getHeartData()+"bpm");
        }else {
            heartDataTv.setText("--bpm");
        }
        viewUtil.setHeartView(healthyDataModel.getHeartData(), getView().findViewById(R.id.heartStateTv),
                getView().findViewById(R.id.heartPv));


        if (healthyDataModel.getSbpData()!=0){
            bloodDataTv.setText(String.format(Locale.ENGLISH,"%d/%dmmHg",healthyDataModel.getSbpData(),healthyDataModel.getDbpData()));
        }else {
            bloodDataTv.setText("--/--mmHg");
        }
        viewUtil.setBloodPressureView(healthyDataModel.getSbpData(),healthyDataModel.getDbpData(),
                getView().findViewById(R.id.bloodStateTv),getView().findViewById(R.id.sbpPv),getView().findViewById(R.id.dbpPv));


        if (healthyDataModel.getBloodOxygenData()!=0){
            bloodODataTv.setText(healthyDataModel.getBloodOxygenData()+"%");
        }else {
            bloodODataTv.setText("--%");
        }
        viewUtil.setBloodOxygenView(healthyDataModel.getBloodOxygenData(),getView().findViewById(R.id.bloodOStateTv),
                getView().findViewById(R.id.bloodOxygenPv));


        if (healthyDataModel.getAnimalHeatData()!=0){
            if (app.getUserInfo().getTempUnit()==0)
                animalHeatDataTv.setText(String.format(Locale.ENGLISH,"%.1f℃",healthyDataModel.getAnimalHeatData()/10f));
            else
                animalHeatDataTv.setText(String.format(Locale.ENGLISH,"%.1f℉",MathUitl.c2f(healthyDataModel.getAnimalHeatData()/10f)));
        }else {
            animalHeatDataTv.setText("--");
        }
        viewUtil.setAnimalHeatView(healthyDataModel.getAnimalHeatData(),getView().findViewById(R.id.animalHeatStateTv),
                getView().findViewById(R.id.animalHeatPv));


        if (healthyDataModel.getEcgData()!=0){
            ecgDataTv.setText(healthyDataModel.getEcgData()+"bpm");
        }else {
            ecgDataTv.setText("--bpm");
        }


        Glide.with(this).load(app.getUserInfo().getAvatar())
                .fallback(R.mipmap.my_head_male_52)
                .error(R.mipmap.my_head_male_52)
                .into( ((CircularImageView)getView().findViewById(R.id.pictureIv)));

    }


    /**
     * 更新数据显示
     * */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdataView(ConnectState connectBean){
            initData();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdatePlan(PlanModel planModel){
        if(planStepTv!=null){
            try {
                HttpMessgeUtil.getInstance().postForSetStepsPlan(planModel.getData()+"",1);
                planStepTv.setText(String.format(Locale.ENGLISH,getString(R.string.planStep),planModel.getData()));
            } catch (IOException e) {
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.pictureIv:
                if (app.getUserInfo().getPhoneNumber()==null&&app.getUserInfo().getEmail()==null)
                    showToast(getString(R.string.visiter));
                else
                    startActivity(new Intent(getActivity(), UserInfoActivity.class));
                break;
            case R.id.stepRl:
                intent.putExtra("type","step");
                startActivity(intent);
                break;
            case R.id.sleepRl:
                intent.putExtra("type","sleep");
                startActivity(intent);
                break;
            case R.id.heartRl:
                intent.putExtra("type","heart");
                startActivity(intent);
                break;
            case R.id.bloodPressureRl:
                intent.putExtra("type","bp");
                startActivity(intent);
                break;
            case R.id.bloodOxygenRl:
                intent.putExtra("type","bo");
                startActivity(intent);
                break;
            case R.id.animalHeatRl:
                intent.putExtra("type","temp");
                startActivity(intent);
                break;
            case R.id.ecgRl:
                startActivity(new Intent(getActivity(), EcgListActivity.class));
                break;
            case R.id.weatherLl:

                    locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
                    LocationUtil.getInstance().getLocation(locationManager,true,myListener,locationListener);
                    getView().findViewById(R.id.weatherLl).setClickable(false);

                break;
        }
    }

    private Sensor mPressure = null;
    private SensorManager sensorManager = null;

    private LocationManager locationManager;
    private LocationProvider locationProvider;
    private GpsStatus.Listener myListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int i) {

        }
    };

    //监听GPS位置改变后得到新的经纬度
    private LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
            if (location != null) {
                //获取国家，省份，城市的名称
                Log.e("LOCATION******", location.toString());
                try {
                    HttpMessgeUtil.getInstance().getWeather(location.getLatitude()+"", location.getLongitude()+"",
                            new GenericsCallback<WeatherBean>(new JsonGenericsSerializator()) {
                                @Override
                                public void onError(Call call, Exception e, int id) {
                                    Log.d("DATA******","error = "+e.getMessage());
                                }

                                @Override
                                public void onResponse(final WeatherBean response, int id) {
                                    if (response.getCode()==200){
                                        app.setWeatherModel(response);
                                        if (getActivity()!=null){
                                            SharedPreferences sharedPreferences = getActivity().getSharedPreferences(FILE,MODE_PRIVATE);
                                            sharedPreferences.edit().putLong("weatherTime",Calendar.getInstance().getTimeInMillis()).commit();
                                            if (EventBus.getDefault().isRegistered(HealthyFragment.this)){
                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        getView().findViewById(R.id.weatherLl).setClickable(true);
                                                        updataWeatherView();
                                                        if (app.isMtk())
                                                            EXCDController.getInstance().writeForUpdateWeather(app.getWeatherModel(),app.getCity());
                                                        else{
                                                            BleClient.getInstance().writeForSetWeather(app.getWeatherModel(),app.getCity());
                                                            BleClient.getInstance().writeForSetElevation();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }
                                }
                            });
                } catch (IOException e) {
                    Log.d("DATA******","error = "+e.getMessage());
                    e.printStackTrace();
                }
                locationManager.removeUpdates(locationListener);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
}
