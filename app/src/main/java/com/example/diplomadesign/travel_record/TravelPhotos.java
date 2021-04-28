package com.example.diplomadesign.travel_record;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.example.diplomadesign.R;

import org.litepal.crud.LitePalSupport;

import java.io.ByteArrayOutputStream;

public class TravelPhotos extends LitePalSupport {
    private int travelPhotos_id;
    private int photo_size;
    private byte[] img0;
    private byte[] img1;
    private byte[] img2;
    private byte[] img3;
    private byte[] img4;
    private byte[] img5;
    private byte[] img6;
    private byte[] img7;
    private byte[] img8;
    public int getTravelPhotos_id() {
        return travelPhotos_id;
    }

    public void setTravelPhotos_id(int id) {
        this.travelPhotos_id = id;
    }

    public int getPhoto_size() {
        return photo_size;
    }

    public void setPhoto_size(int photo_size) {
        this.photo_size = photo_size;
    }

    public byte[] getImg0() {
        return img0;
    }

    public byte[] getImg1() {
        return img1;
    }

    public byte[] getImg2() {
        return img2;
    }

    public byte[] getImg3() {
        return img3;
    }

    public byte[] getImg4() {
        return img4;
    }

    public byte[] getImg5() {
        return img5;
    }

    public byte[] getImg6() {
        return img6;
    }

    public byte[] getImg7() {
        return img7;
    }

    public byte[] getImg8() {
        return img8;
    }

    public void setImg0(byte[] img0) {
        this.img0 = img0;
    }

    public void setImg1(byte[] img1) {
        this.img1 = img1;
    }

    public void setImg2(byte[] img2) {
        this.img2 = img2;
    }

    public void setImg3(byte[] img3) {
        this.img3 = img3;
    }

    public void setImg4(byte[] img4) {
        this.img4 = img4;
    }

    public void setImg5(byte[] img5) {
        this.img5 = img5;
    }

    public void setImg6(byte[] img6) {
        this.img6 = img6;
    }

    public void setImg7(byte[] img7) {
        this.img7 = img7;
    }

    public void setImg8(byte[] img8) {
        this.img8 = img8;
    }

    //将Bitmap图片转化为字节形式
    private byte[]img(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100, baos);
        return baos.toByteArray();
    }
}
