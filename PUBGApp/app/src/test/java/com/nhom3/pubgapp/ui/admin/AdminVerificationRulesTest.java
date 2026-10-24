package com.nhom3.pubgapp.ui.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.SellerVerification;
import com.nhom3.pubgapp.model.enums.UserStatus;
import com.nhom3.pubgapp.model.enums.VerificationStatus;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AdminVerificationRulesTest {

    @Test
    public void onlyActiveAccountWithPendingRequestCanBeReviewed() {
        int allowed = 0;
        for (UserStatus userStatus : UserStatus.values()) {
            for (VerificationStatus requestStatus : VerificationStatus.values()) {
                boolean expected = userStatus == UserStatus.ACTIVE
                        && requestStatus == VerificationStatus.PENDING;
                assertEquals(expected, AdminVerificationRules.canReview(userStatus, requestStatus));
                if (expected) {
                    allowed++;
                }
            }
        }
        assertEquals(1, allowed);
    }

    @Test
    public void lockedPendingRequestCannotBeReviewed() {
        assertFalse(AdminVerificationRules.canReview(UserStatus.TEMP_LOCKED, VerificationStatus.PENDING));
        assertFalse(AdminVerificationRules.canReview(UserStatus.PERM_LOCKED, VerificationStatus.PENDING));
        assertFalse(AdminVerificationRules.canReview(UserStatus.DELETED, VerificationStatus.PENDING));
    }

    @Test
    public void nullStatusesCannotBeReviewed() {
        assertFalse(AdminVerificationRules.canReview(null, VerificationStatus.PENDING));
        assertFalse(AdminVerificationRules.canReview(UserStatus.ACTIVE, null));
    }

    @Test
    public void blankRejectReasonIsInvalid() {
        assertFalse(AdminVerificationRules.isRejectReasonValid(null));
        assertFalse(AdminVerificationRules.isRejectReasonValid(""));
        assertFalse(AdminVerificationRules.isRejectReasonValid("   \n "));
    }

    @Test
    public void rejectReasonWithTextIsValid() {
        assertTrue(AdminVerificationRules.isRejectReasonValid("Ảnh mờ"));
        assertTrue(AdminVerificationRules.isRejectReasonValid("  Số CCCD không khớp  "));
    }

    @Test
    public void findRequestPicksTheRequestOfThatUser() {
        List<SellerVerification> requests = new ArrayList<SellerVerification>();
        SellerVerification first = request("user-a");
        SellerVerification second = request("user-b");
        requests.add(first);
        requests.add(second);
        assertSame(second, AdminVerificationRules.findRequest(requests, "user-b"));
        assertSame(first, AdminVerificationRules.findRequest(requests, "user-a"));
    }

    @Test
    public void findRequestReturnsNullWhenNothingMatches() {
        List<SellerVerification> requests = new ArrayList<SellerVerification>();
        requests.add(request("user-a"));
        assertNull(AdminVerificationRules.findRequest(requests, "user-z"));
        assertNull(AdminVerificationRules.findRequest(requests, null));
        assertNull(AdminVerificationRules.findRequest(null, "user-a"));
    }

    private SellerVerification request(String userId) {
        SellerVerification request = new SellerVerification();
        request.setUserId(userId);
        return request;
    }
}
