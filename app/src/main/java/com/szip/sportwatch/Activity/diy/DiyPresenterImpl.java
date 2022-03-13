package com.szip.sportwatch.Activity.diy;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.szip.sportwatch.Adapter.DIYAdapter;
import com.szip.sportwatch.BLE.EXCDController;
import com.szip.sportwatch.Model.HttpBean.DialBean;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.MathUitl;
import com.yalantis.ucrop.UCrop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class DiyPresenterImpl implements IDiyPresenter{
    private Handler handler;
    private Context context;
    private IDiyView iDiyView;

    public DiyPresenterImpl(Context context, IDiyView iDiyView) {
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
                    iDiyView.setDialView(dialArrayList.get(position).getPointerImg(),"",dialArrayList.get(position).getPointerNumber());
                }
            }
        });
    }

    @Override
    public void sendDial(Uri resultUri, int clock) {
        if (resultUri!=null){
            int PAGENUM = 8000;//分包长度
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
            byte[] datas = baos.toByteArray();
            int num = datas.length/PAGENUM;
            num = datas.length%PAGENUM==0?num:num+1;
            if (iDiyView!=null)
                iDiyView.setDialProgress(num,context.getString(R.string.diyDailing));
            byte[] newDatas;
            for (int i=0;i<datas.length;i+=PAGENUM){
                int len = (datas.length-i>PAGENUM)?PAGENUM:(datas.length-i);
                newDatas = new byte[len];
                System.arraycopy(datas,i,newDatas,0,len);
                EXCDController.getInstance().writeForSendImage(newDatas,i/PAGENUM+1,num,clock,MathUitl.getClockStyle(clock));
            }
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

    @Override
    public void startToSendDial() {

    }

    @Override
    public void sendDialDiy(String resultUri, int address) {

    }

    @Override
    public void resumeSendDial(int page) {

    }
}
