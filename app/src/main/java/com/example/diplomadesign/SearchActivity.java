package com.example.diplomadesign;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.example.diplomadesign.account.LocationInfo;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchActivity extends AppCompatActivity{
    private ImageView back_button;
    private MapView mapView;
    private BaiduMap baiduMap;
    private AutoCompleteTextView mkeyWordView=null;
    private SuggestionSearch mSuggestionSearch=null;
    private ListView mSugListView;
    private static String cityname;//????????????
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide(); //???????????????
        }
        if (Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        List<LocationInfo> locationInfoList_1= LitePal.where("id=?","1")
                .find(LocationInfo.class);
        if (locationInfoList_1.size()!=0) {
            LocationInfo locationInfo = locationInfoList_1.get(0);
            cityname=locationInfo.getCity();
        }
//        Intent intent_2=getIntent();
//        cityname=intent_2.getStringExtra("cityName");//??????????????????
        Toast.makeText(SearchActivity.this,cityname,Toast.LENGTH_SHORT).show();
        back_button=(ImageView) findViewById(R.id.back_my_activity);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_1=new Intent(SearchActivity.this,MainActivity.class);
                startActivity(intent_1);
            }
        });
        mSugListView=(ListView)findViewById(R.id.search_list);

        mapView=(MapView)findViewById(R.id.bmapView);
        baiduMap=mapView.getMap();//??????BaiduMap??????

        mkeyWordView=(AutoCompleteTextView) findViewById(R.id.searchkey);//??????????????????
        searchButton=(Button)findViewById(R.id.search_button);
        mkeyWordView.setThreshold(1);
        // ??????????????????????????????????????????????????????
        mkeyWordView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                //??????????????????
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                //??????????????????
            }

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                //??????????????????
                if (cs.length() <= 0) {
                    return;
                }
                // ??????????????????????????????????????????????????????onSuggestionResult()?????????
                mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())
                        .keyword(cs.toString()) // ?????????
                        .city(cityname)); // ??????
            }
        });
        mSuggestionSearch=SuggestionSearch.newInstance();// ????????????????????????
        mSuggestionSearch.setOnGetSuggestionResultListener(listener);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String keyWord=mkeyWordView.getText().toString();
                if (!keyWord.equals("")){
                    Intent intent=new Intent(SearchActivity.this,SearchActivity2.class);
                    intent.putExtra("destination",mkeyWordView.getText().toString());
                    startActivity(intent);
                }else{
                    Toast.makeText(SearchActivity.this,"????????????????????????",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    OnGetSuggestionResultListener listener = new OnGetSuggestionResultListener() {
        @Override
        public void onGetSuggestionResult(SuggestionResult suggestionResult) {
            //??????sug????????????
            if (suggestionResult == null || suggestionResult.getAllSuggestions() == null) {
                return;
            }

            List<HashMap<String, String>> suggest = new ArrayList<>();
            for (SuggestionResult.SuggestionInfo info : suggestionResult.getAllSuggestions()) {//info??????????????????????????????
                if (info.getKey() != null && info.getAddress().length()!=0 && info.getCity() != null) {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("key",info.getKey());//??????????????????
                    map.put("city",info.getCity());//?????????????????????
                    map.put("dis",info.getAddress());//????????????????????????
                    map.put("latitude",Double.toString(info.getPt().latitude));//??????????????????
                    map.put("longitude",Double.toString(info.getPt().longitude));//??????????????????
                    suggest.add(map);  //suggest???List<HashMap<String,String>>??????
                }
            }

            SimpleAdapter simpleAdapter = new SimpleAdapter(getApplicationContext(),
                    suggest,
                    R.layout.item_layout,
                    new String[]{"key", "city","dis"},
                    new int[]{R.id.sug_key, R.id.sug_city, R.id.sug_dis});
            mSugListView.setAdapter(simpleAdapter);//mSugListView?????????ListView?????????????????????
            simpleAdapter.notifyDataSetChanged();
            mSugListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //                     ???position?????????????????????????????????
                    HashMap<String,String> map=(HashMap<String,String>)mSugListView.getItemAtPosition(position);
                    List<LocationInfo> locationInfoList= LitePal.where("id=?","2")
                            .find(LocationInfo.class);
                    LocationInfo locationInfo=new LocationInfo();
                    locationInfo.setId(2);
                    locationInfo.setCity(map.get("city"));
                    locationInfo.setName(map.get("key"));
                    locationInfo.setLatitude(map.get("latitude"));
                    locationInfo.setLongitude(map.get("longitude"));
                    if (locationInfoList.isEmpty()){
                        locationInfo.save();
                    }else {
                        locationInfo.updateAll("id =?","2");//????????????id=2???????????????????????????????????????
                    }
                    Intent intent=new Intent(SearchActivity.this,WalkActivity.class);
                    startActivity(intent);
//                    HashMap<String,String> map=(HashMap<String,String>)mSugListView.getItemAtPosition(position);
//                    Intent intent_3=new Intent(SearchActivity.this,RouteActivity.class);
////                    intent_3.putExtra("go_to_address",map.get("dis"));
//                    intent_3.putExtra("go_to_key",map.get("key"));
//                    intent_3.putExtra("go_to_location",map.get("location"));
//                    startActivity(intent_3);
//                    Intent intent_3=new Intent(SearchActivity.this,Route_1Activity.class);
//                    startActivity(intent_3);
//                    Log.d("SearchActivity:",map.get("location"));
                }
            });
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        // ???activity??????onResume???????????????mMapView. onResume ()
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // ???activity??????onPause???????????????mMapView. onPause ()
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ??????????????????
        baiduMap.clear();
        // ???activity??????onDestroy???????????????mMapView. onDestroy ()
        mapView.onDestroy();
        mSuggestionSearch.destroy();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK ){
            Intent intent_2=new Intent(SearchActivity.this, MainActivity.class);
            startActivity(intent_2);
            return true;
        }
        return false;
    }
}
