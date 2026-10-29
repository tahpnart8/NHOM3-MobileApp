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
import com.nhom3.pubgapp.databinding.ItemAdminStatusChangeBinding;
import com.nhom3.pubgapp.model.StatusChange;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

// Timeline adapter displaying status transitions of a deal for admin.
public class AdminStatusHistoryAdapter
        extends RecyclerView.Adapter<AdminStatusHistoryAdapter.StatusChangeViewHolder> {

    private final List<StatusChange> items = new ArrayList<StatusChange>();
    private final FormatUtils formatUtils = new FormatUtils();

    public AdminStatusHistoryAdapter() {
    }

    public void submitList(@Nullable List<StatusChange> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public static int getStatusLabelRes(@Nullable DealStatus status) {
        if (status == null) {
            return 0;
        }
        switch (status) {
            case PENDING_CONFIRM:
                return R.string.common_status_deal_pending_confirm;
            case CONFIRMED:
                return R.string.common_status_deal_confirmed;
            case SHIPPED:
                return R.string.common_status_deal_shipped;
            case RECEIVED:
                return R.string.common_status_deal_received;
            case COMPLETED:
                return R.string.common_status_deal_completed;
            case CANCELLED:
                return R.string.common_status_deal_cancelled;
            case DISPUTED:
                return R.string.common_status_deal_disputed;
            default:
                return 0;
        }
    }

    @NonNull
    @Override
    public StatusChangeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new StatusChangeViewHolder(ItemAdminStatusChangeBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull StatusChangeViewHolder holder, int position) {
        StatusChange change = items.get(position);
        ItemAdminStatusChangeBinding binding = holder.binding;
        Context context = binding.getRoot().getContext();

        DealStatus from = change.getFromStatus();
        DealStatus to = change.getToStatus();

        if (from == null) {
            binding.layoutFromStatus.getRoot().setVisibility(View.GONE);
            binding.textArrow.setVisibility(View.GONE);
        } else {
            binding.layoutFromStatus.getRoot().setVisibility(View.VISIBLE);
            binding.textArrow.setVisibility(View.VISIBLE);
            AdminDealAdapter.bindDealStatus(binding.layoutFromStatus.textStatusChip, from);
        }
        AdminDealAdapter.bindDealStatus(binding.layoutToStatus.textStatusChip, to);

        String desc = change.describe();
        binding.textDescription.setText(desc != null ? desc : "");

        String changedBy = change.getChangedBy();
        binding.textChangedBy.setText(context.getString(
                R.string.admin_deal_detail_history_by,
                changedBy != null ? changedBy : ""));

        String reason = change.getReason();
        if (reason != null && !reason.trim().isEmpty()) {
            binding.textReason.setVisibility(View.VISIBLE);
            binding.textReason.setText(context.getString(
                    R.string.admin_deal_detail_history_reason,
                    reason));
        } else {
            binding.textReason.setVisibility(View.GONE);
        }

        Timestamp changedAt = change.getChangedAt();
        binding.textTime.setText(changedAt != null ? formatUtils.formatTime(changedAt) : "");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class StatusChangeViewHolder extends RecyclerView.ViewHolder {
        final ItemAdminStatusChangeBinding binding;

        StatusChangeViewHolder(ItemAdminStatusChangeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
