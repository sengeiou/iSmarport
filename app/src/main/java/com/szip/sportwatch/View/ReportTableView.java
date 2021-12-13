package com.szip.sportwatch.View;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.MathUitl;

/**
 * Created by Administrator on 2019/12/21.
 */

public class ReportTableView extends View {

    private int width,height;//本页面宽，高
    private float tableWidth,tableHeight;//表格区域宽高

    private float textWidth = 0, textHeight = 0;//文字宽高

    private Paint paint = new Paint();//表格画笔
    private Paint textYPaint = new Paint();//Y坐标画笔

    private int textColor;//文字颜色,柱状图底色,柱状图颜色1,柱状图颜色2
    private float textSize;//字体大小

    private int maxValue = 100;//最高的数据

    private int pad15;//15缩进量
    private int pad10;//10缩进量
    private int pad2;//2缩进量

    private int yValueNum;//Y轴的个数
    private int flag;//图标类型 1：计步 2：睡眠 3：心率 4：血压 5：血氧

    private boolean isF = false;//是否用华氏度计数

    public ReportTableView(Context context, AttributeSet attrs){
        super(context,attrs);
        initConfig(context,attrs);
        initView();
    }

    public ReportTableView(Context context){
        super(context);
        initView();
    }

    public void setF(boolean f) {
        isF = f;
        postInvalidate();
    }

    private void initConfig(Context context, AttributeSet attrs){
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ReportView);
        textColor = a.getColor(R.styleable.ReportView_text_color, -1);
        textSize = a.getDimension(R.styleable.ReportView_text_size, MathUitl.dipToPx(15,getContext()));
        yValueNum = a.getInteger(R.styleable.ReportView_y_value_num,0);
        flag = a.getInteger(R.styleable.ReportView_flag,1);
        maxValue = a.getInteger(R.styleable.ReportView_maxVelue,100);
        a.recycle();
        pad15 = MathUitl.dipToPx(15,context);
        pad10 = MathUitl.dipToPx(10,context);
        pad2 = MathUitl.dipToPx(2,context);
    }

    private void initView(){
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(textColor);
        paint.setStrokeWidth(1.0f);
        textYPaint.setColor(textColor);
        textYPaint.setTextSize(textSize);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed){
            width = getWidth();
            height = getHeight();
            textWidth = MathUitl.dipToPx(25,getContext());
            textHeight = MathUitl.dipToPx(6,getContext());
            tableWidth = width- textWidth -pad10*2;
            tableHeight = height- textHeight -pad10;
            Log.e("DATA******","本页面宽： " + width +"  高:" + height);
        }

        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        DrawYView(canvas);
    }

    /**
     * 画纵坐标
     * */
    private void DrawYView(Canvas canvas){

        textYPaint.setTextAlign(Paint.Align.LEFT);
        if(isF){
            textYPaint.setTextSize(MathUitl.dipToPx(8,getContext()));
        }
        paint.setStrokeWidth(1f);

        float diffCoordinate = (tableHeight- textHeight)/yValueNum;
        String[] yMsg = getYMsg(maxValue);

        for(int i = 0; i<yValueNum; i++) {
            float levelCoordinate = tableHeight-diffCoordinate*(i+1);
            Path dashPath = new Path();
            dashPath.moveTo(textWidth, levelCoordinate+pad15*2);
            dashPath.lineTo(textWidth +tableWidth, levelCoordinate+pad15*2);
            canvas.drawText(yMsg[i], textWidth -textYPaint.measureText(yMsg[i], 0, yMsg[i].length())-pad2,
                    levelCoordinate+ textHeight /2+pad15*2, textYPaint);
            canvas.drawPath(dashPath, paint);

        }
    }

    /**
     *获取Y轴坐标数据
     * */
    private String[] getYMsg(int maxValue){
        String[] yMsg= new String[yValueNum];
        for (int i = 0;i<yValueNum;i++){
            if (isF){
                yMsg[i] = String.format("%.1f",(14.4/yValueNum)*(i+1)+93.2);
            }else {
                if (flag == 4)
                    yMsg[i] = String.format("%d",(maxValue/yValueNum)*(i+1)+45);
                else if (flag == 5)
                    yMsg[i] = String.format("%d",(maxValue/yValueNum)*(i+1)+70);
                else if (flag == 6)
                    yMsg[i] = String.format("%d",((maxValue/yValueNum)*(i+1)+340)/10);
                else
                    yMsg[i] = String.format("%d",(maxValue/yValueNum)*(i+1));
            }

        }
        return yMsg;
    }

}
