package com.example.diplomadesign.travel_record;

public class TravelRecordItem {
    private String travel_title;
    private String travel_time;
    private String travel_id;

    public TravelRecordItem(String title,String time,String id){
        this.travel_title=title;
        this.travel_time=time;
        this.travel_id=id;
    }

    public String getTravel_time() {
        return travel_time;
    }

    public String getTravel_title() {
        return travel_title;
    }

    public String getTravel_id() {
        return travel_id;
    }
}
