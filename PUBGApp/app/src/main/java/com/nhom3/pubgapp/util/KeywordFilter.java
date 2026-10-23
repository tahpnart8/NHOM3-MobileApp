package com.nhom3.pubgapp.util;

import com.nhom3.pubgapp.model.BannedKeyword;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/** Matches text against banned keywords so listings can be flagged. */
public class KeywordFilter {

    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}+");

    private List<BannedKeyword> keywords;

    public void setKeywords(List<BannedKeyword> keywords) {
        this.keywords = keywords;
    }

    public boolean containsBanned(String text) {
        if (text == null || keywords == null) {
            return false;
        }
        String normalizedText = normalize(text);
        for (BannedKeyword keyword : keywords) {
            if (keyword.matches(normalizedText)) {
                return true;
            }
        }
        return false;
    }

    public List<String> findMatches(String text) {
        List<String> matches = new ArrayList<String>();
        if (text == null || keywords == null) {
            return matches;
        }
        String normalizedText = normalize(text);
        for (BannedKeyword keyword : keywords) {
            String word = keyword.getKeyword();
            if (keyword.matches(normalizedText) && !matches.contains(word)) {
                matches.add(word);
            }
        }
        return matches;
    }

    // Own copy of the accent stripping: FormatUtils is another module's file and not filled yet.
    private String normalize(String text) {
        String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD);
        String withoutMarks = COMBINING_MARKS.matcher(decomposed).replaceAll("");
        String lower = withoutMarks.toLowerCase(Locale.ROOT);
        return lower.replace('đ', 'd');
    }
}
