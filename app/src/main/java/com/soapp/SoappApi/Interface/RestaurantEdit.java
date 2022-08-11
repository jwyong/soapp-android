package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.ResSuggestEditInfoModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by rlwt on 2/23/18.
 */

public interface RestaurantEdit {
    @POST("api/v1.2/restaurant/suggest_edit")
    Call<List<ResSuggestEditInfoModel>> editRes(@Body ResSuggestEditInfoModel res, @Header
            ("Authorization") String access_token);
}
