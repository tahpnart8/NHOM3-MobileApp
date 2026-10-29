package com.nhom3.pubgapp.viewmodel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.enums.ListingStatus;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ListingDetailViewModelTest {

    private Listing createListingWithStatus(ListingStatus status, String sellerId) {
        Listing listing = new Listing();
        listing.setId("listing-123");
        listing.setSellerId(sellerId);
        listing.setStatus(status);
        return listing;
    }

    @Test
    public void testCanShowListing_null() {
        assertFalse(ListingDetailViewModel.canShowListing(null, "user-1"));
    }

    @Test
    public void testCanShowListing_publicStatuses() {
        Listing available = createListingWithStatus(ListingStatus.AVAILABLE, "seller-1");
        assertTrue(ListingDetailViewModel.canShowListing(available, null));
        assertTrue(ListingDetailViewModel.canShowListing(available, "buyer-1"));
        assertTrue(ListingDetailViewModel.canShowListing(available, "seller-1"));

        Listing reserved = createListingWithStatus(ListingStatus.RESERVED, "seller-1");
        assertTrue(ListingDetailViewModel.canShowListing(reserved, "buyer-1"));

        Listing sold = createListingWithStatus(ListingStatus.SOLD, "seller-1");
        assertTrue(ListingDetailViewModel.canShowListing(sold, "buyer-1"));
    }

    @Test
    public void testCanShowListing_privateStatuses() {
        Listing draft = createListingWithStatus(ListingStatus.DRAFT, "seller-1");
        assertTrue(ListingDetailViewModel.canShowListing(draft, "seller-1"));
        assertFalse(ListingDetailViewModel.canShowListing(draft, "buyer-1"));
        assertFalse(ListingDetailViewModel.canShowListing(draft, null));

        Listing hidden = createListingWithStatus(ListingStatus.HIDDEN, "seller-1");
        assertTrue(ListingDetailViewModel.canShowListing(hidden, "seller-1"));
        assertFalse(ListingDetailViewModel.canShowListing(hidden, "buyer-1"));

        Listing removed = createListingWithStatus(ListingStatus.REMOVED, "seller-1");
        assertTrue(ListingDetailViewModel.canShowListing(removed, "seller-1"));
        assertFalse(ListingDetailViewModel.canShowListing(removed, "buyer-1"));
    }

    @Test
    public void testCanStartChat() {
        Listing available = createListingWithStatus(ListingStatus.AVAILABLE, "seller-1");
        assertFalse(ListingDetailViewModel.canStartChat(null, "buyer-1"));
        assertTrue(ListingDetailViewModel.canStartChat(available, null));
        assertFalse(ListingDetailViewModel.canStartChat(available, "seller-1"));
        assertTrue(ListingDetailViewModel.canStartChat(available, "buyer-1"));

        Listing reserved = createListingWithStatus(ListingStatus.RESERVED, "seller-1");
        assertTrue(ListingDetailViewModel.canStartChat(reserved, null));
        assertTrue(ListingDetailViewModel.canStartChat(reserved, "buyer-1"));

        Listing sold = createListingWithStatus(ListingStatus.SOLD, "seller-1");
        assertFalse(ListingDetailViewModel.canStartChat(sold, null));
        assertFalse(ListingDetailViewModel.canStartChat(sold, "buyer-1"));

        Listing hidden = createListingWithStatus(ListingStatus.HIDDEN, "seller-1");
        assertFalse(ListingDetailViewModel.canStartChat(hidden, "buyer-1"));
    }

    @Test
    public void testCanSendOffer() {
        Listing available = createListingWithStatus(ListingStatus.AVAILABLE, "seller-1");
        assertFalse(ListingDetailViewModel.canSendOffer(null, "buyer-1"));
        assertTrue(ListingDetailViewModel.canSendOffer(available, null));
        assertFalse(ListingDetailViewModel.canSendOffer(available, "seller-1"));
        assertTrue(ListingDetailViewModel.canSendOffer(available, "buyer-1"));

        Listing reserved = createListingWithStatus(ListingStatus.RESERVED, "seller-1");
        assertFalse(ListingDetailViewModel.canSendOffer(reserved, null));
        assertFalse(ListingDetailViewModel.canSendOffer(reserved, "buyer-1"));

        Listing sold = createListingWithStatus(ListingStatus.SOLD, "seller-1");
        assertFalse(ListingDetailViewModel.canSendOffer(sold, null));
        assertFalse(ListingDetailViewModel.canSendOffer(sold, "buyer-1"));
    }

    @Test
    public void testCanFollowSeller() {
        assertFalse(ListingDetailViewModel.canFollowSeller(null, "buyer-1"));
        assertFalse(ListingDetailViewModel.canFollowSeller("", "buyer-1"));
        assertFalse(ListingDetailViewModel.canFollowSeller("seller-1", "seller-1"));
        assertTrue(ListingDetailViewModel.canFollowSeller("seller-1", null));
        assertTrue(ListingDetailViewModel.canFollowSeller("seller-1", "buyer-1"));
    }

    @Test
    public void isSavedIn_listingInSavedList_showsFilledHeart() {
        List<String> savedIds = new ArrayList<String>();
        savedIds.add("listing-123");
        assertTrue(ListingDetailViewModel.isSavedIn(savedIds, "listing-123"));
    }

    @Test
    public void isSavedIn_missingOrNull_showsEmptyHeart() {
        List<String> savedIds = new ArrayList<String>();
        savedIds.add("listing-999");
        assertFalse(ListingDetailViewModel.isSavedIn(savedIds, "listing-123"));
        assertFalse(ListingDetailViewModel.isSavedIn(null, "listing-123"));
        assertFalse(ListingDetailViewModel.isSavedIn(savedIds, null));
    }
}
