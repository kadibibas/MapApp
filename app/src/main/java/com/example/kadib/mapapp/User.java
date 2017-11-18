package com.example.kadib.mapapp;

/**
 * Created by kadibibas on 16.11.2017.
 */

public class User {
    String Device_token;
    String Visibility;
    String Lat;
    String Lng;

    public User() {
    }

    public String getDevice_token() {
        return Device_token;
    }

    public void setDevice_token(String device_token) {
        Device_token = device_token;
    }

    public String getVisibility() {
        return Visibility;
    }

    public void setVisibility(String visibility) {
        Visibility = visibility;
    }

    public String getLat() {
        return Lat;
    }

    public void setLat(String lat) {
        Lat = lat;
    }

    public String getLng() {
        return Lng;
    }

    public void setLng(String lng) {
        Lng = lng;
    }
}


