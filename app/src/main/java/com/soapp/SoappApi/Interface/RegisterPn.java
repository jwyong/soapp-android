package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.RegisterUser;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/* Created by chang on 03/08/2017. */
/* 20171005 - OK */

public interface RegisterPn {
    @POST("api/v1/auth/sms")
    Call<RegisterUser> registerPhoneNumber(@Body RegisterUser user);
}
