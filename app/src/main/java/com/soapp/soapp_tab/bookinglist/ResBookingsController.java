package com.soapp.soapp_tab.bookinglist;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.food.food_nearby.FoodNearbyActivity;
import com.soapp.global.GlobalVariables;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;

import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/* Created by chang on 05/08/2017. */

public class ResBookingsController extends BaseActivity implements View.OnClickListener, SearchView.OnQueryTextListener {
    //basics
    private UIHelper uiHelper = new UIHelper();

    BookingListVM listVM;
    private RecyclerView mRecyclerView;
    private ResBookingsAdapter mAdapter;
    private LinearLayout ll_start_booking;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.soapp_mybooking_list);
        setupToolbar();

        new UIHelper().setStatusBarColor(this, false , R.color.primaryDark4);
        mRecyclerView = findViewById(R.id.my_bookings_rv);
        ll_start_booking = findViewById(R.id.ll_start_booking);
        String selfJid = Preferences.getInstance().getValue(this, GlobalVariables.STRPREF_USER_ID);

        mAdapter = new ResBookingsAdapter();
        mAdapter.setHasStableIds(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setItemAnimator(null);


        listVM = ViewModelProviders.of(this).get(BookingListVM.class);
        listVM.setJid(selfJid);
        listVM.setResID(getIntent().getStringExtra("resid"));
        listVM.init();
        listVM.addSource();
        listVM.getBookList().observe(this, bookingLists -> {
            mAdapter.submitList(bookingLists);
            if (bookingLists != null) {
                if (bookingLists.size() == 0) {
                    ll_start_booking.setVisibility(View.VISIBLE);
                    ll_start_booking.setOnClickListener(this);
                } else {
                    ll_start_booking.setVisibility(View.GONE);
                }
            }
        });
        listVM.getIsSearching().observe(this, aBoolean -> listVM.init());
        listVM.getStringLiveData().observe(this, s -> listVM.init());

        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.res_booking, menu);

        MenuItem search = menu.findItem(R.id.action_res_booking_search);
        SearchView searchView = (SearchView) search.getActionView();

        search.setOnMenuItemClickListener(item -> {
            searchView.setIconified(false);
            return false;
        });

        searchView.setOnQueryTextListener(this);

        //for when searchbar open/closed
        searchView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) { //opened
            }

            @Override
            public void onViewDetachedFromWindow(View v) { //closed
                listVM.setIsSearching(false);
            }
        });
        return true;
    }


    //for search bar
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        if (newText.trim().length() == 0) { //no input
            //reset back to show original list
            listVM.setIsSearching(false);
        } else { //got input
            listVM.removeSource();
            listVM.setIsSearching(true);
            listVM.setStringLiveData(newText.toLowerCase());
        }

        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.ll_start_booking):
                Intent FavAct = new Intent(this, FoodNearbyActivity.class);
                startActivity(FavAct);
                break;

            default:
                break;
        }
    }
}
