package com.nhom3.pubgapp.ui.trade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Payment;
import com.nhom3.pubgapp.model.enums.DealStatus;

import org.junit.Test;

// Pure rules behind the terms dialog countdown and the payment notice of C03.
public class DealTermsFlowTest {

    @Test
    public void secondsLeft_roundsUpSoTheLabelNeverShowsZeroEarly() {
        assertEquals(5, CountdownConfirmDialogFragment.secondsLeft(5000L));
        assertEquals(5, CountdownConfirmDialogFragment.secondsLeft(4200L));
        assertEquals(1, CountdownConfirmDialogFragment.secondsLeft(1L));
        assertEquals(0, CountdownConfirmDialogFragment.secondsLeft(0L));
        assertEquals(0, CountdownConfirmDialogFragment.secondsLeft(-10L));
    }

    @Test
    public void shouldShowPaymentCard_hidesCancelledDealWithoutPayment() {
        Deal deal = new Deal();
        deal.setAgreedPrice(500000L);
        deal.setStatus(DealStatus.CANCELLED);
        assertFalse(DealDetailFragment.shouldShowPaymentCard(deal, null));

        deal.setStatus(DealStatus.PENDING_CONFIRM);
        assertTrue(DealDetailFragment.shouldShowPaymentCard(deal, null));

        deal.setStatus(DealStatus.CANCELLED);
        assertTrue(DealDetailFragment.shouldShowPaymentCard(deal, new Payment()));

        deal.setAgreedPrice(0L);
        assertFalse(DealDetailFragment.shouldShowPaymentCard(deal, new Payment()));
        assertFalse(DealDetailFragment.shouldShowPaymentCard(null, null));
    }
}
