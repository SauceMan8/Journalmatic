package com.sawyergehring.journalmatic;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.widget.Toast;

public class MyJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        Toast.makeText(this, "JobService", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
