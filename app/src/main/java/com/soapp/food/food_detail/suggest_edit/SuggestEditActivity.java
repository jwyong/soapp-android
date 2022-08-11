package com.soapp.food.food_detail.suggest_edit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.soapp.R;
import com.soapp.SoappApi.ApiModel.ResSuggestEditInfoModel;
import com.soapp.SoappApi.Interface.RestaurantEdit;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.base.BaseActivity;
import com.soapp.food.food_detail.FoodDetailLog;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.GlobalVariables;
import com.soapp.global.MapGps;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.setup.Soapp;

import java.text.DecimalFormat;
import java.util.List;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SuggestEditActivity extends BaseActivity implements OnClickListener, OnMapReadyCallback, LocationListener {

    public static final Preferences preferences = Preferences.getInstance();
    final int RESULTS_LOCATION = 1;
    EditText name_etxt, cuisine1_etxt, cuisine2_etxt, cuisine3_etxt, mall_etxt, address_etxt, phonenumber_etxt, postcode_etxt, state_etxt, tue_etxt, wed_etxt, thu_etxt, fri_etxt, sat_etxt, sun_etxt;
    CheckBox alcohol_cbox, wifi_cbox, halal_cbox, outdoor_cbox;
    String resId = "", name = "", description = "", cuisine = "", cuisine1 = "", cuisine2 = "", cuisine3 = "", PriceList = "", address = "", phonenumber = "", postcode, mon = "", tue = "", wed = "", thu = "", fri = "", sat = "", sun = "", alcohol = "", wifi = "", halal = "", outdoor = "";
    String editName, editDesc, editPrice, editMall, editAddress, editPhonenumber, editPostcode, editMon, editTue, editWed, editThu, editFri, editSat, editSun, editAlcohol, editWifi, editHalal, editOutdoor;
    Button confirm_btn;
    Spinner mon_spinner1;
    Spinner mon_spinner2;
    Spinner spinnercuisine;
    Spinner spinnercuisine1;
    Spinner spinnercuisine2;
    Spinner tue_spinner1;
    Spinner tue_spinner2;
    Spinner wed_spinner1;
    Spinner wed_spinner2;
    Spinner thur_spinner1;
    Spinner thur_spinner2;
    Spinner fri_spinner1;
    Spinner fri_spinner2;
    Spinner sat_spinner1;
    Spinner sat_spinner2;
    Spinner sun_spinner1;
    Spinner sun_spinner2;
    Spinner spinnerPricelist;
    Spinner spinnermall;
    AutoCompleteTextView testingforautocomplete_actv;
    TextView desc_etxt, locationID;
    ImageView SuggestEditfood_mapIV;
    LatLng latLng;
    double longitude, latitude;
    LocationManager locationManager;
    Location location;
    String LocationName;
    boolean isGpsEnabled;
    boolean isNetworkEnabled;
    CardView cardView;
    TextView locationtext;
    AutoCompleteTextView autocomplete;
    String[] malllists;
    private int layout;
    final PopupWindow popup = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT);
    private GoogleMap mMap;
    private PlaceAutocompleteFragment placeAutoComplete;
    //    private MapView mapView , mapfilter ;
    private Marker goMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.food_detail_suggest_edit);
        autocomplete = findViewById(R.id.testinggforautocomplete_actv);
        Resources res = getResources();
        String[] malllists = res.getStringArray(R.array.malllists);
        autocomplete.setThreshold(1);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.select_dialog_item, malllists);
        autocomplete.setAdapter(adapter);


        setupToolbar();
        setTitle("Edit Info");
        //edit text
        name_etxt = findViewById(R.id.name_etxt);
        desc_etxt = findViewById(R.id.desc_etxt);
        desc_etxt = findViewById(R.id.desc_etxt);
