package com.example.diplomadesign;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteLine;
import com.baidu.mapapi.search.route.MassTransitRoutePlanOption;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.example.diplomadesign.account.LocationInfo;
import com.example.diplomadesign.baidu.overlayutil.MassTransitRouteOverlay;
import com.example.diplomadesign.baidu.overlayutil.OverlayManager;
import com.example.diplomadesign.show_route.Route;
import com.example.diplomadesign.show_route.RouteAdapter;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class MassTransitActivity extends AppCompatActivity implements BaiduMap.OnMapClickListener,
        OnGetRoutePlanResultListener {
    // 浏览路线节点相关
    private Button mBtnPre = null; // 上一个节点
    private Button mBtnNext = null; // 下一个节点
    private MassTransitRouteLine mMassTransitRouteLine = null;
    private OverlayManager mRouteOverlay = null;
    private boolean mUseDefaultIcon = false; // 切换路线图标

    // 地图相关，使用继承MapView的MyRouteMapView目的是重写touch事件实现泡泡处理
    // 如果不处理touch事件，则无需继承，直接使用MapView即可
    private MapView mMapView = null;    // 地图View
    private BaiduMap mBaidumap = null;
    // 搜索相关
    private RoutePlanSearch mSearch = null;   // 搜索模块，也可去掉地图模块独立使用
    private MassTransitRouteResult mMassTransitRouteResult = null;
    private boolean hasShowDialog = false;
    // 换乘路线规划参数
    private MassTransitRoutePlanOption mMassTransitRoutePlanOption = null;
    private NodeUtils mNodeUtils;
    private TextView mStrartNodeView;
    private TextView mEndNodeView;
    private Spinner mTranstypeSpinner;
    private Spinner mIntercitySpinner;
    private Spinner mIncitySpinner;
    private TextView walk;
    private TextView ride;
    private TextView transit;
    private TextView mass_transit;
    private TextView driving;
    private List<Route> routeList=new ArrayList<>();//显示路程信息
    private TextView routeText_1;
    private TextView routeTime;//路程时间
    private TextView routeText_2;
    private TextView routePrice;//路程费用
    private final int WALK=1;
    private final int RIDE=2;
    private final int DRIVING=3;
    private final int TRANSIT=4;
    private final int MASS_TRANSIT=5;
    private TextView endNodeText;
    private PlanNode startNode;
    private PlanNode endNode;

    private static final String TAG = MassTransitActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mass_transit);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide(); //隐藏标题栏
        }
        if (Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        chooseThis(MASS_TRANSIT);
        chooseOtherTraffic();
        mStrartNodeView = (TextView) findViewById(R.id.st_node);
        mEndNodeView = (TextView) findViewById(R.id.ed_node);
        mIncitySpinner = (Spinner) findViewById(R.id.tactics_incity_sp);
        mTranstypeSpinner = (Spinner) findViewById(R.id.transtype_intercity_sp);
        mIntercitySpinner = (Spinner) findViewById(R.id.tactics_intercity_sp);

        // 初始化地图
        mMapView = (MapView) findViewById(R.id.map);
        mBaidumap = mMapView.getMap();

        mBtnPre = (Button) findViewById(R.id.pre);
        mBtnNext = (Button) findViewById(R.id.next);
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);
        routeTime=(TextView)findViewById(R.id.route_time);
        routePrice=(TextView)findViewById(R.id.route_price);
        routeText_1=(TextView)findViewById(R.id.route_text_1);
        routeText_2=(TextView)findViewById(R.id.route_text_2);
        endNodeText=(TextView)findViewById(R.id.ed_node);
        // 地图点击事件处理
        mBaidumap.setOnMapClickListener(this);
        getInfo();
        // 初始化搜索模块，注册事件监听
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);
        // 创建换乘路线规划option
        if (mMassTransitRoutePlanOption == null){
            // 设置策略前创建
            mMassTransitRoutePlanOption = new MassTransitRoutePlanOption();
        }
        // 设置市内公交换乘策略
        setTacticsIncity();
        // 设置跨城交通方式策略
        setTacticsIntercity();
        // 设置跨城交通方式策略
        setTransTypeIntercity();
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
     *  设置市内公交换乘策略
     */
    private void  setTacticsIncity(){
        List<String> list = new ArrayList<>();
        list.add("推荐");
        list.add("少换成");
        list.add("少步行");
        list.add("不坐地铁");
        list.add("时间短");
        list.add("地铁优先");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        mIncitySpinner.setAdapter(adapter);
        mIncitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        // 推荐
                        mMassTransitRoutePlanOption.tacticsIncity(MassTransitRoutePlanOption.TacticsIncity.ETRANS_SUGGEST);
                        break;
                    case 1:
                        // 少换成
                        mMassTransitRoutePlanOption.tacticsIncity(MassTransitRoutePlanOption.TacticsIncity.ETRANS_LEAST_TRANSFER);
                        break;
                    case 2:
                        // 少步行
                        mMassTransitRoutePlanOption.tacticsIncity(MassTransitRoutePlanOption.TacticsIncity.ETRANS_LEAST_WALK);
                        break;
                    case 3:
                        // 不坐地铁
                        mMassTransitRoutePlanOption.tacticsIncity(MassTransitRoutePlanOption.TacticsIncity.ETRANS_NO_SUBWAY);
                        break;
                    case 4:
                        // 时间短
                        mMassTransitRoutePlanOption.tacticsIncity(MassTransitRoutePlanOption.TacticsIncity.ETRANS_LEAST_TIME);
                        break;
                    case 5:
                        // 地铁优先
                        mMassTransitRoutePlanOption.tacticsIncity(MassTransitRoutePlanOption.TacticsIncity.ETRANS_SUBWAY_FIRST);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * 设置跨城交通方式策略
     */
    private void  setTacticsIntercity(){
        List<String> list = new ArrayList<>();
        list.add("时间短");
        list.add("出发早");
        list.add("价格低");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        mIntercitySpinner.setAdapter(adapter);
        mIntercitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        // 时间短
                        mMassTransitRoutePlanOption.tacticsIntercity(MassTransitRoutePlanOption.TacticsIntercity.ETRANS_LEAST_TIME);
                        break;
                    case 1:
                        // 出发早
                        mMassTransitRoutePlanOption.tacticsIntercity(MassTransitRoutePlanOption.TacticsIntercity.ETRANS_START_EARLY);
                        break;
                    case 2:
                        // 价格低
                        mMassTransitRoutePlanOption.tacticsIntercity(MassTransitRoutePlanOption.TacticsIntercity.ETRANS_LEAST_PRICE);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * 设置跨城交通方式策略
     */
    private void  setTransTypeIntercity(){
        List<String> list = new ArrayList<>();
        list.add("火车优先");
        list.add("飞机优先");
        list.add("大巴优先");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        mTranstypeSpinner.setAdapter(adapter);
        mTranstypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        // 火车优先
                        mMassTransitRoutePlanOption.transtypeintercity(MassTransitRoutePlanOption.TransTypeIntercity.ETRANS_TRAIN_FIRST);
                        break;
                    case 1:
                        // 飞机优先
                        mMassTransitRoutePlanOption.transtypeintercity(MassTransitRoutePlanOption.TransTypeIntercity.ETRANS_PLANE_FIRST);
                        break;
                    case 2:
                        // 大巴优先
                        mMassTransitRoutePlanOption.transtypeintercity(MassTransitRoutePlanOption.TransTypeIntercity.ETRANS_COACH_FIRST);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * 发起路线规划搜索示例
     */
    public void searchButtonProcess(View v) {
        // 重置浏览节点的路线数据
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);
        mBaidumap.clear();

//        final LatLng start = new LatLng(39.90923, 116.447428);
//        PlanNode startNode=PlanNode.withLocation(start);
//        final LatLng end = new LatLng(23.126853, 113.33573);
//        PlanNode endNode=PlanNode.withLocation(end);
        // 发起跨城公共路线检索
        mSearch.masstransitSearch(mMassTransitRoutePlanOption.from(startNode).to(endNode));
    }

    /**
     * 节点浏览
     */
    public void nodeClick(View v) {
        if (null != mMassTransitRouteLine && null != mMassTransitRouteResult) {
            mNodeUtils.browseTransitRouteNode(v,mMassTransitRouteLine,mMassTransitRouteResult);
        }
    }

    /**
     * 切换路线图标，刷新地图使其生效
     * 注意： 起终点图标使用中心对齐.
     */
    public void changeRouteIcon(View v) {
        if (mRouteOverlay == null) {
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
        mRouteOverlay.removeFromMap();
        mRouteOverlay.addToMap();
    }


    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {

    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult result) {

    }

    /**
     * 跨城公共交通路线结果回调
     *
     * @param result 跨城公交线路规划结果
     */
    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult result) {
        if (result != null && result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点模糊，获取建议列表
            Toast.makeText(MassTransitActivity.this, "起终点或途经点地址有岐义，通过result.getSuggestAddrInfo()接口获取建议查询信息", Toast.LENGTH_SHORT).show();
            return;
        }

        if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            Toast.makeText(MassTransitActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            return;
        }

        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            mMassTransitRouteResult = result;
            mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);

            if (!hasShowDialog) {
                // 列表选择
                SelectRouteDialog selectRouteDialog = new SelectRouteDialog(MassTransitActivity.this,
                        result.getRouteLines(), RouteLineAdapter.Type.MASS_TRANSIT_ROUTE);
                mMassTransitRouteResult = result;
                selectRouteDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        hasShowDialog = false;
                    }
                });
                selectRouteDialog.setOnItemInDlgClickLinster(new SelectRouteDialog.OnItemInDlgClickListener() {
                    public void onItemClick(int position) {
                        MassTransitActivity.MyMassTransitRouteOverlay overlay = new MassTransitActivity.MyMassTransitRouteOverlay(mBaidumap);//创建MassTransitRouteOverlay实例
                        mBaidumap.setOnMarkerClickListener(overlay);
                        mRouteOverlay = overlay;
                        mMassTransitRouteLine = mMassTransitRouteResult.getRouteLines().get(position);
                        overlay.setData(mMassTransitRouteResult.getRouteLines().get(position));
                        // 获取选择的路线
                        MassTransitRouteLine line = mMassTransitRouteResult.getRouteLines().get(position);
                        overlay.setData(line);
                        if (mMassTransitRouteResult.getOrigin().getCityId() == mMassTransitRouteResult.getDestination().getCityId()) {
                            // 同城
                            overlay.setSameCity(true);
                        } else {
                            // 跨城
                            overlay.setSameCity(false);
                        }

                        mBaidumap.clear();
                        overlay.addToMap();//在地图上绘制Overlay
                        overlay.zoomToSpan();

                        String arriveTime=line.getArriveTime();//到达时间
                        String price=Double.toString(line.getPrice())+" 元";//价格
                        routeText_1.setText("预计到达时间：");
                        routeTime.setText(arriveTime);
                        routeText_2.setText("预计路程费用：");
                        routePrice.setText(price);
                        List<List<MassTransitRouteLine.TransitStep>> steps=line.getNewSteps();

                        routeList.clear();
                        for (int j=0;j<steps.size();j++){
                            for (int i=0;i<steps.get(j).size();i++){
                                String a=steps.get(j).get(i).getInstructions();
                                if (a.length()!=0){
                                    Route route=new Route(a);
                                    routeList.add(route);
                                }
                            }
                        }

                        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.recycler_show_route);
                        LinearLayoutManager layoutManager=new LinearLayoutManager(MassTransitActivity.this);
                        recyclerView.setLayoutManager(layoutManager);
                        RouteAdapter adapter=new RouteAdapter(routeList);
                        recyclerView.setAdapter(adapter);

                    }

                });

                // 防止多次进入退出，Activity已经释放，但是Dialog仍然弹出，导致的异常释放崩溃
                if (!isFinishing()) {
                    selectRouteDialog.show();
                    hasShowDialog = true;
                }
            }
        }

    }


    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {

    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult result) {

    }

    private class MyMassTransitRouteOverlay extends MassTransitRouteOverlay {
        private MyMassTransitRouteOverlay(BaiduMap baiduMap) {
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
        ride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MassTransitActivity.this,BikingActivity.class);
                startActivity(intent);
            }
        });
        transit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MassTransitActivity.this,TransitActivity.class);
                startActivity(intent);
            }
        });
        driving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MassTransitActivity.this,DrivingActivity.class);
                startActivity(intent);
            }
        });
        walk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MassTransitActivity.this,WalkActivity.class);
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
        // 释放检索对象
        if (mSearch != null) {
            mSearch.destroy();
        }
        mBaidumap.clear();
        mMapView.onDestroy();
        mBaidumap.setMyLocationEnabled(false);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK ){
            Intent intent_2=new Intent(MassTransitActivity.this, SearchActivity.class);
            startActivity(intent_2);
            return true;
        }
        return false;
    }
}