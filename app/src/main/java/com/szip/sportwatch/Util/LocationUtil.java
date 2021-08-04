package com.szip.sportwatch.Util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import androidx.core.app.ActivityCompat;

import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.model.LatLng;
import com.szip.sportwatch.MyApplication;

public class LocationUtil {

    private static LocationUtil locationUtil;

    public static LocationUtil getInstance() {
        if (locationUtil == null) {
            synchronized (LocationUtil.class) {
                if (locationUtil == null) {
                    locationUtil = new LocationUtil();
                }
            }
        }
        return locationUtil;
    }


    public Location getLocation(LocationManager myLocationManager, boolean needNetwork,GpsStatus.Listener myListener, LocationListener locationListener) {
        //获取位置管理服务
        Location gpsLocation = null;
        Location netLocation = null;

        if (ActivityCompat.checkSelfPermission(MyApplication.getInstance(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        myLocationManager.addGpsStatusListener(myListener);

        if (gpsIsOpen(myLocationManager)) {
            myLocationManager.requestLocationUpdates("gps", 2000, 1, locationListener);
            gpsLocation = myLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        if (needNetwork&&netWorkIsOpen(myLocationManager)) {
            myLocationManager.requestLocationUpdates("network", 2000, 1, locationListener);
            gpsLocation = myLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        if (gpsLocation == null && netLocation == null) {
            return null;
        }
        if (gpsLocation != null && netLocation != null) {
            if (gpsLocation.getTime() < netLocation.getTime()) {
                gpsLocation = null;
                return netLocation;
            } else {
                netLocation = null;
                return gpsLocation;
            }
        }
        if (gpsLocation == null) {
            return netLocation;
        } else {
            return gpsLocation;
        }
    }

    public Location getGaoLocation(Location location, Context context){
        if (context.getResources().getConfiguration().locale.getCountry().equals("CN")){
            LatLng mark = null;
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            //初始化坐标转换类
            CoordinateConverter converter = new CoordinateConverter(context);
            converter.from(CoordinateConverter.CoordType.GPS);
            //设置需要转换的坐标
            try {
                converter.coord(new LatLng(latitude,longitude));
                mark=converter.convert();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mark!=null){
                location.setLatitude(mark.latitude);
                location.setLongitude(mark.longitude);
            }
        }
        return location;
    }


    private boolean gpsIsOpen(LocationManager myLocationManager) {
        boolean isOpen = true;
        if (!myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {//没有开启GPS
            isOpen = false;
        }
        return isOpen;
    }

    private boolean netWorkIsOpen(LocationManager myLocationManager) {
        boolean netIsOpen = true;
        if (!myLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {//没有开启网络定位
            netIsOpen = false;
        }
        return netIsOpen;
    }
}
