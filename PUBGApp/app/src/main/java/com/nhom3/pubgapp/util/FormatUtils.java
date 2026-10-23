package com.nhom3.pubgapp.util;

import com.google.firebase.Timestamp;

import java.util.List;

/** Formats price and time, and strips Vietnamese accents for search. */
public class FormatUtils {

    public String formatPrice(long price) {
        if (price <= 0) {
            return "Miễn phí";
        }
        java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols(java.util.Locale.getDefault());
        symbols.setGroupingSeparator('.');
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###", symbols);
        return df.format(price) + " đ";
    }

    public String formatTime(Timestamp time) {
        if (time == null) {
            return "";
        }
        long diffMs = System.currentTimeMillis() - time.toDate().getTime();
        if (diffMs < 60 * 1000) {
            return "Vừa xong";
        }
        if (diffMs < 60 * 60 * 1000) {
            long minutes = diffMs / (60 * 1000);
            return minutes + " phút trước";
        }
        if (diffMs < 24 * 60 * 60 * 1000) {
            long hours = diffMs / (60 * 60 * 1000);
            return hours + " giờ trước";
        }
        if (diffMs < 30L * 24 * 60 * 60 * 1000) {
            long days = diffMs / (24L * 60 * 60 * 1000);
            return days + " ngày trước";
        }
        if (diffMs < 365L * 24 * 60 * 60 * 1000) {
            long months = diffMs / (30L * 24 * 60 * 60 * 1000);
            return months + " tháng trước";
        }
        long years = diffMs / (365L * 24 * 60 * 60 * 1000);
        return years + " năm trước";
    }

    public String removeAccents(String text) {
        if (text == null) {
            return "";
        }
        String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String stripped = pattern.matcher(normalized).replaceAll("");
        stripped = stripped.replace('đ', 'd').replace('Đ', 'd');
        return stripped.toLowerCase(java.util.Locale.ROOT).trim();
    }

    public List<String> toKeywords(String text) {
        List<String> result = new java.util.ArrayList<String>();
        if (text == null || text.trim().isEmpty()) {
            return result;
        }
        String cleaned = removeAccents(text);
        String[] words = cleaned.split("[^a-zA-Z0-9]+");
        java.util.Set<String> seen = new java.util.LinkedHashSet<String>();
        for (int i = 0; i < words.length; i++) {
            String word = words[i].trim();
            if (!word.isEmpty() && !seen.contains(word)) {
                seen.add(word);
                result.add(word);
            }
        }
        return result;
    }
}
