package com.nhom3.pubgapp;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.util.FormatUtils;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FormatUtilsTest {

    private FormatUtils formatUtils;

    @Before
    public void setUp() {
        formatUtils = new FormatUtils();
    }

    @Test
    public void testFormatPrice() {
        assertEquals("Miễn phí", formatUtils.formatPrice(0));
        assertEquals("Miễn phí", formatUtils.formatPrice(-100));

        String formatted = formatUtils.formatPrice(1500000L);
        assertTrue(formatted.contains("1.500.000"));
        assertTrue(formatted.endsWith("đ"));
    }

    @Test
    public void testRemoveAccents() {
        assertEquals("", formatUtils.removeAccents(null));
        assertEquals("dien thoai", formatUtils.removeAccents("Điện thoại"));
        assertEquals("dong ho thong minh", formatUtils.removeAccents("Đồng hồ thông minh"));
        assertEquals("may anh canon", formatUtils.removeAccents("Máy ảnh Canon"));
    }

    @Test
    public void testToKeywords() {
        List<String> keywords = formatUtils.toKeywords("Điện thoại iPhone 12, điện thoại!");
        assertEquals(4, keywords.size());
        assertEquals("dien", keywords.get(0));
        assertEquals("thoai", keywords.get(1));
        assertEquals("iphone", keywords.get(2));
        assertEquals("12", keywords.get(3));
    }

    @Test
    public void testFormatTime() {
        assertEquals("", formatUtils.formatTime(null));

        Timestamp now = Timestamp.now();
        assertEquals("Vừa xong", formatUtils.formatTime(now));

        long tenMinutesAgo = System.currentTimeMillis() - 10 * 60 * 1000;
        Timestamp pastTime = new Timestamp(new Date(tenMinutesAgo));
        assertEquals("10 phút trước", formatUtils.formatTime(pastTime));
    }
}
