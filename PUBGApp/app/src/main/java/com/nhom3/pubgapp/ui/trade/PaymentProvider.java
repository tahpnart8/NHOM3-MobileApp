package com.nhom3.pubgapp.ui.trade;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// A bank or e-wallet of the simulated checkout. Display only: it is never written to Firestore.
public class PaymentProvider {

    private static final String ENTRY_SEPARATOR = "\\|";

    private final String code;
    private final String name;

    public PaymentProvider(@NonNull String code, @NonNull String name) {
        this.code = code;
        this.name = name;
    }

    @NonNull
    public String getCode() {
        return code;
    }

    @NonNull
    public String getName() {
        return name;
    }

    /** Reads entries such as "VCB|Vietcombank" from the string arrays in strings_trade.xml. */
    @NonNull
    public static List<PaymentProvider> fromEntries(@Nullable String[] entries) {
        List<PaymentProvider> providers = new ArrayList<PaymentProvider>();
        if (entries == null) {
            return providers;
        }
        for (String entry : entries) {
            String[] parts = entry.split(ENTRY_SEPARATOR, 2);
            if (parts.length == 2) {
                providers.add(new PaymentProvider(parts[0].trim(), parts[1].trim()));
            }
        }
        return providers;
    }

    /** Keeps the providers whose code or name contains the query, ignoring case and Vietnamese accents. */
    @NonNull
    public static List<PaymentProvider> filter(@NonNull List<PaymentProvider> providers,
                                               @Nullable String query) {
        List<PaymentProvider> result = new ArrayList<PaymentProvider>();
        String needle = simplify(query);
        for (PaymentProvider provider : providers) {
            String code = simplify(provider.getCode());
            String name = simplify(provider.getName());
            if (needle.isEmpty() || code.contains(needle) || name.contains(needle)) {
                result.add(provider);
            }
        }
        return result;
    }

    // "Ví" and "vi" must match, so accents are stripped; "đ" has no decomposed form and is mapped by hand.
    @NonNull
    private static String simplify(@Nullable String text) {
        if (text == null) {
            return "";
        }
        String lower = text.trim().toLowerCase(Locale.ROOT).replace('đ', 'd');
        String decomposed = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}", "");
    }
}
