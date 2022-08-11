package com.soapp.food;

import android.os.Bundle;

import com.soapp.R;
import com.soapp.base.BaseActivity;

/* Created by Soapp on 18/02/2017. */

public class FoodTabActivity extends BaseActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.food_main_activity);
        setTitle(R.string.popup_share_res);
    }
}
