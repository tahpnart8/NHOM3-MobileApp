package com.nhom3.pubgapp.ui.main;

import static org.junit.Assert.assertEquals;
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

    @Test
    public void decideTabAction_publicTabs_alwaysNavigate() {
        assertEquals(MainActivity.TabAction.NAVIGATE,
                MainActivity.decideTabAction(R.id.homeFragment, false, false));
        assertEquals(MainActivity.TabAction.NAVIGATE,
                MainActivity.decideTabAction(R.id.homeFragment, false, true));
        assertEquals(MainActivity.TabAction.NAVIGATE,
                MainActivity.decideTabAction(R.id.searchFragment, true, false));
    }

    @Test
    public void decideTabAction_protectedTab_signedIn_navigates() {
        assertEquals(MainActivity.TabAction.NAVIGATE,
                MainActivity.decideTabAction(R.id.myProfileFragment, true, false));
        assertEquals(MainActivity.TabAction.NAVIGATE,
                MainActivity.decideTabAction(R.id.postListingFragment, true, true));
    }

    @Test
    public void decideTabAction_protectedTab_restoring_waitsRestore() {
        assertEquals(MainActivity.TabAction.WAIT_RESTORE,
                MainActivity.decideTabAction(R.id.myProfileFragment, false, true));
        assertEquals(MainActivity.TabAction.WAIT_RESTORE,
                MainActivity.decideTabAction(R.id.postListingFragment, false, true));
        assertEquals(MainActivity.TabAction.WAIT_RESTORE,
                MainActivity.decideTabAction(R.id.conversationListFragment, false, true));
    }

    @Test
    public void decideTabAction_protectedTab_guestNotRestoring_blocksToSignIn() {
        assertEquals(MainActivity.TabAction.BLOCK_TO_SIGN_IN,
                MainActivity.decideTabAction(R.id.myProfileFragment, false, false));
        assertEquals(MainActivity.TabAction.BLOCK_TO_SIGN_IN,
                MainActivity.decideTabAction(R.id.postListingFragment, false, false));
        assertEquals(MainActivity.TabAction.BLOCK_TO_SIGN_IN,
                MainActivity.decideTabAction(R.id.conversationListFragment, false, false));
    }
}
