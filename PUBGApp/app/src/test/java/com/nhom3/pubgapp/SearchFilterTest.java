package com.nhom3.pubgapp;

import com.google.firebase.firestore.GeoPoint;
import com.nhom3.pubgapp.model.SearchFilter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SearchFilterTest {

    @Test
    public void testPriceRange() {
        SearchFilter filter = new SearchFilter();
        assertFalse(filter.hasPriceRange());

        filter.setMinPrice(500000L);
        assertTrue(filter.hasPriceRange());

        filter.setMinPrice(0L);
        filter.setMaxPrice(2000000L);
        assertTrue(filter.hasPriceRange());
    }

    @Test
    public void testRadius() {
        SearchFilter filter = new SearchFilter();
        assertFalse(filter.hasRadius());

        filter.setCenter(new GeoPoint(10.7769, 106.7009));
        assertFalse(filter.hasRadius());

        filter.setRadiusKm(10);
        assertTrue(filter.hasRadius());
    }

    @Test
    public void testReset() {
        SearchFilter filter = new SearchFilter();
        filter.setKeyword("iPhone");
        filter.setCategoryId("cat_123");
        filter.setMinPrice(1000000L);
        filter.setMaxPrice(5000000L);
        filter.setRadiusKm(15);
        filter.setCenter(new GeoPoint(10.7769, 106.7009));
        filter.setFollowingOnly(true);

        filter.reset();

        assertNull(filter.getKeyword());
        assertNull(filter.getCategoryId());
        assertEquals(0L, filter.getMinPrice());
        assertEquals(0L, filter.getMaxPrice());
        assertEquals(0, filter.getRadiusKm());
        assertNull(filter.getCenter());
        assertFalse(filter.isFollowingOnly());
    }
}
