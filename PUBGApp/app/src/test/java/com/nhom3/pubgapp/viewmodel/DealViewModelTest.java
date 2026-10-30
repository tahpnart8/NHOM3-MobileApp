package com.nhom3.pubgapp.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Payment;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.model.enums.DeliveryMethod;
import com.nhom3.pubgapp.model.enums.PaymentMethod;
import com.nhom3.pubgapp.model.enums.PaymentStatus;
import com.nhom3.pubgapp.ui.trade.DealHistoryAdapter;
import com.nhom3.pubgapp.ui.trade.PaymentHistoryAdapter;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Tests DealViewModel button and action guard conditions without Firebase.
public class DealViewModelTest {

    @Test
    public void canEditTerms_allowsOnlySellerWhilePendingConfirmVerifiedAndOnline() {
        Deal deal = createSampleDeal(DealStatus.PENDING_CONFIRM);

        assertTrue(DealViewModel.canEditTerms(deal, "seller1", true, true));
        assertFalse(DealViewModel.canEditTerms(deal, "buyer1", true, true));

        assertFalse(DealViewModel.canEditTerms(deal, "seller1", false, true));
        assertFalse(DealViewModel.canEditTerms(deal, "seller1", true, false));
        assertFalse(DealViewModel.canEditTerms(deal, "stranger", true, true));

        deal.setStatus(DealStatus.CONFIRMED);
        assertFalse(DealViewModel.canEditTerms(deal, "seller1", true, true));

        deal.setStatus(DealStatus.SHIPPED);
        assertFalse(DealViewModel.canEditTerms(deal, "seller1", true, true));

        deal.setStatus(DealStatus.CANCELLED);
        assertFalse(DealViewModel.canEditTerms(deal, "seller1", true, true));

        assertFalse(DealViewModel.canEditTerms(null, "seller1", true, true));
    }

