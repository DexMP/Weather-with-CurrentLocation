package xyz.rpka.Weather;

import java.util.ArrayList;

class WearherData {
    Coord coord;
    ArrayList<GetWeather> weather = new ArrayList <GetWeather> ();
    private String base;
    Main main;
    Wind wind;
    Clouds clouds;
    private float dt;
    Sys sys;
    private float id;
    private String name;
    private float cod;

    public Coord getCoord() {
        return coord;
    }

    public ArrayList<GetWeather> getWeather() {
        return weather;
    }

    public String getBase() {
        return base;
    }

    public Main getMain() {
        return main;
    }

    public Wind getWind() {
        return wind;
    }

    public Clouds getClouds() {
        return clouds;
    }

    public float getDt() {
        return dt;
    }

    public Sys getSys() {
        return sys;
    }

    public float getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getCod() {
        return cod;
    }
}
