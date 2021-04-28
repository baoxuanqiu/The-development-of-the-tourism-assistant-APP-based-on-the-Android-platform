package com.example.diplomadesign;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.baidu.mapapi.search.weather.LanguageType;
import com.baidu.mapapi.search.weather.OnGetWeatherResultListener;
import com.baidu.mapapi.search.weather.WeatherDataType;
import com.baidu.mapapi.search.weather.WeatherResult;
import com.baidu.mapapi.search.weather.WeatherSearch;
import com.baidu.mapapi.search.weather.WeatherSearchForecasts;
import com.baidu.mapapi.search.weather.WeatherSearchOption;
import com.baidu.mapapi.search.weather.WeatherSearchRealTime;
import com.baidu.mapapi.search.weather.WeatherServerType;
import com.bumptech.glide.Glide;
import com.example.diplomadesign.Weather.ImportDB;
import com.example.diplomadesign.Weather.util.HttpUtil;
import com.example.diplomadesign.account.LocationInfo;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private TextView home_text;
    private TextView my_text;
    private TextView weather_text;
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
//    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;
    public SwipeRefreshLayout swipeRefreshLayout;
    public DrawerLayout drawerLayout;
    private Button navButton;
    private WeatherSearch mWeatherSearch;
    private MapView mapView;
    private BaiduMap baiduMap;
    private LocationClient mLocationClient;
    private ImportDB importDB;
    private ProgressBar progressBar;
    private boolean isGetLocation=false;//数据库中是否有保存我的位置
    private boolean isGetOtherCity=false;//是否选择了其他城市
    private List<WeatherSearchForecasts> weatherSearchForecasts=new ArrayList<>();
    private String cityID="";
    private String cityName="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide(); //隐藏标题栏
        }
        if (Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        //创建city_info数据库
        importDB = new ImportDB(this);
        importDB.openDatabase();

        my_text=(TextView)findViewById(R.id.my_activity_text);
        home_text=(TextView)findViewById(R.id.home_activity_text);
        weather_text=(TextView)findViewById(R.id.weather_activity_text);
        weather_text.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG );
        home_text.setSelected(false);
        my_text.setSelected(false);
        weather_text.setSelected(true);
        my_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_1=new Intent(WeatherActivity.this,MyActivity.class);
                startActivity(intent_1);
            }
        });
        home_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_2=new Intent(WeatherActivity.this,MainActivity.class);
                startActivity(intent_2);
            }
        });

        weatherLayout=(ScrollView)findViewById(R.id.weather_layout);
        titleCity=(TextView)findViewById(R.id.title_city);
        titleUpdateTime=(TextView)findViewById(R.id.title_update_time);
        degreeText=(TextView)findViewById(R.id.degree_text);
        weatherInfoText=(TextView)findViewById(R.id.weather_info_text);
        forecastLayout=(LinearLayout)findViewById(R.id.forecast_layout);
        aqiText=(TextView)findViewById(R.id.aqi_text);
        pm25Text=(TextView)findViewById(R.id.pm25_text);
        comfortText=(TextView)findViewById(R.id.comfort_text);
