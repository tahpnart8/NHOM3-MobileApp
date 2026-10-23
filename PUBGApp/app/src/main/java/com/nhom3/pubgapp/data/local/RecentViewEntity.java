package com.nhom3.pubgapp.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Viewing history on this device only; feeds the For you section.
@Entity(tableName = "recent_view")
public class RecentViewEntity {

    @PrimaryKey
    @NonNull
    private String listingId = "";

    private String categoryId;

    private long viewedAtMillis;

    // Room needs a public constructor it can call, then fills the fields through the setters.
    public RecentViewEntity() {
    }

    @NonNull
    public String getListingId() {
        return listingId;
    }

    public void setListingId(@NonNull String listingId) {
        this.listingId = listingId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public long getViewedAtMillis() {
        return viewedAtMillis;
    }

    public void setViewedAtMillis(long viewedAtMillis) {
        this.viewedAtMillis = viewedAtMillis;
    }
}
