package com.soapp.soapp_tab.reward.my_reward_list;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.soapp.R;
import com.soapp.sql.room.entity.Reward;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

/**
 * Created by rlwt on 7/25/18.
 */

public class MyRewardAdapter extends ListAdapter<Reward, MyRewardHolder> {

    private static final DiffUtil.ItemCallback<Reward> DIFF_CALLBACK = new DiffUtil.ItemCallback<Reward>() {
        @Override
        public boolean areItemsTheSame(
                @NonNull Reward oldReward, @NonNull Reward newReward) {
            // User properties may have changed if reloaded from the DB, but ID is fixed
            return oldReward.getRewardID() != null && newReward.getRewardID().equals(newReward.getRewardID());
        }

        @Override
        public boolean areContentsTheSame(
                @NonNull Reward oldRes, @NonNull Reward newRes) {
            // NOTE: if you use equals, your object must properly override Object#equals()
            // Incorrectly returning false here will result in too many animations.
            return oldRes.equals(newRes);
        }
    };

    MyRewardAdapter(){
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public MyRewardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyRewardHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.reward_my_reward_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyRewardHolder holder, int position) {
        holder.setData(getItem(position));
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getRewardID().hashCode();
    }

    @Override
    public Reward getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }
}
