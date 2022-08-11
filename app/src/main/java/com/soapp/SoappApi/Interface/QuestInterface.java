package com.soapp.SoappApi.Interface;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface QuestInterface {
    @GET("api/v1.3/quest/invite_friends/{sharer_jid}")
    Call<ResponseBody> questInviteFriendsApi(@Path("sharer_jid") String jid, @Header("Authorization") String access_token);
}
