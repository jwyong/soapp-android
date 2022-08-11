package com.soapp.soapp_tab.reward.my_reward_list;

import android.os.Bundle;
import android.widget.TextView;

import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.global.UIHelper;
import com.soapp.setup.Soapp;
import com.soapp.sql.room.entity.Reward;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MyRewardActivity extends BaseActivity {

    TextView tv_redeemed_value;

    MyRewardAdapter myRewardAdapter;
    RecyclerView rv_my_reward;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reward_my_reward_activity);

        //status bar color
        new UIHelper().setStatusBarColor(this, false , R.color.grey8);

        setupToolbar();
        setTitle("");
        initWidget();

        setupRecyclerView();
    }

    public void initWidget(){
        rv_my_reward = findViewById(R.id.rv_my_reward);
        tv_redeemed_value = findViewById(R.id.tv_redeemed_value);
    }

    public void setupRecyclerView(){
        rv_my_reward.setHasFixedSize(true);
        LiveData<List<Reward>> liveDataClaimedRes = Soapp.getDatabase().selectQuery().getRewardList();
        liveDataClaimedRes.observe(this, claimedResList -> {
            myRewardAdapter.submitList(claimedResList);
            tv_redeemed_value.setText(claimedResList.size() + " redeemed");
        });
        myRewardAdapter = new MyRewardAdapter();
        LinearLayoutManager llm = new LinearLayoutManager(MyRewardActivity.this);
        llm.setOrientation(RecyclerView.VERTICAL);
        rv_my_reward.setLayoutManager(llm);
        rv_my_reward.setAdapter(myRewardAdapter);
    }
}
