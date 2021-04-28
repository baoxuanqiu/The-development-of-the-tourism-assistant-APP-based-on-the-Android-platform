package com.example.diplomadesign;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteLine;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.example.diplomadesign.account.LocationInfo;
import com.example.diplomadesign.baidu.overlayutil.BikingRouteOverlay;
import com.example.diplomadesign.baidu.overlayutil.OverlayManager;
import com.example.diplomadesign.show_route.Route;
import com.example.diplomadesign.show_route.RouteAdapter;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class BikingActivity extends AppCompatActivity implements BaiduMap.OnMapClickListener,
        OnGetRoutePlanResultListener {
    // 浏览路线节点相关
    private Button mBtnPre = null; // 上一个节点
    private Button mBtnNext = null; // 下一个节点
    private RouteLine mRouteLine = null;
    private OverlayManager routeOverlay = null;
    boolean mUseDefaultIcon = false;

    // 地图相关，使用继承MapView的MyRouteMapView目的是重写touch事件实现泡泡处理
    // 如果不处理touch事件，则无需继承，直接使用MapView即可
    private MapView mMapView = null;    // 地图View
    private BaiduMap mBaidumap = null;
    // 搜索相关
    private RoutePlanSearch mSearch = null;    // 搜索模块，也可去掉地图模块独立使用
    private BikingRouteResult mBikingRouteResult = null;
    private boolean hasShowDialog = false;
    private NodeUtils mNodeUtils;
    private CheckBox mRidingTypeCB;
    private TextView walk;
    private TextView ride;
    private TextView driving;
    private TextView transit;
    private TextView mass_transit;
    private List<Route> routeList=new ArrayList<>();//显示路程信息
    private TextView routeTime;//路程时间
    private TextView routePrice;//路程费用
    private TextView routeText_1;
    private TextView routeText_2;
    private final int WALK=1;
    private final int RIDE=2;
    private final int DRIVING=3;
    private final int TRANSIT=4;
    private final int MASS_TRANSIT=5;
    private TextView endNodeText;
    private PlanNode startNode;
    private PlanNode endNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biking);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide(); //隐藏标题栏
        }
        if (Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        chooseThis(RIDE);
        chooseOtherTraffic();
        routeTime=(TextView)findViewById(R.id.route_time);
        routePrice=(TextView)findViewById(R.id.route_price);
        routeText_1=(TextView)findViewById(R.id.route_text_1);
        routeText_2=(TextView)findViewById(R.id.route_text_2);
        mRidingTypeCB = (CheckBox) findViewById(R.id.ridingType);
        // 初始化地图
        mMapView = (MapView) findViewById(R.id.map);
        mBaidumap = mMapView.getMap();

        mBtnPre = (Button) findViewById(R.id.pre);
        mBtnNext = (Button) findViewById(R.id.next);
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);
        endNodeText=(TextView)findViewById(R.id.ed_node);
        // 地图点击事件处理
        mBaidumap.setOnMapClickListener(this);
        getInfo();
        // 初始化搜索模块，注册事件监听
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);
        mNodeUtils = new NodeUtils(this, mBaidumap);
    }

    private void getInfo(){
        List<LocationInfo> locationInfoList_1= LitePal.where("id=?","1")
                .find(LocationInfo.class);
        if (locationInfoList_1.size()!=0){
            LocationInfo locationInfo=locationInfoList_1.get(0);
            double latitude=Double.parseDouble(locationInfo.getLatitude());
            double longitude=Double.parseDouble(locationInfo.getLongitude());
            LatLng start = new LatLng(latitude, longitude);
            startNode=PlanNode.withLocation(start);
            navigateTo(latitude,longitude);
        }
        List<LocationInfo> locationInfoList_2= LitePal.where("id=?","2")
                .find(LocationInfo.class);
        if (locationInfoList_2.size()!=0){
            LocationInfo locationInfo=locationInfoList_2.get(0);
            double latitude=Double.parseDouble(locationInfo.getLatitude());
            double longitude=Double.parseDouble(locationInfo.getLongitude());
            LatLng end = new LatLng(latitude, longitude);
            endNode=PlanNode.withLocation(end);
            endNodeText.setText(locationInfo.getName());
        }
    }
    /**
     * 发起路线规划搜索示例
     */
    public void searchButtonProcess(View v) {
        // 重置浏览节点的路线数据
        mRouteLine = null;
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);
        mBaidumap.clear();

        // 设置起终点信息 起点参数
