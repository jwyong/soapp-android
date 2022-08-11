package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.GroupModel;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/* Created by chang on 11/09/2017. */

public interface UpdateGroupProfileNameImage {
    @Multipart
    @POST("api/v1.2/room/update")
    Call<GroupModel> editUser(@Header("Authorization") String access_token, @Part MultipartBody.Part file, @Part("name") RequestBody name, @Part("room_id") RequestBody room_id);
}
