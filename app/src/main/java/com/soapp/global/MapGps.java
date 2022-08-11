package com.soapp.global;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.sql.DatabaseHelper;
import com.soapp.xmpp.GlobalMessageHelper.GlobalHeaderHelper;
import com.soapp.xmpp.GroupChatHelper.GroupChatStanza;
import com.soapp.xmpp.SingleChatHelper.SingleChatStanza;
import com.soapp.xmpp.SmackHelper;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class MapGps extends BaseActivity implements OnMapReadyCallback, LocationListener {
    PlaceAutocompleteFragment placeAutoComplete;
    String room_displayname, infoID, infoImgURL, apptID, jid, from;
    String placeName, placeAddress, poiName;
    SingleChatStanza singleChatStanza = new SingleChatStanza();
    GroupChatStanza groupChatStanza = new GroupChatStanza();
    boolean isGpsEnabled, isNetworkEnabled;
    Location location;
    LocationManager locationManager;
    Double mapLat, mapLon;

    //for map marker
    Marker mapMarker;
    private boolean isGSearch = false;

    Button confirmBtn;
    ImageButton wazeImgBtn, google_mapImgBtn;

    GlobalHeaderHelper globalHeaderHelper = new GlobalHeaderHelper();

    private GoogleMap mMap;
    private Preferences preferences = Preferences.getInstance();
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private UIHelper uiHelper = new UIHelper();
    private GoogleApiClient googleApiClient;
    private LocationManager lManager;

    //to check if need gps or not
    boolean needGPS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity_share);
        setupToolbar();

//        get check gps
        lManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //get from
        from = getIntent().getStringExtra("from");

        //declare button for confirm
        confirmBtn = findViewById(R.id.mapsend);
        wazeImgBtn = findViewById(R.id.waze_imgbtn);
        google_mapImgBtn = findViewById(R.id.google_map_imgbtn);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        String title;
        switch (from) {
            case "scheHolder": //existing appts
                title = getString(R.string.sche_location);
                confirmBtnFunction();

                break;

            case "newAppt": //new appts
                title = getString(R.string.sche_location);
                confirmBtnFunction();

                break;

            case "chatHolder": //in/out shared location
                title = getString(R.string.map_navigate_title);
                confirmBtn.setVisibility(View.GONE);
                break;

            case "chatLog": //share location
                title = getString(R.string.sche_location);
                confirmBtnFunction();

                break;

            case "foodDetailInfo":
                title = getString(R.string.map_navigate_title);
                confirmBtn.setVisibility(View.GONE);
                break;

            default:
                title = getString(R.string.map_navigate_title);
                break;
        }

        setTitle(title);

        //get intents
        String mapLatStr = getIntent().getStringExtra("latitude");

        //if maplat not null means no need gps
        if (mapLatStr != null && !mapLatStr.equals("")) {
            String mapLonStr = getIntent().getStringExtra("longitude");

            mapLat = Double.valueOf(mapLatStr);
            mapLon = Double.valueOf(mapLonStr);

            needGPS = false;

            wazeImgBtn.setVisibility(View.VISIBLE);
            google_mapImgBtn.setVisibility(View.VISIBLE);

            wazeImgBtn.setOnClickListener(view -> {
                try {
                    String uri = "waze://?ll=" + mapLat + "," + mapLon + "&navigate=yes";
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                } catch (ActivityNotFoundException ex) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.waze"));
                    startActivity(intent);
                }
            });

            google_mapImgBtn.setOnClickListener(view -> {
                try {
                    // Create a Uri from an intent string. Use the result to create an Intent.
                    Uri gmmIntentUri = Uri.parse("http://maps.google.com/maps?daddr=" + mapLat + "," + mapLon);

                    // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    // Make the Intent explicit by setting the Google Maps package
                    mapIntent.setPackage("com.google.android.apps.maps");

                    // Attempt to start an activity that can handle the Intent
                    startActivity(mapIntent);
                } catch (ActivityNotFoundException ex) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps"));
                    startActivity(intent);
                }
            });

        } else { //no lat value
            needGPS = true;

            wazeImgBtn.setVisibility(View.GONE);
            google_mapImgBtn.setVisibility(View.GONE);
        }

        if (needGPS) {
            if (PackageManager.PERMISSION_DENIED == this.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    PackageManager.PERMISSION_DENIED == this.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                new PermissionHelper().CheckPermissions(this, 1003, R.string.permission_txt);

            } else {
                checkGps();
            }
        }

        //setup search bar (gmaps)
        placeAutoComplete = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete);
        placeAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                //set boolean to true
                isGSearch = true;

                //get defails of place selected from search dropdown
                placeName = place.getName().toString();
                placeAddress = place.getAddress().toString();

                LatLng latLng = place.getLatLng();
                mapLat = latLng.latitude;
                mapLon = latLng.longitude;

                //set marker on map
                replaceCurrentMarker(latLng, poiName, 1, 14f);
            }

            @Override
            public void onError(Status status) {
            }
        });

        //set autocomplete bias for first time
        setSearchBias();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);

        //click on map marker (KIV)
