package com.nhom3.pubgapp.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.enums.ItemCondition;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.model.enums.SortOrder;
import com.nhom3.pubgapp.model.enums.TradeType;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
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

    @Test
    public void testCanSaveDraft_withTitle() {
        Listing listing = new Listing();
        listing.setTitle("Tiêu đề món đồ");
        assertTrue(ShopViewModel.canSaveDraft(listing, 0));
    }

    @Test
    public void testCanSaveDraft_withDescription() {
        Listing listing = new Listing();
        listing.setDescription("Mô tả chi tiết món đồ");
        assertTrue(ShopViewModel.canSaveDraft(listing, 0));
    }

    @Test
    public void testCanSaveDraft_withImages() {
        Listing listing = new Listing();
        assertTrue(ShopViewModel.canSaveDraft(listing, 1));
    }

    @Test
    public void testCanSaveDraft_emptyAll() {
        Listing listing = new Listing();
        assertFalse(ShopViewModel.canSaveDraft(listing, 0));
    }

    @Test
    public void testCanSaveDraft_nullListing() {
        assertFalse(ShopViewModel.canSaveDraft(null, 0));
        assertFalse(ShopViewModel.canSaveDraft(null, 2));
    }

    @Test
    public void testFilterByTab() {
        List<Listing> listings = new ArrayList<Listing>();

        Listing available = new Listing();
        available.setStatus(ListingStatus.AVAILABLE);
        listings.add(available);

        Listing reserved = new Listing();
        reserved.setStatus(ListingStatus.RESERVED);
        listings.add(reserved);

        Listing sold = new Listing();
        sold.setStatus(ListingStatus.SOLD);
        listings.add(sold);

        Listing hidden = new Listing();
        hidden.setStatus(ListingStatus.HIDDEN);
        listings.add(hidden);

        Listing removed = new Listing();
        removed.setStatus(ListingStatus.REMOVED);
        listings.add(removed);

        Listing draft = new Listing();
        draft.setStatus(ListingStatus.DRAFT);
        listings.add(draft);

        List<Listing> activeTab = ShopViewModel.filterByTab(listings, ShopViewModel.TAB_ACTIVE);
        assertEquals(2, activeTab.size());
        assertTrue(activeTab.contains(available));
        assertTrue(activeTab.contains(reserved));

        List<Listing> soldTab = ShopViewModel.filterByTab(listings, ShopViewModel.TAB_SOLD);
        assertEquals(1, soldTab.size());
        assertTrue(soldTab.contains(sold));

        List<Listing> hiddenTab = ShopViewModel.filterByTab(listings, ShopViewModel.TAB_HIDDEN);
        assertEquals(2, hiddenTab.size());
        assertTrue(hiddenTab.contains(hidden));
        assertTrue(hiddenTab.contains(removed));

        List<Listing> draftTab = ShopViewModel.filterByTab(listings, ShopViewModel.TAB_DRAFT);
        assertEquals(1, draftTab.size());
        assertTrue(draftTab.contains(draft));
    }

    @Test
    public void testSortAndFilter() {
        List<Listing> listings = new ArrayList<Listing>();

        Listing item1 = new Listing();
        item1.setCategoryId("cat-1");
        item1.setCreatedAt(new Timestamp(new Date(1000000L)));
        listings.add(item1);

        Listing item2 = new Listing();
        item2.setCategoryId("cat-2");
        item2.setCreatedAt(new Timestamp(new Date(2000000L)));
        listings.add(item2);

        Listing item3 = new Listing();
        item3.setCategoryId("cat-1");
        item3.setCreatedAt(new Timestamp(new Date(3000000L)));
        listings.add(item3);

        // Filter by cat-1 and sort newest
        List<Listing> cat1 = ShopViewModel.sortAndFilter(listings, SortOrder.NEWEST, "cat-1");
        assertEquals(2, cat1.size());
        assertEquals(item3, cat1.get(0));
        assertEquals(item1, cat1.get(1));

        // No category filter (all categories)
        List<Listing> all = ShopViewModel.sortAndFilter(listings, SortOrder.NEWEST, "");
        assertEquals(3, all.size());
        assertEquals(item3, all.get(0));
        assertEquals(item2, all.get(1));
        assertEquals(item1, all.get(2));
    }

    @Test
    public void testAllowedActions() {
        Listing available = new Listing();
        available.setStatus(ListingStatus.AVAILABLE);
        List<String> actionsAvailable = ShopViewModel.allowedActions(available);
        assertTrue(actionsAvailable.contains(ShopViewModel.ACTION_EDIT));
        assertTrue(actionsAvailable.contains(ShopViewModel.ACTION_MARK_SOLD));
        assertTrue(actionsAvailable.contains(ShopViewModel.ACTION_HIDE));

        Listing reserved = new Listing();
        reserved.setStatus(ListingStatus.RESERVED);
        assertTrue(ShopViewModel.allowedActions(reserved).isEmpty());

        Listing selfHidden = new Listing();
        selfHidden.setStatus(ListingStatus.HIDDEN);
        selfHidden.setStatusReason("");
        List<String> actionsSelfHidden = ShopViewModel.allowedActions(selfHidden);
        assertTrue(actionsSelfHidden.contains(ShopViewModel.ACTION_SHOW));
        assertTrue(actionsSelfHidden.contains(ShopViewModel.ACTION_EDIT));

        Listing adminHidden = new Listing();
        adminHidden.setStatus(ListingStatus.HIDDEN);
        adminHidden.setStatusReason("Vi phạm quy định đăng tin");
        List<String> actionsAdminHidden = ShopViewModel.allowedActions(adminHidden);
        assertEquals(1, actionsAdminHidden.size());
        assertTrue(actionsAdminHidden.contains(ShopViewModel.ACTION_VIEW_REASON));

        Listing sold = new Listing();
        sold.setStatus(ListingStatus.SOLD);
        List<String> actionsSold = ShopViewModel.allowedActions(sold);
        assertEquals(1, actionsSold.size());
        assertTrue(actionsSold.contains(ShopViewModel.ACTION_REOPEN));

        Listing draft = new Listing();
        draft.setStatus(ListingStatus.DRAFT);
        List<String> actionsDraft = ShopViewModel.allowedActions(draft);
        assertTrue(actionsDraft.contains(ShopViewModel.ACTION_EDIT));
        assertTrue(actionsDraft.contains(ShopViewModel.ACTION_PUBLISH));
        assertTrue(actionsDraft.contains(ShopViewModel.ACTION_DELETE));

        Listing removed = new Listing();
        removed.setStatus(ListingStatus.REMOVED);
        assertTrue(ShopViewModel.allowedActions(removed).isEmpty());
    }

    @Test
    public void testIsAllowedStatusChange() {
        assertTrue(ShopViewModel.isAllowedStatusChange(ListingStatus.AVAILABLE, ListingStatus.SOLD));
        assertTrue(ShopViewModel.isAllowedStatusChange(ListingStatus.SOLD, ListingStatus.AVAILABLE));

        assertFalse(ShopViewModel.isAllowedStatusChange(ListingStatus.AVAILABLE, ListingStatus.HIDDEN));
        assertFalse(ShopViewModel.isAllowedStatusChange(ListingStatus.RESERVED, ListingStatus.SOLD));
        assertFalse(ShopViewModel.isAllowedStatusChange(ListingStatus.DRAFT, ListingStatus.AVAILABLE));
        assertFalse(ShopViewModel.isAllowedStatusChange(ListingStatus.SOLD, ListingStatus.REMOVED));
    }
}
