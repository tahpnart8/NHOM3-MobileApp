package com.nhom3.pubgapp.util;

import com.google.firebase.firestore.GeoPoint;
import com.nhom3.pubgapp.model.Listing;

import java.util.List;

/** Distance helpers, because Firestore cannot query by radius. */
public class GeoUtils {

    public double distanceKm(GeoPoint from, GeoPoint to) {
        if (from == null || to == null) {
            return 0.0;
        }
        double earthRadiusKm = 6371.0;
        double lat1 = Math.toRadians(from.getLatitude());
        double lon1 = Math.toRadians(from.getLongitude());
        double lat2 = Math.toRadians(to.getLatitude());
        double lon2 = Math.toRadians(to.getLongitude());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadiusKm * c;
    }

    public boolean isWithinRadius(GeoPoint point, GeoPoint center, int radiusKm) {
        if (point == null || center == null) {
            return false;
        }
        return distanceKm(point, center) <= radiusKm;
    }

    public void sortByDistance(List<Listing> listings, final GeoPoint center) {
        if (listings == null || center == null) {
            return;
        }
        java.util.Collections.sort(listings, new java.util.Comparator<Listing>() {
            @Override
            public int compare(Listing o1, Listing o2) {
                GeoPoint loc1 = o1 != null ? o1.getLocation() : null;
                GeoPoint loc2 = o2 != null ? o2.getLocation() : null;
                if (loc1 == null && loc2 == null) {
                    return 0;
                }
                if (loc1 == null) {
                    return 1;
                }
                if (loc2 == null) {
                    return -1;
                }
                double d1 = distanceKm(loc1, center);
                double d2 = distanceKm(loc2, center);
                return Double.compare(d1, d2);
            }
        });
    }
}
