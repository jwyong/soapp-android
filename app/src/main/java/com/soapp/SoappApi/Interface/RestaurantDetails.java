package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.RestaurantInfo;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/* Created by chang on 21/09/2017. */
/* 20171005 - OK */

public interface RestaurantDetails {
    @POST("api/v1.2/restaurant/details")
    Call<RestaurantInfo> nearbyRes(@Body RestaurantInfo res, @Header("Authorization") String
            access_token);
}
