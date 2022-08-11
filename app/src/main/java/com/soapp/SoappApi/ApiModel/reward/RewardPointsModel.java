package com.soapp.SoappApi.ApiModel.reward;

/**
 * Created by rlwt on 7/20/18.
 */

public class RewardPointsModel {
    private String user_jid;

    private int soapp_points, experience_points;

    public String getUser_jid() {return user_jid;}

    public int getSoapp_points() {return soapp_points;}

    public int getExperience_points() {return experience_points;}
}
