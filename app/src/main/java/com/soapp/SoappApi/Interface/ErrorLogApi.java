package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.ErrorModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ErrorLogApi {
    @POST("api/v1/log/error")
    Call<ErrorModel> sendLogstoServer(@Body ErrorModel model, @Header("Authorization") String acces_token);
}
