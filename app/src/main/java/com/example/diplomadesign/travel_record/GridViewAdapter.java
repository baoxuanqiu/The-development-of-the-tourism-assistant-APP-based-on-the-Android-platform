package com.example.diplomadesign.travel_record;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.diplomadesign.R;

import java.util.ArrayList;

public class GridViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Bitmap> photoList;
    private ArrayList<String> isShowDelete;
//    private ArrayList<Map<String,Object>> list;
    LayoutInflater layoutInflater;
    private String isShowDel="";// 根据这个变量来判断是否显示删除图标，true是显示，false是不显示
    private Handler handler;

    public GridViewAdapter(Context context, Handler handler, ArrayList<Bitmap> photoList, ArrayList<String> isShowDelete) {
        this.context = context;
//        this.list = list;
        this.photoList=photoList;
        this.isShowDelete=isShowDelete;
        this.handler=handler;
        layoutInflater = LayoutInflater.from(context);
    }

//    public void setIsShowDelete(boolean isShowDelete) {
//        this.isShowDelete = isShowDelete;
//        notifyDataSetChanged();
//    }

    @Override
    public int getCount() {
        if (photoList.size()<9){
            return photoList.size()+1;//注意此处
        }else {
            return photoList.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return photoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView==null){
            holder=new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.grid_item, null);
            holder.photoimage=(ImageView)convertView.findViewById(R.id.item);
            holder.deleteimage=(ImageView)convertView.findViewById(R.id.delete_markView);
            convertView.setTag(holder);
        }else{
            holder=(ViewHolder)convertView.getTag();
        }
        if (photoList.size()<9){         //图片数量小于等于8
            if (position<photoList.size()){  //在加号图片之前的所有图片
//            byte[] photoId = (byte[])list.get(position).get("photoImage");
//            Bitmap contactPhoto = BitmapFactory.decodeByteArray(photoId, 0, photoId.length);;
//            holder.photoimage.setImageBitmap(contactPhoto);
                holder.photoimage.setImageBitmap(photoList.get(position));//将图片显示在对应的框里
                isShowDel=isShowDelete.get(position);
                if (isShowDel.equals("true")){
                    holder.deleteimage.setVisibility(View.VISIBLE);  //显示删除标记
                }else {
                    holder.deleteimage.setVisibility(View.GONE);//隐藏删除标记
                }
            }else {
                holder.photoimage.setBackgroundResource(R.drawable.add_1);//最后一个显示加号图片
                holder.deleteimage.setVisibility(View.GONE);//加号图片不显示删除标记
            }
        }else {      //图片数量等于9
            holder.photoimage.setImageBitmap(photoList.get(position));
            isShowDel=isShowDelete.get(position);
            if (isShowDel.equals("true")){
                holder.deleteimage.setVisibility(View.VISIBLE);
            }else {
                holder.deleteimage.setVisibility(View.GONE);
            }
        }

        holder.deleteimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg=new Message();
                Bundle data=new Bundle();
                data.putInt("delIndex",position);//设置删除索引
                msg.setData(data);
                msg.what=1;
                handler.sendMessage(msg);
            }
        });

//        holder.deleteimage.setVisibility(isShowDelete ? View.VISIBLE : View.GONE);// 设置删除按钮是否显示
//        convertView = layoutInflater.inflate(R.layout.grid_item, null);
//        mImageView = (ImageView) convertView.findViewById(R.id.item);
//        deleteView=convertView.findViewById(R.id.delete_markView);
//        deleteView.setVisibility(isShowDelete ? View.VISIBLE : View.GONE);// 设置删除按钮是否显示
//        if (position < list.size()) {
//            mImageView.setImageBitmap(list.get(position));
//        }else{
//            mImageView.setBackgroundResource(R.drawable.pic3);//最后一个显示加号图片
//        }
        return convertView;
    }

}

class ViewHolder {
    ImageView deleteimage;
    ImageView photoimage;
}
