package com.nhom3.pubgapp.data.local;

import androidx.room.TypeConverter;

import com.google.firebase.Timestamp;

import java.util.List;

public class Converters {

    @TypeConverter
    public String fromStringList(List<String> list) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    @TypeConverter
    public List<String> toStringList(String json) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    @TypeConverter
    public long fromTimestamp(Timestamp timestamp) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    @TypeConverter
    public Timestamp toTimestamp(long millis) {
        throw new UnsupportedOperationException("NOT_READY");
    }
}
