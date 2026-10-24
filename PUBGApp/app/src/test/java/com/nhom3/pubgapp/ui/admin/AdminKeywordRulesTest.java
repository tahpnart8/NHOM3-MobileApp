package com.nhom3.pubgapp.ui.admin;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AdminKeywordRulesTest {

    @Test
    public void nullAndEmptyAreInvalid() {
        assertFalse(AdminKeywordRules.isValidKeyword(null));
        assertFalse(AdminKeywordRules.isValidKeyword(""));
        assertFalse(AdminKeywordRules.isValidKeyword("    "));
    }

    @Test
    public void oneCharacterIsTooShort() {
        assertFalse(AdminKeywordRules.isValidKeyword("a"));
        assertFalse(AdminKeywordRules.isValidKeyword("  a  "));
    }

    @Test
    public void twoCharactersIsTheShortestValidKeyword() {
        assertTrue(AdminKeywordRules.isValidKeyword("ab"));
    }

    @Test
    public void sixtyCharactersIsTheLongestValidKeyword() {
        assertTrue(AdminKeywordRules.isValidKeyword(repeat('a', 60)));
        assertFalse(AdminKeywordRules.isValidKeyword(repeat('a', 61)));
    }

    @Test
    public void spacesAroundTheWordDoNotCountTowardLength() {
        assertTrue(AdminKeywordRules.isValidKeyword("  " + repeat('a', 60) + "  "));
    }

    @Test
    public void accentedLettersAreValid() {
        assertTrue(AdminKeywordRules.isValidKeyword("Hàng Giả"));
        assertTrue(AdminKeywordRules.isValidKeyword("đồ lừa đảo"));
    }

    @Test
    public void digitsAreValid() {
        assertTrue(AdminKeywordRules.isValidKeyword("iphone 15"));
        assertTrue(AdminKeywordRules.isValidKeyword("99"));
    }

    @Test
    public void symbolsAreInvalid() {
        assertFalse(AdminKeywordRules.isValidKeyword("ab@"));
        assertFalse(AdminKeywordRules.isValidKeyword("100%"));
        assertFalse(AdminKeywordRules.isValidKeyword("hang-gia"));
    }

    @Test
    public void spaceBetweenWordsIsValid() {
        assertTrue(AdminKeywordRules.isValidKeyword("hang gia"));
    }

    private String repeat(char letter, int count) {
        StringBuilder text = new StringBuilder();
        for (int index = 0; index < count; index++) {
            text.append(letter);
        }
        return text.toString();
    }
}
