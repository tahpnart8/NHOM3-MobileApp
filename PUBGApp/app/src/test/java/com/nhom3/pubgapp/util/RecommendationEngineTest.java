package com.nhom3.pubgapp.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.enums.ItemCondition;
import com.nhom3.pubgapp.model.enums.ListingStatus;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecommendationEngineTest {

    private RecommendationEngine engine;

    @Before
    public void setUp() {
        engine = new RecommendationEngine();
    }

    private Listing createListing(String id, String categoryId, ItemCondition condition,
                                  String province, long price, List<String> keywords,
                                  ListingStatus status, Date createdAt) {
        Listing listing = new Listing();
        listing.setId(id);
        listing.setCategoryId(categoryId);
        listing.setItemCondition(condition);
        listing.setProvince(province);
        listing.setPrice(price);
        listing.setSearchKeywords(keywords);
        listing.setStatus(status);
        if (createdAt != null) {
            listing.setCreatedAt(new Timestamp(createdAt));
        }
        return listing;
    }

    @Test
    public void testSimilarTo_nullOrEmpty_returnsEmptyList() {
        List<Listing> candidates = new ArrayList<Listing>();
        Listing target = createListing("t1", "cat-phone", ItemCondition.NEW, "Hà Nội", 5000000L,
                null, ListingStatus.AVAILABLE, new Date());

        List<Listing> resultNullTarget = engine.similarTo(null, candidates);
        assertTrue(resultNullTarget.isEmpty());

        List<Listing> resultNullCandidates = engine.similarTo(target, null);
        assertTrue(resultNullCandidates.isEmpty());

        List<Listing> resultEmptyCandidates = engine.similarTo(target, candidates);
        assertTrue(resultEmptyCandidates.isEmpty());
    }

    @Test
    public void testSimilarTo_excludesSelfNonAvailableAndDifferentCategory() {
        Listing target = createListing("target1", "cat-phone", ItemCondition.LIKE_NEW, "Hà Nội",
                2000000L, null, ListingStatus.AVAILABLE, new Date());

        List<Listing> candidates = new ArrayList<Listing>();
        // Self listing
        candidates.add(createListing("target1", "cat-phone", ItemCondition.LIKE_NEW, "Hà Nội",
                2000000L, null, ListingStatus.AVAILABLE, new Date()));
        // Sold listing
        candidates.add(createListing("cand-sold", "cat-phone", ItemCondition.LIKE_NEW, "Hà Nội",
                2000000L, null, ListingStatus.SOLD, new Date()));
        // Hidden listing
        candidates.add(createListing("cand-hidden", "cat-phone", ItemCondition.LIKE_NEW, "Hà Nội",
                2000000L, null, ListingStatus.HIDDEN, new Date()));
        // Different category
        candidates.add(createListing("cand-diff-cat", "cat-laptop", ItemCondition.LIKE_NEW, "Hà Nội",
                2000000L, null, ListingStatus.AVAILABLE, new Date()));
        // Valid candidate
        Listing validCand = createListing("cand-valid", "cat-phone", ItemCondition.LIKE_NEW, "Hà Nội",
                2100000L, null, ListingStatus.AVAILABLE, new Date());
        candidates.add(validCand);

        List<Listing> result = engine.similarTo(target, candidates);
        assertEquals(1, result.size());
        assertEquals("cand-valid", result.get(0).getId());
    }

    @Test
    public void testSimilarTo_ranksByScoreMatchingAttributes() {
        List<String> targetKeywords = new ArrayList<String>();
        targetKeywords.add("iphone");
        targetKeywords.add("128gb");
        targetKeywords.add("trang");

        Listing target = createListing("t1", "cat-phone", ItemCondition.LIKE_NEW, "Hà Nội",
                10000000L, targetKeywords, ListingStatus.AVAILABLE, new Date(100000L));

        List<String> keywordsHigh = new ArrayList<String>();
        keywordsHigh.add("iphone");
        keywordsHigh.add("128gb");
        keywordsHigh.add("trang");

        // High similarity: same condition, same province, identical price, identical keywords
        Listing highCand = createListing("c-high", "cat-phone", ItemCondition.LIKE_NEW, "Hà Nội",
                10000000L, keywordsHigh, ListingStatus.AVAILABLE, new Date(100000L));

        List<String> keywordsLow = new ArrayList<String>();
        keywordsLow.add("samsung");

        // Low similarity: different condition, different province, distant price, no shared keywords
        Listing lowCand = createListing("c-low", "cat-phone", ItemCondition.DEFECTIVE, "TP.HCM",
                30000000L, keywordsLow, ListingStatus.AVAILABLE, new Date(100000L));

        List<Listing> candidates = new ArrayList<Listing>();
        candidates.add(lowCand);
        candidates.add(highCand);

        List<Listing> result = engine.similarTo(target, candidates);
        assertEquals(2, result.size());
        assertEquals("c-high", result.get(0).getId());
        assertEquals("c-low", result.get(1).getId());
    }

    @Test
    public void testSimilarTo_limitsToTenItems() {
        Listing target = createListing("target", "cat-phone", ItemCondition.LIKE_NEW, "Hà Nội",
                5000000L, null, ListingStatus.AVAILABLE, new Date());

        List<Listing> candidates = new ArrayList<Listing>();
        for (int i = 0; i < 15; i++) {
            candidates.add(createListing("c" + i, "cat-phone", ItemCondition.LIKE_NEW, "Hà Nội",
                    5000000L + (i * 10000L), null, ListingStatus.AVAILABLE, new Date()));
        }

        List<Listing> result = engine.similarTo(target, candidates);
        assertEquals(10, result.size());
    }

    @Test
    public void testSimilarTo_tiesBrokenByNewerCreatedAt() {
        Listing target = createListing("target", "cat-phone", ItemCondition.NEW, "Hà Nội",
                1000000L, null, ListingStatus.AVAILABLE, new Date(100000L));

        Listing older = createListing("older", "cat-phone", ItemCondition.NEW, "Hà Nội",
                1000000L, null, ListingStatus.AVAILABLE, new Date(1000000L));
        Listing newer = createListing("newer", "cat-phone", ItemCondition.NEW, "Hà Nội",
                1000000L, null, ListingStatus.AVAILABLE, new Date(2000000L));

        List<Listing> candidates = new ArrayList<Listing>();
        candidates.add(older);
        candidates.add(newer);

        List<Listing> result = engine.similarTo(target, candidates);
        assertEquals(2, result.size());
        assertEquals("newer", result.get(0).getId());
        assertEquals("older", result.get(1).getId());
    }
}
