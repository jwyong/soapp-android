package com.soapp.food.food_hotspot_area;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.soapp.R;
import com.soapp.SoappApi.ApiModel.SearchNearbyRestaurant;
import com.soapp.SoappApi.Interface.RestaurantNearby;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.SoappModel.RestaurantModel;
import com.soapp.base.BaseActivity;
import com.soapp.food.food_filter.FoodFilter;
import com.soapp.global.GlobalVariables;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.sql.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.Gravity.CENTER;

/* Created by Soapp on 19/09/2017. */

public class
FoodHotspotActivity extends BaseActivity implements View.OnClickListener {

    public static Boolean fromFoodFilter = false;
    private final List<RestaurantModel> listget = new ArrayList<>();
    private List<RestaurantModel> list = new ArrayList<>();

    private final DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    Preferences preferences = Preferences.getInstance();
    int rowFrontend = 0, rowBackend = 0;
    String query1, query2, query3, type1, type2, type3;
    FoodHotspotHolder holder;
    private FoodHotspotAdapter mAdapter;
    private RecyclerView lv_food;
    private String userlat, userlong;
    private LinearLayout food_progress, food_gps, food_retry, food_loc_permission;
    private LinearLayoutManager llm;
    private ImageView scroll_top;

    public void onCreate(Bundle savedInstanceState) {

        new UIHelper().setStatusBarColor(this, true , R.color.white);

        //initiliaze additional query as empty to get location based first
        query1 = "";
        query2 = "";
        query3 = "";
        type1 = "";
        type2 = "";
        type3 = "";
        userlat = getIntent().getStringExtra("hotspot_lat");
        userlong = getIntent().getStringExtra("hotspot_Long");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.food_list);
        setupToolbar();

        TextView toolbar_title = findViewById(R.id.toolbar_title);
        ImageView location_icon = findViewById(R.id.location_icon);

//        ImageView backBtn = findViewById(R.id.backBtn);
//        backBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });

        location_icon.setVisibility(View.GONE);
        toolbar_title.setText(getIntent().getStringExtra("hotspot_Title"));
        toolbar_title.setGravity(CENTER);
        toolbar_title.setAllCaps(true);

        lv_food = findViewById(R.id.lv_food);
        food_progress = findViewById(R.id.food_progress);
        food_gps = findViewById(R.id.food_gps);
        food_retry = findViewById(R.id.food_retry);
        food_loc_permission = findViewById(R.id.food_Loc_permission);
        scroll_top = findViewById(R.id.scroll_top);

        ImageView action_food_nearby = findViewById(R.id.action_food_nearby);
        ImageView food_sorting_action = findViewById(R.id.food_sorting_action);

        //hide buttons for nearby and search
        action_food_nearby.setVisibility(View.GONE);
        food_sorting_action.setVisibility(View.GONE);

        //set toolbar title to center since hid buttons (TO REMOVE WHEN GOT BUTTONS)
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.setMarginEnd((int) (getResources().getDimension(R.dimen.toolbar_wth_statusbar)));
        toolbar_title.setLayoutParams(layoutParams);

        scroll_top.setOnClickListener(this);

        // get restaurantlist
        getRestaurantList();

    }

    //menuItem
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.food_sorting_action:
                Intent filter = new Intent(this, FoodFilter.class);
                filter.putExtra("lat", userlat);
                filter.putExtra("long", userlong);
                startActivityForResult(filter, 1);
                break;

            case R.id.scroll_top:
                lv_food.scrollToPosition(10);
                lv_food.smoothScrollToPosition(0);
                break;
        }
    }

    private void getRestaurantList() {
        food_retry.setVisibility(View.GONE);
        food_gps.setVisibility(View.GONE);
        food_loc_permission.setVisibility(View.GONE);
        lv_food.setVisibility(View.VISIBLE);

        rowFrontend = 0;
        rowBackend = 0;
        //prepare to post lat long to server
        final String access_token = preferences.getValue(FoodHotspotActivity.this, GlobalVariables.STRPREF_ACCESS_TOKEN);
        final SearchNearbyRestaurant model = new SearchNearbyRestaurant(userlat, userlong,
                "0", query1, query2, query3, type1, type2, type3);

        //build retrofit
        final RestaurantNearby client = RetrofitAPIClient.getClient().create(RestaurantNearby.class);
        final Call<List<SearchNearbyRestaurant>> call = client.nearbyRes(model, "Bearer " +
                access_token);

        call.enqueue(new Callback<List<SearchNearbyRestaurant>>() {
            @Override
            public void onResponse(Call<List<SearchNearbyRestaurant>> call, Response<List<SearchNearbyRestaurant>> response) {
                if (!response.isSuccessful()) {
                    new MiscHelper().retroLogUnsuc(response, "gethospotRestaurantList", "JAY");
                    Toast.makeText(FoodHotspotActivity.this, R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT).show();

                    if (food_progress != null && food_retry != null) {
                        food_progress.setVisibility(View.GONE);
                        food_retry.setVisibility(View.VISIBLE);
                        food_gps.setVisibility(View.GONE);
                        food_loc_permission.setVisibility(View.GONE);
                    }
                    return;
                }
                List<SearchNearbyRestaurant> list2 = response.body();

                int i;
                for (i = 0; i < list2.size(); i++) {
                    final String propic = list2.get(i).getPropic();
                    final String Latitude = list2.get(i).getLatitude();
                    final String Longitude = list2.get(i).getLongitude();
                    final String Name = list2.get(i).getName();
                    final String Location = list2.get(i).getLocation();
                    final String State = list2.get(i).getState();
                    final String rating = list2.get(i).getRating();
                    final String ownerJID = list2.get(i).getOwner_jid();
                    final String ID = list2.get(i).getBiz_id();
                    final String MainCuisine = list2.get(i).getMainCuisine();
                    final String video = list2.get(i).getVideo();

                    final int fav = databaseHelper.checkFavRestaurant(ID);

                    listget.add(new RestaurantModel(propic, Latitude, Longitude, Name, Location,
                            State, rating, ownerJID, ID, MainCuisine, video, fav));
                }
                list = listget;

                mAdapter = new FoodHotspotAdapter(FoodHotspotActivity.this, list);
                llm = new LinearLayoutManager(FoodHotspotActivity.this);
                llm.setOrientation(LinearLayoutManager.VERTICAL);


                //new - remember to set back to LLM if this doesnt work
                LinearLayoutManager llm = new LinearLayoutManager(FoodHotspotActivity.this);
                llm.setOrientation(LinearLayoutManager.VERTICAL);
                if (lv_food == null) {
                    lv_food = findViewById(R.id.lv_food);
                }
                lv_food.setLayoutManager(llm);

                if (food_progress != null) {
                    food_progress.setVisibility(View.GONE);
                }

                lv_food.setAdapter(mAdapter);

                lv_food.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);

                        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                        final int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                        final int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                        if (lastVisibleItemPosition == rowFrontend + 7) {
                            rowFrontend = rowFrontend + 10;
                            rowBackend = rowBackend + 10;

                            SearchNearbyRestaurant model = new SearchNearbyRestaurant(userlat,
                                    userlong, "" + rowBackend, query1, query2, query3, type1, type2, type3);

                            Call<List<SearchNearbyRestaurant>> call = client.nearbyRes(model, "Bearer " +
                                    access_token);

                            call.enqueue(new Callback<List<SearchNearbyRestaurant>>() {
                                @Override
                                public void onResponse(Call<List<SearchNearbyRestaurant>> call, Response<List<SearchNearbyRestaurant>> response) {
                                    if (!response.isSuccessful()) {
                                        new MiscHelper().retroLogUnsuc(response, "SearchNearbyRestaurant", "JAY");
                                        Toast.makeText(FoodHotspotActivity.this,
                                                R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    List<SearchNearbyRestaurant> list3 = response.body();
                                    int i;
                                    for (i = 0; i < list3.size(); i++) {
                                        final String propic = list3.get(i).getPropic();
                                        final String Latitude = list3.get(i).getLatitude();
                                        final String Longitude = list3.get(i).getLongitude();
                                        final String Name = list3.get(i).getName();
                                        final String Location = list3.get(i).getLocation();
                                        final String State = list3.get(i).getState();
                                        final String rating = list3.get(i).getRating();
                                        final String ownerJID = list3.get(i).getOwner_jid();
                                        final String ID = list3.get(i).getBiz_id();
                                        final String MainCuisine = list3.get(i).getMainCuisine();
                                        final String video = list3.get(i).getVideo();

                                        final int fav = databaseHelper.checkFavRestaurant(ID);

                                        if (list != null) {
                                            list.add(new RestaurantModel(propic, Latitude,
                                                    Longitude, Name, Location,
                                                    State, rating, ownerJID, ID, MainCuisine,
                                                    video, fav));
                                        }

                                    }
                                    mAdapter.notifyItemRangeInserted(rowFrontend, rowFrontend + list3.size());
                                }

                                @Override
                                public void onFailure(Call<List<SearchNearbyRestaurant>> call, Throwable t) {
                                    new MiscHelper().retroLogFailure(t, "SearchNearbyRestaurant ", "JAY");
                                    Toast.makeText(FoodHotspotActivity.this, R.string.onfailure, Toast
                                            .LENGTH_SHORT).show();
                                }
                            });

                        }
                        if (scroll_top != null) {
                            if (firstVisibleItemPosition > 5) {
                                scroll_top.setVisibility(View.VISIBLE);
                            } else {
                                scroll_top.setVisibility(View.GONE);
                            }
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<List<SearchNearbyRestaurant>> call, Throwable t) {
                Toast.makeText(FoodHotspotActivity.this, R.string.onfailure, Toast.LENGTH_SHORT).show();
                new MiscHelper().retroLogFailure(t, "gethospotRestaurantList ", "JAY");

                if (food_progress != null && food_retry != null) {
                    food_progress.setVisibility(View.GONE);
                    food_retry.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        list = null;
        FoodFilter.savedState = 0;

        this.finishAndRemoveTask();

        super.onDestroy();
    }
}