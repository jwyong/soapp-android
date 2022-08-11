package com.soapp.settings_tab;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;

import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.global.GlobalVariables;
import com.soapp.global.Preferences;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkManager;

/* Created by chang on 24/09/2017. */

public class ScheduleNotification extends BaseActivity {

    public static ScheduleNotificationAdapter mAdapter;
    public static DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    RecyclerView mRecycleView;
    RecyclerView.LayoutManager mLayoutManager;
    Button done_btn;
    private Preferences preferences = Preferences.getInstance();
    private ProgressDialog dialog;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_notification_recycle);
        setupToolbar();
        setTitle(R.string.settings_reminder_alert);
        mRecycleView = findViewById(R.id.schedule_recycle);
        mLayoutManager = new LinearLayoutManager(this);
        mRecycleView.setLayoutManager(mLayoutManager);
        mAdapter = new ScheduleNotificationAdapter(this);
        mRecycleView.setAdapter(mAdapter);
        done_btn = findViewById(R.id.done_btn);

        done_btn.setOnClickListener(view -> {
            int newTime = preferences.getIntValue(this, GlobalVariables.STRPREF_NOTIFICATION_TIME);

            if (newTime != -1) { //only proceed if valid time is chosen
                //clear ALL previous reminders
                WorkManager.getInstance().cancelAllWorkByTag("ReminderAppt"); //XX minutes before appt
                WorkManager.getInstance().cancelAllWorkByTag("ExactReminderAppt"); //appt happening now
                WorkManager.getInstance().cancelAllWorkByTag("DeleteReminderAppt"); //shift appt to History XX hours after appt
                WorkManager.getInstance().cancelUniqueWork("periodicSetReminder"); //set appts for next 7 days every 6 days

                //set new reminders based on new reminder period
                Soapp.getInstance().setReminderCheckingPeriodically(true);
                finish();

//                new asyncReminderChange().execute(newTime);
            }
        });
    }

//    //progress dialog for setting up reminder alerts
//    private class asyncReminderChange extends android.os.AsyncTask<Integer, Void, Void> {
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            dialog = ProgressDialog.show(ScheduleNotification.this, null, getString(R.string
//                    .updating_appt_reminder));
//        }
//
//        @Override
//        protected Void doInBackground(Integer... params) {
//            final int newTime = params[0];
//
//            databaseHelper.updatePendingAlarms(newTime, true);
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void result) {
//            super.onPostExecute(result);
//
//            Toast.makeText(ScheduleNotification.this, R.string.updated_appt_reminder, Toast
//                    .LENGTH_SHORT).show();
//            if (dialog != null && dialog.isShowing()) {
//                dialog.dismiss();
//            }
//            onBackPressed();
//        }
//    }
}
