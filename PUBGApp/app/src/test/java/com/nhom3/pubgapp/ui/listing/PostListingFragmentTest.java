package com.nhom3.pubgapp.ui.listing;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.enums.OfferType;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

// Tests filtering of dynamic condition values when creating or updating a listing.
public class PostListingFragmentTest {

    @Test
    public void filterConditionValues_dropsEmptyAndBlank() {
        Map<String, String> raw = new HashMap<String, String>();
        raw.put("Pin", "100%");
        raw.put("Bảo hành", "  ");
        raw.put("Màu", "");
        raw.put("RAM", null);
        raw.put("Xuất xứ", "Chính hãng");
        Map<String, String> filtered = PostListingFragment.filterConditionValues(raw);
        assertEquals(2, filtered.size());
        assertEquals("100%", filtered.get("Pin"));
        assertEquals("Chính hãng", filtered.get("Xuất xứ"));
    }

    @Test
    public void isOfferAllowed_sellOnlyAllowsMoney() {
        assertTrue(PostListingFragment.isOfferAllowed(true, false, false, OfferType.MONEY));
        assertFalse(PostListingFragment.isOfferAllowed(true, false, false, OfferType.ITEM));
        assertFalse(PostListingFragment.isOfferAllowed(true, false, false, OfferType.MONEY_AND_ITEM));
    }

    @Test
    public void isOfferAllowed_exchangeOnlyAllowsItemAndBoth() {
        assertFalse(PostListingFragment.isOfferAllowed(false, true, false, OfferType.MONEY));
        assertTrue(PostListingFragment.isOfferAllowed(false, true, false, OfferType.ITEM));
        assertTrue(PostListingFragment.isOfferAllowed(false, true, false, OfferType.MONEY_AND_ITEM));
    }

    @Test
    public void isOfferAllowed_sellAndExchangeAllowsAll() {
        assertTrue(PostListingFragment.isOfferAllowed(true, true, false, OfferType.MONEY));
        assertTrue(PostListingFragment.isOfferAllowed(true, true, false, OfferType.ITEM));
        assertTrue(PostListingFragment.isOfferAllowed(true, true, false, OfferType.MONEY_AND_ITEM));
    }

    @Test
    public void isOfferAllowed_giveawayOrNothingAllowsNone() {
        assertFalse(PostListingFragment.isOfferAllowed(false, false, true, OfferType.MONEY));
        assertFalse(PostListingFragment.isOfferAllowed(false, false, true, OfferType.ITEM));
        assertFalse(PostListingFragment.isOfferAllowed(false, false, false, OfferType.MONEY));
        assertFalse(PostListingFragment.isOfferAllowed(true, true, false, null));
    }
}
