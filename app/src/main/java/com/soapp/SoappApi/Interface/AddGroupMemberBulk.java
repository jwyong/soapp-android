package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.AddRoomMemberBulkModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/* Created by chang on 29/09/2017. */
/* 20171005 - OK */

public interface AddGroupMemberBulk {
    @POST("api/v1.4/room/user/add")
    Call<AddRoomMemberBulkModel> addMemberApi(@Body AddRoomMemberBulkModel user, @Header("Authorization") String access_token);
}
