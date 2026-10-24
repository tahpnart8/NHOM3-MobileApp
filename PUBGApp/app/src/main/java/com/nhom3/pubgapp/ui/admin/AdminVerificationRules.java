package com.nhom3.pubgapp.ui.admin;

import com.nhom3.pubgapp.model.SellerVerification;
import com.nhom3.pubgapp.model.enums.UserStatus;
import com.nhom3.pubgapp.model.enums.VerificationStatus;

import java.util.List;

// Pure rules for the verification block of E03, kept apart from the Fragment so a unit test can reach them.
public final class AdminVerificationRules {

    private AdminVerificationRules() {
    }

    // A request can be decided only while it is PENDING and the account is still ACTIVE.
    public static boolean canReview(UserStatus userStatus, VerificationStatus requestStatus) {
        return userStatus == UserStatus.ACTIVE && requestStatus == VerificationStatus.PENDING;
    }

    // Rejecting needs a reason that is not blank.
    public static boolean isRejectReasonValid(String reason) {
        return reason != null && !reason.trim().isEmpty();
    }

    // The pending list holds every user's request; E03 shows only the one of the user on screen.
    public static SellerVerification findRequest(List<SellerVerification> requests, String userId) {
        if (requests == null || userId == null) {
            return null;
        }
        for (SellerVerification request : requests) {
            if (userId.equals(request.getUserId())) {
                return request;
            }
        }
        return null;
    }
}
