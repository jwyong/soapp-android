package com.soapp.food.food_nearby;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.soapp.R;
import com.soapp.SoappApi.ApiModel.SearchNearbyRestaurant;
import com.soapp.SoappApi.Interface.RestaurantNearby;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.SoappModel.RestaurantModel;
import com.soapp.base.BaseActivity;
import com.soapp.food.food_filter.FoodFilter;
import com.soapp.food.food_filter.Foodfilter_state;
import com.soapp.global.GlobalVariables;
import com.soapp.global.MiscHelper;
import com.soapp.global.PermissionHelper;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.sql.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* Created by Soapp on 19/09/2017. */

public class FoodNearbyActivity extends BaseActivity implements View.OnClickListener {
    //basics
    private UIHelper uiHelper = new UIHelper();

    public static Boolean fromFoodFilter = false;
    private final int RESULTS_LOCATION = 1;
    private final List<RestaurantModel> listget = new ArrayList<>();
    private final DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    Preferences preferences = Preferences.getInstance();
    int rowFrontend = 0, rowBackend = 0;
    String query1, query2, query3, type1, type2, type3;
    FoodNearbyHolder holder;
    private List<RestaurantModel> list;
    private FoodNearbyAdapter mAdapter;
    private RecyclerView lv_food;
    private String userlat, userlong;
    private Location lastLocation;
    private LinearLayout food_progress, food_gps, food_retry, food_loc_permission;
    private LinearLayoutManager llm;
    private ImageView scroll_top;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private LocationSettingsStates locationSettingsStates;
    public PermissionHelper permissionHelper = new PermissionHelper();
    public LocationManager manager;
    public boolean hasLocationResultAlready = false;

    //google location setting
    private GoogleApiClient googleApiClient;

