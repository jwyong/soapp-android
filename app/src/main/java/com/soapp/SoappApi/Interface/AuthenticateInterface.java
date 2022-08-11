package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.AuthenticateModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by rlwt on 5/7/18.
 */

public interface AuthenticateInterface {
    @POST("api/v1/authenticate")
    Call<AuthenticateModel> authenticate(@Body AuthenticateModel user);
}
