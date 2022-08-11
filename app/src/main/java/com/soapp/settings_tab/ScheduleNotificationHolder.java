package com.soapp.settings_tab;

import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.soapp.R;
import com.soapp.global.GlobalVariables;
import com.soapp.global.Preferences;
import com.soapp.setup.Soapp;

import androidx.recyclerview.widget.RecyclerView;

/* Created by chang on 24/09/2017.*/

public class ScheduleNotificationHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


    public TextView notificationtime;
    public CheckBox checkBox;
    public LinearLayout share_contact_click;
    private Preferences preferences = null;


    public ScheduleNotificationHolder(View itemView) {
        super(itemView);
        notificationtime = itemView.findViewById(R.id.notification_reminder_interval);
        checkBox = itemView.findViewById(R.id.notification_reminder_interval_checkbox);
        share_contact_click = itemView.findViewById(R.id.notification_reminder_interval_click);
        itemView.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        preferences = Preferences.getInstance();
        switch (notificationtime.getText().toString()) {
            case "At time of appointment":
                preferences.save(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_NOTIFICATION_TIMESTR, notificationtime.getText().toString());
                preferences.saveint(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_NOTIFICATION_TIME, 0);
                ScheduleNotification.mAdapter.notifyDataSetChanged();
                break;
            case "5 minutes before":
                preferences.save(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_NOTIFICATION_TIMESTR, notificationtime.getText().toString());
                preferences.saveint(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_NOTIFICATION_TIME, 5);
                ScheduleNotification.mAdapter.notifyDataSetChanged();
                break;
            case "15 minutes before":
                preferences.save(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_NOTIFICATION_TIMESTR, notificationtime.getText().toString());
                preferences.saveint(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_NOTIFICATION_TIME, 15);
                ScheduleNotification.mAdapter.notifyDataSetChanged();
                break;
            case "30 minutes before":
                preferences.save(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_NOTIFICATION_TIMESTR, notificationtime.getText().toString());
                preferences.saveint(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_NOTIFICATION_TIME, 30);
                ScheduleNotification.mAdapter.notifyDataSetChanged();
                break;
            case "1 hour before":
                preferences.save(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_NOTIFICATION_TIMESTR, notificationtime.getText().toString());
                preferences.saveint(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_NOTIFICATION_TIME, 60);
                ScheduleNotification.mAdapter.notifyDataSetChanged();
                break;
            case "2 hours before":
                preferences.save(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_NOTIFICATION_TIMESTR, notificationtime.getText().toString());
                preferences.saveint(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_NOTIFICATION_TIME, 120);
                ScheduleNotification.mAdapter.notifyDataSetChanged();
                break;
            case "3 hours before":
                preferences.save(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_NOTIFICATION_TIMESTR, notificationtime.getText().toString());
                preferences.saveint(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_NOTIFICATION_TIME, 180);
                ScheduleNotification.mAdapter.notifyDataSetChanged();
                break;
            case "12 hours before":
                preferences.save(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_NOTIFICATION_TIMESTR, notificationtime.getText().toString());
                preferences.saveint(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_NOTIFICATION_TIME, 720);
                ScheduleNotification.mAdapter.notifyDataSetChanged();
                break;
            case "1 day before":
                preferences.save(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_NOTIFICATION_TIMESTR, notificationtime.getText().toString());
                preferences.saveint(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_NOTIFICATION_TIME, 1440);
                ScheduleNotification.mAdapter.notifyDataSetChanged();
                break;
            case "2 days before":
                preferences.save(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_NOTIFICATION_TIMESTR, notificationtime.getText().toString());
                preferences.saveint(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_NOTIFICATION_TIME, 2880);
                ScheduleNotification.mAdapter.notifyDataSetChanged();
                break;
            case "1 week before":
                preferences.save(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_NOTIFICATION_TIMESTR, notificationtime.getText().toString());
                preferences.saveint(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_NOTIFICATION_TIME, 10080);
                ScheduleNotification.mAdapter.notifyDataSetChanged();
                break;
            default:
                break;

        }

    }
}