    public void onCreate(Bundle savedInstanceState) {

        new UIHelper().setStatusBarColor(this, true, R.color.white);

        //initiliaze additional query as empty to get location based first
        query1 = "";
        query2 = "";
        query3 = "";
        type1 = "";
        type2 = "";
        type3 = "";

        super.onCreate(savedInstanceState);

        setContentView(R.layout.food_list);
        setupToolbar();

        lv_food = findViewById(R.id.lv_food);
        food_progress = findViewById(R.id.food_progress);
        food_gps = findViewById(R.id.food_gps);
        food_retry = findViewById(R.id.food_retry);
        food_loc_permission = findViewById(R.id.food_Loc_permission);
        scroll_top = findViewById(R.id.scroll_top);

        ImageView action_food_nearby = findViewById(R.id.action_food_nearby);
        ImageView food_sorting_action = findViewById(R.id.food_sorting_action);

        food_gps.setOnClickListener(this);
        food_retry.setOnClickListener(this);
        food_loc_permission.setOnClickListener(this);
        action_food_nearby.setOnClickListener(this);
        food_sorting_action.setOnClickListener(this);

        food_progress.setVisibility(View.VISIBLE);
        food_retry.setVisibility(View.GONE);
        food_loc_permission.setVisibility(View.GONE);
        lv_food.setVisibility(View.GONE);

        int permissionFine = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
//        int permissionCoarse = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        manager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
//
//            food_progress.setVisibility(View.GONE);
//            food_gps.setVisibility(View.GONE);
//            lv_food.setVisibility(View.GONE);
//            food_loc_permission.setVisibility(View.VISIBLE);
////            // Call your Alert message
//            Runnable actionOpenSettings = new Runnable() {
//                @Override
//                public void run() {
//
//                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
////                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//                    intent.putExtra("enabled", true);
//                    sendBroadcast(intent);
//                }
//            };
//
//            getLocation();
//            Toast.makeText(this, "turn on ur GPS THx", Toast.LENGTH_SHORT).show();
//
//            uiHelper.dialog2Btns(this, getString(R.string.need_res_loc_title),
//                    getString(R.string.need_res_loc), R.string.open_settings, R.string
//                            .cancel, R.color.white, R.color.black, R.color.primaryDark3,
//                    R.color.white, actionOpenSettings, null, true);


        if (permissionFine != PackageManager.PERMISSION_GRANTED) {
            food_progress.setVisibility(View.GONE);
            food_gps.setVisibility(View.GONE);
            lv_food.setVisibility(View.GONE);
            food_loc_permission.setVisibility(View.VISIBLE);

            permissionHelper.CheckPermissions(this, 1003, R.string.permission_txt);

        } else {
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                food_progress.setVisibility(View.GONE);
                food_gps.setVisibility(View.GONE);
                lv_food.setVisibility(View.GONE);
                food_loc_permission.setVisibility(View.VISIBLE);

//                getLocation();
            }
            getLocation();
        }

//        if (googleApiClient == null) {
//            Log.d("jason", "get here ?");
//            googleApiClient = new GoogleApiClient.Builder(this)
//                    .addApi(LocationServices.API)
//                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
//                        @Override
//                        public void onConnected(Bundle bundle) {
//
//                        }
//
//                        @Override
//                        public void onConnectionSuspended(int i) {
//                            googleApiClient.connect();
//                        }
//                    })
//                    .addOnConnectionFailedListener(connectionResult -> Log.d("Location error", "Location error " + connectionResult.getErrorCode())).build();
//            googleApiClient.connect();
//
//            LocationRequest locationRequest = LocationRequest.create();
//            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//            locationRequest.setInterval(30 * 1000);
//            locationRequest.setFastestInterval(5 * 1000);
//            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
//                    .addLocationRequest(locationRequest);
//
//            builder.setAlwaysShow(true);
//
//            PendingResult<LocationSettingsResult> result =
//                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
//            result.setResultCallback(result1 -> {
//                final Status status = result1.getStatus();
//                switch (status.getStatusCode()) {
//                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                        try {
//                            // Show the dialog by calling startResolutionForResult(),
//                            // and check the result in onActivityResult().
//                            status.startResolutionForResult(FoodNearbyActivity.this, 199);
//
//                            finish();
//                        } catch (IntentSender.SendIntentException e) {
//                            // Ignore the error.
//                        }
//                        break;
//                }
//            });
//        }


//                Runnable actionOpenSettings = new Runnable() {
//                    @Override
//                    public void run() {

//                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//                        intent.putExtra("enabled", true);
//                        sendBroadcast(intent);
//                    }
//                };

//                new MapGps().checkGps();
//                uiHelper.dialog2Btns(this, getString(R.string.need_res_loc_title),
//                        getString(R.string.need_res_loc), R.string.open_settings, R.string
//                                .cancel, R.color.white, R.color.black, R.color.primaryDark3,
//                        R.color.white, actionOpenSettings, null, true);


//        getLocation();

//
//            if (permissionFine == PackageManager.PERMISSION_DENIED || permissionCoarse == PackageManager
//                    .PERMISSION_DENIED) {
//                //no permission to access user location
//                String[] PERMISSIONS_LOCATION = {
//                        Manifest.permission.ACCESS_FINE_LOCATION,
//                        Manifest.permission.ACCESS_COARSE_LOCATION,
//                };
//                requestPermissions(PERMISSIONS_LOCATION, RESULTS_LOCATION);
//
//            } else {//got permission
//
//                getLocation();
//            }
//        } else { //less than M, no need permission
//            getLocation();
//        }

