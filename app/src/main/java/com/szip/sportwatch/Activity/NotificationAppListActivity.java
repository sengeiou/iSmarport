package com.szip.sportwatch.Activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.szip.sportwatch.Adapter.PersonalAppListAdapter;
import com.szip.sportwatch.Interface.OnSmsStateListener;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Service.MainService;
import com.szip.sportwatch.Util.LogUtil;
import com.szip.sportwatch.Util.MathUitl;
import com.szip.sportwatch.Notification.BlockList;
import com.szip.sportwatch.Notification.IgnoreList;
import com.szip.sportwatch.Util.StatusBarCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

public class NotificationAppListActivity extends BaseActivity {

    // Debugging
    private static final String TAG = "AppManager/NotificationAppList";

    // Tab tag enum
    private static final String TAB_TAG_PERSONAL_APP = "personal_app";

    private static final String TAB_TAG_SYSTEM_APP = "system_app";

    // View item filed
    private static final String VIEW_ITEM_INDEX = "item_index";

    private static final String VIEW_ITEM_ICON = "package_icon";

    private static final String VIEW_ITEM_TEXT = "package_text";

    private static final String VIEW_ITEM_CHECKBOX = "package_switch";

    private static final String VIEW_ITEM_NAME = "package_name"; // Only for
    // save to
    // ignore list

    private ListView mPersonalAppListView;


    private List<Map<String, Object>> mPersonalAppList = null;

    private List<Map<String, Object>> mBlockAppList = null;

