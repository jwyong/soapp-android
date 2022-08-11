package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.SPgetAllResModel;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/* Created by Soapp on 18/10/2017. */
//new create group api, still waiting for ding en

public interface GetProfileThumb {
    @POST("api/v1.3/resource/download_thumbnail")
    @Multipart
    Call<List<SPgetAllResModel>> spGetAllRes(@Header("Authorization") String access_token,
                                             @Part("ids") RequestBody jid_listString,
                                             @Part("size") RequestBody size);

    @POST("api/v1.3/resource/download_thumbnail")
    @Multipart
    Observable<List<SPgetAllResModel>> spGetAllRes2(@Header("Authorization") String access_token,
                                                    @Part("ids") RequestBody jid_listString,
                                                    @Part("size") RequestBody size);
}
