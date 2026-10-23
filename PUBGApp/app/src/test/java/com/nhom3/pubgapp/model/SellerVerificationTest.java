package com.nhom3.pubgapp.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.enums.VerificationStatus;

import org.junit.Test;

public class SellerVerificationTest {

    private SellerVerification verificationWith(VerificationStatus status, int resubmitCount) {
        SellerVerification verification = new SellerVerification();
        verification.setStatus(status);
        verification.setResubmitCount(resubmitCount);
        return verification;
    }

    @Test
    public void canResubmit_rejectedUnderLimit_isTrue() {
        assertTrue(verificationWith(VerificationStatus.REJECTED, 0).canResubmit());
        assertTrue(verificationWith(VerificationStatus.REJECTED, 2).canResubmit());
    }

    @Test
    public void canResubmit_rejectedAtLimit_isFalse() {
        assertFalse(verificationWith(VerificationStatus.REJECTED, 3).canResubmit());
        assertFalse(verificationWith(VerificationStatus.REJECTED, 4).canResubmit());
    }

    @Test
    public void canResubmit_notRejected_isFalse() {
        assertFalse(verificationWith(VerificationStatus.NONE, 0).canResubmit());
        assertFalse(verificationWith(VerificationStatus.PENDING, 0).canResubmit());
        assertFalse(verificationWith(VerificationStatus.APPROVED, 0).canResubmit());
    }

    @Test
    public void remainingResubmits_countsDownToZero() {
        assertEquals(3, verificationWith(VerificationStatus.REJECTED, 0).remainingResubmits());
        assertEquals(1, verificationWith(VerificationStatus.REJECTED, 2).remainingResubmits());
        assertEquals(0, verificationWith(VerificationStatus.REJECTED, 3).remainingResubmits());
    }

    @Test
    public void remainingResubmits_neverNegative() {
        assertEquals(0, verificationWith(VerificationStatus.REJECTED, 7).remainingResubmits());
    }

    @Test(expected = IllegalStateException.class)
    public void reject_whenNotPending_throws() {
        verificationWith(VerificationStatus.APPROVED, 0).reject("admin_01", "Ảnh mờ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void reject_withoutNote_throws() {
        verificationWith(VerificationStatus.PENDING, 0).reject("admin_01", " ");
    }
}
