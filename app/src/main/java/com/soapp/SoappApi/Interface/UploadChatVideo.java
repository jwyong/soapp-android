package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.FileModel;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/* Created by chang on 03/09/2017. */
/* 20171005 - DONE (only need implement resource_url next for more speed) */

public interface UploadChatVideo {
    @Multipart
    @POST("api/v1.3/resource")
    Call<FileModel> uploadvideo(@Header("Authorization") String access_token, @Part MultipartBody
            .Part file, @Part MultipartBody.Part type, @Part MultipartBody.Part thumbnail);
}
