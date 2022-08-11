package com.soapp.registration;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.soapp.global.GlobalVariables;
import com.soapp.global.Preferences;

public class InstallListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String rawReferrerString = intent.getStringExtra("referrer");
        if(rawReferrerString != null) {
            Preferences.getInstance().save(context, GlobalVariables.STR_SHARER, rawReferrerString);
        }
    }
}
