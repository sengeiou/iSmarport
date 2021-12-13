package com.szip.sportwatch.View;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import com.szip.sportwatch.Interface.OnPageViewScorllAble;
import com.szip.sportwatch.Model.DrawDataBean;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.DateUtil;
import com.szip.sportwatch.Util.DrawHelper;
import com.szip.sportwatch.Util.MathUitl;

import java.util.List;

/**
 * Created by Administrator on 2019/12/21.
 */

public class ReportScorllView extends View implements GestureDetector.OnGestureListener{

    private int width = 0,height = 0;//本页面宽，高
    private float valueWidth,valueHeight;//绘制数据区域宽高

    private float textWidth = 0, textHeight = 0;//文字宽高

    private Paint paint = new Paint();//表格画笔
    private Paint textXPaint = new Paint();//X坐标画笔
    private Paint linePaint = new Paint();//折线图画笔
    private Paint rectPaint = new Paint();//柱状图画笔

    private int textColor,color1,color2;//文字颜色,柱状图底色,柱状图颜色1,柱状图颜色2
    private float textSize;//字体大小

    private int maxValue = 100;//最高的数据
    private float mBarWidth = -1;//柱状图宽度
    private float mInterval = 0;//柱状图间隔
    private int data_num = 0;
    private int[] datasTop;//分段数据中处于上面的数据（例如深浅睡眠）
    private int[] datasBottom;//分段数据总处于下面的数据

    private int pad15;//15缩进量
    private int pad10;//10缩进量
    private int pad5;//5缩进量
    private int pad2;//2缩进量

    private int flag;//图标类型 1：计步 2：睡眠 3：心率 4：血压 5：血氧

    private String[] xMsg = new String[0];

    private Scroller mScroller;
    private GestureDetector mGestureDetector;
    protected float mSliding = 0;
    private float upPlace;

    private OnPageViewScorllAble onPageViewScorllAble;

    public ReportScorllView(Context context, AttributeSet attrs){
        super(context,attrs);
        initConfig(context,attrs);
        initView();
    }

    public ReportScorllView(Context context){
        super(context);
        initView();
    }

    public void setOnPageViewScorllAble(OnPageViewScorllAble onPageViewScorllAble) {
        this.onPageViewScorllAble = onPageViewScorllAble;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mGestureDetector = new GestureDetector(getContext(), this);
        mScroller = new Scroller(getContext());
    }

    private void initConfig(Context context, AttributeSet attrs){
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ReportView);

