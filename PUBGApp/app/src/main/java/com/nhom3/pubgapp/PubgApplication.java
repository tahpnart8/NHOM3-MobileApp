package com.nhom3.pubgapp;

import android.app.Application;

import com.nhom3.pubgapp.util.NetworkMonitor;

public class PubgApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NetworkMonitor.getInstance().register(this);
        // Room and WorkManager are wired here once their bodies are filled in wave 1.
    }
}
