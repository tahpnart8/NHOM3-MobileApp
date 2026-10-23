package com.nhom3.pubgapp.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ListingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<ListingEntity> listings);

    @Query("SELECT * FROM listing_cache WHERE status = 'AVAILABLE' ORDER BY createdAtMillis DESC")
    List<ListingEntity> getFeed();

    @Query("SELECT * FROM listing_cache WHERE id = :id")
    ListingEntity getById(String id);

    // Listings that were viewed stay cached, so the history screen still opens offline.
    @Query("DELETE FROM listing_cache WHERE id NOT IN "
            + "(SELECT id FROM listing_cache ORDER BY cachedAtMillis DESC LIMIT :keep) "
            + "AND id NOT IN (SELECT listingId FROM recent_view)")
    void trim(int keep);

    @Query("DELETE FROM listing_cache")
    void clearAll();
}
