package com.szip.sportwatch.View;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.DrawHelper;
import com.szip.sportwatch.Util.MathUitl;

import androidx.annotation.Nullable;

import org.w3c.dom.Text;

public class SportSpeedView extends View {

    private int width,height;//本页面宽，高

    private float textWidth = 0, textHeight = 0;//文字宽高
    private float textWidth1 = 0, textHeight1 = 0;//文字宽高

    private Paint textPaint = new Paint();//X坐标画笔
    private Paint textPaint1 = new Paint();//X坐标画笔
    private Paint backPaint = new Paint();//折线图画笔
    private Paint linePaint = new Paint();//折线图画笔


    private float textSize = 20;//字体大小

    private int maxValue = 1200;//最高的数据
    private int mBarWidth = 50;//柱状图宽度
    private int mInterval = 20;//柱状图间隔
    private int[] datas;//数据

    public SportSpeedView(Context context) {
        super(context);
        initView();
    }

    public SportSpeedView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }



    private void initView(){


        textPaint.setColor(Color.parseColor("#000000"));
        textPaint.setStrokeWidth(10);
        textPaint.setTextSize(30);


        textPaint1.setColor(Color.parseColor("#ffffff"));
        textPaint1.setStrokeWidth(10);
        textPaint1.setTextSize(30);

        backPaint = new Paint();
        backPaint.setAntiAlias(true);
        backPaint.setStyle(Paint.Style.STROKE);
        backPaint.setStrokeCap(Paint.Cap.ROUND);
        backPaint.setStrokeWidth(mBarWidth);
        backPaint.setColor(0x88CECFD8);

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeWidth(mBarWidth);


    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        // 设置当前View的宽高
        if (datas!=null)
            setMeasuredDimension(measureWidth, (mBarWidth+mInterval)*datas.length);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed){
            width = getWidth();
            height = getHeight();
            textHeight = MathUitl.dipToPx(6,getContext());
            Log.e("DATA******","本页面宽： " + width +"  高:" + height);
        }

        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        DrawStepTable(canvas);
    }


    /**
     * 画报告
     */
    private void DrawStepTable(Canvas canvas){
        if (datas.length!=0){
            for (int i = 0;i<datas.length;i++){

                String speedStr = String.format("%02d'%02d''",datas[i]/60,datas[i]%60);
                String numStr = String.format("%d",i+1);
//                if (i == datas.length-1){
//                    numStr +=("(<1km)");
//                }
                textWidth = textPaint.measureText(speedStr);
                textWidth1 = textPaint1.measureText(numStr);
                Path path = new Path();
                path.moveTo(0+mBarWidth/2, (mBarWidth+mInterval)*i+mBarWidth/2);
                path.lineTo(width-mBarWidth/2-textWidth, (mBarWidth+mInterval)*i+mBarWidth/2);
                canvas.drawPath(path, backPaint);

                path = new Path();
                Shader mShader = new LinearGradient(0, mBarWidth, width*(datas[i]/(float)maxValue), mBarWidth,
                        new int[] { 0xFF2AD697, 0xFF08ADE9},
                        null, Shader.TileMode.REPEAT);
                linePaint.setShader(mShader);
                path.moveTo(0+mBarWidth/2, (mBarWidth+mInterval)*i+mBarWidth/2);
                path.lineTo((width-textWidth)*(datas[i]/(float)maxValue)-mBarWidth/2, (mBarWidth+mInterval)*i+mBarWidth/2);
                canvas.drawPath(path, linePaint);


                canvas.drawText(speedStr,width-textWidth,(mBarWidth+mInterval)*i+35,
                        textPaint);

                canvas.drawText(numStr,mBarWidth/2,(mBarWidth+mInterval)*i+35,
                        textPaint1);

            }
        }
    }

    /**
     * 添加数据
     * */
    public void addData(String[] list){
        if (list!=null&&list.length!=0){
            if (list.length==1&&list[0].equals("")){
                datas = new int[0];
            }else {
                datas = new int[list.length];
                for (int i = 0;i<list.length;i++){
                    datas[i] = Integer.valueOf(list[i]);
                    if (datas[i]>maxValue)
                        maxValue = datas[i];
                }
            }
        } else {
            datas = new int[0];
        }
        postInvalidate();
    }
}
