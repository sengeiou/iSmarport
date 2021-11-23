package com.szip.sportwatch.Activity.dial;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.szip.sportwatch.Adapter.DialAdapter;
import com.szip.sportwatch.BLE.EXCDController;
import com.szip.sportwatch.Model.HttpBean.DialBean;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.FileUtil;
import com.szip.sportwatch.Util.HttpMessgeUtil;
import com.szip.sportwatch.Util.JsonGenericsSerializator;
import com.szip.sportwatch.Util.MathUitl;
import com.szip.sportwatch.Util.ScreenCapture;
import com.zhy.http.okhttp.callback.GenericsCallback;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import okhttp3.Call;

public class SelectDialPresenterImpl implements ISelectDialPresenter{

    private Context context;
    private ISelectDialView iSelectDialView;
    private ArrayList<DialBean.Dial> dialArrayList = new ArrayList<>();
    private int clock;

    public SelectDialPresenterImpl(Context context, ISelectDialView iSelectDialView) {
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
        EXCDController.getInstance().initDialInfo();
    }

    @Override
    public void sendDial(String resultUri, int address) {
        if (resultUri!=null){
            resultUri = MyApplication.getInstance().getPrivatePath()+resultUri;
            int PAGENUM = 8000;//分包长度
            InputStream in = null;
            try {
                in = new FileInputStream(resultUri);
                byte[] datas = FileUtil.getInstance().toByteArray(in);
                in.close();
                int num = datas.length/PAGENUM;
                num = datas.length%PAGENUM==0?num:num+1;

                if (iSelectDialView!=null)
                    iSelectDialView.setDialProgress(num);

                if (datas.length<=PAGENUM){
                    EXCDController.getInstance().writeForSendImage(datas,1,num,clock, MathUitl.getClockStyle(clock));
                }else {
                    byte[] newDatas;
                    for (int i=0;i<datas.length;i+=PAGENUM){
                        int len = (datas.length-i>PAGENUM)?PAGENUM:(datas.length-i);
                        newDatas = new byte[len];
                        System.arraycopy(datas,i,newDatas,0,len);
                        EXCDController.getInstance().writeForSendImage(newDatas,i/PAGENUM+1,num,clock,MathUitl.getClockStyle(clock));
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void resumeSendDial(int page) {

    }

    @Override
    public void setViewDestory() {
        iSelectDialView = null;
    }


}
