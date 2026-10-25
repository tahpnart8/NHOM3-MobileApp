package com.nhom3.pubgapp.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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

/** Unit tests for RecommendationEngine following goi-y.md specification. */
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

    private Listing createListing(String id, String categoryId, ItemCondition condition,
                                  long price, ListingStatus status) {
        return createListing(id, categoryId, condition, "Hà Nội", price, null, status, new Date());
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

    @Test
    public void priceRange_nullOrEmptyInput_returnsEmptyArray() {
        Listing target = createListing("t1", "cat1", ItemCondition.LIKE_NEW, 1000000L, ListingStatus.AVAILABLE);

        long[] r1 = engine.priceRange(null, new ArrayList<Listing>());
        assertNotNull(r1);
        assertEquals(0, r1.length);

        long[] r2 = engine.priceRange(target, null);
        assertNotNull(r2);
        assertEquals(0, r2.length);

        long[] r3 = engine.priceRange(target, new ArrayList<Listing>());
        assertNotNull(r3);
        assertEquals(0, r3.length);
    }

    @Test
    public void priceRange_lessThanFiveCandidates_returnsEmptyArray() {
        Listing target = createListing("t1", "cat1", ItemCondition.LIKE_NEW, 1000000L, ListingStatus.AVAILABLE);
        List<Listing> candidates = new ArrayList<Listing>();
        candidates.add(createListing("c1", "cat1", ItemCondition.LIKE_NEW, 500000L, ListingStatus.AVAILABLE));
        candidates.add(createListing("c2", "cat1", ItemCondition.LIKE_NEW, 600000L, ListingStatus.AVAILABLE));
        candidates.add(createListing("c3", "cat1", ItemCondition.LIKE_NEW, 700000L, ListingStatus.AVAILABLE));
        candidates.add(createListing("c4", "cat1", ItemCondition.LIKE_NEW, 800000L, ListingStatus.AVAILABLE));

        long[] range = engine.priceRange(target, candidates);
        assertNotNull(range);
        assertEquals(0, range.length);
    }

    @Test
    public void priceRange_atLeastFiveSameCondition_usesP1() {
        Listing target = createListing("t1", "cat1", ItemCondition.LIKE_NEW, 1000000L, ListingStatus.AVAILABLE);
        List<Listing> candidates = new ArrayList<Listing>();
        candidates.add(createListing("c1", "cat1", ItemCondition.LIKE_NEW, 100000L, ListingStatus.AVAILABLE));
        candidates.add(createListing("c2", "cat1", ItemCondition.LIKE_NEW, 200000L, ListingStatus.AVAILABLE));
        candidates.add(createListing("c3", "cat1", ItemCondition.LIKE_NEW, 300000L, ListingStatus.AVAILABLE));
        candidates.add(createListing("c4", "cat1", ItemCondition.LIKE_NEW, 400000L, ListingStatus.AVAILABLE));
        candidates.add(createListing("c5", "cat1", ItemCondition.LIKE_NEW, 500000L, ListingStatus.AVAILABLE));

        candidates.add(createListing("c6", "cat1", ItemCondition.USED, 2000000L, ListingStatus.AVAILABLE));
        candidates.add(createListing("c7", "cat1", ItemCondition.USED, 3000000L, ListingStatus.AVAILABLE));
        candidates.add(createListing("c8", "cat1", ItemCondition.USED, 4000000L, ListingStatus.AVAILABLE));
        candidates.add(createListing("c9", "cat1", ItemCondition.USED, 5000000L, ListingStatus.AVAILABLE));
        candidates.add(createListing("c10", "cat1", ItemCondition.USED, 6000000L, ListingStatus.AVAILABLE));

        long[] range = engine.priceRange(target, candidates);
        assertNotNull(range);
        assertEquals(2, range.length);
        assertEquals(200000L, range[0]);
        assertEquals(400000L, range[1]);
    }

    @Test
    public void priceRange_lessThanFiveSameCondition_atLeastFiveSameCategory_usesP2() {
        Listing target = createListing("t1", "cat1", ItemCondition.LIKE_NEW, 1000000L, ListingStatus.AVAILABLE);
        List<Listing> candidates = new ArrayList<Listing>();
        candidates.add(createListing("c1", "cat1", ItemCondition.LIKE_NEW, 100000L, ListingStatus.AVAILABLE));
        candidates.add(createListing("c2", "cat1", ItemCondition.LIKE_NEW, 200000L, ListingStatus.AVAILABLE));
        candidates.add(createListing("c3", "cat1", ItemCondition.LIKE_NEW, 300000L, ListingStatus.AVAILABLE));
        candidates.add(createListing("c4", "cat1", ItemCondition.USED, 400000L, ListingStatus.AVAILABLE));
        candidates.add(createListing("c5", "cat1", ItemCondition.USED, 500000L, ListingStatus.AVAILABLE));
        candidates.add(createListing("c6", "cat1", ItemCondition.USED, 600000L, ListingStatus.AVAILABLE));

        long[] range = engine.priceRange(target, candidates);
        assertNotNull(range);
        assertEquals(2, range.length);
        assertEquals(200000L, range[0]);
        assertEquals(500000L, range[1]);
    }

    @Test
    public void priceRange_excludesTargetSelfNonAvailableAndZeroPrice() {
        Listing target = createListing("t1", "cat1", ItemCondition.LIKE_NEW, 1000000L, ListingStatus.AVAILABLE);
        List<Listing> candidates = new ArrayList<Listing>();
        candidates.add(createListing("t1", "cat1", ItemCondition.LIKE_NEW, 100000L, ListingStatus.AVAILABLE));
        candidates.add(createListing("c1", "cat1", ItemCondition.LIKE_NEW, 200000L, ListingStatus.SOLD));
        candidates.add(createListing("c2", "cat1", ItemCondition.LIKE_NEW, 300000L, ListingStatus.DRAFT));
        candidates.add(createListing("c3", "cat1", ItemCondition.LIKE_NEW, 400000L, ListingStatus.HIDDEN));
        candidates.add(createListing("c4", "cat1", ItemCondition.LIKE_NEW, 0L, ListingStatus.AVAILABLE));
        candidates.add(createListing("c5", "cat2", ItemCondition.LIKE_NEW, 500000L, ListingStatus.AVAILABLE));

        candidates.add(createListing("c6", "cat1", ItemCondition.LIKE_NEW, 100000L, ListingStatus.AVAILABLE));
        candidates.add(createListing("c7", "cat1", ItemCondition.LIKE_NEW, 200000L, ListingStatus.AVAILABLE));
        candidates.add(createListing("c8", "cat1", ItemCondition.LIKE_NEW, 300000L, ListingStatus.AVAILABLE));
        candidates.add(createListing("c9", "cat1", ItemCondition.LIKE_NEW, 400000L, ListingStatus.AVAILABLE));

        long[] range = engine.priceRange(target, candidates);
        assertNotNull(range);
        assertEquals(0, range.length);
    }

    @Test
    public void priceRange_roundsToNearestTenThousand() {
        Listing target = createListing("t1", "cat1", ItemCondition.USED, 500000L, ListingStatus.AVAILABLE);
        List<Listing> candidates = new ArrayList<Listing>();
        candidates.add(createListing("c1", "cat1", ItemCondition.USED, 104000L, ListingStatus.AVAILABLE));
        candidates.add(createListing("c2", "cat1", ItemCondition.USED, 126000L, ListingStatus.AVAILABLE));
        candidates.add(createListing("c3", "cat1", ItemCondition.USED, 200000L, ListingStatus.AVAILABLE));
        candidates.add(createListing("c4", "cat1", ItemCondition.USED, 345000L, ListingStatus.AVAILABLE));
        candidates.add(createListing("c5", "cat1", ItemCondition.USED, 500000L, ListingStatus.AVAILABLE));

        long[] range = engine.priceRange(target, candidates);
        assertNotNull(range);
        assertEquals(2, range.length);
        assertEquals(130000L, range[0]);
        assertEquals(350000L, range[1]);
    }
}
