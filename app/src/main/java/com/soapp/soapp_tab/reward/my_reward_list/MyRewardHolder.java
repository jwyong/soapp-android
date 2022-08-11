package com.soapp.soapp_tab.reward.my_reward_list;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.soapp.R;
import com.soapp.SoappApi.Interface.RewardInterface;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.GlobalVariables;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.soapp_tab.reward.my_reward_list.MyRewardDetails.MyRewardDetailsActivity;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.entity.Reward;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by rlwt on 7/25/18.
 */

public class MyRewardHolder extends RecyclerView.ViewHolder {

    ConstraintLayout cl_my_reward_item;

    ImageView imgv_reward;

    TextView tv_reward_title, tv_reward_des;

    Button delete_voucher;

    Context context;

    public MyRewardHolder(View itemView) {
        super(itemView);
        cl_my_reward_item = itemView.findViewById(R.id.cl_my_reward_item);
        imgv_reward = itemView.findViewById(R.id.imgv_reward);
        tv_reward_title = itemView.findViewById(R.id.tv_reward_title);
        tv_reward_des = itemView.findViewById(R.id.tv_reward_des);
        delete_voucher = itemView.findViewById(R.id.delete_voucher);

        context = itemView.getContext();
    }

    public void setData(Reward reward){
        String resource_url = reward.getRewardImgURL();
        String title = reward.getRewardTitle();
        String description = reward.getRewardDesc();
        String redemption_id = reward.getRewardRedeemID();

        if(resource_url != null && !resource_url.isEmpty()) {
            GlideApp.with(itemView.getContext())
                    .asBitmap()
                    .load(resource_url)
                    .placeholder(R.drawable.ic_res_default_no_image_black_640px)
                    .transition(BitmapTransitionOptions.withCrossFade())
                    .thumbnail(0.1f)
                    .into(imgv_reward);
        }else{
            imgv_reward.setImageResource(R.drawable.ic_res_default_no_image_black_640px);
        }

        tv_reward_title.setText(title);

        tv_reward_des.setText(description);

        delete_voucher.setOnClickListener(view -> {
            Runnable run_cancelRedemption = () -> cancelRedemptionRetro(itemView, redemption_id);
            new UIHelper().dialog2Btns(itemView.getContext(), context.getString(R.string.delete_voucher),
                    context.getString(R.string.delete_voucher_msg), R.string.delete, R.string.cancel,
                    R.color.red, R.color.black, run_cancelRedemption, null, true);
        });

        cl_my_reward_item.setOnClickListener(view -> {
            Intent intent = new Intent(itemView.getContext(), MyRewardDetailsActivity.class);
            intent.putExtra("redemption_id",redemption_id);
            intent.putExtra("title",title);
            intent.putExtra("description",description);
            itemView.getContext().startActivity(intent);
        });
    }

    public void cancelRedemptionRetro(View itemView, String redemption_id){
        Preferences preferences = Preferences.getInstance();
        String access_token = preferences.getValue(itemView.getContext(), GlobalVariables.STRPREF_ACCESS_TOKEN);
        RequestBody rewardIdRB = RequestBody.create(MediaType.parse("text/plain"), redemption_id);
        RewardInterface client = RetrofitAPIClient.getClient().create(RewardInterface.class);
        retrofit2.Call<ResponseBody> call = client.cancelRedemptionApi("Bearer "+access_token,rewardIdRB);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(!response.isSuccessful()){
                    Toast.makeText(itemView.getContext(), R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
                databaseHelper.deleteRDB1Col(DatabaseHelper.REWARD_TABLE_NAME, DatabaseHelper.REWARD_REDEMPTION_ID, redemption_id);
                new UIHelper().dialog1Btn(context, context.getString(R.string.delete_voucher), context.getString(R.string.delete_voucher_success),
                        R.string.ok_label, R.color.primaryDark3, null, true, true);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(itemView.getContext(), R.string.onfailure, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
