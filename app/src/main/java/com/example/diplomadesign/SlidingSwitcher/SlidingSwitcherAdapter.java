package com.example.diplomadesign.SlidingSwitcher;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diplomadesign.R;
import com.example.diplomadesign.Travel_RecordActivity;

import java.util.ArrayList;
import java.util.List;

public class SlidingSwitcherAdapter extends RecyclerView.Adapter<SlidingSwitcherAdapter.ViewHolder> {
    private ArrayList<Bitmap> photos;
    private Dialog dialog;
    private View inflate;
    private ImageView show_img;

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView picture;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            picture=(ImageView)itemView.findViewById(R.id.photo_img);
        }
    }

    public SlidingSwitcherAdapter(ArrayList<Bitmap> pictures){
        photos=pictures;
    }

    @NonNull
    @Override
    public SlidingSwitcherAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.sliding_switcher_photo,parent,false);
        final ViewHolder holder=new ViewHolder(view);
        holder.picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position=holder.getAdapterPosition();
                dialog = new Dialog(view.getContext(),R.style.DialogTheme);
                //填充对话框的布局
                inflate = LayoutInflater.from(view.getContext()).inflate(R.layout.center_show_photo, null);
                show_img=(ImageView)inflate.findViewById(R.id.show_photo);
                show_img.setImageBitmap(photos.get(position));
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.setContentView(inflate);//将布局设置给Dialog
                dialog.show();
                show_img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull SlidingSwitcherAdapter.ViewHolder holder, int position) {
        holder.picture.setImageBitmap(photos.get(position));
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

}
