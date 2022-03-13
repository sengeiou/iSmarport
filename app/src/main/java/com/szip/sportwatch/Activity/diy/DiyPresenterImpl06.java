package com.szip.sportwatch.Activity.diy;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.szip.sportwatch.Adapter.DIYAdapter;
import com.szip.sportwatch.BLE.BleClient;
import com.szip.sportwatch.Model.HttpBean.DialBean;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.FileUtil;
import com.szip.sportwatch.Util.ScreenCapture;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class DiyPresenterImpl06 implements IDiyPresenter{

    private Handler handler;
    private Context context;
    private IDiyView iDiyView;

    private int clock;

    public DiyPresenterImpl06(Context context, IDiyView iDiyView) {
        this.context = context;
        this.iDiyView = iDiyView;
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void getViewConfig(RecyclerView dialRv, final ArrayList<DialBean.Dial> dialArrayList) {
        dialRv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        final boolean isCircle = MyApplication.getInstance().isCircle();
        DIYAdapter diyAdapter = new DIYAdapter(dialArrayList,context);
        dialRv.setAdapter(diyAdapter);
        dialRv.setHasFixedSize(true);
        dialRv.setNestedScrollingEnabled(false);

        if (iDiyView!=null)
            iDiyView.setView(isCircle);


        diyAdapter.setOnItemClickListener(new DIYAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (iDiyView!=null){
                    iDiyView.setDialView(dialArrayList.get(position).getPointerImg(),dialArrayList.get(position).getPlateBgUrl(),
                            dialArrayList.get(position).getPointerNumber());
                    clock = dialArrayList.get(position).getPointerNumber();
                }
            }
        });
    }
    private int i = 0;
    private byte datas[];
    @Override
    public void sendDial(Uri resultUri, int clock) {
        if (resultUri!=null){
            final int PAGENUM = 128;//分包长度
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileInputStream fis;
            try {
                fis = new FileInputStream(new File(resultUri.getPath()));
                byte[] buf = new byte[1024];
                int n;
                while (-1 != (n = fis.read(buf)))
                    baos.write(buf, 0, n);
            } catch (Exception e) {
                e.printStackTrace();
            }
            final byte[] datas = baos.toByteArray();
            int num = datas.length/PAGENUM;
            num = datas.length%PAGENUM==0?num:num+1;
            if (iDiyView!=null)
                iDiyView.setDialProgress(num,context.getString(R.string.diy_send_background));
            this.datas = datas;
            this.i = 0;
        }
        sendByte();
    }

    private void sendByte(){
        if (i>=datas.length)
            return;
        byte[] newDatas;
        int len = (datas.length- i >128)?128:(datas.length- i);
        newDatas = new byte[len];
        System.arraycopy(datas, i,newDatas,0,len);
        BleClient.getInstance().writeForSendPicture(1,0,0, i/128,newDatas);
        i+=128;
        if (i>=datas.length){
            BleClient.getInstance().writeForSendPicture(2,0,0,0,new byte[0]);
        }
    }

    @Override
    public void cropPhoto(Uri uri) {
        try {
            String[] spaceType = MyApplication.getInstance().getFaceType().split("\\*");
            Uri path = uri;
            //临时用一个名字用来保存裁剪后的图片
            String fileName = MyApplication.getInstance().getPrivatePath()+"crop";
            File file = new File(fileName);
            file.getParentFile().mkdirs();
            Uri target = Uri.fromFile(file);
            UCrop.Options options = new UCrop.Options();
            options.setToolbarColor(context.getResources().getColor(R.color.rayblue));
            options.setStatusBarColor(context.getResources().getColor(R.color.rayblue));
            options.setActiveWidgetColor(context.getResources().getColor(R.color.rayblue));
            options.setCompressionQuality(80);
            UCrop uCrop = UCrop.of(path, target)
                    .withAspectRatio(Float.valueOf(spaceType[0])/Float.valueOf(spaceType[1]), 1f)
                    .withMaxResultSize(Integer.valueOf(spaceType[0]), Integer.valueOf(spaceType[1]))
                    .withOptions(options);
            if (iDiyView!=null){
                iDiyView.getCropPhoto(uCrop);
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void setViewDestory() {
        iDiyView = null;
    }

    private int index = 0;
    private byte fileDatas[];
    private Timer timer;
    private TimerTask timerTask;
    private int page;
    private int ackPakage = 0;
    private boolean isError = false;

    @Override
    public void startToSendDial() {
        BleClient.getInstance().writeForSendDialFile(6, (byte) clock,0,0,null);
    }

    @Override
    public void sendDialDiy(String resultUri, int address) {
        if (resultUri != null) {
            resultUri = MyApplication.getInstance().getPrivatePath()+resultUri;
            InputStream in;
            try {
                in = new FileInputStream(resultUri);
                byte[] datas =  FileUtil.getInstance().toByteArray(in);
                int num = datas.length/175/100;
                num = datas.length/175%100 == 0 ? num : num + 1;
                if (iDiyView != null)
                    iDiyView.setDialProgress(num,context.getString(R.string.diy_send_bin));
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

    private void sendByteDiy(){
        byte[] newDatas;
        int len = (fileDatas.length-index- page >175)?175:(fileDatas.length-index- page);
        if (len<0)
            return;
        newDatas = new byte[len];
        System.arraycopy(fileDatas, page+index,newDatas,0,len);
        BleClient.getInstance().writeForSendDialFile(7,(byte) 0,index+page, page/175,newDatas);
        page+=175;
        if (page>=fileDatas.length-index){
            if(timer!=null){
                removeTimeTask();
            }
            BleClient.getInstance().writeForSendDialFile(8,(byte) 0,0,0,null);
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
                    sendByteDiy();
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
