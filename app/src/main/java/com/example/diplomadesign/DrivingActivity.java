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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyuncs.utils.StringUtils;
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
import com.baidu.mapapi.search.core.RouteNode;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.example.diplomadesign.account.LocationInfo;
import com.example.diplomadesign.baidu.overlayutil.DrivingRouteOverlay;
import com.example.diplomadesign.baidu.overlayutil.OverlayManager;
import com.example.diplomadesign.show_route.Route;
import com.example.diplomadesign.show_route.RouteAdapter;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class DrivingActivity extends AppCompatActivity implements OnGetRoutePlanResultListener, BaiduMap.OnMapClickListener{

    // ????????????????????????
    private Button mBtnPre = null; // ???????????????
    private Button mBtnNext = null; // ???????????????
    private RouteLine mRouteLine = null;
    private OverlayManager mRouteOverlay = null;
    // ??????View
    private MapView mMapView = null;
    private BaiduMap mBaidumap = null;

    // ???????????????????????????????????????????????????
    private RoutePlanSearch mSearch = null;

    // ??????????????????
    private DrivingRouteResult mDrivingRouteResult = null;

    private boolean mUseDefaultIcon = false;
    private boolean hasShowDialog = false;

    // ??????????????????view
    private Spinner mSpinner;

    // ????????????????????????
    private DrivingRoutePlanOption mDrivingRoutePlanOption;
    private NodeUtils mNodeUtils;
    private CheckBox mTrafficPolicyCB;
    private TextView walk;
    private TextView ride;
    private TextView driving;
    private TextView transit;
    private TextView mass_transit;
    private List<Route> routeList=new ArrayList<>();//??????????????????
    private TextView routeTime;//????????????
    private TextView routePrice;//????????????
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
        setContentView(R.layout.activity_driving);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide(); //???????????????
        }
        if (Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        mTrafficPolicyCB = (CheckBox) findViewById(R.id.traffic);

        // ?????????UI??????
        mBtnPre = (Button) findViewById(R.id.pre);
        mBtnNext = (Button) findViewById(R.id.next);
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);

        mMapView = (MapView) findViewById(R.id.map);
        endNodeText=(TextView)findViewById(R.id.ed_node);
        // ???????????????
        mBaidumap = mMapView.getMap();
        mNodeUtils = new NodeUtils(this, mBaidumap);
        mBaidumap.setOnMapClickListener(this);
        getInfo();
        // ??????????????????????????????????????????
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);
        // ?????????????????????????????????view
        mSpinner = (Spinner) findViewById(R.id.spinner);
        chooseThis(DRIVING);
        chooseOtherTraffic();
        routeTime=(TextView)findViewById(R.id.route_time);
        routePrice=(TextView)findViewById(R.id.route_price);
        routeText_1=(TextView)findViewById(R.id.route_text_1);
        routeText_2=(TextView)findViewById(R.id.route_text_2);
        List<String> list = new ArrayList<>();
        list.add("????????????");
        list.add("????????????");
        list.add("????????????");
        list.add("????????????");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_vict, list);
        adapter.setDropDownViewResource(R.layout.spinner_item_vict);
        mSpinner.setAdapter(adapter);
        initViewListener();
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
     * ?????????????????????
     */
    public void initViewListener() {
        // ??????????????????Option   // ?????????????????????
        mDrivingRoutePlanOption = new DrivingRoutePlanOption();
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 0:
                        // ?????????????????????  ??????????????????
                        mDrivingRoutePlanOption.policy(DrivingRoutePlanOption.DrivingPolicy.ECAR_TIME_FIRST);
                        break;
                    case 1:
                        // ??????????????????
                        mDrivingRoutePlanOption.policy(DrivingRoutePlanOption.DrivingPolicy.ECAR_AVOID_JAM);
                        break;
                    case 2:
                        // ??????????????????
                        mDrivingRoutePlanOption.policy(DrivingRoutePlanOption.DrivingPolicy.ECAR_DIS_FIRST);
                        break;
                    case 3:
                        // ??????????????????
                        mDrivingRoutePlanOption.policy(DrivingRoutePlanOption.DrivingPolicy.ECAR_FEE_FIRST);
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
     * ??????????????????????????????
     */
    public void searchButtonProcess(View v) {
        // ?????????????????????????????????
        mRouteLine = null;
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);
        // ????????????????????????
        mBaidumap.clear();
        // ????????????????????? ????????????
//        PlanNode startNode = PlanNode.withCityNameAndPlaceName(mEditStartCity.getText().toString().trim(), mStrartNodeView.getText().toString().trim());
        // ????????????
//        PlanNode endNode = PlanNode.withCityNameAndPlaceName(mEditEndCity.getText().toString().trim(), mEndNodeView.getText().toString().trim());
//                final LatLng start = new LatLng(39.90923, 116.447428);
//        final LatLng start = new LatLng(23.167387, 113.272422);
//        PlanNode startNode=PlanNode.withLocation(start);
//        final LatLng end = new LatLng(23.126853, 113.33573);
//        PlanNode endNode=PlanNode.withLocation(end);
        // ?????????????????????????????????
        if (mTrafficPolicyCB.isChecked()) {
            // ????????????
            mDrivingRoutePlanOption.trafficPolicy(DrivingRoutePlanOption.DrivingTrafficPolicy.ROUTE_PATH_AND_TRAFFIC);
        } else {
            // ????????????
            mDrivingRoutePlanOption.trafficPolicy(DrivingRoutePlanOption.DrivingTrafficPolicy.ROUTE_PATH);
        }

        // ????????????????????????
        mSearch.drivingSearch(mDrivingRoutePlanOption.from(startNode).to(endNode));
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

    /**
     * ????????????????????????
     *
     * @param result ??????????????????
     */
    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
        if (result != null && result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // ?????????????????????????????????????????????????????????????????????????????????
            // result.getSuggestAddrInfo()
            Toast.makeText(DrivingActivity.this, "????????????????????????????????????,?????? result.getSuggestAddrInfo()??????????????????????????????", Toast.LENGTH_SHORT).show();
            return;
        }
        if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            Toast.makeText(DrivingActivity.this, "????????????????????????", Toast.LENGTH_SHORT).show();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);
            if (result.getRouteLines().size() > 1) {
                mDrivingRouteResult = result;
                if (!hasShowDialog) {
                    // ????????????Dialog
                    SelectRouteDialog selectRouteDialog = new SelectRouteDialog(DrivingActivity.this,
                            result.getRouteLines(), RouteLineAdapter.Type.DRIVING_ROUTE);
                    selectRouteDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            hasShowDialog = false;
                        }
                    });
                    selectRouteDialog.setOnItemInDlgClickLinster(new SelectRouteDialog.OnItemInDlgClickListener() {
                        public void onItemClick(int position) {
                            // ?????????????????????
                            mRouteLine = mDrivingRouteResult.getRouteLines().get(position);
                            DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaidumap);//??????DrivingRouteOverlay??????
                            mBaidumap.setOnMarkerClickListener(overlay);
                            mRouteOverlay = overlay;
                            overlay.setData(mDrivingRouteResult.getRouteLines().get(position));
                            overlay.addToMap();
                            overlay.zoomToSpan();
                            showRouteInfo();
                        }

                    });
                    selectRouteDialog.show();
                    hasShowDialog = true;
                }
            } else if (result.getRouteLines().size() == 1) {
                mRouteLine = result.getRouteLines().get(0);
                DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaidumap);//??????DrivingRouteOverlay??????
                mRouteOverlay = overlay;
                mBaidumap.setOnMarkerClickListener(overlay);
                overlay.setData(result.getRouteLines().get(0));//???DrivingRouteOverlay??????????????????
                overlay.addToMap();//??????????????????DrivingRouteOverlay
                overlay.zoomToSpan();
                showRouteInfo();
                mBtnPre.setVisibility(View.VISIBLE);
                mBtnNext.setVisibility(View.VISIBLE);
            } else {
                Log.d("route result", "?????????<0");
                return;
            }
        }
    }

    private void showRouteInfo(){
        List<DrivingRouteLine.DrivingStep> steps=mRouteLine.getAllStep();
        routeList.clear();
        for (int i=0;i<steps.size();i++){
            String a=steps.get(i).getInstructions();
            if (a.length()!=0){
                Route route=new Route(a);
                routeList.add(route);
            }
        }
        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.recycler_show_route);
        LinearLayoutManager layoutManager=new LinearLayoutManager(DrivingActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        RouteAdapter adapter=new RouteAdapter(routeList);
        recyclerView.setAdapter(adapter);
        String distance=String.valueOf(mRouteLine.getDistance())+" ???";
        routeText_1.setText("??????????????????:");
        routeTime.setText(distance);
    }
    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult result) {

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult result) {

    }

    @Override
    public void onMapClick(LatLng point) {
        // ????????????InfoWindow
        mBaidumap.hideInfoWindow();
    }

    @Override
    public void onMapPoiClick(MapPoi poi) {

    }

    // ??????RouteOverly
    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

        private MyDrivingRouteOverlay(BaiduMap baiduMap) {
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

    /**
     * ????????????
     */
    public void nodeClick(View v) {
        if (null != mRouteLine) {
            mNodeUtils.browseRoutNode(v, mRouteLine);
        }
    }

    /**
     * ?????????????????????????????????????????????
     * ????????? ?????????????????????????????????.
     */
    public void changeRouteIcon(View v) {
        if (mRouteOverlay == null) {
            return;
        }
        if (mUseDefaultIcon) {
            ((Button) v).setText("????????????????????????");
            Toast.makeText(this,
                    "??????????????????????????????",
                    Toast.LENGTH_SHORT).show();

        } else {
            ((Button) v).setText("?????????????????????");
            Toast.makeText(this,
                    "?????????????????????????????????",
                    Toast.LENGTH_SHORT).show();

        }
        mUseDefaultIcon = !mUseDefaultIcon;
        mRouteOverlay.removeFromMap();
        mRouteOverlay.addToMap();
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
                Intent intent=new Intent(DrivingActivity.this,BikingActivity.class);
                startActivity(intent);
            }
        });
        transit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(DrivingActivity.this,TransitActivity.class);
                startActivity(intent);
            }
        });
        walk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(DrivingActivity.this,WalkActivity.class);
                startActivity(intent);
            }
        });
        mass_transit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(DrivingActivity.this,MassTransitActivity.class);
                startActivity(intent);
            }
        });
    }
    private void navigateTo(double latitude,double longitude){
        mBaidumap.setMyLocationEnabled(true);
        LatLng ll=new LatLng(latitude,longitude);//???????????????????????????
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
        // ??????????????????
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
            Intent intent_2=new Intent(DrivingActivity.this, SearchActivity.class);
            startActivity(intent_2);
            return true;
        }
        return false;
    }
}