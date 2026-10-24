package com.nhom3.pubgapp.ui.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.enums.UserStatus;

import org.junit.Test;

import java.util.Date;

public class AdminUserActionRulesTest {

    private static final long ONE_DAY_MILLIS = 24L * 60L * 60L * 1000L;

    @Test
    public void warnAndLockAreOnlyForActiveAccounts() {
        for (UserStatus status : UserStatus.values()) {
            boolean expected = status == UserStatus.ACTIVE;
            assertEquals(expected, AdminUserActionRules.canWarn(status));
            assertEquals(expected, AdminUserActionRules.canLock(status));
        }
    }

    @Test
    public void unlockAndRemoveAllAreOnlyForLockedAccounts() {
        for (UserStatus status : UserStatus.values()) {
            boolean expected = status == UserStatus.TEMP_LOCKED || status == UserStatus.PERM_LOCKED;
            assertEquals(expected, AdminUserActionRules.canUnlock(status));
            assertEquals(expected, AdminUserActionRules.canRemoveAllListings(status));
        }
    }

    @Test
    public void deletedAccountHasNoAction() {
        assertFalse(AdminUserActionRules.canWarn(UserStatus.DELETED));
        assertFalse(AdminUserActionRules.canLock(UserStatus.DELETED));
        assertFalse(AdminUserActionRules.canUnlock(UserStatus.DELETED));
        assertFalse(AdminUserActionRules.canRemoveAllListings(UserStatus.DELETED));
    }

    @Test
    public void nullStatusHasNoAction() {
        assertFalse(AdminUserActionRules.canWarn(null));
        assertFalse(AdminUserActionRules.canLock(null));
        assertFalse(AdminUserActionRules.canUnlock(null));
        assertFalse(AdminUserActionRules.canRemoveAllListings(null));
    }

    @Test
    public void onlyTheSecondWarningNeedsALockDuration() {
        assertFalse(AdminUserActionRules.needsLockDuration(0));
        assertTrue(AdminUserActionRules.needsLockDuration(1));
        assertFalse(AdminUserActionRules.needsLockDuration(2));
        assertFalse(AdminUserActionRules.needsLockDuration(3));
        assertFalse(AdminUserActionRules.needsLockDuration(7));
    }

    @Test
    public void nextWarningStatusFollowsTheThreeTiers() {
        assertEquals(UserStatus.ACTIVE, AdminUserActionRules.nextWarningStatus(0));
        assertEquals(UserStatus.TEMP_LOCKED, AdminUserActionRules.nextWarningStatus(1));
        assertEquals(UserStatus.PERM_LOCKED, AdminUserActionRules.nextWarningStatus(2));
        assertEquals(UserStatus.PERM_LOCKED, AdminUserActionRules.nextWarningStatus(7));
    }

    @Test
    public void blankReasonIsInvalid() {
        assertFalse(AdminUserActionRules.isReasonValid(null));
        assertFalse(AdminUserActionRules.isReasonValid(""));
        assertFalse(AdminUserActionRules.isReasonValid("  \n\t "));
    }

    @Test
    public void reasonWithTextIsValid() {
        assertTrue(AdminUserActionRules.isReasonValid("Bán hàng giả"));
        assertTrue(AdminUserActionRules.isReasonValid("  Lừa đảo  "));
    }

    @Test
    public void lockUntilIsNullBelowOneDay() {
        assertNull(AdminUserActionRules.lockUntil(1000L, 0));
        assertNull(AdminUserActionRules.lockUntil(1000L, -1));
    }

    @Test
    public void lockUntilAddsWholeDays() {
        Date oneDay = AdminUserActionRules.lockUntil(1000L, 1);
        Date thirtyDays = AdminUserActionRules.lockUntil(1000L, 30);
        assertNotNull(oneDay);
        assertNotNull(thirtyDays);
        assertEquals(1000L + ONE_DAY_MILLIS, oneDay.getTime());
        assertEquals(1000L + 30L * ONE_DAY_MILLIS, thirtyDays.getTime());
    }

    @Test
    public void parseDaysAcceptsWholeNumbersInRange() {
        assertEquals(1, AdminUserActionRules.parseDays("1"));
        assertEquals(7, AdminUserActionRules.parseDays(" 7 "));
        assertEquals(AdminUserActionRules.MAX_LOCK_DAYS,
                AdminUserActionRules.parseDays(String.valueOf(AdminUserActionRules.MAX_LOCK_DAYS)));
    }

    @Test
    public void parseDaysRejectsEverythingElse() {
        assertEquals(0, AdminUserActionRules.parseDays(null));
        assertEquals(0, AdminUserActionRules.parseDays(""));
        assertEquals(0, AdminUserActionRules.parseDays("0"));
        assertEquals(0, AdminUserActionRules.parseDays("-3"));
        assertEquals(0, AdminUserActionRules.parseDays("2.5"));
        assertEquals(0, AdminUserActionRules.parseDays("abc"));
        assertEquals(0, AdminUserActionRules.parseDays(
                String.valueOf(AdminUserActionRules.MAX_LOCK_DAYS + 1)));
        assertEquals(0, AdminUserActionRules.parseDays("99999999999999"));
    }
}
