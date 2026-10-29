package com.nhom3.pubgapp.viewmodel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FeedViewModelTest {

    @Test
    public void isForYouFresh_recentList_isKept() {
        long loadedAt = 1000L;
        long now = loadedAt + FeedViewModel.FOR_YOU_MAX_AGE_MS - 1L;
        assertTrue(FeedViewModel.isForYouFresh(true, loadedAt, now));
    }

    @Test
    public void isForYouFresh_oldList_isReloaded() {
        long loadedAt = 1000L;
        long now = loadedAt + FeedViewModel.FOR_YOU_MAX_AGE_MS;
        assertFalse(FeedViewModel.isForYouFresh(true, loadedAt, now));
    }

    @Test
    public void isForYouFresh_noData_isReloaded() {
        assertFalse(FeedViewModel.isForYouFresh(false, 1000L, 1001L));
    }
}
