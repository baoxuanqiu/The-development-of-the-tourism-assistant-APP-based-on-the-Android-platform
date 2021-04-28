package com.example.diplomadesign.account;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;

import org.litepal.LitePal;

import java.util.List;

public class GetAccountInfo extends BroadcastReceiver {
    public static String account_tele="";
    public static String account_name;
    public static Bitmap account_image;
    public static Boolean isLogin=false;
    @Override
    public void onReceive(Context context, Intent intent) {
        this.account_tele=intent.getStringExtra("AccountTele");//收到广播传过来的用户手机号码
        isLogin=true;
        Log.d("GetAccountInfo",account_tele);
    }

    public Boolean getIsLogin() {
        List<Account> accounts=LitePal.findAll(Account.class);
        if (!accounts.isEmpty()){
            this.isLogin=true;
        }
        return isLogin;
    }

    public String getAccount_tele() {
        String tele=this.account_tele;
        if (tele.length()==0){
            List<Account> accounts=LitePal.findAll(Account.class);
            if (!accounts.isEmpty()){
                Gson gson=new Gson();
                String real_tele=gson.toJson(accounts.get(0));
                JSONObject jsonObject=JSONObject.parseObject(real_tele);
                tele=jsonObject.getString("telephone");
                this.account_tele=tele;
            }
        }
        return account_tele;
    }

    public String getAccount_name() {
        List<Account> account= LitePal.select("name")
                .where("telephone like ?",account_tele)
                .find(Account.class);
        Gson gson=new Gson();
        String real_name=gson.toJson(account.get(0));//将list集合转化为json字符串
        JSONObject jsonObj = JSONObject.parseObject(real_name);
        this.account_name=jsonObj.getString("name");//json字符串取name的值
        return account_name;
    }

    public Bitmap getAccount_image() {
        List<Account> account1= LitePal.select("headshot")
                .where("telephone like ?",account_tele)
                .find(Account.class);
        byte[] images=account1.get(0).getHeadshot();
        Bitmap bitmap= BitmapFactory.decodeByteArray(images, 0, images.length);
//        Bitmap bitmap_1=setImgSize(bitmap,500);
        this.account_image=bitmap;
        Log.d("getAccountInfo",String.valueOf(bitmap.getWidth()));
        return account_image;
    }

//    private Bitmap compressImg(Bitmap bitmap) {
//        //Bitmap too large to be uploaded into a texture
//
//        if (bitmap.getWidth() <= screenWidth) {
//            return bitmap;
//        } else {
//            Bitmap bmp = Bitmap.createScaledBitmap(bitmap, screenWidth, bitmap.getHeight() * screenWidth / bitmap.getWidth(), true);
//            return bmp;
//        }
//    }

//    //宽高同比例缩放 scale
//    public Bitmap setImgSize(Bitmap bm, int scale ){
//        // 获得图片的宽高.
//        int width = bm.getWidth();
//        int height = bm.getHeight();
//        // 计算缩放比例.
//        float k = ((float) scale) / width;
//        // 取得想要缩放的matrix参数.
//        Matrix matrix = new Matrix();
//        matrix.postScale(k, k);
//        // 得到新的图片.
//        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
//        return newbm;
//    }
}
