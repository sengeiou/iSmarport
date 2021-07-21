package com.szip.run;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mediatek.ctrl.fota.common.FotaOperator;
import com.mediatek.ctrl.fota.common.FotaVersion;
import com.mediatek.ctrl.fota.common.IFotaOperatorCallback;
import com.mediatek.wearable.WearableListener;
import com.mediatek.wearable.WearableManager;

import leprofiles.LocalBluetoothLEManager;

public class MainActivity extends BaseActivity {

    private Context mContext;
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
    private String deviceMac = "F5:37:41:EE:BF:50";
    private TextView mProgressTitle;
    private TextView mProgressText;
    private ProgressBar mProgressBar;

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
                    mProgressTitle.setText(mContext.getResources().getString(R.string.sending_fota_file));
                    mSendFotaProgress = msg.arg1;
                    mProgressText.setText(msg.arg1 + " %");
                    mProgressBar.setProgress(msg.arg1);
                    break;

                case SEND_FOTA_FILE_TO_SD:
                    //准备往手表传送FOTA文件的准备工作
                    Log.d("update******","开始准备烧录");
                    mProgressTitle.setText(mContext.getResources().getString(R.string.read_send_fota_file));
                    mProgressText.setText("0%");
                    mProgressBar.setProgress(0);
                    String path = (String) msg.obj;
                    if (TextUtils.isEmpty(path)) {
                        showToast(mContext.getResources().getString(R.string.update_failed));
                        finish();
                        return;
                    }
                    try {
                        mTransferTask.execute(path);
                    } catch (IllegalStateException e) {
                        showToast(mContext.getResources().getString(R.string.update_failed));
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
                    String str =mContext.getResources().getString(R.string.downloading_fota) + ":" + sizeValues;
                    mProgressTitle.setText(str);
                    break;

                default:
                    return;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("update******","path = "+R.layout.activity_fota);
        setContentView(R.layout.activity_fota);
        mContext = (that == null?this:that);
        //注册一个监听蓝牙状态的广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mContext.registerReceiver(mReceiver, filter);
        mProgressTitle =  findViewById(R.id.progress_title);
        mProgressText =  findViewById(R.id.progress_text);
        mProgressBar =  findViewById(R.id.progress_bar);
        mFileUrl = "/storage/emulated/0/Android/data/com.szip.sportwatch/files/image.bin";
        initBle();


        if (TextUtils.isEmpty(mFileUrl)) {
            showToast(mContext.getResources().getString(R.string.new_version));
            finish();
            return;
        }
    }

    private void initBle() {
        LocalBluetoothLEManager.getInstance().init(mContext);
        boolean isSuccess = WearableManager.getInstance().init(true, mContext, null, R.xml.wearable_config);
        //切换成GATT模式
        if (WearableManager.getInstance().getWorkingMode() == WearableManager.MODE_SPP)
            WearableManager.getInstance().switchMode();

        WearableManager manager = WearableManager.getInstance();
        manager.registerWearableListener(mWearableListener);
        FotaOperator.getInstance(mContext).registerFotaCallback(mFotaCallBack);
        mProgressTitle.setText(mContext.getResources().getString(R.string.searchDevice));
        WearableManager.getInstance().scanDevice(true);

    }

    private WearableListener mWearableListener = new WearableListener() {

        @Override
        public void onConnectChange(int oldState, int newState) {
            Log.d("update******","newstate = "+newState+" ;oldstate = "+oldState);
            if (newState == WearableManager.STATE_CONNECTED){//连接成功，发送同步数据指令
                startDownloadFotaFromNetwork();
            }else if (newState == WearableManager.STATE_CONNECT_LOST){
                mProgressTitle.setText(mContext.getResources().getString(R.string.connecFail));
                if (that==null)
                    finish();
                else
                    that.finish();
            }
        }

        @Override
        public void onDeviceChange(BluetoothDevice device) {
            return;
        }

        @Override
        public void onDeviceScan(BluetoothDevice device) {
            if (device.getAddress().equals(deviceMac)){
                mProgressTitle.setText(mContext.getResources().getString(R.string.connectDevice));
                WearableManager.getInstance().scanDevice(false);
                WearableManager.getInstance().setRemoteDevice(device);
                WearableManager.getInstance().connect();
            }
        }

        @Override
        public void onModeSwitch(int newMode) {
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!mTransferTask.isCancelled()) {
            mTransferTask.cancel(true);
        }
        FotaOperator.getInstance(mContext).unregisterFotaCallback(mFotaCallBack);
        mContext.unregisterReceiver(mReceiver);
        mHandler.removeCallbacksAndMessages(null);
        mSendFotaProgress = 0;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * 从网络下载FOTA文件
     */
    private void startDownloadFotaFromNetwork() {
        Message message = mHandler.obtainMessage();
        message.what = SEND_FOTA_FILE_TO_SD;
        message.obj = mFileUrl;
        mHandler.sendMessage(message);
    }

    //将FOTA文件传到手机
    private AsyncTask<String, Void, Void> mTransferTask = new AsyncTask<String, Void, Void>() {
        @Override
        protected Void doInBackground(String... params) {
            isSending = true;

            String path = params[0];
            Log.d("update******","开始烧录 = "+path);
            FotaOperator.getInstance(mContext).sendFotaFirmwareData(5, path);
            return null;
        }
    };

    /**
     * 监听文件传输状态
     */
    private IFotaOperatorCallback mFotaCallBack = new IFotaOperatorCallback() {

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
                    Log.d("update******","烧录中");
                    break;

                case FOTA_UPDATE_VIA_BT_SUCCESS:
                    Message msg1 = mHandler.obtainMessage();
                    msg1.what = MGS_TEXT_VIEW_UPDATE;
                    msg1.arg1 = MSG_ARG1_UPDATE_FINISHED;
                    mHandler.sendMessage(msg1);
                    Log.d("update******","烧录成功");
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
                        showToast(mContext.getResources().getString(R.string.trigger_failed_due_to_low_battery));
                    } else {
                        showToast(mContext.getResources().getString(R.string.update_failed));
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

    private void updateTextView(final int which) {
        if (which == MSG_ARG1_UPDATE_FINISHED) {
            showToast(mContext.getResources().getString(R.string.updated_firmware));
            finish();
            Log.e("update******", "MSG_ARG1_UPDATE_FINISHED");
        } else if (which == MSG_ARG1_UPDATE_FAILED_CAUSE_DISCONNECTED) {
            if (mSendFotaProgress == 100) {
                mProgressTitle.setText(mContext.getResources().getString(R.string.download_succeed_via_bt));
                //mSendFotaProgress = 0;
            } else {
                showToast(mContext.getResources().getString(R.string.bt_disconnected_while_transfer));
                finish();
            }
            //finish();
            Log.e("update******", "MSG_ARG1_UPDATE_FAILED_CAUSE_DISCONNECTED  progress=" + mSendFotaProgress);
        } else if (which == MSG_ARG1_DOWNLOAD_FINISHED) {
            mProgressTitle.setText(mContext.getResources().getString(R.string.download_succeed_via_bt));
            Log.e("update******", "MSG_ARG1_DOWNLOAD_FINISHED ");
        } else if (which == MSG_ARG1_UPDATE_FAILED) {
            showToast(mContext.getResources().getString(R.string.update_failed));
            finish();
            Log.e("update******", "MSG_ARG1_UPDATE_FAILED ");
        }
    }

}