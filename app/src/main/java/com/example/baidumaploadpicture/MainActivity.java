package com.example.baidumaploadpicture;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements MKOfflineMapListener, OnGetGeoCoderResultListener {
    private static final String LOG_CAT = "net_work";
    private MapView mapView = null;
    private MKOfflineMap mOffline = null;
    private MKOLUpdateElement update;
    private int cityid;
    private Context context;
    private Activity activity;

    private BaiduMap mBaiduMap;
    private GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用
    public LocationClient mLocationClient = null;

    public OnMapStatusChangeListener mOnMapStatusChangeListener = new OnMapStatusChangeListener();
    public OnMarkerClickListener mOnMarkerClickListener = new OnMarkerClickListener();
    public BDLocationListener myListener = new MyLocationListener();
    public OnMapClickListener mOnMapClickListener = new OnMapClickListener();
    private boolean isFirstLoc = true;
    private Boolean isLocation = false;
    private List<String> urlList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        activity = this;

        mapView = findViewById(R.id.mapView);

        urlList.add("https://h5.m.kuosanyun.com/static/images/portrait/rand_user_26.jpg");
        urlList.add("https://h5.m.kuosanyun.com/static/images/portrait/rand_user_2.jpg");
        urlList.add("https://h5.m.kuosanyun.com/static/images/portrait/rand_user_137.jpg");
        urlList.add("https://upload-10014880.file.myqcloud.com/upload/20170412/1944034054.png");
        urlList.add("https://h5.m.kuosanyun.com/static/images/portrait/rand_user_113.jpg");
        urlList.add("https://h5.m.kuosanyun.com/static/images/portrait/rand_user_68.jpg");
        urlList.add("https://h5.m.kuosanyun.com/static/images/portrait/rand_user_176.jpg");
        urlList.add("https://h5.m.kuosanyun.com/static/images/portrait/rand_user_55.jpg");
        urlList.add("https://cvideo.kuosanyun.com/20170906082703444382");
        urlList.add("https://h5.m.kuosanyun.com/static/images/portrait/rand_user_30.jpg");
        urlList.add("https://upload-10014880.file.myqcloud.com/static/images/portrait/rand_user_23.jpg");
        urlList.add("https://h5.m.kuosanyun.com/static/images/portrait/rand_user_84.jpg");
        Log.d(LOG_CAT, "urlList:" + urlList.size());

        mOffline = new MKOfflineMap();
        // 传入接口事件，离线地图更新会触发该回调
        mOffline.init(this);

        judgeNetWorkStatus();//判断网络状况

        mBaiduMap = mapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setMaxAndMinZoomLevel(19, 3);

        mapView.showZoomControls(false);//隐藏地图上比例尺
        mapView.showScaleControl(false);//隐藏缩放控件
        mBaiduMap.getUiSettings().setCompassEnabled(false); //隐藏指南针
        mBaiduMap.getUiSettings().setRotateGesturesEnabled(false);//禁止旋转
        mBaiduMap.getUiSettings().setOverlookingGesturesEnabled(false);//禁止3D俯视效果

        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);

        //地图点击事件
        mBaiduMap.setOnMapClickListener(mOnMapClickListener);
        //marker点击事件
        mBaiduMap.setOnMarkerClickListener(mOnMarkerClickListener);
        //监听地图发生变化的事件
        mBaiduMap.setOnMapStatusChangeListener(mOnMapStatusChangeListener);

        //声明LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());
        //注册监听函数
        mLocationClient.registerLocationListener(myListener);
        initLocation();
        mLocationClient.start();//开始定位
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // activity 销毁时同时销毁地图控件
        MapView.setMapCustomEnable(false);

        /**
         * 退出时，销毁离线地图模块
         */
        mLocationClient.stop();
        mOffline.destroy();
        mapView.onDestroy();
        mSearch.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // activity 暂停时同时暂停地图控件
        mapView.onPause();

        if (update != null) {
//            Log.d(LOG_CAT, "update_pause:" + update.ratio);
            if (update.ratio != 100) {
                mOffline.pause(cityid);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // activity 恢复时同时恢复地图控件
        mapView.onResume();

        if (update != null) {
//            Log.d(LOG_CAT, "update_resume:" + update.ratio);
            if (update.ratio != 100) {
                mOffline.start(cityid);
            }
        }
    }

    //离线地图更新会触发该回调方法
    @Override
    public void onGetOfflineMapState(int type, int state) {
        switch (type) {
            case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
                update = mOffline.getUpdateInfo(state);

                // 处理下载进度更新提示
                if (update != null) {
//                    Log.d(LOG_CAT, getString(R.string.all_download) + String.valueOf(update.ratio) + "%");
                }
            }
            break;
            case MKOfflineMap.TYPE_NEW_OFFLINE:
                // 有新离线地图安装
                Log.d("OfflineDemo", String.format("add offlinemap num:%d", state));
                break;
            case MKOfflineMap.TYPE_VER_UPDATE:
                // 版本更新提示
                break;
            default:

                break;
        }
    }

    private void judgeNetWorkStatus() {
        int netWorkState = NetUtil.getNetWrokState(context);
        switch (netWorkState) {
            case NetUtil.NETWORK_NONE:
                Log.d(LOG_CAT, getString(R.string.net_broken));
                break;
            case NetUtil.NETWORK_MOBILE:
                Log.d(LOG_CAT, getString(R.string.net_work_mobile));
                break;
            case NetUtil.NETWORK_WIFI:
                Log.d(LOG_CAT, getString(R.string.net_work_wifi));
                downloadOfflineMap();
                break;
            default:
                break;
        }
    }

    /**
     * ArrayList<MKOLUpdateElement> allUpdateInfo = mOffline.getAllUpdateInfo();
     * MKOLUpdateElement mkolUpdateElement = allUpdateInfo.get(0);
     * <p>
     * 离线地图更新信息，下面是其中的一些字段
     * <p>
     * int     cityID = mkolUpdateElement.cityID;           城市ID
     * String  cityName = mkolUpdateElement.cityName;       城市名称
     * LatLng  geoPt = mkolUpdateElement.geoPt;             城市中心点坐标
     * int     level = mkolUpdateElement.level;             离线包地图层级
     * int     ratio = mkolUpdateElement.ratio;             下载比率，100为下载完成
     * int     serversize = mkolUpdateElement.serversize;   服务端数据大小
     * int     size = mkolUpdateElement.size;               已下载数据大小
     * int     status = mkolUpdateElement.status;           下载状态
     * boolean update = mkolUpdateElement.update;           是否为更新
     */
    //下载离线地图
    private void downloadOfflineMap() {
        ArrayList<MKOLUpdateElement> allUpdateInfo = mOffline.getAllUpdateInfo();

        if (allUpdateInfo != null && allUpdateInfo.size() > 0) {
            MKOLUpdateElement mkolUpdateElement = allUpdateInfo.get(0);
            int ratio = mkolUpdateElement.ratio;

            if (ratio != 100) {
                downLoadMap();
            } else {
//                Log.d(LOG_CAT, "已经下载过了 ");
            }
        } else {
            downLoadMap();
//            Log.d(LOG_CAT, getString(R.string.start_download) + ":" + cityid);
        }

    }

    private void downLoadMap() {
        ArrayList<MKOLSearchRecord> records = mOffline.searchCity(getString(R.string.global_package));
        if (records == null || records.size() != 1) {
            return;
        }

        String valueOf = String.valueOf(records.get(0).cityID);

        cityid = Integer.parseInt(valueOf.toString());
        mOffline.start(cityid);
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
//        Log.e(LOG_CAT,result.error + "");
//        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
//            Toast.makeText(context, "抱歉，未能找到结果", Toast.LENGTH_LONG)
//                    .show();
//            return;
//        }
//
//        String city = result.getAddressDetail().city;

//        Log.d(LOG_CAT, "city:" + city);
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

        option.setCoorType("bd09ll");
        //可选，设置返回经纬度坐标类型，默认gcj02
        //gcj02：国测局坐标；
        //bd09ll：百度经纬度坐标；
        //bd09：百度墨卡托坐标；
        //海外地区定位，无需设置坐标类型，统一返回wgs84类型坐标

        int span = 1000;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要

        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps

        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集

        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

        mLocationClient.setLocOption(option);
    }

    //定位监听回调事件
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //获取定位结果
            StringBuffer sb = new StringBuffer(256);

            sb.append("time : ");
            sb.append(location.getTime());    //获取定位时间

            sb.append("\nerror code : ");
            sb.append(location.getLocType());    //获取类型类型

            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());    //获取纬度信息

            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());    //获取经度信息

            sb.append("\nradius : ");
            sb.append(location.getRadius());    //获取定位精准度

            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                // GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());    // 单位：公里每小时

                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());    //获取卫星数

                sb.append("\nheight : ");
                sb.append(location.getAltitude());    //获取海拔高度信息，单位米

                sb.append("\ndirection : ");
                sb.append(location.getDirection());    //获取方向信息，单位度

                sb.append("\naddr : ");
                sb.append(location.getAddrStr());    //获取地址信息

                sb.append("\ndescribe : ");
                sb.append("gps定位成功");
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                // 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());    //获取地址信息

                sb.append("\noperationers : ");
                sb.append(location.getOperators());    //获取运营商信息

                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {
                // 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }

            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());    //位置语义化信息

            // map view 销毁后不在处理新接收的位置
            if (location == null || mapView == null) {
                return;
            }

            if (isFirstLoc) {
                isFirstLoc = false;

                double latitude = location.getLatitude();//纬度
                double longitude = location.getLongitude();//经度
                String actual_point = latitude + "," + longitude;
                String vir_point = latitude + "," + longitude;

                Log.i("fhntnn", latitude + "," + longitude);
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

                //判断如果定位点为百度地图的默认值,就去弹窗要求用户去开启GPS
                String lag_lng = latitude + "," + longitude;
                if (lag_lng.equals("4.9E-324,4.9E-324")) {
                    NetUtil.initGPS(context, activity);
                }

                if (!isLocation) {
                    isLocation = true;

                    // 反Geo搜索
                    mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(ll));
                    loadMapPoint(vir_point);
                }
            }
        }

    }

    private void loadMapPoint(String vir_point) {
        for (int i = 0; i < urlList.size(); i++) {
            String lagLng = NetUtil.getLagLng(vir_point);
            addMarker(lagLng, urlList.get(i));
        }
    }

    public class OnMapClickListener implements BaiduMap.OnMapClickListener {

        @Override
        public void onMapClick(LatLng latLng) {
            mBaiduMap.hideInfoWindow();
        }

        @Override
        public boolean onMapPoiClick(MapPoi mapPoi) {
            return false;
        }
    }

    public class OnMarkerClickListener implements BaiduMap.OnMarkerClickListener {
        @Override
        public boolean onMarkerClick(Marker marker) {
            //进入高级群
            Bundle bundle = marker.getExtraInfo();

            if (bundle != null) {


            }

            return true;
        }
    }

    public class OnMapStatusChangeListener implements BaiduMap.OnMapStatusChangeListener {
        @Override
        public void onMapStatusChangeStart(MapStatus mapStatus) {
        }

        @Override
        public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

        }

        @Override
        public void onMapStatusChange(MapStatus mapStatus) {
        }

        @Override
        public void onMapStatusChangeFinish(MapStatus mapStatus) {
            float zoom = mapStatus.zoom;
            int round = Math.round(zoom);
            Log.i(LOG_CAT, "zoom:" + zoom + "," + round + "," + mapStatus.target);

            LatLng latLng_whole = mapStatus.target;
            // 反Geo搜索
            mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(latLng_whole));

        }
    }

    //动态添加marker
    public void addMarker(String actual_point, final String link) {

        if (!TextUtils.isEmpty(actual_point)) {
            String[] str = actual_point.split(",");

            Double latitude = Double.parseDouble(str[0]);
            Double longitude = Double.parseDouble(str[1]);

            final LatLng latLng = new LatLng(latitude, longitude);
            final Bundle bundle = new Bundle();
            bundle.putString("link", link);

            new Thread() {
                @Override
                public void run() {
                    super.run();

                    drawMapSpot(latLng, bundle, link);
                }
            }.start();
        }
    }

    private void drawMapSpot(final LatLng latLng, final Bundle bundle, final String link) {
        View view = null;
        ImageView img_head = null;

        Bitmap loadBitmap = null;

        view = View.inflate(context, R.layout.layout_item, null);
        img_head = view.findViewById(R.id.iv_p_v);

        if (TextUtils.isEmpty(link)) {
            loadBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.nim_avatar_default);
        } else {
            loadBitmap = showImage.loadBitmap(link, true, false, R.mipmap.nim_avatar_default);
        }

        img_head.setImageBitmap(loadBitmap);

        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromView(view);
        MarkerOptions options = new MarkerOptions().position(latLng).icon(descriptor).extraInfo(bundle);
        Marker marker = (Marker) mBaiduMap.addOverlay(options);
//        markerListCopy.add(marker);

        if (loadBitmap != null && !loadBitmap.isRecycled()) {
            loadBitmap.isRecycled();
            loadBitmap = null;
        }

    }
}
