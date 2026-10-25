package com.nhom3.pubgapp.data.local;

import androidx.room.TypeConverter;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Converts complex types (string lists, timestamps) to SQLite primitives for Room.
public class Converters {

    @TypeConverter
    public String fromStringList(List<String> list) {
        if (list == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            String item = list.get(i);
            if (item == null) {
                sb.append("null");
            } else {
                sb.append("\"");
                for (int j = 0; j < item.length(); j++) {
                    char c = item.charAt(j);
                    if (c == '"') {
                        sb.append("\\\"");
                    } else if (c == '\\') {
                        sb.append("\\\\");
                    } else if (c == '\n') {
                        sb.append("\\n");
                    } else if (c == '\r') {
                        sb.append("\\r");
                    } else if (c == '\t') {
                        sb.append("\\t");
                    } else {
                        sb.append(c);
                    }
                }
                sb.append("\"");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @TypeConverter
    public List<String> toStringList(String json) {
        if (json == null) {
            return null;
        }
        List<String> list = new ArrayList<String>();
        String trimmed = json.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            return list;
        }
        trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        if (trimmed.isEmpty()) {
            return list;
        }

        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        boolean isEscaped = false;

        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (isEscaped) {
                if (c == 'n') {
                    current.append('\n');
                } else if (c == 'r') {
                    current.append('\r');
                } else if (c == 't') {
                    current.append('\t');
                } else {
                    current.append(c);
                }
                isEscaped = false;
            } else if (c == '\\') {
                isEscaped = true;
            } else if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                list.add(current.toString());
                current.setLength(0);
            } else {
                if (inQuotes) {
                    current.append(c);
                }
            }
        }
        list.add(current.toString());
        return list;
    }

    @TypeConverter
    public long fromTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return 0L;
        }
        return timestamp.toDate().getTime();
    }

    @TypeConverter
    public Timestamp toTimestamp(long millis) {
        if (millis <= 0L) {
            return null;
        }
        return new Timestamp(new Date(millis));
    }
}
