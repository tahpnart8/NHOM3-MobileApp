package com.nhom3.pubgapp.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.enums.DealStatus;

import org.junit.Test;

import java.util.Date;

public class DealTest {

    @Test
    public void testCanMoveTo() {
        Deal deal = new Deal();
        // Initial state
        assertTrue(deal.canMoveTo(DealStatus.PENDING_CONFIRM));
        assertFalse(deal.canMoveTo(DealStatus.CONFIRMED));

        deal.setStatus(DealStatus.PENDING_CONFIRM);
        assertTrue(deal.canMoveTo(DealStatus.CONFIRMED));
        assertTrue(deal.canMoveTo(DealStatus.CANCELLED));
        assertFalse(deal.canMoveTo(DealStatus.SHIPPED));
        assertFalse(deal.canMoveTo(DealStatus.COMPLETED));

        deal.setStatus(DealStatus.CONFIRMED);
        assertTrue(deal.canMoveTo(DealStatus.SHIPPED));
        assertTrue(deal.canMoveTo(DealStatus.RECEIVED));
        assertTrue(deal.canMoveTo(DealStatus.CANCELLED));
        assertTrue(deal.canMoveTo(DealStatus.DISPUTED));
        assertFalse(deal.canMoveTo(DealStatus.COMPLETED));

        deal.setStatus(DealStatus.SHIPPED);
        assertTrue(deal.canMoveTo(DealStatus.RECEIVED));
        assertTrue(deal.canMoveTo(DealStatus.DISPUTED));
        assertFalse(deal.canMoveTo(DealStatus.CANCELLED));
        assertFalse(deal.canMoveTo(DealStatus.COMPLETED));

        deal.setStatus(DealStatus.RECEIVED);
        assertTrue(deal.canMoveTo(DealStatus.COMPLETED));
        assertFalse(deal.canMoveTo(DealStatus.SHIPPED));

        deal.setStatus(DealStatus.COMPLETED);
        assertFalse(deal.canMoveTo(DealStatus.CANCELLED));
        assertFalse(deal.canMoveTo(DealStatus.PENDING_CONFIRM));
        assertFalse(deal.canMoveTo(DealStatus.CONFIRMED));
        assertFalse(deal.canMoveTo(DealStatus.SHIPPED));
        assertFalse(deal.canMoveTo(DealStatus.RECEIVED));
        assertFalse(deal.canMoveTo(DealStatus.DISPUTED));

        // From DISPUTED: 4 valid, 3 invalid
        deal.setStatus(DealStatus.DISPUTED);
        assertTrue(deal.canMoveTo(DealStatus.CONFIRMED));
        assertTrue(deal.canMoveTo(DealStatus.SHIPPED));
        assertTrue(deal.canMoveTo(DealStatus.RECEIVED));
        assertTrue(deal.canMoveTo(DealStatus.CANCELLED));
        assertFalse(deal.canMoveTo(DealStatus.PENDING_CONFIRM));
        assertFalse(deal.canMoveTo(DealStatus.COMPLETED));
        assertFalse(deal.canMoveTo(DealStatus.DISPUTED));

        // From CANCELLED: all false
        deal.setStatus(DealStatus.CANCELLED);
        assertFalse(deal.canMoveTo(DealStatus.PENDING_CONFIRM));
        assertFalse(deal.canMoveTo(DealStatus.CONFIRMED));
        assertFalse(deal.canMoveTo(DealStatus.SHIPPED));
        assertFalse(deal.canMoveTo(DealStatus.RECEIVED));
        assertFalse(deal.canMoveTo(DealStatus.COMPLETED));
        assertFalse(deal.canMoveTo(DealStatus.CANCELLED));
        assertFalse(deal.canMoveTo(DealStatus.DISPUTED));
    }

