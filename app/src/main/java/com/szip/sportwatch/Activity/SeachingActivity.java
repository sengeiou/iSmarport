package com.szip.sportwatch.Activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.mediatek.wearable.WearableListener;
import com.mediatek.wearable.WearableManager;
import com.szip.sportwatch.Adapter.DeviceAdapter;
import com.szip.sportwatch.DB.LoadDataUtil;
import com.szip.sportwatch.Model.HttpBean.BindBean;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Service.MainService;
import com.szip.sportwatch.Util.HttpMessgeUtil;
import com.szip.sportwatch.Util.JsonGenericsSerializator;
import com.szip.sportwatch.Util.LogUtil;
import com.szip.sportwatch.Util.MathUitl;
import com.szip.sportwatch.Util.ProgressHudModel;
import com.szip.sportwatch.Util.StatusBarCompat;
import com.szip.sportwatch.View.MyAlerDialog;
import com.zhy.http.okhttp.callback.GenericsCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import okhttp3.Call;


public class SeachingActivity extends BaseActivity implements View.OnClickListener {

    private RotateAnimation rotateRight = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,
            0.5f, Animation.RELATIVE_TO_SELF, 0.5f);


    private ArrayList<String> deviceConfig;

    private ImageView searchIv;
    private ListView listView;
    private DeviceAdapter deviceAdapter;

    private Handler mHandler;

    private int selectPos = 0;

    private Runnable mStopRunnable = new Runnable() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    searchDevice(false);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_seaching);
        initData();
        initAnimation();
        initView();
        initEvent();
        checkPermission();
        isLocServiceEnable(SeachingActivity.this);
    }

    private void isLocServiceEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!(gps || network)) {
            MyAlerDialog.getSingle().showAlerDialog(getString(R.string.tip), getString(R.string.checkGPS), getString(R.string.confirm), getString(R.string.cancel),
                    false, new MyAlerDialog.AlerDialogOnclickListener() {
                        @Override
                        public void onDialogTouch(boolean flag) {
                            if (flag){
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }
                        }
                    },SeachingActivity.this);
        }
    }

    private void checkPermission() {
        /**
         * 获取权限·
         * */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        100);
            }else {
                searchDevice(true);
            }
        }else {
            searchDevice(true);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100){
            int code = grantResults[0];
            if (code == PackageManager.PERMISSION_GRANTED){
                searchDevice(true);
            }else {
                MyAlerDialog.getSingle().showAlerDialog(getString(R.string.tip),getString(R.string.findPermission),getString(R.string.applyPermission),
                        getString(R.string.confirm),false, new MyAlerDialog.AlerDialogOnclickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.M)
                            @Override
                            public void onDialogTouch(boolean flag) {
                                if (flag)
                                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            100);
                            }
                        },this);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        searchDevice(false);
        WearableManager.getInstance().unregisterWearableListener(mWearableListener);
    }

    private void initData() {
        mHandler = new Handler();
        WearableManager.getInstance().registerWearableListener(mWearableListener);
        deviceConfig = LoadDataUtil.newInstance().getBleNameConfig();
    }

    /**
     * 搜索设备
     * */
    private void searchDevice(boolean enable) {
        if (enable){
            LogUtil.getInstance().logd("DATA******","开始搜索");
            searchIv.startAnimation(rotateRight);
            mHandler.removeCallbacks(mStopRunnable);
            mHandler.postDelayed(mStopRunnable, 20*1000);
            deviceAdapter.clearList();
            WearableManager.getInstance().scanDevice(true);
        }else {
            LogUtil.getInstance().logd("DATA******","搜索停止");
            searchIv.clearAnimation();
            mHandler.removeCallbacks(mStopRunnable);
            WearableManager.getInstance().scanDevice(false);
        }
    }

    /**
     * 初始化动画
     * */
    private void initAnimation() {
        rotateRight.setDuration(2000);//设置动画持续时间
        rotateRight.setRepeatCount(-1);//设置重复次数
        rotateRight.setInterpolator(new LinearInterpolator());
    }

    /**
     * 初始化视图
     * */
    private void initView() {
        StatusBarCompat.translucentStatusBar(SeachingActivity.this,true);
        setAndroidNativeLightStatusBar(this,true);
        //判断蓝牙状态
        BluetoothAdapter blueadapter = BluetoothAdapter.getDefaultAdapter();
        if (!blueadapter.isEnabled()) {
            Intent bleIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(bleIntent);
        }

        setTitleText(getString(R.string.searchDevice));
        searchIv = findViewById(R.id.rightIv);
        searchIv.setImageResource(R.mipmap.my_device_refresh);
        listView = findViewById(R.id.deviceList);
        deviceAdapter = new DeviceAdapter(this);
        listView.setAdapter(deviceAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BluetoothDevice device = deviceAdapter.getDevice(position);
                selectPos = position;
                ProgressHudModel.newInstance().show(SeachingActivity.this,getString(R.string.waitting)
                        ,getString(R.string.httpError),3000);
                try {
                    HttpMessgeUtil.getInstance().getBindDevice(device.getAddress(),device.getName(), new GenericsCallback<BindBean>(new JsonGenericsSerializator()) {
                        @Override
                        public void onError(Call call, Exception e, int id) {

                        }

                        @Override
                        public void onResponse(BindBean response, int id) {
                            if (response.getCode() == 200){
                                ProgressHudModel.newInstance().diss();
                                //停止蓝牙扫描
                                searchDevice(false);
                                if (!checkBluetoochState())
                                    return;
                                BluetoothDevice device = deviceAdapter.getDevice(selectPos);
                                //缓存蓝牙mac地址
                                MyApplication app = (MyApplication) getApplicationContext();
                                app.getUserInfo().setDeviceCode(device.getAddress());
                                MathUitl.saveStringData(SeachingActivity.this,"deviceCode",device.getAddress()).commit();


                                //启动后台自动连接线程
                                app.setDeviceConfig(device.getName());
                                WearableManager.getInstance().setRemoteDevice(device);
                                MainService.getInstance().startConnect();

                                if (app.getUserInfo().getPhoneNumber()!=null||app.getUserInfo().getEmail()!=null){
                                    //获取云端数据
                                    try {
                                        HttpMessgeUtil.getInstance().getForDownloadReportData(Calendar.getInstance().getTimeInMillis()/1000+"",30+"");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                finish();
                            }else {
                                showToast(response.getMessage());
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * 初始化事件监听
     * */
    private void initEvent() {
        findViewById(R.id.backIv).setOnClickListener(this);
        searchIv.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.backIv:
                WearableManager.getInstance().scanDevice(false);
                finish();
                break;
            case R.id.rightIv:
                if (searchIv.getAnimation()==null){
                    findViewById(R.id.noDeviceLl).setVisibility(View.VISIBLE);
                    searchDevice(true);
                } else
                    searchDevice(false);
                break;
        }
    }

    // register WearableListener
    private WearableListener mWearableListener = new WearableListener() {

        @Override
        public void onDeviceChange(BluetoothDevice device) {
        }

        @Override
        public void onConnectChange(int oldState, int newState) {
        }

        @Override
        public void onDeviceScan(final BluetoothDevice device) {
            if (device.getName()!=null&&deviceConfig.contains(device.getName().replace("_LE",""))) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.noDeviceLl).setVisibility(View.GONE);
                        deviceAdapter.addDevice(device);
                    }
                });
            }
        }

        @Override
        public void onModeSwitch(int newMode) {
        }
    };

    private boolean checkBluetoochState() {
        //判断蓝牙状态
        BluetoothAdapter blueadapter = BluetoothAdapter.getDefaultAdapter();
        if (!blueadapter.isEnabled()) {
            Intent bleIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(bleIntent);
            return false;
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            WearableManager.getInstance().scanDevice(false);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
