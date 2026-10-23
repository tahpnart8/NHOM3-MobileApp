package com.nhom3.pubgapp.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertView(RecentViewEntity view);

    @Query("SELECT * FROM recent_view ORDER BY viewedAtMillis DESC LIMIT :limit")
    List<RecentViewEntity> getRecentViews(int limit);

    // The keyword is the primary key, so searching the same word again replaces its row.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSearch(RecentSearchEntity search);

    @Query("SELECT * FROM recent_search ORDER BY searchedAtMillis DESC LIMIT :limit")
    List<RecentSearchEntity> getRecentSearches(int limit);

    @Query("DELETE FROM recent_view WHERE listingId NOT IN "
            + "(SELECT listingId FROM recent_view ORDER BY viewedAtMillis DESC LIMIT :keep)")
    void trimViews(int keep);

    @Query("DELETE FROM recent_search WHERE keyword NOT IN "
            + "(SELECT keyword FROM recent_search ORDER BY searchedAtMillis DESC LIMIT :keep)")
    void trimSearches(int keep);

    // A @Query holds one statement, so the two deletes sit in helpers that clearAll runs together.
    @Query("DELETE FROM recent_view")
    void clearViews();

    @Query("DELETE FROM recent_search")
    void clearSearches();

    @Transaction
    default void clearAll() {
        clearViews();
        clearSearches();
    }
}
