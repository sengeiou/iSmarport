package com.szip.sportwatch.View;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.szip.sportwatch.Model.DrawDataBean;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.DateUtil;
import com.szip.sportwatch.Util.DrawHelper;
import com.szip.sportwatch.Util.MathUitl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Created by wenh on 16/10/2511:09.
 */

public class ReportView extends View {

    private int allData[];

    private int width,height;//本页面宽，高
    private float valueWidth,valueHeight;//绘制数据区域宽高
    private float tableWidth,tableHeight;//表格区域宽高

    private float yTextWidth = 0, textHeight = 0;//文字宽高

    private Paint paint = new Paint();//表格画笔
    private Paint textYPaint = new Paint();//Y坐标画笔
    private Paint textXPaint = new Paint();//X坐标画笔
    private Paint rectPaint = new Paint();//柱状图画笔
    private Paint linePaint = new Paint();//折线图画笔

    private boolean xValueAble, yValueAble;//X坐标，Y坐标类型
    private int textColor,bgColor,color1,color2;//文字颜色,柱状图底色,柱状图颜色1,柱状图颜色2
    private float textSize;//字体大小
    private boolean isBar;//是否柱状图

    private int maxValue = 100;//最高的数据
    private int maxDraw;
    private float mBarWidth = -1;//柱状图宽度
    private float mInterval = 0;//柱状图间隔
    private int data_num = 0;//默认一个屏幕里面画的数据数
    private int[] datas1;//分段数据中处于上面的数据（例如深浅睡眠）,睡眠状态
    private int[] datas2;//分段数据总处于下面的数据,睡眠时间

    private int pad15;//15缩进量
    private int pad10;//10缩进量
    private int pad5;//5缩进量
    private int pad2;//2缩进量

    private int sleepStartTime = 1320;
    private int sleepTime = 480;
    private int tableTime = 480;

    private float marginTop;//图表区域高的缩进量

    private int xValueNum;//X轴的个数

    private int yValueNum;//Y轴的个数
    private int flag;//图标类型 1：计步 2：睡眠 3：心率 4：血压 5：血氧

    private long reportDate = 0;//报告选中的日期，为0表示今天

    private boolean isF = false;//是否用华氏度计数

    public ReportView(Context context, AttributeSet attrs){
        super(context,attrs);
        initConfig(context,attrs);
        initView();
    }

    public ReportView(Context context){
        super(context);
        initView();
    }

    private void initConfig(Context context, AttributeSet attrs){
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ReportView);

        color1 = a.getColor(R.styleable.ReportView_pross_color1, -1);
        color2 = a.getColor(R.styleable.ReportView_pross_color2, -1);
        textColor = a.getColor(R.styleable.ReportView_text_color, -1);
        bgColor = a.getColor(R.styleable.ReportView_bar_bg_color, -1);
        color1 = a.getColor(R.styleable.ReportView_pross_color1, -1);
        color2 = a.getColor(R.styleable.ReportView_pross_color2, -1);
        xValueAble = a.getBoolean(R.styleable.ReportView_x_value,false);
        yValueAble = a.getBoolean(R.styleable.ReportView_y_value,false);
        mBarWidth = a.getDimension(R.styleable.ReportView_bar_width, MathUitl.dipToPx(10,getContext()));
        textSize = a.getDimension(R.styleable.ReportView_text_size, MathUitl.dipToPx(15,getContext()));
        marginTop = a.getDimension(R.styleable.ReportView_margin_top,0);
        isBar = a.getBoolean(R.styleable.ReportView_is_bar,true);
        data_num = a.getInteger(R.styleable.ReportView_data_number,7);
        yValueNum = a.getInteger(R.styleable.ReportView_y_value_num,0);
        xValueNum = a.getInteger(R.styleable.ReportView_x_value_num,0);
        flag = a.getInteger(R.styleable.ReportView_flag,1);

