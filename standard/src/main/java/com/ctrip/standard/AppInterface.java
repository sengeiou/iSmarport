package com.ctrip.standard;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * @author Zhenhua on 2018/3/7.
 * @email zhshan@ctrip.com ^.^
 */

public interface AppInterface {
    void setContentView(int layoutResID);

    <T extends View> T findViewById(int id);

    void onCreate(Bundle savedInstanceState);
    void onCreate();
    void onStart();

    void onResume();

    void onDestroy();

    void onPause();

    void onSaveInstanceState(Bundle outState);

    /**
     * 需要宿主app注入给插件app上下文
     */
    void attach(Activity activity);
    /**
     * 需要宿主app注入给插件app上下文
     */
    void setContext(Service service);

    int onStartCommand(Intent intent, int flags, int startId);
}
