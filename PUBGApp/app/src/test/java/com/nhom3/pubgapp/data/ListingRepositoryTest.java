package com.nhom3.pubgapp.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.SearchFilter;
import com.nhom3.pubgapp.model.enums.ItemCondition;
import com.nhom3.pubgapp.model.enums.TradeType;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ListingRepositoryTest {

    @Test
    public void testPickQueryWord_nullOrEmpty() {
        assertEquals("", ListingRepository.pickQueryWord(null));
        assertEquals("", ListingRepository.pickQueryWord("   "));
    }

    @Test
    public void testPickQueryWord_singleWord() {
        assertEquals("iphone", ListingRepository.pickQueryWord("iPhone"));
    }

    @Test
    public void testPickQueryWord_multipleWordsWithAccents() {
        // "Điện thoại" -> "dien", "thoai" -> longest is "thoai" (length 5)
        String word = ListingRepository.pickQueryWord("Điện thoại");
        assertEquals("thoai", word);
    }

    @Test
    public void testPickQueryWord_longestWordPicked() {
        // "xe dap the thao" -> "dap" (3), "the" (3), "thao" (4)
        String word = ListingRepository.pickQueryWord("xe đạp thể thao");
        assertEquals("thao", word);
    }

    @Test
    public void testMatchesLocalFilter_nullInputs() {
        assertTrue(ListingRepository.matchesLocalFilter(null, null));
        Listing listing = new Listing();
        assertTrue(ListingRepository.matchesLocalFilter(listing, null));
        SearchFilter filter = new SearchFilter();
        assertTrue(ListingRepository.matchesLocalFilter(null, filter));
    }

    @Test
    public void testMatchesLocalFilter_keywordMatching() {
        Listing listing = new Listing();
        List<String> keywords = new ArrayList<String>();
        keywords.add("iphone");
        keywords.add("13");
        keywords.add("pro");
        listing.setSearchKeywords(keywords);

        SearchFilter filter = new SearchFilter();
        filter.setKeyword("iPhone 13");
        assertTrue(ListingRepository.matchesLocalFilter(listing, filter));

        filter.setKeyword("Samsung");
        assertFalse(ListingRepository.matchesLocalFilter(listing, filter));
    }

    @Test
    public void testMatchesLocalFilter_partialWordDoesNotMatch() {
        Listing listing = new Listing();
        List<String> keywords = new ArrayList<String>();
        keywords.add("iphone");
        listing.setSearchKeywords(keywords);

        SearchFilter filter = new SearchFilter();
        filter.setKeyword("ipho");
        assertFalse(ListingRepository.matchesLocalFilter(listing, filter));
    }

    @Test
    public void testMatchesLocalFilter_priceRange() {
        Listing listing = new Listing();
        listing.setPrice(1500000);

        SearchFilter filter = new SearchFilter();
        filter.setMinPrice(1000000);
        filter.setMaxPrice(2000000);
        assertTrue(ListingRepository.matchesLocalFilter(listing, filter));

        filter.setMinPrice(1600000);
        assertFalse(ListingRepository.matchesLocalFilter(listing, filter));

        filter.setMinPrice(0);
        filter.setMaxPrice(1400000);
        assertFalse(ListingRepository.matchesLocalFilter(listing, filter));
    }

    @Test
    public void testMatchesLocalFilter_conditions() {
        Listing listing = new Listing();
        listing.setItemCondition(ItemCondition.LIKE_NEW);

        SearchFilter filter = new SearchFilter();
        List<ItemCondition> conditions = new ArrayList<ItemCondition>();
        conditions.add(ItemCondition.NEW);
        conditions.add(ItemCondition.LIKE_NEW);
        filter.setConditions(conditions);
        assertTrue(ListingRepository.matchesLocalFilter(listing, filter));

        conditions.clear();
        conditions.add(ItemCondition.USED);
        assertFalse(ListingRepository.matchesLocalFilter(listing, filter));
    }

    @Test
    public void testMatchesLocalFilter_tradeTypes() {
        Listing listing = new Listing();
        List<TradeType> types = new ArrayList<TradeType>();
        types.add(TradeType.SELL);
        types.add(TradeType.EXCHANGE);
        listing.setTradeTypes(types);

        SearchFilter filter = new SearchFilter();
        List<TradeType> filterTypes = new ArrayList<TradeType>();
        filterTypes.add(TradeType.EXCHANGE);
        filter.setTradeTypes(filterTypes);
        assertTrue(ListingRepository.matchesLocalFilter(listing, filter));

        filterTypes.clear();
        filterTypes.add(TradeType.GIVEAWAY);
        assertFalse(ListingRepository.matchesLocalFilter(listing, filter));
    }

    @Test
    public void testMatchesLocalFilter_categoryAndProvince() {
        Listing listing = new Listing();
        listing.setCategoryId("electronics");
        listing.setProvince("TP.HCM");

        SearchFilter filter = new SearchFilter();
        filter.setCategoryId("electronics");
        filter.setProvince("TP.HCM");
        assertTrue(ListingRepository.matchesLocalFilter(listing, filter));

        filter.setProvince("Hà Nội");
        assertFalse(ListingRepository.matchesLocalFilter(listing, filter));
    }
}
