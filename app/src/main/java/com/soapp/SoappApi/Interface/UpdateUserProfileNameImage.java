package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.UserModel;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/* Created by chang on 21/08/2017. */
/* 20171005 - OK */

public interface UpdateUserProfileNameImage {
    @Multipart
    @POST("api/v1.2/user/")
    Call<UserModel> editUser(@Header("Authorization") String access_token,
                             @Part MultipartBody.Part file,
                             @Part("name") RequestBody name);
}