//        carWashText=(TextView)findViewById(R.id.car_wash_text);
        sportText=(TextView)findViewById(R.id.sport_text);
        bingPicImg=(ImageView)findViewById(R.id.bing_pic_img);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        navButton=(Button)findViewById(R.id.nav_button);
        mWeatherSearch = WeatherSearch.newInstance();

        Intent intent=getIntent();
        cityID=intent.getStringExtra("city_id");
        cityName=intent.getStringExtra("city_name");
        if (cityID!=null){
            isGetOtherCity=true;
            titleCity.setText(cityName);
            initView(cityID);
        }
        if (!isGetOtherCity){
            List<LocationInfo> locationInfos= LitePal.findAll(LocationInfo.class);
            if (!locationInfos.isEmpty()){
                List<LocationInfo> locationInfoList=LitePal.where("name=?","MyLocation")
                        .find(LocationInfo.class);
                if (!locationInfoList.isEmpty()){
                    isGetLocation=true;
                    LocationInfo locationInfo=locationInfoList.get(0);
                    titleCity.setText(locationInfo.getDiname());
                    initView(locationInfo.getCityID());
                }
            }
        }
        if (!isGetOtherCity && !isGetLocation){
            Toast.makeText(WeatherActivity.this,"请回到首页进行定位！！",Toast.LENGTH_SHORT).show();
        }


        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String bingPic=prefs.getString("bing_pic",null);
        if (bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }


        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadBingPic();
            }
        });
    }

    private void initView(final String districtId){
        WeatherSearchOption weatherSearchOption = new WeatherSearchOption();
        weatherSearchOption
                .weatherDataType(WeatherDataType.WEATHER_DATA_TYPE_FORECASTS_FOR_DAY)
                .districtID(districtId)
                .languageType(LanguageType.LanguageTypeChinese)
                .serverType(WeatherServerType.WEATHER_SERVER_TYPE_DEFAULT);
        mWeatherSearch.setWeatherSearchResultListener(new OnGetWeatherResultListener() {
            @Override
            public void onGetWeatherResultListener(final WeatherResult weatherResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (null == weatherResult) {
                            return;
                        }
//                        List<WeatherSearchForecasts> weatherSearchForecasts=new ArrayList<>();
                        weatherSearchForecasts=weatherResult.getForecasts();
                        if (weatherSearchForecasts==null || weatherSearchForecasts.size()==0){
                            return;
                        }
                        forecastLayout.removeAllViews();
                        for (WeatherSearchForecasts weatherSearchForecasts1:weatherSearchForecasts){
                            String data=weatherSearchForecasts1.getDate();
                            String lowestTmp=String.valueOf(weatherSearchForecasts1.getLowestTemp())+ "℃";
                            String highestTmp=String.valueOf(weatherSearchForecasts1.getHighestTemp())+ "℃";
                            String phenomenonDay=weatherSearchForecasts1.getPhenomenonDay();
                            String phenomenonNight=weatherSearchForecasts1.getPhenomenonNight();
                            String phenomenon=phenomenonDay+"->"+phenomenonNight;
                            View view= LayoutInflater.from(WeatherActivity.this).inflate(R.layout.show_weather_forecast_item,forecastLayout,false);
                            TextView dataText=(TextView)view.findViewById(R.id.data_text);
                            TextView infoText=(TextView)view.findViewById(R.id.info_text);
                            TextView maxText=(TextView)view.findViewById(R.id.max_text);
                            TextView minText=(TextView)view.findViewById(R.id.min_text);
                            dataText.setText(data);
                            infoText.setText(phenomenon);
                            maxText.setText(highestTmp);
                            minText.setText(lowestTmp);
                            forecastLayout.addView(view);
                        }

                    }
                });
            }
        });
        mWeatherSearch.request(weatherSearchOption);

        final WeatherSearchOption weatherSearchOption_1=new WeatherSearchOption();
        weatherSearchOption_1.weatherDataType(WeatherDataType.WEATHER_DATA_TYPE_ALL)
                .districtID(districtId)
                .languageType(LanguageType.LanguageTypeChinese)
                .serverType(WeatherServerType.WEATHER_SERVER_TYPE_DEFAULT);
        mWeatherSearch.setWeatherSearchResultListener(new OnGetWeatherResultListener() {
            @Override
            public void onGetWeatherResultListener(final WeatherResult weatherResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (null == weatherResult) {
                            return;
                        }
                        WeatherSearchRealTime weatherSearchRealTime = weatherResult.getRealTimeWeather();
                        if (null == weatherSearchRealTime) {
                            return;
                        }
                        String updateTime =weatherSearchRealTime.getUpdateTime();
                        String updateTime_1=updateTime.substring(8,10);
                        String updateTime_2=updateTime.substring(10,12);
                        String updateTime_3=updateTime_1+":"+updateTime_2;
                        titleUpdateTime.setText(updateTime_3);
                        String temp = weatherSearchRealTime.getTemperature() + "℃";
                        degreeText.setText(temp);
                        String phenomenon = weatherSearchRealTime.getPhenomenon();
                        weatherInfoText.setText(phenomenon);
                        String sensoryTemp = weatherSearchRealTime.getSensoryTemp()+"℃";
                        String relativeHumidity = weatherSearchRealTime.getRelativeHumidity() + "%";
                        pm25Text.setText(relativeHumidity);
                        aqiText.setText(sensoryTemp);
                        String windDirection="风向： "+String.valueOf(weatherSearchRealTime.getWindDirection());
                        String windPower="风力： "+String.valueOf(weatherSearchRealTime.getWindPower());
                        comfortText.setText(windDirection);
                        sportText.setText(windPower);
                    }
                });
            }
        });
        mWeatherSearch.request(weatherSearchOption_1);
    }

    private void loadBingPic(){
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mWeatherSearch) {
            mWeatherSearch.destroy();
        }
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
