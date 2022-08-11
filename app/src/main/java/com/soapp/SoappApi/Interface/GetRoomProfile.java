package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.GetRoomRepo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

/* Created by chang on 16/08/2017. */
/* 20171005 - OK */

public interface GetRoomProfile {
    @GET("api/v1.3/room/{room}/{size}")
    Call<GetRoomRepo> getRoomProfile(@Path("room") String id, @Path("size") String size, @Header("Authorization") String access_token);
}