        //new
        scroll_top.setOnClickListener(view -> {
            lv_food.scrollToPosition(10);
            lv_food.smoothScrollToPosition(0);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.action_food_nearby:
                hasLocationResultAlready = false;
                neabyButtonClick();
                break;

            case R.id.food_sorting_action:
                Intent filter = new Intent(this, FoodFilter.class);

                filter.putExtra("lat", userlat);
                filter.putExtra("long", userlong);
                startActivityForResult(filter, 1);
                break;

            case R.id.food_Loc_permission:

                if (PackageManager.PERMISSION_DENIED == FoodNearbyActivity.this.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    permissionHelper.CheckPermissions(FoodNearbyActivity.this, 1003, R.string.permission_txt);

                } else {
//                    if ((manager.isProviderEnabled(LocationManager.GPS_PROVIDER))) {
                    getLocation();
//                    }
                }

//                if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//
//                    Log.d("jason", "you gps is turn on");
//
//
//                } else {
//
//
//                    Log.d("jason", "you gps is turn off");
//
////                    Toast.makeText(this, "turn on ur GPS THx", Toast.LENGTH_SHORT).show();
//
//
//                }

//
//                if (PackageManager.PERMISSION_GRANTED == FoodNearbyActivity.this.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
//                    getLocation();
//                }
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

//                    new PermissionHelper().CheckPermissions(FoodNearbyActivity.this, 1003, R.string.permission_txt, dunAskagain);

//                    String[] PERMISSIONS_LOCATION = {
//                            Manifest.permission.ACCESS_FINE_LOCATION,
//                            Manifest.permission.ACCESS_COARSE_LOCATION,
//                    };
//                    requestPermissions(PERMISSIONS_LOCATION, RESULTS_LOCATION);
//                } else {
//                    neabyButtonClick();
//                }
                break;

            case R.id.food_retry:
                neabyButtonClick();
                break;

            case R.id.food_gps:
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                break;

            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        food_progress.setVisibility(View.GONE);
        food_gps.setVisibility(View.GONE);
        lv_food.setVisibility(View.GONE);
        food_loc_permission.setVisibility(View.VISIBLE);

        switch (requestCode) {
            case 1003:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean showRationale = false;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            showRationale = shouldShowRequestPermissionRationale(permission);

                        }
                        if (!showRationale) {
                            preferences.save(FoodNearbyActivity.this, "askAgain", String.valueOf(showRationale));
                            preferences.save(FoodNearbyActivity.this, "foodMap", "false");
                            break;
                        } else {
                            preferences.save(FoodNearbyActivity.this, "askAgain", String.valueOf(showRationale));
                        }
                    } else {

//                        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//
//                            Log.d("jason", "no open gps yet");
//
//                        } else {

                        getLocation();

//                        }

                        break;
                    }
                }

