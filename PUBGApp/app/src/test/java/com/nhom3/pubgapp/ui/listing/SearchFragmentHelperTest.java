package com.nhom3.pubgapp.ui.listing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.User;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Verifies filtering of incomplete recent searches and current user in search results.
public class SearchFragmentHelperTest {

    @Test
    public void cleanRecentSearches_dropsSingleCharAndIncompletePrefixes() {
        List<String> raw = Arrays.asList("vot", "v", "vo", "ao", "ao khoac", "VOT", "  ");
        List<String> cleaned = SearchFragment.cleanRecentSearches(raw);

        // Incomplete prefixes, single letters and duplicates are dropped.
        assertEquals(2, cleaned.size());
        assertEquals("vot", cleaned.get(0));
        assertEquals("ao khoac", cleaned.get(1));
    }

    @Test
    public void cleanRecentSearches_handlesNullAndEmpty() {
        List<String> cleanedNull = SearchFragment.cleanRecentSearches(null);
        assertNotNull(cleanedNull);
        assertTrue(cleanedNull.isEmpty());

        List<String> cleanedEmpty = SearchFragment.cleanRecentSearches(new ArrayList<String>());
        assertNotNull(cleanedEmpty);
        assertTrue(cleanedEmpty.isEmpty());
    }

    @Test
    public void filterOutCurrentUser_removesMatchingUserId() {
        User u1 = new User();
        u1.setId("user_1");
        User u2 = new User();
        u2.setId("user_2");
        User u3 = new User();
        u3.setId("user_3");

        List<User> list = Arrays.asList(u1, u2, u3);
        List<User> filtered = SearchFragment.filterOutCurrentUser(list, "user_2");

        assertEquals(2, filtered.size());
        assertEquals("user_1", filtered.get(0).getId());
        assertEquals("user_3", filtered.get(1).getId());
    }

    @Test
    public void filterOutCurrentUser_guestOrNullRetainsAll() {
        User u1 = new User();
        u1.setId("user_1");
        List<User> list = Arrays.asList(u1);

        List<User> filteredNull = SearchFragment.filterOutCurrentUser(list, null);
        assertEquals(1, filteredNull.size());

        List<User> filteredEmpty = SearchFragment.filterOutCurrentUser(list, "");
        assertEquals(1, filteredEmpty.size());
    }
}
