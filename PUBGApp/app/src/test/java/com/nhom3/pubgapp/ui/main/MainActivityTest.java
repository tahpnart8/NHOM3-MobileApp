package com.nhom3.pubgapp.ui.main;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.R;

import org.junit.Test;

// Covers the decisions MainActivity makes without Android: which tabs a guest may open, when the auto
// release check runs, and when the bell badge shows. The wiring itself needs a device.
public class MainActivityTest {

    @Test
    public void isTabBlockedForGuest_guestOnPostTab_isBlocked() {
        assertTrue(MainActivity.isTabBlockedForGuest(R.id.postListingFragment, false));
    }

    @Test
    public void isTabBlockedForGuest_guestOnChatTab_isBlocked() {
        assertTrue(MainActivity.isTabBlockedForGuest(R.id.conversationListFragment, false));
    }

    @Test
    public void isTabBlockedForGuest_guestOnProfileTab_isBlocked() {
        assertTrue(MainActivity.isTabBlockedForGuest(R.id.myProfileFragment, false));
    }

    @Test
    public void isTabBlockedForGuest_guestOnHomeAndSearch_isAllowed() {
        assertFalse(MainActivity.isTabBlockedForGuest(R.id.homeFragment, false));
        assertFalse(MainActivity.isTabBlockedForGuest(R.id.searchFragment, false));
    }

    @Test
    public void isTabBlockedForGuest_signedInUser_neverBlocked() {
        assertFalse(MainActivity.isTabBlockedForGuest(R.id.postListingFragment, true));
        assertFalse(MainActivity.isTabBlockedForGuest(R.id.conversationListFragment, true));
        assertFalse(MainActivity.isTabBlockedForGuest(R.id.myProfileFragment, true));
    }

    @Test
    public void shouldCheckAutoRelease_signedInAndNotYetChecked_runs() {
        assertTrue(MainActivity.shouldCheckAutoRelease(false, true));
    }

    @Test
    public void shouldCheckAutoRelease_alreadyChecked_doesNotRunAgain() {
        // A rotation recreates the Activity; the saved flag keeps the check from running twice.
        assertFalse(MainActivity.shouldCheckAutoRelease(true, true));
    }

    @Test
    public void shouldCheckAutoRelease_guest_doesNotRun() {
        assertFalse(MainActivity.shouldCheckAutoRelease(false, false));
    }

    @Test
    public void isBadgeVisible_nullOrZero_hidden() {
        assertFalse(MainActivity.isBadgeVisible(null));
        assertFalse(MainActivity.isBadgeVisible(Integer.valueOf(0)));
    }

    @Test
    public void isBadgeVisible_oneOrMore_shown() {
        assertTrue(MainActivity.isBadgeVisible(Integer.valueOf(1)));
    }

    @Test
    public void isBadgeOverflow_ninetyNine_showsExactNumber() {
        assertFalse(MainActivity.isBadgeOverflow(Integer.valueOf(99)));
    }

    @Test
    public void isBadgeOverflow_oneHundred_showsNinetyNinePlus() {
        assertTrue(MainActivity.isBadgeOverflow(Integer.valueOf(100)));
    }
}
