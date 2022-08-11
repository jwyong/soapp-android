package com.soapp.SoappApi.ApiModel;

/* Created by chang on 21/09/2017. */

import java.util.List;

public class RestaurantInfo {

    private String restaurant_id;

    private String propic, Mon1, Tue1, Wed1, Thu1, Fri1, Sat1, Sun1, Latitude, Longitude, Price,
            i1URL, i2URL, i3URL, i4URL, i5URL, i6URL, i7URL, i8URL;
    private String MainCuisine, Cuisine2, Cuisine3, Address, Mall;
    private String Phone1, Description, rating, halal, alcohol, smoking, wifi, video;
    private int Booking;
    private List<ResDetPromotionModel> promotion;

    //added 20180212 for incoming share booking query
    private String Location, State;

    public RestaurantInfo(String restaurant_id) {
        this.restaurant_id = restaurant_id;
    }

    public String getPropic() {
        return propic;
    }

    public void setPropic(String propic) {
        this.propic = propic;
    }

    public String getMon1() {
        return Mon1;
    }

    public void setMon1(String mon1) {
        Mon1 = mon1;
    }

    public String getTue1() {
        return Tue1;
    }

    public void setTue1(String tue1) {
        Tue1 = tue1;
    }

    public String getWed1() {
        return Wed1;
    }

    public void setWed1(String wed1) {
        Wed1 = wed1;
    }

    public String getThu1() {
        return Thu1;
    }

    public void setThu1(String thu1) {
        Thu1 = thu1;
    }

    public String getFri1() {
        return Fri1;
    }

    public void setFri1(String fri1) {
        Fri1 = fri1;
    }

    public String getSat1() {
        return Sat1;
    }

    public void setSat1(String sat1) {
        Sat1 = sat1;
    }

    public String getSun1() {
        return Sun1;
    }

    public void setSun1(String sun1) {
        Sun1 = sun1;
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

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public String getI1URL() {
        return i1URL;
    }

    public void setI1URL(String i1URL) {
        this.i1URL = i1URL;
    }

    public String getI2URL() {
        return i2URL;
    }

    public void setI2URL(String i2URL) {
        this.i2URL = i2URL;
    }

    public String getI3URL() {
        return i3URL;
    }

    public void setI3URL(String i3URL) {
        this.i3URL = i3URL;
    }

    public String getI4URL() {
        return i4URL;
    }

    public void setI4URL(String i4URL) {
        this.i4URL = i4URL;
    }

    public String getI5URL() {
        return i5URL;
    }

    public void setI5URL(String i5URL) {
        this.i5URL = i5URL;
    }

    public String getI6URL() {
        return i6URL;
    }

    public void setI6URL(String i6URL) {
        this.i6URL = i6URL;
    }

    public String getI7URL() {
        return i7URL;
    }

    public void setI7URL(String i7URL) {
        this.i7URL = i7URL;
    }

    public String getI8URL() {
        return i8URL;
    }

    public void setI8URL(String i8URL) {
        this.i8URL = i8URL;
    }

    public String getPhone1() {
        return Phone1;
    }

    public void setPhone1(String phone1) {
        Phone1 = phone1;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getHalal() {
        return halal;
    }

    public void setHalal(String halal) {
        this.halal = halal;
    }

    public String getAlcohol() {
        return alcohol;
    }

    public void setAlcohol(String alcohol) {
        this.alcohol = alcohol;
    }

    public String getSmoking() {
        return smoking;
    }

    public void setSmoking(String smoking) {
        this.smoking = smoking;
    }

    public String getWifi() {
        return wifi;
    }

    public void setWifi(String wifi) {
        this.wifi = wifi;
    }

    public String getRestaurant_id() {
        return restaurant_id;
    }

    public void setRestaurant_id(String restaurant_id) {
        this.restaurant_id = restaurant_id;
    }

    public String getMainCuisine() {
        return MainCuisine;
    }

    public void setMainCuisine(String mainCuisine) {
        MainCuisine = mainCuisine;
    }

    public String getCuisine2() {
        return Cuisine2;
    }

    public void setCuisine2(String cuisine2) {
        Cuisine2 = cuisine2;
    }

    public String getCuisine3() {
        return Cuisine3;
    }

    public void setCuisine3(String cuisine3) {
        Cuisine3 = cuisine3;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getMall() {
        return Mall;
    }

    public void setMall(String mall) {
        Mall = mall;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public List<ResDetPromotionModel> getPromotion() {
        return promotion;
    }

    public void setPromotion(List<ResDetPromotionModel> promotion) {
        this.promotion = promotion;
    }

    public int getBooking() {
        return Booking;
    }

    public void setBooking(int booking) {
        Booking = booking;
    }
}
