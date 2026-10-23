package com.nhom3.pubgapp.util;

import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.User;

import java.util.List;

/** On-device scoring for similar listings, the For You feed and the price range. */
public class RecommendationEngine {

    public List<Listing> similarTo(Listing listing, List<Listing> candidates) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public List<Listing> forYou(List<Listing> candidates, List<String> viewedCategoryIds,
            List<String> searchedKeywords, List<String> savedCategoryIds, User user) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public long[] priceRange(Listing listing, List<Listing> candidates) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    private double score(Listing listing) {
        throw new UnsupportedOperationException("NOT_READY");
    }
}
