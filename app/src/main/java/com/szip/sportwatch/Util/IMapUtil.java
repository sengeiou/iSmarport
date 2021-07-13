package com.szip.sportwatch.Util;

import android.os.Bundle;
import android.view.MotionEvent;

public interface IMapUtil {
    void setLatlng(String[] lats,String[] lngs);
    void moveCamera();
    void addMarker();
    void addPolyline();
    void onResume();
    void onDestroy();
    void onCreate(Bundle savedInstanceState);
}
