package com.xpand.tinkerdome;

import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.view.KeyEvent;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.tencent.bugly.crashreport.CrashReport;
import com.xpand.dispatcher.jsonbean.City;
import com.xpand.dispatcher.jsonbean.NearbyStations;
import com.xpand.dispatcher.utils.LogTool;
import com.xpand.dispatcher.utils.PrefUtils;
import com.xpand.dispatcher.view.ExitDialog;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by Laer on 2016/10/31.
 */
public class App extends Application {
    private static App mInstance;
    public static PrefUtils pre;
    private LocationClient mLocationClient;
    public static BDLocation currentLocation;
    private MyLocationListener mMyLocationListener;
    public LatLng MLatLng;
    public String errorMsg = "";
    //屏幕的宽高
    public static int screenH;
    public static int screenW;
    public static boolean isReceiverMSG;//是否接受普通消息
    public static  boolean isReceverWork;//是否接受工作消息
    public List<Activity> activityList = new ArrayList<>();
    public static int version;//当前版本号
    public String getErrorMsg() {
        return errorMsg;
    }
    public NearbyStations nearbyStations;//定位界面的附近站点
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
    public List<City> citys=new ArrayList<>();//当前值班表中tab栏城市的的数据
    public List<City> getCitys() {
        return citys;
    }

    public void setCitys(List<City> citys) {
        this.citys = citys;
    }

    public NearbyStations getNearbyStations() {
        return nearbyStations;
    }

    public void setNearbyStations(NearbyStations nearbyStations) {
        this.nearbyStations = nearbyStations;
    }

    /* private Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    Activity activity = activityList.get(activityList.size() - 1);
                    if (!activity.getLocalClassName().contains("LoginActivity")) {
                        new ExitDialog(activity, R.layout.again_dialog).show();
                    }
                }
            };
        */
    public void showLogin() {
        if (!errorMsg.contains("登录"))
            return;
        Activity activity = activityList.get(activityList.size() - 1);
        if (!activity.getLocalClassName().contains("LoginActivity")) {
            ExitDialog dialog = new ExitDialog(activity, R.layout.again_dialog);
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                    return false;
                }
            });
            dialog.show();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        version= Integer.parseInt(android.os.Build.VERSION.SDK );
        CrashReport.initCrashReport(getApplicationContext(), "900060232", false);
        //初始化极光推送
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        mInstance = this;
        pre = PrefUtils.getInstance();
    }

    public void addActivity(Activity activity) {
        activityList.add(activity);
        LogTool.i("当前activity开启数量：" + activityList.size());
        LogTool.i("当前开启的activity是：" + activity.getLocalClassName());
    }

    public void removeActivity(Activity activity) {
        activityList.remove(activity);
        LogTool.i("当前activity开启数量：" + activityList.size());
        LogTool.i("当前关闭的activity是：" + activity.getLocalClassName());
    }

    public void exit() {
        for (Activity activity : activityList) {
            activity.finish();
        }
    }

    /**
     * @return
     * @Description 获取单例的方法
     */
    public static App getInstance() {
        if (mInstance == null) {
            mInstance = new App();
        }

        return mInstance;
    }


    public static BDLocation getCurrentLocation() {
        return currentLocation;
    }

    public LocationClient getmLocationClient() {
        return mLocationClient;
    }

    public void initLocationService() {

        mLocationClient = new LocationClient(this.getApplicationContext());
        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);
        initLocation();
        //   mLocationClient.start();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();

        // option.setLocationMode(
        // LocationMode.Device_Sensors);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.disableCache(true);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");// 可选，默认gcj02，设置返回的定位结果坐标系，
        option.setScanSpan(1000);// 可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);// 可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);// 可选，默认false,设置是否使用gps
        option.setLocationNotify(true);// 可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIgnoreKillProcess(true);// 可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setEnableSimulateGps(false);// 可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        option.setIsNeedLocationDescribe(true);// 可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(false);// 可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setNeedDeviceDirect(true);  //是否需要方向
        option.setIsNeedAltitude(true);//获取高度信息
        mLocationClient.setLocOption(option);

    }



    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            LogTool.e("========================");
           /* if (!validateBdLocation(bdLocation)) {
                return;
            }*/
            int errorCode = bdLocation.getLocType();
            if (errorCode != 61 && errorCode != 65 && errorCode != 161) {//61 GPS定位结果，GPS定位成功。//65 定位缓存的结果。161 网络定位结果，网络定位定位成功。
                return;
            }
            if (currentLocation == null) {
                currentLocation = bdLocation;
                return;
            }
            stopLocation();
            currentLocation = bdLocation;
            MLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            if (onLocationChangedListener != null) {
                onLocationChangedListener.onLocationChanged(bdLocation);
            }
            //清空定位监听
            setOnLocationChangedListener(null);
        }
    }
    private boolean isStart;//是否开始定位。
    public void startLocation() {
        if (!isStart){
            isStart=true;
            initLocationService();
            mLocationClient.start();
        }
    }

    public void stopLocation() {
        isStart=false;
        LogTool.e("===========完成=============");
        mLocationClient.stop();
    }

    public interface OnLocationChangedListener {
        void onLocationChanged(BDLocation location);
    }

    private OnLocationChangedListener onLocationChangedListener;

    public void setOnLocationChangedListener(OnLocationChangedListener onLocationChangedListener) {
        this.onLocationChangedListener = onLocationChangedListener;
    }

}
