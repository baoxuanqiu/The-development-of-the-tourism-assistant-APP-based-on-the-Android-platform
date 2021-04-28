package com.example.diplomadesign.account;

import org.litepal.crud.LitePalSupport;

public class LocationInfo extends LitePalSupport {
    private int id;
    private String name;
    private String city;
    private String diname;
    private String latitude;//纬度
    private String longitude;//经度
    private String cityID;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDiname() {
        return diname;
    }

    public void setDiname(String diname) {
        this.diname = diname;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getCityID() {
        return cityID;
    }

    public void setCityID(String cityID) {
        this.cityID = cityID;
    }

    public LocationInfo(){super();}
    public LocationInfo(int id,String name,String city,String diname,String latitude,String longitude,String cityID){
        this.id=id;
        this.name=name;
        this.city=city;
        this.diname=diname;
        this.latitude=latitude;
        this.longitude=longitude;
        this.cityID=cityID;
    }
}
