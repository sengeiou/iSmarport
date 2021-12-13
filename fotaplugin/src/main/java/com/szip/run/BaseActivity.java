package com.szip.run;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ctrip.standard.AppInterface;

public class BaseActivity extends Activity implements AppInterface {

    protected Activity that;

    @Override
    public void attach(Activity activity) {
        //上下文注入进来了
        Log.d("DATA******","上下文注入进来了");
        this.that = activity;
    }

    @Override
    public void setContext(Service service) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return 0;
    }

    /**
     * super.setContentView(layoutResID)最终调用的是系统给我们注入的上下文
     */
    @Override
    public void setContentView(int layoutResID) {
        if (that == null) {
            super.setContentView(layoutResID);
        } else {
            that.setContentView(layoutResID);
        }
    }

    @Override
    public <T extends View> T findViewById(int id) {
        if (that == null) {
            return super.findViewById(id);
        } else {
            return that.findViewById(id);
        }
    }


    @Override
    public ClassLoader getClassLoader() {
        if (that == null) {
            return super.getClassLoader();
        } else {
            return that.getClassLoader();
        }
    }

    @NonNull
    @Override
    public LayoutInflater getLayoutInflater() {
        if (that == null) {
            return super.getLayoutInflater();
        } else {
            return that.getLayoutInflater();
        }
    }


    @Override
    public WindowManager getWindowManager() {
        if (that == null) {
            return super.getWindowManager();
        } else {
            return that.getWindowManager();
        }
    }

    @Override
    public Window getWindow() {
        if (that == null) {
            return super.getWindow();
        } else {
            return that.getWindow();
        }
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (that == null) {
            super.onCreate(savedInstanceState);
        } else {
//            that.onCreate(savedInstanceState);
        }
    }

    @Override
    public void onCreate() {

    }


    public void onStart() {
        if (that == null) {
            super.onStart();
        } else {
//            that.onStart();
        }
    }

    public void onDestroy() {
        if (that == null) {
            super.onDestroy();
        } else {
//            that.onDestroy();
        }
    }

    public void onPause() {
        if (that == null) {
            super.onPause();
        } else {
//            that.onPause();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (that == null) {
            super.onSaveInstanceState(outState);
        } else {
//            that.onSaveInstanceState(outState);
        }

    }

    public void onResume() {
        if (that == null) {
            super.onResume();
        } else {
//            that.onResume();
        }
    }

    protected void showToast(String msg){
        if (that == null){
            Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(that,msg,Toast.LENGTH_SHORT).show();
        }
    }

}
