package com.szip.sportwatch.Activity.dial;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.szip.sportwatch.Adapter.DialAdapter;
import com.szip.sportwatch.BLE.BleClient;
import com.szip.sportwatch.Model.HttpBean.DialBean;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.FileUtil;
import com.szip.sportwatch.Util.HttpMessgeUtil;
import com.szip.sportwatch.Util.JsonGenericsSerializator;
import com.szip.sportwatch.Util.ScreenCapture;
import com.zhy.http.okhttp.callback.GenericsCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;

public class SelectDialPresenterImpl06 implements ISelectDialPresenter{

    private Context context;
    private ISelectDialView iSelectDialView;
    private ArrayList<DialBean.Dial> dialArrayList = new ArrayList<>();
    public SelectDialPresenterImpl06(Context context, ISelectDialView iSelectDialView) {
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

        if (iSelectDialView!=null&&dialArrayList.size()!=0)
            iSelectDialView.setView(dialArrayList.get(0).getPreviewUrl(),
                    dialArrayList.get(0).getPlateBgUrl(),dialArrayList.get(0).getPointerNumber());

        dialAdapter.setOnItemClickListener(new DialAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (position==-1){
                    if (iSelectDialView!=null)
                        iSelectDialView.setDialView(null,null,-1);
                } else{
                    if (iSelectDialView!=null){
                        iSelectDialView.setDialView(dialArrayList.get(position).getPreviewUrl(),
                                dialArrayList.get(position).getPlateBgUrl(),dialArrayList.get(position).getPointerNumber());
                    }
                }
            }
        });
    }

    private int i = 0;
    private byte datas[];

    @Override
    public void sendDial(String resultUri, int clock) {
        if (resultUri != null) {
            final int PAGENUM = 200;//分包长度
            InputStream in = null;
            try {
                in = new FileInputStream(MyApplication.getInstance().getPrivatePath()+"dial.jpg");
                byte[] datas =  FileUtil.getInstance().toByteArray(in);
                in.close();
                int num = datas.length / PAGENUM;
                num = datas.length % PAGENUM == 0 ? num : num + 1;
                if (iSelectDialView != null)
                    iSelectDialView.setDialProgress(num);
                this.datas = datas;
                this.i = 0;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        sendByte();
    }

    private void sendByte(){
        byte[] newDatas;
        int len = (datas.length- i >200)?200:(datas.length- i);
        newDatas = new byte[len];
        System.arraycopy(datas, i,newDatas,0,len);
        BleClient.getInstance().writeForSendPicture(1,0,0, i/200,newDatas);
        i+=200;
        if (i>=datas.length){
            BleClient.getInstance().writeForSendPicture(2,0,0,0,new byte[0]);
        }
    }
}
