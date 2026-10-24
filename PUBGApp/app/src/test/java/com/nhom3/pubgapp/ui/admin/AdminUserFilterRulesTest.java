package com.nhom3.pubgapp.ui.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.VerificationStatus;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AdminUserFilterRulesTest {

    @Test
    public void maskPhoneKeepsLastThreeDigits() {
        assertEquals("*******567", AdminUserFilterRules.maskPhone("0901234567"));
    }

    @Test
    public void maskPhoneKeepsSeparatorsAndMasksOnlyDigits() {
        assertEquals("+** *** ***789", AdminUserFilterRules.maskPhone("+84 123 456789"));
    }

    @Test
    public void maskPhoneWithThreeDigitsShowsAllDigits() {
        assertEquals("123", AdminUserFilterRules.maskPhone("123"));
    }

    @Test
    public void maskPhoneShorterThanThreeDigitsIsUnchanged() {
        assertEquals("12", AdminUserFilterRules.maskPhone("12"));
    }

    @Test
    public void maskPhoneEmptyAndNullGiveEmptyText() {
        assertEquals("", AdminUserFilterRules.maskPhone(""));
        assertEquals("", AdminUserFilterRules.maskPhone(null));
    }

    @Test
    public void endOfDayOnLastDayOfMonthStaysOnThatDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2026, Calendar.JANUARY, 31, 8, 15, 0);
        Date end = AdminUserFilterRules.endOfDay(calendar.getTime());

        Calendar result = Calendar.getInstance();
        result.setTime(end);
        assertEquals(2026, result.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, result.get(Calendar.MONTH));
        assertEquals(31, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(23, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(59, result.get(Calendar.MINUTE));
        assertEquals(59, result.get(Calendar.SECOND));
        assertEquals(999, result.get(Calendar.MILLISECOND));
    }

    @Test
    public void startOfDayIsMidnightOfThatDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2026, Calendar.FEBRUARY, 28, 23, 59, 30);
        Date start = AdminUserFilterRules.startOfDay(calendar.getTime());

        Calendar result = Calendar.getInstance();
        result.setTime(start);
        assertEquals(28, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, result.get(Calendar.MINUTE));
        assertEquals(0, result.get(Calendar.SECOND));
        assertEquals(0, result.get(Calendar.MILLISECOND));
    }

    @Test
    public void endOfDayIsAfterStartOfSameDayByAlmostOneDay() {
        Date now = new Date();
        long span = AdminUserFilterRules.endOfDay(now).getTime()
                - AdminUserFilterRules.startOfDay(now).getTime();
        assertTrue(span >= 23L * 60 * 60 * 1000);
    }

    @Test
    public void formatDateUsesDayMonthYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2026, Calendar.MARCH, 5);
        assertEquals("05/03/2026", AdminUserFilterRules.formatDate(calendar.getTime()));
        assertEquals("", AdminUserFilterRules.formatDate(null));
    }

    @Test
    public void keepPendingVerificationDropsEveryOtherStatus() {
        User pending = userWith(VerificationStatus.PENDING);
        User approved = userWith(VerificationStatus.APPROVED);
        User none = userWith(VerificationStatus.NONE);
        User unset = userWith(null);
        List<User> all = new ArrayList<User>();
        all.add(approved);
        all.add(pending);
        all.add(none);
        all.add(unset);

        List<User> result = AdminUserFilterRules.keepPendingVerification(all);

        assertEquals(1, result.size());
        assertSame(pending, result.get(0));
    }

    @Test
    public void keepPendingVerificationOfNullIsEmpty() {
        assertTrue(AdminUserFilterRules.keepPendingVerification(null).isEmpty());
    }

    private User userWith(VerificationStatus status) {
        User user = new User();
        user.setVerificationStatus(status);
        return user;
    }
}
