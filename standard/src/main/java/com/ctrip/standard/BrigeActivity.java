package com.ctrip.standard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.File;

public class BrigeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brige);
        PluginManager.getInstance().setContext(this);
    }

    public void onClick(View view) {
        /**
         * 点击跳往插件app的activity，一律跳转到PRoxyActivity
         */
        Intent intent = new Intent(this, FotaProxyActivity.class);
        intent.putExtra("className", PluginManager.getInstance().getEntryName());
        startActivity(intent);
    }

    public void load(View view) {
        /**
         * 事先放置到SD卡根目录的plugin.apk
         * 现实场景中是有服务端下发
         */
        File file = this.getExternalFilesDir( "/fotaplugin-release.apk");
        Log.d("DATA******","FILE = "+file.getAbsoluteFile().getPath());
        PluginManager.getInstance().loadPath(file.getAbsoluteFile().getPath());
    }
}