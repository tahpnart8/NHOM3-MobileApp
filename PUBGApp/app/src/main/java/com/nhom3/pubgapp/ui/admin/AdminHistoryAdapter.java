package com.nhom3.pubgapp.ui.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.ItemAdminHistoryRowBinding;
import com.nhom3.pubgapp.model.ActivityLog;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.Review;
import com.nhom3.pubgapp.model.Warning;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.ui.common.StatusChips;
import com.nhom3.pubgapp.util.FormatUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** One adapter for the five history tabs of E03; the Fragment turns each model into a Row. */
public class AdminHistoryAdapter extends RecyclerView.Adapter<AdminHistoryAdapter.HistoryViewHolder> {

    /** Plain text of one row. At most one of the two statuses is set; with neither the chip is hidden. */
    public static final class Row {

        private final String title;
        private final String subtitle;
        private final String date;
        private final ListingStatus listingStatus;
        private final DealStatus dealStatus;

        public Row(String title, String subtitle, String date,
                @Nullable ListingStatus listingStatus, @Nullable DealStatus dealStatus) {
            this.title = title;
            this.subtitle = subtitle;
            this.date = date;
            this.listingStatus = listingStatus;
            this.dealStatus = dealStatus;
        }
    }

    static final String DATE_PATTERN = "dd/MM/yyyy";
    private static final String DATE_TIME_PATTERN = "dd/MM/yyyy HH:mm";
    private static final Locale LOCALE = Locale.forLanguageTag("vi-VN");

    private final List<Row> rows = new ArrayList<Row>();

    // The row builders return null for a null list: that tab has not loaded, which is not the same as empty.
    @Nullable
    static List<Row> listingRows(@Nullable List<Listing> listings) {
        if (listings == null) {
            return null;
        }
        FormatUtils formatUtils = new FormatUtils();
        List<Row> result = new ArrayList<Row>();
        for (Listing listing : listings) {
            result.add(new Row(listing.getTitle(), formatUtils.formatPrice(listing.getPrice()),
                    formatDate(listing.getCreatedAt(), DATE_PATTERN), listing.getStatus(), null));
        }
        return result;
    }

    @Nullable
    static List<Row> dealRows(@Nullable List<Deal> deals) {
        if (deals == null) {
            return null;
        }
        FormatUtils formatUtils = new FormatUtils();
        List<Row> result = new ArrayList<Row>();
        for (Deal deal : deals) {
            result.add(new Row(deal.getItemTitle(), formatUtils.formatPrice(deal.getAgreedPrice()),
                    formatDate(deal.getUpdatedAt(), DATE_PATTERN), null, deal.getStatus()));
        }
        return result;
    }

    @Nullable
    static List<Row> reviewRows(Context context, @Nullable List<Review> reviews) {
        if (reviews == null) {
            return null;
        }
        List<Row> result = new ArrayList<Row>();
        for (Review review : reviews) {
            String comment = review.getComment();
            boolean hasComment = comment != null && !comment.trim().isEmpty();
            String subtitle = hasComment ? comment : context.getString(R.string.admin_user_detail_review_no_comment);
            String title = context.getString(R.string.admin_user_detail_review_stars, review.getRating());
            result.add(new Row(title, subtitle, formatDate(review.getCreatedAt(), DATE_PATTERN), null, null));
        }
        return result;
    }

    @Nullable
    static List<Row> warningRows(Context context, @Nullable List<Warning> warnings) {
        if (warnings == null) {
            return null;
        }
        List<Row> result = new ArrayList<Row>();
        for (Warning warning : warnings) {
            String title = context.getString(R.string.admin_user_detail_warning_level, warning.getLevel());
            result.add(new Row(title, warning.getReason(), formatDate(warning.getCreatedAt(), DATE_PATTERN),
                    null, null));
        }
        return result;
    }

    @Nullable
    static List<Row> logRows(Context context, @Nullable List<ActivityLog> logs) {
        if (logs == null) {
            return null;
        }
        List<Row> result = new ArrayList<Row>();
        for (ActivityLog log : logs) {
            String target = context.getString(R.string.admin_user_detail_log_target,
                    nullToEmpty(log.getTargetType()), nullToEmpty(log.getTargetId())).trim();
            result.add(new Row(log.getAction(), target, formatDate(log.getCreatedAt(), DATE_TIME_PATTERN),
                    null, null));
        }
        return result;
    }

    static String formatDate(@Nullable Timestamp time, String pattern) {
        if (time == null) {
            return "";
        }
        return new SimpleDateFormat(pattern, LOCALE).format(time.toDate());
    }

    private static String nullToEmpty(@Nullable String text) {
        return text == null ? "" : text;
    }

    public void submitRows(List<Row> newRows) {
        rows.clear();
        if (newRows != null) {
            rows.addAll(newRows);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemAdminHistoryRowBinding binding = ItemAdminHistoryRowBinding.inflate(inflater, parent, false);
        return new HistoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.bind(rows.get(position));
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static final class HistoryViewHolder extends RecyclerView.ViewHolder {

        private final ItemAdminHistoryRowBinding binding;

        HistoryViewHolder(ItemAdminHistoryRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Row row) {
            binding.textRowTitle.setText(row.title);
            binding.textRowSubtitle.setText(row.subtitle);
            binding.textRowSubtitle.setVisibility(isBlank(row.subtitle) ? View.GONE : View.VISIBLE);
            binding.textRowDate.setText(row.date);
            if (row.listingStatus != null) {
                StatusChips.bind(binding.includeRowChip.getRoot(), row.listingStatus);
            } else if (row.dealStatus != null) {
                StatusChips.bind(binding.includeRowChip.getRoot(), row.dealStatus);
            } else {
                binding.includeRowChip.getRoot().setVisibility(View.GONE);
            }
        }

        private boolean isBlank(@Nullable String text) {
            return text == null || text.trim().isEmpty();
        }
    }
}
