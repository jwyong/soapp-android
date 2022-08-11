package com.soapp.soapp_tab.reward.reward_personal_info;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.soapp.R;
import com.soapp.SoappApi.ApiModel.reward.RewardModel;
import com.soapp.SoappApi.Interface.RewardInterface;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.base.BaseActivity;
import com.soapp.global.GlobalVariables;
import com.soapp.global.Preferences;
import com.soapp.global.SharingHelper;
import com.soapp.global.UIHelper;
import com.soapp.soapp_tab.reward.my_reward_list.MyRewardActivity;
import com.soapp.soapp_tab.reward.reward_list.RewardActivity;

import java.util.List;

import androidx.constraintlayout.widget.ConstraintLayout;
import io.branch.referral.Branch;
import io.github.rockerhieu.emojicon.EmojiconTextView;
import me.tankery.lib.circularseekbar.CircularSeekBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RewardPersonalInfoActivity extends BaseActivity {

    ConstraintLayout cl_bottom_left;
    ConstraintLayout cl_below_welcome_plus_name;

    ImageView imgv_inside_circular_seekbar, imgv_reward_personal_share, imgv_reward_personal_register;

    ConstraintLayout progress_bar_cl;

    TextView tv_point_value, tv_my_reward_list, tv_between_circular_seekbar, tv_claim_in_csb, tv_share_now,
            tv_share, tv_register, rewards_terms;
    EmojiconTextView tv_welcome_plus_name;
    LinearLayout claim_ll;

    CircularSeekBar csb_points_value;

    Preferences preferences = Preferences.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reward_reward_personal_info_activity);

        //status bar color
        new UIHelper().setStatusBarColor(this, false , R.color.black3a);

        setupToolbar();
        setTitle("");
        initWidget();
        setupTextView();

        rewardMyPointsRetro();
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {

        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Branch.getInstance().initSession((referringParams, error) -> {
        });
    }

    public void initWidget() {
        imgv_inside_circular_seekbar = findViewById(R.id.imgv_inside_circular_seekbar);
        imgv_reward_personal_share = findViewById(R.id.imgv_reward_personal_share);
        imgv_reward_personal_register = findViewById(R.id.imgv_reward_personal_register);

        tv_my_reward_list = findViewById(R.id.tv_my_reward_list);
        tv_between_circular_seekbar = findViewById(R.id.tv_between_circular_seekbar);
        tv_claim_in_csb = findViewById(R.id.tv_claim_in_csb);
        claim_ll = findViewById(R.id.claim_ll);
        tv_point_value = findViewById(R.id.tv_point_value);
        tv_share_now = findViewById(R.id.tv_share_now);
        tv_share = findViewById(R.id.tv_share);
        tv_register = findViewById(R.id.tv_register);
        tv_welcome_plus_name = findViewById(R.id.tv_welcome_plus_name);
        csb_points_value = findViewById(R.id.csb_points_value);
        rewards_terms = findViewById(R.id.rewards_terms);

        progress_bar_cl = findViewById(R.id.progress_bar_cl);

        cl_below_welcome_plus_name = findViewById(R.id.cl_below_welcome_plus_name);

    }

    public void setupTextView() {
        String name = preferences.getValue(this, GlobalVariables.STRPREF_USERNAME);
        String welcome_msg = "Welcome, " + name;
        tv_welcome_plus_name.setText(welcome_msg);

        tv_share_now.setOnClickListener(view -> {
            new SharingHelper().shareSoappToFriends(this, findViewById(R.id.progress_bar_cl), view);
        });

        rewards_terms.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                "https://www.soappchat.com/tnc/download-an-ice-cream.html"))));
    }

    public void setupQuestLvl() {
        String quest_lvl = preferences.getValue(RewardPersonalInfoActivity.this, GlobalVariables.QUEST_LVL);

        switch (quest_lvl) {
            //register liao only
            case "nil"://fyi it actually is 100-QT1,0,0
                setupQuestLvl0(0);
                break;

            //shared, time to claim
            case "100-QT1,1,1":
                setupQuestLvl0(1);
                break;

            //shared and claimed
            default:
                setupQuestLvl0(2);
                break;
        }
    }

    public void setupQuestLvl0(int status) {
        String point_of_quest;
        String quest_title;

        float max = 2;
        float progress = 0;
        switch (status) {
            //shared - can claim now
            case 1:
                point_of_quest = "2 / 2";
                quest_title = point_of_quest;
                progress = (float) 1.99999;
                //show tick for share
                imgv_reward_personal_share.setVisibility(View.VISIBLE);

                //yay can claim
                tv_my_reward_list.setVisibility(View.VISIBLE);
                tv_my_reward_list.setOnClickListener(view -> {
                    Intent intent = new Intent(RewardPersonalInfoActivity.this, MyRewardActivity.class);
                    startActivity(intent);
                });

                tv_claim_in_csb.setVisibility(View.VISIBLE);
                claim_ll.setOnClickListener(view -> {
                    Intent intent = new Intent(RewardPersonalInfoActivity.this, RewardActivity.class);
                    startActivity(intent);
                });

                imgv_inside_circular_seekbar.setOnClickListener(view -> {
                    Intent intent = new Intent(RewardPersonalInfoActivity.this, RewardActivity.class);
                    startActivity(intent);
                });

                break;

            //claimed (redeemed, but not necessarily redeemed from shop
            case 2:
                point_of_quest = "";
                quest_title = getString(R.string.claimed);
                progress = (float) 1.99999;

                //hide all but show my reward list
                tv_claim_in_csb.setVisibility(View.GONE);
                tv_register.setVisibility(View.GONE);
                imgv_reward_personal_register.setVisibility(View.GONE);
                imgv_reward_personal_share.setVisibility(View.GONE);
                tv_share.setVisibility(View.GONE);

                tv_my_reward_list.setVisibility(View.VISIBLE);
                tv_my_reward_list.setOnClickListener(view -> {
                    Intent intent = new Intent(RewardPersonalInfoActivity.this, MyRewardActivity.class);
                    startActivity(intent);
                });

                //set text to "finished claiming, check My Reward List to redeem"
                TextView reward_desc_tv = findViewById(R.id.reward_desc_tv);
                reward_desc_tv.setText(R.string.reward_desc_claimed);
                break;

            default:
                point_of_quest = "1 / 2";
                quest_title = point_of_quest;
                progress = 1;
                //hide tick for share
                imgv_reward_personal_share.setVisibility(View.INVISIBLE);
                //cant claim yet cuz havent share
                tv_claim_in_csb.setVisibility(View.GONE);
                break;
        }

        tv_point_value.setText(quest_title);

        //between circular seekbar
        tv_between_circular_seekbar.setText(point_of_quest);

        //in circular seekbar
        setupCircularSeekBar(max, progress);
    }

