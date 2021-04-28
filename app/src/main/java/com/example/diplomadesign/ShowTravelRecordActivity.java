package com.example.diplomadesign;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diplomadesign.SlidingSwitcher.AutoScrollLayoutManager;
import com.example.diplomadesign.SlidingSwitcher.SlidingSwitcherAdapter;
import com.example.diplomadesign.travel_record.TravelPhotos;
import com.example.diplomadesign.travel_record.TravelRecord;
import com.google.gson.Gson;

import org.litepal.LitePal;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ShowTravelRecordActivity extends AppCompatActivity {
    private String travelRecordId;
    private ArrayList<Bitmap> photoList=new ArrayList<>();
    private TextView title;
    private TextView time;
    private TextView context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_travel_record);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide(); //隐藏标题栏
        }
        Intent intent_1=getIntent();
        travelRecordId=intent_1.getStringExtra("travel_record_id");
//        Toast.makeText(this,travelRecordId,Toast.LENGTH_LONG).show();//显示第几个记录
        title=(TextView)findViewById(R.id.title_text);
        time=(TextView)findViewById(R.id.time_text);
        context=(TextView)findViewById(R.id.context_text);

        getPhotos(travelRecordId);
        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.slidingSwitcher_recycler);
        AutoScrollLayoutManager layoutManager=new AutoScrollLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        SlidingSwitcherAdapter adapter=new SlidingSwitcherAdapter(photoList);
        recyclerView.setAdapter(adapter);
        // 调用该方法来触发自动滚动
        recyclerView.smoothScrollToPosition(adapter.getItemCount());

        setInfo(travelRecordId);
        setContext(travelRecordId);
    }
    private void getPhotos(String num){
        List<TravelPhotos> travelPhotos= LitePal.select("photo_size")
                .where("travelPhotos_id=?",num)
                .find(TravelPhotos.class);
        int photoSize=travelPhotos.get(0).getPhoto_size();
        if (photoSize==0){
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.scenery);
            photoList.add(bitmap);
        }
        int img_id=0;
        byte[] images = new byte[0];
        while (photoSize>0){
            String imgID="img"+Integer.toString(img_id);
            List<TravelPhotos> travelPhotosList=LitePal.select(imgID)
                    .where("travelPhotos_id=?",num)
                    .find(TravelPhotos.class);
            switch (img_id){
                case 0:
                    images=travelPhotosList.get(0).getImg0();
                    break;
                case 1:
                    images=travelPhotosList.get(0).getImg1();
                    break;
                case 2:
                    images=travelPhotosList.get(0).getImg2();
                    break;
                case 3:
                    images=travelPhotosList.get(0).getImg3();
                    break;
                case 4:
                    images=travelPhotosList.get(0).getImg4();
                    break;
                case 5:
                    images=travelPhotosList.get(0).getImg5();
                    break;
                case 6:
                    images=travelPhotosList.get(0).getImg6();
                    break;
                case 7:
                    images=travelPhotosList.get(0).getImg7();
                    break;
                case 8:
                    images=travelPhotosList.get(0).getImg8();
                    break;
                default:
            }
            Bitmap bitmap= BitmapFactory.decodeByteArray(images, 0, images.length);
            Bitmap bitmap_1=setImgSize(bitmap,1000);
            photoList.add(bitmap_1);
            img_id++;
            photoSize--;
        }
    }
    private void setInfo(String num){
        List<TravelRecord> travelRecords=LitePal.select("title")
                .where("travelRecord_id=?",num)
                .find(TravelRecord.class);
        String title_text=travelRecords.get(0).getTitle();
        title.setText(title_text);
        List<TravelRecord> travelRecords1=LitePal.select("time")
                .where("travelRecord_id=?",num)
                .find(TravelRecord.class);
        String time_text=travelRecords1.get(0).getTime();
        time.setText(time_text);
    }

    private void setContext(String num){
        FileInputStream inputStream=null;
        BufferedReader reader=null;
        StringBuilder content=new StringBuilder();
        try {
            inputStream=openFileInput(num);
            reader=new BufferedReader(new InputStreamReader(inputStream));
            String line="";
            while ((line=reader.readLine())!=null){
                content.append(line);
                content.append("\n");
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if (reader!=null){
                try {
                    reader.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        if (!TextUtils.isEmpty(content.toString())){
            context.setText(content.toString());
        }
    }

    //宽高同比例缩放 scale
    public Bitmap setImgSize(Bitmap bm, int scale ){
        // 获得图片的宽高.
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例.
        float k = ((float) scale) / width;
        // 取得想要缩放的matrix参数.
        Matrix matrix = new Matrix();
        matrix.postScale(k, k);
        // 得到新的图片.
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }

    //退出将退到“我的”界面
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent=new Intent(ShowTravelRecordActivity.this,MyActivity.class);
            startActivity(intent);
        }
        return super.onKeyDown(keyCode, event);
    }
}