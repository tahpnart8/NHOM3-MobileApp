package com.nhom3.pubgapp.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.enums.ListingStatus;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileViewModelSavedTest {

    @Test
    public void listKeepsTheSavedOrder() {
        List<String> savedIds = ids("c", "a", "b");
        Map<String, Listing> found = new HashMap<String, Listing>();
        found.put("a", listing("a", ListingStatus.AVAILABLE));
        found.put("b", listing("b", ListingStatus.SOLD));
        found.put("c", listing("c", ListingStatus.RESERVED));
        List<Listing> result = ProfileViewModel.buildSavedList(savedIds, found);
        assertEquals("c", result.get(0).getId());
        assertEquals("a", result.get(1).getId());
        assertEquals("b", result.get(2).getId());
    }

    @Test
    public void unreadableListingBecomesAnIdOnlyCard() {
        List<String> savedIds = ids("a", "gone");
        Map<String, Listing> found = new HashMap<String, Listing>();
        found.put("a", listing("a", ListingStatus.AVAILABLE));
        List<Listing> result = ProfileViewModel.buildSavedList(savedIds, found);
        Listing gone = result.get(1);
        assertEquals("gone", gone.getId());
        assertNull(gone.getStatus());
        assertTrue(ProfileViewModel.isUnavailable(gone));
    }

    @Test
    public void countMatchesTheSavedIds() {
        List<String> savedIds = ids("a", "b", "c", "d");
        List<Listing> result = ProfileViewModel.buildSavedList(savedIds, new HashMap<String, Listing>());
        assertEquals(4, result.size());
    }

    @Test
    public void hiddenRemovedAndDraftAreUnavailable() {
        assertTrue(ProfileViewModel.isUnavailable(listing("a", ListingStatus.HIDDEN)));
        assertTrue(ProfileViewModel.isUnavailable(listing("b", ListingStatus.REMOVED)));
        assertTrue(ProfileViewModel.isUnavailable(listing("c", ListingStatus.DRAFT)));
    }

    @Test
    public void soldAndAvailableStayAvailable() {
        assertFalse(ProfileViewModel.isUnavailable(listing("a", ListingStatus.SOLD)));
        assertFalse(ProfileViewModel.isUnavailable(listing("b", ListingStatus.AVAILABLE)));
        assertFalse(ProfileViewModel.isUnavailable(listing("c", ListingStatus.RESERVED)));
    }

    private static List<String> ids(String... values) {
        List<String> result = new ArrayList<String>();
        for (String value : values) {
            result.add(value);
        }
        return result;
    }

    private static Listing listing(String id, ListingStatus status) {
        Listing listing = new Listing();
        listing.setId(id);
        listing.setStatus(status);
        return listing;
    }
}
