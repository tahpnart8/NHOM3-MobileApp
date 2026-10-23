package com.nhom3.pubgapp.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(
        entities = {
                ListingEntity.class,
                ConversationEntity.class,
                MessageEntity.class,
                DealEntity.class,
                RecentViewEntity.class,
                RecentSearchEntity.class
        },
        version = 1,
        exportSchema = false)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "pubgapp.db";

    // Not final: the singleton is created on first use. Volatile so every thread sees it.
    private static volatile AppDatabase INSTANCE;

    public abstract ListingDao listingDao();

    public abstract ChatDao chatDao();

    public abstract DealDao dealDao();

    public abstract HistoryDao historyDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        synchronized (AppDatabase.class) {
            if (INSTANCE == null) {
                Context appContext = context.getApplicationContext();
                INSTANCE = Room.databaseBuilder(appContext, AppDatabase.class, DB_NAME).build();
            }
        }
        return INSTANCE;
    }
}
