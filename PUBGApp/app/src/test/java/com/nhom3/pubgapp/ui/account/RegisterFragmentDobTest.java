package com.nhom3.pubgapp.ui.account;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Date;

// Checks the rule that a date of birth cannot lie in the future.
public class RegisterFragmentDobTest {

    @Test
    public void isFutureDate_dayAfterNow_isTrue() {
        Date now = new Date(1_000_000_000_000L);
        Date tomorrow = new Date(now.getTime() + 86_400_000L);
        assertTrue(RegisterFragment.isFutureDate(tomorrow, now));
    }

    @Test
    public void isFutureDate_pastOrNow_isFalse() {
        Date now = new Date(1_000_000_000_000L);
        assertFalse(RegisterFragment.isFutureDate(new Date(now.getTime() - 1L), now));
        assertFalse(RegisterFragment.isFutureDate(now, now));
    }
}