//        PlanNode startNode = PlanNode.withCityNameAndPlaceName(mEditStartCity.getText().toString().trim(),
//                mStrartNodeView.getText().toString().trim());
//        // 终点参数
//        PlanNode endNode = PlanNode.withCityNameAndPlaceName(mEditEndCity.getText().toString().trim(),
//                mEndNodeView.getText().toString().trim());
//        final LatLng start = new LatLng(39.90923, 116.447428);
//        final LatLng start = new LatLng(23.147387, 113.272422);
//        PlanNode startNode=PlanNode.withLocation(start);
//        final LatLng end = new LatLng(23.126853, 113.33573);
//        PlanNode endNode=PlanNode.withLocation(end);
        // 步行路线规划参数
        BikingRoutePlanOption bikingRoutePlanOption = new BikingRoutePlanOption().from(startNode).to(endNode);
        // 骑行类型（0：普通骑行模式，1：电动车模式）默认是普通模式
        if (mRidingTypeCB.isChecked()){
            bikingRoutePlanOption.ridingType(0);
        } else {
            bikingRoutePlanOption.ridingType(1);
        }
        // 发起骑行路线规划检索
        mSearch.bikingSearch(bikingRoutePlanOption);
    }

    /**
     * 节点浏览
     */
    public void nodeClick(View v) {
        if (null != mRouteLine) {
            mNodeUtils.browseRoutNode(v,mRouteLine);
        }
    }

    /**
     * 切换路线图标，刷新地图使其生效
     * 注意： 起终点图标使用中心对齐.
     */
    public void changeRouteIcon(View v) {
        if (routeOverlay == null) {
            return;
        }
        if (mUseDefaultIcon) {
            ((Button) v).setText("自定义起终点图标");
            Toast.makeText(this, "将使用系统起终点图标", Toast.LENGTH_SHORT).show();

        } else {
            ((Button) v).setText("系统起终点图标");
            Toast.makeText(this, "将使用自定义起终点图标", Toast.LENGTH_SHORT).show();
        }
        mUseDefaultIcon = !mUseDefaultIcon;
        routeOverlay.removeFromMap();
        routeOverlay.addToMap();
    }


    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {

    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult result) {
    }

    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult result) {
    }


    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {

    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    /**
     * 骑行路线结果回调
     *
     * @param result  骑行路线结果
     *
     */
    @Override
    public void onGetBikingRouteResult(BikingRouteResult result) {
        if(null == result){
            return;
        }
        if (result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(BikingActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            AlertDialog.Builder builder = new AlertDialog.Builder(BikingActivity.this);
            builder.setTitle("提示");
            builder.setMessage("检索地址有歧义，请重新设置。\n可通过getSuggestAddrInfo()接口获得建议查询信息");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            mBtnPre.setVisibility(View.VISIBLE);//先前一步的按钮显示出来
            mBtnNext.setVisibility(View.VISIBLE);//向后一步的按钮显示出来

            if (result.getRouteLines().size() > 1) {//路线规划中不止一步
                mBikingRouteResult = result;
                if (!hasShowDialog) {
                    SelectRouteDialog selectRouteDialog = new SelectRouteDialog(BikingActivity.this,
                            result.getRouteLines(),
                            RouteLineAdapter.Type.DRIVING_ROUTE);
                    selectRouteDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            hasShowDialog = false;
                        }
                    });
                    selectRouteDialog.setOnItemInDlgClickLinster(new SelectRouteDialog.OnItemInDlgClickListener() {
                        public void onItemClick(int position) {
                            mRouteLine = mBikingRouteResult.getRouteLines().get(position);
                            BikingRouteOverlay overlay = new MyBikingRouteOverlay(mBaidumap);
                            mBaidumap.setOnMarkerClickListener(overlay);
                            routeOverlay = overlay;
                            overlay.setData(mBikingRouteResult.getRouteLines().get(position));
                            overlay.addToMap();
                            overlay.zoomToSpan();
                            showRouteInfo();
                        }

                    });
                    selectRouteDialog.show();
                    hasShowDialog = true;
                }
            } else if (result.getRouteLines().size() == 1) {//路线规划只有一步
                mRouteLine = result.getRouteLines().get(0);
                BikingRouteOverlay overlay = new MyBikingRouteOverlay(mBaidumap);//创建BikingRouteOverlay实例
                routeOverlay = overlay;
                mBaidumap.setOnMarkerClickListener(overlay);
                overlay.setData(result.getRouteLines().get(0));//为BikingRouteOverlay实例设置数据
                overlay.addToMap();//在地图上绘制BikingRouteOverlay
                overlay.zoomToSpan();
                showRouteInfo();
                mBtnPre.setVisibility(View.VISIBLE);
                mBtnNext.setVisibility(View.VISIBLE);
            } else {
                Log.d("route result", "结果数<0");
                return;
            }

        }
    }

    private void showRouteInfo(){
        List<BikingRouteLine.BikingStep> steps=mRouteLine.getAllStep();
        routeList.clear();
        for (int i=0;i<steps.size();i++){
            String a=steps.get(i).getInstructions();
            if (a.length()!=0){
                Route route=new Route(a);
                routeList.add(route);
            }
        }
        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.recycler_show_route);
        LinearLayoutManager layoutManager=new LinearLayoutManager(BikingActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        RouteAdapter adapter=new RouteAdapter(routeList);
        recyclerView.setAdapter(adapter);
        String distance=String.valueOf(mRouteLine.getDistance())+" 米";
        routeText_1.setText("预计路程距离:");
        routeTime.setText(distance);
    }

    private class MyBikingRouteOverlay extends BikingRouteOverlay {
        private MyBikingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (mUseDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (mUseDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }


    @Override
    public void onMapClick(LatLng point) {
        mBaidumap.hideInfoWindow();
    }

    @Override
    public void onMapPoiClick(MapPoi poi) {

    }

    private void chooseThis(int choose){
        walk=(TextView)findViewById(R.id.walk_activity_text);
        ride=(TextView)findViewById(R.id.ride_activity_text);
        transit=(TextView)findViewById(R.id.transit_activity_text);
        mass_transit=(TextView)findViewById(R.id.mass_transit_activity_text);
        driving=(TextView)findViewById(R.id.driving_activity_text);
        switch (choose){
            case WALK:
                walk.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG );
                walk.setSelected(true);
                ride.setSelected(false);
                driving.setSelected(false);
                transit.setSelected(false);
                mass_transit.setSelected(false);
                break;
            case RIDE:
                ride.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG );
                walk.setSelected(false);
                ride.setSelected(true);
                driving.setSelected(false);
                transit.setSelected(false);
                mass_transit.setSelected(false);
                break;
            case DRIVING:
                driving.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG );
                walk.setSelected(false);
                ride.setSelected(false);
                driving.setSelected(true);
                transit.setSelected(false);
                mass_transit.setSelected(false);
                break;
            case TRANSIT:
                transit.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG );
                walk.setSelected(false);
                ride.setSelected(false);
                driving.setSelected(false);
                transit.setSelected(true);
                mass_transit.setSelected(false);
                break;
            case MASS_TRANSIT:
                mass_transit.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG );
                walk.setSelected(false);
                ride.setSelected(false);
                driving.setSelected(false);
                transit.setSelected(false);
                mass_transit.setSelected(true);
                break;
        }

    }
    private void chooseOtherTraffic(){
        walk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(BikingActivity.this,WalkActivity.class);
                startActivity(intent);
            }
        });
        transit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(BikingActivity.this,TransitActivity.class);
                startActivity(intent);
            }
        });
        driving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(BikingActivity.this,DrivingActivity.class);
                startActivity(intent);
            }
        });
        mass_transit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(BikingActivity.this,MassTransitActivity.class);
                startActivity(intent);
            }
        });
    }
    private void navigateTo(double latitude,double longitude){
        mBaidumap.setMyLocationEnabled(true);
        LatLng ll=new LatLng(latitude,longitude);//获取纬度值，经度值
        MapStatusUpdate update= MapStatusUpdateFactory.newLatLng(ll);
        mBaidumap.animateMapStatus(update);
        update=MapStatusUpdateFactory.zoomTo(19f);
        mBaidumap.animateMapStatus(update);
        MyLocationData.Builder locationBuilder=new MyLocationData.Builder();
        locationBuilder.latitude(latitude);
        locationBuilder.longitude(longitude);
        MyLocationData locationData=locationBuilder.build();
        mBaidumap.setMyLocationData(locationData);

    }
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSearch != null) {
            // 释放检索对象
            mSearch.destroy();
        }
        mBaidumap.clear();
        //  释放地图控件
        mMapView.onDestroy();
        mBaidumap.setMyLocationEnabled(false);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK ){
            Intent intent_2=new Intent(BikingActivity.this, SearchActivity.class);
            startActivity(intent_2);
            return true;
        }
        return false;
    }
}