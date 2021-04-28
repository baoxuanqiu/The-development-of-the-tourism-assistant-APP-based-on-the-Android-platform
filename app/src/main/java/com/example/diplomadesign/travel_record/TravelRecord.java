package com.example.diplomadesign.travel_record;

import android.graphics.Bitmap;

import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.List;

public class TravelRecord extends LitePalSupport {
    private int travelRecord_id;
    private String title;
    private String time;

    public void setTravelRecord_id(int id) {
        this.travelRecord_id = id;
    }
    public int getTravelRecord_id() {
        return travelRecord_id;
    }
    public void setTime(String time) {
        this.time = time;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }
    public TravelRecord(){
        super();
    }

    public TravelRecord(int id, String title, String time){
        super();
        this.travelRecord_id=id;
        this.title=title;
        this.time=time;
    }
}
