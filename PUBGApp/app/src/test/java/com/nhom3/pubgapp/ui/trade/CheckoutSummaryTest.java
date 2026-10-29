package com.nhom3.pubgapp.ui.trade;

import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.model.enums.PaymentMethod;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CheckoutSummaryTest {

    private Deal confirmedDeal() {
        Deal deal = new Deal();
        deal.setBuyerId("buyer");
        deal.setStatus(DealStatus.CONFIRMED);
        deal.setAgreedPrice(150000L);
        return deal;
    }

    @Test
    public void onlyTheBuyerOfAConfirmedPricedDealCanPay() {
        assertTrue(CheckoutSummary.canPay(confirmedDeal(), "buyer"));
        assertFalse(CheckoutSummary.canPay(confirmedDeal(), "seller"));
        assertFalse(CheckoutSummary.canPay(null, "buyer"));

        Deal free = confirmedDeal();
        free.setAgreedPrice(0L);
        assertFalse(CheckoutSummary.canPay(free, "buyer"));

        Deal pending = confirmedDeal();
        pending.setStatus(DealStatus.PENDING_CONFIRM);
        assertFalse(CheckoutSummary.canPay(pending, "buyer"));
    }

    @Test
    public void parseMethodAcceptsOnlyKnownNames() {
        assertEquals(PaymentMethod.E_WALLET, CheckoutSummary.parseMethod("E_WALLET"));
        assertNull(CheckoutSummary.parseMethod("PAYPAL"));
        assertNull(CheckoutSummary.parseMethod(null));
    }
}
