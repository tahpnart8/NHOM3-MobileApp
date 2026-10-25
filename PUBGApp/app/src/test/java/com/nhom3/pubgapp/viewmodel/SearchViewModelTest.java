package com.nhom3.pubgapp.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.SearchFilter;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.SortOrder;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchViewModelTest {

    @Test
    public void testIsPriceRangeValid_unbounded() {
        assertTrue(SearchViewModel.isPriceRangeValid(0, 0));
        assertTrue(SearchViewModel.isPriceRangeValid(100000, 0));
        assertTrue(SearchViewModel.isPriceRangeValid(0, 500000));
    }

    @Test
    public void testIsPriceRangeValid_validRange() {
        assertTrue(SearchViewModel.isPriceRangeValid(100000, 100000));
        assertTrue(SearchViewModel.isPriceRangeValid(100000, 500000));
    }

    @Test
    public void testIsPriceRangeValid_invalidRange() {
        assertFalse(SearchViewModel.isPriceRangeValid(500000, 100000));
    }

    @Test
    public void testSortResults_nullList() {
        List<Listing> sorted = SearchViewModel.sortResults(null, SortOrder.NEWEST);
        assertTrue(sorted.isEmpty());
    }

    @Test
    public void testSortResults_priceAsc() {
        List<Listing> list = new ArrayList<Listing>();
        Listing l1 = new Listing();
        l1.setPrice(300000);
        Listing l2 = new Listing();
        l2.setPrice(100000);
        Listing l3 = new Listing();
        l3.setPrice(200000);
        list.add(l1);
        list.add(l2);
        list.add(l3);

        List<Listing> sorted = SearchViewModel.sortResults(list, SortOrder.PRICE_ASC);
        assertEquals(3, sorted.size());
        assertEquals(100000, sorted.get(0).getPrice());
        assertEquals(200000, sorted.get(1).getPrice());
        assertEquals(300000, sorted.get(2).getPrice());
    }

    @Test
    public void testSortResults_priceDesc() {
        List<Listing> list = new ArrayList<Listing>();
        Listing l1 = new Listing();
        l1.setPrice(100000);
        Listing l2 = new Listing();
        l2.setPrice(500000);
        Listing l3 = new Listing();
        l3.setPrice(300000);
        list.add(l1);
        list.add(l2);
        list.add(l3);

        List<Listing> sorted = SearchViewModel.sortResults(list, SortOrder.PRICE_DESC);
        assertEquals(3, sorted.size());
        assertEquals(500000, sorted.get(0).getPrice());
        assertEquals(300000, sorted.get(1).getPrice());
        assertEquals(100000, sorted.get(2).getPrice());
    }

    @Test
    public void testSortResults_newest() {
        List<Listing> list = new ArrayList<Listing>();
        Listing l1 = new Listing();
        l1.setCreatedAt(new Timestamp(1000, 0));
        Listing l2 = new Listing();
        l2.setCreatedAt(new Timestamp(3000, 0));
        Listing l3 = new Listing();
        l3.setCreatedAt(new Timestamp(2000, 0));
        list.add(l1);
        list.add(l2);
        list.add(l3);

        List<Listing> sorted = SearchViewModel.sortResults(list, SortOrder.NEWEST);
        assertEquals(3, sorted.size());
        assertEquals(3000, sorted.get(0).getCreatedAt().getSeconds());
        assertEquals(2000, sorted.get(1).getCreatedAt().getSeconds());
        assertEquals(1000, sorted.get(2).getCreatedAt().getSeconds());
    }

    @Test
    public void testFilterByRadius_nullOrUnbounded() {
        List<Listing> list = new ArrayList<Listing>();
        Listing l1 = new Listing();
        list.add(l1);

        com.google.firebase.firestore.GeoPoint samplePoint =
                new com.google.firebase.firestore.GeoPoint(10.0, 106.0);
        List<Listing> nullList = SearchViewModel.filterByRadius(null, samplePoint, 3, null);
        assertTrue(nullList.isEmpty());

        List<Listing> noCenter = SearchViewModel.filterByRadius(list, null, 3, null);
        assertEquals(1, noCenter.size());

        List<Listing> noRadius = SearchViewModel.filterByRadius(list, samplePoint, 0, null);
        assertEquals(1, noRadius.size());
    }

    @Test
    public void testFilterByRadius_filtersCorrectly() {
        com.google.firebase.firestore.GeoPoint center = new com.google.firebase.firestore.GeoPoint(10.7769, 106.7009);

        Listing near1 = new Listing();
        near1.setTitle("Near 0.02km");
        near1.setLocation(new com.google.firebase.firestore.GeoPoint(10.7770, 106.7010));

        Listing near2 = new Listing();
        near2.setTitle("Near 1.5km");
        near2.setLocation(new com.google.firebase.firestore.GeoPoint(10.7900, 106.7000));

        Listing near4km = new Listing();
        near4km.setTitle("Near 4.8km");
        near4km.setLocation(new com.google.firebase.firestore.GeoPoint(10.8200, 106.7000));

        Listing far14km = new Listing();
        far14km.setTitle("Far 13.7km");
        far14km.setLocation(new com.google.firebase.firestore.GeoPoint(10.9000, 106.7000));

        Listing noLoc = new Listing();
        noLoc.setTitle("No Location");
        noLoc.setLocation(null);

        List<Listing> list = new ArrayList<Listing>();
        list.add(near1);
        list.add(near2);
        list.add(near4km);
        list.add(far14km);
        list.add(noLoc);

        // 1 km filter: only near1
        List<Listing> within1km = SearchViewModel.filterByRadius(list, center, 1, null);
        assertEquals(1, within1km.size());
        assertEquals("Near 0.02km", within1km.get(0).getTitle());

        // 3 km filter: near1 and near2
        List<Listing> within3km = SearchViewModel.filterByRadius(list, center, 3, null);
        assertEquals(2, within3km.size());
        assertEquals("Near 0.02km", within3km.get(0).getTitle());
        assertEquals("Near 1.5km", within3km.get(1).getTitle());

        // 5 km filter: near1, near2, near4km
        List<Listing> within5km = SearchViewModel.filterByRadius(list, center, 5, null);
        assertEquals(3, within5km.size());
        assertEquals("Near 0.02km", within5km.get(0).getTitle());
        assertEquals("Near 1.5km", within5km.get(1).getTitle());
        assertEquals("Near 4.8km", within5km.get(2).getTitle());
    }

    @Test
    public void testSortByDistance_ordersNearestFirst() {
        com.google.firebase.firestore.GeoPoint center = new com.google.firebase.firestore.GeoPoint(10.7769, 106.7009);

        Listing near1 = new Listing();
        near1.setTitle("Near 0.02km");
        near1.setLocation(new com.google.firebase.firestore.GeoPoint(10.7770, 106.7010));

        Listing near2 = new Listing();
        near2.setTitle("Near 1.5km");
        near2.setLocation(new com.google.firebase.firestore.GeoPoint(10.7900, 106.7000));

        Listing far = new Listing();
        far.setTitle("Far 13.7km");
        far.setLocation(new com.google.firebase.firestore.GeoPoint(10.9000, 106.7000));

        Listing noLoc = new Listing();
        noLoc.setTitle("No Location");
        noLoc.setLocation(null);

        List<Listing> list = new ArrayList<Listing>();
        list.add(far);
        list.add(noLoc);
        list.add(near2);
        list.add(near1);

        List<Listing> sorted = SearchViewModel.sortByDistance(list, center, null);
        assertEquals(4, sorted.size());
        assertEquals("Near 0.02km", sorted.get(0).getTitle());
        assertEquals("Near 1.5km", sorted.get(1).getTitle());
        assertEquals("Far 13.7km", sorted.get(2).getTitle());
        assertEquals("No Location", sorted.get(3).getTitle());
    }

    @Test
    public void testCalculateSellerRating_valid() {
        assertEquals(4.5, SearchViewModel.calculateSellerRating(18, 4), 0.001);
    }

    @Test
    public void testCalculateSellerRating_noRatings() {
        assertEquals(0.0, SearchViewModel.calculateSellerRating(0, 0), 0.001);
    }

    @Test
    public void testCalculateSellerRating_user() {
        User u = new User();
        u.setRatingSum(10);
        u.setRatingCount(2);
        assertEquals(5.0, SearchViewModel.calculateSellerRating(u), 0.001);

        assertEquals(0.0, SearchViewModel.calculateSellerRating((User) null), 0.001);
    }

    @Test
    public void testSortResults_topRated_differentRatings() {
        List<Listing> list = new ArrayList<Listing>();
        Listing l1 = new Listing();
        l1.setSellerId("s1");
        Listing l2 = new Listing();
        l2.setSellerId("s2");
        Listing l3 = new Listing();
        l3.setSellerId("s3");
        list.add(l1);
        list.add(l2);
        list.add(l3);

        Map<String, Double> ratings = new HashMap<String, Double>();
        ratings.put("s1", 4.5);
        ratings.put("s2", 3.0);
        ratings.put("s3", 5.0);

        List<Listing> sorted = SearchViewModel.sortResults(list, SortOrder.TOP_RATED, ratings);
        assertEquals(3, sorted.size());
        assertEquals("s3", sorted.get(0).getSellerId());
        assertEquals("s1", sorted.get(1).getSellerId());
        assertEquals("s2", sorted.get(2).getSellerId());
    }

    @Test
    public void testSortResults_topRated_unratedStandsBehind() {
        List<Listing> list = new ArrayList<Listing>();
        Listing l1 = new Listing();
        l1.setSellerId("s1");
        Listing l2 = new Listing();
        l2.setSellerId("s2");
        list.add(l1);
        list.add(l2);

        Map<String, Double> ratings = new HashMap<String, Double>();
        ratings.put("s1", 0.0);
        ratings.put("s2", 4.0);

        List<Listing> sorted = SearchViewModel.sortResults(list, SortOrder.TOP_RATED, ratings);
        assertEquals(2, sorted.size());
        assertEquals("s2", sorted.get(0).getSellerId());
        assertEquals("s1", sorted.get(1).getSellerId());
    }

    @Test
    public void testSortResults_topRated_equalRatingNewestFirst() {
        List<Listing> list = new ArrayList<Listing>();
        Listing l1 = new Listing();
        l1.setSellerId("s1");
        l1.setCreatedAt(new Timestamp(1000, 0));
        Listing l2 = new Listing();
        l2.setSellerId("s2");
        l2.setCreatedAt(new Timestamp(2000, 0));
        list.add(l1);
        list.add(l2);

        Map<String, Double> ratings = new HashMap<String, Double>();
        ratings.put("s1", 4.0);
        ratings.put("s2", 4.0);

        List<Listing> sorted = SearchViewModel.sortResults(list, SortOrder.TOP_RATED, ratings);
        assertEquals(2, sorted.size());
        assertEquals("s2", sorted.get(0).getSellerId());
        assertEquals("s1", sorted.get(1).getSellerId());
    }

    @Test
    public void testSortResults_topRated_bothUnratedNewestFirst() {
        List<Listing> list = new ArrayList<Listing>();
        Listing l1 = new Listing();
        l1.setSellerId("s1");
        l1.setCreatedAt(new Timestamp(1000, 0));
        Listing l2 = new Listing();
        l2.setSellerId("s2");
        l2.setCreatedAt(new Timestamp(3000, 0));
        list.add(l1);
        list.add(l2);

        Map<String, Double> ratings = new HashMap<String, Double>();

        List<Listing> sorted = SearchViewModel.sortResults(list, SortOrder.TOP_RATED, ratings);
        assertEquals(2, sorted.size());
        assertEquals("s2", sorted.get(0).getSellerId());
        assertEquals("s1", sorted.get(1).getSellerId());
    }

    @Test
    public void testCanReorderExistingResults_onlyAllowedBetweenNewestAndTopRated() {
        SearchFilter fNewest = new SearchFilter();
        fNewest.setSortOrder(SortOrder.NEWEST);

        SearchFilter fTopRated = new SearchFilter();
        fTopRated.setSortOrder(SortOrder.TOP_RATED);

        SearchFilter fPriceAsc = new SearchFilter();
        fPriceAsc.setSortOrder(SortOrder.PRICE_ASC);

        SearchFilter fPriceDesc = new SearchFilter();
        fPriceDesc.setSortOrder(SortOrder.PRICE_DESC);

        // Allowed between NEWEST and TOP_RATED
        assertTrue(SearchViewModel.canReorderExistingResults(fNewest, fTopRated));
        assertTrue(SearchViewModel.canReorderExistingResults(fTopRated, fNewest));

        // Same sort order should not reorder
        assertFalse(SearchViewModel.canReorderExistingResults(fNewest, fNewest));

        // To or from PRICE_ASC / PRICE_DESC must not reorder in memory (needs re-query)
        assertFalse(SearchViewModel.canReorderExistingResults(fNewest, fPriceAsc));
        assertFalse(SearchViewModel.canReorderExistingResults(fTopRated, fPriceAsc));
        assertFalse(SearchViewModel.canReorderExistingResults(fPriceAsc, fTopRated));
        assertFalse(SearchViewModel.canReorderExistingResults(fPriceDesc, fTopRated));

        // Null filters
        assertFalse(SearchViewModel.canReorderExistingResults(null, fTopRated));
        assertFalse(SearchViewModel.canReorderExistingResults(fNewest, null));

        // Other filters changed: must not reorder in memory
        SearchFilter fChangedCategory = new SearchFilter();
        fChangedCategory.setSortOrder(SortOrder.TOP_RATED);
        fChangedCategory.setCategoryId("cat123");
        assertFalse(SearchViewModel.canReorderExistingResults(fNewest, fChangedCategory));
    }
}
