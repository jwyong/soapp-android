package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.NotificationRegisterTokenModel;
import com.soapp.SoappApi.ApiModel.RegisterModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by rlwt on 5/7/18.
 */

public interface RegisterInterface {
    @POST("api/v1/register")
    Call<RegisterModel> register(@Body RegisterModel user);

    @POST("api/v1.2/notification/register_token")
    Call<NotificationRegisterTokenModel> notificationRegisterToken(@Body NotificationRegisterTokenModel body, @Header("Authorization")
            String access_token);
}
