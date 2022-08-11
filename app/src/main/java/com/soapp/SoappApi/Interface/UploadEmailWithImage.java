package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.UploadEmail;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/*Created by chang on 01/10/2017. */
/* 20171005 - NEED CHANGE LATER */

public interface UploadEmailWithImage {
    @Multipart
    @POST("api/v1/bug/report")
    Call<UploadEmail> uploade(@Header("Authorization") String access_token, @Part MultipartBody.Part file, @Part("jid") RequestBody user_id,
                              @Part("name") RequestBody name, @Part("CountryCode") RequestBody CountryCode, @Part("phone") RequestBody phone,
                              @Part("email") RequestBody email, @Part("message") RequestBody message);
}
