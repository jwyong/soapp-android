package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.RestaurantBookingInfo;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/* Created by chang on 21/09/2017. */
/* 20171005 - OK */

public interface RestaurantCancel {
    @POST("api/v1.1/restaurant/booking/cancel")
    Call<RestaurantBookingInfo> resBook(@Body RestaurantBookingInfo resBookingInfo, @Header("Authorization")
            String access_token);
}
