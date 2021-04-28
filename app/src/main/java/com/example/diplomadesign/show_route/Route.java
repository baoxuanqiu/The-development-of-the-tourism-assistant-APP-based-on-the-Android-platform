package com.example.diplomadesign.show_route;

public class Route {
    private String route;
    private int img;

    public int getImg() {
        return img;
    }

    public String getRoute() {
        return route;
    }

    public void setImg(int img) {
        this.img = img;
    }

    public void setRoute(String route) {
        this.route = route;
    }
    public Route(String route){
        this.route=route;
    }
}
