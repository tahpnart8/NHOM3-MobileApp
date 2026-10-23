package com.nhom3.pubgapp.util;

import android.content.Context;

import androidx.lifecycle.LiveData;

/** Tracks connectivity so screens can choose local or network data. */
public class NetworkMonitor {

    private static NetworkMonitor instance;

    private LiveData<Boolean> online;

    public static NetworkMonitor getInstance() {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void register(Context context) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void unregister() {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public boolean isOnline() {
        throw new UnsupportedOperationException("NOT_READY");
    }
}
