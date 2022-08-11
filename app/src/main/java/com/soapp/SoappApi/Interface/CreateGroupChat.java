package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.CreateGroupModel;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/* Created by Soapp on 18/10/2017. */
//new create group api, still waiting for ding en

public interface CreateGroupChat {
    @POST("api/v1.4/room/create")
    @Multipart
    Call<CreateGroupModel> createGroupApi(@Header("Authorization") String access_token,
                                          //room details
                                          @Part("room_name") RequestBody room_name,
                                          @Part MultipartBody.Part room_image,
                                          @Part("member_ids") RequestBody member_ids, //e.g.: 698dfb219aad,9160fc73d5a5,b8894a5aaf29,

                                          //xmpp details
                                          @Part("message_id") RequestBody message_id, //uniqueID
                                          @Part("pubsub_host") RequestBody pubsub_host,
                                          @Part("xmpp_host") RequestBody xmpp_host,
                                          @Part("xmpp_resources") RequestBody xmpp_resources);
}

