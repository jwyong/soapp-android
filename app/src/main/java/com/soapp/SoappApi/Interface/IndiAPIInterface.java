package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.GetUserRepo;
import com.soapp.SoappApi.ApiModel.PostPhoneGetJidModel;
import com.soapp.SoappApi.ApiModel.SyncContactsModel;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/* Created by Jayyong on 03/04/2018. */

public interface IndiAPIInterface {
    //check if posted phone numbers (String array) are registered on Soapp (SyncContact)
    @POST("api/v1.3/contacts/check")
    Call<List<SyncContactsModel>> syncContacts(@Body PostPhoneGetJidModel model, @Header("Authorization") String access_token);

    @POST("api/v1.3/contacts/check")
    Observable<List<SyncContactsModel>> syncContacts2(@Body PostPhoneGetJidModel model, @Header("Authorization") String access_token);

    //get individual user profile
    @GET("api/v1.3/user/{user}/{size}")
    Call<GetUserRepo> getIndiProfile(@Path("user") String id, @Path("size") String size, @Header("Authorization") String access_token);
}
