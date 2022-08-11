package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.UploadEmail;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/* Created by chang on 01/10/2017. */
/* 20171005 - OK */

public interface UploadEmailWithoutImage {
    @POST("api/v1/bug/report")
    Call<UploadEmail> upload(@Header("Authorization") String access_token, @Body UploadEmail uploadEmail);
}
