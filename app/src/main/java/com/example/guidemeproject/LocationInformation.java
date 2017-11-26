package com.example.guidemeproject;

/**
 * Created by Sumarliyanti on 21/10/2017.
 */

public class LocationInformation {
    public double longitude;
    public double latitude;
    public String description;
    public String location;

    public LocationInformation(){

    }

    public LocationInformation(double longitude, double latitude, String description, String location) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.description = description;
        this.location = location;
    }

    public double getLocationLong() {
        return longitude;
    }

    public double getLocationLat() {
        return latitude;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation(){
        return location;
    }

}
