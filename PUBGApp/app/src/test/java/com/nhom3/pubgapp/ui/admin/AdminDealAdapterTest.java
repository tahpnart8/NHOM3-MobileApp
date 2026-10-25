package com.nhom3.pubgapp.ui.admin;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.enums.DealStatus;

import org.junit.Test;

import java.util.Date;

// Unit tests for admin deal adapter overdue marking rules.
public class AdminDealAdapterTest {

    @Test
    public void isOverdue_returnsFalseWhenDealOrNowIsNull() {
        Timestamp now = new Timestamp(new Date(1700000000000L));
        Deal deal = new Deal();
        deal.setStatus(DealStatus.SHIPPED);
        deal.setAutoReleaseAt(now);

        assertFalse(AdminDealAdapter.isOverdue(null, now));
        assertFalse(AdminDealAdapter.isOverdue(deal, null));
        assertFalse(AdminDealAdapter.isOverdue(null, null));
    }

    @Test
    public void isOverdue_returnsFalseForNonShippedStatusesEvenIfDatePassed() {
        long nowMillis = 1700000000000L;
        Timestamp now = new Timestamp(new Date(nowMillis));
        Timestamp past = new Timestamp(new Date(nowMillis - 3600000L));

        Deal deal = new Deal();
        deal.setAutoReleaseAt(past);

        deal.setStatus(DealStatus.PENDING_CONFIRM);
        assertFalse(AdminDealAdapter.isOverdue(deal, now));

        deal.setStatus(DealStatus.CONFIRMED);
        assertFalse(AdminDealAdapter.isOverdue(deal, now));

        deal.setStatus(DealStatus.RECEIVED);
        assertFalse(AdminDealAdapter.isOverdue(deal, now));

        deal.setStatus(DealStatus.COMPLETED);
        assertFalse(AdminDealAdapter.isOverdue(deal, now));

        deal.setStatus(DealStatus.CANCELLED);
        assertFalse(AdminDealAdapter.isOverdue(deal, now));

        deal.setStatus(DealStatus.DISPUTED);
        assertFalse(AdminDealAdapter.isOverdue(deal, now));
    }

    @Test
    public void isOverdue_handlesShippedStatusWithFuturePastAndNullDates() {
        long nowMillis = 1700000000000L;
        Timestamp now = new Timestamp(new Date(nowMillis));
        Timestamp past = new Timestamp(new Date(nowMillis - 86400000L));
        Timestamp future = new Timestamp(new Date(nowMillis + 86400000L));

        Deal deal = new Deal();
        deal.setStatus(DealStatus.SHIPPED);

        deal.setAutoReleaseAt(null);
        assertFalse(AdminDealAdapter.isOverdue(deal, now));

        deal.setAutoReleaseAt(future);
        assertFalse(AdminDealAdapter.isOverdue(deal, now));

        deal.setAutoReleaseAt(now);
        assertTrue(AdminDealAdapter.isOverdue(deal, now));

        deal.setAutoReleaseAt(past);
        assertTrue(AdminDealAdapter.isOverdue(deal, now));
    }
}