//        price_etxt = findViewById(R.id.price_etxt); ( turn to spinner already)
//        mall_etxt = findViewById(R.id.mall_etxt); 14/3/2018
//        address_etxt = findViewById(R.id.address_etxt);
        phonenumber_etxt = findViewById(R.id.phonenumber_etxt);
//        postcode_etxt = (EditText) findViewById(R.id.postcode_etxt);
//        postcode_etxt.setVisibility(View.GONE);
//        state_etxt = (EditText) findViewById(R.id.state_etxt);
//        state_etxt.setVisibility(View.GONE);
        mon_spinner1 = findViewById(R.id.mon_spinner1);
        mon_spinner2 = findViewById(R.id.mon_spinner2);
        tue_spinner1 = findViewById(R.id.tue_spinner1);
        tue_spinner2 = findViewById(R.id.tue_spinner2);
        wed_spinner1 = findViewById(R.id.wed_spinner1);
        wed_spinner2 = findViewById(R.id.wed_spinner2);
        thur_spinner1 = findViewById(R.id.thur_spinner1);
        thur_spinner2 = findViewById(R.id.thur_spinner2);
        fri_spinner1 = findViewById(R.id.fri_spinner1);
        fri_spinner2 = findViewById(R.id.fri_spinner2);
        sat_spinner1 = findViewById(R.id.sat_spinner1);
        sat_spinner2 = findViewById(R.id.sat_spinner2);
        sun_spinner1 = findViewById(R.id.sun_spinner1);
        sun_spinner2 = findViewById(R.id.sun_spinner2);
        spinnercuisine = findViewById(R.id.spinnercuisine);
        spinnercuisine1 = findViewById(R.id.spinnercuisine1);
        spinnercuisine2 = findViewById(R.id.spinnercuisine2);
        spinnerPricelist = findViewById(R.id.spinnerPricelist);
//        spinnermall = findViewById(R.id.spinnermall); 15/3
//        mapfilter = findViewById(R.id.mapfilter); 15/3
        cardView = findViewById(R.id.cardView);
        locationtext = findViewById(R.id.locationtext);
        SuggestEditfood_mapIV = findViewById(R.id.res_map);


        SuggestEditfood_mapIV.setClickable(true);

        cardView.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Intent ShareMap = new Intent(SuggestEditActivity.this, MapGps.class);
//                boolean bottle = false;
                startActivity(ShareMap);
                popup.dismiss();
            }
        });


//         this is for the clickable imageview > bring to the editlocationmap
        SuggestEditfood_mapIV.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent EditLocationMap = new Intent(SuggestEditActivity.this, MapGps.class);

                startActivity(EditLocationMap);
            }

        });

        String mapURL = new MiscHelper().getGmapsStaticURL(FoodDetailLog.foodDetailInfo[20], FoodDetailLog.foodDetailInfo[21]);
        GlideApp.with(SuggestEditActivity.this)
                .asBitmap()
                .load(mapURL)
                .placeholder(R.drawable.ic_def_location_loading_400px)
                .into(SuggestEditfood_mapIV);


//        State
        //checkbox
        alcohol_cbox = findViewById(R.id.alcohol_cbox);
        wifi_cbox = findViewById(R.id.wifi_cbox);
        halal_cbox = findViewById(R.id.halal_cbox);
        outdoor_cbox = findViewById(R.id.outdoor_cbox);
        //button
        confirm_btn = findViewById(R.id.confirm_btn);
        confirm_btn.setOnClickListener(this);
        Intent in = getIntent();


        if (in.hasExtra("resId")) {
            resId = in.getStringExtra("resId");
        }

        if (in.hasExtra("Name")) {
            name = in.getStringExtra("Name");
            name_etxt.setText(name);
        }

        if (in.hasExtra("Description")) {
            description = in.getStringExtra("Description");
            desc_etxt.setText(description);
        }

        if (in.hasExtra("PhoneNumber")) {
            phonenumber = in.getStringExtra("PhoneNumber");
            phonenumber_etxt.setText(phonenumber);
        }
        //spinner
        if (in.hasExtra("Mon")) {
            mon = in.getStringExtra("Mon");

            mon_spinner1.setSelection(0);
        }
        if (in.hasExtra("Tue")) {
            tue = in.getStringExtra("Tue");
            tue_spinner2.setSelection(0);
        }
