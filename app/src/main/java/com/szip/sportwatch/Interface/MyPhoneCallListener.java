package com.szip.sportwatch.Interface;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.Util.LogUtil;

public class MyPhoneCallListener extends PhoneStateListener {

    private static final String TAG = "DATA******";
    protected CallListener listener;
    private boolean isInit = false;
    /**
     * 返回电话状态
     *
     * CALL_STATE_IDLE 无任何状态时
     * CALL_STATE_OFFHOOK 接起电话时
     * CALL_STATE_RINGING 电话进来时
     */


    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);
        if (isInit){
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:// 电话挂断
                    Log.d(TAG ,"电话挂断...");
                    if (listener!=null)
                        listener.onCallRinging(null,null);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK: //电话通话的状态
                    Log.d(TAG ,"正在通话...");
                    if (listener!=null)
                        listener.onCallRinging(null,null);
                    break;
                case TelephonyManager.CALL_STATE_RINGING: //电话响铃的状态
                    Log.d(TAG ,"电话响铃");
                    LogUtil.getInstance().logd("data******","num = "+incomingNumber);
                    String name = getContactNameFromPhoneBook(MyApplication.getInstance(),incomingNumber);
                    if (listener!=null)
                        listener.onCallRinging(incomingNumber,name);
                    break;
            }
        }else {
            isInit = true;
        }
    }
    //写个回调
    public void setCallListener(CallListener callListener){
        this.listener = callListener;
    }
    //回调接口
    public interface CallListener{
        void onCallRinging(String num,String name);
    }

    private String getContactNameFromPhoneBook(Context context, String phoneNum) {
        try {
            String contactName = "";
            String[] projection = { ContactsContract.PhoneLookup.DISPLAY_NAME,
                    ContactsContract.PhoneLookup.NUMBER};
            Uri uri = Uri.withAppendedPath(
                    ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(phoneNum));
            Cursor cursor = context.getContentResolver().query(uri, projection,null, null, null);
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                cursor.close();
                LogUtil.getInstance().logd("data******","contactName = "+contactName);
                return contactName;
            } else{
                return "";
            }
        }catch (SecurityException e){

        }
        return "";
    }
}
