package com.nhom3.pubgapp.ui.admin;

import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.VerificationStatus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Small rules behind the E02 user list. Pure Java so they run in a unit test.
public final class AdminUserFilterRules {

    private static final int VISIBLE_DIGITS = 3;
    private static final char MASK = '*';
    private static final String DATE_PATTERN = "dd/MM/yyyy";

    private AdminUserFilterRules() {
    }

    // Keeps the last three digits and masks the digits before them; other characters stay.
    public static String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return "";
        }
        int totalDigits = 0;
        for (int index = 0; index < phone.length(); index++) {
            if (Character.isDigit(phone.charAt(index))) {
                totalDigits++;
            }
        }
        int digitsToMask = totalDigits - VISIBLE_DIGITS;
        StringBuilder masked = new StringBuilder();
        int digitIndex = 0;
        for (int index = 0; index < phone.length(); index++) {
            char current = phone.charAt(index);
            if (Character.isDigit(current)) {
                masked.append(digitIndex < digitsToMask ? MASK : current);
                digitIndex++;
            } else {
                masked.append(current);
            }
        }
        return masked.toString();
    }

    // 00:00:00.000 of the given day in the device time zone.
    public static Date startOfDay(Date day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    // 23:59:59.999 of the given day, so the "to" date counts the whole day.
    public static Date endOfDay(Date day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(day);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    public static String formatDate(Date day) {
        if (day == null) {
            return "";
        }
        return new SimpleDateFormat(DATE_PATTERN, Locale.ROOT).format(day);
    }

    // The "Chờ xác thực" switch: filtering happens on the loaded page, not in the query.
    public static List<User> keepPendingVerification(List<User> users) {
        List<User> pending = new ArrayList<User>();
        if (users == null) {
            return pending;
        }
        for (User user : users) {
            if (user.getVerificationStatus() == VerificationStatus.PENDING) {
                pending.add(user);
            }
        }
        return pending;
    }
}
