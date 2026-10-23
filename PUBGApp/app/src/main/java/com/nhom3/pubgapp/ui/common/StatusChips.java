package com.nhom3.pubgapp.ui.common;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.google.android.material.color.MaterialColors;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.model.enums.ComplaintStatus;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.model.enums.OfferStatus;
import com.nhom3.pubgapp.model.enums.PaymentStatus;
import com.nhom3.pubgapp.model.enums.Priority;
import com.nhom3.pubgapp.model.enums.ReportStatus;
import com.nhom3.pubgapp.model.enums.UserStatus;
import com.nhom3.pubgapp.model.enums.VerificationStatus;

// Labels and colours a view_status_chip per giao-dien.md section 4. A null state hides the chip.
public final class StatusChips {

    private static final int STYLE_NEUTRAL = 0;
    private static final int STYLE_GREEN = 1;
    private static final int STYLE_BEIGE = 2;
    private static final int STYLE_ERROR = 3;
    private static final int STYLE_INFO = 4;

    private StatusChips() {
    }

    public static void bind(@NonNull TextView chip, @Nullable ListingStatus status) {
        if (status == null) {
            hide(chip);
            return;
        }
        switch (status) {
            case DRAFT:
                apply(chip, R.string.common_status_listing_draft, STYLE_NEUTRAL);
                break;
            case AVAILABLE:
                apply(chip, R.string.common_status_listing_available, STYLE_GREEN);
                break;
            case RESERVED:
                apply(chip, R.string.common_status_listing_reserved, STYLE_BEIGE);
                break;
            case SOLD:
                apply(chip, R.string.common_status_listing_sold, STYLE_NEUTRAL);
                break;
            case HIDDEN:
                apply(chip, R.string.common_status_listing_hidden, STYLE_NEUTRAL);
                break;
            case REMOVED:
                apply(chip, R.string.common_status_listing_removed, STYLE_ERROR);
                break;
            default:
                hide(chip);
                break;
        }
    }

    public static void bind(@NonNull TextView chip, @Nullable DealStatus status) {
        if (status == null) {
            hide(chip);
            return;
        }
        switch (status) {
            case PENDING_CONFIRM:
                apply(chip, R.string.common_status_deal_pending_confirm, STYLE_BEIGE);
                break;
            case CONFIRMED:
                apply(chip, R.string.common_status_deal_confirmed, STYLE_INFO);
                break;
            case SHIPPED:
                apply(chip, R.string.common_status_deal_shipped, STYLE_INFO);
                break;
            case RECEIVED:
                apply(chip, R.string.common_status_deal_received, STYLE_GREEN);
                break;
            case COMPLETED:
                apply(chip, R.string.common_status_deal_completed, STYLE_GREEN);
                break;
            case CANCELLED:
                apply(chip, R.string.common_status_deal_cancelled, STYLE_NEUTRAL);
                break;
            case DISPUTED:
                apply(chip, R.string.common_status_deal_disputed, STYLE_ERROR);
                break;
            default:
                hide(chip);
                break;
        }
    }

    public static void bind(@NonNull TextView chip, @Nullable OfferStatus status) {
        if (status == null) {
            hide(chip);
            return;
        }
        switch (status) {
            case PENDING:
                apply(chip, R.string.common_status_offer_pending, STYLE_BEIGE);
                break;
            case ACCEPTED:
                apply(chip, R.string.common_status_offer_accepted, STYLE_GREEN);
                break;
            case REJECTED:
                apply(chip, R.string.common_status_offer_rejected, STYLE_NEUTRAL);
                break;
            default:
                hide(chip);
                break;
        }
    }

    public static void bind(@NonNull TextView chip, @Nullable PaymentStatus status) {
        if (status == null) {
            hide(chip);
            return;
        }
        switch (status) {
            case HELD:
                apply(chip, R.string.common_status_payment_held, STYLE_BEIGE);
                break;
            case RELEASED:
                apply(chip, R.string.common_status_payment_released, STYLE_GREEN);
                break;
            case REFUNDED:
                apply(chip, R.string.common_status_payment_refunded, STYLE_NEUTRAL);
                break;
            default:
                hide(chip);
                break;
        }
    }

    public static void bind(@NonNull TextView chip, @Nullable ReportStatus status) {
        if (status == null) {
            hide(chip);
            return;
        }
        switch (status) {
            case OPEN:
                apply(chip, R.string.common_status_report_open, STYLE_BEIGE);
                break;
            case RESOLVED:
                apply(chip, R.string.common_status_report_resolved, STYLE_GREEN);
                break;
            case DISMISSED:
                apply(chip, R.string.common_status_report_dismissed, STYLE_NEUTRAL);
                break;
            case CANCELLED:
                apply(chip, R.string.common_status_report_cancelled, STYLE_NEUTRAL);
                break;
            default:
                hide(chip);
                break;
        }
    }

