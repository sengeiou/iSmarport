package com.szip.sportwatch.Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.R;

import static java.lang.System.in;

/**
 * 截屏
 * 
 * */
public class ScreenCapture {
	/**
	 *  指定区域截屏
	 * @param Activity activity,int x,int y,int width,int height
	 * @return filePath 文件路径
	 * */
	@RequiresApi(api = Build.VERSION_CODES.M)
	public static Uri  getBitmap(Activity activity, View layout){

		if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
			Log.d("SZIP******","没授权");
			return null;
		}

		//SYSTEM_UI_FLAG_FULLSCREEN表示全屏的意思，也就是会将状态栏隐藏
		//设置系统UI元素的可见性
		layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
		//启用或禁用绘图缓存
		layout.setDrawingCacheEnabled(true);
		//创建绘图缓存
		layout.buildDrawingCache();
		//拿到绘图缓存
		Bitmap bitmap = layout.getDrawingCache();
		bitmap = Bitmap.createBitmap(bitmap, 0,MathUitl.dipToPx(60,activity),layout.getWidth(),layout.getHeight()-MathUitl.dipToPx(60,activity));
		String filePath = null;
		try {
			// 获取内置SD卡路径

			String sdCardPath = MyApplication.getInstance().getPrivatePath();
			File fileDir = new File(sdCardPath);
			if (!fileDir.exists()) {
				fileDir.mkdir();
			}
			// 图片文件路径
			filePath= sdCardPath+"share.jpg";
			File file = new File(filePath);
			FileOutputStream os = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
			os.flush();
			os.close();
		} catch (Exception e) {
		}
		layout.destroyDrawingCache();
		layout.setSystemUiVisibility(View.VISIBLE);

		File file = new File(filePath);
		if (file.exists()) {
			Uri uri;
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
				uri = Uri.fromFile(file);
			} else {
				uri = FileProvider.getUriForFile(activity, "com.szip.sportwatch.fileprovider", file);
			}
			return uri;
		}
		return null;

	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	public static Uri  getScollerBitmap(Activity activity, ScrollView layout){

		if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
			Log.d("SZIP******","没授权");
			return null;
		}

		int h = 0;
		// 获取listView实际高度
		for (int i = 0; i < layout.getChildCount(); i++) {
			h += layout.getChildAt(i).getHeight();
			layout.getChildAt(i).setBackgroundResource(R.drawable.bg_color);
		}

		//SYSTEM_UI_FLAG_FULLSCREEN表示全屏的意思，也就是会将状态栏隐藏
		//设置系统UI元素的可见性
		layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
		//启用或禁用绘图缓存
		layout.setDrawingCacheEnabled(true);
		//创建绘图缓存
		layout.buildDrawingCache();
		//拿到绘图缓存
		Bitmap bitmap;
		bitmap = Bitmap.createBitmap(layout.getWidth(), h,
				Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(bitmap);
		layout.draw(canvas);
		String filePath = null;
		try {
			// 获取内置SD卡路径
			String sdCardPath = MyApplication.getInstance().getPrivatePath();
			File fileDir = new File(sdCardPath);
			if (!fileDir.exists()) {
				fileDir.mkdir();
			}
			// 图片文件路径
			filePath= sdCardPath+getCurrentTime()+"share.jpg";
			File file = new File(filePath);
			FileOutputStream os = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
			os.flush();
			os.close();
		} catch (Exception e) {
		}
		layout.destroyDrawingCache();
		layout.setSystemUiVisibility(View.VISIBLE);
		File file = new File(filePath);
		if (file.exists()) {
			Uri uri;
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
				uri = Uri.fromFile(file);
			} else {
				uri = FileProvider.getUriForFile(activity, "com.szip.sportwatch.fileprovider", file);
			}
			return uri;
		}
		return null;
	}

	/**
	 *获取当前时间
	 * @return
	 */
	private static  String getCurrentTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss",Locale.ENGLISH);
		return sdf.format(new java.util.Date());
	}

	public static byte[] imageToByte(Context context,int id) {
		byte[]  buffer = new byte[0];
		try {
			InputStream in = context.getResources().openRawResource(id);
			//获取文件的字节数
			int lenght = in.available();
			//创建byte数组
			buffer = new byte[lenght];
			//将文件中的数据读到byte数组中
			in.read(buffer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.d("BITMAP******",byteToHexString(buffer));
		return buffer;

	}
	public static Bitmap getPicFromBytes(byte[] bytes,
										 BitmapFactory.Options opts) {
		if (bytes != null)
			if (opts != null)
				return BitmapFactory.decodeByteArray(bytes, 0, bytes.length,
						opts);
			else
				return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		return null;
	}


	private static BitmapFactory.Options getBitmapOption(int inSampleSize){
		System.gc();
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPurgeable = true;
		options.inSampleSize = inSampleSize;
		return options;
	}

	/**
	 * byte[]转变为16进制String字符, 每个字节2位, 不足补0
	 */
	public static String byteToHexString(byte[] bytes) {
		String result = null;
		String hex = null;
		if (bytes != null && bytes.length > 0) {
			final StringBuilder stringBuilder = new StringBuilder(bytes.length);
			for (byte byteChar : bytes) {
				hex = Integer.toHexString(byteChar & 0xFF);
				if (hex.length() == 1) {
					hex = '0' + hex;
				}
				stringBuilder.append(","+hex.toUpperCase());
			}
			result = stringBuilder.toString();
		}
		return result.substring(1);
	}

}