                break;
//
            case RESULTS_LOCATION: //location permissions
                boolean allow = true;
                for (int i = 0, len = permissions.length; i < len; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        allow = false;
                    }
                }
                if (allow) {
                    getLocation();
                } else { //no location permission, show no restaurant
                    Runnable actionOpenSettings = new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);

                            food_loc_permission.setVisibility(View.GONE);
                            food_retry.setVisibility(View.VISIBLE);
                        }
                    };

                    uiHelper.dialog2Btns(this, getString(R.string.need_res_loc_title),
                            getString(R.string.need_res_loc), R.string.open_settings, R.string
                                    .cancel, R.color.white, R.color.black,
                            actionOpenSettings, null, true);
                }
                break;

            default:
                break;
        }
    }

    private void neabyButtonClick() {
        if (food_progress != null && food_retry != null) {
            food_gps.setVisibility(View.GONE);
            food_retry.setVisibility(View.GONE);
            food_loc_permission.setVisibility(View.GONE);
            food_progress.setVisibility(View.VISIBLE);
        }
        query1 = "";
        query2 = "";
        query3 = "";
        type1 = "";
        type2 = "";
        type3 = "";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionFine = checkSelfPermission(Manifest.permission
                    .ACCESS_FINE_LOCATION);
            int permissionCoarse = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);

            if (permissionFine == PackageManager.PERMISSION_DENIED || permissionCoarse == PackageManager
                    .PERMISSION_DENIED) { //no permission to access user location
                String[] PERMISSIONS_LOCATION = {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                };
                requestPermissions(PERMISSIONS_LOCATION, RESULTS_LOCATION);

            } else { //got location permission
                getLocation();
            }
        } else {
            getLocation();
        }
    }

    private void getLocation() {

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            foodcheckGps();
        } else {

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            SettingsClient settingsClient = LocationServices.getSettingsClient(this);

            locationRequest = LocationRequest.create();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(5000);
            locationRequest.setNumUpdates(1);

            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(locationRequest);
            LocationSettingsRequest mLocationSettingsRequest = builder.build();

            final Task<LocationSettingsResponse> locationResponse = settingsClient.checkLocationSettings(mLocationSettingsRequest);

            locationResponse.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                @Override
                public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                    try {
                        LocationSettingsResponse response = task.getResult(ApiException.class);
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        locationSettingsStates = response.getLocationSettingsStates();
//                    checkForLocationAvaibility();

                        locationResponse.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {

                            @Override
                            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                                if (ActivityCompat.checkSelfPermission(FoodNearbyActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    return;
                                }

                                locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult result) {
                                        super.onLocationResult(result);
                                        if (lastLocation != null) {

                                            if (lastLocation.distanceTo(result.getLocations().get(0)) > 50) {
                                                lastLocation = result.getLocations().get(0);
                                            }
                                        } else {

                                            lastLocation = result.getLocations().get(0);
                                        }

                                        if (locationSettingsStates.isLocationUsable() && locationSettingsStates.isGpsUsable() && locationSettingsStates.isNetworkLocationUsable()) {
                                            preferences.save(FoodNearbyActivity.this, "userlat", String.valueOf(lastLocation.getLatitude()));
                                            preferences.save(FoodNearbyActivity.this, "userlong", String.valueOf(lastLocation.getLongitude()));

                                        } else if (locationSettingsStates.isLocationUsable() && locationSettingsStates.isGpsUsable()) {
                                            preferences.save(FoodNearbyActivity.this, "userlat", String.valueOf(lastLocation.getLatitude()));
                                            preferences.save(FoodNearbyActivity.this, "userlong", String.valueOf(lastLocation.getLongitude()));

                                            Toast.makeText(FoodNearbyActivity.this, getString(R.string.gps_accurate), Toast.LENGTH_SHORT).show();

                                        } else if (locationSettingsStates.isNetworkLocationPresent() && locationSettingsStates.isNetworkLocationUsable()) {
                                            preferences.save(FoodNearbyActivity.this, "userlat", String.valueOf(lastLocation.getLatitude()));
                                            preferences.save(FoodNearbyActivity.this, "userlong", String.valueOf(lastLocation.getLongitude()));

                                            Toast.makeText(FoodNearbyActivity.this, getString(R.string.gps_accurate), Toast.LENGTH_SHORT).show();
                                        }
                                        if (!hasLocationResultAlready) {
                                            hasLocationResultAlready = true;
                                            get_list();
                                        }

                                    }
                                };
                                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                            }
                        });

                        locationResponse.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                getLastLocation();


                                int statusCode = ((ApiException) e).getStatusCode();
                                switch (statusCode) {
                                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                        break;
                                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                        String errorMessage = "setting change unavailable";
                                        break;
                                }
                            }
                        });


                    } catch (ApiException exception) {
                        switch (exception.getStatusCode()) {

                            //no gps/location services
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                //before show dialog, show empty page since no gps dy
                                food_progress.setVisibility(View.GONE);
                                lv_food.setVisibility(View.GONE);
                                food_loc_permission.setVisibility(View.GONE);

                                food_gps.setVisibility(View.VISIBLE);

                                AlertDialog alertDialog = new AlertDialog.Builder(FoodNearbyActivity
                                        .this).create();
                                alertDialog.setTitle(R.string.need_res_gps_title);
                                alertDialog.setMessage(getString(R.string.need_res_gps));

                                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string
                                        .open_settings), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();

                                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                                        food_progress.setVisibility(View.GONE);
                                        food_gps.setVisibility(View.GONE);
                                        lv_food.setVisibility(View.GONE);
                                        food_loc_permission.setVisibility(View.GONE);

                                        food_retry.setVisibility(View.VISIBLE);
                                    }
                                });

                                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                                        new DialogInterface.OnClickListener() {

                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color
                                        .RED);
                                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color
                                        .BLACK);

                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                // Location settings are not satisfied. However, we have no way to fix the
                                // settings so we won't show the dialog.

                                break;
                        }
                    }
                }
            });
        }
    }

