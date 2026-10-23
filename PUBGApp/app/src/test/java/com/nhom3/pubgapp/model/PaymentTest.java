package com.nhom3.pubgapp.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.enums.PaymentStatus;

import org.junit.Test;

public class PaymentTest {

    @Test
    public void testRelease() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.HELD);
        assertTrue(payment.isHeld());

        payment.release();
        assertEquals(PaymentStatus.RELEASED, payment.getStatus());
        assertNotNull(payment.getReleasedAt());
    }

    @Test
    public void testRefund() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.HELD);

        payment.refund();
        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
        assertNotNull(payment.getReleasedAt());
    }
}
