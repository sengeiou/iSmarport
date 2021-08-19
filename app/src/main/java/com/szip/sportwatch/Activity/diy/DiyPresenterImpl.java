package com.szip.sportwatch.Activity.diy;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.szip.sportwatch.Adapter.DIYAdapter;
import com.szip.sportwatch.BLE.EXCDController;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.MathUitl;
import com.yalantis.ucrop.UCrop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

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
    public void getViewConfig(RecyclerView dialRv) {
        dialRv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        final int [] dials;
        final int [] clock;
        final boolean isCircle = MyApplication.getInstance().isCircle();
        if (isCircle){
            dials = new int[]{R.mipmap.diy_preview_c_1_1,R.mipmap.diy_preview_c_2_1,R.mipmap.diy_preview_c_3_1,
                    R.mipmap.diy_preview_c_4_1, R.mipmap.diy_preview_c_5_1,R.mipmap.diy_preview_c_6_1, R.mipmap.diy_preview_c_7_1,
                    R.mipmap.diy_preview_c_8_1,R.mipmap.diy_preview_c_9_1, R.mipmap.diy_preview_c_10_1, R.mipmap.diy_preview_c_11_1,
                    R.mipmap.diy_preview_c_12_1,R.mipmap.diy_preview_c_13_1,R.mipmap.diy_31,R.mipmap.diy_30,R.mipmap.diy_34};

            clock = new int[]{15,25,11,17,18,19,20,21,22,23,24,26,27,31,30,34};
        }else {
            dials = new int[]{R.mipmap.diy_preview_1_1,R.mipmap.diy_preview_2_1,R.mipmap.diy_preview_3_1,R.mipmap.diy_preview_4_1,
                    R.mipmap.diy_preview_5_1,R.mipmap.diy_preview_6_1,R.mipmap.diy_preview_7_1,R.mipmap.diy_preview_9_1,
                    R.mipmap.diy_preview_10_1,R.mipmap.diy_preview_11_1,R.mipmap.diy_preview_13_1,R.mipmap.diy_preview_14_1,R.mipmap.diy_28};

            clock = new int[]{1,2,3,5,7,8,9,10,12,14,11,13,28};
        }
        DIYAdapter diyAdapter = new DIYAdapter(dials);
        dialRv.setAdapter(diyAdapter);
        dialRv.setHasFixedSize(true);
        dialRv.setNestedScrollingEnabled(false);

        if (iDiyView!=null)
            iDiyView.setView(isCircle);


        diyAdapter.setOnItemClickListener(new DIYAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (iDiyView!=null){
                    iDiyView.setDialView(dials[position],clock[position]);
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
                iDiyView.setDialProgress(num);
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
            Uri path = uri;
            //临时用一个名字用来保存裁剪后的图片
            String fileName = MyApplication.getInstance().getPrivatePath()+"crop.jpg";
            File file = new File(fileName);
            file.getParentFile().mkdirs();
            Uri target = Uri.fromFile(file);
            UCrop.Options options = new UCrop.Options();
            options.setToolbarColor(context.getResources().getColor(R.color.rayblue));
            options.setStatusBarColor(context.getResources().getColor(R.color.rayblue));
            options.setActiveWidgetColor(context.getResources().getColor(R.color.rayblue));
            UCrop uCrop = UCrop.of(path, target)
                    .withAspectRatio(1f, 1f)
                    .withMaxResultSize(240, 240)
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
}