    public static void bind(@NonNull TextView chip, @Nullable ComplaintStatus status) {
        if (status == null) {
            hide(chip);
            return;
        }
        switch (status) {
            case OPEN:
                apply(chip, R.string.common_status_complaint_open, STYLE_BEIGE);
                break;
            case NEEDS_EVIDENCE:
                apply(chip, R.string.common_status_complaint_needs_evidence, STYLE_BEIGE);
                break;
            case RESOLVED:
                apply(chip, R.string.common_status_complaint_resolved, STYLE_GREEN);
                break;
            case WITHDRAWN:
                apply(chip, R.string.common_status_complaint_withdrawn, STYLE_NEUTRAL);
                break;
            default:
                hide(chip);
                break;
        }
    }

    public static void bind(@NonNull TextView chip, @Nullable VerificationStatus status) {
        if (status == null) {
            hide(chip);
            return;
        }
        switch (status) {
            case NONE:
                apply(chip, R.string.common_status_verification_none, STYLE_NEUTRAL);
                break;
            case PENDING:
                apply(chip, R.string.common_status_verification_pending, STYLE_BEIGE);
                break;
            case APPROVED:
                apply(chip, R.string.common_status_verification_approved, STYLE_GREEN);
                break;
            case REJECTED:
                apply(chip, R.string.common_status_verification_rejected, STYLE_ERROR);
                break;
            default:
                hide(chip);
                break;
        }
    }

    public static void bind(@NonNull TextView chip, @Nullable UserStatus status) {
        if (status == null) {
            hide(chip);
            return;
        }
        switch (status) {
            case ACTIVE:
                apply(chip, R.string.common_status_account_active, STYLE_GREEN);
                break;
            case TEMP_LOCKED:
                apply(chip, R.string.common_status_account_temp_locked, STYLE_BEIGE);
                break;
            case PERM_LOCKED:
                apply(chip, R.string.common_status_account_perm_locked, STYLE_ERROR);
                break;
            case DELETED:
                apply(chip, R.string.common_status_account_deleted, STYLE_NEUTRAL);
                break;
            default:
                hide(chip);
                break;
        }
    }

    public static void bind(@NonNull TextView chip, @Nullable Priority priority) {
        if (priority == null) {
            hide(chip);
            return;
        }
        switch (priority) {
            case LOW:
                apply(chip, R.string.common_status_priority_low, STYLE_NEUTRAL);
                break;
            case MEDIUM:
                apply(chip, R.string.common_status_priority_medium, STYLE_INFO);
                break;
            case HIGH:
                apply(chip, R.string.common_status_priority_high, STYLE_ERROR);
                break;
            default:
                hide(chip);
                break;
        }
    }

    private static void hide(@NonNull TextView chip) {
        chip.setVisibility(View.GONE);
    }

    private static void apply(@NonNull TextView chip, @StringRes int labelRes, int style) {
        int textColor = textColorFor(chip, style);
        int backgroundColor = backgroundColorFor(chip, style);
        chip.setVisibility(View.VISIBLE);
        chip.setText(labelRes);
        chip.setTextColor(textColor);
        chip.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
    }

    @ColorInt
    private static int textColorFor(@NonNull View chip, int style) {
        Context context = chip.getContext();
        switch (style) {
            case STYLE_GREEN:
                return ContextCompat.getColor(context, R.color.success);
            case STYLE_BEIGE:
                return ContextCompat.getColor(context, R.color.warning);
            case STYLE_INFO:
                return ContextCompat.getColor(context, R.color.info);
            case STYLE_ERROR:
                return MaterialColors.getColor(chip, com.google.android.material.R.attr.colorOnPrimary);
            default:
                return MaterialColors.getColor(chip, com.google.android.material.R.attr.colorOnSurfaceVariant);
        }
    }

    @ColorInt
    private static int backgroundColorFor(@NonNull View chip, int style) {
        Context context = chip.getContext();
        switch (style) {
            case STYLE_GREEN:
                return ContextCompat.getColor(context, R.color.chip_background_green);
            case STYLE_BEIGE:
                return ContextCompat.getColor(context, R.color.chip_background_beige);
            case STYLE_ERROR:
                return ContextCompat.getColor(context, R.color.error);
            default:
                return MaterialColors.getColor(chip, com.google.android.material.R.attr.colorSurface);
        }
    }
}
