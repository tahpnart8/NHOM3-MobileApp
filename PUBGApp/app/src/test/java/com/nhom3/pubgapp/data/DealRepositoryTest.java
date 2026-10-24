package com.nhom3.pubgapp.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.Offer;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.model.enums.OfferType;
import com.nhom3.pubgapp.model.enums.TradeType;

import org.junit.Test;

// Tests DealRepository.buildFromOffer pure conversion logic without network.
public class DealRepositoryTest {

    @Test
    public void buildFromOffer_mapsMoneyOfferToSellTradeType() {
        Listing listing = new Listing();
        listing.setTitle("Bàn phím cơ");
        listing.setCoverThumb("thumb_key");

        Offer offer = new Offer();
        offer.setId("offer1");
        offer.setListingId("listing1");
        offer.setBuyerId("buyer1");
        offer.setSellerId("seller1");
        offer.setOfferType(OfferType.MONEY);
        offer.setAmount(500000);

        Deal deal = DealRepository.buildFromOffer(offer, listing);
        assertNotNull(deal);
        assertEquals("listing1", deal.getListingId());
        assertEquals("offer1", deal.getOfferId());
        assertEquals("buyer1", deal.getBuyerId());
        assertEquals("seller1", deal.getSellerId());
        assertEquals(TradeType.SELL, deal.getTradeType());
        assertEquals(500000, deal.getAgreedPrice());
        assertEquals("Bàn phím cơ", deal.getItemTitle());
        assertEquals("thumb_key", deal.getItemThumb());
        assertEquals(DealStatus.PENDING_CONFIRM, deal.getStatus());
        assertTrue(deal.isSellerConfirmed());
        assertFalse(deal.isBuyerConfirmed());
        assertNotNull(deal.getStatusHistory());
        assertEquals(1, deal.getStatusHistory().size());
        assertEquals(DealStatus.PENDING_CONFIRM, deal.getStatusHistory().get(0).getToStatus());
        assertEquals("seller1", deal.getStatusHistory().get(0).getChangedBy());
    }

    @Test
    public void buildFromOffer_mapsItemOfferToExchangeTradeType() {
        Listing listing = new Listing();
        listing.setTitle("Tai nghe");
        listing.setCoverThumb("thumb_headphone");

        Offer offer = new Offer();
        offer.setId("offer2");
        offer.setListingId("listing2");
        offer.setBuyerId("buyer2");
        offer.setSellerId("seller2");
        offer.setOfferType(OfferType.ITEM);
        offer.setAmount(0);
        offer.setExchangeItem("Loa bluetooth");

        Deal deal = DealRepository.buildFromOffer(offer, listing);
        assertNotNull(deal);
        assertEquals(TradeType.EXCHANGE, deal.getTradeType());
        assertEquals(0, deal.getAgreedPrice());
        assertEquals("Loa bluetooth", deal.getExchangeItem());
    }

    @Test
    public void getOtherPartyId_returnsCorrectPartner() {
        Deal deal = new Deal();
        deal.setBuyerId("buyer123");
        deal.setSellerId("seller456");

        assertEquals("seller456", DealRepository.getOtherPartyId(deal, "buyer123"));
        assertEquals("buyer123", DealRepository.getOtherPartyId(deal, "seller456"));
        assertEquals("", DealRepository.getOtherPartyId(deal, "stranger789"));
        assertEquals("", DealRepository.getOtherPartyId(deal, null));
        assertEquals("", DealRepository.getOtherPartyId(null, "buyer123"));
    }

    @Test
    public void shouldUpdateListingToSold_requiresReservedAndMatchingDealId() {
        Listing listing = new Listing();
        listing.setStatus(ListingStatus.RESERVED);
        listing.setReservedDealId("deal_abc");

        assertTrue(DealRepository.shouldUpdateListingToSold(listing, "deal_abc"));
        assertFalse(DealRepository.shouldUpdateListingToSold(listing, "other_deal"));

        listing.setStatus(ListingStatus.AVAILABLE);
        assertFalse(DealRepository.shouldUpdateListingToSold(listing, "deal_abc"));

        listing.setStatus(ListingStatus.SOLD);
        assertFalse(DealRepository.shouldUpdateListingToSold(listing, "deal_abc"));

        assertFalse(DealRepository.shouldUpdateListingToSold(null, "deal_abc"));
        assertFalse(DealRepository.shouldUpdateListingToSold(listing, null));
        assertFalse(DealRepository.shouldUpdateListingToSold(listing, ""));
    }
}
