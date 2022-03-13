package com.szip.sportwatch.Activity.dial;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.szip.sportwatch.Activity.BaseActivity;
import com.szip.sportwatch.Activity.diy.DIYActivity;
import com.szip.sportwatch.Model.EvenBusModel.UpdateDialView;
import com.szip.sportwatch.Model.FileSendConst;
import com.szip.sportwatch.Model.HttpBean.DialBean;
import com.szip.sportwatch.Model.SendDialModel;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Service.MainService;
import com.szip.sportwatch.Util.HttpMessgeUtil;
import com.szip.sportwatch.Util.JsonGenericsSerializator;
import com.szip.sportwatch.Util.ProgressHudModel;
import com.szip.sportwatch.Util.StatusBarCompat;
import com.zhy.http.okhttp.callback.GenericsCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;

public class SelectDialActivity extends BaseActivity implements ISelectDialView{


    private ISelectDialPresenter iSelectDialPresenter;
    private String pictureUrl;
    private ImageView dialIv,changeIv;

    private String faceType = "";
    private boolean isCircle = false;
    private boolean isSendPic = false;

    private ArrayList<DialBean.Dial> dialArrayList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_select_dial);
        StatusBarCompat.translucentStatusBar(this,true);
        setAndroidNativeLightStatusBar(this,true);
        isCircle = MyApplication.getInstance().isCircle();
        faceType = MyApplication.getInstance().getFaceType();
        getDialList();
        EventBus.getDefault().register(this);
    }

    private boolean isFileDial(){
        if (dialArrayList.get(0).getPlateBgUrl().indexOf(".bin")<0)
            return false;
        else
            return true;
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
        iSelectDialPresenter.setViewDeStory();
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
                if (!ProgressHudModel.newInstance().isShow()&&pictureUrl!=null){
                    ProgressHudModel.newInstance().show(SelectDialActivity.this,getString(R.string.loading),
                            getString(R.string.connect_error),40000);
                    boolean hasFile = MainService.getInstance().downloadFirmsoft(pictureUrl);
                    if(hasFile)
                        iSelectDialPresenter.startToSendDial();
                }
            }
        });
    }

    private void initView() {
        setTitleText(getString(R.string.face));
        RecyclerView dialRv = findViewById(R.id.dialRv);
        changeIv = findViewById(R.id.changeIv);
        iSelectDialPresenter.getViewConfig(dialRv,dialArrayList);
    }

    /**
     * 更新数据显示
     * */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdataView(UpdateDialView updateView){
        if(updateView.getType()== FileSendConst.PROGRESS){//进度+1
            iSelectDialPresenter.sendDial(null,-1);
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
            iSelectDialPresenter.sendDial(fileNames[fileNames.length-1],updateView.getData());
        }else if (updateView.getType() == FileSendConst.CONTINUE){//断点续传
            iSelectDialPresenter.resumeSendDial(updateView.getData());
        }else {
            isSendPic = false;
            ProgressHudModel.newInstance().diss();
            showToast(getString(R.string.diyDailOK));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSendPicture(SendDialModel sendDialModel){
        if (sendDialModel.isLoadSuccess()){
            iSelectDialPresenter.startToSendDial();
        }else {
            showToast(getString(R.string.httpError));
            ProgressHudModel.newInstance().diss();
        }
    }

    @Override
    public void initList(boolean initSuccess) {
        if (!initSuccess)
            showToast(getString(R.string.httpError));
        if (MyApplication.getInstance().isMtk()) {
            iSelectDialPresenter = new SelectDialPresenterImpl(getApplicationContext(),this);
        } else{
            if (dialArrayList!=null&&dialArrayList.size()!=0){
                if (isFileDial())
                    iSelectDialPresenter = new SelectDialPresenterWithFileImpl(getApplicationContext(),this);
                else
                    iSelectDialPresenter = new SelectDialPresenterImpl06(getApplicationContext(),this);
            }
        }
        initView();
        initEvent();
    }

    @Override
    public void setView(String id, String pictureId) {
        if (isCircle){
            changeIv.setImageResource(R.mipmap.change_watch_c);
            dialIv = findViewById(R.id.dialIv_c);
        }else {
            if (faceType.equals("320*385")){
                changeIv.setImageResource(R.mipmap.change_watch_06);
                dialIv = findViewById(R.id.dialIv_r06);
            }else {
                dialIv = findViewById(R.id.dialIv_r);
            }
        }
        this.pictureUrl = pictureId;
        Glide.with(this).load(id).into(dialIv);
    }

    @Override
    public void setDialView(String dialId, String pictureId) {
        if (dialId==null){
            ArrayList<DialBean.Dial> list = new ArrayList<>();
            for (DialBean.Dial dial:dialArrayList){
                if (dial.getPointerImg()!=null)
                    list.add(dial);
            }
            Intent intent = new Intent(SelectDialActivity.this, DIYActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("list",list);
            intent.putExtra("data",bundle);
            startActivity(intent);
            finish();
        }else {
            this.pictureUrl = pictureId;
            Glide.with(this).load(dialId).into(dialIv);
        }
    }

    @Override
    public void setDialProgress(int max) {
        ProgressHudModel.newInstance().showWithPie(this,getString(R.string.diyDailing),max,
                getString(R.string.diyDailError),30*1000);
    }

    private void getDialList() {
        try {
            HttpMessgeUtil.getInstance().getDialList(MyApplication.getInstance().getDialGroupId(),
                    new GenericsCallback<DialBean>(new JsonGenericsSerializator()) {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            initList(false);
                        }
                        @Override
                        public void onResponse(DialBean response, int id) {
                            if (response.getCode() == 200){
                                dialArrayList = response.getData().getList();
                                initList(true);
                            }else {
                                initList(false);
                            }
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
