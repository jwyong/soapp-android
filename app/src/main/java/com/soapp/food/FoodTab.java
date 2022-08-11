package com.soapp.food;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.soapp.R;
import com.soapp.food.food_filter.FoodFilter;
import com.soapp.food.food_hotspot_area.FoodHotspotActivity;
import com.soapp.food.food_nearby.FoodNearbyActivity;
import com.soapp.global.DateTime.VerticalTextView;
import com.soapp.global.UIHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/* Created by Ryan Soapp on 11/29/2017. */

public class FoodTab extends Fragment {
    public UIHelper uiHelper = new UIHelper();
    View rootView;
    RecyclerView F_Promo_View_Recyc;

    CardView btn_nearby, food_hotspot_1, food_hotspot_2, food_hotspot_3, food_hotspot_4;
    String hotspot_Lat[] = {"3.138154", "3.073249", "3.034657", "3.114131"};
    String hotspot_Long[] = {"101.621112", "101.595873", "101.769243", "101.621660"};
    String hotspot_Title[] = {"Damansara Uptown", "Sunway", "Mahkota Cheras", "Taman Sea"};
    private String userlat, userlong;
    private VerticalTextView verticalTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.food_tab_fragment, container, false);

            verticalTextView = rootView.findViewById(R.id.verticalTextView);
            Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Quicksand_Bold.otf");
            verticalTextView.setTypeface(typeface);

            // setting the statusbar
            ImageView statusBar = rootView.findViewById(R.id.statusBar);
            ConstraintLayout.LayoutParams statusParams =
                    new ConstraintLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            new UIHelper().getStatusBarHeight(getActivity()));
            statusBar.setLayoutParams(statusParams);
//            statusBar.setBackgroundColor(getResources().getColor(R.color.grey8));
            //** end setting statusbar **//

            ImageView food_sorting_action = rootView.findViewById(R.id.food_sorting_action);
            food_sorting_action.setVisibility(View.GONE);
            food_sorting_action.setOnClickListener(view -> {

                Intent filter = new Intent(getActivity(), FoodFilter.class);

                filter.putExtra("lat", userlat);
                filter.putExtra("long", userlong);
                startActivityForResult(filter, 1);

            });

            btn_nearby = rootView.findViewById(R.id.btn_nearby);

            btn_nearby.setOnClickListener(view -> {
                Intent FavAct = new Intent(getActivity(), FoodNearbyActivity.class);
                startActivity(FavAct);
            });

            food_hotspot_1 = rootView.findViewById(R.id.food_hotspot_1);
            food_hotspot_1.setOnClickListener(view -> {
                Intent Uptown = new Intent(getActivity(), FoodHotspotActivity.class);
                Uptown.putExtra("hotspot_lat", hotspot_Lat[0]);
                Uptown.putExtra("hotspot_Long", hotspot_Long[0]);
                Uptown.putExtra("hotspot_Title", hotspot_Title[0]);
                startActivity(Uptown);
            });

            food_hotspot_2 = rootView.findViewById(R.id.food_hotspot_2);
            food_hotspot_2.setOnClickListener(view -> {
                Intent Sunway = new Intent(getActivity(), FoodHotspotActivity.class);
                Sunway.putExtra("hotspot_lat", hotspot_Lat[1]);
                Sunway.putExtra("hotspot_Long", hotspot_Long[1]);
                Sunway.putExtra("hotspot_Title", hotspot_Title[1]);
                startActivity(Sunway);
            });

            food_hotspot_3 = rootView.findViewById(R.id.food_hotspot_3);
            food_hotspot_3.setOnClickListener(view -> {
                Intent Mahkota = new Intent(getActivity(), FoodHotspotActivity.class);
                Mahkota.putExtra("hotspot_lat", hotspot_Lat[2]);
                Mahkota.putExtra("hotspot_Long", hotspot_Long[2]);
                Mahkota.putExtra("hotspot_Title", hotspot_Title[2]);
                startActivity(Mahkota);
            });

            food_hotspot_4 = rootView.findViewById(R.id.food_hotspot_4);
            food_hotspot_4.setOnClickListener(view -> {
                Intent Tamansea = new Intent(getActivity(), FoodHotspotActivity.class);
                Tamansea.putExtra("hotspot_lat", hotspot_Lat[3]);
                Tamansea.putExtra("hotspot_Long", hotspot_Long[3]);
                Tamansea.putExtra("hotspot_Title", hotspot_Title[3]);
                startActivity(Tamansea);
            });


            F_Promo_View_Recyc = rootView.findViewById(R.id.F_Promo_View_Recyc);
            FoodTabAdapter adapter = new FoodTabAdapter(getContext());
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

            F_Promo_View_Recyc.setLayoutManager(linearLayoutManager);
            F_Promo_View_Recyc.setAdapter(adapter);
        }
        return rootView;
    }

}
