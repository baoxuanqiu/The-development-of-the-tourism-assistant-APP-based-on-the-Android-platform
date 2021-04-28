package com.example.diplomadesign.show_route;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diplomadesign.R;

import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {
    private List<Route> mRouteList;
    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView route;
        public ViewHolder(View view){
            super(view);
            route=(TextView)view.findViewById(R.id.route_text);
        }
    }

    public RouteAdapter(List<Route> routes){
        mRouteList=routes;
    }
    @NonNull
    @Override
    public RouteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.route_item,parent,false);
        ViewHolder holder=new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RouteAdapter.ViewHolder holder, int position) {
        Route route=mRouteList.get(position);
        holder.route.setText(route.getRoute());
    }

    @Override
    public int getItemCount() {
        return mRouteList.size();
    }
}
