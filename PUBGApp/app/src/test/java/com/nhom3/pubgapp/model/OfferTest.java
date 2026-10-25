package com.nhom3.pubgapp.model;

import static org.junit.Assert.assertEquals;

import com.nhom3.pubgapp.model.enums.OfferStatus;

import org.junit.Test;

public class OfferTest {

    private Offer offerWith(OfferStatus status) {
        Offer offer = new Offer();
        offer.setStatus(status);
        return offer;
    }

    @Test
    public void accept_pending_becomesAccepted() {
        Offer offer = offerWith(OfferStatus.PENDING);
        offer.accept();
        assertEquals(OfferStatus.ACCEPTED, offer.getStatus());
    }

    @Test
    public void reject_pending_becomesRejected() {
        Offer offer = offerWith(OfferStatus.PENDING);
        offer.reject();
        assertEquals(OfferStatus.REJECTED, offer.getStatus());
    }

    @Test
    public void accept_alreadyRejected_staysRejected() {
        Offer offer = offerWith(OfferStatus.REJECTED);
        offer.accept();
        assertEquals(OfferStatus.REJECTED, offer.getStatus());
    }

    @Test
    public void reject_alreadyAccepted_staysAccepted() {
        Offer offer = offerWith(OfferStatus.ACCEPTED);
        offer.reject();
        assertEquals(OfferStatus.ACCEPTED, offer.getStatus());
    }

    @Test
    public void accept_alreadyAccepted_staysAccepted() {
        Offer offer = offerWith(OfferStatus.ACCEPTED);
        offer.accept();
        assertEquals(OfferStatus.ACCEPTED, offer.getStatus());
    }

    @Test
    public void reject_alreadyRejected_staysRejected() {
        Offer offer = offerWith(OfferStatus.REJECTED);
        offer.reject();
        assertEquals(OfferStatus.REJECTED, offer.getStatus());
    }
}