//        mMap.setOnMarkerClickListener(marker -> {
//            mapLat = String.valueOf(marker.getPosition().latitude);
//            mapLon = String.valueOf(marker.getPosition().longitude);
//            places = marker.getTitle();
//            marker.showInfoWindow();
//            return true;
//        });

        //click on map marker title (KIV)
//        mMap.setOnInfoWindowClickListener(marker -> {
//            mapLat = String.valueOf(marker.getPosition().latitude);
//            longitude = String.valueOf(marker.getPosition().longitude);
//            places = marker.getTitle();
//        });

        //when click on poi on map
        mMap.setOnPoiClickListener(pointOfInterest -> {
            //set boolean (not searching via searchBar)
            isGSearch = false;

            //get defails of place
            poiName = pointOfInterest.name;

            LatLng latLng = pointOfInterest.latLng;
            mapLat = latLng.latitude;
            mapLon = latLng.longitude;

            //set marker on map
            replaceCurrentMarker(latLng, poiName, 0, 14f);
        });

        mMap.setOnMapLongClickListener(latLng -> {
            //set boolean (not searching via searchBar)
            isGSearch = false;

            //get defails of place
            mapLat = latLng.latitude;
            mapLon = latLng.longitude;

            DecimalFormat decimalFormat = new DecimalFormat(".####");

            //set marker on map
            replaceCurrentMarker(latLng, decimalFormat.format(mapLat) + ", " + decimalFormat.format(mapLon), 0, 14f);
        });

        if (needGPS) {
            //set default map zooming location to peninsular msia
            mapLat = 3.120093;
            mapLon = 101.600407;

            LatLng soapp = new LatLng(mapLat, mapLon);

            //just zoom to soapp
            replaceCurrentMarker(soapp, null, 2, 6f);

            //for now chatlogs don't need gps /if permission not granted, set location to Soapp
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            } else { //location permission granted
                mMap.setMyLocationEnabled(true);

                locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

                isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (isNetworkEnabled || isGpsEnabled) {
                    //user gps enabled
                    if (isNetworkEnabled) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, this);

                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                            if (location != null) {
                                mapLat = location.getLatitude();
                                mapLon = location.getLongitude();
                            }
                        }
                    }

                    if (isGpsEnabled) {
                        if (location == null) {
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    1,
                                    1, this);
                            if (locationManager != null) {
                                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                if (location != null) {
                                    mapLat = location.getLatitude();
                                    mapLon = location.getLongitude();
                                }
                            }
                        }
                    }

                    if (location != null) { //safe unwrapping
                        //set boundary for search area
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        placeAutoComplete.setBoundsBias(new LatLngBounds(latLng, latLng
                        ));

                        onLocationChanged(location);

                    } else {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                    }

                } else { //no gps from user
                    Toast.makeText(this, R.string.gps_accurate, Toast.LENGTH_SHORT).show();

                    //set default map zooming location to peninsular msia
                    LatLng latLng = new LatLng(3.120093, 101.600407);

                    replaceCurrentMarker(latLng, null, 2, 6f);

                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                }
            }
        } else { //chatholder, foodDetailInfo, scheholder
            placeName = getIntent().getStringExtra("placeName");
            placeAddress = getIntent().getStringExtra("placeAddress");

            LatLng latLng = new LatLng(Double.valueOf(mapLat), Double.valueOf(mapLon));

            replaceCurrentMarker(latLng, placeName, 2, 14f);

            //set bounds for search radius
            placeAutoComplete.setBoundsBias(new LatLngBounds(latLng, latLng));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1003:
                for (int i = 0, len = permissions.length; i < len; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        String permission = permissions[i];

                        boolean showRationale = false;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            showRationale = shouldShowRequestPermissionRationale(permission);

                        }
                        if (!showRationale) {
                            preferences.save(this, "dunAskagain", String.valueOf(showRationale));
                            onBackPressed();
                            break;
                        } else {
                            preferences.save(this, "dunAskagain", String.valueOf(showRationale));
                            onBackPressed();
                            Toast.makeText(this, R.string.need_appt_loc, Toast.LENGTH_SHORT)
                                    .show();
                        }

                    } else {
                        checkGps();
                        break;
                    }
                }

                break;
        }
    }

    // check got open gps or not and set the location mode
    public void checkGps() {
        if (!lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
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

            //ask user to turn on gps - not required if for newAppt
            if (!from.equals("newAppt")) {
                PendingResult<LocationSettingsResult> result =
                        LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());

                result.setResultCallback(result1 -> {
                    final Status status = result1.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(MapGps.this, 199);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                    }
                });
            }
        }
    }

    //function for when clicking "confirm button"
    public void confirmBtnFunction() {
        switch (from) {
            case "newAppt": //new appts
                confirmBtn.setOnClickListener(v -> {
                    if (mapLat != 0) {
                        //get placeName and placeAddress from geocoder using mapLat and mapLon
                        getNameAddFromLatLon();

                        Intent backIntent = new Intent();

                        //format lat and long to 6 digits
                        backIntent.putExtra("latitude", String.valueOf(mapLat));
                        backIntent.putExtra("longitude", String.valueOf(mapLon));

                        backIntent.putExtra("locationName", placeName);
                        backIntent.putExtra("locationAdd", placeAddress);

                        setResult(RESULT_OK, backIntent);
                        finish();
                    } else {
                        uiHelper.dialog1Btn(this, getString(R.string.appt_no_map_title), getString(R.string.appt_no_map_msg),
                                R.string.ok_label, R.color.black, null, true, false);
                    }
                });
                break;

            case "scheHolder": //existing appts
                //set action for "confirm" button
                confirmBtn.setOnClickListener(v -> {
                    if (mapLat != 0) {
                        room_displayname = getIntent().getStringExtra("displayname");
                        apptID = getIntent().getStringExtra("apptID");
                        jid = getIntent().getStringExtra("jid");

                        //get placeName and placeAddress from geocoder using mapLat and mapLon
                        getNameAddFromLatLon();

                        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                            String uniqueId = UUID.randomUUID().toString();
                            String staticurl = new MiscHelper().getGmapsStaticURL(mapLat + "", mapLon + "");
                            String user_displayname = preferences.getValue(this, GlobalVariables.STRPREF_USERNAME);
                            String pushMsg = getString(R.string.appt_location_changed);

                            if (jid.length() == 12) { //individual
                                singleChatStanza.SoappAppointmentStanza(jid, pushMsg, "", staticurl,
                                        placeName, placeAddress, String.valueOf(mapLat), String.valueOf(mapLon), "", "",
                                        "", "", uniqueId, "", "",
                                        apptID, user_displayname, "appointment", "0", "3");

                            } else { //group
                                String userJid = preferences.getValue(this, GlobalVariables.STRPREF_USER_ID);

                                groupChatStanza.GroupAppointment(jid, userJid, uniqueId, "",
                                        staticurl, placeName, mapLat.toString(), mapLon.toString(), "", "",
                                        "", "", "", apptID, room_displayname,
                                        pushMsg, "appointment", "", "3");
                            }
                            databaseHelper.outgoingApptLocation(jid, apptID, staticurl, mapLat.toString(), mapLon.toString(),
                                    placeName, "", "", uniqueId);

                            finish();
                        } else {
                            Toast.makeText(this, getString(R.string.xmpp_waiting_connection), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        uiHelper.dialog1Btn(this, getString(R.string.appt_no_map_title), getString(R.string.appt_no_map_msg),
                                R.string.ok_label, R.color.black, null, true, false);
                    }

                });
                break;

            case "chatLog": //chatlogs
                confirmBtn.setOnClickListener(v -> {
                    if (mapLat != 0) {
                        String jid = getIntent().getStringExtra("jid");
                        String userJid = preferences.getValue(this, GlobalVariables.STRPREF_USER_ID);
                        String displayname = databaseHelper.getNameFromContactRoster(jid);
                        String staticurl = new MiscHelper().getGmapsStaticURL(mapLat + "", mapLon + "");
                        String userName = preferences.getValue(this, GlobalVariables.STRPREF_USERNAME);
                        String uniqueId = UUID.randomUUID().toString();

                        //get placeName and placeAddress from geocoder using mapLat and mapLon
                        getNameAddFromLatLon();

                        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                            globalHeaderHelper.GlobalHeaderTime(jid);
                            if (jid.length() == 12) {
                                databaseHelper.MapOutputDatabase(jid, staticurl, uniqueId,
                                        System.currentTimeMillis(), mapLat.toString(), mapLon.toString(), placeName, placeAddress);

                                singleChatStanza.SoappMapStanza(staticurl, jid, mapLat.toString(), mapLon.toString(), uniqueId,
                                        userName, placeName, placeAddress);

                                finish();
                            } else {
                                groupChatStanza.GroupMap(jid, userJid, userName, mapLat.toString(), mapLon.toString(), uniqueId,
                                        staticurl, displayname, placeName, placeAddress);

                                databaseHelper.GroupMapOutputDatabase(jid, staticurl, uniqueId, System
                                        .currentTimeMillis(), userJid, mapLat.toString(), mapLon.toString(), placeName, placeAddress);
                            }
                        } else {
                            databaseHelper.saveMessageAndSendWhenOnline("map", jid, userJid, placeName, uniqueId,
                                    System.currentTimeMillis(), null, null, mapLat.toString(), mapLon.toString(),
                                    staticurl, placeAddress);
                        }
                        finish();

                    } else {
                        uiHelper.dialog1Btn(this, getString(R.string.appt_no_map_title), getString(R.string.appt_no_map_msg),
                                R.string.ok_label, R.color.black, null, true, false);
                    }
                });
                break;

            default:
                break;

        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            // Getting latitude of the current location
            mapLat = location.getLatitude();

            // Getting longitude of the current location
            mapLon = location.getLongitude();

            // Creating a LatLng object for the current location
            LatLng latLng = new LatLng(mapLat, mapLon);

            replaceCurrentMarker(latLng, getString(R.string.map_current_loc), 1, 14f);

            locationManager.removeUpdates(this);
            placeAutoComplete.setBoundsBias(new LatLngBounds(
                    latLng, latLng
            ));

            //reset autocomplete bias
            setSearchBias();
        }
    }

    //function to get name and address from lat and long only
    private void getNameAddFromLatLon() {
        if (isGSearch) {
            return;
        }

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(mapLat, mapLon, 1);

            //get name and address
            if (poiName == null || poiName.equals("")) {
                placeName = addresses.get(0).getFeatureName();

            } else {
                placeName = poiName;
            }
            placeAddress = addresses.get(0).getAddressLine(0);

        } catch (Exception e) {
            DecimalFormat decimalFormat = new DecimalFormat(".####");
            if (placeName == null || placeName.equals("")) {
                placeName = decimalFormat.format(mapLat) + ", " + decimalFormat.format(mapLon);
            }
            placeAddress = "No Address";
        }
    }

    //function to replace current marker with new one (not add new one)
    private void replaceCurrentMarker(LatLng latLng, String title, int cameraZoomMode, float zoomLevel) {
        //clear previous marker
        if (mapMarker != null) {
            mapMarker.remove();
        }

        //pan camera to new marker
        switch (cameraZoomMode) {
            case 0: //don't move at all
                break;

            case 1: //move and animate
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
                break;

            case 2: //move only
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
                break;

            default:
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
                break;
        }

        //set new marker info
        if (title != null) {
            int height = (int) (25 * GlobalVariables.screenDensity);
            int width = (int) (25 * GlobalVariables.screenDensity);
            BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_location_full_green_100px);
            Bitmap b = bitmapdraw.getBitmap();
            Bitmap normalMarker = Bitmap.createScaledBitmap(b, width, height, false);


            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {


                    return null;
                }
            });

            MarkerOptions markerOpt = new MarkerOptions();
            markerOpt.position(latLng)
                    .title(title)
                    .icon(BitmapDescriptorFactory.fromBitmap(normalMarker));


            MapGpsAdapter mapadapter = new MapGpsAdapter(this);

            mMap.setInfoWindowAdapter(mapadapter);
            mapMarker = mMap.addMarker(markerOpt);
            mapMarker.showInfoWindow();
        }

        //reset autocomplete bias
        setSearchBias();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    protected void onPause() {
        super.onPause();
        setMyLocationFalseMap();
    }

    @Override
    protected void onDestroy() {
        setMyLocationFalseMap();

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setMyLocationTrueMap();
    }

    void setMyLocationFalseMap() {
        if (mMap != null) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            mMap.setMyLocationEnabled(false);
        }
    }

    void setMyLocationTrueMap() {
        if (mMap != null) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            mMap.setMyLocationEnabled(true);
        }
    }

    //function for setting bias for autocomplete
    private void setSearchBias() {
        //set bias to xxkm around user's location
        double originLat, originLon;
        if (mapLat != null) { //got lat, set bias
            originLat = mapLat;
            originLon = mapLon;

        } else { //no lat, set to nearby starting coords
            originLat = 3.120093;
            originLon = 101.600407;

            //default set to my
//            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
//                    .setCountry("MY")
//                    .build();
//
//            placeAutoComplete.setFilter(typeFilter);
        }

        Double radius = 0.1;
        double minLat = originLat - radius;
        double minLon = originLon - radius;
        double maxLat = originLat + radius;
        double maxLon = originLon + radius;

        placeAutoComplete.setBoundsBias(new LatLngBounds(
                new LatLng(minLat, minLon),
                new LatLng(maxLat, maxLon)));
    }

    public class MapGpsAdapter implements GoogleMap.InfoWindowAdapter {

        private Activity context;

        public MapGpsAdapter(Activity context) {
            this.context = context;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            View view = context.getLayoutInflater().inflate(R.layout.custom_googlemap_title, null);

            ImageView google_map_images = view.findViewById(R.id.google_map_images);
            TextView google_map_txt = view.findViewById(R.id.google_map_txt);
            TextView google_map_txt1 = view.findViewById(R.id.google_map_txt1);

            google_map_txt.setText(marker.getTitle());
            google_map_txt1.setVisibility(View.GONE);


            return view;
        }

    }

}
