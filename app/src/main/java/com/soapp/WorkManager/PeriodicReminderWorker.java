package com.soapp.WorkManager;

import android.content.ContentValues;
import android.content.Context;

import com.soapp.R;
import com.soapp.global.UIHelper;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.SoappDatabase;
import com.soapp.sql.room.entity.Appointment;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;
import androidx.work.Worker;

public class PeriodicReminderWorker extends Worker {
    @NonNull
    @Override
    public Result doWork() {

        SoappDatabase rdb = Soapp.getDatabase();

        long endTime = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(30);
        List<Appointment> list = rdb.selectQuery().getRecentAppttoRemind(endTime);
        ContentValues cv = new ContentValues();

        WorkManager.getInstance().cancelAllWorkByTag("ReminderAppt"); //XX minutes before appt
        WorkManager.getInstance().cancelAllWorkByTag("ExactReminderAppt"); //appt happening now
        WorkManager.getInstance().cancelAllWorkByTag("DeleteReminderAppt"); //shift appt to History XX hours after appt


        int count = 0;
        for (Appointment appt: list) {
            cv.clear();
            if (appt.getApptTime() < System.currentTimeMillis()) {

                DatabaseHelper.getInstance().deleteApptID(null, appt.getApptID(), false, null, null, false);
            } else {
                count = +DatabaseHelper.getInstance().scheduleLocalNotification(appt.getApptID());
            }

        }

        //show alert dialog if need
        if (count > 0 && getInputData().getBoolean("needAlertDialog", false)) {
            Context context = Soapp.getInstance().getApplicationContext();

            //get pref and use global func to convert to readable time string
//            String readerTime = ;

            String msg = String.format(Locale.ENGLISH, "There %s %d %s upcoming", count == 1 ? "is" : "are",
                    count, count == 1 ? "appointment" : "appointments");

            new UIHelper().dialog2Btns(context, context.getString(R.string.appt_next_reminder_title),
                    msg, R.string.ok_label, R.string.cancel, R.color.white, R.color.primaryDark3,
                    null, null, true);
        }

        return Result.SUCCESS;
    }

}