//
        if (in.hasExtra("Wed")) {
            wed = in.getStringExtra("Wed");
            wed_spinner1.setSelection(0);
        }
        if (in.hasExtra("Thu")) {
            thu = in.getStringExtra("Thu");
            thur_spinner1.setSelection(0);
        }
        if (in.hasExtra("Fri")) {
            fri = in.getStringExtra("Fri");
            fri_spinner1.setSelection(0);
        }
        if (in.hasExtra("Sat")) {
            sat = in.getStringExtra("Sat");
            sat_spinner1.setSelection(0);
        }
        if (in.hasExtra("Sun")) {
            sun = in.getStringExtra("Sun");
            sun_spinner1.setSelection(0);
        }

        if (in.hasExtra("spinnerPricelist")) {
            PriceList = in.getStringExtra("spinnerPricelist");
            spinnerPricelist.setSelection(0);
        }


        if (in.hasExtra("Alcohol")) {
            alcohol = in.getStringExtra("Alcohol");
            if (alcohol.equals("Yes")) {
                alcohol_cbox.setChecked(true);
            }
        }
        if (in.hasExtra("Wifi")) {
            wifi = in.getStringExtra("Wifi");
            if (wifi.equals("Yes")) {
                wifi_cbox.setChecked(true);
            }
        }
        if (in.hasExtra("Halal")) {
            halal = in.getStringExtra("Halal");
            if (halal.equals("Yes")) {
                halal_cbox.setChecked(true);
            }
        }
        if (in.hasExtra("Outdoor")) {
            outdoor = in.getStringExtra("Outdoor");
            if (outdoor.equals("Yes")) {
                outdoor_cbox.setChecked(true);
            }
        }

        try {


            latLng = new LatLng(Double.parseDouble(in.getStringExtra("lat")), Double.parseDouble(in.getStringExtra("long")));
        } catch (Exception e) {
        }

//        if place_autocomplete delete. application stops
        placeAutoComplete = (PlaceAutocompleteFragment) this.getFragmentManager().findFragmentById(R.id.place_autocomplete);
        placeAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
//                places = place.getName().toString();
                Marker marker = mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(String.valueOf(place.getName())));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 16.0f));
                marker.showInfoWindow();
                latLng = place.getLatLng();

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        latLng = marker.getPosition();
                        return true;
                    }
                });
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        latLng = marker.getPosition();
//                places = marker.getTitle();
                    }
                });
                mMap.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
                    @Override
                    public void onPoiClick(PointOfInterest pointOfInterest) {
                        if (goMarker != null) {
                            goMarker.remove();
                        }
                        Toast.makeText(Soapp.getInstance(), pointOfInterest.name, Toast.LENGTH_SHORT).show();
                        latLng = pointOfInterest.latLng;
//                places = pointOfInterest.name;
                        MarkerOptions markerOptions = new MarkerOptions().position(pointOfInterest.latLng).title(pointOfInterest.name).draggable(false);
                        goMarker = mMap.addMarker(markerOptions);
                        goMarker.showInfoWindow();

                    }
                });

//not reflecting on the phone, cmd below.

                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        if (goMarker != null) {
                            goMarker.remove();
                        }
                        latLng = latLng;
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
            }


            @Override
            public void onError(Status status) {
            }
        });
        //default set to my
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder().setCountry("MY").build();
        placeAutoComplete.setFilter(typeFilter);

//using static image view to dislpay the current rest
//        mapView = this.findViewById(R.id.mapfilter);
//        mapView.onCreate(savedInstanceState);
//        mapView.onResume();
//        mapView.getMapAsync(this);//when you already implement OnMapReadyCallback in your fragment

