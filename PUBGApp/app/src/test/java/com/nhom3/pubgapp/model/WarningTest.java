package com.nhom3.pubgapp.model;

import static org.junit.Assert.assertEquals;

import com.nhom3.pubgapp.model.enums.UserStatus;

import org.junit.Test;

public class WarningTest {

    private Warning warningOfLevel(int level) {
        Warning warning = new Warning();
        warning.setLevel(level);
        return warning;
    }

    @Test
    public void levelOneOnlyWarns() {
        assertEquals(UserStatus.ACTIVE, warningOfLevel(1).resultingStatus());
    }

    @Test
    public void levelTwoLocksTemporarily() {
        assertEquals(UserStatus.TEMP_LOCKED, warningOfLevel(2).resultingStatus());
    }

    @Test
    public void levelThreeLocksPermanently() {
        assertEquals(UserStatus.PERM_LOCKED, warningOfLevel(3).resultingStatus());
    }

    @Test
    public void levelAboveThreeStaysPermanentlyLocked() {
        assertEquals(UserStatus.PERM_LOCKED, warningOfLevel(5).resultingStatus());
    }

    @Test
    public void levelBelowOneDoesNotLock() {
        assertEquals(UserStatus.ACTIVE, warningOfLevel(0).resultingStatus());
    }
}