//    private void checkForLocationAvaibility() {
//        if (ActivityCompat.checkSelfPermission(FoodNearbyActivity.this,
//                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(FoodNearbyActivity.this,
//                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                String[] PERMISSIONS_LOCATION = {
//                        Manifest.permission.ACCESS_FINE_LOCATION,
//                        Manifest.permission.ACCESS_COARSE_LOCATION,
//                };
//                requestPermissions(PERMISSIONS_LOCATION, RESULTS_LOCATION);
//            }
//
//            return;
//        }
//
//        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        }
//    }

    private void getLastLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(FoodNearbyActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(FoodNearbyActivity.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // GPS location can be null if GPS is switched off
                            if (location != null) {
                                preferences.save(FoodNearbyActivity.this, "userlat", String.valueOf(3.152506));
                                preferences.save(FoodNearbyActivity.this, "userlong", String.valueOf(101.595420));
                            } else {
//                                checkForLocationAvaibility();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
//                            checkForLocationAvaibility();
                        }
                    });
        } catch (Exception e) {
        }
    }

    private void get_list() {
        userlat = preferences.getValue(FoodNearbyActivity.this, "userlat");
        userlong = preferences.getValue(FoodNearbyActivity.this, "userlong");

        //if last location different from current location > 50
        if (Double.parseDouble(userlat) != lastLocation.getLatitude() && Double.parseDouble(userlong) != lastLocation.getLongitude()) {

            if (list != null) {
                list.clear();
            }
            if (listget != null) {
                listget.clear();
            }
            getRestaurantList();

        } else { //if last location equals/< 50 diff from current location (can be first time come in)
            if (list == null) { //first time come in
                getRestaurantList();

            } else if (fromFoodFilter) {

                fromFoodFilter = false;
                listget.clear();
                list.clear();
                getRestaurantList();
            } else {
                food_progress.setVisibility(View.GONE);
                lv_food.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Your are still in proximity area", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getRestaurantList() {
        food_progress.setVisibility(View.VISIBLE);
        food_retry.setVisibility(View.GONE);
        food_gps.setVisibility(View.GONE);
        food_loc_permission.setVisibility(View.GONE);
        lv_food.setVisibility(View.VISIBLE);

        rowFrontend = 0;
        rowBackend = 0;
        //prepare to post lat long to server
        final String access_token = preferences.getValue(FoodNearbyActivity.this, GlobalVariables.STRPREF_ACCESS_TOKEN);
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
                    new MiscHelper().retroLogUnsuc(response, "getRestaurantList ", "JAY");
                    Toast.makeText(FoodNearbyActivity.this, R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT).show();

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

                    if (listget != null) {
                        listget.add(new RestaurantModel(propic, Latitude, Longitude, Name, Location,
                                State, rating, ownerJID, ID, MainCuisine, video, fav));
                    }
                }
                list = listget;
                hasLocationResultAlready = false;
                mAdapter = new FoodNearbyAdapter(FoodNearbyActivity.this, list);
                llm = new LinearLayoutManager(FoodNearbyActivity.this);
                llm.setOrientation(RecyclerView.VERTICAL);


                //new - remember to set back to LLM if this doesnt work
                LinearLayoutManager llm = new LinearLayoutManager(FoodNearbyActivity.this);
                llm.setOrientation(RecyclerView.VERTICAL);
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
                        if (lastVisibleItemPosition >= (rowFrontend + 7)) {
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

                                        new MiscHelper().retroLogUnsuc(response, "getRestaurantList2 ", "JAY");
                                        Toast.makeText(FoodNearbyActivity.this,
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
                                    new MiscHelper().retroLogFailure(t, "getRestaurantList2 ", "JAY");
                                    Toast.makeText(FoodNearbyActivity.this, R.string.onfailure, Toast
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
                new MiscHelper().retroLogFailure(t, "getRestaurantList ", "JAY");
                Toast.makeText(FoodNearbyActivity.this, R.string.onfailure, Toast.LENGTH_SHORT).show();

                if (food_progress != null && food_retry != null) {
                    food_progress.setVisibility(View.GONE);
                    food_retry.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        } catch (Exception e) {
        }
        list = null;
        FoodFilter.savedState = 0;

        this.finishAndRemoveTask();

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case 199:

                if (resultCode == 0) {

                } else {
                    getLocation();
                }

                break;

        }

        if (requestCode == 1) {
            switch (resultCode) {
                case 5:
                    if (!data.getStringExtra("cuisineValue").equals("")) { //got cuisine
                        setTitle(R.string.filtered_fnb);

                        query1 = data.getStringExtra("cuisineValue");
                        type1 = "MainCuisine";

                        if (data.getStringExtra("state") != null && !data.getStringExtra
                                ("state").equals("Please select State")) { //cuisine + state
                            query2 = data.getStringExtra("state");
                            type2 = "State";
                            userlat = null;
                            userlong = null;


                            if (data.getStringExtra("location") != null && !data.getStringExtra
                                    ("location").equals("Please select location")) { //cuisine +
                                // state + location
                                query3 = data.getStringExtra("location");
                                type3 = "Location";

                            }
                        } else { //no state/location
                            query2 = "";
                            type2 = "";
                            query3 = "";
                            type3 = "";
                        }
                    } else if (data.getStringExtra("state") != null && !data.getStringExtra
                            ("state").equals("Please select State")) { //state
                        setTitle(R.string.filtered_fnb);

                        query1 = data.getStringExtra("state");
                        type1 = "State";
                        query2 = "";
                        type2 = "";
                        query3 = "";
                        type3 = "";
                        userlat = null;
                        userlong = null;


                        if (data.getStringExtra("location") != null && !data.getStringExtra
                                ("location").equals("Please select location")) { //state + location
                            query2 = data.getStringExtra("location");
                            type2 = "Location";
                            query3 = "";
                            type3 = "";
                        }
                    } else { //no state, location or cuisine - reset indiScheList
                        setTitle(R.string.CurrentLocation);

                        query1 = "";
                        type1 = "";
                        query2 = "";
                        type2 = "";
                        query3 = "";
                        type3 = "";
                    }

                    if (FoodFilter.savedState != 0) {
                        if (FoodFilter.pagePosition == 0) { //coords chosen from map
                            setTitle(R.string.filtered_fnb);

                            userlat = data.getStringExtra("lat");
                            userlong = data.getStringExtra("long");
                            query2 = "";
                            type2 = "";
                            query3 = "";
                            type3 = "";
                            if (!type1.equals("MainCuisine")) {
                                query1 = "";
                                type1 = "";
                            }
                        } else { //state and location
                            userlat = null;
                            userlong = null;
                            preferences.save(FoodNearbyActivity.this, "location", String.valueOf(Foodfilter_state.spinnerlocation.getSelectedItemId()));
                        }
                    }
                    fromFoodFilter = true;

                    if (list != null) {
                        list.clear();
                    }

                    if (listget != null) {
                        listget.clear();
                    }
                    getRestaurantList();
            }
        }


    }

    // check got open gps or not and set the location mode
    public void foodcheckGps() {
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(connectionResult -> {

                    }).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(FoodNearbyActivity.this, 199);
//                                activity.finish();
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                    }
                }
            });
        }
    }


    //function to go back to food filter activity if came from there
//    @Override
//    public void onBackPressed() {
//        if (fromFoodFilter) {
//            Intent filter = new Intent(this, FoodFilter.class);
//
//            filter.putExtra("lat", userlat);
//            filter.putExtra("long", userlong);
//            startActivityForResult(filter, 1);
//        } else {
//            super.onBackPressed();
//        }
//    }
}