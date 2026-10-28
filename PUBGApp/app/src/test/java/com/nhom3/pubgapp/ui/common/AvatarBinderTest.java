package com.nhom3.pubgapp.ui.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

// Covers the part of AvatarBinder that runs without Android: which letter the avatar shows.
public class AvatarBinderTest {

    @Test
    public void initialOf_null_isEmpty() {
        assertEquals("", AvatarBinder.initialOf(null));
    }

    @Test
    public void initialOf_blank_isEmpty() {
        assertEquals("", AvatarBinder.initialOf("   "));
    }

    @Test
    public void initialOf_leadingSpaces_usesFirstLetter() {
        assertEquals("N", AvatarBinder.initialOf("  nguyen van a"));
    }

    @Test
    public void initialOf_vietnameseLetter_isUpperCased() {
        assertEquals("Đ", AvatarBinder.initialOf("đặng"));
    }

    @Test
    public void initialOf_letterOutsideBasicPlane_keepsWholeCharacter() {
        // A surrogate pair cut in half would draw a broken glyph.
        assertEquals("𝐀", AvatarBinder.initialOf("𝐀bc"));
    }
}
