package com.soapp.food.food_filter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;

import com.google.android.gms.maps.model.LatLng;
import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.food.food_nearby.FoodNearbyActivity;
import com.soapp.global.Preferences;
import com.soapp.home.SyncTabAdapter;
import com.soapp.setup.Soapp;

import androidx.viewpager.widget.ViewPager;

/* Created by ash on 22/01/2018. */

public class FoodFilter extends BaseActivity {

    private static final String TAG = "wtf";
    public static int pagePosition = 0;
    public static int savedState = 0;
    public static LatLng latLng;
    public static String location;
    public static String state;
    Preferences preferences = Preferences.getInstance();
    Switch advancesearch;
    ViewPager viewPager;
    Button btnFilterCfm;
    Spinner cuisineSpinner;
    String cuisineValue;
    ImageButton btnFilterClear;
    private ImageButton foodfilter_arrowRight;
    private ImageButton foodfilter_arrowLeft;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.food_filter_activity);
        setupToolbar();
        setTitle(R.string.filterandb);

        location = "";
        viewPager = findViewById(R.id.viewpager);
        btnFilterCfm = findViewById(R.id.filter_confirm);
        cuisineSpinner = findViewById(R.id.spinnercuisine);
        btnFilterClear = findViewById(R.id.sort_clear);

        setupViewPager(viewPager);

        String userLat = preferences.getValue(Soapp.getInstance().getApplicationContext(), "userlat");
        String userLong = preferences.getValue(Soapp.getInstance().getApplicationContext(), "userlong");

        if (userLat.equals("nil")) { //use Soapp's long lat if no value
            userLat = "3.1553303";
            userLong = "101.5954977";
        }
        latLng = new LatLng(Double.parseDouble(userLat), Double.parseDouble(userLong));

        advancesearch = findViewById(R.id.switch_advance_search);
        advancesearch.setChecked(true);

//        if (savedState == 0) {
//            advancesearch.setChecked(false);
//        } else {
        if (advancesearch.isChecked()) {
            savedState = 1;
            viewPager.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.VISIBLE);
        }
//        }

        //check the current state before we display the screen
        if (!advancesearch.isChecked()) {
            viewPager.setVisibility(View.INVISIBLE);
        }

        advancesearch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    savedState = 1;
                    viewPager.setVisibility(View.VISIBLE);

                } else {
                    savedState = 0;
                    viewPager.setVisibility(View.INVISIBLE);
                }
            }
        });

        btnFilterCfm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goBack = new Intent(FoodFilter.this, FoodNearbyActivity.class);

                goBack.putExtra("cuisineValue", cuisineValue);

                if (pagePosition == 1) {
                    goBack.putExtra("state", state);
                    goBack.putExtra("location", location);

                } else {
                    goBack.putExtra("usemap", "true");
                    goBack.putExtra("lat", String.valueOf(latLng.latitude));
                    goBack.putExtra("long", String.valueOf(latLng.longitude));

                    preferences.save(Soapp.getInstance().getApplicationContext(), "savedLat", String.valueOf(latLng.latitude));
                    preferences.save(Soapp.getInstance().getApplicationContext(), "savedLong", String.valueOf(latLng.longitude));
                    Foodfilter_map.firstTime = false;
                }

                setResult(5,goBack);
                finish();
            }
        });

        cuisineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (cuisineSpinner.getSelectedItemId() == 0) {
                    cuisineValue = "";
                } else {
                    cuisineValue = cuisineSpinner.getSelectedItem().toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                cuisineValue = "";
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                pagePosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (pagePosition == 1) {
            viewPager.setCurrentItem(1);
        } else {
            viewPager.setCurrentItem(0);
        }

        btnFilterClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cuisineSpinner.setSelection(0);
                Foodfilter_state.spinnerlocation.setSelection(0);
                Foodfilter_state.spinnerstate.setSelection(0);
                preferences.save(Soapp.getInstance().getApplicationContext(), "state", "0");
                preferences.save(Soapp.getInstance().getApplicationContext(), "location", "0");
            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        SyncTabAdapter adapter = new SyncTabAdapter(this.getSupportFragmentManager());

        adapter.addFragment(new Foodfilter_map(), null);
        adapter.addFragment(new Foodfilter_state(), null);

        viewPager.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        FoodNearbyActivity.fromFoodFilter = false;
    }
}
