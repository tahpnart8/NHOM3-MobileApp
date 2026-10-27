package com.nhom3.pubgapp.ui.trust;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ReviewAdapterTest {

    private static final String FALLBACK = "Người dùng";

    @Test
    public void knownNameIsShown() {
        assertEquals("Nguyễn Văn A", ReviewAdapter.resolveName("Nguyễn Văn A", FALLBACK));
    }

    @Test
    public void nameIsTrimmed() {
        assertEquals("Lê Thị B", ReviewAdapter.resolveName("  Lê Thị B ", FALLBACK));
    }

    @Test
    public void unknownNameKeepsFallback() {
        assertEquals(FALLBACK, ReviewAdapter.resolveName(null, FALLBACK));
    }

    @Test
    public void blankNameKeepsFallback() {
        assertEquals(FALLBACK, ReviewAdapter.resolveName("   ", FALLBACK));
    }
}
