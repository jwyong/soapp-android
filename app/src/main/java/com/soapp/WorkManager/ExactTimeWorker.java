package com.soapp.WorkManager;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;

import com.soapp.R;
import com.soapp.home.Home;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.entity.Appointment;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;

public class ExactTimeWorker extends Worker {
    @NonNull
    @Override
    public Result doWork() {
        String apptId = getInputData().getString("apptId");
        String tittle = DatabaseHelper.getInstance().getApptTitle(apptId);
        Context context = Soapp.getInstance().getApplicationContext();

        Uri savedSoundUri = Uri.parse("android.resource://com.soapp/" + R.raw
                .soapp_appt);
        long[] vibrate = new long[]{300, 300};

        Intent intentHome = new Intent(Soapp.getInstance().getApplicationContext(), Home.class);
        intentHome.putExtra("remoteSche", "1");

        PendingIntent pendingIntent = PendingIntent.getActivity(Soapp.getInstance().getApplicationContext(), 0, intentHome,
                PendingIntent.FLAG_ONE_SHOT);

        int lights = ((int) TimeUnit.SECONDS.toMillis(1));

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

        NotificationCompat.Action.Builder replyAction = new NotificationCompat.Action.Builder(R.drawable.ic_green_dot_42px,
                "Waze", wazePendingIntent);
        NotificationCompat.Action.Builder naviAction = new NotificationCompat.Action.Builder(R.drawable.ic_green_dot_42px,
                "Google Maps", gmapsPendingIntent);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "Reminder")
                .setSmallIcon(R.mipmap.push_small_logo)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.push_local))
                .setColor(context.getResources().getColor(R.color.primaryLogo))
                .setContentTitle(String.format("%s with %s", tittle, DatabaseHelper.getInstance().getNameFromContactRoster(getInputData().getString("jid"))))
                .setContentText(tittle + " is happening now")
                .setAutoCancel(true)
                .setGroup("Reminder")
                .setSound(savedSoundUri)
                .setVibrate(vibrate)
                .setContentIntent(pendingIntent)
                .setLights(Color.BLUE, lights,lights)
                .addAction(replyAction.build())
                .addAction(naviAction.build());

        int id = String.format("reminder-%s", apptId).hashCode();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify("ApptReminder", id, notificationBuilder.build());

        return Result.SUCCESS;
    }
}
