package com.soapp.local_notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;

/* Created by Soapp on 08/11/2017. */

public class LocalBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String tag = intent.getStringExtra("tag");

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        Bundle myExtrasBundle = new Bundle();
        myExtrasBundle.putString("tag", tag);

        if (intent.hasExtra("apptIDClear")) { //apptIDClear = clear appt details
            myExtrasBundle.putString("apptIDClear", intent.getStringExtra("apptIDClear"));

        } else if (intent.hasExtra("apptID")) { //apptID = appt reminder alert
            myExtrasBundle.putString("apptID", intent.getStringExtra("apptID"));
            myExtrasBundle.putString("displayname", intent.getStringExtra("displayname"));

        } else if (intent.hasExtra("wake")) { //wake
            myExtrasBundle.putString("wake", intent.getStringExtra("wake"));
        }
//
//        Job localJob = dispatcher.newJobBuilder()
//                .setService(SoappJobScheduler.class)
//                .setTag(tag)
//                .setRecurring(false)
//                .setTrigger(Trigger.NOW)
//                .setReplaceCurrent(true)
//                .setExtras(myExtrasBundle)
//                .build();
//
//        dispatcher.mustSchedule(localJob);
    }
}