        maxValue = a.getInteger(R.styleable.ReportView_maxVelue,100);
        a.recycle();
        pad15 = MathUitl.dipToPx(15,context);
        pad10 = MathUitl.dipToPx(10,context);
        pad5 = MathUitl.dipToPx(5,context);
        pad2 = MathUitl.dipToPx(2,context);
    }

    private void initView(){

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(textColor);
        paint.setStrokeWidth(1.0f);

        textXPaint.setColor(textColor);
        textXPaint.setTextSize(textSize);

        textYPaint.setColor(textColor);
        textYPaint.setTextSize(textSize);

        rectPaint.setColor(color1);

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeWidth(mBarWidth);
        linePaint.setColor(color1);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed){
            width = getWidth();
            height = getHeight();
            if (yValueAble)
                yTextWidth = MathUitl.dipToPx(25,getContext());
            textHeight = MathUitl.dipToPx(6,getContext());
            valueWidth = width-pad15*2- yTextWidth;
            mInterval = (valueWidth -mBarWidth*data_num)/(data_num-1);
            valueHeight = height- textHeight -pad10-marginTop;
            tableWidth = width- yTextWidth -pad10*2;
            tableHeight = height- textHeight -pad10;


            Log.e("DATA******","本页面宽： " + width +"  高:" + height);
        }

        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        DrawYView(canvas);
        DrawXView(canvas);
        DrawStepTable(canvas);
    }


    public void setF(boolean f) {
        isF = f;
        postInvalidate();
    }

    /**
     * 画纵坐标
     * */
    private void DrawYView(Canvas canvas){
        if (yValueAble){
            textYPaint.setTextAlign(Paint.Align.LEFT);
            paint.setStrokeWidth(1f);
            DashPathEffect dashPathEffect = new DashPathEffect(new float[]{4,4},0);
            paint.setPathEffect(dashPathEffect);
            float diffCoordinate;
            if(isBar)
                diffCoordinate = (height- textHeight- pad10-pad5)/yValueNum;
            else
                diffCoordinate = ((float) height-pad5)/yValueNum;
            String[] yMsg = getYMsg(maxDraw);
            for(int i = 0; i<yValueNum; i++) {
                    if(isBar){
                        if (yValueNum == 2){
                            float levelCoordinate = tableHeight-diffCoordinate*(i+1);
                            Path dashPath = new Path();
                            dashPath.moveTo(yTextWidth, levelCoordinate);
                            dashPath.lineTo(yTextWidth+tableWidth, levelCoordinate);
                            canvas.drawText(yMsg[i], yTextWidth -textXPaint.measureText(yMsg[i], 0, yMsg[i].length())-pad2,
                                    levelCoordinate+ textHeight /2, textYPaint);
                            canvas.drawPath(dashPath, paint);
                        }else {
                            float levelCoordinate = height- textHeight- pad10-pad5-diffCoordinate*(i+1);
                            Path dashPath = new Path();
                            dashPath.moveTo(yTextWidth, levelCoordinate+pad5);
                            dashPath.lineTo(yTextWidth +tableWidth, levelCoordinate+pad5);
                            canvas.drawText(yMsg[i], yTextWidth -textXPaint.measureText(yMsg[i], 0, yMsg[i].length())-pad2,
                                    levelCoordinate+ textHeight /2+pad2*3, textYPaint);
                            canvas.drawPath(dashPath, paint);
                        }

                    }else {
                        float levelCoordinate = height-pad5-diffCoordinate*(i+1);
                        Path dashPath = new Path();
                        dashPath.moveTo(yTextWidth, levelCoordinate+pad5);
                        dashPath.lineTo(yTextWidth +tableWidth, levelCoordinate+pad5);
                        canvas.drawText(yMsg[i], yTextWidth -textXPaint.measureText(yMsg[i], 0, yMsg[i].length())-pad2,
                                levelCoordinate+ textHeight /2+pad2*3, textYPaint);
                        canvas.drawPath(dashPath, paint);
                    }
            }
            if (yValueNum == 2){
                Paint paint1 = new Paint();
                paint1.setStyle(Paint.Style.STROKE);
                paint1.setColor(textColor);
                paint1.setStrokeWidth(1.5f);
                Path dashPath = new Path();
                dashPath.moveTo(yTextWidth, tableHeight-pad2);
                dashPath.lineTo(yTextWidth+tableWidth, tableHeight-pad2);
                canvas.drawPath(dashPath, paint1);
            }

        }
    }

    /**
     * 画横坐标
     * */
    private void DrawXView(Canvas canvas){
        if (xValueAble){
            String[] xMsg = getXMsg();
            for (int i = 0;i<xMsg.length;i++){
                float w = textXPaint.measureText(xMsg[i], 0, xMsg[i].length());
                float x;
                if (xMsg.length == 5){
                    if (i == 0)
                        x = yTextWidth +(yTextWidth ==0?pad15:(pad5))+mBarWidth/2-w/2;
                    else
                        x = yTextWidth +(yTextWidth ==0?pad15:(pad5))+(mInterval+mBarWidth)*(7*i-1)+mBarWidth/2-w/2;
                }else if (xMsg.length == 3){
                    x = yTextWidth +(yTextWidth ==0?pad15:(pad5))+(mInterval+mBarWidth)*(6*i+5)+mBarWidth/2-w/2;
                }else if (xMsg.length == 2){
                    x = yTextWidth+(tableWidth-w)*i;
                }else
                    x = yTextWidth +(yTextWidth ==0?pad15:(pad5))+(mInterval+mBarWidth)*i+mBarWidth/2-w/2;
                canvas.drawText(xMsg[i], x, height-pad2, textXPaint);
//                Path dashPath = new Path();
//                dashPath.moveTo(x, tableHeight);
            }
            if (xValueNum == -1){
                Paint mPointPaint = new Paint();
                Bitmap awakeBit = BitmapFactory.decodeResource(getContext().getResources(),R.mipmap.report_day_icon_sun);
                Rect mSrcRect = new Rect(0, 0, awakeBit.getWidth(), awakeBit.getHeight());
                Rect mDestRect = new Rect((int)(pad15+mBarWidth/2-awakeBit.getWidth()/2),
                        height-pad2-awakeBit.getHeight(),
                        (int)(pad15+mBarWidth/2+awakeBit.getWidth()/2),
                        height-pad2);
                canvas.drawBitmap(awakeBit,mSrcRect,mDestRect,mPointPaint);
                awakeBit = BitmapFactory.decodeResource(getContext().getResources(),R.mipmap.report_day_icon_moon);
                Rect mDestRect1 = new Rect((int)(pad15+mBarWidth/2-awakeBit.getWidth()/2+(mInterval+mBarWidth)*23),
                        height-pad2-awakeBit.getHeight(),
                        (int)(pad15+(mInterval+mBarWidth)*23+mBarWidth/2+awakeBit.getWidth()/2),
                        height-pad2);
                canvas.drawBitmap(awakeBit,mSrcRect,mDestRect1,mPointPaint);
            }
        }
    }

    /**
     * 画报告
     */
    private void DrawStepTable(Canvas canvas){
        if (isBar){//柱状图
            if (maxDraw == 2){
                RectF[] rectFS = getRectSleep();
                for (int i = 0; i< rectFS.length; i++){
                    if (datas1[i] == 2)
                        rectPaint.setColor(color1);
                    else
                        rectPaint.setColor(color2);
                    canvas.drawRect(rectFS[i], rectPaint);
                }
            }else {
                Paint mPointPaint = new Paint();
                RectF[] rectFS = getRectFTop();
                RectF[] rectFSBottom = getRectFBottom();
                for (int i = 0; i< rectFS.length; i++){
                    if (datas1[i]==0){//如果数值为0，画默认柱状图
                        rectPaint.setColor(bgColor);
                        canvas.drawRoundRect(rectFS[i],mBarWidth/2,mBarWidth/2, rectPaint);
                    } else{
                        rectPaint.setColor(color1);
                        canvas.drawRoundRect(rectFS[i],mBarWidth/2,mBarWidth/2, rectPaint);//画第一段柱状图
                        if (datas2[i]!=0){//如果存在第二段柱状图，画第二段柱状图
                            rectPaint.setColor(color2);
                            canvas.drawRoundRect(rectFSBottom[i],mBarWidth/2,mBarWidth/2, rectPaint);
                        }
                        if (datas2[i]!=0&&(rectFSBottom[i].top-rectFS[i].top)>=mBarWidth/2&&
                                (rectFSBottom[i].bottom-rectFSBottom[i].top)>=mBarWidth*1.5f){//如果第一段数据比第二段数据高超过半个圆角，则补上直角
                            RectF rectF= new RectF(rectFSBottom[i].left, rectFSBottom[i].top,
                                    rectFSBottom[i].right, rectFSBottom[i].top+mBarWidth);
                            canvas.drawRect(rectF, rectPaint);
                        }
                    }

                    if (data_num==7&&(flag == 1||flag == 2)){//如果是计步或者睡眠的周视图，要画详情数据
                        Bitmap arrow = BitmapFactory.decodeResource(getContext().getResources(),R.mipmap.report_month_arrow);
                        Rect mSrcRect = new Rect(0, 0, arrow.getWidth(), arrow.getHeight());
                        Rect mDestRect = new Rect((int)(rectFS[i].centerX()-arrow.getWidth()/2),
                                (int)(rectFS[i].top-arrow.getHeight()-pad2),
                                (int)(rectFS[i].centerX()+arrow.getWidth()/2),
                                (int)(rectFS[i].top-pad2));
                        canvas.drawBitmap(arrow,mSrcRect,mDestRect,mPointPaint);
                        String str;
                        if (flag == 1)
                            str = String.format(Locale.ENGLISH,"%d", datas1[i]);
                        else
                            str = String.format(Locale.ENGLISH,"%dh%dm", datas1[i]/60,datas1[i]%60);
                        float w = textXPaint.measureText(str, 0, str.length());
                        canvas.drawText(str, mDestRect.centerX()-w/2, mDestRect.top-pad2, textXPaint);
                    }
                }
            }
        }else {
            if (flag == 3){//心率
                if (data_num!=0){
                    RectF[] rectFS = getRectFTop();
                    Path path = new Path();
                    path.moveTo(rectFS[0].centerX(), rectFS[0].top);
                    if (rectFS.length==1){
                        DrawHelper.pathCubicTo(path, new PointF(rectFS[0].centerX(),rectFS[0].top),
                                new PointF(rectFS[0].centerX(),rectFS[0].top));

                    }else {
                        for (int i = 1; i< datas1.length; i++){
                            DrawHelper.pathCubicTo(path, new PointF(rectFS[i-1].centerX(),rectFS[i-1].top),
                                    new PointF(rectFS[i].centerX(),rectFS[i].top));
                        }
                    }

                    canvas.drawPath(path, linePaint);//画心率图
                }
            }
        }
    }

    /**
     * 获取睡眠报告矩形的范围
     * */
    private RectF[] getRectSleep(){
        if (datas1 ==null)
            datas1 = new int[data_num];
        RectF[] rectFS = new RectF[data_num];
        float start = yTextWidth;
        for (int i = 0; i< datas1.length; i++){
            float x = start;
            float width = datas2[i]/(float)tableTime*tableWidth;
            start = x+width;
            float top =  tableHeight-(tableHeight- textHeight)/2*(datas1[i]==0?1:datas1[i]);
            rectFS[i] = new RectF(x, top, x+width, tableHeight-pad5);
        }

        return rectFS;
    }

    /**
     * 获取矩形的范围
     * */
    private RectF[] getRectFTop(){
        if (isBar){
            if (datas1 ==null)
                datas1 = new int[data_num];
            RectF[] rectFS = new RectF[datas1.length];
            for (int i = 0; i< datas1.length; i++){
                float x = yTextWidth +(yTextWidth ==0?pad15:(pad5))+(mInterval+mBarWidth)*i;
                float top;
                if (datas1[i] == 0)
                    top = height- textHeight- pad10-(data_num == 7?mBarWidth/3:mBarWidth);
                else
                    top = datas1[i]/(float) maxDraw *(height- textHeight- pad10-pad5)<(data_num == 7?mBarWidth/3:mBarWidth)?
                            height- textHeight- pad10-(data_num == 7?mBarWidth/3:mBarWidth):
                            height- textHeight- pad10-datas1[i]/(float) maxDraw *(height- textHeight- pad10-pad5);

                if (data_num==7&&(flag == 1||flag == 2)&&
                        (height- textHeight- pad10-(data_num == 7?mBarWidth/3:mBarWidth)-top>15)){
                    top+=pad15;
                }
                rectFS[i] = new RectF(x, top, x+mBarWidth, height- textHeight- pad10);
            }

            return rectFS;
        }else {
            if (datas1 ==null)
                datas1 = new int[data_num];
            RectF[] rectFS = new RectF[datas1.length];
            for (int i = 0; i< datas1.length; i++){
                float x = yTextWidth +(yTextWidth ==0?pad15:(pad5))+(mInterval+mBarWidth)*i;
                float top;
                if (datas1[i] == 0)
                    top = height-mBarWidth;
                else
                    top = datas1[i]/(float) maxDraw *(height-pad5)<mBarWidth?
                            height-mBarWidth:height - datas1[i]/(float) maxDraw *(height-pad5);
                rectFS[i] = new RectF(x, top, x+mBarWidth, height);
            }

            return rectFS;
        }

    }

    private RectF[] getRectFBottom(){
        if (datas2 ==null)
            datas2 = new int[data_num];
        RectF[] rectFS = new RectF[data_num];
        for (int i = 0; i< datas2.length; i++){
            float x = yTextWidth +(yTextWidth ==0?pad15:(pad5))+(mInterval+mBarWidth)*i;
            float top;
            top = datas2[i]/(float) maxDraw *(height- textHeight- pad10-pad5)<(data_num == 7?mBarWidth/3:mBarWidth)?
                    height- textHeight- pad10-(data_num == 7?mBarWidth/3:mBarWidth):
                    height- textHeight- pad10-datas2[i]/(float) maxDraw *(height- textHeight- pad10-pad5);
//            if (data_num==7&&(flag == 1||flag == 2)&&top!=height- textHeight- pad10-(data_num == 7?mBarWidth/3:mBarWidth)){
//                top+=pad15;
//            }
            rectFS[i] = new RectF(x, top, x+mBarWidth, height- textHeight- pad10);
        }

        return rectFS;
    }

    /**
     *获取Y轴坐标数据
     * */
    private String[] getYMsg(int maxValue){
        String[] yMsg= new String[yValueNum];
        if (yValueNum==2){
            yMsg[0] = "";
            yMsg[1] = "";
        }else {
            for (int i = 0;i<yValueNum;i++) {
                if (isF) {
                    yMsg[i] = String.format("%d", (16 / yValueNum) * (i + 1) + 94);
                } else {
                    if (flag == 1)
                        yMsg[i] = String.format(Locale.ENGLISH, "%dk", (maxValue / yValueNum) * (i + 1) / 1000);
                    else if (flag == 2)
                        yMsg[i] = String.format(Locale.ENGLISH, "%dh", (maxValue / yValueNum) * (i + 1) / 60);
                    else if (flag == 3)
                        yMsg[i] = i == yValueNum - 1 ? maxDraw + 40 + "" : String.format(Locale.ENGLISH, "%d", (maxValue / yValueNum) * (i + 1) + 40);
                    else if (flag == 4)
                        yMsg[i] = String.format(Locale.ENGLISH, "%d", (maxValue / yValueNum) * (i + 1) + 45);
                    else if (flag == 5)
                        yMsg[i] = String.format(Locale.ENGLISH, "%d", (maxValue / yValueNum) * (i + 1) + 70);
                    else if (flag == 6)
                        yMsg[i] = String.format(Locale.ENGLISH, "%d", ((maxValue / yValueNum) * (i + 1) + 340) / 10);
                    else
                        yMsg[i] = String.format(Locale.ENGLISH, "%d", (maxValue / yValueNum) * (i + 1));
                }
            }
        }

        return yMsg;
    }

    /**
     * 获取X轴坐标数据
     * */
    private String[] getXMsg(){
        String[] xMsg = new String[xValueNum>0?xValueNum:0];
        if (xValueNum == 7){//周的横坐标
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(reportDate*1000);
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            for (int i = 0;i<7;i++){
                    switch ((day-6+i)>0?(day-6+i):(day+1+i)){
                        case 1:
                            xMsg[i] = getContext().getResources().getString(R.string.sun);
                            break;
                        case 2:
                            xMsg[i] = getContext().getResources().getString(R.string.mon);
                            break;
                        case 3:
                            xMsg[i] = getContext().getResources().getString(R.string.tues);
                            break;
                        case 4:
                            xMsg[i] = getContext().getResources().getString(R.string.wed);
                            break;
                        case 5:
                            xMsg[i] = getContext().getResources().getString(R.string.thus);
                            break;
                        case 6:
                            xMsg[i] = getContext().getResources().getString(R.string.fri);
                            break;
                        case 7:
                            xMsg[i] = getContext().getResources().getString(R.string.sta);
                            break;
                    }
            }
            if (reportDate== DateUtil.getTimeOfToday())
                xMsg[6] = getContext().getResources().getString(R.string.today);
        }else if (xValueNum == 5){//月的横坐标
            SimpleDateFormat format = new SimpleDateFormat("MM.dd",Locale.ENGLISH);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(reportDate*1000);
            long time = calendar.getTimeInMillis()/1000;
            for (int i =0;i<5;i++){
                if (i==0){
                    Date date = new Date((time-27*24*60*60)*1000);
                    xMsg[i] = format.format(date);
                }else {
                    Date date = new Date((time-(4-i)*7*24*60*60)*1000);
                    xMsg[i] = format.format(date);
                }

            }
            if (reportDate== DateUtil.getTimeOfToday())
                xMsg[4] = getContext().getResources().getString(R.string.today);
        }else if (xValueNum == 12){//年的横坐标
            SimpleDateFormat format = new SimpleDateFormat("MM",Locale.ENGLISH);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(reportDate*1000);
            for (int i = 11;i>=0;i--){
                if (i!=11)
                    calendar.add(Calendar.MONTH,-1);
                Date date = new Date(calendar.getTimeInMillis());
                xMsg[i] = format.format(date);
            }
        }else if (xValueNum == -1){
            xMsg = new String[3];
            xMsg[0] = "6am";
            xMsg[1] = "12pm";
            xMsg[2] = "18pm";
        }else if (xValueNum == 2){
            xMsg = new String[2];
            xMsg[0] = String.format(Locale.ENGLISH,"%02d:%02d",sleepStartTime/60,sleepStartTime%60);
            int time = (sleepStartTime+sleepTime)>24*60?(sleepStartTime+sleepTime)%(24*60):sleepStartTime+sleepTime;
            xMsg[1] = String.format(Locale.ENGLISH,"%02d:%02d",time/60,time%60);
        }
        return xMsg;
    }

    public void setReportDate(long reportDate) {
        this.reportDate = reportDate;
    }

    /**
     * 设置睡眠起始时间
     * */
    public void setSleepState(int sleepStartTime,int sleepTime,int tableTime){
        this.sleepStartTime = sleepStartTime;
        this.sleepTime = sleepTime;
        this.tableTime = tableTime;
    }

    /**
     * 添加数据
     * */
    public void addData(List<DrawDataBean> list){
        maxDraw = maxValue;
        if (list!=null){
            data_num = list.size();
            datas1 = new int[list.size()];
            datas2 = new int[list.size()];
            if(flag==3&&data_num<10&&!isBar)
                data_num = 10;
            for (int i = 0;i<list.size();i++){
                datas1[i] = list.get(i).getValue();
                datas2[i] = list.get(i).getValue1();
                if (datas1[i]> maxValue)
                    if (datas1[i]>maxDraw)
                        maxDraw = datas1[i];
            }
        } else {
            datas1 = new int[0];
            datas2 = new int[0];
            data_num = 0;
        }
        mInterval = (valueWidth -mBarWidth*data_num)/(data_num-1);
        postInvalidate();
    }

}
