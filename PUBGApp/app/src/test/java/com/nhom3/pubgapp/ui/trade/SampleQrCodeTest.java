package com.nhom3.pubgapp.ui.trade;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SampleQrCodeTest {

    @Test
    public void transferContentUsesTheStartOfTheDealId() {
        assertEquals("PUBGABCDEFGH", SampleQrCode.buildTransferContent("abcdefghijkl"));
        assertEquals("PUBGXY", SampleQrCode.buildTransferContent(" xy "));
        assertEquals("PUBG", SampleQrCode.buildTransferContent(null));
    }

    @Test
    public void modulesHaveTheThreeCornerSquares() {
        boolean[][] modules = SampleQrCode.buildModules("PUBGABCDEFGH");
        int last = SampleQrCode.MODULE_COUNT - 1;
        assertTrue(modules[0][0]);
        assertTrue(modules[3][3]);
        assertFalse(modules[1][1]);
        assertFalse(modules[7][7]);
        assertTrue(modules[0][last]);
        assertTrue(modules[last][0]);
    }

    @Test
    public void sameContentGivesTheSamePicture() {
        boolean[][] first = SampleQrCode.buildModules("PUBG123");
        boolean[][] second = SampleQrCode.buildModules("PUBG123");
        for (int row = 0; row < SampleQrCode.MODULE_COUNT; row++) {
            assertTrue(Arrays.equals(first[row], second[row]));
        }
    }
}