        color1 = a.getColor(R.styleable.ReportView_pross_color1, -1);
        color2 = a.getColor(R.styleable.ReportView_pross_color2, -1);
        textColor = a.getColor(R.styleable.ReportView_text_color, -1);
        color1 = a.getColor(R.styleable.ReportView_pross_color1, -1);
        color2 = a.getColor(R.styleable.ReportView_pross_color2, -1);
        mBarWidth = a.getDimension(R.styleable.ReportView_bar_width, MathUitl.dipToPx(10,getContext()));
        textSize = a.getDimension(R.styleable.ReportView_text_size, MathUitl.dipToPx(15,getContext()));
        data_num = a.getInteger(R.styleable.ReportView_data_number,0);
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
            textWidth = textXPaint.measureText("22:22", 0, 5);
            textHeight = MathUitl.dipToPx(6,getContext());
            mInterval = (width - mBarWidth*7-textWidth)/6;
            valueHeight = height- textHeight -pad10;
            if (data_num>7)//滑到最右边
                mSliding = -(data_num-7)*(mInterval+mBarWidth);
            Log.e("DATA******","本页面宽： " + width +"  高:" + height);
        }

        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(mSliding, 0);
        DrawXView(canvas);
        DrawStepTable(canvas);
    }



    /**
     * 画横坐标
     * */
    private void DrawXView(Canvas canvas){

        for (int i = 0;i<xMsg.length;i++){//画文字
            float w = textXPaint.measureText(xMsg[i], 0, xMsg[i].length());
            float x = textWidth/2+(mInterval+mBarWidth)*i+mBarWidth/2-w/2;
            canvas.drawText(xMsg[i], x, height-pad2, textXPaint);

        }

        //画竖线，如果数据低于七个，则画七条

        for (int i = 0;xMsg.length>7?i<xMsg.length:i<7;i++){
            float x = textWidth/2+(mInterval+mBarWidth)*i+mBarWidth/2;
            Path path = new Path();
            path.moveTo(x, valueHeight-pad5);
            path.lineTo(x,pad15+pad10);
            canvas.drawPath(path,paint);
            float top = valueHeight-mBarWidth;
            RectF rectF = new RectF(x-mBarWidth/2, top, x+mBarWidth/2, valueHeight);
            canvas.drawRoundRect(rectF,mBarWidth/2,mBarWidth/2,textXPaint);
        }


    }

    /**
     * 画报告
     */
    private void DrawStepTable(Canvas canvas){

        RectF[] rectFS = getRectFTop();
        Path path;

        path = new Path();
        if (rectFS.length!=0){
            path.moveTo(rectFS[0].centerX(), rectFS[0].top+mBarWidth/2+pad15*2);
            if (rectFS.length==1){
                DrawHelper.pathCubicTo(path, new PointF(rectFS[0].centerX(),rectFS[0].top+mBarWidth/2+pad15*2),
                        new PointF(rectFS[0].centerX()+1,rectFS[0].top+mBarWidth/2+pad15*2));

            }else {
                for (int i = 1; i< datasTop.length; i++){
                    DrawHelper.pathCubicTo(path, new PointF(rectFS[i-1].centerX(),rectFS[i-1].top+mBarWidth/2+pad15*2),
                            new PointF(rectFS[i].centerX(),rectFS[i].top+mBarWidth/2+pad15*2));
                }
            }

            linePaint.setColor(color1);
            canvas.drawPath(path, linePaint);//画心率图

            if (flag == 4){
                RectF[] rectFSBottom = getRectFBottom();
                path = new Path();
                path.moveTo(rectFSBottom[0].centerX(), rectFSBottom[0].top+mBarWidth/2+pad15*2);
                if (rectFSBottom.length==1){
                    DrawHelper.pathCubicTo(path, new PointF(rectFSBottom[0].centerX(),rectFSBottom[0].top+mBarWidth/2+pad15*2),
                            new PointF(rectFSBottom[0].centerX()+1,rectFSBottom[0].top+mBarWidth/2+pad15*2));

                }else {
                    for (int i = 1; i< datasTop.length; i++){
                        DrawHelper.pathCubicTo(path, new PointF(rectFSBottom[i-1].centerX(),rectFSBottom[i-1].top+mBarWidth/2+pad15*2),
                                new PointF(rectFSBottom[i].centerX(),rectFSBottom[i].top+mBarWidth/2+pad15*2));
                    }
                }

            }
            linePaint.setColor(color2);
            canvas.drawPath(path, linePaint);//画心率图
        }
    }

    /**
     * 获取矩形的范围
     * */
    private RectF[] getRectFTop(){
        if (datasTop ==null)
            datasTop = new int[data_num];
        RectF[] rectFS = new RectF[data_num];
        for (int i = 0; i< datasTop.length; i++){
            float x = textWidth/2+(mInterval+mBarWidth)*i;
            float top;
            if (datasTop[i] == 0)
                top = valueHeight-mBarWidth-pad5;
            else
                top = valueHeight- datasTop[i]/(float) maxValue *(valueHeight);
            rectFS[i] = new RectF(x, top, x+mBarWidth, valueHeight-pad5);
        }

        return rectFS;
    }

    private RectF[] getRectFBottom(){
        if (datasBottom ==null)
            datasBottom = new int[data_num];
        RectF[] rectFS = new RectF[data_num];
        for (int i = 0; i< datasBottom.length; i++){
            float x = textWidth/2+(mInterval+mBarWidth)*i;
            float top;
            if (datasBottom[i] == 0)
                top = valueHeight-mBarWidth-pad5;
            else
                top = valueHeight- datasBottom[i]/(float) maxValue *(valueHeight);
            rectFS[i] = new RectF(x, top, x+mBarWidth, valueHeight-pad5);
        }

        return rectFS;
    }

    /**
     * 添加数据
     * */
    public void addData(List<DrawDataBean> list){
        this.mSliding = 0;

        data_num = list.size();
        datasTop = new int[data_num];
        datasBottom = new int[data_num];
        xMsg = new String[data_num];
        for (int i = 0;i<data_num;i++){
            datasTop[i] = list.get(i).getValue();
            datasBottom[i] = list.get(i).getValue1();
            xMsg[i] = DateUtil.getStringDateFromSecond(list.get(i).getTime(),"HH:mm");
            if (datasTop[i]> maxValue)
                maxValue = datasTop[i];
        }
        if (width!=0&&data_num>7){
            mSliding = -(data_num-7)*(mInterval+mBarWidth);
        }

        postInvalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        mScroller.forceFinished(true);
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        invalidate();
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return judgeSliding(-distanceX);
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        upPlace = e2.getX();
        mScroller.fling((int)e2.getX(), (int)e2.getY(), (int)velocityX/2,
                0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
        return true;
    }

    @Override
    public void computeScroll(){
        if(mScroller.computeScrollOffset()) {
            float vSliding = mScroller.getCurrX()-upPlace;
            judgeSliding(vSliding);
        }
    }

    protected boolean judgeSliding(float tempSlided){
        if(data_num>7) {
            mSliding += tempSlided;
            if (mSliding <= -(data_num-7)*(mInterval+mBarWidth)){//划到最右端了
                if (onPageViewScorllAble!=null)
                    onPageViewScorllAble.onScroll(true);
            }else {
                if (onPageViewScorllAble!=null)
                    onPageViewScorllAble.onScroll(false);
            }
            if(mSliding>=0 || mSliding<=-(data_num-7)*(mInterval+mBarWidth)) {
                //跨越两边界了
                mSliding = mSliding>=0 ? 0 :mSliding<=-(data_num-7)*(mInterval+mBarWidth) ? -(data_num-7)*(mInterval+mBarWidth) : mSliding;
                invalidate();
                return false;
            }else {
                //正常滑动距离刷新界面
                invalidate();
                return true;
            }
        }
        return false;
    }
}
