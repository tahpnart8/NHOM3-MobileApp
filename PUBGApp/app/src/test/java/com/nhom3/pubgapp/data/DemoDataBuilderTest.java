package com.nhom3.pubgapp.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.firebase.firestore.GeoPoint;
import com.nhom3.pubgapp.model.Category;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.enums.ItemCondition;
import com.nhom3.pubgapp.util.GeoUtils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DemoDataBuilderTest {

    private static final int MAX_CONDITION_FIELDS = 30;
    private static final int MAX_TITLE_LENGTH = 150;

    private List<Listing> sampleListings() {
        List<String> imageIds = new ArrayList<String>();
        List<String> thumbs = new ArrayList<String>();
        for (int variant = 0; variant < DemoDataBuilder.IMAGE_VARIANTS; variant++) {
            imageIds.add("image_" + variant);
            thumbs.add("thumb_" + variant);
        }
        return DemoDataBuilder.buildListings(imageIds, thumbs);
    }

    private List<Listing> listingsOf(List<Listing> all, String categoryId) {
        List<Listing> result = new ArrayList<Listing>();
        for (Listing listing : all) {
            if (categoryId.equals(listing.getCategoryId())) {
                result.add(listing);
            }
        }
        return result;
    }

    @Test
    public void categories_haveSixRootsAndTwelveChildren() {
        List<Category> roots = DemoDataBuilder.buildRootCategories();
        List<Category> children = DemoDataBuilder.buildChildCategories();
        assertEquals(6, roots.size());
        assertEquals(12, children.size());
        Set<String> rootIds = new HashSet<String>();
        for (Category root : roots) {
            assertTrue(root.isRoot());
            assertTrue(root.getId().startsWith(DemoDataBuilder.CATEGORY_ID_PREFIX));
            rootIds.add(root.getId());
        }
        for (Category child : children) {
            assertTrue(rootIds.contains(child.getParentId()));
            assertTrue(child.getConditionFields().size() <= MAX_CONDITION_FIELDS);
        }
    }

    @Test
    public void listings_haveSixPerChildWithUniqueDemoIds() {
        List<Listing> listings = sampleListings();
        assertEquals(72, listings.size());
        Set<String> ids = new HashSet<String>();
        for (Listing listing : listings) {
            assertTrue(listing.getId().startsWith(DemoDataBuilder.LISTING_ID_PREFIX));
            ids.add(listing.getId());
            assertTrue(listing.getTitle().length() <= MAX_TITLE_LENGTH);
            assertEquals(1, listing.getImageIds().size());
            assertFalse(listing.getCoverThumb().isEmpty());
        }
        assertEquals(72, ids.size());
        for (Category child : DemoDataBuilder.buildChildCategories()) {
            assertEquals(DemoDataBuilder.LISTINGS_PER_CHILD, listingsOf(listings, child.getId()).size());
        }
    }

    @Test
    public void firstChildren_allUsed_otherChildrenMixed() {
        List<Listing> listings = sampleListings();
        List<Category> children = DemoDataBuilder.buildChildCategories();
        for (int index = 0; index < children.size(); index++) {
            Set<ItemCondition> conditions = new HashSet<ItemCondition>();
            for (Listing listing : listingsOf(listings, children.get(index).getId())) {
                conditions.add(listing.getItemCondition());
            }
            if (index < DemoDataBuilder.SAME_CONDITION_CHILDREN) {
                assertEquals(1, conditions.size());
            } else {
                assertTrue(conditions.size() > 1);
            }
        }
    }

    @Test
    public void prices_spreadWidelyInsideEachCategory() {
        List<Listing> listings = sampleListings();
        for (Category child : DemoDataBuilder.buildChildCategories()) {
            long lowest = Long.MAX_VALUE;
            long highest = 0L;
            for (Listing listing : listingsOf(listings, child.getId())) {
                assertTrue(listing.getPrice() > 0L);
                lowest = Math.min(lowest, listing.getPrice());
                highest = Math.max(highest, listing.getPrice());
            }
            assertTrue(highest >= 2L * lowest);
        }
    }

    // Issue #339: titles are accented, and the unaccented words a user types still find them.
    @Test
    public void titles_areAccentedAndStillFoundWithoutAccents() {
        boolean foundRacket = false;
        for (Listing listing : sampleListings()) {
            if (listing.getTitle().startsWith("Vợt cầu lông")) {
                foundRacket = true;
                listing.buildSearchKeywords();
                assertTrue(listing.getSearchKeywords().contains("vot"));
                assertTrue(listing.getSearchKeywords().contains("cau"));
                assertTrue(listing.getSearchKeywords().contains("long"));
            }
        }
        assertTrue(foundRacket);
    }

    @Test
    public void titles_shareTheCategoryKeyword() {
        List<Listing> listings = sampleListings();
        for (Category child : DemoDataBuilder.buildChildCategories()) {
            List<Listing> inCategory = listingsOf(listings, child.getId());
            String keyword = inCategory.get(0).getTitle().split(" ")[0];
            for (Listing listing : inCategory) {
                assertTrue(listing.getTitle().startsWith(keyword));
            }
        }
    }

    @Test
    public void locations_coverThreeProvincesAndTheRadiusChips() {
        GeoPoint center = new GeoPoint(DemoDataBuilder.CENTER_LATITUDE, DemoDataBuilder.CENTER_LONGITUDE);
        GeoUtils geoUtils = new GeoUtils();
        Set<String> provinces = new HashSet<String>();
        boolean withinOne = false;
        boolean withinThree = false;
        boolean withinFive = false;
        boolean beyondFive = false;
        for (Listing listing : sampleListings()) {
            provinces.add(listing.getProvince());
            if (!"TP.HCM".equals(listing.getProvince())) {
                continue;
            }
            withinOne = withinOne || geoUtils.isWithinRadius(listing.getLocation(), center, 1);
            withinThree = withinThree || geoUtils.isWithinRadius(listing.getLocation(), center, 3);
            withinFive = withinFive || geoUtils.isWithinRadius(listing.getLocation(), center, 5);
            beyondFive = beyondFive || !geoUtils.isWithinRadius(listing.getLocation(), center, 5);
        }
        assertTrue(provinces.size() >= 3);
        assertTrue(withinOne && withinThree && withinFive && beyondFive);
    }

    @Test
    public void conditionValues_useOnlyTheCategoryFields() {
        List<Listing> listings = sampleListings();
        for (Category child : DemoDataBuilder.buildChildCategories()) {
            for (Listing listing : listingsOf(listings, child.getId())) {
                for (String field : listing.getConditionValues().keySet()) {
                    assertTrue(child.getConditionFields().contains(field));
                }
            }
        }
    }
}
