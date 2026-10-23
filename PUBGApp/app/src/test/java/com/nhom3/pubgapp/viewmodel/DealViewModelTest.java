package com.nhom3.pubgapp.viewmodel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.enums.DealStatus;

import org.junit.Test;

// Tests DealViewModel button and action guard conditions without Firebase.
public class DealViewModelTest {

    @Test
    public void canEditTerms_allowsOnlyPendingConfirmParticipantVerifiedAndOnline() {
        Deal deal = createSampleDeal(DealStatus.PENDING_CONFIRM);

        assertTrue(DealViewModel.canEditTerms(deal, "buyer1", true, true));
        assertTrue(DealViewModel.canEditTerms(deal, "seller1", true, true));

        assertFalse(DealViewModel.canEditTerms(deal, "buyer1", false, true));
        assertFalse(DealViewModel.canEditTerms(deal, "buyer1", true, false));
        assertFalse(DealViewModel.canEditTerms(deal, "stranger", true, true));

        deal.setStatus(DealStatus.CONFIRMED);
        assertFalse(DealViewModel.canEditTerms(deal, "buyer1", true, true));

        deal.setStatus(DealStatus.SHIPPED);
        assertFalse(DealViewModel.canEditTerms(deal, "buyer1", true, true));

        deal.setStatus(DealStatus.CANCELLED);
        assertFalse(DealViewModel.canEditTerms(deal, "buyer1", true, true));

        assertFalse(DealViewModel.canEditTerms(null, "buyer1", true, true));
    }

    @Test
    public void canConfirm_allowsOnlyPendingConfirmWhenNotYetConfirmed() {
        Deal deal = createSampleDeal(DealStatus.PENDING_CONFIRM);
        deal.setBuyerConfirmed(false);
        deal.setSellerConfirmed(false);

        assertTrue(DealViewModel.canConfirm(deal, "buyer1", true, true));
        assertTrue(DealViewModel.canConfirm(deal, "seller1", true, true));

        deal.setBuyerConfirmed(true);
        assertFalse(DealViewModel.canConfirm(deal, "buyer1", true, true));
        assertTrue(DealViewModel.canConfirm(deal, "seller1", true, true));

        deal.setSellerConfirmed(true);
        assertFalse(DealViewModel.canConfirm(deal, "seller1", true, true));

        assertFalse(DealViewModel.canConfirm(deal, "buyer1", false, true));
        assertFalse(DealViewModel.canConfirm(deal, "buyer1", true, false));

        deal.setStatus(DealStatus.CONFIRMED);
        assertFalse(DealViewModel.canConfirm(deal, "buyer1", true, true));
    }

    @Test
    public void canCancel_doesNotRequireVerificationAndAllowsPendingConfirmOrConfirmed() {
        Deal deal = createSampleDeal(DealStatus.PENDING_CONFIRM);

        assertTrue(DealViewModel.canCancel(deal, "buyer1", true));
        assertTrue(DealViewModel.canCancel(deal, "seller1", true));

        deal.setStatus(DealStatus.CONFIRMED);
        assertTrue(DealViewModel.canCancel(deal, "buyer1", true));
        assertTrue(DealViewModel.canCancel(deal, "seller1", true));

        deal.setStatus(DealStatus.SHIPPED);
        assertFalse(DealViewModel.canCancel(deal, "buyer1", true));

        deal.setStatus(DealStatus.RECEIVED);
        assertFalse(DealViewModel.canCancel(deal, "buyer1", true));

        deal.setStatus(DealStatus.COMPLETED);
        assertFalse(DealViewModel.canCancel(deal, "buyer1", true));

        deal.setStatus(DealStatus.CANCELLED);
        assertFalse(DealViewModel.canCancel(deal, "buyer1", true));

        assertFalse(DealViewModel.canCancel(deal, "buyer1", false));
        assertFalse(DealViewModel.canCancel(deal, "stranger", true));
        assertFalse(DealViewModel.canCancel(null, "buyer1", true));
    }

    @Test
    public void isPaymentInfoVisible_showsOnlyWhenNeedsPayment() {
        Deal deal = createSampleDeal(DealStatus.CONFIRMED);
        deal.setAgreedPrice(500000);
        assertTrue(DealViewModel.isPaymentInfoVisible(deal));

        deal.setAgreedPrice(0);
        assertFalse(DealViewModel.isPaymentInfoVisible(deal));

        assertFalse(DealViewModel.isPaymentInfoVisible(null));
    }

    private Deal createSampleDeal(DealStatus status) {
        Deal deal = new Deal();
        deal.setId("deal1");
        deal.setBuyerId("buyer1");
        deal.setSellerId("seller1");
        deal.setStatus(status);
        return deal;
    }
}
