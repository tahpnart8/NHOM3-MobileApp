package com.nhom3.pubgapp.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.BannedKeyword;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class KeywordFilterTest {

    private KeywordFilter filter;

    private BannedKeyword keyword(String word) {
        BannedKeyword banned = new BannedKeyword();
        banned.setKeyword(word);
        return banned;
    }

    @Before
    public void setUp() {
        List<BannedKeyword> keywords = new ArrayList<BannedKeyword>();
        keywords.add(keyword("hang gia"));
        keywords.add(keyword("lua dao"));
        filter = new KeywordFilter();
        filter.setKeywords(keywords);
    }

    @Test
    public void textWithBannedWordIsFound() {
        assertTrue(filter.containsBanned("Ban dien thoai hang gia gia re"));
    }

    @Test
    public void cleanTextIsNotFound() {
        assertFalse(filter.containsBanned("Ban dien thoai cu, con bao hanh"));
    }

    @Test
    public void upperCaseTextIsFound() {
        assertTrue(filter.containsBanned("HANG GIA CAO CAP"));
    }

    @Test
    public void accentedTextIsFound() {
        assertTrue(filter.containsBanned("Bán đồ hàng giả"));
    }

    @Test
    public void accentedKeywordWithCapitalDIsFound() {
        List<BannedKeyword> keywords = new ArrayList<BannedKeyword>();
        keywords.add(keyword("dao"));
        filter.setKeywords(keywords);
        assertTrue(filter.containsBanned("Lừa ĐẢO"));
    }

    @Test
    public void nullTextAndMissingKeywordsAreSafe() {
        assertFalse(filter.containsBanned(null));
        assertFalse(new KeywordFilter().containsBanned("hang gia"));
    }

    @Test
    public void findMatchesReturnsEveryMatchOnce() {
        List<String> matches = filter.findMatches("Hàng giả, lừa đảo, hàng giả");
        assertEquals(2, matches.size());
        assertTrue(matches.contains("hang gia"));
        assertTrue(matches.contains("lua dao"));
    }

    @Test
    public void findMatchesIsEmptyForCleanText() {
        assertTrue(filter.findMatches("Ban ghe go").isEmpty());
    }
}
