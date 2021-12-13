package com.szip.sportwatch.Activity;

import androidx.annotation.NonNull;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mediatek.ctrl.fota.common.FotaOperator;
import com.mediatek.ctrl.fota.common.FotaVersion;
import com.mediatek.ctrl.fota.common.IFotaOperatorCallback;
import com.mediatek.wearable.WearableManager;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.LogUtil;
import com.szip.sportwatch.Util.StatusBarCompat;

import java.net.URL;
import java.util.List;

public class UpdateFirmwareActivity extends BaseActivity{

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    private static final int SEND_FOTA_FILE_TO_SD = 0x001;
    private static final int SHOW_FOTA_FILE_SIZE = 0x002;

    private static final int MGS_TEXT_VIEW_UPDATE = 10;
    private static final int MSG_SEND_TIME_OUT = 11;
    private static final int SEND_TIMEOUT = 2 * 60 * 1000;
    private static final int MSG_PROGRESS_UPDATE = 20;

    private static final int MSG_ARG1_DOWNLOAD_FINISHED = 1;
    private static final int MSG_ARG1_UPDATE_FINISHED = 2;
    private static final int MSG_ARG1_UPDATE_FAILED_CAUSE_DISCONNECTED = 3;
    private static final int MSG_ARG1_DOWNLOAD_FAILED = 4;
    private static final int MSG_ARG1_UPDATE_FAILED = 5;

    private static final int FOTA_SEND_VIA_BT_SUCCESS = 2;
    // update via bin success
    private static final int FOTA_UPDATE_VIA_BT_SUCCESS = 3;

    // update via bt errors
    private static final int FOTA_UPDATE_VIA_BT_COMMON_ERROR = -1;
    // FP write file failed
    private static final int FOTA_UPDATE_VIA_BT_WRITE_FILE_FAILED = -2;
    // FP disk full error
    private static final int FOTA_UPDATE_VIA_BT_DISK_FULL = -3;
    // FP data transfer failed
    private static final int FOTA_UPDATE_VIA_BT_DATA_TRANSFER_ERROR = -4;
    // FP update Fota trigger failed
    private static final int FOTA_UPDATE_VIA_BT_TRIGGER_FAILED = -5;
    // FP update fot failed
    private static final int FOTA_UPDATE_VIA_BT_FAILED = -6;
    // FP trigger failed cause of low battery
    private static final int FOTA_UPDATE_TRIGGER_FAILED_CAUSE_LOW_BATTERY = -7;
    // get FP version failed
    private static final String FOTA_VERSION_GET_FAILED = "-8";
    // //// M : update via bt signals end

    private static final int FILE_NOT_FOUND_ERROR = -100;
    private static final int READ_FILE_FAILED = -101;
    //FOTA时间，在改事件 内，操作返回键无效
    private final long fota_effective_time = 5 * 60 * 1000;

    private String mFileUrl;
    private String fileName;
    private TextView mProgressTitle;
    private TextView mProgressText;
    private ProgressBar mProgressBar;
    private Context mContext;

    private boolean isSending = false;
    private int mSendFotaProgress = 0;

    private MyHandler mHandler = new MyHandler();





    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MGS_TEXT_VIEW_UPDATE:
                    updateTextView(msg.arg1);
                    break;

                case MSG_SEND_TIME_OUT:
                    //sIsSending = false;
                    Log.e("update******", "MGS_TEXT_VIEW_UPDATE");
                    break;

                case MSG_PROGRESS_UPDATE:
                    //开始往手表传送文件
                    mProgressTitle.setText(getString(R.string.sending_fota_file));
                    mSendFotaProgress = msg.arg1;
                    mProgressText.setText(msg.arg1 + " %");
                    mProgressBar.setProgress(msg.arg1);
                    break;

                case SEND_FOTA_FILE_TO_SD:
                    //准备往手表传送FOTA文件的准备工作
                    LogUtil.getInstance().logd("update******","开始准备烧录");
                    mProgressTitle.setText(getString(R.string.read_send_fota_file));
                    mProgressText.setText("0%");
                    mProgressBar.setProgress(0);
                    String path = (String) msg.obj;
                    if (TextUtils.isEmpty(path)) {
                        showToast(getString(R.string.update_failed));
                        finish();
                        return;
                    }
                    try {
                        mTransferTask.execute(path);
                    } catch (IllegalStateException e) {
                        showToast(getString(R.string.update_failed));
                        finish();
                    }

