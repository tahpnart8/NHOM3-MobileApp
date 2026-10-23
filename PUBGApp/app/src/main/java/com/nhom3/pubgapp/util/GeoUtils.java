package com.nhom3.pubgapp.util;

import com.google.firebase.firestore.GeoPoint;
import com.nhom3.pubgapp.model.Listing;

import java.util.List;

/** Distance helpers, because Firestore cannot query by radius. */
public class GeoUtils {

    public double distanceKm(GeoPoint from, GeoPoint to) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public boolean isWithinRadius(GeoPoint point, GeoPoint center, int radiusKm) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void sortByDistance(List<Listing> listings, GeoPoint center) {
        throw new UnsupportedOperationException("NOT_READY");
    }
}
