package com.nhom3.pubgapp;

import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.Offer;
import com.nhom3.pubgapp.model.enums.ItemCondition;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.model.enums.OfferType;
import com.nhom3.pubgapp.model.enums.TradeType;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ListingTest {

    private Listing listing;

    @Before
    public void setUp() {
        listing = new Listing();
        listing.setStatus(ListingStatus.DRAFT);
        listing.setTitle("Tai nghe Sony WH-1000XM4");
        listing.setDescription("Chống ồn tốt, còn bảo hành");
        listing.setCategoryId("audio_01");

        List<String> imageIds = new ArrayList<String>();
        imageIds.add("img_01");
        listing.setImageIds(imageIds);

        List<TradeType> tradeTypes = new ArrayList<TradeType>();
        tradeTypes.add(TradeType.SELL);
        listing.setTradeTypes(tradeTypes);

        List<OfferType> offerTypes = new ArrayList<OfferType>();
        offerTypes.add(OfferType.MONEY);
        listing.setAcceptedOfferTypes(offerTypes);

        listing.setPrice(3500000L);
        listing.setMinOfferPrice(3000000L);
        listing.setItemCondition(ItemCondition.LIKE_NEW);
    }

    @Test
    public void testIsDraft() {
        assertTrue(listing.isDraft());
        assertFalse(listing.isAvailable());
    }

    @Test
    public void testPublishSuccess() {
        listing.publish();
        assertEquals(ListingStatus.AVAILABLE, listing.getStatus());
        assertTrue(listing.isAvailable());
        assertFalse(listing.isDraft());
    }

    @Test(expected = IllegalStateException.class)
    public void testPublishFailsWhenTitleEmpty() {
        listing.setTitle("   ");
        listing.publish();
    }

    @Test(expected = IllegalStateException.class)
    public void testPublishFailsWhenCategoryEmpty() {
        listing.setCategoryId(null);
        listing.publish();
    }

    @Test(expected = IllegalStateException.class)
    public void testPublishFailsWhenNoImages() {
        listing.setImageIds(new ArrayList<String>());
        listing.publish();
    }

    @Test(expected = IllegalStateException.class)
    public void testPublishFailsWhenNotDraft() {
        listing.setStatus(ListingStatus.AVAILABLE);
        listing.publish();
    }

    @Test
    public void testAcceptsOffer() {
        listing.publish();

        Offer validOffer = new Offer();
        validOffer.setOfferType(OfferType.MONEY);
        validOffer.setAmount(3200000L);
        assertTrue(listing.acceptsOffer(validOffer));

        Offer lowOffer = new Offer();
        lowOffer.setOfferType(OfferType.MONEY);
        lowOffer.setAmount(2500000L);
        assertFalse(listing.acceptsOffer(lowOffer));

        Offer itemOffer = new Offer();
        itemOffer.setOfferType(OfferType.ITEM);
        itemOffer.setAmount(0L);
        assertFalse(listing.acceptsOffer(itemOffer));
    }

    @Test
    public void testAcceptsOfferWhenNotAvailable() {
        Offer offer = new Offer();
        offer.setOfferType(OfferType.MONEY);
        offer.setAmount(3500000L);
        assertFalse(listing.acceptsOffer(offer));
    }

    @Test
    public void testReserveAndRelease() {
        listing.publish();
        listing.reserve("deal_123");
        assertEquals(ListingStatus.RESERVED, listing.getStatus());
        assertEquals("deal_123", listing.getReservedDealId());

        listing.release();
        assertEquals(ListingStatus.AVAILABLE, listing.getStatus());
        assertNull(listing.getReservedDealId());
    }

    @Test
    public void testMarkSold() {
        listing.publish();
        listing.markSold();
        assertEquals(ListingStatus.SOLD, listing.getStatus());
    }

    @Test
    public void testHideAndRestore() {
        listing.publish();
        listing.hide("Tam dung ban");
        assertEquals(ListingStatus.HIDDEN, listing.getStatus());
        assertEquals("Tam dung ban", listing.getStatusReason());

        listing.restore();
        assertEquals(ListingStatus.AVAILABLE, listing.getStatus());
        assertNull(listing.getStatusReason());
    }

    @Test(expected = IllegalStateException.class)
    public void testMarkSoldFromDraftFails() {
        listing.markSold();
    }

    @Test
    public void testMarkSoldFromReservedSucceeds() {
        listing.publish();
        listing.reserve("deal_123");
        listing.markSold();
        assertEquals(ListingStatus.SOLD, listing.getStatus());
    }

    @Test(expected = IllegalStateException.class)
    public void testHideFromDraftFails() {
        listing.hide("Ly do");
    }

    @Test(expected = IllegalStateException.class)
    public void testRestoreFromAvailableFails() {
        listing.publish();
        listing.restore();
    }

    @Test(expected = IllegalStateException.class)
    public void testReserveFromDraftFails() {
        listing.reserve("deal_123");
    }

    @Test(expected = IllegalStateException.class)
    public void testReleaseFromAvailableFails() {
        listing.publish();
        listing.release();
    }

    @Test
    public void testRemoveFromDraftAvailableHidden() {
        listing.remove("Ly do draft");
        assertEquals(ListingStatus.REMOVED, listing.getStatus());

        listing.setStatus(ListingStatus.AVAILABLE);
        listing.remove("Ly do available");
        assertEquals(ListingStatus.REMOVED, listing.getStatus());

        listing.setStatus(ListingStatus.HIDDEN);
        listing.remove("Ly do hidden");
        assertEquals(ListingStatus.REMOVED, listing.getStatus());
    }

    @Test(expected = IllegalStateException.class)
    public void testRemoveFromSoldFails() {
        listing.setStatus(ListingStatus.SOLD);
        listing.remove("Ly do");
    }

    @Test(expected = IllegalStateException.class)
    public void testRemoveFromRemovedFails() {
        listing.setStatus(ListingStatus.REMOVED);
        listing.remove("Ly do");
    }

    @Test
    public void testBuildSearchKeywords() {
        listing.buildSearchKeywords();
        List<String> keywords = listing.getSearchKeywords();
        assertTrue(keywords.contains("tai"));
        assertTrue(keywords.contains("nghe"));
        assertTrue(keywords.contains("sony"));
        assertTrue(keywords.contains("chong"));
    }
}
