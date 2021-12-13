package com.szip.sportwatch.Util;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.szip.sportwatch.MyApplication;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import static android.os.Environment.MEDIA_MOUNTED;
import static android.os.Environment.getExternalStorageState;

public class FileUtil {
    private static FileUtil mInstance;
    private Context context;

    private FileUtil(){

    }

    public static FileUtil getInstance()
    {
        if (mInstance == null)
        {
            synchronized (FileUtil.class)
            {
                if (mInstance == null)
                {
                    mInstance = new FileUtil();
                }
            }
        }
        return mInstance;
    }

    public void initFile(Context context){
        this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void writeFileSdcardFile(String fileName, byte[] writeStr){
//        if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
//            Log.d("DATA******","没授权");
//            return;
//        }
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000);

        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Log.d("DATA******","路径 = "+uri);
        try {
            OutputStream fout = context.getContentResolver().openOutputStream(uri);
            fout.write(writeStr);
            fout.close();
        } catch (Exception e) {
            Log.d("DATA******","保存失败 = "+e.getMessage());
            e.printStackTrace();
        }
    }

    public void writeUriSdcardFile(Uri uri){

        try {
            AssetFileDescriptor audioAsset = context.getContentResolver()
                    .openAssetFileDescriptor(uri, "r");
            InputStream in = audioAsset.createInputStream();
            OutputStream out = new FileOutputStream(MyApplication.getInstance().getPrivatePath()+"camera");
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeLog(String logPath,byte[] datas){
        File file;
        FileOutputStream fos = null;

        try {
            file = new File(logPath);
            fos = new FileOutputStream(file, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (fos==null){
            return;
        }
        try {
            fos.write(datas);
            fos.flush();
            fos.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteFile(String fileName){
        if(fileName!=null) {
            File file = new File(fileName);
            if (file != null)
                file.delete();
        }
    }

    public byte[] toByteArray(InputStream in) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 4];
        int n = 0;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }
}