//        locationtext.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                Intent ShareMap  = new Intent(SuggestEditActivity.this, MapShareLocation.class);
////                boolean bottle = false;
////                ShareMap.putExtra("Bottle", bottle);
//                startActivity(ShareMap);
//            }


//            SuggestEditfood_mapIV.setOnClickListener(new OnClickListener() {
//
//                public void onClick(View v) {
//                    Intent ShareMap = new Intent(SuggestEditActivity.this , MapShareLocation.class);
//                    startActivity(ShareMap);
//
//                }
//


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.confirm_btn:
                getTextFromEditField();
                checkIfEditFieldEmpty();
                sendResSuggestEditInfo();
                break;
            case R.id.alcohol_cbox:
                break;

            default:
                Toast.makeText(this, "default", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void checkIfEditFieldEmpty() {

        if (!name.equals(editName)) {
            name = editName;
        }

        if (!description.equals(editDesc)) {
            description = editDesc;
        }


//       from text changed to spinner already for this <-----------
//        if (!cuisine1.equals(editCuisine1)) {
//            cuisine1 = editCuisine1;
//        }
//
//        if (!cuisine2.equals(editCuisine2)) {
//            cuisine2 = editCuisine2;
//        }
//
//        if (!cuisine3.equals(editCuisine3)) {
//            cuisine3 = editCuisine3;
//        }

//        if (!price.equals(editPrice)) {
//            price = editPrice;
//        }

        if (!address.equals(editAddress)) {
            address = editAddress;
//        }
//
//        if (!mall.equals(editMall)) {
//            mall = editMall;
//        }

            if (!alcohol.equals(editAlcohol)) {
                alcohol = editAlcohol;
            }

            if (!phonenumber.equals(editPhonenumber)) {
                phonenumber = editPhonenumber;
            }

            if (!wifi.equals(editWifi)) {
                wifi = editWifi;
            }

            if (!outdoor.equals(editOutdoor)) {
                outdoor = editOutdoor;
            }

            if (!halal.equals(editHalal)) {
                halal = editHalal;
            }
//        from text changed to spinner already for this <-----------
//        if (!BussH_Spinner.equals(editMon)) {
//            mon = editMon;
//        }

//        if (!tue.equals(editTue)) {
//            tue = editTue;
//        }
//
//        if (!wed.equals(editWed)) {
//            wed = editWed;
//        }
//
//        if (!thu.equals(editThu)) {
//            thu = editThu;
//        }
//
//        if (!fri.equals(editFri)) {
//            fri = editFri;
//        }
//
//        if (!sat.equals(editSat)) {
//            sat = editSat;
//        }
//
//        if (!sun.equals(editSun)) {
//            sun = editSun;
//        }
        }
    }

    public void getTextFromEditField() {
        editName = name_etxt.getText().toString();
        editDesc = desc_etxt.getText().toString();
//        editCuisine1 = cuisine1_etxt.getText().toString();
//        editCuisine2 = cuisine2_etxt.getText().toString();
//        editCuisine3 = cuisine3_etxt.getText().toString();
//        editPrice = price_etxt.getText().toString();
//        editMall = mall_etxt.getText().toString();
        editAddress = address_etxt.getText().toString();
        if (alcohol_cbox.isChecked()) {
            editAlcohol = "1";
        } else {
            editAlcohol = "0";
        }
        editPhonenumber = phonenumber_etxt.getText().toString();
        if (wifi_cbox.isChecked()) {
            editWifi = "1";
        } else {
            editWifi = "0";
        }
        if (outdoor_cbox.isChecked()) {
            editOutdoor = "1";
        } else {
            editOutdoor = "0";
        }
        if (halal_cbox.isChecked()) {
            editHalal = "1";
        } else {
            editHalal = "0";
        }


//        from text changed to spinner already for this <-----------
//        editMon = BussH_Spinner.getText().toString();
//        editTue = tue_etxt.getText().toString();
//        editWed = wed_etxt.getText().toString();
//        editThu = thu_etxt.getText().toString();
//        editFri = fri_etxt.getText().toString();
//        editSat = sat_etxt.getText().toString();
//        editSun = sun_etxt.getText().toString();
    }

    public void sendResSuggestEditInfo() {
        final String access_token = preferences.getValue(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_ACCESS_TOKEN);
        final ResSuggestEditInfoModel model = new ResSuggestEditInfoModel(resId, name, cuisine1, PriceList, address, spinnermall, alcohol, phonenumber, wifi, outdoor, halal,
                mon, tue, wed, thu, fri, sat, sun);

        //build retrofit
        RestaurantEdit client = RetrofitAPIClient.getClient().create(RestaurantEdit.class);
        Call<List<ResSuggestEditInfoModel>> call = client.editRes(model, access_token);
        call.enqueue(new Callback<List<ResSuggestEditInfoModel>>() {
            @Override
            public void onResponse(Call<List<ResSuggestEditInfoModel>> call, Response<List<ResSuggestEditInfoModel>> response) {
                if (!response.isSuccessful()) {
                    new MiscHelper().retroLogUnsuc(response, "RestaurantEdit " , "JAY");
                    Toast.makeText(SuggestEditActivity.this, "onResponse fail", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(SuggestEditActivity.this, "onResponse success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<List<ResSuggestEditInfoModel>> call, Throwable t) {
                new MiscHelper().retroLogFailure(t , "RestaurantEdit" , "JAY");
                Toast.makeText(SuggestEditActivity.this, "onFailure", Toast.LENGTH_SHORT).show();
            }
        });
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        goMarker = mMap.addMarker(markerOptions);

        mMap.getUiSettings().setMapToolbarEnabled(false);

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {

            @Override
            public boolean onMyLocationButtonClick() {
                mMap.moveCamera(CameraUpdateFactory.zoomTo(6.0f));
                return false;
            }


            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.getUiSettings().setMapToolbarEnabled(false);
                int permissionFine = ActivityCompat.checkSelfPermission(SuggestEditActivity.this, Manifest
                        .permission.ACCESS_FINE_LOCATION);
                int permissionCoarse = ActivityCompat.checkSelfPermission(SuggestEditActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);

                if (permissionFine == PackageManager.PERMISSION_DENIED || permissionCoarse == PackageManager
                        .PERMISSION_DENIED) {
                    String[] PERMISSIONS_LOCATION = {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                    };
                    ActivityCompat.requestPermissions(SuggestEditActivity.this, PERMISSIONS_LOCATION, RESULTS_LOCATION);
                } else {

                    mMap.setMyLocationEnabled(true);
                    locationManager = (LocationManager) Soapp.getInstance().getApplicationContext()
                            .getSystemService(LOCATION_SERVICE);

                    isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

//                    if ( latitude == null || longitude;("")); {
                    LatLng myPosition = new LatLng(Double.doubleToLongBits(latitude), Double.doubleToLongBits(longitude));
                    MarkerOptions markerOptions = new MarkerOptions().position(myPosition).title(LocationName);
                    Marker marker = googleMap.addMarker(markerOptions);
                    marker.showInfoWindow();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 16.0f));
                }
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


//
//
//
// to get the current longitude and latitude

//        @Override
//        public void onLocationChanged(Location location); {
//            if (location != null) {
//                // Getting latitude of the current location
//                double latitude = location.getLatitude();
//
//                // Getting longitude of the current location
//                double longitude = location.getLongitude();
//
//                // Creating a LatLng object for the current location
//                LatLng myPosition = new LatLng(latitude, longitude);
//                latitude = Double.valueOf(myPosition.latitude);
//                longitude = Double.valueOf(myPosition.longitude);
//                mMap.addMarker(new MarkerOptions().position(myPosition).title("You are here"));
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 16.0f));
//                locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}





