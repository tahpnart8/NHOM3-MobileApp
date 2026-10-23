package com.nhom3.pubgapp.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BannedKeywordTest {

    private BannedKeyword keyword(String word) {
        BannedKeyword banned = new BannedKeyword();
        banned.setKeyword(word);
        return banned;
    }

    @Test
    public void matchesWhenTextContainsKeyword() {
        assertTrue(keyword("hang gia").matches("ban hang gia re"));
    }

    @Test
    public void doesNotMatchOtherText() {
        assertFalse(keyword("hang gia").matches("ban hang chinh hang"));
    }

    @Test
    public void emptyKeywordNeverMatches() {
        assertFalse(keyword("").matches("bat ky chu nao"));
        assertFalse(keyword(null).matches("bat ky chu nao"));
    }

    @Test
    public void nullTextNeverMatches() {
        assertFalse(keyword("hang gia").matches(null));
    }
}
