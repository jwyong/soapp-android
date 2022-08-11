package com.soapp.soapp_tab.reward.reward_list.reward_details;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.soapp.R;
import com.soapp.SoappApi.ApiModel.reward.RewardModel;
import com.soapp.SoappApi.Interface.RewardInterface;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.base.BaseActivity;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.sql.DatabaseHelper;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.soapp.global.GlobalVariables.STRPREF_ACCESS_TOKEN;

public class RewardDetailsActivity extends BaseActivity {

    ImageView imgv_res_pic, res_logo;
    TextView toolbar_title, rewards_details, tv_tnc, redeem_valid_date;
    Button redeem_btn;

    String reward_id, date_end, detail_resource_url, logo_resource_url, terms_and_conditions, resource_url, description, summary, title, restaurant_id;
    int current_quantity, max_quantity, soapp_points_required;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reward_reward_details);
        setupToolbar();

        initIntent();
        initWidget();
        setupImageView();
        setupTextView();
        setupRedeemButton();

    }

    public void initWidget(){
        imgv_res_pic = findViewById(R.id.imgv_res_pic);
        res_logo = findViewById(R.id.res_logo);

        toolbar_title = findViewById(R.id.toolbar_title);
        rewards_details = findViewById(R.id.rewards_details);
        tv_tnc = findViewById(R.id.tv_tnc);
        redeem_valid_date = findViewById(R.id.redeem_valid_date);

        redeem_btn = findViewById(R.id.redeem_btn);
    }

    public void initIntent(){
        Intent intent = getIntent();
        reward_id = intent.getStringExtra("reward_id");
        date_end = intent.getStringExtra("date_end");
        detail_resource_url = intent.getStringExtra("detail_resource_url");
        logo_resource_url = intent.getStringExtra("logo_resource_url");
        terms_and_conditions = intent.getStringExtra("terms_and_conditions");
        resource_url = intent.getStringExtra("resource_url");
        description = intent.getStringExtra("description");
        summary = intent.getStringExtra("summary");
        title = intent.getStringExtra("title");
        restaurant_id = intent.getStringExtra("restaurant_id");

        current_quantity = intent.getIntExtra("current_quantity",-1);
        max_quantity = intent.getIntExtra("max_quantity",-1);
        soapp_points_required = intent.getIntExtra("soapp_points_required",-1);
    }

    public void setupImageView(){
        if(detail_resource_url != null && !detail_resource_url.isEmpty()){
            GlideApp.with(this)
                    .asBitmap()
                    .load(detail_resource_url)
                    .placeholder(R.drawable.ic_res_default_no_image_black_640px)
                    .transition(BitmapTransitionOptions.withCrossFade())
                    .thumbnail(0.1f)
                    .into(imgv_res_pic);
        }else{
            imgv_res_pic.setImageResource(R.drawable.ic_res_default_no_image_black_640px);
        }

        if(logo_resource_url != null && !logo_resource_url.isEmpty()){
            GlideApp.with(this)
                    .asBitmap()
                    .load(logo_resource_url)
                    .placeholder(R.drawable.ic_res_default_no_image_black_640px)
                    .transition(BitmapTransitionOptions.withCrossFade())
                    .thumbnail(0.1f)
                    .into(res_logo);
        }else{
            res_logo.setImageResource(R.drawable.ic_res_default_no_image_black_640px);
        }
    }

    public void setupTextView(){
        toolbar_title.setText(title);
        rewards_details.setText(description);
        tv_tnc.setText(terms_and_conditions);
        tv_tnc.setMovementMethod(new ScrollingMovementMethod());
        String new_data_end = processDateEnd(date_end);
        redeem_valid_date.setText("EXPIRED ON "+new_data_end);

    }

    public void setupRedeemButton(){
        redeem_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(reward_id != null && !reward_id.isEmpty() ) {
                    Runnable run_redeemReward = () -> redeemRewardRetro(reward_id);
                    new UIHelper().dialog2Btns(RewardDetailsActivity.this, "Redeem Voucher", "Are you sure to redeem this voucher ?",
                            R.string.ok_label, R.string.cancel, R.color.black, R.color.black, run_redeemReward, null,
                            true);
//                    redeemRewardRetro(reward_id);
                }else{
                    Toast.makeText(RewardDetailsActivity.this,"missing reward id", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void redeemRewardRetro(String reward_id){
        Preferences preferences = Preferences.getInstance();
        String access_token = preferences.getValue(RewardDetailsActivity.this,STRPREF_ACCESS_TOKEN);
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
                            new UIHelper().dialog1Btn(RewardDetailsActivity.this, "Redeem Voucher", "Insufficient points to redeem reward",
                                    R.string.cancel, R.color.primaryDark3, null, true, false);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(RewardDetailsActivity.this, R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT).show();
                    return;
                }
                DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
                String redemption_id = response.body().getRedemption_id();
                databaseHelper.rewardInputDatabase(reward_id, resource_url, title, description, date_end, restaurant_id, redemption_id);
                new UIHelper().dialog1Btn(RewardDetailsActivity.this, "Redeem Voucher", "Successful",
                        R.string.ok_label, R.color.primaryDark3, null, true, true);
            }

            @Override
            public void onFailure(Call<RewardModel> call, Throwable t) {
                Toast.makeText(RewardDetailsActivity.this, R.string.onfailure, Toast.LENGTH_SHORT).show();
            }
        });

    }

    public String processDateEnd(String date_end){
        String new_date_end = date_end;
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date today = sdf.parse(new_date_end);
            new_date_end = sdf.format(today);
        } catch (ParseException ignore) { }

        return new_date_end;
    }
}

