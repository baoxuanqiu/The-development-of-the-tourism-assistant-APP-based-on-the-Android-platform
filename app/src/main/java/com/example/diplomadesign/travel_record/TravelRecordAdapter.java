package com.example.diplomadesign.travel_record;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diplomadesign.MyActivity;
import com.example.diplomadesign.R;
import com.example.diplomadesign.ShowTravelRecordActivity;
import com.example.diplomadesign.Travel_RecordActivity;

import org.litepal.LitePal;

import java.io.File;
import java.util.List;

public class TravelRecordAdapter extends RecyclerView.Adapter<TravelRecordAdapter.ViewHolder> {
    private List<TravelRecordItem> mTravelRecordItemList;

    public TravelRecordAdapter(List<TravelRecordItem> travelRecordItems) {
        mTravelRecordItemList = travelRecordItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.travel_record_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.travelRecordItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                TravelRecordItem travelRecordItem = mTravelRecordItemList.get(position);
                Intent intent = new Intent(view.getContext(), ShowTravelRecordActivity.class);
                intent.putExtra("travel_record_id", travelRecordItem.getTravel_id());
                view.getContext().startActivity(intent);
            }
        });
        holder.travelRecordItemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                final AlertDialog.Builder builder=new AlertDialog.Builder(view.getContext());
                builder.setTitle("删除");
                builder.setMessage("确定删除吗？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int position = holder.getAdapterPosition();
                        TravelRecordItem travelRecordItem = mTravelRecordItemList.get(position);
                        String num=travelRecordItem.getTravel_id();
                        LitePal.deleteAll(TravelRecord.class,"travelRecord_id=?",num);
                        LitePal.deleteAll(TravelPhotos.class,"travelPhotos_id=?",num);
                        mTravelRecordItemList.remove(position);
                        notifyDataSetChanged();
//                        Log.d("MyActivity",num+"is exist.......");
                        File file=new File("./data/data/com.example.diplomadesign/files/"+num);//文件的地址
                        if (file.exists()){
                            Log.d("MyActivity",num+"is exist.");
                            file.delete();
                        }
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();
                return true;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TravelRecordItem travelRecordItem = mTravelRecordItemList.get(position);
        holder.title.setText(travelRecordItem.getTravel_title());
        holder.time.setText(travelRecordItem.getTravel_time());
        holder.id.setText(travelRecordItem.getTravel_id());
    }

    @Override
    public int getItemCount() {
        return mTravelRecordItemList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View travelRecordItemView;
        TextView title;
        TextView time;
        TextView id;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            travelRecordItemView = itemView;
            title = (TextView) itemView.findViewById(R.id.travel_record_title);
            time = (TextView) itemView.findViewById(R.id.travel_record_time);
            id = (TextView) itemView.findViewById(R.id.travel_record_id);
        }
    }


}