package com.nhom3.pubgapp.data.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.firebase.Timestamp;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Round-trip unit tests for Room TypeConverters.
public class ConvertersTest {

    private final Converters converters = new Converters();

    @Test
    public void fromStringList_handlesNullAndEmpty() {
        assertNull(converters.fromStringList(null));

        List<String> empty = new ArrayList<String>();
        assertEquals("[]", converters.fromStringList(empty));
    }

    @Test
    public void toStringList_handlesNullAndEmpty() {
        assertNull(converters.toStringList(null));

        List<String> fromEmpty = converters.toStringList("[]");
        assertNotNull(fromEmpty);
        assertTrue(fromEmpty.isEmpty());

        List<String> fromBlank = converters.toStringList("");
        assertNotNull(fromBlank);
        assertTrue(fromBlank.isEmpty());

        List<String> fromInvalid = converters.toStringList("invalid json");
        assertNotNull(fromInvalid);
        assertTrue(fromInvalid.isEmpty());
    }

    @Test
    public void stringList_roundTrip_handlesNormalCommasQuotesAndVietnamese() {
        List<String> original = new ArrayList<String>();
        original.add("Điện tử");
        original.add("Gia dụng, đời sống");
        original.add("Sách \"Kinh tế học\"");
        original.add("Dòng 1\nDòng 2\tTab");

        String json = converters.fromStringList(original);
        assertNotNull(json);

        List<String> roundTrip = converters.toStringList(json);
        assertNotNull(roundTrip);
        assertEquals(original.size(), roundTrip.size());

        for (int i = 0; i < original.size(); i++) {
            assertEquals(original.get(i), roundTrip.get(i));
        }
    }

    @Test
    public void fromTimestamp_handlesNullAndValidDate() {
        assertEquals(0L, converters.fromTimestamp(null));

        long millis = 1700000000000L;
        Timestamp timestamp = new Timestamp(new Date(millis));
        assertEquals(millis, converters.fromTimestamp(timestamp));
    }

    @Test
    public void toTimestamp_handlesZeroNegativeAndValidDate() {
        assertNull(converters.toTimestamp(0L));
        assertNull(converters.toTimestamp(-1000L));

        long millis = 1700000000000L;
        Timestamp result = converters.toTimestamp(millis);
        assertNotNull(result);
        assertEquals(millis, result.toDate().getTime());
    }

    @Test
    public void timestamp_roundTrip_preservesExactMilliseconds() {
        long millis = 1729900000000L;
        Timestamp original = new Timestamp(new Date(millis));

        long converted = converters.fromTimestamp(original);
        assertEquals(millis, converted);

        Timestamp restored = converters.toTimestamp(converted);
        assertNotNull(restored);
        assertEquals(original.toDate().getTime(), restored.toDate().getTime());
    }
}
