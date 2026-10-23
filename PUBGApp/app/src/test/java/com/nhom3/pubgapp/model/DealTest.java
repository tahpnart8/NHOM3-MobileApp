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
    }

    @Test
    public void testIsAutoReleaseDue() {
        Deal deal = new Deal();
        long nowMillis = System.currentTimeMillis();
        Timestamp now = new Timestamp(new Date(nowMillis));

        // When not in SHIPPED status
        deal.setStatus(DealStatus.CONFIRMED);
        deal.setAutoReleaseAt(new Timestamp(new Date(nowMillis - 1000)));
        assertFalse(deal.isAutoReleaseDue(now));

        // When in SHIPPED status but not yet due
        deal.setStatus(DealStatus.SHIPPED);
        deal.setAutoReleaseAt(new Timestamp(new Date(nowMillis + 100000)));
        assertFalse(deal.isAutoReleaseDue(now));

        // When in SHIPPED status and due time has passed
        deal.setAutoReleaseAt(new Timestamp(new Date(nowMillis - 1000)));
        assertTrue(deal.isAutoReleaseDue(now));
    }

    @Test
    public void testIsPendingExpired() {
        Deal deal = new Deal();
        long nowMillis = System.currentTimeMillis();
        Timestamp now = new Timestamp(new Date(nowMillis));

        // When status is not PENDING_CONFIRM
        deal.setStatus(DealStatus.CONFIRMED);
        deal.setCreatedAt(new Timestamp(new Date(nowMillis - 4L * 24 * 60 * 60 * 1000)));
        assertFalse(deal.isPendingExpired(now));

        // When status is PENDING_CONFIRM and only 2 days passed
        deal.setStatus(DealStatus.PENDING_CONFIRM);
        deal.setCreatedAt(new Timestamp(new Date(nowMillis - 2L * 24 * 60 * 60 * 1000)));
        assertFalse(deal.isPendingExpired(now));

        // When status is PENDING_CONFIRM and 4 days passed
        deal.setCreatedAt(new Timestamp(new Date(nowMillis - 4L * 24 * 60 * 60 * 1000)));
        assertTrue(deal.isPendingExpired(now));
    }
}
