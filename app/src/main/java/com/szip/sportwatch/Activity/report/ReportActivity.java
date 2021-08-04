package com.szip.sportwatch.Activity.report;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.szip.sportwatch.Activity.BaseActivity;
import com.szip.sportwatch.Adapter.MyPagerAdapter;
import com.szip.sportwatch.DB.LoadDataUtil;
import com.szip.sportwatch.Interface.CalendarListener;
import com.szip.sportwatch.Model.EvenBusModel.UpdateReport;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.DateUtil;
import com.szip.sportwatch.Util.FileUtil;
import com.szip.sportwatch.Util.StatusBarCompat;
import com.szip.sportwatch.View.CalendarPicker;
import com.szip.sportwatch.View.NoScrollViewPager;

import org.greenrobot.eventbus.EventBus;

public class ReportActivity extends BaseActivity implements View.OnClickListener,ISportView{

    private String[] tabs;
    private TabLayout mTab;
    private NoScrollViewPager mPager;
    public long reportDate = DateUtil.getTimeOfToday();
    private ISportPresenter iSportPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_report);
        tabs = new String[]{getString(R.string.day),getString(R.string.week),getString(R.string.month),getString(R.string.year)};
        String type = getIntent().getStringExtra("type");
        LoadDataUtil.newInstance().initCalendarPoint(type);
        initView(type);
        initEvent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileUtil.getInstance().deleteFile(MyApplication.getInstance().getPrivatePath()+"share.jpg");
    }

    private void initView(String type) {
        StatusBarCompat.translucentStatusBar(ReportActivity.this,true);
        setAndroidNativeLightStatusBar(this,true);
        mTab = findViewById(R.id.reportTl);
        mPager = findViewById(R.id.reportVp);
        switch (type){
            case "step":
                iSportPresenter = new StepPresenterImpl(getApplicationContext(),this);
                break;
            case "heart":
                iSportPresenter = new HeartPresenterImpl(getApplicationContext(),this);
                break;
            case "bp":
                iSportPresenter = new BloodPressurePresenterImpl(this,this);
                break;
            case "bo":
                iSportPresenter = new BloodOxygenPresenterImpl(this,this);
                break;
            case "temp":
                iSportPresenter = new TemperaturePresenterImpl(this,this);
                break;
            case "sleep":
                iSportPresenter = new SleepPresenterImpl(getApplicationContext(),this);
                break;
        }
        iSportPresenter.getPageView(getSupportFragmentManager());
    }

    private void initEvent() {
        findViewById(R.id.backIv).setOnClickListener(this);
        findViewById(R.id.image1).setOnClickListener(this);
        findViewById(R.id.image2).setOnClickListener(this);
    }

    @Override
    public void initPager(MyPagerAdapter myPagerAdapter,String title) {
        setTitleText(title);
        // 给ViewPager设置适配器
        mPager.setAdapter(myPagerAdapter);
        // 使用 TabLayout 和 ViewPager 相关联
        mTab.setupWithViewPager(mPager);

        // TabLayout 指示器 (记得自己手动创建3个Fragment,注意是 app包下的Fragment 还是 V4包下的 Fragment)
        for (int i = 0; i < myPagerAdapter.getCount(); i++) {
            TabLayout.Tab tab = mTab.getTabAt(i);//获得每一个tab
            tab.setCustomView(R.layout.main_top_layout);//给每一个tab设置view
            if (i == 0) {
                // 设置第一个tab的TextView是被选择的样式
                tab.getCustomView().findViewById(R.id.main_tv).setSelected(true);//第一个tab被选中
            }
            TextView textView = (TextView) tab.getCustomView().findViewById(R.id.main_tv);
            textView.setText(tabs[i]);//设置tab上的文字
        }
        mTab.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.getCustomView().findViewById(R.id.main_tv).setSelected(true);
                mPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getCustomView().findViewById(R.id.main_tv).setSelected(false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public void setViewPagerScroll(boolean isScroll){
        mPager.setScroll(isScroll);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.backIv:
                finish();
                break;
            case R.id.image2:
                CalendarPicker.getInstance()
                        .enableAnimation(true)
                        .setFragmentManager(getSupportFragmentManager())
                        .setAnimationStyle(R.style.CustomAnim)
                        .setDate(DateUtil.getStringDateFromSecond(reportDate,"yyyy-MM-dd"))
                        .setFlag(0)
                        .setCalendarListener(new CalendarListener() {
                            @Override
                            public void onClickDate(String date) {
                                if (DateUtil.getTimeScopeForDay(date,"yyyy-MM-dd")>DateUtil.getTimeOfToday()){
                                    showToast(getString(R.string.tomorrow));
                                }else {
                                    reportDate = DateUtil.getTimeScopeForDay(date,"yyyy-MM-dd");
                                    EventBus.getDefault().post(new UpdateReport());
                                }
                            }
                        })
                        .show();
                break;
            case R.id.image1:{
              checkPermission();
            }
                break;
        }
    }

    private void checkPermission() {
        /**
         * 获取权限·
         * */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        100);
            }else {
                shareShow(findViewById(R.id.reportLl));
            }
        }else {
            shareShow(findViewById(R.id.reportLl));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100){
            int code = grantResults[0];
            if (code == PackageManager.PERMISSION_GRANTED){
                shareShow(findViewById(R.id.reportLl));
            }else {
                showToast(getString(R.string.shareFailForPermission));
            }
        }
    }
}
