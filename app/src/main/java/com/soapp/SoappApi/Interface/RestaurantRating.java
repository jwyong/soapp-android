package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.RestaurantRatingModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/* Created by Soapp on 21/11/2017. */

public interface RestaurantRating {
    @POST("api/v1/restaurant/review/create_or_edit_review")
    Call<RestaurantRatingModel> resRating(@Body RestaurantRatingModel resRatingModel, @Header("Authorization") String access_token);
}
