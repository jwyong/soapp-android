package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.RestaurantReviewImageUploadModel;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/* Created by Soapp on 30/10/2017. */

public interface RestaurantReviewImageUpload {
    @Multipart
    @POST("api/v1.2/restaurant/imageUpload")
    Call<RestaurantReviewImageUploadModel> uploadImage(@Header("Authorization") String access_token, @Part MultipartBody
            .Part res_ID, @Part MultipartBody.Part image);
}