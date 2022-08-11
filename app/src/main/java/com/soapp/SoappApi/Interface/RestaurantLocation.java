package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.RestaurantLocModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

/* Created by Soapp on 17/11/2017. */

public interface RestaurantLocation {
    @GET("api/v1/restaurant/location/{res_id}")
    Call<RestaurantLocModel> resLocation(@Path("res_id") String resID, @Header("Authorization")
            String access_token);
}
