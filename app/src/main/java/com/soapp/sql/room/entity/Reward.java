package com.soapp.sql.room.entity;

import android.content.ContentValues;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by rlwt on 7/24/18.
 */

@Entity(tableName = "REWARD")
public class Reward {
    @PrimaryKey(autoGenerate = true)
    private Integer RewardRow;

    private String RewardID;

    private String RewardImgURL;

    private String RewardTitle;

    private String RewardDesc;

    private String RewardDateEnd;

    private String RewardResID;

    private String RewardRedeemID;

    public Reward() {
    }

    public static Reward fromContentValues(ContentValues values) {
        final Reward reward = new Reward();

        if (values.containsKey("RewardID")) {
            reward.RewardID = values.getAsString("RewardID");
        }

        if (values.containsKey("RewardImgURL")) {
            reward.RewardImgURL = values.getAsString("RewardImgURL");
        }

        if (values.containsKey("RewardTitle")) {
            reward.RewardTitle = values.getAsString("RewardTitle");
        }

        if (values.containsKey("RewardDesc")) {
            reward.RewardDesc = values.getAsString("RewardDesc");
        }

        if (values.containsKey("RewardDateEnd")) {
            reward.RewardDateEnd = values.getAsString("RewardDateEnd");
        }

        if (values.containsKey("RewardResID")) {
            reward.RewardResID = values.getAsString("RewardResID");
        }

        if (values.containsKey("RewardRedeemID")) {
            reward.RewardRedeemID = values.getAsString("RewardRedeemID");
        }

        return reward;
    }

    public Integer getRewardRow() {return RewardRow;}
    public void setRewardRow(Integer rewardRow) {this.RewardRow = rewardRow;}

    public String getRewardID() {return RewardID;}
    public void setRewardID(String rewardID) {this.RewardID = rewardID;}

    public String getRewardImgURL() {return RewardImgURL;}
    public void setRewardImgURL(String rewardImgURL) {this.RewardImgURL = rewardImgURL;}

    public String getRewardTitle() {return RewardTitle;}
    public void setRewardTitle(String rewardTitle) {this.RewardTitle = rewardTitle;}

    public String getRewardDesc() {return RewardDesc;}
    public void setRewardDesc(String rewardDesc) {this.RewardDesc = rewardDesc;}

    public String getRewardDateEnd() {return RewardDateEnd;}
    public void setRewardDateEnd(String rewardDateEnd) {this.RewardDateEnd = rewardDateEnd;}

    public String getRewardResID() {return RewardResID;}
    public void setRewardResID(String rewardResID) {this.RewardResID = rewardResID;}

    public String getRewardRedeemID() {return RewardRedeemID;}
    public void setRewardRedeemID(String rewardRedeemID) {this.RewardRedeemID = rewardRedeemID;}
}