package com.soapp.SoappModel;

/**
 * Created by chang on 29/09/2017.
 */

public class TrackingModel {

    private String username;
    private String latitude;
    private String longitude;

    public TrackingModel(String username, String latitude, String longitude) {
        this.username = username;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
}
