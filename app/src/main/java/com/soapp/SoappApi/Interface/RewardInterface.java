package com.soapp.SoappApi.Interface;

import com.soapp.SoappApi.ApiModel.reward.RewardModel;

import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**(
 * Created by rlwt on 7/20/18.
 */

public interface RewardInterface {
    @POST("api/v1.3/reward/search/quest")
    Call<List<RewardModel.Quest>> searchRewardApi(@Header("Authorization") String access_token, @Body RewardModel body);

    @POST("api/v1.3/reward/redeem")
    @Multipart
    Call<RewardModel> redeemRewardApi(@Header("Authorization") String access_token, @Part("reward_id") RequestBody reward_id);

    @POST("api/v1.3/reward/cancel_redemption")
    @Multipart
    Call<ResponseBody> cancelRedemptionApi(@Header("Authorization") String access_token, @Part("redemption_id") RequestBody redemption_id);

    @GET("api/v1.3/reward/my_rewards")
    Call<RewardModel> getRewardMyPointsApi(@Header("Authorization") String access_token);
}
