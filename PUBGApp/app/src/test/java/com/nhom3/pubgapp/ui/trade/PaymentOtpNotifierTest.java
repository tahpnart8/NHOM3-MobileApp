package com.nhom3.pubgapp.ui.trade;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PaymentOtpNotifierTest {

    @Test
    public void codesHaveSixDigitsWithLeadingZerosKept() {
        Random random = new Random(42L);
        for (int attempt = 0; attempt < 200; attempt++) {
            String code = PaymentOtpNotifier.generateCode(random);
            assertEquals(6, code.length());
            assertTrue(code.matches("[0-9]{6}"));
        }
    }

    @Test
    public void maskPhoneShowsOnlyTheLastThreeDigits() {
        assertEquals("••••678", PaymentOtpNotifier.maskPhone("0912 345 678"));
        assertEquals("••••789", PaymentOtpNotifier.maskPhone("+84 123-456-789"));
        assertNull(PaymentOtpNotifier.maskPhone("12"));
        assertNull(PaymentOtpNotifier.maskPhone(null));
    }
}
