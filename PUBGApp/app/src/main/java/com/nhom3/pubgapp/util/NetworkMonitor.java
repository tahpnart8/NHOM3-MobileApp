package com.nhom3.pubgapp.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/** Tracks connectivity so screens can choose local or network data. */
public class NetworkMonitor {

    private static NetworkMonitor instance;

    private final MutableLiveData<Boolean> onlineSource = new MutableLiveData<Boolean>(Boolean.FALSE);
    private LiveData<Boolean> online = onlineSource;
    // Read from any thread by isOnline, written by the system callback thread.
    private volatile boolean connected;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback callback;

    public static synchronized NetworkMonitor getInstance() {
        if (instance == null) {
            instance = new NetworkMonitor();
        }
        return instance;
    }

    public synchronized void register(Context context) {
        if (callback != null || context == null) {
            return;
        }
        // The application context outlives any screen, so holding the manager cannot leak one.
        connectivityManager = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return;
        }
        setConnected(hasInternet(connectivityManager.getActiveNetwork()));
        callback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                setConnected(true);
            }

            @Override
            public void onLost(@NonNull Network network) {
                setConnected(false);
            }
        };
        connectivityManager.registerDefaultNetworkCallback(callback);
    }

    public synchronized void unregister() {
        if (connectivityManager != null && callback != null) {
            connectivityManager.unregisterNetworkCallback(callback);
        }
        callback = null;
        connectivityManager = null;
    }

    /** False until register has run, so a screen never offers a network action on an unknown state. */
    public boolean isOnline() {
        return connected;
    }

    private boolean hasInternet(Network network) {
        if (network == null || connectivityManager == null) {
            return false;
        }
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    // postValue, because the system calls the callback off the main thread.
    private void setConnected(boolean value) {
        connected = value;
        onlineSource.postValue(Boolean.valueOf(value));
    }
}
