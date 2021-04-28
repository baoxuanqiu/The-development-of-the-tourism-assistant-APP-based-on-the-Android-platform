package com.example.diplomadesign;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.CallLog;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diplomadesign.account.Account;
import com.example.diplomadesign.travel_record.GridViewAdapter;
import com.example.diplomadesign.travel_record.TravelPhotos;
import com.example.diplomadesign.travel_record.TravelRecord;

import org.litepal.LitePal;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Travel_RecordActivity extends AppCompatActivity {
    private GridView mGridView;
    private GridViewAdapter adapter;//创建自定义类GridViewAdapter的实例
    private static final int TAKE_PHOTO=1;
    private static final int CHOOSE_PHOTO=2;
    private ArrayList<Bitmap> photoList=new ArrayList<>();
    private ArrayList<String> isShowDelete=new ArrayList<>();
    private String showDelete="true";
    private String noShowDelete="false";
    private Handler handler;
    private EditText text;
    private EditText title;
    private Uri imageUri;
    private Dialog dialog;
    private Dialog dialog_1;
    private View inflate;
    private View inflate_1;
    private TextView camera;
    private TextView pic;
    private TextView cancel;
    private Boolean isHide=false;
    private Boolean isHide_1=false;
    private LinearLayout back;
    private ImageView show_img;
    private Button complete_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel__record);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide(); //隐藏标题栏
        }

        complete_button=(Button)findViewById(R.id.complete_button);
        back=(LinearLayout)findViewById(R.id.return_linear);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Travel_RecordActivity.this,MyActivity.class);
                startActivity(intent);
            }
        });
        text=(EditText)findViewById(R.id.travel_text);
        title=(EditText)findViewById(R.id.travel_title);
        mGridView=(GridView) findViewById(R.id.gv_test);
        deletePhoto();
        adapter=new GridViewAdapter(Travel_RecordActivity.this,handler,photoList,isShowDelete);
        mGridView.setAdapter(adapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {       //九宫格某一张图片的点击事件
                if((position==parent.getChildCount()-1)&&(photoList.size()<9)){
                    showBottomChoose(view);            //显示选择拍照或是照片的选项
//                    Toast.makeText(Travel_RecordActivity.this, "您点击了添加", Toast.LENGTH_SHORT).show();
                }else {
                    if (isShowDelete.get(position).equals(showDelete)){   //showDelete为true,即显示删除的标记
                        isShowDelete.set(position,noShowDelete);          //noShowDelete为false,即不显示删除的标记
                        adapter = new GridViewAdapter(Travel_RecordActivity.this, handler,photoList,isShowDelete);//重新绑定一次adapter
                        mGridView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();//刷新gridview
                    }else{       //显示点击的图片
                        dialog_1 = new Dialog(Travel_RecordActivity.this,R.style.DialogTheme);
                        //填充对话框的布局
                        inflate_1 = LayoutInflater.from(Travel_RecordActivity.this).inflate(R.layout.center_show_photo, null);
                        show_img=(ImageView)inflate_1.findViewById(R.id.show_photo);
                        show_img.setImageBitmap(photoList.get(position));
                        dialog_1.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                        dialog_1.setContentView(inflate_1);//将布局设置给Dialog
                        dialog_1.show();
                        show_img.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog_1.hide();
                                isHide_1=true;
                            }
                        });
                    }
                }
            }

        });
        mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if ((position<parent.getChildCount()-1)||(photoList.size()==9)){
                    if (isShowDelete.get(position).equals(showDelete)){
                        isShowDelete.set(position,noShowDelete);//将删除图标改为不显示
                        adapter = new GridViewAdapter(Travel_RecordActivity.this, handler,photoList,isShowDelete);//重新绑定一次adapter
                        mGridView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();//刷新gridview
                    }else{
                        isShowDelete.set(position,showDelete);
                        adapter = new GridViewAdapter(Travel_RecordActivity.this, handler,photoList,isShowDelete);//重新绑定一次adapter
                        mGridView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();//刷新gridview
                        deletePhoto();
                    }
                }
                return true;
            }
        });

        complete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder=new AlertDialog.Builder(Travel_RecordActivity.this);
                builder.setTitle("确定保存吗？");
                builder.setMessage("保存后不能再修改。");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //保存数据
                        String travel_title=title.getText().toString();
                        if (travel_title.length()==0){
                            travel_title="无题";
                        }
                        String travel_text=text.getText().toString();
                        String travelId=query_TravelRecordId();
                        int travel_id_1=Integer.parseInt(travelId);
                        Log.d("Travel_RecordActivity",String.valueOf(travel_id_1));
                        save_to_file(travelId,travel_text);

                        TravelRecord travelRecord=new TravelRecord();
                        travelRecord.setTime(getTime());
                        travelRecord.setTravelRecord_id(travel_id_1);
                        travelRecord.setTitle(travel_title);
                        travelRecord.save();
                        setPhoto(travel_id_1);
                        Intent intent=new Intent(Travel_RecordActivity.this,MyActivity.class);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton("再看看", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //继续编辑数据
                    }
                });
                builder.show();
            }
        });
    }

    private String query_TravelRecordId(){
        String id;
        List<TravelRecord> travelRecords= LitePal.findAll(TravelRecord.class);
        if (travelRecords.isEmpty()){
            id="1";
            return id;
        }else{
            int idnum=LitePal.max(TravelRecord.class,"travelRecord_id",int.class)+1;
            id=String.valueOf(idnum);
            return id;
        }
    }

    public void deletePhoto(){
        handler=new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg){
                Bundle data=msg.getData();
                switch (msg.what){
                    case 1:
                        int delIndex=data.getInt("delIndex");
                        final int delIndex_1=delIndex;
                        photoList.remove(delIndex_1);
                        isShowDelete.remove(delIndex_1);
                        adapter.notifyDataSetChanged();
                        adapter=new GridViewAdapter(Travel_RecordActivity.this, handler,photoList,isShowDelete);
                        mGridView.setAdapter(adapter);
                }
            }
        };
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
                    imageUri= FileProvider.getUriForFile(Travel_RecordActivity.this,"com.example.diplomadesign.fileprovider",outputImage);
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
                if(ContextCompat.checkSelfPermission(Travel_RecordActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(Travel_RecordActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
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
        super.onActivityResult(requestCode,resultCode,data);
        switch (requestCode){
            case TAKE_PHOTO:
                if (resultCode==RESULT_OK){
                    try {
                        //将拍摄的照片显示出来
                        Bitmap bitmap= BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        photoList.add(bitmap);
                        isShowDelete.add(noShowDelete);
                        adapter.notifyDataSetChanged();
                        adapter=new GridViewAdapter(Travel_RecordActivity.this, handler,photoList,isShowDelete);
                        mGridView.setAdapter(adapter);
//                        Account account=new Account();
//                        account.setHeadshot(img(bitmap));
//                        account.updateAll("telephone=?",my_telephone);
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if(resultCode==RESULT_OK && data!=null){
                    Uri uri=data.getData();
                    try {
                        Bitmap bitmap=getBitmapFromUri(uri);
                        photoList.add(bitmap);
                        isShowDelete.add(noShowDelete);
//                        Toast.makeText(this,"hello",Toast.LENGTH_LONG).show();
                        adapter.notifyDataSetChanged();
                        adapter=new GridViewAdapter(Travel_RecordActivity.this, handler,photoList,isShowDelete);
                        mGridView.setAdapter(adapter);
                    } catch (FileNotFoundException e) {
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isHide==true){
            dialog.dismiss();
        }
        if (isHide_1==true){
            dialog_1.dismiss();
        }
    }
    //将Bitmap图片转化为字节形式
    private byte[]img(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100, baos);
        return baos.toByteArray();
    }

    public String getTime(){//获取当前时间
        Calendar calendar=Calendar.getInstance();
        int year=calendar.get(Calendar.YEAR);
        int month=calendar.get(Calendar.MONTH)+1;
        int day=calendar.get(Calendar.DAY_OF_MONTH);
        String time=year+"."+month+"."+day;
        return time;
    }

    private void save_to_file(String num,String intputText){
        FileOutputStream outputStream=null;
        BufferedWriter writer=null;
        try {
            outputStream=openFileOutput(num, Context.MODE_PRIVATE);
            writer=new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(intputText);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                if (writer!=null){
                    writer.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    private void setPhoto(int id){
        TravelPhotos travelPhotos=new TravelPhotos();
        travelPhotos.setTravelPhotos_id(id);
        int i=0;
        int size=photoList.size();
        travelPhotos.setPhoto_size(size);
        while(i<size){
            Bitmap bitmap=photoList.get(i);
            switch (i){
                case 0:
                    travelPhotos.setImg0(img(bitmap));
                    break;
                case 1:
                    travelPhotos.setImg1(img(bitmap));
                    break;
                case 2:
                    travelPhotos.setImg2(img(bitmap));
                    break;
                case 3:
                    travelPhotos.setImg3(img(bitmap));
                    break;
                case 4:
                    travelPhotos.setImg4(img(bitmap));
                    break;
                case 5:
                    travelPhotos.setImg5(img(bitmap));
                    break;
                case 6:
                    travelPhotos.setImg6(img(bitmap));
                    break;
                case 7:
                    travelPhotos.setImg7(img(bitmap));
                    break;
                case 8:
                    travelPhotos.setImg8(img(bitmap));
                    break;
            }
            i++;
        }
        travelPhotos.save();
    }
}
