package com.nhom3.pubgapp.util;

import static org.junit.Assert.assertEquals;

import com.nhom3.pubgapp.R;

import org.junit.Test;

public class AirplaneModeReceiverTest {

    @Test
    public void messageFor_on_saysSavedDataStillWorks() {
        assertEquals(R.string.common_airplane_mode_on, AirplaneModeReceiver.messageFor(true));
    }

    @Test
    public void messageFor_off_saysReconnecting() {
        assertEquals(R.string.common_airplane_mode_off, AirplaneModeReceiver.messageFor(false));
    }
}
