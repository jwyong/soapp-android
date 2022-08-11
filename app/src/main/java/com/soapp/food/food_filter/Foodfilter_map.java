package com.soapp.food.food_filter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.soapp.R;
import com.soapp.global.Preferences;
import com.soapp.setup.Soapp;

import java.text.DecimalFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

/* Created by ash on 08/02/2018. */

public class Foodfilter_map extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "wtf";
    public static boolean firstTime = true;
    private final Preferences preferences = Preferences.getInstance();
    private View view;
    //    private Context context;
    private GoogleMap mMap;
    //    private MapSchedule mapSchedule;
    private PlaceAutocompleteFragment placeAutoComplete;
    //    private String places;
    private Marker goMarker;
    private double userlat;
    private double userlong;
    private MapView mapView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        if (savedInstanceState == null) {
        view = inflater.inflate(R.layout.food_filter_map, container, false);
//        context = Soapp.getInstance().getApplicationContext();

        placeAutoComplete = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete);
        placeAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
//                places = place.getName().toString();
                Marker marker = mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(String.valueOf(place.getName())));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 16.0f));
                marker.showInfoWindow();
                FoodFilter.latLng = place.getLatLng();

            }

            @Override
            public void onError(Status status) {
            }
        });
        //default set to my
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder().setCountry("MY").build();
        placeAutoComplete.setFilter(typeFilter);

        return view;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (firstTime) {
            String lat = preferences.getValue(Soapp.getInstance().getApplicationContext(), "userlat");
            String lon = preferences.getValue(Soapp.getInstance().getApplicationContext(), "userlong");

            if (lat.equals("nil")) {
                lat = "3.1553303";
                lon = "101.5954977";
            }
            userlat = Double.parseDouble(lat);
            userlong = Double.parseDouble(lon);
        } else {
            userlat = Double.parseDouble(preferences.getValue(Soapp.getInstance().getApplicationContext(), "savedLat"));
            userlong = Double.parseDouble(preferences.getValue(Soapp.getInstance().getApplicationContext(), "savedLong"));
        }
        FoodFilter.latLng = new LatLng(userlat, userlong);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mapView = view.findViewById(R.id.mapfilter);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);//when you already implement OnMapReadyCallback in your fragment
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(FoodFilter.latLng, 16.0f));
        MarkerOptions markerOptions = new MarkerOptions().position(FoodFilter.latLng);
        goMarker = mMap.addMarker(markerOptions);

        mMap.getUiSettings().setMapToolbarEnabled(false);
        setMyLocationTrueMap();
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {

            @Override
            public boolean onMyLocationButtonClick() {
                mMap.moveCamera(CameraUpdateFactory.zoomTo(6.0f));
                return false;
            }
        });
//        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//            @Override
//            public boolean onMarkerClick(Marker marker) {
//                FoodFilter.latLng = marker.getPosition();
//                places = marker.getTitle();
//                marker.showInfoWindow();
//                return true;
//            }
//        });
// from here
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                FoodFilter.latLng = marker.getPosition();
                return true;
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                FoodFilter.latLng = marker.getPosition();
//                places = marker.getTitle();
            }
        });
        mMap.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(PointOfInterest pointOfInterest) {
                if (goMarker != null) {
                    goMarker.remove();
                }
                Toast.makeText(getContext(), pointOfInterest.name, Toast.LENGTH_SHORT).show();
                FoodFilter.latLng = pointOfInterest.latLng;
//                places = pointOfInterest.name;
                MarkerOptions markerOptions = new MarkerOptions().position(pointOfInterest.latLng).title(pointOfInterest.name).draggable(true);
                goMarker = mMap.addMarker(markerOptions);
                goMarker.showInfoWindow();

            }
        });
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (goMarker != null) {
                    goMarker.remove();
                }
                FoodFilter.latLng = latLng;
                String latitude = String.valueOf(latLng.latitude);
                String longitude = String.valueOf(latLng.longitude);

                DecimalFormat df = new DecimalFormat("###.######");
                String LatStr = df.format(Double.parseDouble(latitude));
                String LongStr = df.format(Double.parseDouble(longitude));

                MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(LatStr +
                        ", " + LongStr);
                goMarker = mMap.addMarker(markerOptions);
                goMarker.showInfoWindow();
            }
        });

        mMap.setTrafficEnabled(true);

    }

    @Override
    public void onResume() {
        super.onResume();

        if (mMap != null) {
            setMyLocationTrueMap();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        setMyLocationFalseMap();
    }

    @Override
    public void onDestroy() {
        setMyLocationFalseMap();

        super.onDestroy();
    }

    void setMyLocationFalseMap() {
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(false);
    }

    void setMyLocationTrueMap() {
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }
}
