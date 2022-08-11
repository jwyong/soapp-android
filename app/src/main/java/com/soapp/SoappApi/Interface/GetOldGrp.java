package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.GetOldGrpModel;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

/* Created by Soapp on 30/10/2017. */

public interface GetOldGrp {
    @GET("api/v1.4/room/list/{size}")
    Call<List<GetOldGrpModel>> getOldGrps(@Path("size") String Size, @Header("Authorization") String access_token);

    @GET("api/v1.4/room/list/{size}")
    Observable<List<GetOldGrpModel>> getOldGrps2(@Path("size") String Size, @Header("Authorization") String access_token);
}