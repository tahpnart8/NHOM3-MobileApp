package com.nhom3.pubgapp.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Payment;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.model.enums.DeliveryMethod;
import com.nhom3.pubgapp.model.enums.PaymentStatus;

import org.junit.Test;

import java.util.Date;

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

    @Test
    public void canPay_allowsOnlyConfirmedBuyerWhenNeedsPaymentAndNotYetPaid() {
        Deal deal = createSampleDeal(DealStatus.CONFIRMED);
        deal.setAgreedPrice(500000);

        assertTrue(DealViewModel.canPay(deal, null, "buyer1", true, true));

        assertFalse(DealViewModel.canPay(deal, null, "seller1", true, true));
        assertFalse(DealViewModel.canPay(deal, null, "stranger", true, true));

        deal.setStatus(DealStatus.PENDING_CONFIRM);
        assertFalse(DealViewModel.canPay(deal, null, "buyer1", true, true));

        deal.setStatus(DealStatus.SHIPPED);
        assertFalse(DealViewModel.canPay(deal, null, "buyer1", true, true));

        deal.setStatus(DealStatus.CONFIRMED);
        deal.setAgreedPrice(0);
        assertFalse(DealViewModel.canPay(deal, null, "buyer1", true, true));

        deal.setAgreedPrice(500000);
        com.nhom3.pubgapp.model.Payment heldPayment = new com.nhom3.pubgapp.model.Payment();
        heldPayment.setStatus(com.nhom3.pubgapp.model.enums.PaymentStatus.HELD);
        assertFalse(DealViewModel.canPay(deal, heldPayment, "buyer1", true, true));

        assertFalse(DealViewModel.canPay(deal, null, "buyer1", false, true));
        assertFalse(DealViewModel.canPay(deal, null, "buyer1", true, false));
        assertFalse(DealViewModel.canPay(null, null, "buyer1", true, true));
    }

    @Test
    public void formatRemainingTime_handlesMultipleDaysUnderOneDayZeroAndExpired() {
        long nowMillis = 1700000000000L;
        Timestamp now = new Timestamp(new Date(nowMillis));

        // Multiple days remaining (e.g. 5 days)
        Timestamp future5Days = new Timestamp(new Date(nowMillis + 5L * 24 * 60 * 60 * 1000 + 3600000));
        assertEquals("Còn 5 ngày để xác nhận", DealViewModel.formatRemainingTime(future5Days, now));

        // Under 1 day remaining (e.g. 10 hours 15 mins)
        long underDayMillis = (10L * 60 + 15) * 60 * 1000;
        Timestamp futureHours = new Timestamp(new Date(nowMillis + underDayMillis));
        assertEquals("Còn 10 giờ 15 phút để xác nhận", DealViewModel.formatRemainingTime(futureHours, now));

        // Exactly 0 remaining
        assertEquals("Đã hết đếm ngược, hệ thống sẽ tự chuyển sang Đã nhận",
                DealViewModel.formatRemainingTime(now, now));

        // Past due / expired
        Timestamp pastDue = new Timestamp(new Date(nowMillis - 60000));
        assertEquals("Đã hết đếm ngược, hệ thống sẽ tự chuyển sang Đã nhận",
                DealViewModel.formatRemainingTime(pastDue, now));

        assertEquals("", DealViewModel.formatRemainingTime(null, now));
        assertEquals("", DealViewModel.formatRemainingTime(future5Days, null));
    }

    @Test
    public void canMarkShipped_allowsOnlyConfirmedShippingVerifiedSeller() {
        Deal deal = createSampleDeal(DealStatus.CONFIRMED);
        deal.setDeliveryMethod(DeliveryMethod.SHIPPING);
        deal.setAgreedPrice(0);

        assertTrue(DealViewModel.canMarkShipped(deal, null, "seller1", true));
        assertFalse(DealViewModel.canMarkShipped(deal, null, "buyer1", true));
        assertFalse(DealViewModel.canMarkShipped(deal, null, "seller1", false));

        deal.setDeliveryMethod(DeliveryMethod.MEET_UP);
        assertFalse(DealViewModel.canMarkShipped(deal, null, "seller1", true));

        deal.setDeliveryMethod(DeliveryMethod.SHIPPING);
        deal.setStatus(DealStatus.PENDING_CONFIRM);
        assertFalse(DealViewModel.canMarkShipped(deal, null, "seller1", true));

        deal.setStatus(DealStatus.CONFIRMED);
        deal.setAgreedPrice(500000);
        assertFalse(DealViewModel.canMarkShipped(deal, null, "seller1", true));

        Payment held = new Payment();
        held.setStatus(PaymentStatus.HELD);
        assertTrue(DealViewModel.canMarkShipped(deal, held, "seller1", true));
    }

    @Test
    public void canMarkReceived_allowsOnlyBuyerWithCorrectStatusAndHeldPayment() {
        Deal shippingDeal = createSampleDeal(DealStatus.SHIPPED);
        shippingDeal.setDeliveryMethod(DeliveryMethod.SHIPPING);
        shippingDeal.setAgreedPrice(0);

        assertTrue(DealViewModel.canMarkReceived(shippingDeal, null, "buyer1", true));
        assertFalse(DealViewModel.canMarkReceived(shippingDeal, null, "seller1", true));
        assertFalse(DealViewModel.canMarkReceived(shippingDeal, null, "buyer1", false));

        shippingDeal.setStatus(DealStatus.CONFIRMED);
        assertFalse(DealViewModel.canMarkReceived(shippingDeal, null, "buyer1", true));

        Deal meetUpDeal = createSampleDeal(DealStatus.CONFIRMED);
        meetUpDeal.setDeliveryMethod(DeliveryMethod.MEET_UP);
        meetUpDeal.setAgreedPrice(0);
        assertTrue(DealViewModel.canMarkReceived(meetUpDeal, null, "buyer1", true));

        meetUpDeal.setStatus(DealStatus.SHIPPED);
        assertTrue(DealViewModel.canMarkReceived(meetUpDeal, null, "buyer1", true));

        meetUpDeal.setAgreedPrice(500000);
        assertFalse(DealViewModel.canMarkReceived(meetUpDeal, null, "buyer1", true));

        Payment held = new Payment();
        held.setStatus(PaymentStatus.HELD);
        assertTrue(DealViewModel.canMarkReceived(meetUpDeal, held, "buyer1", true));
    }

    @Test
    public void canComplain_allowsShippedBeforeAutoReleaseOrConfirmedMeetUp() {
        long nowMillis = 1700000000000L;
        Timestamp now = new Timestamp(new Date(nowMillis));
        Timestamp future = new Timestamp(new Date(nowMillis + 86400000));
        Timestamp past = new Timestamp(new Date(nowMillis - 86400000));

        Deal shippedDeal = createSampleDeal(DealStatus.SHIPPED);
        shippedDeal.setAutoReleaseAt(future);

        assertTrue(DealViewModel.canComplain(shippedDeal, "buyer1", now));
        assertTrue(DealViewModel.canComplain(shippedDeal, "seller1", now));
        assertFalse(DealViewModel.canComplain(shippedDeal, "stranger", now));

        shippedDeal.setAutoReleaseAt(past);
        assertFalse(DealViewModel.canComplain(shippedDeal, "buyer1", now));
        assertFalse(DealViewModel.canComplain(shippedDeal, "seller1", now));

        Deal meetUpDeal = createSampleDeal(DealStatus.CONFIRMED);
        meetUpDeal.setDeliveryMethod(DeliveryMethod.MEET_UP);
        assertTrue(DealViewModel.canComplain(meetUpDeal, "buyer1", now));
        assertFalse(DealViewModel.canComplain(meetUpDeal, "seller1", now));

        Deal shippingConfirmed = createSampleDeal(DealStatus.CONFIRMED);
        shippingConfirmed.setDeliveryMethod(DeliveryMethod.SHIPPING);
        assertFalse(DealViewModel.canComplain(shippingConfirmed, "buyer1", now));
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
