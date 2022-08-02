package com.szip.sportwatch.BLE;


import android.util.Log;

import com.mediatek.ctrl.notification.NotificationData;
import com.mediatek.wearable.Controller;
import com.szip.sportwatch.DB.LoadDataUtil;
import com.szip.sportwatch.Notification.AppList;
import com.szip.sportwatch.Util.MathUitl;

import java.util.Map;

public class NotificationController extends Controller {


    private static final String sControllerTag = "NotificationController";
    private static NotificationController mInstance;

    private NotificationController() {
        super(sControllerTag, CMD_1);
    }

    public static NotificationController getInstance() {
        if (mInstance != null) {
            return mInstance;
        } else {
            mInstance = new NotificationController();
        }
        return mInstance;
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void tearDown() {
        super.tearDown();
    }

    @Override
    public void onConnectionStateChange(int state) {
        super.onConnectionStateChange(state);
    }


    public void sendNotfications(NotificationData notificationData){
        String str = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><event_report><header><category>notification</category><subType>text</subType>" +
                "<msgId>%s</msgId><action>add</action></header><body><sender>%s</sender><appId>%s</appId><title><![CDATA[%s]]></title><content>" +
                "<![CDATA[%s]]></content><timestamp>%d</timestamp></body></event_report>";

        if (notificationData==null)
            return;
        if(notificationData.getTextList()==null||notificationData.getTextList().length<2)
            return;
        String data = String.format(str,notificationData.getMsgId(), MathUitl.getApplicationName(notificationData.getPackageName()),notificationData.getAppID()
                ,notificationData.getTextList()[0], notificationData.getTextList()[1],notificationData.getWhen()/1000);
        Log.d("data******","data = "+data);
        this.send("",data.getBytes(),false,false,0);
    }

    @Override
    public void send(String s, byte[] bytes, boolean b, boolean b1, int i) {
        try {
            super.send(String.valueOf(bytes.length), bytes, b, b1, i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onReceive(byte[] bytes) {
        String data = new String(bytes);
        if (data.indexOf("block_sender")>=0){
            data = data.replace("/","");
            String param[] = data.split("<appId>");
            if (param.length==3){
                Map<Object, Object> appList = AppList.getInstance().getAppList();

                if (!MathUitl.isNumeric(param[1]))
                    return;
                int key = Integer.valueOf(param[1]);
                String packageName = (String) appList.get(key);

                if (packageName==null)
                    return;

                com.szip.sportwatch.DB.dbModel.NotificationData notificationData = LoadDataUtil.newInstance().getNotification(packageName);
                if (notificationData!=null)
                    notificationData.state = false;
                notificationData.save();
            }
        }
    }
}
