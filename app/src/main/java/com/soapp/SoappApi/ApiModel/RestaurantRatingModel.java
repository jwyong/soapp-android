package com.soapp.SoappApi.ApiModel;

/* Created by Soapp on 17/11/2017. */

public class RestaurantRatingModel {

    private String res_id;
    private int rating_delicacy;
    private int rating_environment;
    private String message;


    public RestaurantRatingModel(String res_id, int rating_delicacy, int rating_environment) {
        this.res_id = res_id;
        this.rating_delicacy = rating_delicacy;
        this.rating_environment = rating_environment;
    }

    public String getRes_id() {
        return res_id;
    }

    public void setRes_id(String res_id) {
        this.res_id = res_id;
    }

    public int getRating_delicacy() {
        return rating_delicacy;
    }

    public void setRating_delicacy(int rating_delicacy) {
        this.rating_delicacy = rating_delicacy;
    }

    public int getRating_environment() {
        return rating_environment;
    }

    public void setRating_environment(int rating_environment) {
        this.rating_environment = rating_environment;
    }

    public String getMessage() {
        return message;
    }
}
