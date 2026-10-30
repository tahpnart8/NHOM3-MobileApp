package com.nhom3.pubgapp.ui.trade;

import static org.junit.Assert.assertEquals;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.model.enums.DealStatus;

import org.junit.Test;

public class MessageAdapterOfferTitleTest {

    @Test
    public void offerTitle_withoutDeal_isPlainOffer() {
        assertEquals(R.string.trade_chat_offer_title, MessageAdapter.offerTitleFor(null));
    }

    @Test
    public void offerTitle_runningDeal_isInProgress() {
        assertEquals(R.string.trade_chat_offer_title_active, MessageAdapter.offerTitleFor(DealStatus.SHIPPED));
    }

    @Test
    public void offerTitle_completedDeal_isDone() {
        assertEquals(R.string.trade_chat_offer_title_done, MessageAdapter.offerTitleFor(DealStatus.COMPLETED));
    }

    @Test
    public void offerTitle_cancelledDeal_staysPlainOffer() {
        assertEquals(R.string.trade_chat_offer_title, MessageAdapter.offerTitleFor(DealStatus.CANCELLED));
    }
}
