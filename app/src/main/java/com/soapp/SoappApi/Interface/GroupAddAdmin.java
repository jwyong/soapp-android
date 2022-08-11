package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.AddRoomAdminModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/* Created by chang on 29/08/2017. */
/* 20171005 - OK */

public interface GroupAddAdmin {
    @POST("api/v1.4/room/user/update")
    Call<AddRoomAdminModel> addAdminApi(@Body AddRoomAdminModel user, @Header("Authorization") String access_token);
}
