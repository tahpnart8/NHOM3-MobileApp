package com.nhom3.pubgapp.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DealDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<DealEntity> deals);

    @Query("SELECT * FROM deal_cache WHERE buyerId = :uid OR sellerId = :uid ORDER BY updatedAtMillis DESC")
    List<DealEntity> getMyDeals(String uid);

    @Query("DELETE FROM deal_cache")
    void clearAll();
}
