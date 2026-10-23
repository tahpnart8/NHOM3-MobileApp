package com.nhom3.pubgapp;

import com.nhom3.pubgapp.model.MediaImage;
import com.nhom3.pubgapp.model.enums.ImagePurpose;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class MediaImageTest {

    @Test
    public void testToBytesWithValidBase64() {
        byte[] original = "test image payload".getBytes(StandardCharsets.UTF_8);
        String base64 = Base64.getEncoder().encodeToString(original);

        MediaImage image = new MediaImage();
        image.setData(base64);
        image.setPurpose(ImagePurpose.LISTING);

        byte[] decoded = image.toBytes();
        assertArrayEquals(original, decoded);
    }

    @Test
    public void testToBytesWithEmptyData() {
        MediaImage image = new MediaImage();
        image.setData("");
        assertEquals(0, image.toBytes().length);

        image.setData(null);
        assertEquals(0, image.toBytes().length);
    }

    @Test
    public void testToBytesWithInvalidBase64() {
        MediaImage image = new MediaImage();
        image.setData("not_valid_base64!!!");
        assertEquals(0, image.toBytes().length);
    }
}
