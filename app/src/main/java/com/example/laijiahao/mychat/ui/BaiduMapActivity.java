package com.example.laijiahao.mychat.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.laijiahao.mychat.R;
import com.example.laijiahao.mychat.domain.Info;
import com.example.laijiahao.mychat.utils.MyOrientationListener;

import java.util.List;

public class BaiduMapActivity extends BaseActivity {

    private MapView mMapView;
    private BaiduMap mBaiduMap;

    private Context context;

    //定位相关
    private LocationClient mLocationClient;
    private MyLocationListener mLocationListener;
    private boolean isFirstIn = true;
    private double mLatitude;
    private double mLongtitude;
    //自定义定位图标
    private BitmapDescriptor mIconLocation;
    private MyOrientationListener myOrientationListener;
    private float mCurrentX;
    private MyLocationConfiguration.LocationMode mLocationMode;

    // 覆盖物相关
    private BitmapDescriptor mMarker;
    private RelativeLayout mMarkerLy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);//去除actionbar
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_site);

        this.context = this;

        initView();

        //初始化定位
        initLocation();
        initMarker();
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(Marker marker)
            {
                Bundle extraInfo = marker.getExtraInfo();
                Info info = (Info) extraInfo.getSerializable("info");
                ImageView iv = (ImageView) mMarkerLy
                        .findViewById(R.id.id_info_img);
                TextView distance = (TextView) mMarkerLy
                        .findViewById(R.id.id_info_distance);
                TextView name = (TextView) mMarkerLy
                        .findViewById(R.id.id_info_name);
                TextView zan = (TextView) mMarkerLy
                        .findViewById(R.id.id_info_zan);
                iv.setImageResource(info.getImgId());
                distance.setText(info.getDistance());
                name.setText(info.getName());
                zan.setText(info.getZan() + "");

                InfoWindow infoWindow;
                TextView tv = new TextView(context);
                tv.setBackgroundResource(R.drawable.location_tips);
                tv.setPadding(30, 20, 30, 50);
                tv.setText(info.getName());
                tv.setTextColor(Color.parseColor("#ffffff"));

                final LatLng latLng = marker.getPosition();
//                //将经纬度转换成点坐标
//                Point p = mBaiduMap.getProjection().toScreenLocation(latLng);
//                //给y设置一个偏移量
//                p.y -= 47;
//                //将点坐标转换成经纬度
//                LatLng ll = mBaiduMap.getProjection().fromScreenLocation(p);

                infoWindow = new InfoWindow(tv,latLng,-47);
                mBaiduMap.showInfoWindow(infoWindow);
                mMarkerLy.setVisibility(View.VISIBLE);
                return true;
            }
        });
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener()
        {

            @Override
            public boolean onMapPoiClick(MapPoi arg0)
            {
                return false;
            }

            @Override
            public void onMapClick(LatLng arg0)
            {
                mMarkerLy.setVisibility(View.GONE);
                mBaiduMap.hideInfoWindow();
            }
        });

    }

    private void initMarker()
    {
        //构建Marker图标
        mMarker = BitmapDescriptorFactory.fromResource(R.drawable.maker);
        mMarkerLy = (RelativeLayout) findViewById(R.id.id_maker_ly);
    }

    //初始化定位
    private void initLocation() {

        mLocationMode = MyLocationConfiguration.LocationMode.NORMAL;
        mLocationClient = new LocationClient(this);
        //初始化监听器并通过LocationClient对它进行注册
        mLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mLocationListener);

        //LocationClientOption进行定位的一些设置
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll"); //坐标类型,返回的定位结果是百度经纬度，默认gcj02，
        option.setIsNeedAddress(true); //帮我们返回当前的位置，设置是否需要地址信息，默认不需要
        option.setOpenGps(true); //打开gps
        option.setScanSpan(1000); //设置定位间隔时间,每隔多少秒进行一个请求，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        mLocationClient.setLocOption(option);

        // 初始化图标
        mIconLocation = BitmapDescriptorFactory
                .fromResource(R.drawable.navi_map_gps_locked);

        myOrientationListener = new MyOrientationListener(context);

        myOrientationListener
                .setOnOrientationListener(new MyOrientationListener.OnOrientationListener()
                {
                    @Override
                    public void onOrientationChanged(float x)
                    {
                        mCurrentX = x;
                    }
                });
    }

    private void initView() {
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.id_bmapView);
        mBaiduMap = mMapView.getMap();
        //打开Activity,比例会显示为15.0f，标尺为500米左右
        //构造一个更新地图的msu对象，然后设置该对象为缩放等级15.0，最后设置地图状态
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
        mBaiduMap.setMapStatus(msu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        // 地图首先开启定位的允许
        mBaiduMap.setMyLocationEnabled(true);
        if (!mLocationClient.isStarted())
            mLocationClient.start();
        // 开启方向传感器
        myOrientationListener.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        // 停止定位
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
        // 停止方向传感器
        myOrientationListener.stop();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.baidumenu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.id_map_common:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                break;
            case R.id.id_map_site:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.id_map_traffic:
                //首先判断当前是否显示traffic
                if(mBaiduMap.isTrafficEnabled()){
                    mBaiduMap.setTrafficEnabled(false);
                    item.setTitle("实时交通(off)");
                }else{
                    mBaiduMap.setTrafficEnabled(true);
                    item.setTitle("实时交通(on)");
                }
                break;
            case R.id.id_map_location:
                centerToMyLocation(mLatitude, mLongtitude);
                break;
            case R.id.id_map_mode_common:
                mLocationMode = MyLocationConfiguration.LocationMode.NORMAL;
                break;
            case R.id.id_map_mode_following:
                mLocationMode = MyLocationConfiguration.LocationMode.FOLLOWING;
                break;
            case R.id.id_map_mode_compass:
                mLocationMode = MyLocationConfiguration.LocationMode.COMPASS;
                break;
            case R.id.id_add_overlay:
                addOverlays(Info.infos);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 添加覆盖物
     *
     * @param infos
     */
    private void addOverlays(List<Info> infos)
    {
        //清除map的图层
        mBaiduMap.clear();
        LatLng latLng = null;
        Marker marker = null;
        OverlayOptions options;
        for (Info info : infos)
        {
            // 经纬度，定义Maker坐标点
            latLng = new LatLng(info.getLatitude(), info.getLongitude());
            // 图标   构建MarkerOption，用于在地图上添加Marker
            options = new MarkerOptions().position(latLng)
                    .icon(mMarker)
                    .zIndex(5);//设置marker所在层级
            //在地图上添加Marker，并显示
            marker = (Marker) mBaiduMap.addOverlay(options);
            Bundle arg0 = new Bundle();
            arg0.putSerializable("info", info);
            marker.setExtraInfo(arg0);
        }

        //把地图移动到最后一个marker的地址
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
        mBaiduMap.setMapStatus(msu);

    }

    /**
     * 定位到我的位置
     * @param mLatitude
     * @param mLongtitude
     */
    private void centerToMyLocation(double mLatitude, double mLongtitude) {
        LatLng latLng = new LatLng(mLatitude, mLongtitude);
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
        //更新mBaiduMap，地图的位置使用动画的效果传过去
        mBaiduMap.animateMapStatus(msu);
    }

    private class MyLocationListener implements BDLocationListener {

        //定位成功以后的回调
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            //将接收到的bdLocation转换成MyLocationData
            //当参数比较多，会在类里面建一个Builder内部类用来初始化参数，初始化完成以后就.build来建立对象
            MyLocationData data = new MyLocationData.Builder()
                    .direction(mCurrentX)//GPS定位时方向角度
                    .accuracy(bdLocation.getRadius()) //设置定位数据的精度信息，单位：米
                    .latitude(bdLocation.getLatitude()) //设置定位数据的纬度
                    .longitude(bdLocation.getLongitude()) //设置定位数据的经度
                    .build();
            mBaiduMap.setMyLocationData(data);

            // 设置自定义图标,得到定位的地址定位，更新图标
            /**
             * BitmapDescriptor	customMarker  用户自定义定位图标
             boolean	enableDirection  是否允许显示方向信息
             MyLocationConfiguration.LocationMode	locationMode  定位图层显示方式
             */
            MyLocationConfiguration config = new MyLocationConfiguration(
                    mLocationMode, true, mIconLocation);
            mBaiduMap.setMyLocationConfigeration(config);

            //更新经纬度,当每次定位成功以后会更新一下，保证每次都是最新的数据
            mLatitude = bdLocation.getLatitude();
            mLongtitude = bdLocation.getLongitude();

            //第一次就定位到中心点
            if (isFirstIn)
            {
                centerToMyLocation(mLatitude, mLongtitude);
                isFirstIn = false;

                Toast.makeText(context, bdLocation.getAddrStr(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

}
