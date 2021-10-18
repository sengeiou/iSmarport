package com.mediatek.ctrl.map;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.Permission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class j {
    private static j rY;
    private final String TAG = "AppManager/SmsController";
    private final String rZ = "com.mtk.map.SmsController.action.SENT_RESULT";
    private final String sa = "com.mtk.map.SmsController.action.DELIVERED_RESULT";
    private final String sb = "com.mtk.map.SmsController.action.SENT_MESSAGE_ID";
    public static final String sc = "com.mtk.map.SmsController.action.SEND_MESSAGE";
    private final int sd = 1;
    private final int se = 0;
    private final int sf = 100;
    private static final String[] sg = new String[]{"_id", "subject", "date", "address", "status", "read", "person", "body", "thread_id", "type", "read"};
    private static final int sh = 0;
    private static final int si = 1;
    private static final int sj = 2;
    private static final int sk = 3;
    private static final int sl = 4;
    private static final int sm = 5;
    private static final int sn = 7;
    private static final int so = 9;
    private static final int sp = -1;
    public static String sq = null;
    public static String sr = null;
    private final Context mContext;
    private final ContentResolver mContentResolver;
    private final HashMap ss = new HashMap();

    public static j c(Context var0) {
        if (rY == null) {
            rY = new j(var0);
        }

        return rY;
    }

    private j(Context var1) {
        this.mContext = var1;
        this.mContentResolver = var1.getContentResolver();

                IntentFilter var2 = new IntentFilter();
                var2.addAction("com.mtk.map.SmsController.action.SENT_RESULT");
                var2.addAction("com.mtk.map.SmsController.action.DELIVERED_RESULT");
                var2.addAction("com.mtk.map.SmsController.action.SEND_MESSAGE");
                k var3 = new k(this);
                this.mContext.registerReceiver(var3, var2);
                SmsContentObserver var4 = new SmsContentObserver(this.mContext, this);
                this.mContentResolver.registerContentObserver(Uri.parse("content://sms/"), false, var4);
                this.mContentResolver.registerContentObserver(Uri.parse("content://mms-sms/conversataions"), false, var4);
    }

    public void onStop() {
        this.M();
    }

    public h a(int var1, int var2, String var3) {
        int var4 = this.w(var3);
        Object var5 = null;
        Object var6 = null;
        Object var7 = null;
        Object var8 = null;
        int var9 = 0;
        StringBuilder var11 = new StringBuilder();
        ArrayList var12 = new ArrayList();
        String[] var13 = sg;
        String var15 = null;
        String var16 = null;
        Uri var10;
        if (var4 != 100) {
            var10 = this.o(var4);
            if (var10 == null) {
                Log.i("AppManager/SmsController", "unrecognized mailbox uri");
                return null;
            }
        } else {
            var10 = Uri.parse("content://sms/");
        }

        Cursor var14;
        try {
            var14 = this.mContentResolver.query(var10, var13, var11.toString(), (String[])var12.toArray(new String[var12.size()]), "date DESC");
        } catch (SQLiteException var21) {
            var21.printStackTrace();
            Log.i("AppManager/SmsController", "fail to query");
            return null;
        } catch (SecurityException error){
            return null;
        }

        if (var14 == null) {
            Log.i("AppManager/SmsController", "messageCursor == null");
            return null;
        } else {
            h var17 = new h();
            Log.i("AppManager/SmsController", "messageCursor.getCount()" + var14.getCount());
            boolean var18 = false;

            while(var14.moveToNext() && (var1 == 0 || var17.E() < var1)) {
                if (var14.getInt(5) == 1) {
                    var18 = true;
                }

                String var19 = var14.getString(3);
                if (var4 == 1) {
                    var15 = var19;
                } else {
                    var16 = var19;
                }

                if ((var6 == null || ((String)var6).length() <= 0 || !this.a(this.t(var16), (String)var8, (String)var6)) && (var5 == null || ((String)var5).length() <= 0 || var4 != 1 || !this.a(this.t(var15), (String)var7, (String)var5)) && var1 > 0) {
                    i var20 = this.a(var14, var4, var2);
                    if (var20 != null) {
                        var17.a(var20);
                        ++var9;
                        var17.k(1);
                    }
                }
            }

            var14.close();
            if (var18) {
                var17.C();
            }

            return var17;
        }
    }

    public a f(long var1) {
        Log.i("AppManager/SmsController", "getMessage()");
        var1 &= 1152921504606846975L;
        Uri var3 = ContentUris.withAppendedId(Uri.parse("content://sms/"), var1);
        Cursor var4 = this.mContentResolver.query(var3, sg, (String)null, (String[])null, (String)null);
        if (var4 != null && var4.moveToFirst()) {
            String var5 = var4.getString(7);
            String var6 = var4.getString(3);
            int var7 = var4.getInt(9);
            String var8 = new String();
            String var9 = new String();
            if (var7 == 1) {
                var8 = var6;
                this.t(var6);
            } else {
                var9 = var6;
                this.t(var6);
            }

            a var10 = new a();
            var10.reset();
            l var11 = new l();
            var11.y(var8);
            var10.k(var11.toString());
            var10.m(var5);
            var11.reset();
            var11.y(var9);
            var10.l(var11.toString());
            var10.i(this.p(var4.getInt(5)));
            var4.close();
            return var10;
        } else {
            Log.i("AppManager/SmsController", "find no record for the request : id is " + var1);
            return null;
        }
    }

    public static void a(j var1,Intent var2,int var3){

    }

    public static void b(j var1,Intent var2,int var3){

    }

    public boolean a(String var1, String var2) {
        try {
            Log.i("AppManager/SmsController", "Start to Push message, the telephone is:" + var1 + " and the text is:" + var2);
        } catch (Exception var16) {
            String var4 = var16.toString();
            if (var4 == null) {
                var4 = "push error";
            }

            Log.w("AppManager/SmsController", var4);
            g var5 = new g();
            var5.setAction(6);
            MapController.getInstance(this.mContext).sendMap(var5.toString(), (byte[])null);
        }

        String var3 = null;
        long var17 = -1L;
        boolean var6 = true;
        if (var2 != null && !var2.equals("\n")) {
            var2 = var2.trim();
        }

        var3 = this.t(var1);
        if (var6) {
            ContentValues var8 = new ContentValues();
            var8.put("type", 4);
            var8.put("date", System.currentTimeMillis());
            var8.put("address", var3);
            byte var7 = 1;
            var8.put("read", Integer.valueOf(var7));
            var8.put("body", var2);
            var8.put("status", 64);
            var8.put("seen", 0);
            Uri var9 = this.mContentResolver.insert(Uri.parse("content://sms/"), var8);
            if (var9 != null) {
                Cursor var10 = this.mContentResolver.query(var9, new String[]{"_id"}, (String)null, (String[])null, (String)null);
                if (var10 != null && var10.moveToFirst()) {
                    var17 = var10.getLong(0);
                    var10.close();
                }
            }
        } else {
            var17 = -1L;
        }

        if (var3 != null) {
            SmsManager var18 = SmsManager.getDefault();
            if (var2 == null) {
                return false;
            }

            ArrayList var19 = var18.divideMessage(var2);
            ArrayList var20 = new ArrayList(var19.size());
            ArrayList var11 = new ArrayList(var19.size());

            for(int var12 = 0; var12 < var19.size(); ++var12) {
                Intent var13 = new Intent("com.mtk.map.SmsController.action.SENT_RESULT");
                Intent var14 = new Intent("com.mtk.map.SmsController.action.DELIVERED_RESULT");
                var13.putExtra("com.mtk.map.SmsController.action.SENT_MESSAGE_ID", var17);
                var14.putExtra("com.mtk.map.SmsController.action.SENT_MESSAGE_ID", var17);
                if (var12 == var19.size() - 1) {
                    String var15 = "com.mtk.map.SmsController.action.FINAL_MESSAGE";
                    var13.putExtra(var15, true);
                    var14.putExtra(var15, true);
                }

                var20.add(PendingIntent.getBroadcast(this.mContext, 0, var14, 268435456));
                var11.add(PendingIntent.getBroadcast(this.mContext, var12, var13, 268435456));
            }

            try{
                var18.sendMultipartTextMessage(var3, (String)null, var19, var11, var20);
            }catch (SecurityException error){
                Log.e("SZIP******","ERROR = "+error.getMessage());
            }

        }

        return true;
    }

    public boolean a(long var1, int var3) {
        Log.i("AppManager/SmsController", "setMessageStatus():id is " + var1 + ", state is " + var3);
        Uri var4 = ContentUris.withAppendedId(Uri.parse("content://sms/"), var1);
        String[] var5 = new String[]{"read"};
        if (var3 == -1) {
            Log.i("AppManager/SmsController", "the status to be set is invalid");
            return false;
        } else {
            Cursor var7 = this.mContentResolver.query(var4, var5, (String)null, (String[])null, (String)null);
            g var8;
            if (var7 != null && var7.moveToFirst()) {
                if (var7.getInt(0) == var3) {
                    Log.i("AppManager/SmsController", "state is same, no need to update");
                    var8 = new g();
                    var8.setAction(-5);
                    MapController.getInstance(this.mContext).sendMap(var8.toString(), (byte[])null);
                } else {
                    ContentValues var10 = new ContentValues();
                    var10.put("read", var3);
                    this.mContentResolver.update(var4, var10, (String)null, (String[])null);
                    g var9 = new g();
                    var9.setAction(5);
                    MapController.getInstance(this.mContext).sendMap(var9.toString(), (byte[])null);
                }

                var7.close();
            } else {
                var8 = new g();
                var8.setAction(-5);
                MapController.getInstance(this.mContext).sendMap(var8.toString(), (byte[])null);
            }

            return true;
        }
    }

    public boolean g(long var1) {
        Log.i("AppManager/SmsController", "deleteMessage():id is " + var1);
        Uri var4 = ContentUris.withAppendedId(Uri.parse("content://sms/"), var1);
        String[] var5 = new String[]{"type"};
        Cursor var6 = this.mContentResolver.query(var4, var5, (String)null, (String[])null, (String)null);
        boolean var3;
        if (var6 != null && var6.moveToFirst()) {
            int var9 = var6.getInt(0);
            if (var9 == 100) {
                this.mContentResolver.delete(var4, (String)null, (String[])null);
                this.ss.remove(var1);
            } else {
                ContentValues var8 = new ContentValues();
                var8.put("type", 100);
                this.mContentResolver.update(var4, var8, (String)null, (String[])null);
                this.ss.put(var1, var9);
                Log.i("AppManager/SmsController", "succeed");
            }

            g var10 = new g();
            var10.setAction(5);
            MapController.getInstance(this.mContext).sendMap(var10.toString(), (byte[])null);
            var3 = true;
            var6.close();
        } else {
            Log.i("AppManager/SmsController", "the message does not exist in SMS provider");
            g var7 = new g();
            var7.setAction(-5);
            MapController.getInstance(this.mContext).sendMap(var7.toString(), (byte[])null);
            var3 = false;
        }

        return var3;
    }

    public void M() {
        Log.i("AppManager/SmsController", "clearDeletedMessage()");
        String[] var3 = new String[]{"type"};
        Uri var1 = Uri.parse("content://sms/");
        Iterator var5 = this.ss.entrySet().iterator();

        while(var5.hasNext()) {
            Map.Entry var6 = (Map.Entry)var5.next();
            Long var2 = (Long)var6.getKey();
            var1 = ContentUris.withAppendedId(Uri.parse("content://sms/"), var2);
            Cursor var7 = this.mContentResolver.query(var1, var3, (String)null, (String[])null, (String)null);
            if (var7 != null && var7.moveToFirst()) {
                int var4 = var7.getInt(0);
                if (var4 == 100) {
                    try {
                        this.mContentResolver.delete(var1, (String)null, (String[])null);
                    } catch (IllegalArgumentException var9) {
                    }
                }
            }

            if (var7 != null) {
                var7.close();
            }
        }

        this.ss.clear();
    }

    public void a(Long var1, String var2, int var3) {
        if ("MessageDeleted".equals(this.n(var3))) {
            MapController.mKeys.add(var1);
        }

        Log.i("AppManager/SmsController", "onMessageEvent arrived: " + this.n(var3));
        XmlSerializer var4 = Xml.newSerializer();
        StringWriter var5 = new StringWriter();

        try {
            var4.setOutput(var5);
            var4.startDocument("UTF-8", false);
            var4.startTag((String)null, "MAP-event-report");
            var4.attribute((String)null, "version", "1.0");
            var4.startTag((String)null, "event");
            var4.attribute((String)null, "type", this.n(var3));
            var4.attribute((String)null, "handle", String.valueOf(var1 | 1152921504606846976L));
            var4.attribute((String)null, "folder", var2);
            var4.attribute((String)null, "msg_type", "SMS_GSM");
            var4.endTag((String)null, "event");
            var4.endTag((String)null, "MAP-event-report");
            var4.endDocument();
            var4.flush();
        } catch (Exception var9) {
            Log.e("AppManager/SmsController", "error occurred while creating xml file");
        }

        if (var5 != null) {
            try {
                byte[] var6 = var5.toString().getBytes("UTF-8");
                f var7 = new f();
                var7.setAction(7);
                var7.b(2);
                var7.c(0);
                var7.a(var6.length);
                MapController.getInstance(this.mContext).sendMapD(var7.toString(), var6);
            } catch (UnsupportedEncodingException var8) {
                var8.printStackTrace();
            }
        }

    }

    private String n(int var1) {
        switch(var1) {
            case 1:
                return "NewMessage";
            case 2:
                return "MessageDeleted";
            case 3:
                return "MessageShift";
            default:
                return null;
        }
    }

    private Uri o(int var1) {
        switch(var1) {
            case 1:
                return Uri.parse("content://sms/inbox");
            case 2:
                return Uri.parse("content://sms/sent");
            case 3:
                return Uri.parse("content://sms/draft");
            case 4:
                return Uri.parse("content://sms/outbox");
            case 5:
                return Uri.parse("content://sms/failed");
            default:
                return null;
        }
    }

    private String t(String var1) {
        if (var1 != null && var1.length() != 0) {
            var1 = var1.replaceAll(" ", "");
            var1 = var1.replaceAll("-", "");
            return var1;
        } else {
            return null;
        }
    }

    private boolean a(String[] var1, String[] var2) {
        if (var1 != null && var2 != null && var1.length != 0 && var2.length != 0) {
            String[] var6 = var2;
            int var5 = var2.length;

            for(int var4 = 0; var4 < var5; ++var4) {
                String var3 = var6[var4];
                String[] var10 = var1;
                int var9 = var1.length;

                for(int var8 = 0; var8 < var9; ++var8) {
                    String var7 = var10[var8];
                    if (var7.indexOf(var3) != 0 || var3.contains(var7)) {
                        return true;
                    }
                }
            }

            return false;
        } else {
            return false;
        }
    }

    private boolean a(String var1, String var2, String var3) {
        boolean var4 = var2 == null && var3 == null;
        if (var1 == null) {
            return false;
        } else if (var4) {
            return true;
        } else {
            if (var2 != null) {
                String[] var5 = var1.split(";");
                String[] var6 = var2.split(";");
                if (this.a(var5, var6)) {
                    return true;
                }
            }

            return var3 != null && this.u(var3) ? var1.contains(var3) : false;
        }
    }

    private boolean u(String var1) {
        int var2 = 0;
        int var3 = var1.length();

        for(int var4 = 0; var4 < var3; ++var4) {
            char var5 = var1.charAt(var4);
            if (Character.isDigit(var5)) {
                ++var2;
            } else if (var5 != '*' && var5 != '#' && var5 != 'N' && var5 != '.' && var5 != ';' && var5 != '-' && var5 != '(' && var5 != ')' && var5 != ' ' && (var5 != '+' || var2 != 0)) {
                return false;
            }
        }

        if (var2 > 0) {
            return true;
        } else {
            return false;
        }
    }

    private i a(Cursor var1, int var2, int var3) {
        i var4 = new i();
        int var5 = this.q(var1.getInt(4));
        if (var5 == -1) {
            return null;
        } else {
            int var7 = this.p(var1.getInt(5));
            if (var7 == -1) {
                return null;
            } else {
                boolean var6 = true;
                var6 = var1.getString(7) != null;
                String var8 = var1.getString(1);
                if (var8 == null) {
                    var8 = var1.getString(7);
                }

                if (var3 == 0) {
                    var8 = "";
                } else if (var8.length() > var3) {
                    var8 = var8.substring(0, var3 - 1);
                }

                XmlSerializer var9 = Xml.newSerializer();

                try {
                    StringWriter var10 = new StringWriter();
                    var9.setOutput(var10);
                    var9.startDocument("UTF-8", false);
                    var9.startTag((String)null, "MAP-msg-listing");
                    var9.attribute((String)null, (String)b.qY.get(1), var8);
                } catch (Exception var12) {
                    Log.i("AppManager/SmsController", "add xml attribute fail");
                    return null;
                }

                String var13 = var1.getString(2);
                var4.c(var1.getLong(0) | 1152921504606846976L);
                var4.setSubject(var8);
                var4.d(Long.valueOf(var13));
                String var11 = var1.getString(3);
                if (sq == null || !sq.equals(var11)) {
                    sq = var11;
                    sr = this.v(var11);
                }

                if (var2 == 1) {
                    var4.q(var11);
                    var4.p(sr);
                } else {
                    var4.s(var11);
                    var4.r(sr);
                }

                var4.G();
                if (var1.getString(7) != null) {
                    var4.setSize(var1.getString(7).length());
                } else {
                    var4.setSize(0);
                }

                var4.a(var6);
                var4.l(var5);
                var4.H();
                var4.i(var7);
                var4.J();
                var4.I();
                return var4;
            }
        }
    }

    private String v(String var1) {
        if (var1 == null) {
            return null;
        } else if (var1.equals("")) {
            return null;
        } else {
            String var2 = var1;

            try {
                Uri var3 = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(var2));
                Cursor var4 = this.mContentResolver.query(var3, new String[]{"display_name"}, (String)null, (String[])null, (String)null);
                if (var4 != null && var4.moveToFirst()) {
                    var2 = var4.getString(0);
                }

                var4.close();
                Log.i("AppManager/SmsController", "getContactName(), contactName=" + var2);
                return var2;
            } catch (Exception var5) {
                Log.i("AppManager/SmsController", "getContactName Exception");
                return var1;
            }
        }
    }

    private int p(int var1) {
        switch(var1) {
            case 0:
                return 0;
            case 1:
                return 1;
            default:
                return -1;
        }
    }

    private int w(String var1) {
        if (var1 == null) {
            return -1;
        } else if (var1.equals("inbox")) {
            return 1;
        } else if (var1.equals("outbox")) {
            return 4;
        } else if (var1.equals("failed")) {
            return 5;
        } else if (var1.equals("sent")) {
            return 2;
        } else if (var1.equals("draft")) {
            return 3;
        } else {
            return var1.equals("deleted") ? 100 : -1;
        }
    }

    private int q(int var1) {
        return 0;
    }

    private void a(Context var1, Uri var2, int var3, int var4, int var5) {
        boolean var6 = false;
        boolean var7 = false;
        switch(var3) {
            case 1:
            case 3:
                break;
            case 2:
            case 4:
                var7 = true;
                break;
            case 5:
            case 6:
                var6 = true;
                break;
            default:
                return;
        }

        ContentValues var8 = new ContentValues(3);
        var8.put("type", var3);
        var8.put("status", var5);
        if (var6) {
            var8.put("read", 0);
        } else if (var7) {
            var8.put("read", 1);
        }

        this.mContentResolver.update(var2, var8, (String)null, (String[])null);
    }

    private void a(Intent var1, int var2) {
        int var3 = var1.getIntExtra("errorCode", 0);
        long var4 = var1.getLongExtra("com.mtk.map.SmsController.action.SENT_MESSAGE_ID", -1L);
        String[] var6 = new String[]{"type"};
        Log.i("AppManager/SmsController", "handleSentResult:result is " + var2 + ", error is " + var3 + ", id is " + var4);
        Uri var7 = ContentUris.withAppendedId(Uri.parse("content://sms/"), var4);
        Cursor var8 = this.mContentResolver.query(var7, var6, (String)null, (String[])null, (String)null);
        if (var8 != null) {
            if (!var8.moveToFirst()) {
                var8.close();
            } else {
                if (var2 == -1) {
                    int var9 = var8.getInt(0);
                    if (var9 == 4) {
                        Log.i("AppManager/SmsController", "the sms is in outbox");
                        this.a(this.mContext, var7, 2, var3, -1);
                    } else {
                        Log.i("AppManager/SmsController", "the message is not in outbox:" + var9);
                    }
                } else {
                    this.a(this.mContext, var7, 5, var3, 128);
                }

                var8.close();
            }
        }
    }

    private void b(Intent var1, int var2) {
        byte[] var3 = (byte[])var1.getExtras().get("pdu");
        long var4 = var1.getLongExtra("com.mtk.map.SmsController.action.SENT_MESSAGE_ID", -1L);
        Uri var6 = ContentUris.withAppendedId(Uri.parse("content://sms/"), var4);
        String[] var7 = new String[]{"_id"};
        Log.i("AppManager/SmsController", "handleDeliverResult: id is " + var4 + " pdu is empty? " + (var3 == null) + "result is " + var2);
        if (var3 != null && var2 == -1) {
            SmsMessage var8 = SmsMessage.createFromPdu(var3);
            if (var8 != null) {
                Cursor var9 = this.mContentResolver.query(var6, var7, (String)null, (String[])null, (String)null);
                if (var9 != null && var9.moveToFirst()) {
                    ContentValues var10 = new ContentValues();

                    try {
                        var10.put("status", var8.getStatus());
                        this.mContentResolver.update(var6, var10, (String)null, (String[])null);
                    } catch (Exception var13) {
                        String var12 = var13.toString();
                        if (var12 == null) {
                            var12 = "querry error";
                        }

                        Log.w("AppManager/SmsController", var12);
                    }

                    Log.i("AppManager/SmsController", "update status");
                }

                if (var9 != null) {
                    var9.close();
                }

            }
        }
    }
}
