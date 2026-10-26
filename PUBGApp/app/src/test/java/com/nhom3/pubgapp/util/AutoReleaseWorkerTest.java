package com.nhom3.pubgapp.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

// Unit tests for AutoReleaseWorker guard conditions.
public class AutoReleaseWorkerTest {

    @Test
    public void shouldRun_requiresBothSignedInUserAndOnlineNetwork() {
        assertTrue(AutoReleaseWorker.shouldRun(true, true));
        assertFalse(AutoReleaseWorker.shouldRun(false, true));
        assertFalse(AutoReleaseWorker.shouldRun(true, false));
        assertFalse(AutoReleaseWorker.shouldRun(false, false));
    }

    @Test
    public void workName_matchesExpectedUniquePeriodicWorkName() {
        assertEquals("auto_release_deals", AutoReleaseWorker.WORK_NAME);
    }
}
