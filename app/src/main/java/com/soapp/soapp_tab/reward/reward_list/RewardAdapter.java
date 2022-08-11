package com.soapp.soapp_tab.reward.reward_list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.soapp.R;
import com.soapp.SoappApi.ApiModel.RewardRestaurantPoints;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by rlwt on 7/20/18.
 */

public class RewardAdapter extends RecyclerView.Adapter<RewardHolder>{

    MyRewardInterface mRewardInterface;

    public interface MyRewardInterface {
        void kilLPage();
    }

    List<RewardRestaurantPoints> rewardRestaurantPointsList;

    Context context;

    public RewardAdapter(List<RewardRestaurantPoints> rewardRestaurantPointsList, Context context, MyRewardInterface mRewardInterface){
        this.rewardRestaurantPointsList = rewardRestaurantPointsList;
        this.context = context;
        this.mRewardInterface = mRewardInterface;
    }

    @NonNull
    @Override
    public RewardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RewardHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.reward_rewards_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RewardHolder holder, int position) {
        holder.setData(rewardRestaurantPointsList.get(position), mRewardInterface);
    }

    @Override
    public int getItemCount() {
        return rewardRestaurantPointsList.size();
    }

    @Override
    public long getItemId(int position) {
        return rewardRestaurantPointsList.get(position).getReward_id().hashCode();
    }
}
