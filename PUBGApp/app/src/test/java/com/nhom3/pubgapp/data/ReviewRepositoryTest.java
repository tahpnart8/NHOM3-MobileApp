package com.nhom3.pubgapp.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

// Tests ReviewRepository pure validation logic without network.
public class ReviewRepositoryTest {

    @Test
    public void isValidComment_allowsNullAndEmpty() {
        assertTrue(ReviewRepository.isValidComment(null));
        assertTrue(ReviewRepository.isValidComment(""));
        assertTrue(ReviewRepository.isValidComment("   "));
    }

    @Test
    public void isValidComment_enforcesMax500Chars() {
        StringBuilder sb500 = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            sb500.append('a');
        }
        assertTrue(ReviewRepository.isValidComment(sb500.toString()));

        StringBuilder sb501 = new StringBuilder();
        for (int i = 0; i < 501; i++) {
            sb501.append('a');
        }
        assertFalse(ReviewRepository.isValidComment(sb501.toString()));
    }
}
