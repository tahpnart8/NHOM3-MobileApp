package com.nhom3.pubgapp.util;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.ListingStatus;

import com.nhom3.pubgapp.model.enums.ItemCondition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** On-device scoring for similar listings, the For You feed and the price range. */
public class RecommendationEngine {

    private static class ScoredListing {
        final Listing listing;
        final double score;

        ScoredListing(Listing listing, double score) {
            this.listing = listing;
            this.score = score;
        }
    }

    public List<Listing> similarTo(Listing listing, List<Listing> candidates) {
        if (listing == null || candidates == null || candidates.isEmpty()) {
            return new ArrayList<Listing>();
        }
        String targetId = listing.getId();
        String targetCatId = listing.getCategoryId();
        List<ScoredListing> scoredList = new ArrayList<ScoredListing>();

        for (int i = 0; i < candidates.size(); i++) {
            Listing c = candidates.get(i);
            if (c == null) {
                continue;
            }
            if (targetId != null && targetId.equals(c.getId())) {
                continue;
            }
            if (c.getStatus() != ListingStatus.AVAILABLE) {
                continue;
            }
            if (targetCatId != null && !targetCatId.equals(c.getCategoryId())) {
                continue;
            }
            double itemScore = scoreSimilar(listing, c);
            scoredList.add(new ScoredListing(c, itemScore));
        }

        Collections.sort(scoredList, new Comparator<ScoredListing>() {
            @Override
            public int compare(ScoredListing a, ScoredListing b) {
                int scoreComp = Double.compare(b.score, a.score);
                if (scoreComp != 0) {
                    return scoreComp;
                }
                Timestamp timeA = a.listing.getCreatedAt();
                Timestamp timeB = b.listing.getCreatedAt();
                if (timeA != null && timeB != null) {
                    return timeB.compareTo(timeA);
                }
                if (timeA != null) {
                    return -1;
                }
                if (timeB != null) {
                    return 1;
                }
                return 0;
            }
        });

        int limit = Math.min(10, scoredList.size());
        List<Listing> result = new ArrayList<Listing>();
        for (int i = 0; i < limit; i++) {
            result.add(scoredList.get(i).listing);
        }
        return result;
    }

    private double scoreSimilar(Listing target, Listing candidate) {
        double score = 1.0;
        if (target.getItemCondition() != null && target.getItemCondition() == candidate.getItemCondition()) {
            score += 0.5;
        }
        if (target.getProvince() != null && !target.getProvince().isEmpty()
                && target.getProvince().equals(candidate.getProvince())) {
            score += 0.3;
        }

        long targetPrice = target.getPrice();
        long candidatePrice = candidate.getPrice();
        double doganGia = 0.0;
        if (targetPrice > 0L && candidatePrice > 0L) {
            long diff = Math.abs(candidatePrice - targetPrice);
            long denom = Math.max(targetPrice, 1L);
            double ratio = (double) diff / (double) denom;
            doganGia = 1.0 - Math.min(1.0, ratio);
        }
        score += 1.0 * doganGia;

        double jaccard = calculateJaccard(target.getSearchKeywords(), candidate.getSearchKeywords());
        score += 1.0 * jaccard;
        return score;
    }

    private double calculateJaccard(List<String> targetKeywords, List<String> candidateKeywords) {
        Set<String> setTarget = new HashSet<String>();
        if (targetKeywords != null) {
            for (int i = 0; i < targetKeywords.size(); i++) {
                String kw = targetKeywords.get(i);
                if (kw != null && !kw.trim().isEmpty()) {
                    setTarget.add(kw.trim().toLowerCase());
                }
            }
        }
        Set<String> setCandidate = new HashSet<String>();
        if (candidateKeywords != null) {
            for (int i = 0; i < candidateKeywords.size(); i++) {
                String kw = candidateKeywords.get(i);
                if (kw != null && !kw.trim().isEmpty()) {
                    setCandidate.add(kw.trim().toLowerCase());
                }
            }
        }
        if (setTarget.isEmpty() && setCandidate.isEmpty()) {
            return 0.0;
        }
        Set<String> union = new HashSet<String>(setTarget);
        union.addAll(setCandidate);
        if (union.isEmpty()) {
            return 0.0;
        }
        int intersectionCount = 0;
        for (String kw : setTarget) {
            if (setCandidate.contains(kw)) {
                intersectionCount++;
            }
        }
        return (double) intersectionCount / (double) union.size();
    }

    public List<Listing> forYou(List<Listing> candidates, List<String> viewedCategoryIds,
            List<String> searchedKeywords, List<String> savedCategoryIds, User user) {
        if (candidates == null || candidates.isEmpty()) {
            return new ArrayList<Listing>();
        }

        String currentUserId = user != null ? user.getId() : null;
        List<ScoredListing> scoredList = new ArrayList<ScoredListing>();
        for (int i = 0; i < candidates.size(); i++) {
            Listing c = candidates.get(i);
            if (c == null) {
                continue;
            }
            if (c.getStatus() != ListingStatus.AVAILABLE) {
                continue;
            }
            if (currentUserId != null && currentUserId.equals(c.getSellerId())) {
                continue;
            }
            double itemScore = score(c, viewedCategoryIds, searchedKeywords, savedCategoryIds, user);
            scoredList.add(new ScoredListing(c, itemScore));
        }

        if (scoredList.isEmpty()) {
            return new ArrayList<Listing>();
        }

        Collections.sort(scoredList, new Comparator<ScoredListing>() {
            @Override
            public int compare(ScoredListing a, ScoredListing b) {
                int cmp = Double.compare(b.score, a.score);
                if (cmp != 0) {
                    return cmp;
                }
                Timestamp timeA = a.listing.getCreatedAt();
                Timestamp timeB = b.listing.getCreatedAt();
                if (timeA != null && timeB != null) {
                    return timeB.compareTo(timeA);
                }
                if (timeA != null) {
                    return -1;
                }
                if (timeB != null) {
                    return 1;
                }
                return 0;
            }
        });

        int limit = Math.min(20, scoredList.size());
        List<Listing> result = new ArrayList<Listing>();
        for (int i = 0; i < limit; i++) {
            result.add(scoredList.get(i).listing);
        }
        return result;
    }