                    break;

                case SHOW_FOTA_FILE_SIZE:
                    //下载文件前，显示要下载FoTA文件的大小
                    String sizeValues = "";
                    long fileSize = (long) msg.obj;
                    if (fileSize >= (1024 * 1024)) {
                        float values = fileSize / (1204 * 1024.0f);
                        sizeValues = (int) values + " M";
                    } else {
                        float values = fileSize / 1024.0f;
                        sizeValues = (int) values + " KB";
                    }
                    String str = getResources().getString(R.string.downloading_fota) + ":" + sizeValues;
                    mProgressTitle.setText(str);
                    break;

                default:
                    return;
            }
        }
    }


    SensorManager mSensorManager;//管理器实例
    Sensor stepCounter;//传感器
    float mSteps = 0;//步数
    TextView steps;//显示步数
    TextView time;//显示时间

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_update_firmware);
//        StatusBarCompat.translucentStatusBar(this,true);
//        setAndroidNativeLightStatusBar(this,true);
//        mSharedPreferences = getSharedPreferences(MyApplication.FILE, MODE_PRIVATE);
//        mEditor = mSharedPreferences.edit();
//        mEditor.putLong("UPDATE_FOTA_START_TIME", System.currentTimeMillis());
//        mEditor.commit();
//        mContext = this;
//        FotaOperator.getInstance(this).registerFotaCallback(mFotaCallBack);
//        //注册一个监听蓝牙状态的广播
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
//        this.registerReceiver(mReceiver, filter);
//
//        mProgressTitle =  findViewById(R.id.progress_title);
//        mProgressText =  findViewById(R.id.progress_text);
//        mProgressBar =  findViewById(R.id.progress_bar);
//
//        mFileUrl = getExternalFilesDir(null).getPath() + "/image.bin";
//        LogUtil.getInstance().logd("update******","path = "+mFileUrl);
////                SmartDeviceBLL.getInstance().getFotaFileUrl();
//        if (TextUtils.isEmpty(mFileUrl)) {
//            showToast(getString(R.string.new_version));
//            finish();
//            return;
//        }
//        String[] str = mFileUrl.split("/");
//        fileName = str[str.length - 1];
//            startDownloadFotaFromNetwork();


        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        steps = findViewById(R.id.steps);
        time = findViewById(R.id.time);
        // 获取计步器sensor
        stepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        Sensor stepCounter1 = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if(stepCounter != null&&stepCounter1 != null){
            // 如果sensor找到，则注册监听器
            Log.e("DATA******","注册计步器");
            mSensorManager.registerListener(sensorEventListener,stepCounter,1000);
        }
        else{
            Log.e("DATA******","no step counter sensor found");
        }
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            Log.d("DATA******","步数有变");
            mSteps = event.values[0];
            steps.setText("你已经走了"+String.valueOf((int)mSteps)+"步");
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d("DATA******","步数精确度有变");
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100){
            int code = grantResults[0];
            if (code == PackageManager.PERMISSION_GRANTED){
                startDownloadFotaFromNetwork();
            }else {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (!mTransferTask.isCancelled()) {
//            mTransferTask.cancel(true);
//        }
//        FotaOperator.getInstance(this).unregisterFotaCallback(mFotaCallBack);
//        this.unregisterReceiver(mReceiver);
//        mHandler.removeCallbacksAndMessages(null);
//        mSendFotaProgress = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

//    @Override
//    public void onBackPressed() {
//        long nowTime = System.currentTimeMillis();
//        long startTime = mSharedPreferences.getLong("UPDATE_FOTA_START_TIME", nowTime);
//        if ((nowTime - startTime) >= fota_effective_time) {
//            super.onBackPressed();
//        } else {
//            int duration = (int) (fota_effective_time / (60 * 1000));
//            String str = getResources().getString(R.string.warning_updating_text, duration);
//            showToast(str);
//        }
//    }

    /**
     * 从网络下载FOTA文件
     */
    private void startDownloadFotaFromNetwork() {
//        try {
//            DownFirmwareFile downAsyncTask = new DownFirmwareFile();
//            downAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new URL(mFileUrl));
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
        Message message = mHandler.obtainMessage();
        message.what = SEND_FOTA_FILE_TO_SD;
        message.obj = getExternalFilesDir(null).getPath() + "/image.bin";
        mHandler.sendMessage(message);
    }

    //从网络下载FOTA文件
    private class DownFirmwareFile extends AsyncTask<URL, Integer, String> {
        @Override
        protected String doInBackground(URL... params) {
//            /*String name = Environment.getExternalStorageDirectory() + File.separator + fileName;
//            File file = new File(name);*/
//            String path = FileUtil.getCachePath(mContext);
//            if (TextUtils.isEmpty(path)) {
//                return "";
//            }
//            File file = new File(path, fileName);
//            String name = file.getAbsolutePath();
//            InputStream iStream = null;
//            OutputStream oStream = null;
//            HttpURLConnection conn = null;
//            try {
//                if (file.exists()) {
//                    file.delete();
//                }
//                file.createNewFile();
//
//                conn = (HttpURLConnection) params[0].openConnection();
//                conn.setConnectTimeout(60000);
//                conn.setReadTimeout(60000);
//                long dataLength = conn.getContentLength();
//                Message message = mHandler.obtainMessage();
//                message.what = SHOW_FOTA_FILE_SIZE;
//                message.obj = dataLength;
//                mHandler.sendMessage(message);
//                iStream = conn.getInputStream();
//                oStream = new FileOutputStream(file, false);
//                byte[] buffer = new byte[4096];
//                int len = iStream.read(buffer);
//                int newrate = 0;
//                int oldrate = 0;
//                long currentLen = 0;
//                while (len != -1) {
//                    currentLen = currentLen + len;
//                    newrate = (int) ((currentLen * 100) / dataLength);
//                    if (newrate != oldrate) {
//                        publishProgress(newrate);
//                        oldrate = newrate;
//                    }
//                    oStream.write(buffer, 0, len);
//                    oStream.flush();
//                    len = iStream.read(buffer);
//                }
//                oStream.close();
//                iStream.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//                name = "";
//            } finally {
//                try {
//                    if (iStream != null) {
//                        iStream.close();
//                    }
//                    if (oStream != null) {
//                        oStream.close();
//                    }
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Message message = mHandler.obtainMessage();
            message.what = SEND_FOTA_FILE_TO_SD;
            message.obj = s;
            mHandler.sendMessage(message);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mProgressBar.setProgress(values[0]);
            mProgressText.setText(values[0] + "%");
        }
    }

    //将FOTA文件传到手机
    private AsyncTask<String, Void, Void> mTransferTask = new AsyncTask<String, Void, Void>() {
        @Override
        protected Void doInBackground(String... params) {
            isSending = true;

            String path = params[0];
            //Uri uri = Uri.parse(path);
            LogUtil.getInstance().logd("update******","开始烧录 = "+path);
            FotaOperator.getInstance(UpdateFirmwareActivity.this).sendFotaFirmwareData(5, path);
            return null;
        }
    };

    /**
     * 监听文件传输状态
     */
    private IFotaOperatorCallback mFotaCallBack = new IFotaOperatorCallback() {


        @Override
        public void onFotaTypeReceived(int i) {

        }

        @Override
        public void onCustomerInfoReceived(String s) {
            Log.e("update******", "s" + s);
        }

        @Override
        public void onFotaVersionReceived(FotaVersion fotaVersion) {
            Log.e("update******", "status" + fotaVersion.mVersionString);
        }

        @Override
        public void onStatusReceived(int status) {
            Log.e("update******", "status" + status);
            switch (status) {
                case FOTA_SEND_VIA_BT_SUCCESS:
                    Message msg = mHandler.obtainMessage();
                    msg.what = MGS_TEXT_VIEW_UPDATE;
                    msg.arg1 = MSG_ARG1_DOWNLOAD_FINISHED;
                    mHandler.sendMessage(msg);
                    LogUtil.getInstance().logd("update******","烧录中");
                    break;

                case FOTA_UPDATE_VIA_BT_SUCCESS:
                    Message msg1 = mHandler.obtainMessage();
                    msg1.what = MGS_TEXT_VIEW_UPDATE;
                    msg1.arg1 = MSG_ARG1_UPDATE_FINISHED;
                    mHandler.sendMessage(msg1);
                    LogUtil.getInstance().logd("update******","烧录成功");
                    break;
                case FOTA_UPDATE_VIA_BT_COMMON_ERROR:
                case FOTA_UPDATE_VIA_BT_WRITE_FILE_FAILED:
                case FOTA_UPDATE_VIA_BT_DISK_FULL:
                case FOTA_UPDATE_VIA_BT_DATA_TRANSFER_ERROR:
                case FOTA_UPDATE_VIA_BT_TRIGGER_FAILED:
                case FOTA_UPDATE_VIA_BT_FAILED:
                case FOTA_UPDATE_TRIGGER_FAILED_CAUSE_LOW_BATTERY:
                case FILE_NOT_FOUND_ERROR:
                case READ_FILE_FAILED:
                    if (!mTransferTask.isCancelled() && mTransferTask.getStatus() == AsyncTask.Status.RUNNING) {
                        mTransferTask.cancel(true);
                    }
                    if (status == FOTA_UPDATE_TRIGGER_FAILED_CAUSE_LOW_BATTERY) {
                        showToast(getString(R.string.trigger_failed_due_to_low_battery));
                    } else {
                        showToast(getString(R.string.update_failed));
                    }

                    Message msg2 = mHandler.obtainMessage();
                    msg2.what = MGS_TEXT_VIEW_UPDATE;
                    msg2.arg1 = MSG_ARG1_UPDATE_FAILED;
                    mHandler.sendMessage(msg2);
                default:
                    break;
            }
        }

        @Override
        public void onConnectionStateChange(int newConnectionState) {
            Log.e("update******", "newConnectionState" + newConnectionState);
            if (newConnectionState == WearableManager.STATE_CONNECT_LOST) {
                mTransferTask.cancel(true);
                Message message = mHandler.obtainMessage();
                message.what = MGS_TEXT_VIEW_UPDATE;
                message.arg1 = MSG_ARG1_UPDATE_FAILED_CAUSE_DISCONNECTED;
                mHandler.sendMessage(message);
            }
        }

        @Override
        public void onProgress(int progress) {
            Message msg = mHandler.obtainMessage(MSG_PROGRESS_UPDATE);
            msg.arg1 = progress;
            mHandler.sendMessage(msg);
            Log.e("update******", "onProgress:" + progress);
        }
    };

    /**
     * 监听蓝牙状态的改变
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_OFF) {
                    mTransferTask.cancel(true);
                    Message message = mHandler.obtainMessage();
                    message.what = MGS_TEXT_VIEW_UPDATE;
                    message.arg1 = MSG_ARG1_UPDATE_FAILED_CAUSE_DISCONNECTED;
                    mHandler.sendMessage(message);
                }
            }
        }
    };

    private void doFinishAction() {
            /*hasSend = true;
            sIsSending = false;*/
        showToast(getString(R.string.send_firmware_date_success));
    }

    private void updateTextView(final int which) {
        if (which == MSG_ARG1_UPDATE_FINISHED) {
            showToast(getString(R.string.updated_firmware));
            finish();
            Log.e("update******", "MSG_ARG1_UPDATE_FINISHED");
        } else if (which == MSG_ARG1_UPDATE_FAILED_CAUSE_DISCONNECTED) {
            if (mSendFotaProgress == 100) {
                mProgressTitle.setText(getString(R.string.download_succeed_via_bt));
                //mSendFotaProgress = 0;
            } else {
                showToast(getString(R.string.bt_disconnected_while_transfer));
                finish();
            }
            //finish();
            Log.e("update******", "MSG_ARG1_UPDATE_FAILED_CAUSE_DISCONNECTED  progress=" + mSendFotaProgress);
        } else if (which == MSG_ARG1_DOWNLOAD_FINISHED) {
            mProgressTitle.setText(getString(R.string.download_succeed_via_bt));
            Log.e("update******", "MSG_ARG1_DOWNLOAD_FINISHED ");
        } else if (which == MSG_ARG1_UPDATE_FAILED) {
            showToast(getString(R.string.update_failed));
            finish();
            Log.e("update******", "MSG_ARG1_UPDATE_FAILED ");
        }
    }

}
