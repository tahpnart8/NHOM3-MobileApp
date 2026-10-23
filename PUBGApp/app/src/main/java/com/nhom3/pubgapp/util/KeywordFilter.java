package com.nhom3.pubgapp.util;

import com.nhom3.pubgapp.model.BannedKeyword;

import java.util.List;

/** Matches text against banned keywords so listings can be flagged. */
public class KeywordFilter {

    private List<BannedKeyword> keywords;

    public void setKeywords(List<BannedKeyword> keywords) {
        this.keywords = keywords;
    }

    public boolean containsBanned(String text) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public List<String> findMatches(String text) {
        throw new UnsupportedOperationException("NOT_READY");
    }
}
