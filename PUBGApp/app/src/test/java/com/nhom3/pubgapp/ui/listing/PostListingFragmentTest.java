package com.nhom3.pubgapp.ui.listing;

import static org.junit.Assert.assertEquals;

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
}