    public long[] priceRange(Listing listing, List<Listing> candidates) {
        if (listing == null || candidates == null || candidates.isEmpty()) {
            return new long[0];
        }
        String targetId = listing.getId();
        String targetCategoryId = listing.getCategoryId();
        ItemCondition targetCondition = listing.getItemCondition();

        List<Long> p1 = new ArrayList<Long>();
        List<Long> p2 = new ArrayList<Long>();

        for (int i = 0; i < candidates.size(); i++) {
            Listing c = candidates.get(i);
            if (c == null) {
                continue;
            }
            if (targetId != null && targetId.equals(c.getId())) {
                continue;
            }
            if (c.getStatus() != ListingStatus.AVAILABLE) {
                continue;
            }
            if (targetCategoryId == null || !targetCategoryId.equals(c.getCategoryId())) {
                continue;
            }
            long price = c.getPrice();
            if (price <= 0L) {
                continue;
            }
            if (targetCondition != null && targetCondition == c.getItemCondition()) {
                p1.add(price);
            }
            p2.add(price);
        }

        List<Long> selectedPrices;
        if (p1.size() >= 5) {
            selectedPrices = p1;
        } else if (p2.size() >= 5) {
            selectedPrices = p2;
        } else {
            return new long[0];
        }

        Collections.sort(selectedPrices);
        int n = selectedPrices.size();
        int idx25 = (int) Math.ceil(0.25 * n) - 1;
        if (idx25 < 0) {
            idx25 = 0;
        }
        int idx75 = (int) Math.ceil(0.75 * n) - 1;
        if (idx75 < 0) {
            idx75 = 0;
        }
        if (idx75 >= n) {
            idx75 = n - 1;
        }

        long low = roundToTenThousand(selectedPrices.get(idx25));
        long high = roundToTenThousand(selectedPrices.get(idx75));
        return new long[] { low, high };
    }

    private long roundToTenThousand(long val) {
        if (val <= 0L) {
            return 0L;
        }
        return ((val + 5000L) / 10000L) * 10000L;
    }

    // Chấm điểm tin đăng cho người dùng mặc định khi không truyền ngữ cảnh
    private double score(Listing listing) {
        return score(listing, null, null, null, null);
    }

    double score(Listing listing, List<String> viewedCategoryIds,
            List<String> searchedKeywords, List<String> savedCategoryIds, User user) {
        if (listing == null) {
            return 0.0;
        }

        double categoryViewRatio = 0.0;
        if (viewedCategoryIds != null && !viewedCategoryIds.isEmpty()) {
            int matchCount = 0;
            String catId = listing.getCategoryId();
            for (int i = 0; i < viewedCategoryIds.size(); i++) {
                String v = viewedCategoryIds.get(i);
                if (v != null && v.equals(catId)) {
                    matchCount++;
                }
            }
            categoryViewRatio = (double) matchCount / viewedCategoryIds.size();
        }

        double keywordScore = 0.0;
        if (searchedKeywords != null && !searchedKeywords.isEmpty()
                && listing.getSearchKeywords() != null && !listing.getSearchKeywords().isEmpty()) {
            FormatUtils formatUtils = new FormatUtils();
            List<String> listingKeywords = listing.getSearchKeywords();
            boolean match = false;
            int limit = Math.min(10, searchedKeywords.size());
            for (int i = 0; i < limit; i++) {
                String rawKw = searchedKeywords.get(i);
                if (rawKw == null || rawKw.trim().isEmpty()) {
                    continue;
                }
                String unaccented = formatUtils.removeAccents(rawKw).trim();
                List<String> tokens = formatUtils.toKeywords(rawKw);
                for (int j = 0; j < listingKeywords.size(); j++) {
                    String lk = listingKeywords.get(j);
                    if (lk == null) {
                        continue;
                    }
                    if (lk.equalsIgnoreCase(unaccented)) {
                        match = true;
                        break;
                    }
                    if (tokens != null) {
                        for (int k = 0; k < tokens.size(); k++) {
                            if (lk.equalsIgnoreCase(tokens.get(k))) {
                                match = true;
                                break;
                            }
                        }
                    }
                    if (match) {
                        break;
                    }
                }
                if (match) {
                    keywordScore = 2.0;
                    break;
                }
            }
        }

        double interestedScore = 0.0;
        if (user != null && user.getInterestedCategoryIds() != null
                && listing.getCategoryId() != null
                && user.getInterestedCategoryIds().contains(listing.getCategoryId())) {
            interestedScore = 1.0;
        }

        double savedScore = 0.0;
        if (savedCategoryIds != null && listing.getCategoryId() != null
                && (savedCategoryIds.contains(listing.getCategoryId())
                || (listing.getId() != null && savedCategoryIds.contains(listing.getId())))) {
            savedScore = 0.5;
        }

        double recencyScore = 1.0;
        if (listing.getCreatedAt() != null) {
            long now = System.currentTimeMillis();
            long created = listing.getCreatedAt().toDate().getTime();
            long diffMs = Math.max(0L, now - created);
            double days = (double) diffMs / (1000.0 * 60.0 * 60.0 * 24.0);
            recencyScore = 1.0 / (1.0 + days / 7.0);
        }

        return (3.0 * categoryViewRatio) + keywordScore + interestedScore + savedScore + recencyScore;
    }
}
