package com.soapp.SoappApi.ApiModel;

/* Created by chang on 19/09/2017. */

public class SearchNearbyRestaurant {

    //test
    private String message;

    //Post materials
    private String user_lat;
    private String user_lon;
    private String row;
    private String query1;
    private String query2;
    private String query3;
    private String type1;
    private String type2;
    private String type3;

    //Response materials
    private String propic, Latitude, Longitude, Name, Location, State, rating, owner_jid, biz_id,
            MainCuisine, video;

    public SearchNearbyRestaurant(String user_lat, String user_lon, String row, String query1, String query2, String query3, String type1, String type2, String type3) {
        this.user_lat = user_lat;
        this.user_lon = user_lon;
        this.row = row;
        this.query1 = query1;
        this.query2 = query2;
        this.query3 = query3;
        this.type1 = type1;
        this.type2 = type2;
        this.type3 = type3;
    }

    public String getUser_lat() {
        return user_lat;
    }

    public void setUser_lat(String user_lat) {
        this.user_lat = user_lat;
    }

    public String getUser_lon() {
        return user_lon;
    }

    public void setUser_lon(String user_lon) {
        this.user_lon = user_lon;
    }

    public String getRow() {
        return row;
    }

    public void setRow(String row) {
        this.row = row;
    }

    public String getQuery1() {
        return query1;
    }

    public void setQuery1(String query1) {
        this.query1 = query1;
    }

    public String getQuery2() {
        return query2;
    }

    public void setQuery2(String query2) {
        this.query2 = query2;
    }

    public String getQuery3() {
        return query3;
    }

    public void setQuery3(String query3) {
        this.query3 = query3;
    }

    public String getType1() {
        return type1;
    }

    public void setType1(String type1) {
        this.type1 = type1;
    }

    public String getType2() {
        return type2;
    }

    public void setType2(String type2) {
        this.type2 = type2;
    }

    public String getType3() {
        return type3;
    }

    public void setType3(String type3) {
        this.type3 = type3;
    }

    public String getPropic() {
        return propic;
    }

    public void setPropic(String propic) {
        this.propic = propic;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getOwner_jid() {
        return owner_jid;
    }

    public void setOwner_jid(String owner_jid) {
        this.owner_jid = owner_jid;
    }

    public String getBiz_id() {
        return biz_id;
    }

    public void setBiz_id(String biz_id) {
        this.biz_id = biz_id;
    }

    public String getMainCuisine() {
        return MainCuisine;
    }

    public void setMainCuisine(String mainCuisine) {
        MainCuisine = mainCuisine;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getMessage() {
        return message;
    }
}
