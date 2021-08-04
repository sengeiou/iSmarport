package com.szip.sportwatch.Activity.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.widget.FrameLayout;

import androidx.annotation.RequiresApi;

import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.FileUtil;
import com.szip.sportwatch.View.CameraPreview;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CameraPresenterImp implements ICameraPresenter{
    private Context context;

    private static final int FRONT = 1;//前置摄像头标记
    private static final int BACK = 2;//后置摄像头标记
    private int currentCameraType = -1;//当前打开的摄像头标记

    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaPlayer mediaPlayer;
    private SensorManager sm = null;
    private int angle;

    public CameraPresenterImp(Context context) {
        this.context = context;
    }


    @Override
    public void initCamera(FrameLayout layout) {
        mCamera = openCamera(BACK); //通过自己封装的方法，获取Camera类
        // 创建自己创建的预览类
        mPreview = new CameraPreview(context, mCamera);
        layout.addView(mPreview);//将预览类加入显示界面
    }

    @Override
    public void registerSensor() {
        if (sm == null) {
            sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
        sm.registerListener(sensorEventListener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager
                .SENSOR_DELAY_NORMAL);
    }

    @Override
    public void unRegisterSensor() {
        if (sm == null) {
            sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
        sm.unregisterListener(sensorEventListener);
    }

    @Override
    public void removeCamera() {
        if(mCamera!=null){
            mCamera.stopPreview();
            mCamera.release();
            mCamera=null;
        }
    }

    @Override
    public void changeCamera() throws IOException {
        mCamera.stopPreview();
        mCamera.release();
        if(currentCameraType == FRONT){
            mCamera = openCamera(BACK);
        }else if(currentCameraType == BACK){
            mCamera = openCamera(FRONT);
        }
        mCamera.setPreviewDisplay(mPreview.getHolder());
        mCamera.startPreview();
    }

    @Override
    public void takePicture() {
        mCamera.autoFocus(new Camera.AutoFocusCallback() { //自动聚焦
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                // 从Camera捕获图片
                mCamera.takePicture(null, null, mPicture);
                final AudioManager am=(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                if (mediaPlayer==null){
                    mediaPlayer = MediaPlayer.create(context, R.raw.camera);
                    mediaPlayer.start();
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mediaPlayer = null;
                        }
                    });
                }
            }
        });
    }

    @SuppressLint("NewApi")
    private Camera openCamera(int type){
        int frontIndex =-1;
        int backIndex = -1;
        int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for(int cameraIndex = 0; cameraIndex<cameraCount; cameraIndex++){
            Camera.getCameraInfo(cameraIndex, info);
            if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                frontIndex = cameraIndex;
            }else if(info.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
                backIndex = cameraIndex;
            }
        }
        Camera camera = null;

        currentCameraType = type;
        if(type == FRONT && frontIndex != -1){
            camera = Camera.open(frontIndex);
            camera.setDisplayOrientation(90);
        }else if(type == BACK && backIndex != -1){
            camera = Camera.open(backIndex);
            camera.setDisplayOrientation(90);
        }

        return camera;
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        //添加拍照的回调对象mPicture，mCamera.takePicture的必要参数
        //实现将拍照的图片存储到自己想要的目录

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onPictureTaken(byte[] data, final Camera camera) {
            Bitmap bMap;
            bMap = BitmapFactory.decodeByteArray(data, 0, data.length);

            Bitmap bMapRotate;
            if (angle==0||angle==180) {  //竖拍
                Matrix matrix = new Matrix();
                matrix.reset();
                if (currentCameraType==BACK)
                    matrix.postRotate(90);
                else
                    matrix.postRotate(270);
                bMapRotate = Bitmap.createBitmap(bMap, 0, 0, bMap.getWidth(),
                        bMap.getHeight(), matrix, true);
                bMap = bMapRotate;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bMap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] dataFile = baos.toByteArray();
            FileUtil.getInstance().writeFileSdcardFile(System.currentTimeMillis()+".jpg",dataFile);
            mCamera.startPreview();
        }
    };

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (Sensor.TYPE_ACCELEROMETER != event.sensor.getType()) {
                return;
            }
            float[] values = event.values;
            angle = getSensorAngle(values[0], values[1]);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private int getSensorAngle(float x, float y) {
        if (Math.abs(x) > Math.abs(y)) {
            /**
             * 横屏倾斜角度比较大
             */
            if (x > 4) {
                /**
                 * 左边倾斜
                 */
                return 270;
            } else if (x < -4) {
                /**
                 * 右边倾斜
                 */
                return 90;
            } else {
                /**
                 * 倾斜角度不够大
                 */
                return 0;
            }
        } else {
            if (y > 7) {
                /**
                 * 左边倾斜
                 */
                return 0;
            } else if (y < -7) {
                /**
                 * 右边倾斜
                 */
                return 180;
            } else {
                /**
                 * 倾斜角度不够大
                 */
                return 0;
            }
        }
    }
}
