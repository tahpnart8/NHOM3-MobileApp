package com.nhom3.pubgapp.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.enums.ItemCondition;
import com.nhom3.pubgapp.model.enums.TradeType;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ShopViewModelTest {

    private Listing createValidListing() {
        Listing listing = new Listing();
        listing.setTitle("Bàn phím cơ");
        listing.setCategoryId("cat-pc");
        listing.setItemCondition(ItemCondition.LIKE_NEW);
        List<TradeType> types = new ArrayList<>();
        types.add(TradeType.SELL);
        listing.setTradeTypes(types);
        listing.setPrice(350000L);
        return listing;
    }

    @Test
    public void testFindMissingFields_valid() {
        Listing listing = createValidListing();
        List<String> missing = ShopViewModel.findMissingFields(listing, 2);
        assertTrue(missing.isEmpty());
    }

    @Test
    public void testFindMissingFields_missingTitle() {
        Listing listing = createValidListing();
        listing.setTitle("   ");
        List<String> missing = ShopViewModel.findMissingFields(listing, 1);
        assertTrue(missing.contains(ShopViewModel.FIELD_TITLE));
    }

    @Test
    public void testFindMissingFields_missingCategory() {
        Listing listing = createValidListing();
        listing.setCategoryId(null);
        List<String> missing = ShopViewModel.findMissingFields(listing, 1);
        assertTrue(missing.contains(ShopViewModel.FIELD_CATEGORY));
    }

    @Test
    public void testFindMissingFields_missingCondition() {
        Listing listing = createValidListing();
        listing.setItemCondition(null);
        List<String> missing = ShopViewModel.findMissingFields(listing, 1);
        assertTrue(missing.contains(ShopViewModel.FIELD_CONDITION));
    }

    @Test
    public void testFindMissingFields_invalidImageCount() {
        Listing listing = createValidListing();
        List<String> missing0 = ShopViewModel.findMissingFields(listing, 0);
        assertTrue(missing0.contains(ShopViewModel.FIELD_IMAGES));

        List<String> missing6 = ShopViewModel.findMissingFields(listing, 6);
        assertTrue(missing6.contains(ShopViewModel.FIELD_IMAGES));
    }

    @Test
    public void testFindMissingFields_missingTradeTypes() {
        Listing listing = createValidListing();
        listing.setTradeTypes(new ArrayList<TradeType>());
        List<String> missing = ShopViewModel.findMissingFields(listing, 1);
        assertTrue(missing.contains(ShopViewModel.FIELD_TRADE_TYPE));
    }

    @Test
    public void testFindMissingFields_sellWithoutPrice() {
        Listing listing = createValidListing();
        listing.setPrice(0L);
        List<String> missing = ShopViewModel.findMissingFields(listing, 1);
        assertTrue(missing.contains(ShopViewModel.FIELD_PRICE));
    }

    @Test
    public void testNormalizePrice_sellOnly() {
        List<TradeType> types = new ArrayList<>();
        types.add(TradeType.SELL);
        long price = ShopViewModel.normalizePrice(types, 250000L);
        assertEquals(250000L, price);
    }

    @Test
    public void testNormalizePrice_noSell() {
        List<TradeType> types = new ArrayList<>();
        types.add(TradeType.EXCHANGE);
        long price = ShopViewModel.normalizePrice(types, 250000L);
        assertEquals(0L, price);
    }

    @Test
    public void testNormalizePrice_withGiveaway() {
        List<TradeType> types = new ArrayList<>();
        types.add(TradeType.SELL);
        types.add(TradeType.GIVEAWAY);
        long price = ShopViewModel.normalizePrice(types, 250000L);
        assertEquals(0L, price);
    }
}
