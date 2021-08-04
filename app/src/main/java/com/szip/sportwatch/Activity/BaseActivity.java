package com.szip.sportwatch.Activity;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.FileUtil;
import com.szip.sportwatch.Util.LogUtil;
import com.szip.sportwatch.Util.ProgressHudModel;
import com.szip.sportwatch.Util.ScreenCapture;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Properties;

import static com.szip.sportwatch.MyApplication.FILE;

/**
 * Created by Administrator on 2019/11/28.
 */

public class BaseActivity extends AppCompatActivity {

    protected void showToast(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }

    /**
     * 分享
     * */
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void shareShow(View view){
        Uri str = ScreenCapture.getBitmap
                (this, view);

        Log.d("SZIP******","str = "+str);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_STREAM, str);
        startActivityForResult(Intent.createChooser(intent,getString(R.string.app_name)),101);
    }

    /**
     * 分享长图
     * */
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void shareShowLong(ScrollView view){
        Uri str = ScreenCapture.getScollerBitmap
                (this, view);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_STREAM, str);
        startActivityForResult(Intent.createChooser(intent,getString(R.string.app_name)),101);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101){
            FileUtil.getInstance().deleteFile(MyApplication.getInstance().getPrivatePath()+"share.jpg");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.getInstance().logd("SZIP******","退出保存");
        ProgressHudModel.newInstance().diss();
        getSharedPreferences(FILE,MODE_PRIVATE).edit().putInt("updownTime",((MyApplication)getApplication()).getUpdownTime()).commit();
    }

    /**
     * 适配Android字体（随着系统字体改变而改变）
     * */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.fontScale != 1)//非默认值
            getResources();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        if (res.getConfiguration().fontScale != 1) {//非默认值
            Configuration newConfig = new Configuration();
            newConfig.setToDefaults();//设置默认
            res.updateConfiguration(newConfig, res.getDisplayMetrics());
        }
        return res;
    }

    /**
     * 修改状态栏字体颜色
     * */
    protected void setAndroidNativeLightStatusBar(Activity activity, boolean dark) {
        View decor = activity.getWindow().getDecorView();
        if (dark) {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    protected void setTitleText(String msg){
        TextView textView = findViewById(R.id.titleTv);
        if (textView!=null){
            textView.setText(msg);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected boolean checkPermission(String permission){
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

}
