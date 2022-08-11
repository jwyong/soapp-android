package com.soapp.setup;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.net.TrafficStats;
import android.os.Build;
import android.os.StrictMode;

import com.bumptech.glide.request.target.CustomViewTarget;
import com.soapp.WorkManager.PeriodicReminderWorker;
import com.soapp.WorkManager.VersionCtrlWorker;
import com.soapp.global.CheckInternetAvaibility;
import com.soapp.global.GlobalVariables;
import com.soapp.sql.room.DataRepository;
import com.soapp.sql.room.SoappDatabase;

import java.util.concurrent.TimeUnit;

import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import io.branch.referral.BranchApp;

public class Soapp extends BranchApp {
    private static Soapp mInstance;

    //for deep link sharing
//    private InstallListener installListener = new InstallListener();

    public static synchronized Soapp getInstance() {
        return mInstance;
    }

    public static synchronized SoappDatabase getDatabase() {
        if (mInstance != null) {
            return SoappDatabase.getInstance(mInstance);
        }
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //set debug
//        isDebug();

        //set singleton instance for db
        mInstance = this;

        //check internet connection
        CheckInternetAvaibility.getINSTANCE();

//        rescheduleUpload();

        //create push notifications for > Android 8
        createNotificationChannel();

        //set reminder for next 7 days
        setReminderCheckingPeriodically(false);
        checkversionCtrlPeriodically();

//        Configuration configuration = new Configuration.Builder()
//                .setJobSchedulerJobIdRange(1, 10000)
//                .setMaxSchedulerLimit(100)
//                .setMinimumLoggingLevel(Log.VERBOSE)
//                .build();
//
//        WorkManager.initialize(this, configuration);

        //check for sharing referer (register receiver)
//        if(!Preferences.getInstance().getValue(this, GlobalVariables.STR_SHARER).equals("1")) {
// no need register
// if already 1
//            registerReceiver(installListener, new IntentFilter("com.android.vending
// .INSTALL_REFERRER"));
//        }
    }

    //init singleton database
    public DataRepository getRepository() {
        return DataRepository.getInstance(SoappDatabase.getInstance(mInstance));
    }

//    public static void rescheduleUpload() {
//
//        Intent intentWake = new Intent("com.soapp.localSche");
//        intentWake.setClass(Soapp.getInstance().getApplicationContext(), LocalBroadcastReceiver
// .class);
//        intentWake.putExtra("tag", "uploaderror");
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(Soapp.getInstance()
//                .getApplicationContext(), 88888, intentWake, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        Random r = new Random();
//        int minute = r.nextInt(59);
//
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(System.currentTimeMillis());
//        calendar.set(Calendar.HOUR_OF_DAY, 1);
//        calendar.set(Calendar.MINUTE, minute);
//
//        AlarmManager alarmManager = (AlarmManager) Soapp.getInstance().getApplicationContext()
// .getSystemService
// (Context.ALARM_SERVICE);
//
//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
//                AlarmManager.INTERVAL_DAY, pendingIntent);
//    }

    //function for debugging
    private void isDebug() {
//        if (isDebug.equals("1")) { //debug
        //strict mode
        TrafficStats.setThreadStatsTag(3333);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .penaltyFlashScreen()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());

        //debugGhost for DB
//            try {
//                new DebugGhostBridge(this, "soappDB", 8);
////                Class aClass = Class.forName("com.sanid.lib.debugghost.DebugGhostBridge");
////                Constructor constructor = aClass.getConstructor(new Class[]{Context.class,
////                        String.class, Integer.TYPE});
////                Method method = aClass.getMethod("startDebugGhost");
////
////                Object dgb = constructor.newInstance(Soapp.getInstance(),
////                        "soappDB", 8);
////                method.invoke(dgb, new Object[]{});
//
//            } catch (Exception e) {
//                Log.i("DebugGhost", "DebugGhost not loaded: " + e.getMessage());
//            }
//        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Chat";
            String description = "Receive push notifications for incoming messages";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channelChat = new NotificationChannel("Chat", name, importance);
            channelChat.setDescription(description);

            NotificationChannel channelChatGrp =
                    new NotificationChannel(GlobalVariables.chatNotiGroup, name + " " +
                            "Group", NotificationManager.IMPORTANCE_LOW);
            channelChat.setDescription("Group incoming messages notifications");

            NotificationChannel channelApptGrp =
                    new NotificationChannel(GlobalVariables.apptNotiGroup, "Appointment " +
                            "Group", NotificationManager.IMPORTANCE_LOW);
            channelChat.setDescription("Group appointment updates notifications");

            NotificationChannel channelAppt = new NotificationChannel("Appointment", "Appointment"
                    , importance);
            channelAppt.setDescription("Receive push notifications for appointment updates");

            NotificationChannel channelRes = new NotificationChannel("Restaurant", "Restaurant",
                    importance);
            channelRes.setDescription("Receive push notifications for booking updates");

            NotificationChannel channelAlarm = new NotificationChannel("Reminder", "Reminder",
                    importance);
            channelAlarm.setDescription("Receive reminder for appointments and bookings on timely" +
                    " manner");

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channelChat);
            notificationManager.createNotificationChannel(channelAppt);
            notificationManager.createNotificationChannel(channelRes);
            notificationManager.createNotificationChannel(channelAlarm);

            notificationManager.createNotificationChannel(channelChatGrp);
            notificationManager.createNotificationChannel(channelApptGrp);

            notificationManager.createNotificationChannel(new NotificationChannel("Debug", "Debug",
                    NotificationManager.IMPORTANCE_LOW));
        }
    }

    public void setReminderCheckingPeriodically(boolean needAlertDialog) {
        Data data = new Data.Builder()
                .putBoolean("needAlertDialog", needAlertDialog)
                .build();

        PeriodicWorkRequest periodicWorkRequest =
                new PeriodicWorkRequest.Builder(PeriodicReminderWorker.class, 1,
                        TimeUnit.DAYS)
                        .addTag("periodicSetReminder")
                        .setInputData(data)
                        .build();
        WorkManager.getInstance().enqueueUniquePeriodicWork("periodicSetReminder",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest);
    }

    public void checkversionCtrlPeriodically() {
        PeriodicWorkRequest versionCtrlPeriodic =
                new PeriodicWorkRequest.Builder(VersionCtrlWorker.class, 1,
                        TimeUnit.DAYS)
                        .addTag("versionCtrlPeriodic")
                        .build();
        WorkManager.getInstance().enqueueUniquePeriodicWork("versionCtrlPeriodic",
                ExistingPeriodicWorkPolicy.KEEP,
                versionCtrlPeriodic);
    }
}




