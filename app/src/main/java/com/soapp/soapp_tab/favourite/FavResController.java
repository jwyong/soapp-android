package com.soapp.soapp_tab.favourite;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.food.food_nearby.FoodNearbyActivity;
import com.soapp.global.UIHelper;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/* Created by Ryan Soapp on 12/4/2017. */

public class FavResController extends BaseActivity implements View.OnClickListener {

    public static Activity activity;
    private RecyclerView mRecyclerView;
    private FavResAdapter mAdapter;
    private LinearLayout ll_start_fav;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        new UIHelper().setStatusBarColor(this , false , R.color.primaryDark4);
        setContentView(R.layout.soapp_fav_activity);

        //status bar color
        new UIHelper().setStatusBarColor(this, false , R.color.grey8);

        mRecyclerView = findViewById(R.id.rv_fav_food);
        ll_start_fav = findViewById(R.id.ll_start_fav);

        setupToolbar();
        setTitle(R.string.soapp_favourites);

        mAdapter = new FavResAdapter();
        mAdapter.setHasStableIds(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setItemAnimator(null);

        FavVM favVM = ViewModelProviders.of(this).get(FavVM.class);
        favVM.getResData().observe(this, restaurants -> {
            mAdapter.submitList(restaurants);
            if (restaurants != null && restaurants.size() == 0) {
                ll_start_fav.setVisibility(View.VISIBLE);
                ll_start_fav.setOnClickListener(this);
            } else {
                ll_start_fav.setVisibility(View.GONE);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.ll_start_fav):
                Intent FavAct = new Intent(this, FoodNearbyActivity.class);
                startActivity(FavAct);
                break;

            default:
                break;
        }
    }
}
