package com.soapp.soapp_tab.reward.reward_list;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.soapp.R;
import com.soapp.SoappApi.ApiModel.RewardRestaurantPoints;
import com.soapp.SoappApi.ApiModel.reward.RewardModel;
import com.soapp.SoappApi.Interface.RewardInterface;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.GlobalVariables;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.soapp_tab.reward.my_reward_list.MyRewardActivity;
import com.soapp.sql.DatabaseHelper;

import java.io.IOException;

import androidx.recyclerview.widget.RecyclerView;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.soapp.global.GlobalVariables.STRPREF_ACCESS_TOKEN;

/**
 * Created by rlwt on 7/20/18.
 */

public class RewardHolder extends RecyclerView.ViewHolder {

    RewardAdapter.MyRewardInterface mRewardInterface;

    ImageView res_pic, res_logo;
    TextView res_name, redeem_detail, redeem_point, tv_redeem;
    Context context;
    RelativeLayout cl_reward_item;

    public RewardHolder(View itemView) {
        super(itemView);

        res_pic = itemView.findViewById(R.id.res_pic);
        res_logo = itemView.findViewById(R.id.res_logo);

        res_name = itemView.findViewById(R.id.res_name);
        redeem_detail = itemView.findViewById(R.id.redeem_detail);
        redeem_point = itemView.findViewById(R.id.redeem_point);
        tv_redeem = itemView.findViewById(R.id.tv_redeem);

        cl_reward_item = itemView.findViewById(R.id.cl_reward_item);

        context = itemView.getContext();
    }

