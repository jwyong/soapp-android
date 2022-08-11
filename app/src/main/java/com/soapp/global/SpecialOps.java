package com.soapp.global;

import android.content.Context;

import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;

public class SpecialOps {
    private Preferences preferences = Preferences.getInstance();
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();

    public void executeSpOps(Context context, int prevVersionCode, int currentVersionCode) {
        //spOps1 - drop workManager db
        int spVerCode = 106;
        if (prevVersionCode < spVerCode) {
            Soapp.getInstance().deleteDatabase("androidx.work.workdb");
            Soapp.getInstance().setReminderCheckingPeriodically(false);

            //save previous ver code
            preferences.saveint(context, GlobalVariables.prevVerCode, spVerCode);

            //restart soapp
//            Intent mStartActivity = new Intent(context, Splash.class);
//            int mPendingIntentId = android.os.Process.myPid();
//
//            PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity,
//                    PendingIntent.FLAG_CANCEL_CURRENT);
//            AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);

//            android.os.Process.killProcess(mPendingIntentId);
        }

        //spOps2
        spVerCode = 139;
        if (prevVersionCode < spVerCode) {
            //delete all profileFull
            databaseHelper.SPdeleteDefaultImage();

            //set mediaStatus for outgoing vid from -7 (compressing) to 7 (cancel compress)
            databaseHelper.SPClearOutVid();
        }

        //after done spOps, need to save currentVer to pref
        preferences.saveint(context, GlobalVariables.prevVerCode, currentVersionCode);
    }
}
