package com.soapp.WorkManager;

import com.soapp.sql.DatabaseHelper;

import androidx.annotation.NonNull;
import androidx.work.Worker;

public class DeleteApptWorker extends Worker {
    @NonNull
    @Override
    public Result doWork() {
        String apptId = getInputData().getString("apptId");

        DatabaseHelper.getInstance().deleteApptID(null, apptId, false, null, null, false);

        return Result.SUCCESS;
    }
}
