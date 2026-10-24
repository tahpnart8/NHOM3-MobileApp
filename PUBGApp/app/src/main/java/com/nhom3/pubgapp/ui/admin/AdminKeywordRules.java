package com.nhom3.pubgapp.ui.admin;

import androidx.annotation.Nullable;

// Pure check of the banned keyword box in E04. The repository normalizes and the Rules check again.
public final class AdminKeywordRules {

    static final int MIN_LENGTH = 2;
    static final int MAX_LENGTH = 60;

    private AdminKeywordRules() {
    }

    // Two to sixty characters after trimming, each a letter (accents allowed), a digit or a space.
    public static boolean isValidKeyword(@Nullable String raw) {
        if (raw == null) {
            return false;
        }
        String trimmed = raw.trim();
        if (trimmed.length() < MIN_LENGTH || trimmed.length() > MAX_LENGTH) {
            return false;
        }
        for (int index = 0; index < trimmed.length(); index++) {
            char letter = trimmed.charAt(index);
            if (!Character.isLetterOrDigit(letter) && !Character.isWhitespace(letter)) {
                return false;
            }
        }
        return true;
    }
}
