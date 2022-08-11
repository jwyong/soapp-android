package com.soapp.global;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;

import com.soapp.setup.Soapp;
import com.soapp.xmpp.CheckSmackHelper;
import com.soapp.xmpp.SmackHelper;

import androidx.lifecycle.MutableLiveData;

/**
 * Created by ibrahim on 27/02/2018.
 */

public class CheckInternetAvaibility extends ConnectivityManager.NetworkCallback {
    public static CheckInternetAvaibility INSTANCE = null;

    //for LiveData of "not connected to server" UI
    public static MutableLiveData<Boolean> xmppauth = new MutableLiveData<>();

    public static boolean trueData, trueWifi;
    public static boolean gotInternet = false;
    public static int networktype = -1;
    private NetworkRequest networkRequest;

    Preferences preferences = Preferences.getInstance();

    public static synchronized CheckInternetAvaibility getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new CheckInternetAvaibility();
            xmppauth.postValue(false);
        }

        return INSTANCE;
    }

    public CheckInternetAvaibility() {

        networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();
        enable(Soapp.getInstance());
    }

    public void enable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.registerNetworkCallback(networkRequest, this);

    }

    @Override
    public void onAvailable(Network network) {//got internet
        super.onAvailable(network);

        gotInternet = true;

        networktype = getNetworkType();

        //only connect to smack if user already registered
        if (preferences.getValue(Soapp.getInstance(), GlobalVariables.STRPREF_LOGIN_STATUS).equals("successful")) {
            CheckSmackHelper.getInstance().check();
        }
    }

    @Override
    public void onLosing(Network network, int maxMsToLive) {//before internet lost
        super.onLosing(network, maxMsToLive);
    }

    @Override
    public void onLost(Network network) {//after internet lost
        super.onLost(network);
        gotInternet = false;

//        if (SmackHelper.getXMPPConnection() != null) {
//            SmackHelper.getXMPPConnection().notifyConnectionError(new SmackException
// .NotConnectedException("Internet Lost"));
//        }
        if (SmackHelper.getXMPPConnection() != null) {
            SmackHelper.getXMPPConnection().instantShutdown();
        }
//        try {
//            PingManager.getInstanceFor(SmackHelper.getXMPPConnection()).pingMyServer(true,
// TimeUnit.SECONDS.toMillis(3));
//
//        } catch (SmackException.NotConnectedException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

    }

    @Override
    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) { // cahnges of connection
        super.onCapabilitiesChanged(network, networkCapabilities);

        CheckSmackHelper.getInstance().check();

//        SmackHelper.getXMPPConnection().disconnect();
    }

    @Override
    public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) { //
        // changes of linking
        super.onLinkPropertiesChanged(network, linkProperties);
    }

    //    return data of wifi value
    private int getNetworkType() {
        ConnectivityManager cm = (ConnectivityManager) Soapp.getInstance()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork == null) {
            return -1;
        } else {
            return activeNetwork.getType();
        }
    }
}
