package com.szip.sportwatch.Activity.dial;

import android.content.Context;
import android.util.Log;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.szip.sportwatch.Adapter.DialAdapter;
import com.szip.sportwatch.BLE.BleClient;
import com.szip.sportwatch.Model.HttpBean.DialBean;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.Util.FileUtil;
import com.szip.sportwatch.Util.HttpMessgeUtil;
import com.szip.sportwatch.Util.JsonGenericsSerializator;
import com.zhy.http.okhttp.callback.GenericsCallback;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;

public class SelectDialPresenterWithFileImpl implements ISelectDialPresenter{
    private Context context;
    private ISelectDialView iSelectDialView;
    private ArrayList<DialBean.Dial> dialArrayList = new ArrayList<>();
    private int clock;

    public SelectDialPresenterWithFileImpl(Context context, ISelectDialView iSelectDialView) {
        this.context = context;
        this.iSelectDialView = iSelectDialView;
        getDialList();
    }

    private void getDialList() {
        try {
            HttpMessgeUtil.getInstance().getDialList(MyApplication.getInstance().getDialGroupId(),
                    new GenericsCallback<DialBean>(new JsonGenericsSerializator()) {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            if (iSelectDialView!=null)
                                iSelectDialView.initList(false);
                        }

                        @Override
                        public void onResponse(DialBean response, int id) {
                            if (response.getCode() == 200){
                                dialArrayList = response.getData().getList();
                                if (iSelectDialView!=null)
                                    iSelectDialView.initList(true);
                            }else {
                                if (iSelectDialView!=null)
                                    iSelectDialView.initList(false);
                            }
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getViewConfig(RecyclerView dialRv) {
        dialRv.setLayoutManager(new GridLayoutManager(context, 3));
        DialAdapter dialAdapter = new DialAdapter(dialArrayList,context);
        dialRv.setAdapter(dialAdapter);
        dialRv.setHasFixedSize(true);
        dialRv.setNestedScrollingEnabled(false);

        if (iSelectDialView!=null&&dialArrayList.size()!=0){
            iSelectDialView.setView(dialArrayList.get(0).getPreviewUrl(),
                    dialArrayList.get(0).getPlateBgUrl());
            clock = dialArrayList.get(0).getPointerNumber();
        }

        dialAdapter.setOnItemClickListener(new DialAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (position==-1){
                    if (iSelectDialView!=null)
                        iSelectDialView.setDialView(null,null);
                } else{
                    if (iSelectDialView!=null){
                        iSelectDialView.setDialView(dialArrayList.get(position).getPreviewUrl(),
                                dialArrayList.get(position).getPlateBgUrl());
                        clock = dialArrayList.get(position).getPointerNumber();
                    }
                }
            }
        });
    }

    @Override
    public void startToSendDial() {
        BleClient.getInstance().writeForSendDialFile(3, (byte) clock,0,0,null);
    }

    private int index = 0;
    private byte fileDatas[];
    private Timer timer;
    private TimerTask timerTask;
    private int page;
    private int ackPakage = 0;
    private boolean isError = false;

    @Override
    public void sendDial(String resultUri, int address) {
        if (resultUri != null) {
            resultUri = MyApplication.getInstance().getPrivatePath()+resultUri;
            InputStream in;
            try {
                in = new FileInputStream(resultUri);
                byte[] datas =  FileUtil.getInstance().toByteArray(in);
                int num = datas.length/175/100;
                num = datas.length/175%100 == 0 ? num : num + 1;
                if (iSelectDialView != null)
                    iSelectDialView.setDialProgress(num);
                in.close();
                fileDatas = datas;
                index = address;
                page = 0;
                newTimerTask(0);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            newTimerTask(0);
        }
    }

    @Override
    public void resumeSendDial(int pageNum) {
        if (!isError){
            isError = true;
            removeTimeTask();
            page = pageNum*175;
            newTimerTask(500);
        }
    }

    @Override
    public void setViewDestory() {
        iSelectDialView = null;
    }

    private void sendByte(){
        byte[] newDatas;
        int len = (fileDatas.length-index- page >175)?175:(fileDatas.length-index- page);
        if (len<0)
            return;
        newDatas = new byte[len];
        System.arraycopy(fileDatas, page+index,newDatas,0,len);
        BleClient.getInstance().writeForSendDialFile(4,(byte) 0,index+page, page/175,newDatas);
        page+=175;
        if (page>=fileDatas.length-index){
            if(timer!=null){
                removeTimeTask();
            }
            BleClient.getInstance().writeForSendDialFile(5,(byte) 0,0,0,null);
            return;
        }
        ackPakage++;
        if (ackPakage==100&&timer!=null){
            removeTimeTask();
        }
    }

    private void newTimerTask(long delay){
        isError = false;
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (!isError)
                    sendByte();
            }
        };
        timer.schedule(timerTask,delay,20);
    }

    private void removeTimeTask(){
        timer.cancel();
        timer = null;
        ackPakage = 0;
    }

}
