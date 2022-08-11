package com.soapp.local_notifications;

import com.firebase.jobdispatcher.JobParameters;
import com.soapp.sql.DatabaseHelper;

/* Created by Soapp on 07/11/2017. */

public class SoappJobScheduler extends com.firebase.jobdispatcher.JobService {
    @Override
    public boolean onStartJob(JobParameters job) {
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }
}