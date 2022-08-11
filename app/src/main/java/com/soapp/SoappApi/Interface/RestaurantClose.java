package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.ResReportClosed;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by rlwt on 2/27/18.
 */

public interface RestaurantClose {
    @POST("api/v1.2/restaurant/report_closed")
    Call<ResReportClosed> closeRes(@Body String resId, @Header
            ("Authorization") String access_token);
}
