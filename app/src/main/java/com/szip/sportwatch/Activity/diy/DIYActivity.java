package com.szip.sportwatch.Activity.diy;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.szip.sportwatch.Activity.BaseActivity;
import com.szip.sportwatch.BLE.BleClient;
import com.szip.sportwatch.BLE.EXCDController;
import com.szip.sportwatch.Model.EvenBusModel.UpdateDIYView;
import com.szip.sportwatch.Model.EvenBusModel.UpdateDialView;
import com.szip.sportwatch.Model.FileSendConst;
import com.szip.sportwatch.Model.HttpBean.DialBean;
import com.szip.sportwatch.Model.SendDialModel;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Service.MainService;
import com.szip.sportwatch.Util.FileUtil;
import com.szip.sportwatch.Util.MathUitl;
import com.szip.sportwatch.Util.ProgressHudModel;
import com.szip.sportwatch.Util.StatusBarCompat;
import com.szip.sportwatch.View.CircularImageView;
import com.yalantis.ucrop.UCrop;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class DIYActivity extends BaseActivity implements IDiyView{

    private RecyclerView clockRv;
    private Uri resultUri;
    private String pictureUrl;

    private CircularImageView backgroundIv;
    private ImageView clockIv,diyIv;

    private boolean isSendPic = false;
    private IDiyPresenter iDiyPresenter;
    private int clock = -1;

    private String faceType = "";
    private ArrayList<DialBean.Dial> dialArrayList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_diy);
        if (MyApplication.getInstance().isMtk()){
            iDiyPresenter = new DiyPresenterImpl(getApplicationContext(),this);
        } else{
            iDiyPresenter = new DiyPresenterImpl06(getApplicationContext(),this);
        }
        faceType = MyApplication.getInstance().getFaceType();
        StatusBarCompat.translucentStatusBar(this,true);
        setAndroidNativeLightStatusBar(this,true);
        EventBus.getDefault().register(this);
        Bundle bundle = getIntent().getBundleExtra("data");
        dialArrayList = (ArrayList<DialBean.Dial>) bundle.getSerializable("list");
        initView();
        initEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isSendPic){
            ProgressHudModel.newInstance().diss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        iDiyPresenter.setViewDestory();
        EventBus.getDefault().unregister(this);
        FileUtil.getInstance().deleteFile(MyApplication.getInstance().getPrivatePath()+"crop");
        FileUtil.getInstance().deleteFile(MyApplication.getInstance().getPrivatePath()+"camera");
    }

    private void initEvent() {
        findViewById(R.id.diyIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });
        findViewById(R.id.backIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.rightIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 保存成功
                if (resultUri==null){
                    showToast(getString(R.string.upDownBackground));
                    return;
                }
                if (clock==-1){
                    showToast(getString(R.string.chooseClock));
                    return;
                }

                if (pictureUrl==null)
                    return;

                if (!ProgressHudModel.newInstance().isShow()){
                    ProgressHudModel.newInstance().show(DIYActivity.this,getString(R.string.loading),
                            getString(R.string.connect_error),10000);
                    if (MyApplication.getInstance().isMtk())
                        EXCDController.getInstance().initDialInfo();
                    else{
                        boolean hasFile = MainService.getInstance().downloadFirmsoft(pictureUrl);
                        if(hasFile)
                            BleClient.getInstance().writeForSendPicture(0,clock,0,0,new byte[0]);
                    }

                }
            }
        });
    }

    private void initView() {
        setTitleText(getString(R.string.diyDail));
        clockRv = findViewById(R.id.clockRv);
        clockIv = findViewById(R.id.clockIv);
        diyIv = findViewById(R.id.diyIv);
        iDiyPresenter.getViewConfig(clockRv,dialArrayList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == UCrop.RESULT_ERROR) {
            Log.d("DATA******", "URI = " + data.getData());
            showToast(getString(R.string.crop_pic_failed));
            return;
        }
        switch (requestCode) {
            case 1: {
                if (data == null || data.getData() == null)
                    return;
                Log.d("DATA******", "URI1 = " + data.getData());
                FileUtil.getInstance().writeUriSdcardFile(data.getData());
                File file = new File(MyApplication.getInstance().getPrivatePath() + "camera");
                if (file.exists()) {
                    Uri uri;
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        uri = Uri.fromFile(file);
                    } else {
                        uri = FileProvider.getUriForFile(this, "com.szip.sportwatch.fileprovider", file);
                    }
                    iDiyPresenter.cropPhoto(uri);
                }

            }
            break;
            case UCrop.REQUEST_CROP: {
                if (data != null) {
                    if (!MathUitl.toJpgFile()){
                        showToast(getString(R.string.crop_pic_failed1));
                        return;
                    }

                    resultUri = UCrop.getOutput(data);
                    try {
                        backgroundIv.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (findViewById(R.id.bottomRl).getVisibility() == View.GONE) {
                        findViewById(R.id.bottomRl).setVisibility(View.VISIBLE);
                    }
                }
            }
            break;
        }
    }

    /**
     * 更新数据显示
     * */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateView(UpdateDialView updateView){
        if(updateView.getType()== FileSendConst.PROGRESS){//进度+1
            iDiyPresenter.sendDial(null,-1);
            ProgressHudModel.newInstance().setProgress();
        }else if (updateView.getType()==FileSendConst.FINISH){//完成
            isSendPic = false;
            ProgressHudModel.newInstance().diss();
            showToast(getString(R.string.diyDailOK));
        }else if (updateView.getType()==FileSendConst.ERROR){//失败
            isSendPic = false;
            ProgressHudModel.newInstance().diss();
            showToast(getString(R.string.diyDailError1));
        }else if (updateView.getType()==FileSendConst.SEND_BIN){//开始发送bin文件
            ProgressHudModel.newInstance().diss();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    iDiyPresenter.startToSendDial();
                }
            },100);

        }else {
            isSendPic = true;
            ProgressHudModel.newInstance().diss();
            iDiyPresenter.sendDial(resultUri,clock);
        }
    }

    /**
     * 更新数据显示
     * */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateDIYView(UpdateDIYView updateView){
        if(updateView.getType()== FileSendConst.PROGRESS){//进度+1
            iDiyPresenter.sendDialDiy(null,-1);
            ProgressHudModel.newInstance().setProgress();
        }else if (updateView.getType()==FileSendConst.FINISH){//完成
            isSendPic = false;
            ProgressHudModel.newInstance().diss();
            showToast(getString(R.string.diyDailOK));
        }else if (updateView.getType()==FileSendConst.ERROR){//失败
            isSendPic = false;
            ProgressHudModel.newInstance().diss();
            showToast(getString(R.string.diyDailError1));
        }else if (updateView.getType()==FileSendConst.START_SEND){//准备开始
            Log.i("data******","准备发送数据");
            isSendPic = true;
            ProgressHudModel.newInstance().diss();
            String fileNames[] = pictureUrl.split("/");
            iDiyPresenter.sendDialDiy(fileNames[fileNames.length-1],updateView.getData());
        }else if (updateView.getType() == FileSendConst.CONTINUE){//断点续传
            iDiyPresenter.resumeSendDial(updateView.getData());
        }else {
            isSendPic = false;
            ProgressHudModel.newInstance().diss();
            showToast(getString(R.string.diyDailOK));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSendPicture(SendDialModel sendDialModel){
        if (sendDialModel.isLoadSuccess()){
            BleClient.getInstance().writeForSendPicture(0,clock,0,0,new byte[0]);
        }else {
            showToast(getString(R.string.httpError));
            ProgressHudModel.newInstance().diss();
        }
    }

    @Override
    public void setView(boolean isCircle) {
        if (isCircle){
            diyIv.setImageResource(R.mipmap.diy_c);
            backgroundIv = findViewById(R.id.backgroundIv_c);
        }else {
            if (faceType.equals("320*385")){
                diyIv.setImageResource(R.mipmap.diy_06);
                backgroundIv = findViewById(R.id.backgroundIv_r06);
            }else {
                backgroundIv = findViewById(R.id.backgroundIv_r);
            }
        }
    }

    @Override
    public void setDialView(String dial,String pictureUrl, int clock) {
        Glide.with(this).load(dial).into(clockIv);
        this.pictureUrl = pictureUrl;
        this.clock = clock;
    }

    @Override
    public void getCropPhoto(UCrop uCrop) {
        uCrop.start(this);
    }

    @Override
    public void setDialProgress(int num,String str) {
        ProgressHudModel.newInstance().showWithPie(DIYActivity.this,str,num,
                getString(R.string.diyDailError),30*1000);
    }
}
