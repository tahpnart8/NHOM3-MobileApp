package com.nhom3.pubgapp;

import com.nhom3.pubgapp.util.SessionManager;

/** Helper for Espresso tests: ensures clean session state without faking authentication (R12). */
public final class EspressoTestHelper {

    private EspressoTestHelper() {
        // Utility class has private constructor.
    }

    /** Clears any active session to ensure tests execute in guest state. */
    public static void clearSession() {
        SessionManager.getInstance().clear();
    }

    /** Clears any active session to ensure tests execute in guest state. */
    public static void ensureSignedOut() {
        SessionManager.getInstance().clear();
    }
}