    // private int mPersonalAppSelectedCount = 0;
    private PersonalAppListAdapter mPersonalAppAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.notification_app_list);
        StatusBarCompat.translucentStatusBar(NotificationAppListActivity.this,true);
        setAndroidNativeLightStatusBar(this,true);
        initView();

        LoadPackageTask loadPackageTask = new LoadPackageTask(this);
        try {
            loadPackageTask.execute("");
        } catch (Exception e) {
            Toast toast = Toast.makeText(NotificationAppListActivity.this,
                    R.string.launchfail, 1);
            toast.show();
        }

    }

    private void initView() {
        setTitleText(getString(R.string.notification));
        findViewById(R.id.rightIv).setVisibility(View.GONE);
        findViewById(R.id.backIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        saveIgnoreList();
        saveBlockList();
    }

    private void saveBlockList() {

        // Save personal app

        BlockList.getInstance().saveBlockList();

        // Load package in background
    }

    private void saveIgnoreList() {
        IgnoreList.getInstance().saveIgnoreList();
        // Prompt user that have saved successfully .
        // Toast.makeText(this, R.string.save_successfully,
        // Toast.LENGTH_SHORT).show();
    }

    private void initUiComponents() {
        mPersonalAppListView = findViewById(R.id.list_notify_personal_app);
        mPersonalAppAdapter = new PersonalAppListAdapter(this,onSmsStateListener);
        mPersonalAppListView.setAdapter(mPersonalAppAdapter);
    }


    private OnSmsStateListener onSmsStateListener = new OnSmsStateListener() {
        @Override
        public void onSmsStateChange(boolean check) {
            if (check) {
                LogUtil.getInstance().logd("SZIP******","进入回调");
                checkPermission();
            } else {
                MainService.getInstance().stopSmsService();
            }
        }
    };


    private void checkPermission() {
        /**
         * 获取权限·
         * */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED){
                LogUtil.getInstance().logd("SZIP******","申请权限");
                requestPermissions(new String[]{Manifest.permission.READ_SMS},
                        100);
            }else {
                LogUtil.getInstance().logd("SZIP******","申请已经打开");
                MainService.getInstance().startSmsService();
            }
        }else {
            MainService.getInstance().startSmsService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100){
            int code = grantResults[0];
            if (code == PackageManager.PERMISSION_GRANTED){
                MainService.getInstance().startSmsService();
            }else {
                showToast(getString(R.string.permissionErrorForSMS));
                mPersonalAppAdapter.notifyDataSetChanged();
            }
        }
    }

    private class PackageItemComparator implements Comparator<Map<String, Object>> {

        private final String mKey;

        public PackageItemComparator() {
            mKey = NotificationAppListActivity.VIEW_ITEM_TEXT;
        }

        /**
         * Compare package in alphabetical order.
         *
         * @see java.util.Comparator#compare(Object, Object)
         */
        @Override
        public int compare(Map<String, Object> packageItem1, Map<String, Object> packageItem2) {

            String packageName1 = (String) packageItem1.get(mKey);
            String packageName2 = (String) packageItem2.get(mKey);
            return packageName1.compareToIgnoreCase(packageName2);
        }
    }

    private class LoadPackageTask extends AsyncTask<String, Integer, Boolean> {

        private ProgressDialog mProgressDialog;

        private final Context mContext;

        public LoadPackageTask(Context context) {
            Log.i(TAG, "LoadPackageTask(), Create LoadPackageTask!");

            mContext = context;
            createProgressDialog();
        }

        /*
         * Show a ProgressDialog to prompt user to wait
         */
        private void createProgressDialog() {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setTitle(R.string.progress_dialog_title);
            mProgressDialog.setMessage(mContext.getString(R.string.progress_dialog_message));
            mProgressDialog.show();

            Log.i(TAG, "createProgressDialog(), ProgressDialog shows");
        }

        @Override
        protected Boolean doInBackground(String... arg0) {
            Log.i(TAG, "doInBackground(), Begin load and sort package list!");

            // Load and sort package list
            loadPackageList();
            sortPackageList();

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.i(TAG, "onPostExecute(), Load and sort package list complete!");

            // Do the operation after load and sort package list completed
            initUiComponents();
            mPersonalAppAdapter.setmPersonalAppList(mPersonalAppList);
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                mProgressDialog = null;
            }
        }

        private synchronized void loadPackageList() {
            mPersonalAppList = new ArrayList<Map<String, Object>>();
            mBlockAppList = new ArrayList<Map<String, Object>>();
            HashSet<String> ignoreList = IgnoreList.getInstance().getIgnoreList();
            HashSet<CharSequence> blockList = BlockList.getInstance().getBlockList();
            HashSet<String> exclusionList = IgnoreList.getInstance().getExclusionList();
            List<PackageInfo> packagelist = getPackageManager().getInstalledPackages(0);
            for (PackageInfo packageInfo : packagelist) {
                if (packageInfo != null) {
                    // Whether this package should be exclude;
                    if (exclusionList.contains(packageInfo.packageName)) {
                        continue;
                    }

                    /*
                     * Add this package to package list
                     */
                    Map<String, Object> packageItem = new HashMap<String, Object>();

                    // Add app icon
                    Drawable icon = mContext.getPackageManager().getApplicationIcon(
                            packageInfo.applicationInfo);
                    packageItem.put(VIEW_ITEM_ICON, icon);

                    // Add app name
                    String appName = mContext.getPackageManager()
                            .getApplicationLabel(packageInfo.applicationInfo).toString();
                    packageItem.put(VIEW_ITEM_TEXT, appName);
                    packageItem.put(VIEW_ITEM_NAME, packageInfo.packageName);

                    // Add if app is selected
                    boolean isChecked = ((!ignoreList.contains(packageInfo.packageName)) && (!blockList
                            .contains(packageInfo.packageName)));
                    packageItem.put(VIEW_ITEM_CHECKBOX, isChecked);

                    // Add to package list
                    if (!MathUitl.isSystemApp(packageInfo.applicationInfo)) {
                        mPersonalAppList.add(packageItem);
                    }
                }
            }
        }

        private synchronized void sortPackageList() {
            // Sort package list in alphabetical order.
            PackageItemComparator comparator = new PackageItemComparator();

            // Sort personal app list
            if (mPersonalAppList != null) {
                Collections.sort(mPersonalAppList, comparator);
            }
        }
    }

}
