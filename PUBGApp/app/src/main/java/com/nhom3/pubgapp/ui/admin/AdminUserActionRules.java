package com.nhom3.pubgapp.ui.admin;

import androidx.annotation.Nullable;

import com.nhom3.pubgapp.model.Warning;
import com.nhom3.pubgapp.model.enums.UserStatus;

import java.util.Date;

// Pure rules for the action block of E03: which buttons a status allows and how the inputs are read.
public final class AdminUserActionRules {

    static final int MAX_LOCK_DAYS = 3650;
    private static final long MILLIS_PER_DAY = 24L * 60L * 60L * 1000L;

    private AdminUserActionRules() {
    }

    public static boolean canWarn(@Nullable UserStatus status) {
        return status == UserStatus.ACTIVE;
    }

    public static boolean canLock(@Nullable UserStatus status) {
        return status == UserStatus.ACTIVE;
    }

    public static boolean canUnlock(@Nullable UserStatus status) {
        return status == UserStatus.TEMP_LOCKED || status == UserStatus.PERM_LOCKED;
    }

    public static boolean canRemoveAllListings(@Nullable UserStatus status) {
        return status == UserStatus.TEMP_LOCKED || status == UserStatus.PERM_LOCKED;
    }

    // The status the next warning gives. It comes from Warning, so the three-tier ladder lives in one place.
    public static UserStatus nextWarningStatus(int warningCount) {
        Warning next = new Warning();
        next.setLevel(warningCount + 1);
        return next.resultingStatus();
    }

    public static boolean needsLockDuration(int warningCount) {
        return nextWarningStatus(warningCount) == UserStatus.TEMP_LOCKED;
    }

    public static boolean isReasonValid(@Nullable String reason) {
        return reason != null && !reason.trim().isEmpty();
    }

    // Returns null for fewer than one day, which the repository reads as a permanent lock.
    @Nullable
    public static Date lockUntil(long nowMillis, int days) {
        if (days < 1) {
            return null;
        }
        return new Date(nowMillis + days * MILLIS_PER_DAY);
    }

    // Reads the days box: a whole number from 1 to MAX_LOCK_DAYS, otherwise 0.
    static int parseDays(@Nullable String text) {
        if (text == null) {
            return 0;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty() || trimmed.length() > String.valueOf(MAX_LOCK_DAYS).length()) {
            return 0;
        }
        for (int index = 0; index < trimmed.length(); index++) {
            if (!Character.isDigit(trimmed.charAt(index))) {
                return 0;
            }
        }
        int days = Integer.parseInt(trimmed);
        return days > MAX_LOCK_DAYS ? 0 : days;
    }
}
