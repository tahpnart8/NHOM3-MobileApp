package com.nhom3.pubgapp.ui.admin;

import androidx.annotation.Nullable;

import com.nhom3.pubgapp.model.enums.ListingStatus;

// Which moderation buttons a listing status allows. The Security Rules decide again at write time.
public final class AdminListingActionRules {

    private AdminListingActionRules() {
    }

    public static boolean canHide(@Nullable ListingStatus status) {
        return status == ListingStatus.AVAILABLE;
    }

    // Only a listing hidden by an admin has a reason; the owner's own hide leaves it empty.
    public static boolean canRestore(@Nullable ListingStatus status, @Nullable String statusReason) {
        if (status != ListingStatus.HIDDEN) {
            return false;
        }
        return statusReason != null && !statusReason.trim().isEmpty();
    }

    public static boolean canRemove(@Nullable ListingStatus status) {
        return status == ListingStatus.DRAFT
                || status == ListingStatus.AVAILABLE
                || status == ListingStatus.HIDDEN;
    }
}
