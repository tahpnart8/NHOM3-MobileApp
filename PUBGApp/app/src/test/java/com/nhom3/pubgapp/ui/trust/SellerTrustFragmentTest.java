package com.nhom3.pubgapp.ui.trust;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

public class SellerTrustFragmentTest {

    @Test
    public void joinedDateShowsMonthAndYear() {
        assertEquals("10/2026", SellerTrustFragment.formatJoinedMonth(dateOf(2026, Calendar.OCTOBER)));
    }

    @Test
    public void singleDigitMonthKeepsLeadingZero() {
        assertEquals("03/2025", SellerTrustFragment.formatJoinedMonth(dateOf(2025, Calendar.MARCH)));
    }

    // Mid-month at noon in the default time zone, so the formatter never lands on a neighbour month.
    private static Date dateOf(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month, 15, 12, 0);
        return calendar.getTime();
    }
}
