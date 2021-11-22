package com.szip.sportwatch.Util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;


import com.szip.sportwatch.MyApplication;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by Administrator on 2019/12/7.
 */

public class TopExceptionHandler implements Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler defaultUEH;
    private Context mContext = null;

    public TopExceptionHandler(Context context) {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        this.mContext = context;
    }

    public void uncaughtException(Thread t, Throwable e) {
        StackTraceElement[] arr = e.getStackTrace();
        String report = e.toString()+"\n\n";
        report += "--------- Stack trace ---------\n\n";
        for (int i=0; i<arr.length; i++) {
            report += "    "+arr[i].toString()+"\n";
        }
        report += "-------------------------------\n\n";

        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause

        report += "--------- Cause ---------\n\n";
        Throwable cause = e.getCause();
        if(cause != null) {
            report += cause.toString() + "\n\n";
            arr = cause.getStackTrace();
            for (int i=0; i<arr.length; i++) {
                report += "    "+arr[i].toString()+"\n";
            }
        }
        report += "-------------------------------\n\n";

//        try {
//            HttpMessgeUtil.getInstance().postAppCrashLog("iSmarport",mContext.getApplicationContext().
//                    getPackageManager().getPackageInfo("com.szip.sportwatch", 0).versionName, Build.BRAND+ Build.MODEL,report);

            FileUtil.getInstance().writeLog(MyApplication.getInstance().getPrivatePath()+
                            DateUtil.getStringDateFromSecond(Calendar.getInstance().getTimeInMillis()/1000,"yyyy-MM-dd") + "error.txt",
                    (DateUtil.getStringDateFromSecond(Calendar.getInstance().getTimeInMillis()/1000,"yyyy-MM-dd hh:mm:ss")+"|"+"error"+
                            "|"+report+"\r").getBytes());

//        } catch (IOException e1) {
//            e1.printStackTrace();
//        } catch (PackageManager.NameNotFoundException e1) {
//            e1.printStackTrace();
//        }


        defaultUEH.uncaughtException(t, e);
    }
}

