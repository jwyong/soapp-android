package com.soapp.sql.room.entity;

import android.content.ContentValues;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;


/* Created by ibrahim on 25/03/2018. */

@Entity(tableName = "RESTAURANT")
public class Restaurant {

    @PrimaryKey
    @NonNull
    private String ResID;
    private String ResTitle;
    private String ResLocation;
    private String ResState;
    private String ResMainCuisine;
    private String ResLatitude;
    private String ResLongitude;
    private String ResImageUrl;
    private String ResPhoneNum;
    private Integer ResOverallRating = 0;
    private Integer ResFavourited = 0;


    public Restaurant() {
    }

    @Ignore
    public Restaurant(String resID, String resImageUrl, String resLocation, String resState, String resLatitude, String resLongitude, String resTitle, String resPhoneNum, Integer resRating1) {
        ResID = resID;
        ResImageUrl = resImageUrl;
        ResLocation = resLocation;
        ResState = resState;
        ResLatitude = resLatitude;
        ResLongitude = resLongitude;
        ResTitle = resTitle;
        ResPhoneNum = resPhoneNum;
        ResOverallRating = resRating1;
    }

    @Ignore
    public Restaurant(String resID, String resTitle, String resLocation, String resState, String resMainCuisine, String resLatitude, String resLongitude, String resImageUrl, String resPhoneNum, Integer resOverallRating, Integer resFavourited) {
        ResID = resID;
        ResTitle = resTitle;
        ResLocation = resLocation;
        ResState = resState;
        ResMainCuisine = resMainCuisine;
        ResLatitude = resLatitude;
        ResLongitude = resLongitude;
        ResImageUrl = resImageUrl;
        ResPhoneNum = resPhoneNum;
        ResOverallRating = resOverallRating;
        ResFavourited = resFavourited;
    }

    public static Restaurant fromContentValues(ContentValues values) {

        final Restaurant restaurant = new Restaurant();

        if (values.containsKey("ResID")) {
            restaurant.ResID = values.getAsString("ResID");
        }
        if (values.containsKey("ResImageUrl")) {
            restaurant.ResImageUrl = values.getAsString("ResImageUrl");
        }
        if (values.containsKey("ResLocation")) {
            restaurant.ResLocation = values.getAsString("ResLocation");
        }
        if (values.containsKey("ResState")) {
            restaurant.ResState = values.getAsString("ResState");
        }
        if (values.containsKey("ResTitle")) {
            restaurant.ResTitle = values.getAsString("ResTitle");
        }
        if (values.containsKey("ResOverallRating")) {
            restaurant.ResOverallRating = values.getAsInteger("ResOverallRating");
        }
        if (values.containsKey("ResMainCuisine")) {
            restaurant.ResMainCuisine = values.getAsString("ResMainCuisine");
        }
        if (values.containsKey("ResLatitude")) {
            restaurant.ResLatitude = values.getAsString("ResLatitude");
        }
        if (values.containsKey("ResLongitude")) {
            restaurant.ResLongitude = values.getAsString("ResLongitude");
        }
        if (values.containsKey("ResPhoneNum")) {
            restaurant.ResPhoneNum = values.getAsString("ResPhoneNum");
        }
        if (values.containsKey("ResFavourited")) {
            restaurant.ResFavourited = values.getAsInteger("ResFavourited");
        }

        return restaurant;
    }

    public String getResID() {
        return ResID;
    }

    public void setResID(String resID) {
        ResID = resID;
    }

    public String getResImageUrl() {
        return ResImageUrl;
    }

    public void setResImageUrl(String resImageUrl) {
        ResImageUrl = resImageUrl;
    }

    public String getResLocation() {
        return ResLocation;
    }

    public void setResLocation(String resLocation) {
        ResLocation = resLocation;
    }

    public String getResState() {
        return ResState;
    }

    public void setResState(String resState) {
        ResState = resState;
    }

    public String getResLatitude() {
        return ResLatitude;
    }

    public void setResLatitude(String resLatitude) {
        ResLatitude = resLatitude;
    }

    public String getResLongitude() {
        return ResLongitude;
    }

    public void setResLongitude(String resLongitude) {
        ResLongitude = resLongitude;
    }

    public String getResTitle() {
        return ResTitle;
    }

    public void setResTitle(String resTitle) {
        ResTitle = resTitle;
    }

    public String getResPhoneNum() {
        return ResPhoneNum;
    }

    public void setResPhoneNum(String resPhoneNum) {
        ResPhoneNum = resPhoneNum;
    }

    public Integer getResOverallRating() {
        return ResOverallRating;
    }

    public void setResOverallRating(Integer resOverallRating) {
        ResOverallRating = resOverallRating;
    }

    public String getResMainCuisine() {
        return ResMainCuisine;
    }

    public void setResMainCuisine(String resMainCuisine) {
        ResMainCuisine = resMainCuisine;
    }

    public Integer getResFavourited() {
        return ResFavourited;
    }

    public void setResFavourited(Integer resFavourited) {
        ResFavourited = resFavourited;
    }

}
