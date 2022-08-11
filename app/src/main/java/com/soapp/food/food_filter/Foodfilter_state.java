package com.soapp.food.food_filter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.soapp.R;
import com.soapp.global.Preferences;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/* Created by ash on 08/02/2018. */

public class Foodfilter_state extends Fragment {

    private static final Preferences preferences = Preferences.getInstance();
    private static final String TAG = "wtf";
    public static Spinner spinnerstate;
    public static Spinner spinnerlocation;
    View view;
    Context context;
    String[] locationString;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.food_filter_state, container, false);

        context = getContext();
        spinnerstate = view.findViewById(R.id.spinnerstate);
        spinnerlocation = view.findViewById(R.id.spinnerlocation);

        spinnerstate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (spinnerstate.getSelectedItem().toString().equals("Kuala Lumpur (KL)")) {
                    locationString = getResources().getStringArray(R.array.kl_location_arrays);
                    spinnerlocation.setVisibility(View.VISIBLE);
                } else if (spinnerstate.getSelectedItem().toString().equals("Selangor")) {
                    locationString = getResources().getStringArray(R.array.selangor_location_arrays);
                    spinnerlocation.setVisibility(View.VISIBLE);
                } else if (spinnerstate.getSelectedItem().toString().equals("Penang")) {
                    locationString = getResources().getStringArray(R.array.penang_location_arrays);
                    spinnerlocation.setVisibility(View.VISIBLE);
                } else if (spinnerstate.getSelectedItem().toString().equals("Malacca")) {
                    locationString = getResources().getStringArray(R.array.malacca_location_arrays);
                    spinnerlocation.setVisibility(View.VISIBLE);
                } else if (spinnerstate.getSelectedItem().toString().equals("Perak")) {
                    locationString = getResources().getStringArray(R.array.perak_location_arrays);
                    spinnerlocation.setVisibility(View.VISIBLE);
                } else if (spinnerstate.getSelectedItem().toString().equals("Johor")) {
                    locationString = getResources().getStringArray(R.array.johor_location_arrays);
                    spinnerlocation.setVisibility(View.VISIBLE);
                } else if (spinnerstate.getSelectedItem().toString().equals("Kedah")) {
                    spinnerlocation.setVisibility(View.VISIBLE);
                    locationString = getResources().getStringArray(R.array.kedah_location_arrays);
                } else if (spinnerstate.getSelectedItem().toString().equals("Kelantan")) {
                    spinnerlocation.setVisibility(View.VISIBLE);
                    locationString = getResources().getStringArray(R.array.kelantan_location_arrays);
                } else if (spinnerstate.getSelectedItem().toString().equals("Negeri Sembilan")) {
                    spinnerlocation.setVisibility(View.VISIBLE);
                    locationString = getResources().getStringArray(R.array.negerisembilan_location_arrays);
                } else if (spinnerstate.getSelectedItem().toString().equals("Pahang")) {
                    spinnerlocation.setVisibility(View.VISIBLE);
                    locationString = getResources().getStringArray(R.array.pahang_location_arrays);
                } else if (spinnerstate.getSelectedItem().toString().equals("Perlis")) {
                    spinnerlocation.setVisibility(View.VISIBLE);
                    locationString = getResources().getStringArray(R.array.perlis_location_arrays);
                } else if (spinnerstate.getSelectedItem().toString().equals("Sabah")) {
                    spinnerlocation.setVisibility(View.VISIBLE);
                    locationString = getResources().getStringArray(R.array.sabah_location_arrays);
                } else if (spinnerstate.getSelectedItem().toString().equals("Sarawak")) {
                    spinnerlocation.setVisibility(View.VISIBLE);
                    locationString = getResources().getStringArray(R.array.sarawak_location_arrays);
                } else if (spinnerstate.getSelectedItem().toString().equals("Terengganu")) {
                    locationString = getResources().getStringArray(R.array.terengganu_location_arrays);
                    spinnerlocation.setVisibility(View.VISIBLE);

                } else if (spinnerstate.getSelectedItem().toString().equals("Please select State")) {
                    spinnerlocation.setClickable(false);
                    locationString = new String[0];
                    spinnerstate.findFocus();
                    spinnerlocation.setVisibility(View.GONE);

                }

                ArrayAdapter<String> ary = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, locationString);
                ary.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerlocation.setAdapter(ary);

                if (spinnerstate.getSelectedItemId() > 0) {
                    preferences.save(getContext(), "state", "" + spinnerstate.getSelectedItemId());
                }
