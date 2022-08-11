package com.soapp.SoappApi.ApiModel;

/**
 * Created by rlwt on 7/20/18.
 */

public class RewardRestaurantPoints {

    private String quest_id, reward_id, resource_url, detail_resource_url, title, description, summary, terms_and_conditions, date_start, date_end, restaurant_id, logo_resource_url, restaurant_name;
    private int soapp_points_required, max_quantity, current_quantity;

    public String getQuest_id() { return quest_id; }

    public String getReward_id() {return reward_id;}

    public String getResource_url() {return resource_url;}

    public String getDetail_resource_url() {return detail_resource_url;}

    public String getTitle() {return title;}

    public String getDescription() {return description;}

    public String getSummary() {return summary;}

    public String getTerms_and_conditions() {return terms_and_conditions;}

    public String getDate_start() {return date_start;}

    public String getDate_end() {return date_end;}

    public String getLogo_resource_url() {return logo_resource_url;}

    public int getSoapp_points_required() {return soapp_points_required;}

    public int getMax_quantity() {return max_quantity;}

    public int getCurrent_quantity() {return current_quantity;}

    public String getRestaurant_id() {return restaurant_id;}

    public String getRestaurant_name() { return restaurant_name; }
}
