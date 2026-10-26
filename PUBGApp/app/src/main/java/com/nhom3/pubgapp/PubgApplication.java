package com.nhom3.pubgapp;

import android.app.Application;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.nhom3.pubgapp.util.AutoReleaseWorker;
import com.nhom3.pubgapp.util.NetworkMonitor;

import java.util.concurrent.TimeUnit;

public class PubgApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NetworkMonitor.getInstance().register(this);
        scheduleAutoRelease();
    }

    private void scheduleAutoRelease() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                AutoReleaseWorker.class,
                15,
                TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                AutoReleaseWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request);
    }
}
