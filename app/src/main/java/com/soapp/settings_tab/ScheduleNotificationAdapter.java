package com.soapp.settings_tab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.soapp.R;
import com.soapp.global.GlobalVariables;
import com.soapp.global.Preferences;
import com.soapp.setup.Soapp;

import androidx.recyclerview.widget.RecyclerView;

/* Created by chang on 24/09/2017. */

public class ScheduleNotificationAdapter extends RecyclerView.Adapter<ScheduleNotificationHolder> {

    public String[] notificationtime = {"At time of appointment", "5 minutes before", "15 minutes before", "30 minutes before",
            "1 hour before", "2 hours before", "3 hours before", "12 hours before", "1 day before", "2 days before", "1 week before"};
    private Context context;
    private Preferences preferences = Preferences.getInstance();

    public ScheduleNotificationAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ScheduleNotificationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.setting_tab_notification_item, parent, false);
        return new ScheduleNotificationHolder(view);
    }

    @Override
    public void onBindViewHolder(ScheduleNotificationHolder holder, int position) {
        holder.notificationtime.setText(notificationtime[position]);
        String timereference = preferences.getValue(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_NOTIFICATION_TIMESTR);
        if (timereference != null) {
            if (timereference.equals(notificationtime[position])) {
                holder.checkBox.setChecked(true);
            } else {
                holder.checkBox.setChecked(false);
            }
            if (position == 4 && timereference.equals("nil")) { //set default time to 1 hour
                // (index 4)
                holder.checkBox.setChecked(true);
            }
        } else {
            holder.checkBox.setChecked(false);
        }
    }

    @Override
    public int getItemCount() {
        return notificationtime.length;
    }
}
