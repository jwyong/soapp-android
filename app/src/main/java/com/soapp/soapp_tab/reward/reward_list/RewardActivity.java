package com.soapp.soapp_tab.reward.reward_list;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.soapp.R;
import com.soapp.SoappApi.ApiModel.RewardRestaurantPoints;
import com.soapp.SoappApi.ApiModel.reward.RewardModel;
import com.soapp.SoappApi.Interface.RewardInterface;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.base.BaseActivity;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.soapp.global.GlobalVariables.STRPREF_ACCESS_TOKEN;

public class RewardActivity extends BaseActivity {

    LinearLayout ll_reward_progress;

    RecyclerView rv_reward;
    RewardAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reward_reward_main_page);
        setupToolbar();

        //status bar color
        new UIHelper().setStatusBarColor(this, false , R.color.grey2);

        initWidget();
//        3.156054, 101.596032 soapp
//        rewardRetro("101.596032", "3.156054", "100000");
        rewardRetro(100,0, "", "");
    }

    public void initWidget(){
        ll_reward_progress = findViewById(R.id.ll_reward_progress);
        ll_reward_progress.setVisibility(View.VISIBLE);
        rv_reward = findViewById(R.id.rv_reward);
        rv_reward.setVisibility(View.GONE);
    }

    public void setupRecyclerView(List<RewardRestaurantPoints> rewardRestaurantPointsList){
        ll_reward_progress.setVisibility(View.GONE);
        rv_reward.setVisibility(View.VISIBLE);
        rv_reward.setHasFixedSize(true);
        mAdapter = new RewardAdapter(rewardRestaurantPointsList, RewardActivity.this, new RewardAdapter.MyRewardInterface() {
            @Override
            public void kilLPage() {
                finish();
            }
        });
        LinearLayoutManager llm = new LinearLayoutManager(RewardActivity.this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rv_reward.setLayoutManager(llm);
        rv_reward.setAdapter(mAdapter);
    }

    public void rewardRetro(int limit, int offset, String order_by, String order ){
        Preferences preferences = Preferences.getInstance();
        RewardModel rewardModel = new RewardModel(limit, offset, order_by, order);
        String access_token = preferences.getValue(RewardActivity.this,STRPREF_ACCESS_TOKEN);
        RewardInterface client = RetrofitAPIClient.getClient().create(RewardInterface.class);
        retrofit2.Call<List<RewardModel.Quest>> call = client.searchRewardApi("Bearer "+access_token,rewardModel);
        call.enqueue(new Callback<List<RewardModel.Quest>>() {
            @Override
            public void onResponse(Call<List<RewardModel.Quest>> call, Response<List<RewardModel.Quest>> response) {
                if(!response.isSuccessful()){
                    Toast.makeText(RewardActivity.this, R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                List<RewardRestaurantPoints> rewardRestaurantPointsList =  new ArrayList<>();
                List<RewardModel.Quest> questList = response.body();
                for (RewardModel.Quest r:
                        questList) {
                    for (RewardRestaurantPoints rr:
                         r.getRewards()) {
                        rewardRestaurantPointsList.add(rr);
                    };
                }
                setupRecyclerView(rewardRestaurantPointsList);
            }

            @Override
            public void onFailure(Call<List<RewardModel.Quest>> call, Throwable t) {
                Toast.makeText(RewardActivity.this, R.string.onfailure, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
