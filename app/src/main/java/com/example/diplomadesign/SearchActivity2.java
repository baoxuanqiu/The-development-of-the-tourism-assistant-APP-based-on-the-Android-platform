package com.example.diplomadesign;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.example.diplomadesign.account.LocationInfo;

import org.litepal.LitePal;

import java.util.List;

public class SearchActivity2 extends AppCompatActivity implements OnGetGeoCoderResultListener {
    private String destination;//目的地
    // 搜索模块，也可去掉地图模块独立使用
    private GeoCoder mSearch = null;
    private BaiduMap mBaiduMap = null;
    private MapView mMapView = null;
    private EditText mEditCity;
    private BitmapDescriptor mbitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_marka);
    private Button go;
    private TextView destinationText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search2);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide(); //隐藏标题栏
        }
        if (Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        final Intent intent=getIntent();
        destination=intent.getStringExtra("destination");
        destinationText=(TextView)findViewById(R.id.destination);
        destinationText.setText(destination);

        go=(Button)findViewById(R.id.go_button);
        go.setVisibility(View.INVISIBLE);
        mEditCity = (EditText) findViewById(R.id.destination_city);
        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        navigateTo();
        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1=new Intent(SearchActivity2.this,WalkActivity.class);
                startActivity(intent1);
            }
        });
    }
    /**
     * 发起搜索
     */
    public void searchButtonProcess(View v) {
        // 发起Geo搜索
        mSearch.geocode(new GeoCodeOption()
                .city(mEditCity.getText().toString())// 城市
                .address(destination)); // 地址
    }

    /**
     * 地理编码查询结果回调函数
     *
     * @param result  地理编码查询结果
     */
    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(SearchActivity2.this, "抱歉，未能找到结果", Toast.LENGTH_LONG).show();
            return;
        }

        mBaiduMap.clear();
        mBaiduMap.addOverlay(new MarkerOptions().position(result.getLocation()).icon(mbitmap));
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result.getLocation()));
        List<LocationInfo> locationInfoList= LitePal.where("id=?","2")
                .find(LocationInfo.class);
        LocationInfo locationInfo=new LocationInfo();
        locationInfo.setId(2);
        locationInfo.setCity(mEditCity.getText().toString());
        locationInfo.setName(destination);
        locationInfo.setLatitude(Double.toString(result.getLocation().latitude));
        locationInfo.setLongitude(Double.toString(result.getLocation().longitude));
        if (locationInfoList.isEmpty()){
            locationInfo.save();
        }else {
            locationInfo.updateAll("id =?","2");
        }
        go.setVisibility(View.VISIBLE);
//        String strInfo = String.format("纬度：%f 经度：%f", result.getLocation().latitude, result.getLocation().longitude);
//        Toast.makeText(SearchActivity2.this, strInfo, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {

    }

    private void navigateTo(){
        double latitude=39.90923;
        double longitude=116.447428;
        List<LocationInfo> locationInfoList_1= LitePal.where("id=?","1")
                .find(LocationInfo.class);
        if (locationInfoList_1.size()!=0) {
            LocationInfo locationInfo = locationInfoList_1.get(0);
            latitude=Double.parseDouble(locationInfo.getLatitude());
            longitude=Double.parseDouble(locationInfo.getLongitude());
        }
        mBaiduMap.setMyLocationEnabled(true);
        LatLng ll=new LatLng(latitude,longitude);//获取纬度值，经度值
        MapStatusUpdate update=MapStatusUpdateFactory.newLatLng(ll);
        mBaiduMap.animateMapStatus(update);
        update=MapStatusUpdateFactory.zoomTo(19f);
        mBaiduMap.animateMapStatus(update);
        MyLocationData.Builder locationBuilder=new MyLocationData.Builder();
        locationBuilder.latitude(latitude);
        locationBuilder.longitude(longitude);
        MyLocationData locationData=locationBuilder.build();
        mBaiduMap.setMyLocationData(locationData);

    }
    @Override
    protected void onResume() {
        super.onResume();
        // 在activity执行onResume时必须调用mMapView. onResume ()
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 在activity执行onPause时必须调用mMapView. onPause ()
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mbitmap.recycle();
        // 释放检索对象
        mSearch.destroy();
        // 清除所有图层
        mBaiduMap.clear();
        // 在activity执行onDestroy时必须调用mMapView. onDestroy ()
        mMapView.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK ){
            Intent intent_2=new Intent(SearchActivity2.this, SearchActivity.class);
            startActivity(intent_2);
            return true;
        }
        return false;
    }
}