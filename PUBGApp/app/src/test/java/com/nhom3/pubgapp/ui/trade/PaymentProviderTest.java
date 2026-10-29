package com.nhom3.pubgapp.ui.trade;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PaymentProviderTest {

    private static final String[] ENTRIES = {"VCB|Vietcombank", "TCB|Techcombank", "VDD|Ví di động", "broken"};

    @Test
    public void fromEntriesSkipsMalformedLines() {
        List<PaymentProvider> providers = PaymentProvider.fromEntries(ENTRIES);
        assertEquals(3, providers.size());
        assertEquals("VCB", providers.get(0).getCode());
        assertEquals("Vietcombank", providers.get(0).getName());
    }

    @Test
    public void filterMatchesCodeOrNameIgnoringCaseAndAccents() {
        List<PaymentProvider> providers = PaymentProvider.fromEntries(ENTRIES);
        assertEquals(1, PaymentProvider.filter(providers, "tcb").size());
        assertEquals(2, PaymentProvider.filter(providers, "bank").size());
        assertEquals("VDD", PaymentProvider.filter(providers, "vi di").get(0).getCode());
        assertEquals(0, PaymentProvider.filter(providers, "khong co").size());
    }

    @Test
    public void emptyQueryKeepsEveryProvider() {
        List<PaymentProvider> providers = PaymentProvider.fromEntries(ENTRIES);
        assertEquals(3, PaymentProvider.filter(providers, "  ").size());
        assertEquals(3, PaymentProvider.filter(providers, null).size());
    }
}
