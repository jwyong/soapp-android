package com.soapp.SoappApi.ApiModel.reward;

import com.soapp.SoappApi.ApiModel.RewardRestaurantPoints;

import java.util.List;

/**
 * Created by rlwt on 7/20/18.
 */

public class RewardModel {
//    private String user_lon, user_lat, distance;

    int limit, offset;
    String order_by, order;

    public RewardModel(int limit, int offset, String order_by, String order){
        this.limit = limit;
        this.offset = offset;
        this.order_by = order_by;
        this.order = order;
    }

    private RewardPointsModel points;

    private List<RewardRestaurantPoints> rewards_restaurant_points;

    public RewardPointsModel getPoints() {return points;}

    public List<RewardRestaurantPoints> getRewards_restaurant_points() {return rewards_restaurant_points;}

    //
    private String redemption_id;

    public String getRedemption_id() {return redemption_id;}

    //
    private String user_jid;
    private  int soapp_points, experience_points;
    private List<Quest> quests;

    public String getUser_jid() {return user_jid;}

    public int getSoapp_points() {return soapp_points;}

    public int getExperience_points() {return experience_points;}

    public List<Quest> getQuests() { return quests; }

    public class Quest{
        private String quest_id;
        private String quest_title;
        private int quest_completed;
        private int quest_uses;
        private List<RewardRestaurantPoints> rewards;

        public String getQuest_id() { return quest_id; }

        public String getQuest_title() { return quest_title; }

        public int getQuest_completed() { return quest_completed; }

        public int getQuest_uses() { return quest_uses; }

        public List<RewardRestaurantPoints> getRewards() { return rewards; }
    }
}
