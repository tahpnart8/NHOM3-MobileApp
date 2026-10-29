package com.nhom3.pubgapp.ui.trade;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.ViewCheckoutSummaryBinding;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.model.enums.PaymentMethod;
import com.nhom3.pubgapp.ui.common.ImageLoader;
import com.nhom3.pubgapp.util.FormatUtils;

// Shared pieces of the C07 checkout steps: the order summary card and the "may this user pay" check.
public final class CheckoutSummary {

    private CheckoutSummary() {
    }

    /** Same rule as DealRepository.pay: only the buyer, only a confirmed deal with a price. */
    public static boolean canPay(@Nullable Deal deal, @Nullable String userId) {
        if (deal == null || userId == null || userId.isEmpty()) {
            return false;
        }
        return userId.equals(deal.getBuyerId())
                && deal.getStatus() == DealStatus.CONFIRMED
                && deal.needsPayment();
    }

    public static void bind(@NonNull ViewCheckoutSummaryBinding summary, @NonNull Deal deal,
                            @Nullable String sellerName) {
        Context context = summary.getRoot().getContext();
        String title = deal.getItemTitle() != null ? deal.getItemTitle() : "";
        summary.textProductTitle.setText(title);
        summary.textSeller.setText(formatSeller(context, sellerName));
        summary.textAmount.setText(new FormatUtils().formatPrice(deal.getAgreedPrice()));
        String thumb = deal.getItemThumb();
        if (thumb != null && !thumb.trim().isEmpty()) {
            ImageLoader.load(thumb, summary.imageProductThumb, R.drawable.ic_inbox, R.drawable.ic_inbox);
        } else {
            summary.imageProductThumb.setImageResource(R.drawable.ic_inbox);
        }
    }

    @NonNull
    public static String formatSeller(@NonNull Context context, @Nullable String sellerName) {
        if (sellerName == null || sellerName.trim().isEmpty()) {
            return context.getString(R.string.trade_checkout_seller_fallback);
        }
        return context.getString(R.string.trade_checkout_seller_label, sellerName.trim());
    }

    public static int getMethodStringRes(@Nullable PaymentMethod method) {
        if (method == PaymentMethod.E_WALLET) {
            return R.string.trade_deal_payment_method_wallet;
        }
        if (method == PaymentMethod.BANK_TRANSFER) {
            return R.string.trade_deal_payment_method_transfer;
        }
        return R.string.trade_deal_payment_method_card;
    }

    /** Reads a method name from navigation arguments; an unknown or missing name gives null. */
    @Nullable
    public static PaymentMethod parseMethod(@Nullable String name) {
        if (name == null) {
            return null;
        }
        PaymentMethod[] methods = PaymentMethod.values();
        for (PaymentMethod method : methods) {
            if (method.name().equals(name)) {
                return method;
            }
        }
        return null;
    }
}
