package com.szip.sportwatch.Activity.gpsSport;


import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.szip.sportwatch.Activity.BaseActivity;
import com.szip.sportwatch.Activity.SportTrackActivity;
import com.szip.sportwatch.DB.SaveDataUtil;
import com.szip.sportwatch.DB.dbModel.SportData;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.StatusBarCompat;
import com.szip.sportwatch.View.MyAlerDialog;
import com.szip.sportwatch.View.PulldownUpdateView;

public class GpsActivity extends BaseActivity implements IGpsView{

    private TextView distanceTv,speedTv,timeTv,calorieTv,countDownTv;
    private View switchView;
    private ImageView lockIv,mapIv,switchIv;
    private RelativeLayout switchRl,finishRl;
    private FrameLayout lockFl;
    private RelativeLayout updateRl;

    private IGpsPresenter iGpsPresenter;

    private long countDownTime = 3;

    private PulldownUpdateView updateView;

    private ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 1f, 1f,
            1f, 50f, 50f);
    private ScaleAnimation touchAnimation = new ScaleAnimation(1f, 0.9f, 1f,
            0.9f, 50f, 50f);

    private long firstime = 0;
    private boolean started = false;


    /** Called when the activity is first created. */

    @Override

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_gps);
//        iGpsPresenter = new GpsPresenterImpl(getApplicationContext(),this);
        iGpsPresenter = new GpsPresenterImpl(getApplicationContext(),this,(LocationManager) getSystemService(LOCATION_SERVICE));
        initView();
        initEvent();
        initAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        iGpsPresenter.finishLocationService();
    }

    private void initView() {
        StatusBarCompat.translucentStatusBar(this,true);
        setAndroidNativeLightStatusBar(this,true);
        updateView = findViewById(R.id.updateView);
        distanceTv = findViewById(R.id.distanceTv);
        speedTv = findViewById(R.id.speedTv);
        timeTv = findViewById(R.id.timeTv);
        calorieTv = findViewById(R.id.calorieTv);
        lockIv = findViewById(R.id.lockIv);
        mapIv = findViewById(R.id.mapIv);
        switchIv = findViewById(R.id.switchIv);
        switchRl = findViewById(R.id.switchRl);
        finishRl = findViewById(R.id.finishRl);
        updateRl = findViewById(R.id.updateRl);
        lockFl = findViewById(R.id.lockFl);
        countDownTv = findViewById(R.id.countDownTv);
        switchView = findViewById(R.id.switchView);
    }

    private void initEvent() {
        updateView.setListener(pulldownListener);
        lockIv.setOnClickListener(onClickListener);
        mapIv.setOnClickListener(onClickListener);
        switchRl.setOnClickListener(onClickListener);
        finishRl.setOnClickListener(onClickListener);
    }

    /**
     * 初始化动画
     * */
    private void initAnimation() {
        touchAnimation.setDuration(50);//设置动画持续时间
        touchAnimation.setRepeatCount(0);//设置重复次数
        touchAnimation.setInterpolator(new LinearInterpolator());
        scaleAnimation.setDuration(1000);//设置动画持续时间
        scaleAnimation.setRepeatCount(3);//设置重复次数
        scaleAnimation.setInterpolator(new LinearInterpolator());
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                lockFl.setVisibility(View.GONE);
                if (iGpsPresenter!=null)
                    iGpsPresenter.startLocationService();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                if (countDownTime==1)
                    countDownTv.setText("GO!");
                else
                    countDownTv.setText(String.valueOf(--countDownTime));
            }
        });
    }

    /**
     * 控件下拉监听
     * */
    private PulldownUpdateView.PulldownListener pulldownListener = new PulldownUpdateView.PulldownListener() {
        @Override
        public void updateNow() {
            lockFl.setVisibility(View.GONE);
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.lockIv:
                    countDownTv.setVisibility(View.GONE);
                    updateRl.setVisibility(View.VISIBLE);
                    lockFl.setVisibility(View.VISIBLE);
                    break;
                case R.id.mapIv:
                    if (iGpsPresenter!=null&&started)
                        iGpsPresenter.openMap(getSupportFragmentManager());
                    else
                        showToast(getString(R.string.started));
                    break;
                case R.id.switchRl:
                    switchRl.startAnimation(touchAnimation);
                    if (switchRl.getTag().equals("start")){
                        if (iGpsPresenter!=null)
                            iGpsPresenter.startLocationService();
                    }else {
                        if (iGpsPresenter!=null)
                            iGpsPresenter.stopLocationService();
                    }
                    break;
                case R.id.finishRl:
                    switchRl.startAnimation(touchAnimation);
                    if (iGpsPresenter!=null)
                        iGpsPresenter.finishLocationService();
                    break;
            }
        }
    };

    @Override
    public void startCountDown() {
        started = true;
        lockFl.setVisibility(View.VISIBLE);
        countDownTv.setVisibility(View.VISIBLE);
        updateRl.setVisibility(View.GONE);
        countDownTv.startAnimation(scaleAnimation);
    }

    @Override
    public void startRun() {
        switchView.setBackgroundResource(R.drawable.bg_circle_white);
        switchIv.setImageResource(R.mipmap.sport_icon_stop);
        switchRl.setTag("");
        finishRl.setVisibility(View.GONE);
    }

    @Override
    public void stopRun() {
        switchView.setBackgroundResource(R.drawable.bg_circle_green);
        switchIv.setImageResource(R.mipmap.sport_icon_continue);
        switchRl.setTag("start");
        finishRl.setVisibility(View.VISIBLE);
    }

    @Override
    public void saveRun(final SportData sportData) {
        MyAlerDialog.getSingle().showAlerDialog(getString(R.string.tip), getString(R.string.runFinsh), getString(R.string.confirm), getString(R.string.cancel), false,
                new MyAlerDialog.AlerDialogOnclickListener() {
                    @Override
                    public void onDialogTouch(boolean flag) {
                        if (flag){
                            if (sportData.time>30){
                                SaveDataUtil.newInstance().saveSportData(sportData);
                                Intent intent = new Intent(GpsActivity.this, SportTrackActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("sport",sportData);
                                intent.putExtra("data",bundle);
                                startActivity(intent);
                                finish();
                            }else {
                             showToast(getString(R.string.runShort));
                             finish();
                            }
                        }
                    }
                },this);
    }

    @Override
    public void upDateTime(final int time) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timeTv.setText(String.format("%02d:%02d:%02d",time/60/60,time/60%60,time%60));
            }
        });
    }

    @Override
    public void upDateRunData(int speed, float distance, float calorie) {
        speedTv.setText(String.format("%d'%d''",speed/60,speed%60));
        distanceTv.setText(String.format("%.2f",distance/1000));
        calorieTv.setText(String.format("%.1f",calorie));
    }

    /**
     * 双击退出
     * */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long secondtime = System.currentTimeMillis();
            if (secondtime - firstime > 3000) {
                Toast.makeText(this, getString(R.string.touchAgain1),
                        Toast.LENGTH_SHORT).show();
                firstime = System.currentTimeMillis();
                return true;
            } else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}