    @Test
    public void testIsAutoReleaseDue() {
        Deal deal = new Deal();
        long nowMillis = System.currentTimeMillis();
        Timestamp now = new Timestamp(new Date(nowMillis));

        // Null checks
        assertFalse(deal.isAutoReleaseDue(null));
        deal.setStatus(DealStatus.SHIPPED);
        deal.setAutoReleaseAt(null);
        assertFalse(deal.isAutoReleaseDue(now));

        // When not in SHIPPED status
        deal.setStatus(DealStatus.CONFIRMED);
        deal.setAutoReleaseAt(new Timestamp(new Date(nowMillis - 1000)));
        assertFalse(deal.isAutoReleaseDue(now));

        // When in SHIPPED status but not yet due (1ms before)
        deal.setStatus(DealStatus.SHIPPED);
        deal.setAutoReleaseAt(new Timestamp(new Date(nowMillis + 1)));
        assertFalse(deal.isAutoReleaseDue(now));

        // When in SHIPPED status and exact match
        deal.setAutoReleaseAt(now);
        assertTrue(deal.isAutoReleaseDue(now));

        // When in SHIPPED status and due time has passed
        deal.setAutoReleaseAt(new Timestamp(new Date(nowMillis - 1000)));
        assertTrue(deal.isAutoReleaseDue(now));
    }

    @Test
    public void testIsPendingExpired() {
        Deal deal = new Deal();
        long nowMillis = System.currentTimeMillis();
        Timestamp now = new Timestamp(new Date(nowMillis));

        // Null checks
        assertFalse(deal.isPendingExpired(null));
        deal.setStatus(DealStatus.PENDING_CONFIRM);
        deal.setCreatedAt(null);
        assertFalse(deal.isPendingExpired(now));

        // When status is not PENDING_CONFIRM
        deal.setStatus(DealStatus.CONFIRMED);
        deal.setCreatedAt(new Timestamp(new Date(nowMillis - 4L * 24 * 60 * 60 * 1000)));
        assertFalse(deal.isPendingExpired(now));

        // 3 days exact boundary
        long threeDaysMillis = 3L * 24 * 60 * 60 * 1000;

        // Exactly 3 days ago: now == createdAt + 3 days -> expired
        deal.setStatus(DealStatus.PENDING_CONFIRM);
        deal.setCreatedAt(new Timestamp(new Date(nowMillis - threeDaysMillis)));
        assertTrue(deal.isPendingExpired(now));

        // 1 ms before 3 days -> not expired
        deal.setCreatedAt(new Timestamp(new Date(nowMillis - threeDaysMillis + 1)));
        assertFalse(deal.isPendingExpired(now));

        // 1 ms after 3 days -> expired
        deal.setCreatedAt(new Timestamp(new Date(nowMillis - threeDaysMillis - 1)));
        assertTrue(deal.isPendingExpired(now));
    }

    @Test
    public void testModelGuardsForTransitions() {
        Deal deal = new Deal();
        deal.setBuyerId("buyer1");
        deal.setSellerId("seller1");
        deal.setStatus(DealStatus.PENDING_CONFIRM);

        // Cancel with empty reason is rejected
        deal.cancel("buyer1", "");
        org.junit.Assert.assertEquals(DealStatus.PENDING_CONFIRM, deal.getStatus());

        // Cancel with non-participant is rejected
        deal.cancel("stranger", "Reason");
        org.junit.Assert.assertEquals(DealStatus.PENDING_CONFIRM, deal.getStatus());

        // Cancel with valid participant and reason succeeds
        deal.cancel("buyer1", "No longer need item");
        org.junit.Assert.assertEquals(DealStatus.CANCELLED, deal.getStatus());

        // Cannot cancel already cancelled deal
        deal.cancel("buyer1", "Another reason");
        org.junit.Assert.assertEquals(DealStatus.CANCELLED, deal.getStatus());

        // Mark received rejected when not in SHIPPED or CONFIRMED
        Deal deal2 = new Deal();
        deal2.setStatus(DealStatus.PENDING_CONFIRM);
        deal2.markReceived("img_proof");
        org.junit.Assert.assertEquals(DealStatus.PENDING_CONFIRM, deal2.getStatus());

        // Complete rejected when not in RECEIVED
        deal2.setStatus(DealStatus.CONFIRMED);
        deal2.complete();
        org.junit.Assert.assertEquals(DealStatus.CONFIRMED, deal2.getStatus());
    }
}
