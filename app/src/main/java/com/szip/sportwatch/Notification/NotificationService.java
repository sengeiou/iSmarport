
package com.szip.sportwatch.Notification;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mediatek.ctrl.notification.NotificationEventListener;
import com.szip.sportwatch.MyApplication;

import java.util.HashSet;
import java.util.Map;

public class NotificationService implements NotificationEventListener {
    private static final String TAG = "AppManager/NotificationService";

    public void notifyBlockListChanged(String appId) {

    }

    @Override
    public void notifyNotificationDeleted(String msgId) {
        // delete the notification
        Intent intent = new Intent();
        intent.setAction("android.deletion.notification");
        intent.putExtra("msgid", Integer.valueOf(msgId));
        Context context = MyApplication.getInstance().getApplicationContext();
        context.startService(intent);
    }

    @Override
    public void notifyNotificationActionOperate(String msgId, String actionId) {
        NotificationSyncList.getInstance().handleNotificationAction(msgId,actionId);
    }

    @Override
    public void clearAllNotificationData() {
        NotificationSyncList.getInstance().clearSyncList();
    }

}
