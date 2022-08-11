package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.Resource1v3Model;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

/**
 * Created by rlwt on 5/7/18.
 */

public interface Resource1v3Interface {
    @GET("api/v1.3/resource/{user_jid}")
    Call<Resource1v3Model> getResource(@Path("user_jid") String jid, @Header("Authorization") String access_token);
}
