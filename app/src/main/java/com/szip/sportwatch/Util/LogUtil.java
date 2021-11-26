package com.szip.sportwatch.Util;

import android.content.Context;
import android.util.Log;

import com.szip.sportwatch.MyApplication;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

public class LogUtil {
    private static LogUtil mInstance;
    private Context context;
    private LogUtil(){
    }

    public static LogUtil getInstance()
    {
        if (mInstance == null)
        {
            synchronized (LogUtil.class)
            {
                if (mInstance == null)
                {
                    mInstance = new LogUtil();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context){
        this.context = context;
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(context));
    }

    public void logd(String tag,String msg){
        if (context!=null){
            Log.d(tag,msg);
            if (!tag.equals("DATA******"))
                return;
            else
                writeEvent(tag,msg);
        }

    }

    public void loge(String tag,String msg){
        if (context!=null){
            Log.e(tag,msg);
            if (!tag.equals("DATA******"))
                return;
            else
                writeEventForHttp(tag,msg);
        }
    }



    public void writeEvent(String tag,String msg){

        FileUtil.getInstance().writeLog(MyApplication.getInstance().getPrivatePath()+
                DateUtil.getStringDateFromSecond(Calendar.getInstance().getTimeInMillis()/1000,"yyyy-MM-dd") + ".txt",
                (DateUtil.getStringDateFromSecond(Calendar.getInstance().getTimeInMillis()/1000,"yyyy-MM-dd HH:mm:ss")+"|"+tag+
                        "|"+msg+"\r").getBytes());
    }
    public void writeEventForHttp(String tag,String msg){

        FileUtil.getInstance().writeLog(MyApplication.getInstance().getPrivatePath()+
                        DateUtil.getStringDateFromSecond(Calendar.getInstance().getTimeInMillis()/1000,"yyyy-MM-dd") + "http.txt",
                (DateUtil.getStringDateFromSecond(Calendar.getInstance().getTimeInMillis()/1000,"yyyy-MM-dd HH:mm:ss")+"|"+tag+
                        "|"+msg+"\r").getBytes());
    }

}
