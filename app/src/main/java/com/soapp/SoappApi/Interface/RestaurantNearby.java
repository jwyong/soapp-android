package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.SearchNearbyRestaurant;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/* Created by chang on 19/09/2017. */
/* 20171005 - OK */

public interface RestaurantNearby {
    @POST("api/v1.2/restaurant/search")
    Call<List<SearchNearbyRestaurant>> nearbyRes(@Body SearchNearbyRestaurant res, @Header
            ("Authorization") String access_token);
}