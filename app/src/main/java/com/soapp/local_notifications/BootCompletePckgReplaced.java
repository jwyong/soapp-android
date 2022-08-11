package com.soapp.local_notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/* Created by Soapp on 09/11/2017. */

public class BootCompletePckgReplaced extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //set wake to alarm manager again - includes update all pending alarms for next 24 hours
//        SoappJobScheduler.localVerControlWake(false);

    }


}
