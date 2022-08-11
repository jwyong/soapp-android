package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.CreateApptForExistGrpModel;
import com.soapp.SoappApi.ApiModel.GetGrpApptModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CreateApptForExistGrp {

    @POST("api/v1.4/room/appointment/create")
    Call<CreateApptForExistGrpModel> createNewAppt(@Header("Authorization") String access_token, @Body CreateApptForExistGrpModel model);

    @GET("api/v1.4/room/appointment/get/{appointment_id}")
    Call<GetGrpApptModel> getGrpApptInfo(@Header("Authorization") String access_token, @Path("appointment_id") String apptID);
}
