package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.VersionControlModel;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

/* Created by Soapp on 21/11/2017. */

public interface SpecialFunctions {
    //version control
    @FormUrlEncoded
    @POST("api/v1.1/servercheck/check_version")
    Call<VersionControlModel> versionControl(@Field("app_version") String version, @Field("app_type") String appType,
                                             @Header("Authorization") String access_token);

    //delete soapp acc
    @FormUrlEncoded
    @POST("api/v1/user/delete_account")
    Call<ResponseBody> deleteAcc(@Header("Authorization") String access_token, @Field("user_jid") String user_jid);
}
