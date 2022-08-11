package com.soapp.xmpp;

import android.content.Intent;
import android.util.Log;

import com.soapp.WorkManager.ConnectSmackWorker;
import com.soapp.global.CheckInternetAvaibility;
import com.soapp.global.GlobalVariables;
import com.soapp.global.Preferences;
import com.soapp.setup.Soapp;
import com.soapp.xmpp.connection.MessagingService;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class CheckSmackHelper {
    Preferences preferences = Preferences.getInstance();
    String loginStatus;
    String username;
    String pass;

    public static CheckSmackHelper instance = null;
    private static final String TAG = "wtf";

    public static synchronized CheckSmackHelper getInstance() {
        if (instance == null) {
            instance = new CheckSmackHelper();
        }
        return instance;
    }

    public CheckSmackHelper() {
        loginStatus = preferences.getValue(Soapp.getInstance(), GlobalVariables.STRPREF_LOGIN_STATUS);
        username = preferences.getValue(Soapp.getInstance(), GlobalVariables.STRPREF_USER_ID);
        pass = preferences.getValue(Soapp.getInstance(), GlobalVariables.STRPREF_XMPP_PASSWORD);

        check();
    }

    public void check() {
        if (CheckInternetAvaibility.gotInternet) {
            Log.d("wtf", "check: got int ");
            if (!CheckServiceRunning()) {
                Log.d("wtf", "check: no serv");
                MessagingService.start(Soapp.getInstance(), username, pass);

            } else if (SmackHelper.getXMPPConnection() == null) {
                Log.d(TAG, "check: xmpp null");
                Intent xmppService = new Intent(Soapp.getInstance(), MessagingService.class);
                Soapp.getInstance().stopService(xmppService);
                Soapp.getInstance().startService(xmppService);
                MessagingService.start(Soapp.getInstance(), username, pass);

            } else if (!SmackHelper.getXMPPConnection().isConnected()) {
                Log.d(TAG, "check: not connected");
                connectWorker();

            } else if (!SmackHelper.getXMPPConnection().isAuthenticated()) {
                Log.d(TAG, "check: not not auth");
                connectWorker();
            }
        }
    }

    private boolean CheckServiceRunning() {
        if (MessagingService.isRunning != null) return MessagingService.isRunning;
        else return false;
    }

    void connectWorker() {
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(ConnectSmackWorker.class).build();

        WorkManager.getInstance().enqueue(work);
    }

}
