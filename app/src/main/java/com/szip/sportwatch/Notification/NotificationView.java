package com.szip.sportwatch.Notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.szip.sportwatch.Activity.main.MainActivity;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.FileUtil;

public class NotificationView {
    private Context context;
    private static NotificationView mInstance;



    private NotificationView(){
    }

    public void init(Context context){
        this.context = context;
    }

    public static NotificationView getInstance()
    {
        if (mInstance == null)
        {
            synchronized (FileUtil.class)
            {
                if (mInstance == null)
                {
                    mInstance = new NotificationView();
                }
            }
        }
        return mInstance;
    }
    private NotificationManager notificationManager;
    public Notification getNotify(boolean state){
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        notificationManager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //        获取Notification实例
        Notification notification=new NotificationCompat.Builder(context,"0103")
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setContentText(state?context.getResources().getString(R.string.connected):context.getResources().getString(R.string.disConnect))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(false) //点击通知栏后是否消失
                .setColor(Color.parseColor("#2CBDF2"))
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.launch_logo))
                .setSmallIcon(R.mipmap.small)
                // 设置点击通知栏后跳转地址
                .setContentIntent(PendingIntent.getActivity(context, 1,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .build();
//        添加渠道
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("0103", "iSmarport", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("description");
            notificationManager.createNotificationChannel(channel);
        }
        // 设置常驻 Flag
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        return notification;
    }
}