    public void setData(final RewardRestaurantPoints rewardRestaurantPoints, RewardAdapter.MyRewardInterface mRewardInterface){
        this.mRewardInterface = mRewardInterface;
        String reward_id = rewardRestaurantPoints.getReward_id();
        String date_end = rewardRestaurantPoints.getDate_end();
        String detail_resource_url = rewardRestaurantPoints.getDetail_resource_url();
        String logo_resource_url = rewardRestaurantPoints.getLogo_resource_url();
        String terms_and_conditions = rewardRestaurantPoints.getTerms_and_conditions();
        String resource_url = rewardRestaurantPoints.getResource_url();
        String description = rewardRestaurantPoints.getDescription();
        String summary = rewardRestaurantPoints.getSummary();
        String title = rewardRestaurantPoints.getTitle();
        String restaurant_id = rewardRestaurantPoints.getRestaurant_id();
        String restaurant_name = rewardRestaurantPoints.getRestaurant_name();

        int current_quantity = rewardRestaurantPoints.getCurrent_quantity();
        int max_quantity = rewardRestaurantPoints.getMax_quantity();
        int soapp_points_required = rewardRestaurantPoints.getSoapp_points_required();
//        cl_reward_item.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent rewardDetailsIntent = new Intent(context, RewardDetailsActivity.class);
//                rewardDetailsIntent.putExtra("reward_id",reward_id);
//                rewardDetailsIntent.putExtra("date_end",date_end);
//                rewardDetailsIntent.putExtra("detail_resource_url",detail_resource_url);
//                rewardDetailsIntent.putExtra("logo_resource_url",logo_resource_url);
//                rewardDetailsIntent.putExtra("terms_and_conditions",terms_and_conditions);
//                rewardDetailsIntent.putExtra("resource_url",resource_url);
//                rewardDetailsIntent.putExtra("description",description);
//                rewardDetailsIntent.putExtra("summary",summary);
//                rewardDetailsIntent.putExtra("title",title);
//                rewardDetailsIntent.putExtra("current_quantity",current_quantity);
//                rewardDetailsIntent.putExtra("max_quantity",max_quantity);
//                rewardDetailsIntent.putExtra("soapp_points_required",soapp_points_required);
//                rewardDetailsIntent.putExtra("restaurant_id",restaurant_id);
//                rewardDetailsIntent.putExtra("restaurant_name",restaurant_name);
//
//                context.startActivity(rewardDetailsIntent);
//            }
//        });

        //main reward img
        if(resource_url != null && !resource_url.isEmpty()){
            GlideApp.with(context)
                    .asBitmap()
                    .load(resource_url)
                    .placeholder(R.drawable.ic_res_default_no_image_black_640px)
                    .transition(BitmapTransitionOptions.withCrossFade())
                    .thumbnail(0.1f)
                    .into(res_pic);
        }else{
            res_pic.setImageResource(R.drawable.ic_res_default_no_image_black_640px);
        }

        //small restaurant logo
        if(logo_resource_url != null && !logo_resource_url.isEmpty()){
            GlideApp.with(context)
                    .asBitmap()
                    .load(logo_resource_url)
                    .placeholder(R.drawable.ic_res_default_no_image_black_640px)
                    .transition(BitmapTransitionOptions.withCrossFade())
                    .thumbnail(0.1f)
                    .into(res_logo);
        }else{
            res_logo.setImageResource(R.drawable.ic_logo_soapp_500px);
        }


        if(title != null && !title.isEmpty()){
            res_name.setText(title);
        }else{
            res_name.setText("-");
        }

        if(restaurant_name != null && !restaurant_name.isEmpty()){
            redeem_detail.setText(restaurant_name);
        }else{
            redeem_detail.setText("-");
        }

        redeem_point.setText(String.valueOf(soapp_points_required)+" POINTS TO REDEEM");

        if(reward_id != null || !reward_id.isEmpty()){
            tv_redeem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Runnable run_redeemReward = () -> redeemRewardRetro(reward_id, resource_url, title, description, date_end, restaurant_id);;
                    new UIHelper().dialog2Btns(context, context.getString(R.string.redeem_voucher), context.getString(R.string.redeem_voucher_msg),
                            R.string.ok_label, R.string.cancel, R.color.black, R.color.black, run_redeemReward, null,
                            true);
                }
            });
        }
    }

    public void redeemRewardRetro(String reward_id, String resource_url, String title, String description, String date_end, String restaurant_id){
        Preferences preferences = Preferences.getInstance();
        String access_token = preferences.getValue(context,STRPREF_ACCESS_TOKEN);
        RequestBody rewardIdRB = RequestBody.create(MediaType.parse("text/plain"), reward_id);
        RewardInterface client = RetrofitAPIClient.getClient().create(RewardInterface.class);
        retrofit2.Call<RewardModel> call = client.redeemRewardApi("Bearer "+access_token,rewardIdRB);
        call.enqueue(new Callback<RewardModel>() {
            @Override
            public void onResponse(Call<RewardModel> call, Response<RewardModel> response) {
                if(!response.isSuccessful()){
                    try {
                        String error = response.errorBody().string();
                        int insufficient_points = error.indexOf("Insufficient points to redeem reward");
                        if(insufficient_points != -1){
                            new UIHelper().dialog1Btn(context, "Redeem Voucher", "Insufficient points to redeem reward",
                                    R.string.cancel, R.color.primaryDark3, null, true, false);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(context, R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT).show();
                    return;
                }
                DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
                preferences.save(context, GlobalVariables.QUEST_LVL, "100-QT1,1,0");
                String redemption_id = response.body().getRedemption_id();
                databaseHelper.rewardInputDatabase(reward_id, resource_url, title, description, date_end, restaurant_id, redemption_id);
//                new UIHelper().dialog1Btn(context, context.getString(R.string.redeem_voucher), context.getString(R.string.redeem_voucher_success),
//                        R.string.ok_label, R.color.primaryDark3, null, true, true);

                //go to my reward list
                Intent intent = new Intent(context, MyRewardActivity.class);
                context.startActivity(intent);

                mRewardInterface.kilLPage();
            }

            @Override
            public void onFailure(Call<RewardModel> call, Throwable t) {
                Toast.makeText(context, R.string.onfailure, Toast.LENGTH_SHORT).show();
            }
        });

    }
}
