package com.szip.sportwatch.Activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.szip.sportwatch.Activity.gpsSport.GpsActivity;
import com.szip.sportwatch.Activity.SportDataListActivity;
import com.szip.sportwatch.Activity.userInfo.UserInfoActivity;
import com.szip.sportwatch.DB.LoadDataUtil;
import com.szip.sportwatch.DB.dbModel.SportData;
import com.szip.sportwatch.Fragment.BaseFragment;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.DateUtil;
import com.szip.sportwatch.Util.MathUitl;
import com.szip.sportwatch.View.CircularImageView;

import java.util.Locale;

/**
 * Created by Administrator on 2019/12/1.
 */

public class SportFragment extends BaseFragment implements View.OnClickListener{

    private CircularImageView pictureIv;
    private TextView userNameTv, dataTv,speedTv,calorieTv,sportTimeTv,heartTv,strideTv,distanceTv1;
    private ImageView sportTypeIv;
    private MyApplication app;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_sport;
    }

    @Override
    protected void afterOnCreated(Bundle savedInstanceState) {
        app = (MyApplication) getActivity().getApplicationContext();
        initView();
        initEvent();

    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    private void initEvent() {
        getView().findViewById(R.id.pictureIv).setOnClickListener(this);
        getView().findViewById(R.id.userNameTv).setOnClickListener(this);
        getView().findViewById(R.id.historyIv).setOnClickListener(this);
        getView().findViewById(R.id.runIv).setOnClickListener(this);
    }

    private void initView() {
        pictureIv = getView().findViewById(R.id.pictureIv);
        sportTypeIv = getView().findViewById(R.id.sportTypeIv);
        userNameTv = getView().findViewById(R.id.userNameTv);
        dataTv = getView().findViewById(R.id.dataTv);
        speedTv = getView().findViewById(R.id.speedTv);
        calorieTv = getView().findViewById(R.id.buttonFirstTv);
        sportTimeTv = getView().findViewById(R.id.buttonSecondTv);
        heartTv = getView().findViewById(R.id.heartTv);
        strideTv = getView().findViewById(R.id.strideTv);
        distanceTv1 = getView().findViewById(R.id.distanceTv1);
    }

    /**
     * 初始化数据
     * */
    private void initData() {
        SportData sportData = LoadDataUtil.newInstance().getBestSportData();
        int typeId = R.mipmap.sport_icon_type;
        switch (sportData.type){
            case 1:
                typeId = R.mipmap.sport_best_walk;
                getView().findViewById(R.id.bottomLl).setVisibility(View.VISIBLE);
                break;
            case 2:
                typeId = R.mipmap.sport_best_run;
                getView().findViewById(R.id.bottomLl).setVisibility(View.VISIBLE);
                break;
            case 3:
                typeId = R.mipmap.sport_best_treadmill;
                getView().findViewById(R.id.bottomLl).setVisibility(View.GONE);
                break;
            case 4:
                typeId = R.mipmap.sport_best_type_mountain;
                getView().findViewById(R.id.bottomLl).setVisibility(View.VISIBLE);
                break;
            case 5:
                typeId = R.mipmap.sport_best_marathon;
                getView().findViewById(R.id.bottomLl).setVisibility(View.VISIBLE);
                break;
            case 6:
                typeId = R.mipmap.sport_best_trainingrun;
                getView().findViewById(R.id.bottomLl).setVisibility(View.VISIBLE);
                break;
            case 9:
                typeId = R.mipmap.sport_best_badminton;
                getView().findViewById(R.id.bottomLl).setVisibility(View.GONE);
                break;
            case 10:
                typeId = R.mipmap.sport_best_type_basketball;
                getView().findViewById(R.id.bottomLl).setVisibility(View.GONE);
                break;
            case 11:
                typeId = R.mipmap.sport_best_type_bike;
                getView().findViewById(R.id.bottomLl).setVisibility(View.GONE);
                break;
            case 12:
                typeId = R.mipmap.sport_best_skiing;
                getView().findViewById(R.id.bottomLl).setVisibility(View.GONE);
                break;
            case 16:
                typeId = R.mipmap.sport_best_type_pingpang;
                getView().findViewById(R.id.bottomLl).setVisibility(View.GONE);
                break;
            case 17:
                typeId = R.mipmap.sport_best_type_football;
                getView().findViewById(R.id.bottomLl).setVisibility(View.GONE);
                break;
            case 18:
                typeId = R.mipmap.sport_best_swim;
                getView().findViewById(R.id.bottomLl).setVisibility(View.GONE);
                break;
            case 19:
                typeId = R.mipmap.sport_best_rock;
                getView().findViewById(R.id.bottomLl).setVisibility(View.GONE);
                break;
            case 20:
                typeId = R.mipmap.sport_best_boating;
                getView().findViewById(R.id.bottomLl).setVisibility(View.GONE);
                break;
            case 22:
                typeId = R.mipmap.sport_best_surfing;
                getView().findViewById(R.id.bottomLl).setVisibility(View.GONE);
                break;
        }
        sportTypeIv.setImageResource(typeId);
        dataTv.setText(String.format(Locale.ENGLISH,"%.1f",((sportData.calorie+55)/100)/10f));
        if (app.getUserInfo().getUnit()==0){
            distanceTv1.setText(String.format(Locale.ENGLISH,"%.2f",((sportData.distance+5)/10)/100f));
            ((TextView)getView().findViewById(R.id.deistanceUnitTv)).setText(getString(R.string.distance)+"km)");
        } else{
            distanceTv1.setText(String.format(Locale.ENGLISH,"%.2f", MathUitl.km2Miles(sportData.distance)));
            ((TextView)getView().findViewById(R.id.deistanceUnitTv)).setText(getString(R.string.distance)+"Mi)");
        }
        speedTv.setText(String.format(Locale.ENGLISH,"%02d'%02d''",sportData.speed/60,sportData.speed%60));
        calorieTv.setText(String.format(Locale.ENGLISH,"%.1f", ((sportData.calorie+55)/100)/10f));
        sportTimeTv.setText(String.format(Locale.ENGLISH,"%02d:%02d:%02d",sportData.sportTime/3600, sportData.sportTime%3600/60,sportData.sportTime%3600%60));
        if (sportData.heart!=0)
            heartTv.setText(sportData.heart+"");
        if (sportData.stride!=0)
            strideTv.setText(sportData.stride+"");
        if (sportData.time!=0)
            ((TextView)getView().findViewById(R.id.time)).setText(DateUtil.getStringDateFromSecond(sportData.time,"yyyy/MM/dd HH:mm:ss"));
        else
            ((TextView)getView().findViewById(R.id.time)).setText("----/--/-- --:--:--");
        Log.d("DATA******","time = "+sportData.time);
        Glide.with(this).load(app.getUserInfo().getAvatar())
                    .fallback(R.mipmap.my_head_male_52)
                    .error(R.mipmap.my_head_male_52)
                    .into(pictureIv);
          userNameTv.setText(app.getUserInfo().getUserName());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.pictureIv:
            case R.id.userNameTv:
                if (app.getUserInfo().getPhoneNumber()==null&&app.getUserInfo().getEmail()==null)
                    showToast(getString(R.string.visiter));
                else
                    startActivity(new Intent(getActivity(), UserInfoActivity.class));
                break;
            case R.id.historyIv:
                startActivity(new Intent(getActivity(), SportDataListActivity.class));
                break;
            case R.id.runIv:
                startActivity(new Intent(getActivity(), GpsActivity.class));
                break;
        }
    }
}
