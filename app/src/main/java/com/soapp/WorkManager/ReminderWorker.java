package com.soapp.WorkManager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;

import com.soapp.R;
import com.soapp.global.GlobalVariables;
import com.soapp.global.Preferences;
import com.soapp.home.Home;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.entity.Appointment;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.Worker;

public class ReminderWorker extends Worker {
    @NonNull
    @Override
    public Result doWork() {

        Preferences preferences = Preferences.getInstance();
        Context context = Soapp.getInstance().getApplicationContext();
        Data data = getInputData();
        long time = data.getLong("time", -1);

        String apptId = data.getString("apptId");
        String title = DatabaseHelper.getInstance().getApptTitle(apptId);

        int prefTime = preferences.getIntValue(context, GlobalVariables.STRPREF_NOTIFICATION_TIME);


        if (!preferences.getValue(context,
                "ApptReminder").equals("off")) { //if reminder is on
            String content0;

            if (time == -1) {
                time = prefTime < 0 ? GlobalVariables.defaultReminderAlert : prefTime;
            }

            if (time == 0) {
                content0 = context.getString(R.string.appt_now);
            } else if (time < 60) { //less than an hour, use "minutes"
                content0 = context.getString(R.string.appt_in) + " " + time + " " +
                        "" + context.getString(R.string.appt_minutes);
            } else if (time == 60) { //1 hour
                content0 = context.getString(R.string.appt_in) + " 1 " + context.getString(R
                        .string.appt_hour);
            } else if (time < 2880) { //less than 2 days, use "hours" e.g. 24 hours
                content0 = context.getString(R.string.appt_in) + " " + (time / 60) + " " +
                        context.getString(R.string.appt_hours);
            } else if (time < 10080) { //less than a week, use "days"
                content0 = context.getString(R.string.appt_in) + " " + (time / 1440) +
                        " " + context.getString(R.string.appt_days);
            } else { //a week
                content0 = context.getString(R.string.appt_in) + " 1 " + context.getString
                        (R.string.appt_week);
            }
            Uri savedSoundUri = Uri.parse("android.resource://com.soapp/" + R.raw.soapp_appt);
            long[] vibrate = new long[]{300, 300};

            Intent intentHome = new Intent(Soapp.getInstance().getApplicationContext(), Home.class);
            intentHome.putExtra("remoteSche", "1");

            PendingIntent pendingIntent = PendingIntent.getActivity(Soapp.getInstance().getApplicationContext(), 0,
                    intentHome,
                    PendingIntent.FLAG_ONE_SHOT);

            int lights = ((int) TimeUnit.SECONDS.toMillis(1));

//            RemoteInput remoteInputStatus = new RemoteInput.Builder("ReplyReminder")
//                    .setLabel("Waze")
//                    .setAllowFreeFormInput(false)
//                    .setChoices(new String[]{"Going", "Not Going", "Undecided"})
//
//                    .build();
//
//            RemoteInput remoteInputNavi = new RemoteInput.Builder("ReplyReminder")
//                    .setAllowFreeFormInput(false)
//                    .setLabel("Navigate")
//                    .setChoices(new CharSequence[]{"Waze", "Gmaps"})
//                    .build();

            Appointment a = DatabaseHelper.getInstance().getAllApptDet(apptId);

            final Intent wazeIntent = new Intent("com.soapp.MsgReply");
            wazeIntent.putExtra("type", "waze");
            wazeIntent.putExtra("lat", a.getApptLatitude());
            wazeIntent.putExtra("lon", a.getApptLongitude());
            wazeIntent.putExtra("apptID", apptId);

            final PendingIntent wazePendingIntent =
                    PendingIntent.getBroadcast(context,
                            ("wazeReminder" + apptId).hashCode(),
                            wazeIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

            final Intent gmapIntent = new Intent("com.soapp.MsgReply");
            gmapIntent.putExtra("type", "gmaps");
            gmapIntent.putExtra("lat", a.getApptLatitude());
            gmapIntent.putExtra("lon", a.getApptLongitude());
            gmapIntent.putExtra("apptID", apptId);

            final PendingIntent gmapsPendingIntent =
                    PendingIntent.getBroadcast(context,
                            ("gmapReminder" + apptId).hashCode(),
                            gmapIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Action.Builder replyAction = new Notification.Action.Builder(R.drawable.ic_green_dot_42px,
                    "Waze", wazePendingIntent);
            Notification.Action.Builder naviAction = new Notification.Action.Builder(R.drawable.ic_green_dot_42px,
                    "Google Maps", gmapsPendingIntent);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                replyAction.setAllowGeneratedReplies(true);
                naviAction.setAllowGeneratedReplies(true);
            }

            Notification.Builder notificationBuilder = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {


                notificationBuilder = new Notification.Builder(context, "Reminder")
                        .setSmallIcon(R.mipmap.push_small_logo)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.push_local))
                        .setColor(context.getResources().getColor(R.color.primaryLogo))
                        .setContentTitle(String.format("%s with %s", title,
                                DatabaseHelper.getInstance().getNameFromContactRoster(getInputData().getString("jid"))))
                        .setContentText(content0)
                        .setAutoCancel(true)
                        .setGroup("Reminder")
                        .setSound(savedSoundUri)
                        .setVibrate(vibrate)
                        .setContentIntent(pendingIntent)
                        .setActions(replyAction.build(), naviAction.build())
                        .setStyle(new Notification.InboxStyle())
                        .setLights(Color.BLUE, lights, lights);
            } else {
                notificationBuilder = new Notification.Builder(context)
                        .setSmallIcon(R.mipmap.push_small_logo)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.push_local))
                        .setColor(context.getResources().getColor(R.color.primaryLogo))
                        .setContentTitle(String.format("%s with %s", title,
                                DatabaseHelper.getInstance().getNameFromContactRoster(getInputData().getString("jid"))))
                        .setContentText(content0)
                        .setAutoCancel(true)
                        .setGroup("Reminder")
                        .setSound(savedSoundUri)
                        .setVibrate(vibrate)
                        .setContentIntent(pendingIntent)
                        .addAction(replyAction.build())
                        .addAction(naviAction.build())
                        .setLights(Color.BLUE, lights, lights);
            }

            int id = String.format("reminder-%s", apptId).hashCode();
            final NotificationManager nm = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify("ApptReminder", id, notificationBuilder.build());
            nm.notify("ApptReminder", id, notificationBuilder.build());

        }

        return Result.SUCCESS;
    }
}