    @Test
    public void canConfirm_allowsOnlyBuyerAfterSellerConfirmedAndBeforeOwnConfirm() {
        Deal deal = createSampleDeal(DealStatus.PENDING_CONFIRM);
        deal.setBuyerConfirmed(false);
        deal.setSellerConfirmed(true);

        assertTrue(DealViewModel.canConfirm(deal, "buyer1", true, true));
        assertFalse(DealViewModel.canConfirm(deal, "seller1", true, true));
        assertFalse(DealViewModel.canConfirm(deal, "buyer1", false, true));
        assertFalse(DealViewModel.canConfirm(deal, "buyer1", true, false));

        deal.setSellerConfirmed(false);
        assertFalse(DealViewModel.canConfirm(deal, "buyer1", true, true));

        deal.setSellerConfirmed(true);
        deal.setBuyerConfirmed(true);
        assertFalse(DealViewModel.canConfirm(deal, "buyer1", true, true));

        deal.setBuyerConfirmed(false);
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

    @Test
    public void matchesFilter_classifiesDealStatusesCorrectly() {
        assertFalse(DealHistoryAdapter.matchesFilter(null, DealHistoryAdapter.FILTER_ALL));

        Deal pending = createSampleDeal(DealStatus.PENDING_CONFIRM);
        Deal confirmed = createSampleDeal(DealStatus.CONFIRMED);
        Deal shipped = createSampleDeal(DealStatus.SHIPPED);
        Deal disputed = createSampleDeal(DealStatus.DISPUTED);
        Deal received = createSampleDeal(DealStatus.RECEIVED);
        Deal completed = createSampleDeal(DealStatus.COMPLETED);
        Deal cancelled = createSampleDeal(DealStatus.CANCELLED);

        // FILTER_ALL matches any non-null deal with status
        assertTrue(DealHistoryAdapter.matchesFilter(pending, DealHistoryAdapter.FILTER_ALL));
        assertTrue(DealHistoryAdapter.matchesFilter(completed, DealHistoryAdapter.FILTER_ALL));
        assertTrue(DealHistoryAdapter.matchesFilter(cancelled, DealHistoryAdapter.FILTER_ALL));

        // FILTER_IN_PROGRESS
        assertTrue(DealHistoryAdapter.matchesFilter(pending, DealHistoryAdapter.FILTER_IN_PROGRESS));
        assertTrue(DealHistoryAdapter.matchesFilter(confirmed, DealHistoryAdapter.FILTER_IN_PROGRESS));
        assertTrue(DealHistoryAdapter.matchesFilter(shipped, DealHistoryAdapter.FILTER_IN_PROGRESS));
        assertTrue(DealHistoryAdapter.matchesFilter(disputed, DealHistoryAdapter.FILTER_IN_PROGRESS));
        assertFalse(DealHistoryAdapter.matchesFilter(received, DealHistoryAdapter.FILTER_IN_PROGRESS));
        assertFalse(DealHistoryAdapter.matchesFilter(completed, DealHistoryAdapter.FILTER_IN_PROGRESS));
        assertFalse(DealHistoryAdapter.matchesFilter(cancelled, DealHistoryAdapter.FILTER_IN_PROGRESS));

        // FILTER_COMPLETED
        assertFalse(DealHistoryAdapter.matchesFilter(shipped, DealHistoryAdapter.FILTER_COMPLETED));
        assertTrue(DealHistoryAdapter.matchesFilter(received, DealHistoryAdapter.FILTER_COMPLETED));
        assertTrue(DealHistoryAdapter.matchesFilter(completed, DealHistoryAdapter.FILTER_COMPLETED));
        assertFalse(DealHistoryAdapter.matchesFilter(cancelled, DealHistoryAdapter.FILTER_COMPLETED));

        // FILTER_CANCELLED
        assertFalse(DealHistoryAdapter.matchesFilter(completed, DealHistoryAdapter.FILTER_CANCELLED));
        assertTrue(DealHistoryAdapter.matchesFilter(cancelled, DealHistoryAdapter.FILTER_CANCELLED));

        // Unknown filter index
        assertFalse(DealHistoryAdapter.matchesFilter(pending, 999));
    }

    @Test
    public void filterDeals_returnsMatchingDealsOrEmptyList() {
        assertEquals(0, DealHistoryAdapter.filterDeals(null, DealHistoryAdapter.FILTER_ALL).size());

        List<Deal> list = new ArrayList<Deal>();
        list.add(createSampleDeal(DealStatus.PENDING_CONFIRM));
        list.add(createSampleDeal(DealStatus.SHIPPED));
        list.add(createSampleDeal(DealStatus.COMPLETED));
        list.add(createSampleDeal(DealStatus.CANCELLED));

        List<Deal> all = DealHistoryAdapter.filterDeals(list, DealHistoryAdapter.FILTER_ALL);
        assertEquals(4, all.size());

        List<Deal> inProgress = DealHistoryAdapter.filterDeals(list, DealHistoryAdapter.FILTER_IN_PROGRESS);
        assertEquals(2, inProgress.size());

        List<Deal> completed = DealHistoryAdapter.filterDeals(list, DealHistoryAdapter.FILTER_COMPLETED);
        assertEquals(1, completed.size());

        List<Deal> cancelled = DealHistoryAdapter.filterDeals(list, DealHistoryAdapter.FILTER_CANCELLED);
        assertEquals(1, cancelled.size());
    }

    @Test
    public void getRoleStringRes_distinguishesPayerAndPayee() {
        Payment payment = new Payment();
        payment.setPayerId("user_buyer");
        payment.setPayeeId("user_seller");

        assertEquals(R.string.trade_payment_role_payer,
                PaymentHistoryAdapter.getRoleStringRes(payment, "user_buyer"));
        assertEquals(R.string.trade_payment_role_payee,
                PaymentHistoryAdapter.getRoleStringRes(payment, "user_seller"));
        assertEquals(0,
                PaymentHistoryAdapter.getRoleStringRes(payment, "other_user"));
        assertEquals(0,
                PaymentHistoryAdapter.getRoleStringRes(null, "user_buyer"));
        assertEquals(0,
                PaymentHistoryAdapter.getRoleStringRes(payment, null));
    }

    @Test
    public void getMethodStringRes_mapsAllPaymentMethods() {
        assertEquals(R.string.trade_payment_method_card,
                PaymentHistoryAdapter.getMethodStringRes(PaymentMethod.CARD));
        assertEquals(R.string.trade_payment_method_e_wallet,
                PaymentHistoryAdapter.getMethodStringRes(PaymentMethod.E_WALLET));
        assertEquals(R.string.trade_payment_method_bank_transfer,
                PaymentHistoryAdapter.getMethodStringRes(PaymentMethod.BANK_TRANSFER));
        assertEquals(0,
                PaymentHistoryAdapter.getMethodStringRes(null));
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
