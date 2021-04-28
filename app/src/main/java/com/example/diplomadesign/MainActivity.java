package com.example.diplomadesign;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.CursorWindow;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telecom.Call;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.example.diplomadesign.account.Account;
import com.example.diplomadesign.account.LocationInfo;

import org.litepal.LitePal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public LocationClient mLocationClient;
    private TextView positionText;
    private TextView home_text;
    private TextView my_text;
    private TextView weather_text;
    private MapView mapView;
    private BaiduMap baiduMap;
    private boolean isFirstLocate=true;
    private RelativeLayout search_relative;
    private String cityName="";
    private boolean isLocation=true;
    private GeoCoder mGeoCoder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide(); //隐藏标题栏
        }
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        LitePal.getDatabase();//创建数据库

        my_text=(TextView)findViewById(R.id.my_activity_text);
        home_text=(TextView)findViewById(R.id.home_activity_text);
        weather_text=(TextView)findViewById(R.id.weather_activity_text);
        home_text .getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG );
        home_text.setSelected(true);
        my_text.setSelected(false);
        weather_text.setSelected(false);
        my_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_1=new Intent(MainActivity.this,MyActivity.class);
                startActivity(intent_1);
            }
        });
        weather_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_2=new Intent(MainActivity.this,WeatherActivity.class);
                startActivity(intent_2);
            }
        });

        mapView=(MapView)findViewById(R.id.bmapView);
        positionText = (TextView) findViewById(R.id.position_text_view);
        baiduMap=mapView.getMap();//获取BaiduMap实例
        baiduMap.setMyLocationEnabled(true);
        mGeoCoder = GeoCoder.newInstance();

        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
            requestLocation();
        }

        search_relative=(RelativeLayout)findViewById(R.id.search_relativeLayout);
        search_relative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_3=new Intent(MainActivity.this,SearchActivity.class);
//                intent_3.putExtra("cityName",cityName);
                startActivity(intent_3);
            }
        });

        try {
            Field field = CursorWindow.class.getDeclaredField("sCursorWindowSize");
            field.setAccessible(true);
            field.set(null, 100 * 1024 * 1024); //the 100MB is the new size
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    private void initLocation(){
        LocationClientOption option=new LocationClientOption();
        option.setScanSpan(5000);//每隔5秒钟会更新一下当前位置
        option.setIsNeedAddress(true);//获取当前位置的详细地址信息
        mLocationClient.setLocOption(option);
    }

    private void navigateTo(BDLocation location){//location：用户的当前位置信息
        if (isFirstLocate){   //判断是否第一次获取所在地地址
            LatLng ll=new LatLng(location.getLatitude(),location.getLongitude());//获取纬度值，经度值
            MapStatusUpdate update=MapStatusUpdateFactory.newLatLng(ll);//设置地图新中心点,latLng:地图新中心点
            baiduMap.animateMapStatus(update);//更新地图状态
            update=MapStatusUpdateFactory.zoomTo(19f);//zoomTo:设置缩放级别是19
            baiduMap.animateMapStatus(update);//更新地图状态
            isFirstLocate=false;//不是第一次获取地址信息
        }

        //地图上显示所在位置有一个点标识
        MyLocationData.Builder locationBuilder=new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData=locationBuilder.build();
        baiduMap.setMyLocationData(locationData);//设置定位数据
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        isLocation=true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
//            StringBuilder currentPosition = new StringBuilder();
//            currentPosition.append("纬度：").append(location.getLatitude()).append("\n");
//            currentPosition.append("经线：").append(location.getLongitude()).append("\n");
//            currentPosition.append("国家：").append(location.getCountry()).append("\n");
//            currentPosition.append("省：").append(location.getProvince()).append("\n");
//            currentPosition.append("市：").append(location.getCity()).append("\n");
//            currentPosition.append("区：").append(location.getDistrict()).append("\n");
//            currentPosition.append("街道：").append(location.getStreet()).append("\n");
//            currentPosition.append("定位方式：");
//            if (location.getLocType() == BDLocation.TypeGpsLocation) {
//                currentPosition.append("GPS");
//            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
//                currentPosition.append("网络");
//            }
            cityName=location.getCity();//将本地城市名赋值给cityName
            if (location.getLocType() == BDLocation.TypeGpsLocation
                    || location.getLocType() == BDLocation.TypeNetWorkLocation) {
                navigateTo(location);//调用navigateTo方法，将地图移动到当前位置上来
                if (isLocation){//判断是否要更新我的位置，true表示要
                    List<LocationInfo> locationInfos=LitePal.findAll(LocationInfo.class);
                    if (locationInfos.isEmpty()){//我的位置未存入数据库
                        LocationInfo locationInfo=new LocationInfo();
                        locationInfo.setId(1);
                        locationInfo.setCity(location.getCity());
                        locationInfo.setDiname(location.getDistrict());
                        locationInfo.setName("MyLocation");
                        locationInfo.setLatitude(Double.toString(location.getLatitude()));
                        locationInfo.setLongitude(Double.toString(location.getLongitude()));
                        locationInfo.save();
                    } else {//我的位置已经存入数据库
                        List<LocationInfo> locationInfoList=LitePal.where("name=?","MyLocation")
                                .find(LocationInfo.class);
                        if (locationInfoList.isEmpty()){
                            LocationInfo locationInfo=new LocationInfo();
                            locationInfo.setId(1);
                            locationInfo.setCity(location.getCity());
                            locationInfo.setDiname(location.getDistrict());
                            locationInfo.setName("MyLocation");
                            locationInfo.setLatitude(Double.toString(location.getLatitude()));
                            locationInfo.setLongitude(Double.toString(location.getLongitude()));
                            locationInfo.save();
                        }else {
                            LocationInfo locationInfo=new LocationInfo();
                            locationInfo.setCity(location.getCity());
                            locationInfo.setDiname(location.getDistrict());
                            locationInfo.setLatitude(Double.toString(location.getLatitude()));
                            locationInfo.setLongitude(Double.toString(location.getLongitude()));
                            locationInfo.updateAll("name=? and id =?","MyLocation","1");
                        }
                    }
                    LatLng center=new LatLng(location.getLatitude(),location.getLongitude());
                    UpdateCityId(center);//更新城市ID
                    isLocation=false;
                }
            }
        }

    }

    private void UpdateCityId(final LatLng center){

//        final LatLng center=new LatLng(location.getLatitude(),location.getLongitude());//获取纬度值，经度值
        ReverseGeoCodeOption rgcOption =
                new ReverseGeoCodeOption().location(center).radius(500);
        mGeoCoder.reverseGeoCode(rgcOption);
        mGeoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener(){
            /**
             * 地理编码查询结果回调函数
             *
             * @param result 地理编码查询结果
             */
            @Override
            public void onGetGeoCodeResult(GeoCodeResult result) {
            }
            /**
             * 反地理编码查询结果回调函数
             *
             * @param result 反地理编码查询结果
             */
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                int adCode = result.getAdcode();
                String districtId=String.valueOf(adCode);
                LocationInfo locationInfo=new LocationInfo();
                locationInfo.setCityID(districtId);
                locationInfo.updateAll("name=? and id =?","MyLocation","1");
//                Toast.makeText(MainActivity.this,districtId,Toast.LENGTH_SHORT).show();
            }
        });
        mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(center));
    }
    //退出使退回手机主界面
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
