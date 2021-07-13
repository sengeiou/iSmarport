package com.szip.sportwatch.View;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.MapView;

public class MyRelativeLayout extends RelativeLayout {
    private MyScrollView scrollView;
    private boolean isTouch = false;
    private MapView mapView;
    private com.amap.api.maps.MapView gaodeView;
    public MyRelativeLayout(Context context) {
        super(context);
    }

    public MyRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setScrollView(MyScrollView scrollView, com.amap.api.maps.MapView gaodeView, MapView mapView) {
        this.scrollView = scrollView;
        this.gaodeView = gaodeView;
        this.mapView = mapView;
    }


    //当点击到地图的时候，让ScrollView不拦截事件，把事件传递到子View
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            scrollView.requestDisallowInterceptTouchEvent(false);
            isTouch = false;
        } else {
            scrollView.requestDisallowInterceptTouchEvent(true);
            isTouch = true;
            if (mapView.getVisibility()!=GONE)
                mapView.dispatchTouchEvent(ev);
            else
                gaodeView.dispatchTouchEvent(ev);
        }
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isTouch){
            if (mapView.getVisibility()!=GONE)
                mapView.dispatchTouchEvent(ev);
            else
                gaodeView.dispatchTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

}
