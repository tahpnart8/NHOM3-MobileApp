package com.nhom3.pubgapp.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.nhom3.pubgapp.R;

/** Hears the system broadcast sent when airplane mode is switched on or off. */
public class AirplaneModeReceiver extends BroadcastReceiver {

    // The system puts the new state in this boolean extra of ACTION_AIRPLANE_MODE_CHANGED.
    private static final String EXTRA_STATE = "state";

    /** Told the new state each time the user switches airplane mode. */
    public interface Listener {
        void onAirplaneModeChanged(boolean on);
    }

    private final Listener listener;

    public AirplaneModeReceiver(Listener listener) {
        this.listener = listener;
    }

    /** The only broadcast this receiver wants; the screen passes it to registerReceiver. */
    public static IntentFilter createFilter() {
        return new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())) {
            return;
        }
        boolean on = intent.getBooleanExtra(EXTRA_STATE, false);
        listener.onAirplaneModeChanged(on);
    }

    /** The message shown to the user for the new state. */
    public static int messageFor(boolean on) {
        if (on) {
            return R.string.common_airplane_mode_on;
        }
        return R.string.common_airplane_mode_off;
    }
}
