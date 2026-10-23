package com.nhom3.pubgapp.ui.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

// Covers the one piece of ImageLoader that runs without Android: the shrink factor. Everything else needs
// BitmapFactory and a ContentResolver, so it is checked on a device instead.
public class ImageLoaderTest {

    @Test
    public void calculateSampleSize_imageSmallerThanTarget_doesNotShrink() {
        assertEquals(1, ImageLoader.calculateSampleSize(320, 240, 480));
    }

    @Test
    public void calculateSampleSize_imageAtTarget_doesNotShrink() {
        assertEquals(1, ImageLoader.calculateSampleSize(480, 480, 480));
    }

    @Test
    public void calculateSampleSize_imageTwiceTarget_halves() {
        assertEquals(2, ImageLoader.calculateSampleSize(960, 960, 480));
    }

    @Test
    public void calculateSampleSize_fullSizeListingImage_shrinksToPowerOfTwo() {
        // 1024 px is the cap a stored image can have; a 160 px thumbnail slot wants it eight times smaller.
        assertEquals(4, ImageLoader.calculateSampleSize(1024, 1024, 160));
    }

    @Test
    public void calculateSampleSize_narrowImage_keepsShortSideAboveTarget() {
        // The short side decides, so a tall strip is not shrunk past the target width.
        assertEquals(1, ImageLoader.calculateSampleSize(400, 4000, 300));
    }

    @Test
    public void calculateSampleSize_unknownBounds_doesNotShrink() {
        // BitmapFactory reports -1 for data it cannot read; shrinking by a guess would be worse.
        assertEquals(1, ImageLoader.calculateSampleSize(-1, -1, 480));
    }

    @Test
    public void calculateSampleSize_zeroTarget_doesNotLoop() {
        assertEquals(1, ImageLoader.calculateSampleSize(1024, 1024, 0));
    }
}
