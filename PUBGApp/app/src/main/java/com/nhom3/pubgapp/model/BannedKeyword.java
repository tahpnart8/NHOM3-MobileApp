package com.nhom3.pubgapp.model;

public class BannedKeyword extends BaseDocument {
    private String keyword;
    private String createdBy;

    public BannedKeyword() {
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    // The caller normalizes the text; the keyword is stored normalized already.
    public boolean matches(String normalizedText) {
        if (normalizedText == null || keyword == null) {
            return false;
        }
        String needle = keyword.trim();
        if (needle.isEmpty()) {
            return false;
        }
        return normalizedText.contains(needle);
    }
}
