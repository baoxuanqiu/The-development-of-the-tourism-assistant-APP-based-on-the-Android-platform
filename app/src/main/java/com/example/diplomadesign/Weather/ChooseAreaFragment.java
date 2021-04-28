package com.example.diplomadesign.Weather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.diplomadesign.R;
import com.example.diplomadesign.Weather.util.HttpUtil;
import com.example.diplomadesign.WeatherActivity;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    private static final String TAG = "ChooseAreaFragment";

    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;

    private TextView titleText;

    private Button backButton;

    private ListView listView;

    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();

    /**
     * 省列表
     */
    private List<String> provinceList=new ArrayList<>();

    /**
     * 市列表
     */
    private List<String> cityList=new ArrayList<>();

    /**
     * 县列表
     */
    private List<String> countyList=new ArrayList<>();

    /**
     * 选中的省份
     */
    private String selectedProvince;

    /**
     * 选中的城市
     */
    private String selectedCity;

    /**
     * 选中的县
     */
    private String selectedCounty;

    /**
     * 当前选中的级别
     */
    private int currentLevel;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);
        titleText=(TextView)view.findViewById(R.id.title_text);
        backButton=(Button)view.findViewById(R.id.back_button);
        listView=(ListView)view.findViewById(R.id.list_view);
        adapter=new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {   //当前列表条目属于省份
                    selectedProvince = provinceList.get(position);
                    queryCities(selectedProvince);
                } else if (currentLevel == LEVEL_CITY) {   //当前列表条目属于城市
                    selectedCity = cityList.get(position);
                    queryCounties(selectedProvince,selectedCity);
                }else if (currentLevel==LEVEL_COUNTY){    //当前列表条目属于区域
                    selectedCounty=countyList.get(position);
                    String cityID=queryCityID(selectedCity,selectedCounty); //获取当前城市区域下的城市ID
//                    Toast.makeText(getActivity(),cityID,Toast.LENGTH_LONG).show();
                    Intent intent=new Intent(getActivity(),WeatherActivity.class);  //跳转到“天气”界面
                    intent.putExtra("city_id",cityID);//保存城市ID
                    intent.putExtra("city_name",selectedCounty);//保存目的地县名称
                    startActivity(intent);
                    getActivity().finish();
//                    String weatherId_1=countyList.get(position).getWeatherId();
//                    String weatherId=weatherId_1.substring(2);
//                    String countyName=countyList.get(position).getCountyName();//获取县城的名称
//                    if (getActivity() instanceof WeatherActivity){
//                        Intent intent=new Intent(getActivity(),ShowWeatherActivity.class);
//                        intent.putExtra("weather_id",weatherId);
//                        intent.putExtra("county_name",countyName);
//                        startActivity(intent);
//                        getActivity().finish();
//                    }else if (getActivity() instanceof ShowWeatherActivity){
//                        ShowWeatherActivity activity=(ShowWeatherActivity)getActivity();
//                        activity.drawerLayout.closeDrawers();
//                        activity.swipeRefreshLayout.setRefreshing(true);
//                        activity.requestWeather(weatherId,countyName);
//                    }

                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  //返回键的触发事件
                if (currentLevel == LEVEL_COUNTY) {//若列表处于县级，返回市级列表
                    queryCities(selectedProvince);
                } else if (currentLevel == LEVEL_CITY) {//若列表处于市级，返回省级列表
                    queryProvinces();
                }
            }
        });
        queryProvinces();//从这里开始加载省级数据
    }

    //查询全国所有的省，从数据库查询
    private void queryProvinces(){
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        SQLiteDatabase db=SQLiteDatabase.openOrCreateDatabase(ImportDB.DB_PATH + "/" + ImportDB.DB_NAME, null);
        Cursor cursor = db.query(true, "city_info", new String[]{"province"}, null, null, "province", null, "id asc", null, null);
        dataList.clear();
        provinceList.clear();
        if (cursor.moveToFirst()) {
            do {
                String province = cursor.getString(cursor.getColumnIndex("province"));
                provinceList.add(province);
                dataList.add(province);
            } while (cursor.moveToNext());
        }
        cursor.close();
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
        currentLevel=LEVEL_PROVINCE;
    }

    //查询全国所有的市，从数据库查询
    private void queryCities(String province) {
        titleText.setText(province);
        backButton.setVisibility(View.VISIBLE);
        SQLiteDatabase db=SQLiteDatabase.openOrCreateDatabase(ImportDB.DB_PATH + "/" + ImportDB.DB_NAME, null);
        Cursor cursor = db.query(true, "city_info", new String[]{"city"}, "province=?", new String[]{province}, "city", null, "id asc", null, null);
        dataList.clear();
        cityList.clear();
        if (cursor.moveToFirst()) {
            do {
                String city = cursor.getString(cursor.getColumnIndex("city"));
                cityList.add(city);
                dataList.add(city);
            } while (cursor.moveToNext());
        }
        cursor.close();
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
        currentLevel=LEVEL_CITY;

    }

    //查询全国所有的县，从数据库查询
    private void queryCounties(String province,String city) {
        titleText.setText(city);
        backButton.setVisibility(View.VISIBLE);
        SQLiteDatabase db=SQLiteDatabase.openOrCreateDatabase(ImportDB.DB_PATH + "/" + ImportDB.DB_NAME, null);
        Cursor cursor = db.query(true, "city_info", new String[]{"diname"}, "province=? and city=?", new String[]{province,city}, "diname", null, "id asc", null, null);
        dataList.clear();
        countyList.clear();
        if (cursor.moveToFirst()) {
            do {
                String diname = cursor.getString(cursor.getColumnIndex("diname"));
                countyList.add(diname);
                dataList.add(diname);
            } while (cursor.moveToNext());
        }
        cursor.close();
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
        currentLevel=LEVEL_COUNTY;

    }

    private String queryCityID(String City,String Diname){
        String cityID="";
        SQLiteDatabase db=SQLiteDatabase.openOrCreateDatabase(ImportDB.DB_PATH + "/" + ImportDB.DB_NAME, null);
        Cursor cursor = db.query(true, "city_info", new String[]{"city_id"}, "city=? and diname=?", new String[]{City,Diname}, "city_id", null, null, null, null);
        if (cursor.moveToFirst()) {
            cityID = cursor.getString(cursor.getColumnIndex("city_id"));
        }
        cursor.close();
        return cityID;
    }



//    private void showProgressDialog(){
//        if (progressDialog==null){
//            progressDialog=new ProgressDialog(getActivity());
//            progressDialog.setMessage("正在加载....");
//            progressDialog.setCanceledOnTouchOutside(false);
//        }
//        progressDialog.show();
//    }
//
//    private void closeProgressDialog(){
//        if (progressDialog!=null){
//            progressDialog.dismiss();
//        }
//    }
}
