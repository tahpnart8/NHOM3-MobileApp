package com.nhom3.pubgapp;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.UserStatus;
import com.nhom3.pubgapp.model.enums.VerificationStatus;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserTest {

    private User user;

    @Before
    public void setUp() {
        user = new User();
        user.setEmail("user@example.com");
        user.setDisplayName("Nguyễn Thùy Ngân");
        user.setStatus(UserStatus.ACTIVE);
        user.setVerificationStatus(VerificationStatus.NONE);
        user.setRatingSum(0);
        user.setRatingCount(0);
    }

    @Test
    public void testIsLockedWhenActive() {
        assertFalse(user.isLocked());
    }

    @Test
    public void testIsLockedWhenPermLocked() {
        user.setStatus(UserStatus.PERM_LOCKED);
        assertTrue(user.isLocked());
    }

    @Test
    public void testIsLockedWhenTempLockedInFuture() {
        user.setStatus(UserStatus.TEMP_LOCKED);
        long futureMillis = System.currentTimeMillis() + 86400000L;
        user.setLockedUntil(new Timestamp(new Date(futureMillis)));
        assertTrue(user.isLocked());
    }

    @Test
    public void testIsLockedWhenTempLockedInPast() {
        user.setStatus(UserStatus.TEMP_LOCKED);
        long pastMillis = System.currentTimeMillis() - 86400000L;
        user.setLockedUntil(new Timestamp(new Date(pastMillis)));
        assertFalse(user.isLocked());
    }

    @Test
    public void testIsDeleted() {
        assertFalse(user.isDeleted());
        user.setStatus(UserStatus.DELETED);
        assertTrue(user.isDeleted());
    }

    @Test
    public void testCanSellSuccess() {
        user.setStatus(UserStatus.ACTIVE);
        user.setVerificationStatus(VerificationStatus.APPROVED);
        assertTrue(user.canSell(true));
    }

    @Test
    public void testCanSellFailsWhenNotVerified() {
        user.setStatus(UserStatus.ACTIVE);
        user.setVerificationStatus(VerificationStatus.NONE);
        assertFalse(user.canSell(true));
    }

    @Test
    public void testCanSellFailsWhenEmailNotVerified() {
        user.setStatus(UserStatus.ACTIVE);
        user.setVerificationStatus(VerificationStatus.APPROVED);
        assertFalse(user.canSell(false));
    }

    @Test
    public void testCanSellFailsWhenLocked() {
        user.setStatus(UserStatus.PERM_LOCKED);
        user.setVerificationStatus(VerificationStatus.APPROVED);
        assertFalse(user.canSell(true));
    }

    @Test
    public void testCanBuyMatchesCanSell() {
        user.setStatus(UserStatus.ACTIVE);
        user.setVerificationStatus(VerificationStatus.APPROVED);
        assertTrue(user.canBuy(true));
        assertFalse(user.canBuy(false));
    }

    @Test
    public void testRatingAverageAndApplyRating() {
        assertEquals(0.0, user.ratingAverage(), 0.001);
        user.applyRating(4);
        user.applyRating(5);
        assertEquals(9, user.getRatingSum());
        assertEquals(2, user.getRatingCount());
        assertEquals(4.5, user.ratingAverage(), 0.001);
    }

    @Test
    public void testIsActiveSince() {
        long now = System.currentTimeMillis();
        user.setLastActiveAt(new Timestamp(new Date(now)));
        Timestamp fiveMinutesAgo = new Timestamp(new Date(now - 300000L));
        assertTrue(user.isActiveSince(fiveMinutesAgo));

        Timestamp tenMinutesLater = new Timestamp(new Date(now + 600000L));
        assertFalse(user.isActiveSince(tenMinutesLater));
    }

    @Test
    public void testBuildSearchKeywords() {
        user.setDisplayName("Nguyễn Thùy Ngân");
        user.buildSearchKeywords();
        List<String> keywords = user.getSearchKeywords();
        assertTrue(keywords.contains("nguyen"));
        assertTrue(keywords.contains("thuy"));
        assertTrue(keywords.contains("ngan"));
    }

    @Test
    public void testLastReviewIdStartsEmptyNotNull() {
        assertEquals("", new User().getLastReviewId());
    }

    @Test
    public void testLastReviewIdKeepsTheReviewId() {
        user.setLastReviewId("review-1");
        assertEquals("review-1", user.getLastReviewId());
    }
}
