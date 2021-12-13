package com.szip.sportwatch.View;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.DrawHelper;
import com.szip.sportwatch.Util.MathUitl;

import java.util.Locale;

import androidx.annotation.Nullable;

public class SportReportView extends View {

    private int width,height;//本页面宽，高
    private float valueWidth;//绘制数据区域宽高
    private float tableWidth,tableHeight;//表格区域宽高

    private float yTextWidth = 0, textHeight = 0;//文字宽高

    private Paint paint = new Paint();//表格画笔
    private Paint textYPaint = new Paint();//Y坐标画笔
    private Paint textXPaint = new Paint();//X坐标画笔
    private Paint rectPaint = new Paint();//柱状图画笔
    private Paint linePaint = new Paint();//折线图画笔

    private boolean  yValueAble;//X坐标，Y坐标类型
    private int textColor,color1;//文字颜色,柱状图底色,柱状图颜色1,柱状图颜色2
    private float textSize;//字体大小


    private float mBarWidth = -1;//柱状图宽度
    private float mInterval = 0;//柱状图间隔
    private int data_num = 0;//默认一个屏幕里面画的数据数
    private int[] datas;//数据

    private int pad15;//15缩进量
    private int pad10;//10缩进量
    private int pad5;//5缩进量
    private int pad2;//2缩进量

    private int sectionDraw = 3;//绘图区间的大小
    private int maxDraw;//绘图区间的最大值
    private int minDraw;//绘图区间的最小值
    private int yValueNum = 3;//Y轴的个数

    private int flag = 0;//0 默认 1 速度（Y轴除10） 2 海拔（Y轴有负值）

    public SportReportView(Context context) {
        super(context);
        initView();
    }

    public SportReportView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initConfig(context,attrs);
        initView();
    }

    private void initConfig(Context context, AttributeSet attrs){
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SportReportView);

        color1 = a.getColor(R.styleable.SportReportView_color_pross, -1);
        textColor = a.getColor(R.styleable.SportReportView_color_text, -1);
        yValueAble = a.getBoolean(R.styleable.SportReportView_value_y,false);
        mBarWidth = a.getDimension(R.styleable.SportReportView_width_bar, MathUitl.dipToPx(10,getContext()));
        textSize = a.getDimension(R.styleable.SportReportView_size_text, MathUitl.dipToPx(15,getContext()));
        data_num = a.getInteger(R.styleable.SportReportView_number_data,2);
        flag = a.getInteger(R.styleable.SportReportView_flag_y,0);
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
            textHeight = MathUitl.dipToPx(6,getContext());
            valueWidth = width- yTextWidth;
            mInterval = (valueWidth -mBarWidth*data_num)/(data_num-1);
            tableWidth = width- yTextWidth;
            tableHeight = height- textHeight -pad10;
            Log.e("DATA******","本页面宽： " + width +"  高:" + height);
        }

        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        DrawYView(canvas);
        DrawStepTable(canvas);
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
            diffCoordinate = ((float) height-pad5)/yValueNum;
            String[] yMsg = getYMsg(sectionDraw);
            for(int i = 0; i<yValueNum; i++) {
                float levelCoordinate = height-pad5-diffCoordinate*(i+1);
                Path dashPath = new Path();
                dashPath.moveTo(yTextWidth, levelCoordinate+pad5);
                dashPath.lineTo(yTextWidth +tableWidth, levelCoordinate+pad5);
                canvas.drawText(yMsg[i], yTextWidth -textXPaint.measureText(yMsg[i], 0, yMsg[i].length())-pad2,
                        levelCoordinate+ textHeight /2+pad2*3, textYPaint);
                canvas.drawPath(dashPath, paint);
            }
        }
    }


    /**
     * 画报告
     */
    private void DrawStepTable(Canvas canvas){
        if (data_num!=0){
            RectF[] rectFS = getRectFTop();
            Path path = new Path();
            path.moveTo(rectFS[0].centerX(), rectFS[0].top);
            if (rectFS.length==1){
                DrawHelper.pathCubicTo(path, new PointF(rectFS[0].centerX(),rectFS[0].top),
                        new PointF(rectFS[0].centerX(),rectFS[0].top));

            }else {
                for (int i = 1; i< datas.length; i++){
                    DrawHelper.pathCubicTo(path, new PointF(rectFS[i-1].centerX(),rectFS[i-1].top),
                            new PointF(rectFS[i].centerX(),rectFS[i].top));
                }
            }

            canvas.drawPath(path, linePaint);//画心率图
        }

    }


    /**
     * 获取矩形的范围
     * */
    private RectF[] getRectFTop(){
        if (datas ==null)
            datas = new int[data_num];
        RectF[] rectFS = new RectF[datas.length];
        for (int i = 0; i< datas.length; i++) {
            float x = yTextWidth +(yTextWidth ==0?pad15:(pad5))+(mInterval+mBarWidth)*i;
            float top;
            if (datas[i] == minDraw)
                top = height-mBarWidth;
            else
                top = (datas[i]-minDraw)/(float) sectionDraw *(height-pad5)<mBarWidth?
                        height-mBarWidth:height - (datas[i]-minDraw)/(float) sectionDraw *(height-pad5);
            rectFS[i] = new RectF(x, top, x+mBarWidth, height);
        }
        return rectFS;
    }


    /**
     *获取Y轴坐标数据
     * */
    private String[] getYMsg(int maxValue){
        String[] yMsg= new String[yValueNum];
        for (int i = 0;i<yValueNum;i++){
            if (flag == 1){
                yMsg[i] = String.format(Locale.ENGLISH,"%.1f",((maxValue/yValueNum)*(i+1)+minDraw)/10f);
            }else{
                yMsg[i] = String.format(Locale.ENGLISH,"%d",(maxValue/yValueNum)*(i+1)+minDraw);
            }
            if (yValueAble&&yTextWidth<textYPaint.measureText(yMsg[i]))
                yTextWidth = textYPaint.measureText(yMsg[i])+pad2;
        }
        return yMsg;
    }


    /**
     * 添加数据
     * */
    public void addData(String[] list){

        if (list!=null&&list.length!=0){
            if (list.length==1&&list[0].equals("")){
                datas = new int[0];
                data_num = 0;
            }else {
                data_num = list.length;
                datas = new int[list.length];
                if(data_num<2)
                    data_num = 2;
                maxDraw = Integer.valueOf(list[0]);
                minDraw = Integer.valueOf(list[0]);
                for (int i = 0;i<list.length;i++){
                    datas[i] = Integer.valueOf(list[i]);
                    if (minDraw >datas[i])
                        minDraw = datas[i];
                    if (maxDraw <datas[i])
                        maxDraw = datas[i];
                }
                initTableData();
            }
        } else {
            datas = new int[0];
            data_num = 0;
        }
        mInterval = (valueWidth -mBarWidth*data_num)/(data_num-1);
        postInvalidate();
    }

    /**
     * 计算表格Y坐标区间的各项数据
     * */
    private void initTableData(){
        sectionDraw = (int)((maxDraw - minDraw)*1.5);
        if (sectionDraw == 0){//数据没有波动
            sectionDraw = 3;
            minDraw -= 1;
        }else {
            if (sectionDraw%3!=0)
                sectionDraw += 3-sectionDraw%3;
            minDraw = minDraw -(sectionDraw-(maxDraw - minDraw))/2;
        }
    }

}
