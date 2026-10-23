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

    public boolean matches(String normalizedText) {
        throw new UnsupportedOperationException("NOT_READY");
    }
}
