package com.szip.sportwatch.Activity.main;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.szip.sportwatch.Activity.BaseActivity;
import com.szip.sportwatch.Activity.help.GuideActivity;
import com.szip.sportwatch.Model.UpdateSportView;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Service.MainService;
import com.szip.sportwatch.Util.LogUtil;
import com.szip.sportwatch.Util.StatusBarCompat;
import com.szip.sportwatch.View.HostTabView;
import com.szip.sportwatch.View.MyToastView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentTabHost;

import static com.szip.sportwatch.MyApplication.FILE;

public class MainActivity extends BaseActivity implements IMainView{

    private ArrayList<HostTabView> mTableItemList;
    private MyApplication app;
    private RelativeLayout layout;
    private FragmentTabHost fragmentTabHost;
    private boolean isVisiable = false;
    private long firstime = 0;
    private IMainPrisenter iMainPrisenter;
    /**
     * 淡入
     * */
    private AlphaAnimation alphaAnimation  = new AlphaAnimation(0f, 1f);
    /**
     * 淡出
     * */
    private AlphaAnimation alphaAnimation1  = new AlphaAnimation(1f, 0f);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        StatusBarCompat.translucentStatusBar(MainActivity.this,true);
        app = (MyApplication) getApplicationContext();

        layout = findViewById(R.id.layout);
        iMainPrisenter = new MainPresenterImpl(this,this);
        iMainPrisenter.checkBluetoochState();
        initAnimation();
        initHost();
        checkPermission();
    }


    private void checkPermission(){
        if (app.isCamerable()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
                    requestPermissions(new String[]{Manifest.permission.CAMERA,
                    }, 101);
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED||
                    checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED||
                    checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_DENIED||
                    checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED
            ) {
                requestPermissions(new String[]{Manifest.permission.READ_SMS,Manifest.permission.SEND_SMS,Manifest.permission.READ_CALL_LOG
                                ,Manifest.permission.READ_CONTACTS},
                        102);
            }
        }
    }




    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.getInstance().logd("SZIP******","MAIN DESTROY");
        iMainPrisenter.setViewDestory();
        MainService.getInstance().stopConnect();
    }

    @Override
    public void initHostFinish(ArrayList<HostTabView> hostTabViews) {
        mTableItemList =hostTabViews;
        mTableItemList.get(1).setView(app.getSportVisiable());
        SharedPreferences sharedPreferences = getSharedPreferences(FILE, Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("isFirst",true)){
            sharedPreferences.edit().putBoolean("isFirst",false).commit();
            startActivity(new Intent(MainActivity.this, GuideActivity.class));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateSport(UpdateSportView updateSportView){
        mTableItemList.get(1).setView(app.getSportVisiable());
    }

    /**
     * 初始化选项卡视图
     * */
    private void initHost() {
        //实例化FragmentTabHost对象
        fragmentTabHost = findViewById(android.R.id.tabhost);
        fragmentTabHost.setup(this,getSupportFragmentManager(),android.R.id.tabcontent);
        iMainPrisenter.initHost(fragmentTabHost);
    }
    
    /**
     * 初始化动画
     * */
    private void initAnimation() {
        alphaAnimation.setDuration(500);//设置动画持续时间
        alphaAnimation.setRepeatCount(0);//设置重复次数
        alphaAnimation.setFillAfter(true);//动画执行完后是否停留在执行完的状态

        alphaAnimation1.setDuration(500);//设置动画持续时间
        alphaAnimation1.setRepeatCount(0);//设置重复次数
        alphaAnimation1.setFillAfter(true);//动画执行完后是否停留在执行完的状态
    }


    /**
     * 中间自定义弹窗
     * */
    public void showMyToast(final String msg){
        if (!isVisiable){
            RelativeLayout.LayoutParams layoutParams =
                    new RelativeLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

            final LinearLayout linearLayout = MyToastView.getInstance(this).showToast(msg);
            linearLayout.startAnimation(alphaAnimation);
            layout.addView(linearLayout,layoutParams);
            isVisiable = true;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    linearLayout.startAnimation(alphaAnimation1);
                    layout.removeView(linearLayout);
                    isVisiable = false;
                }
            },2000);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101){
            int code = grantResults[0];
            if (!(code == PackageManager.PERMISSION_GRANTED)){
                showToast(getString(R.string.permissionErrorForCamare));
                app.setCamerable(false);
            }
        }
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
                Toast.makeText(this, getString(R.string.touchAgain),
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