//                try {
                String loc = preferences.getValue(getContext(), "location");
                int locInt;

                if (loc.equals("nil")) {
                    locInt = 0;
                } else {
                    locInt = Integer.parseInt(loc);
                }
                if (locInt > 0) {
                    spinnerlocation.setSelection(locInt);
                }
//                } catch (IndexOutOfBoundsException e) {
//                    spinnerlocation.setSelection(0);
//                }

                FoodFilter.state = spinnerstate.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinnerlocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                preferences.save(getContext(), "location", "" + spinnerstate.getSelectedItemId());

                FoodFilter.location = spinnerlocation.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //set state spinner based on preferences value
        String state = preferences.getValue(getContext(), "state");
        int stateInt;
        if (state.equals("nil")) {
            stateInt = 0;
        } else {
            stateInt = Integer.parseInt(state);
        }
        if (stateInt >= 0) {
            spinnerstate.setSelection(stateInt);
        }

        //set location spinner based on preferences value
        String prefLoc = preferences.getValue(getContext(), "location");

        if (prefLoc.equals("nil")) { //first time ever, no pref saved yet
            spinnerlocation.setClickable(false);
            locationString = new String[0];
            spinnerstate.findFocus();
            spinnerlocation.setVisibility(View.GONE);
        } else { //got pref int
            int prefLocInt = Integer.parseInt(prefLoc);
            spinnerlocation.setVisibility(View.VISIBLE);

            switch (prefLocInt) {
                case 1: //Kuala Lumpur (KL)
                    locationString = getResources().getStringArray(R.array.kl_location_arrays);
                    break;

                case 2: //Selangor
                    locationString = getResources().getStringArray(R.array.selangor_location_arrays);
                    break;

                case 3: //Penang
                    locationString = getResources().getStringArray(R.array.penang_location_arrays);
                    break;

                case 4: //Malacca
                    locationString = getResources().getStringArray(R.array.malacca_location_arrays);
                    break;

                case 5: //Perak
                    locationString = getResources().getStringArray(R.array.perak_location_arrays);
                    break;

                case 6: //Johor
                    locationString = getResources().getStringArray(R.array.johor_location_arrays);
                    break;

                case 7: //Kedah
                    locationString = getResources().getStringArray(R.array.kedah_location_arrays);
                    break;

                case 8: //Kelantan
                    locationString = getResources().getStringArray(R.array.kelantan_location_arrays);
                    break;

                case 9: //Negeri Sembilan
                    locationString = getResources().getStringArray(R.array.negerisembilan_location_arrays);
                    break;

                case 10: //Pahang
                    locationString = getResources().getStringArray(R.array.pahang_location_arrays);
                    break;

                case 11: //Perlis
                    locationString = getResources().getStringArray(R.array.perlis_location_arrays);
                    break;

                case 12: //Sabah
                    locationString = getResources().getStringArray(R.array.sabah_location_arrays);
                    break;

                case 13: //Sarawak
                    locationString = getResources().getStringArray(R.array.sarawak_location_arrays);
                    break;

                case 14: //Terengganu
                    locationString = getResources().getStringArray(R.array.terengganu_location_arrays);
                    break;

                default: //no state selected
                    spinnerlocation.setClickable(false);
                    locationString = new String[0];
                    spinnerstate.findFocus();
                    spinnerlocation.setVisibility(View.GONE);
                    break;
            }

            //set array for location spinner based on state pref
            ArrayAdapter<String> ary = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, locationString);
            ary.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerlocation.setAdapter(ary);

            //set selection for location spinner after set array
//            try {
            String loc = preferences.getValue(getContext(), "location");
            int locInt;

            if (loc.equals("nil")) {
                locInt = 0;
            } else {
                locInt = Integer.parseInt(loc);
            }
            if (locInt > 0) {
                spinnerlocation.setSelection(locInt);
            }
        }
        return view;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }
}
