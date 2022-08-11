package com.soapp.food.food_detail.info;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.soapp.R;
import com.soapp.food.food_detail.FoodDetailLog;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.MapGps;
import com.soapp.global.MiscHelper;
import com.soapp.global.UIHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

public class FoodDetailInfoFragment extends Fragment implements View.OnClickListener {
    //basics
    UIHelper uiHelper = new UIHelper();

    //elements
    ConstraintLayout food_det_op_hours;
    TextView tv_rest_showall, tv_rest_operation_hour, tv_address, tv_monday, tv_tuesday, tv_wednesday, tv_thursday, tv_friday, tv_saturday, tv_sunday,
            tv_rest_get_direction, tv_cuisine_1, tv_cuisine_2, tv_cuisine_3, tv_rest_contactno, tv_call_now;
    ImageView imgv_tick_halal, imgv_tick_alcohol, imgv_tick_wifi, imgv_tick_smoking;

    //variables
    String[] foodDetailInfo;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.food_details_rest_information, container, false);
        FoodDetailLog foodDetailLog = (FoodDetailLog) getActivity();
        foodDetailInfo = foodDetailLog.getfoodDetailInfo();

        food_det_op_hours = view.findViewById(R.id.food_det_op_hours);
        food_det_op_hours.setOnClickListener(this);

        tv_rest_contactno = view.findViewById(R.id.tv_rest_contactno);
        tv_rest_contactno.setText(foodDetailInfo[7]);

        tv_address = view.findViewById(R.id.tv_rest_add);
        tv_address.setText(foodDetailInfo[6]);

        tv_monday = view.findViewById(R.id.tv_monday);
        tv_tuesday = view.findViewById(R.id.tv_tuesday);
        tv_wednesday = view.findViewById(R.id.tv_wednesday);
        tv_thursday = view.findViewById(R.id.tv_thursday);
        tv_friday = view.findViewById(R.id.tv_friday);
        tv_saturday = view.findViewById(R.id.tv_saturday);
        tv_sunday = view.findViewById(R.id.tv_sunday);

        tv_rest_operation_hour = view.findViewById(R.id.tv_rest_operation_hour);
        tv_rest_showall = view.findViewById(R.id.tv_rest_showall);
        tv_rest_showall.setOnClickListener(this);

        //set operating hours
        tv_monday.setText(foodDetailInfo[13]);
        tv_tuesday.setText(foodDetailInfo[14]);
        tv_wednesday.setText(foodDetailInfo[15]);
        tv_thursday.setText(foodDetailInfo[16]);
        tv_friday.setText(foodDetailInfo[17]);
        tv_saturday.setText(foodDetailInfo[18]);
        tv_sunday.setText(foodDetailInfo[19]);

        tv_cuisine_1 = view.findViewById(R.id.tv_cuisine_1);
        if (!foodDetailInfo[1].equals("null")) {
            tv_cuisine_1.setVisibility(View.VISIBLE);
            tv_cuisine_1.setText(foodDetailInfo[1]);
        }
        tv_cuisine_2 = view.findViewById(R.id.tv_cuisine_2);
        if (!foodDetailInfo[2].equals("null")) {
            tv_cuisine_2.setVisibility(View.VISIBLE);
            tv_cuisine_2.setText(foodDetailInfo[2]);
        }
        tv_cuisine_3 = view.findViewById(R.id.tv_cuisine_3);
        if (!foodDetailInfo[3].equals("null")) {
            tv_cuisine_3.setVisibility(View.VISIBLE);
            tv_cuisine_3.setText(foodDetailInfo[3]);
        }

        tv_rest_get_direction = view.findViewById(R.id.tv_rest_get_direction);
        if (foodDetailInfo[2] != null) {
            tv_rest_get_direction.setOnClickListener(this);
        }

        tv_call_now = view.findViewById(R.id.tv_call_now);
        tv_call_now.setOnClickListener(this);

        //additional details
        imgv_tick_halal = view.findViewById(R.id.imgv_tick_halal);
        if (!foodDetailInfo[9].equals("1")) {
            imgv_tick_halal.setImageResource(R.drawable.ic_red_cross_40px);
        }
        imgv_tick_wifi = view.findViewById(R.id.imgv_tick_wifi);
        if (!foodDetailInfo[10].equals("1")) {
            imgv_tick_wifi.setImageResource(R.drawable.ic_red_cross_40px);
        }
        imgv_tick_alcohol = view.findViewById(R.id.imgv_tick_alcohol);
        if (!foodDetailInfo[11].equals("1")) {
            imgv_tick_alcohol.setImageResource(R.drawable.ic_red_cross_40px);
        }
        imgv_tick_smoking = view.findViewById(R.id.imgv_tick_smoking);
        if (!foodDetailInfo[12].equals("1")) {
            imgv_tick_smoking.setImageResource(R.drawable.ic_red_cross_40px);
        }

        ImageView tv_rest_map = view.findViewById(R.id.tv_rest_map);//Location
        tv_rest_map.setOnClickListener(this);

        String mapURL = new MiscHelper().getGmapsStaticURL(foodDetailInfo[20], foodDetailInfo[21]);

        //operating hours - open now
        switch (FoodDetailLog.operatingHours) {
            case 0: //closed
                tv_rest_operation_hour.setText(R.string.food_closed);
                break;

            case 1: //open
                tv_rest_operation_hour.setText(R.string.food_open);
                break;

            default: //---
                tv_rest_operation_hour.setText("---");
                break;
        }

        //restaurant map
        GlideApp.with(this)
                .asBitmap()
                .load(mapURL)
                .placeholder(R.drawable.in_propic_circle_520px)
                .apply(RequestOptions.circleCropTransform())
                .transition(BitmapTransitionOptions.withCrossFade())
                .into(tv_rest_map);
        return view;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_rest_map: //restaurant map
                Intent intent = new Intent(getContext(), MapGps.class);

                //specify from
                intent.putExtra("from", "foodDetailInfo");

                intent.putExtra("latitude", foodDetailInfo[20]);
                intent.putExtra("longitude", foodDetailInfo[21]);
                intent.putExtra("placeName", FoodDetailLog.resTitle);
                intent.putExtra("placeAddress", foodDetailInfo[6]);

                getContext().startActivity(intent);
                break;

            case R.id.tv_rest_showall:
                if (food_det_op_hours.getVisibility() == View.VISIBLE) {
                    tv_rest_showall.setText("show more");
                    food_det_op_hours.setVisibility(View.GONE);

                } else {
                    tv_rest_showall.setText("show less");
                    food_det_op_hours.setVisibility(View.VISIBLE);

                }
                break;

            case R.id.tv_rest_get_direction:
                int[] point = new int[2];
                view.getLocationOnScreen(point);

                uiHelper.showNavigationPopup(getContext(), foodDetailInfo[20], foodDetailInfo[21],
                        point[0], point[1]);
                break;

            case R.id.tv_call_now:
                Runnable callIntent = () -> {
                    Intent intent1 = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", foodDetailInfo[7], null));
                    startActivity(intent1);
                };

                uiHelper.dialog2Btns(getContext(), null,
                        getString(R.string.contact_call_msg), R.string.ok_label, R.string.cancel, R.color.white, R.color.black,
                        callIntent, null, true);
                break;
        }
    }
}
