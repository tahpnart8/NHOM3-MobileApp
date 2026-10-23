package com.nhom3.pubgapp.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Search history on this device only; feeds the For you section.
@Entity(tableName = "recent_search")
public class RecentSearchEntity {

    @PrimaryKey
    @NonNull
    private String keyword = "";

    private long searchedAtMillis;

    // Room needs a public constructor it can call, then fills the fields through the setters.
    public RecentSearchEntity() {
    }

    @NonNull
    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(@NonNull String keyword) {
        this.keyword = keyword;
    }

    public long getSearchedAtMillis() {
        return searchedAtMillis;
    }

    public void setSearchedAtMillis(long searchedAtMillis) {
        this.searchedAtMillis = searchedAtMillis;
    }
}
