package com.nhom3.pubgapp;

import com.google.firebase.firestore.GeoPoint;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.util.GeoUtils;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GeoUtilsTest {

    private GeoUtils geoUtils;

    @Before
    public void setUp() {
        geoUtils = new GeoUtils();
    }

    @Test
    public void testDistanceKm() {
        GeoPoint hanoi = new GeoPoint(21.0285, 105.8542);
        GeoPoint hcmc = new GeoPoint(10.7769, 106.7009);

        assertEquals(-1.0, geoUtils.distanceKm(null, hanoi), 0.001);
        assertEquals(-1.0, geoUtils.distanceKm(hanoi, null), 0.001);

        double distanceSame = geoUtils.distanceKm(hanoi, hanoi);
        assertEquals(0.0, distanceSame, 0.001);

        // Khoảng cách theo công thức haversine giữa tọa độ mẫu Hà Nội và TP.HCM xấp xỉ 1143.5 km
        double distance = geoUtils.distanceKm(hanoi, hcmc);
        assertEquals(1143.5, distance, 0.5);
    }

    @Test
    public void testIsWithinRadius() {
        GeoPoint center = new GeoPoint(10.7769, 106.7009);
        GeoPoint nearby = new GeoPoint(10.7800, 106.7050);
        GeoPoint faraway = new GeoPoint(21.0285, 105.8542);

        assertFalse(geoUtils.isWithinRadius(null, center, 5));
        assertFalse(geoUtils.isWithinRadius(nearby, null, 5));
        assertFalse(geoUtils.isWithinRadius(nearby, center, -1));
        assertTrue(geoUtils.isWithinRadius(nearby, center, 5));
        assertFalse(geoUtils.isWithinRadius(faraway, center, 50));
    }

    @Test
    public void testSortByDistance() {
        GeoPoint center = new GeoPoint(10.7769, 106.7009);

        Listing nearListing = new Listing();
        nearListing.setId("near");
        nearListing.setLocation(new GeoPoint(10.7800, 106.7050));

        Listing farListing = new Listing();
        farListing.setId("far");
        farListing.setLocation(new GeoPoint(21.0285, 105.8542));

        List<Listing> list = new ArrayList<Listing>();
        list.add(farListing);
        list.add(nearListing);

        geoUtils.sortByDistance(list, center);

        assertEquals("near", list.get(0).getId());
        assertEquals("far", list.get(1).getId());
    }
}
