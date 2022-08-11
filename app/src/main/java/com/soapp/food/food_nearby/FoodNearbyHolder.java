package com.soapp.food.food_nearby;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.soapp.R;

import androidx.recyclerview.widget.RecyclerView;

/* Created by Soapp on 19/09/2017. */

public class FoodNearbyHolder extends RecyclerView.ViewHolder {

    public ImageView res_profile_img, res_fav_btn, res_appt_btn, res_loc_btn, res_share_btn,
            res_rating, res_cam_btn;
    public TextView res_name, res_cuisine, res_loc_state, res_vid, ll_testje, star_num;


    public FoodNearbyHolder(View itemView) {
        super(itemView);
        res_profile_img = itemView.findViewById(R.id.res_profile_img);
        res_fav_btn = itemView.findViewById(R.id.res_fav_btn);
        res_cam_btn = itemView.findViewById(R.id.res_cam_btn);
        res_appt_btn = itemView.findViewById(R.id.res_appt_btn);
        res_loc_btn = itemView.findViewById(R.id.res_loc_btn);
        res_share_btn = itemView.findViewById(R.id.res_share_btn);
        star_num = itemView.findViewById(R.id.star_num);
        res_name = itemView.findViewById(R.id.res_name);
        res_cuisine = itemView.findViewById(R.id.res_cuisine);
//        res_loc_state = itemView.findViewById(R.id.res_loc_state);
        res_fav_btn = itemView.findViewById(R.id.res_fav_btn);
        res_vid = itemView.findViewById(R.id.res_vid);

    }
}



