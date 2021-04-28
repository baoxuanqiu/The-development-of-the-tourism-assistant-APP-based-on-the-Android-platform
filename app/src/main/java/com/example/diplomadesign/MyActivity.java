package com.example.diplomadesign;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diplomadesign.account.Account;
import com.example.diplomadesign.account.GetAccountInfo;
import com.example.diplomadesign.travel_record.TravelRecord;
import com.example.diplomadesign.travel_record.TravelRecordAdapter;
import com.example.diplomadesign.travel_record.TravelRecordItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.litepal.LitePal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyActivity extends AppCompatActivity {
    private TextView home_text;
    private TextView my_text;
    private TextView weather_text;
    private ImageView myimage;
    private TextView myName;
    private Button login;
    private Boolean isLogin;
    private Dialog dialog;
    private View inflate;
    private TextView camera;
    private TextView pic;
    private TextView cancel;
    private Uri imageUri;
    private static final int TAKE_PHOTO=1;
    private static final int CHOOSE_PHOTO=2;
    private Boolean isHide=false;
    private String my_telephone;
    private FloatingActionButton fab;
    private List<TravelRecordItem> travelRecordItems=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide(); //隐藏标题栏
        }
        if (Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        my_text=(TextView)findViewById(R.id.my_activity_text);
        home_text=(TextView)findViewById(R.id.home_activity_text);
        weather_text=(TextView)findViewById(R.id.weather_activity_text);
        my_text .getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG );
        home_text.setSelected(false);
        my_text.setSelected(true);
        weather_text.setSelected(false);
        home_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_1=new Intent(MyActivity.this,MainActivity.class);
                startActivity(intent_1);
            }
        });
        weather_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_2=new Intent(MyActivity.this,WeatherActivity.class);
                startActivity(intent_2);
            }
        });

        if (initTravelRecordItem()){
            RecyclerView recyclerView=(RecyclerView)findViewById(R.id.recycler_view);
            LinearLayoutManager layoutManager=new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            TravelRecordAdapter travelRecordAdapter=new TravelRecordAdapter(travelRecordItems);
            recyclerView.setAdapter(travelRecordAdapter);
        }
        myimage=(ImageView)findViewById(R.id.user_head_image);
        myName=(TextView)findViewById(R.id.myname_text);
        login=(Button)findViewById(R.id.login_button);//登录账号或显示账户信息
        GetAccountInfo getAccountInfo=new GetAccountInfo();
        isLogin=getAccountInfo.getIsLogin();//判断是否登录
        if (isLogin){
            my_telephone=getAccountInfo.getAccount_tele();
            String real_name=getAccountInfo.getAccount_name();
            myName.setText(real_name);
            Bitmap real_image=getAccountInfo.getAccount_image();
            myimage.setImageBitmap(real_image);
        }
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isLogin){
                    Intent intent_3=new Intent(MyActivity.this,LoginActivity.class);
                    startActivity(intent_3);
                }else {
                    Intent intent_4=new Intent(MyActivity.this,AccountInfoActivity.class);
                    startActivity(intent_4);
                }
            }
        });
        myimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLogin){
                    showBottomChoose(v);
                }else {
                    Toast.makeText(MyActivity.this,"请先登录才能更换头像",Toast.LENGTH_SHORT).show();
                }
            }
        });

        fab=(FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent_5=new Intent(MyActivity.this,Travel_RecordActivity.class);
//                startActivity(intent_5);
                if (isLogin){
                    Intent intent_5=new Intent(MyActivity.this,Travel_RecordActivity.class);
                    startActivity(intent_5);
                }else {
                    Toast.makeText(MyActivity.this,"请先登录才能记录旅程",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void showBottomChoose(View view){
        dialog = new Dialog(this,R.style.DialogTheme);
        //填充对话框的布局
        inflate = LayoutInflater.from(this).inflate(R.layout.bottom_choose_photo, null);
        //初始化控件
        camera = (TextView) inflate.findViewById(R.id.camera);
        pic = (TextView) inflate.findViewById(R.id.picture);
        cancel = (TextView) inflate.findViewById(R.id.cancel);
        camera.setOnClickListener(new View.OnClickListener() {//选择拍照
            @Override
            public void onClick(View v) {
                dialog.hide();
                isHide=true;
                //创建File对象，用于存储拍照后的图片
                File outputImage=new File(getExternalCacheDir(),"output_image.jpg");
                try{
                    if(outputImage.exists()){
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
                if(Build.VERSION.SDK_INT>=24){//将File对象转换为Uri对象
                    imageUri= FileProvider.getUriForFile(MyActivity.this,"com.example.diplomadesign.fileprovider",outputImage);
                }else{
                    imageUri= Uri.fromFile(outputImage);
                }
                //启动相机程序
                Intent intent_2=new Intent("android.media.action.IMAGE_CAPTURE");
                intent_2.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                startActivityForResult(intent_2,TAKE_PHOTO);
            }
        });
        pic.setOnClickListener(new View.OnClickListener() {//选择从相册选择
            @Override
            public void onClick(View v) {
                dialog.hide();
                isHide=true;
                //申请获取相机的权限
                if(ContextCompat.checkSelfPermission(MyActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MyActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else{
                    openAlbum();
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {//选择取消
            @Override
            public void onClick(View v) {
//                Toast.makeText(MyActivity.this,"You clicked the cancel",Toast.LENGTH_SHORT).show();
                dialog.hide();
                isHide=true;
            }
        });
        dialog.setContentView(inflate);//将布局设置给Dialog
        Window dialogWindow = dialog.getWindow();//获取当前Activity所在的窗体
        dialogWindow.setGravity( Gravity.BOTTOM);//设置Dialog从窗体底部弹出
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();//获得窗体的属性
//        lp.y = 20;//设置Dialog距离底部的距离
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;//这两句是为了保证textView可以水平满屏
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialogWindow.setAttributes(lp);//将属性设置给窗体
        dialog.show();//显示对话框
    }
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case TAKE_PHOTO:
                if (resultCode==RESULT_OK){
                    try {
                        //将拍摄的照片显示出来
                        Bitmap bitmap= BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        myimage.setImageBitmap(bitmap);
                        Account account=new Account();
                        account.setHeadshot(img(bitmap));
                        account.updateAll("telephone=?",my_telephone);
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if(resultCode==RESULT_OK){
                    Uri uri=data.getData();
                    try {
                        Bitmap bitmap=getBitmapFromUri(uri);
                        myimage.setImageBitmap(bitmap);
                        Account account=new Account();
                        account.setHeadshot(img(bitmap));
                        account.updateAll("telephone=?",my_telephone);
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }

    private final Bitmap getBitmapFromUri(Uri uri) throws FileNotFoundException {
        ParcelFileDescriptor parcelFileD = getContentResolver().openFileDescriptor(uri, "r");
        Bitmap bitmap_1;
        if (parcelFileD != null) {
            Bitmap bitmap_2 = BitmapFactory.decodeFileDescriptor(parcelFileD.getFileDescriptor());
            bitmap_1 = bitmap_2;
        } else {
            bitmap_1 = null;
        }
        return bitmap_1;
    }

    //打开相册
    private void openAlbum(){
        Intent intent_3=new Intent("android.intent.action.OPEN_DOCUMENT");
        intent_3.addCategory(Intent.CATEGORY_OPENABLE);
        intent_3.setType("image/*");
        startActivityForResult(intent_3,CHOOSE_PHOTO);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isHide==true){
            dialog.dismiss();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else{
                    Toast.makeText(this,"你关闭了打开相册的权限",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    //将Bitmap图片转化为字节形式
    private byte[]img(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100, baos);
        return baos.toByteArray();
    }

    private Boolean initTravelRecordItem(){
        List<TravelRecord> travelRecords= LitePal.findAll(TravelRecord.class);
        if (travelRecords.isEmpty()){
            return false;
        }else{
            for (TravelRecord travelRecord:travelRecords){
                String travel_title=travelRecord.getTitle();
                String travel_time=travelRecord.getTime();
                String travel_id=String.valueOf(travelRecord.getTravelRecord_id());
                TravelRecordItem travelRecordItem=new TravelRecordItem(travel_title,travel_time,travel_id);
                travelRecordItems.add(travelRecordItem);
            }
            return true;
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
