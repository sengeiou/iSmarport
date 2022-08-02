package com.szip.sportwatch.Notification;

import android.content.Context;
import android.util.Log;


import com.szip.sportwatch.MyApplication;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used for save application list. Their notification will
 * be pushed to remote device. AppList is a single class.
 */
public final class AppList {
    // Debugging
    private static final String TAG = "AppManager/AppList";

    private static final String SAVE_FILE_NAME = "MyCandyAppList";

    public static final String MAX_APP = "MaxApp";

    public static final CharSequence BATTERYLOW_APPID = "com.mtk.btnotification.batterylow";

    public static final CharSequence SMSRESULT_APPID = "com.mtk.btnotification.smsresult";

    public static final int CREATE_LENTH = 3;

    private static final AppList mInstance = new AppList();

    private Map<Object, Object> mAppList = null;

    private Context mContext = null;

    private AppList() {
        Log.i(TAG, "AppList(), AppList created!");

        mContext = MyApplication.getInstance().getApplicationContext();
    }

    /**
     * Return the instance of AppList class.
     *
     * @return the AppList instance
     */
    public static AppList getInstance() {
        return mInstance;
    }

    /**
     * Return the passed application list.
     *
     * @return the AppList list
     */
    public Map<Object, Object> getAppList() {
        if (mAppList == null) {
            loadAppListFromFile();
        }

        Log.i(TAG, "getAppList(), mAppList = " + mAppList.toString());
        return mAppList;
    }

    @SuppressWarnings("unchecked")
    private void loadAppListFromFile() {
        Log.i(TAG, "loadIgnoreListFromFile(),  file_name= " + SAVE_FILE_NAME);
        ObjectInputStream inputStream = null;

        if (mAppList == null) {
            try {
                inputStream = new ObjectInputStream(mContext.openFileInput(SAVE_FILE_NAME));
                Object obj = (inputStream.readObject());
                mAppList = (Map<Object, Object>) obj;
                inputStream.close();
            } catch (ClassNotFoundException exception) {
                exception.printStackTrace();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        if (mAppList == null) {
            mAppList = new HashMap<Object, Object>();
            mAppList.put(10,"com.tencent.mobileqq");//QQ
            mAppList.put(11,"com.tencent.mm");//微信
            mAppList.put(12,"com.instagram.android");//instagram
            mAppList.put(13,"com.twitter.android");//推特
            mAppList.put(14,"com.whatsapp");//whatsapp
            mAppList.put(15,"com.facebook.katana");//facebook
            mAppList.put(16,"com.facebook.orca");//facebook message
            mAppList.put(17,"com.skype.rover");//facebook message
            mAppList.put(18,"com.linkedin.android");//facebook message
            mAppList.put(19,"jp.naver.line.android");//facebook message
            mAppList.put(20,"com.snapchat.android");//facebook message
            mAppList.put(21,"com.pinterest");//facebook message
            mAppList.put(22,"com.google.android.apps.plus");//facebook message
            mAppList.put(23,"com.tumblr");//facebook message
            mAppList.put(24,"com.viber.voip");//facebook message
            mAppList.put(25,"com.vkontakte.android");//facebook message
            mAppList.put(26,"org.telegram.messenger");//facebook message
            mAppList.put(27,"com.zhiliaoapp.musically");//facebook message
            mAppList.put("MaxApp",27);
        }
    }

    /**
     * Save passed applications to file.
     *
     * @param appList passed applications list
     */
    public void saveAppList(Map<Object, Object> appList) {
        Log.i(TAG, "saveAppList(),  file_name= " + SAVE_FILE_NAME);

        FileOutputStream fileoutputstream;
        ObjectOutputStream objectoutputstream;

        try {
            fileoutputstream = mContext.openFileOutput(SAVE_FILE_NAME, Context.MODE_PRIVATE);
            objectoutputstream = new ObjectOutputStream(fileoutputstream);
            objectoutputstream.writeObject(appList);
            objectoutputstream.close();
            fileoutputstream.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return;
        }

        mAppList = appList;
        Log.i(TAG, "saveAppList(),  mAppList= " + mAppList);
    }
}