//    public void setupQuestLvl1() {
//        //between circular seekbar
//        tv_between_circular_seekbar.setText("0 / " + GlobalVariables.QUEST_LVL_1_MAX_POINT);
//
//        //in circular seekbar
//        tv_claim_in_csb.setVisibility(View.VISIBLE);
//        setupCircularSeekBar(GlobalVariables.QUEST_LVL_1_MAX_POINT, 0);
//    }
//
//    public void setupQuestLvl2() {
//
//        //between circular seekbar
//        tv_between_circular_seekbar.setText("350 / " + GlobalVariables.QUEST_LVL_1_MAX_POINT);
//
//        //in circular seekbar
//        tv_claim_in_csb.setVisibility(View.GONE);
//        setupCircularSeekBar(GlobalVariables.QUEST_LVL_1_MAX_POINT, 350);
//
//        //right of circular seekbar
//        //top
//        tv_register.setVisibility(View.INVISIBLE);
//        imgv_reward_personal_register.setVisibility(View.INVISIBLE);
//        //mid
//        tv_share.setText("NEXT UPDATE");
//        imgv_reward_personal_share.setVisibility(View.GONE);
//    }
//
//    public void setupConstraintLayout() {
//        cl_bottom_left.setOnClickListener(view -> {
//            Intent intent = new Intent(RewardPersonalInfoActivity.this, MyRewardActivity.class);
//            startActivity(intent);
//        });
//    }
//
//    public void setupImageView() {
//        imgv_inside_circular_seekbar.setOnClickListener(view -> {
//            Intent intent = new Intent(RewardPersonalInfoActivity.this, RewardActivity.class);
//            startActivity(intent);
//        });
//    }

    public void setupCircularSeekBar(float max, float progress) {
        csb_points_value.setProgress(progress);
        csb_points_value.setMax(max);
    }

    public void inviteListenser() {
        setupQuestLvl();
    }


    public void rewardMyPointsRetro() {
        String access_token = preferences.getValue(RewardPersonalInfoActivity.this, GlobalVariables.STRPREF_ACCESS_TOKEN);
        RewardInterface client = RetrofitAPIClient.getClient().create(RewardInterface.class);
        retrofit2.Call<RewardModel> call = client.getRewardMyPointsApi("Bearer " + access_token);
        call.enqueue(new Callback<RewardModel>() {
            @Override
            public void onResponse(Call<RewardModel> call, Response<RewardModel> response) {
                if (!response.isSuccessful()) {
                    setupQuestLvl();
                    progress_bar_cl.setVisibility(View.GONE);

                    Toast.makeText(RewardPersonalInfoActivity.this, R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT).show();
                    return;
                }
                preferences.save(RewardPersonalInfoActivity.this, GlobalVariables.QUEST_LVL, "nil");
                int soapp_points = response.body().getSoapp_points();
                int experience_points = response.body().getExperience_points();
                List<RewardModel.Quest> quest = response.body().getQuests();
                for (RewardModel.Quest q : quest) {
                    //if quest completed, and uses only go to next level
                    //save latest quest that completed
                    if (q.getQuest_completed() == 1) {
                        preferences.save(RewardPersonalInfoActivity.this, GlobalVariables.QUEST_LVL, q.getQuest_id() + "," + q.getQuest_completed() + "," + q.getQuest_uses());
                    }
                }
                preferences.save(RewardPersonalInfoActivity.this, GlobalVariables.SOAPP_POINTS, String.valueOf(soapp_points));
                preferences.save(RewardPersonalInfoActivity.this, GlobalVariables.EXPERIENCE_POINTS, String.valueOf(soapp_points));

                setupQuestLvl();
                progress_bar_cl.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<RewardModel> call, Throwable t) {
                setupQuestLvl();
                progress_bar_cl.setVisibility(View.GONE);

                Toast.makeText(RewardPersonalInfoActivity.this, R.string.onfailure, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
