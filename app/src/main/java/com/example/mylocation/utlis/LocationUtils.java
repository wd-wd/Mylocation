package com.example.mylocation.utlis;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.operators.flowable.FlowableJust;
import io.reactivex.schedulers.Schedulers;

public class LocationUtils {
    private LocationManager locationManager;
    static LocationUtils locationUtils;
    public static LocationUtils getInstance(){
        if (locationUtils == null){
            locationUtils = new LocationUtils();
        }
        return locationUtils;
    }


    public String getLocations(Context context){
        String strLocation = "0,0";
        DecimalFormat df = new DecimalFormat("#####0.0000");
        if (!checkPermission(context, permission.ACCESS_COARSE_LOCATION)){
            Toast.makeText(context,"定位权限关闭，无法获取地理位置", Toast.LENGTH_SHORT).show();
        }
        try {
            //获取系统的服务，
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            //创建一个criteria对象
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            //设置不需要获取海拔方向数据
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            //设置允许产生资费
            criteria.setCostAllowed(true);
            //要求低耗电
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            String provider = locationManager.getBestProvider(criteria, true);
            Log.i("Tobin", "Location Provider is "+ provider);
            Location location = locationManager.getLastKnownLocation(provider);

            /**
             * 重要函数，监听数据测试
             * 位置提供器、监听位置变化的时间间隔（毫秒），监听位置变化的距离间隔（米），LocationListener监听器
             */
           locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    locationManager.removeUpdates(locationListener);
                }
            },2000);

            //第一次获得设备的位置
            if (location != null){
//                strLocation = df.format(location.getLatitude()) + "," + df.format(location.getLongitude());

                // 耗时操作
                strLocation = df.format(location.getLatitude()) + "," + df.format(location.getLongitude())+" " + convertAddress(context, location.getLatitude(),location.getLongitude());

            }

        }catch (SecurityException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        return strLocation;

    }

    /**
     * LocationListern监听器
     * 参数：地理位置提供器、监听位置变化的时间间隔、位置变化的距离间隔、LocationListener监听器
     */
    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

        }
        public void onProviderDisabled(String provider){
            Log.i("Tobin", "Provider now is disabled..");
        }
        public void onProviderEnabled(String provider){
            Log.i("Tobin", "Provider now is enabled..");
        }
        public void onStatusChanged(String provider, int status, Bundle extras){ }
    };


    /**
     * @param latitude 经度
     * @param longitude 纬度
     * @return 详细位置信息 GeoCoder是基于后台backend的服务，因此这个方法不是对每台设备都适用。
     */
    public String convertAddress(Context context, double latitude, double longitude) {
        Geocoder mGeocoder = new Geocoder(context, Locale.getDefault());
        StringBuilder mStringBuilder = new StringBuilder();

        try {
            final List<Address> mAddresses = mGeocoder.getFromLocation(latitude, longitude, 1);
            if (mAddresses.size() > 0) {
                Address address = mAddresses.get(0);
//                for (int i = 0;address.getAddressLine(i)!=null;i++){
//                    String addressLine = address.getAddressLine(i);
//                }
                mStringBuilder.append(address.getAddressLine(0));
//                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
//                    //每一组地址里面还会有许多地址。这里我取的前2个地址。xxx街道-xxx位置
//                    if (i == 0) {
//                        mStringBuilder.append(address.getAddressLine(i)).append("-");
//                    }
//                    if (i == 1) {
//                        mStringBuilder.append(address.getAddressLine(i));
//                        break;
//                    }
//                }
            }

//            if (!mAddresses.isEmpty()) {
//                Address address = mAddresses.get(0);
//                mStringBuilder.append(address.getCountryName()).append(", ").append(address.getAdminArea()).append(", ").append(address.getLocality());
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mStringBuilder.toString();
    }

    private boolean checkPermission(Context context, permission permName) {
        int perm = context.checkCallingOrSelfPermission("android.permission."+permName.toString());
        return perm == PackageManager.PERMISSION_GRANTED;
    }

    private enum permission{
        ACCESS_COARSE_LOCATION,
        ACCESS_FINE_LOCATION
    }
}
