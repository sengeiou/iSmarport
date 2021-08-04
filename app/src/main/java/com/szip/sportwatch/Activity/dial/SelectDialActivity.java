package com.szip.sportwatch.Activity.dial;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.szip.sportwatch.Activity.BaseActivity;
import com.szip.sportwatch.Activity.diy.DIYActivity;
import com.szip.sportwatch.BLE.BleClient;
import com.szip.sportwatch.BLE.EXCDController;
import com.szip.sportwatch.Model.EvenBusModel.UpdateView;
import com.szip.sportwatch.Model.SendDialModel;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Service.MainService;
import com.szip.sportwatch.Util.ProgressHudModel;
import com.szip.sportwatch.Util.StatusBarCompat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SelectDialActivity extends BaseActivity implements ISelectDialView{


    private ISelectDialPresenter iSelectDialPresenter;
    private String pictureUrl;
    private int clock = -1;
    private ImageView dialIv,changeIv;
    private int progress = 0;
    private boolean isSendPic = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (MyApplication.getInstance().isMtk()) {
            setContentView(R.layout.activity_select_dial);
            iSelectDialPresenter = new SelectDialPresenterImpl(getApplicationContext(),this);
        } else{
            setContentView(R.layout.activity_select_dial06);
            iSelectDialPresenter = new SelectDialPresenterImpl06(getApplicationContext(),this);
        }
        StatusBarCompat.translucentStatusBar(this,true);
        setAndroidNativeLightStatusBar(this,true);
        EventBus.getDefault().register(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(!isSendPic){
            progress = 0;
            ProgressHudModel.newInstance().diss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    private void initEvent() {
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
                if (!ProgressHudModel.newInstance().isShow()){
                    ProgressHudModel.newInstance().show(SelectDialActivity.this,getString(R.string.loading),
                            getString(R.string.connect_error),10000);
                    MainService.getInstance().downloadFirmsoft(pictureUrl,"dial.jpg");
                }
            }
        });
    }

    private void initView() {
        setTitleText(getString(R.string.face));
        RecyclerView dialRv = findViewById(R.id.dialRv);
        changeIv = findViewById(R.id.changeIv);
        iSelectDialPresenter.getViewConfig(dialRv);
    }

    /**
     * 更新数据显示
     * */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdataView(UpdateView updateView){
        if(updateView.getState().equals("0")){//进度+1
            progress++;
            iSelectDialPresenter.sendDial(null,-1);
            ProgressHudModel.newInstance().setProgress(progress);
        }else if (updateView.getState().equals("1")){//完成
            isSendPic = false;
            progress = 0;
            ProgressHudModel.newInstance().diss();
            showToast(getString(R.string.diyDailOK));
        }else if (updateView.getState().equals("2")){//失败
            if (isSendPic){
                isSendPic = false;
                progress = 0;
                ProgressHudModel.newInstance().diss();
                showToast(getString(R.string.diyDailError));
            }
        }else {
            isSendPic = true;
            progress = 0;
            ProgressHudModel.newInstance().diss();
            iSelectDialPresenter.sendDial(MyApplication.getInstance().getPrivatePath()+"dial.jpg",clock);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSendPicture(SendDialModel sendDialModel){
        if (sendDialModel.isLoadSuccess()){
            if (MyApplication.getInstance().isMtk())
                EXCDController.getInstance().initDialInfo();
            else
                BleClient.getInstance().writeForSendPicture(0,clock,0,0,new byte[0]);
        }else {
            showToast(getString(R.string.httpError));
            ProgressHudModel.newInstance().diss();
        }
    }

    @Override
    public void initList(boolean initSuccess) {
        if (!initSuccess)
            showToast(getString(R.string.httpError));
        initView();
        initEvent();
    }

    @Override
    public void setView(String id, String pictureId, int clock) {
        if (MyApplication.getInstance().isCircle()){
            changeIv.setImageResource(R.mipmap.change_watch_c);
            dialIv = findViewById(R.id.dialIv_c);
        }else {
            dialIv = findViewById(R.id.dialIv_r);
        }
        this.pictureUrl = pictureId;
        this.clock = clock;
        Glide.with(this).load(id).into(dialIv);
    }

    @Override
    public void setDialView(String dialId, String pictureId, int clock) {
        if (dialId==null){
            startActivity(new Intent(SelectDialActivity.this, DIYActivity.class));
            finish();
        }else {
            this.pictureUrl = pictureId;
            this.clock = clock;
            Glide.with(this).load(dialId).into(dialIv);
        }
    }

    @Override
    public void setDialProgress(int max) {
        ProgressHudModel.newInstance().showWithPie(this,getString(R.string.diyDailing),max,
                getString(R.string.diyDailError),100*1000);
    }
}
