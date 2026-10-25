package com.nhom3.pubgapp.ui.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.ItemAdminReportBinding;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.enums.ReportReason;
import com.nhom3.pubgapp.model.enums.ReportStatus;
import com.nhom3.pubgapp.model.enums.ReportTarget;
import com.nhom3.pubgapp.ui.common.ImageLoader;
import com.nhom3.pubgapp.ui.common.StatusChips;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Rows of E04: one row per reported target, with the buttons that fit its type and state.
public class AdminReportAdapter extends RecyclerView.Adapter<AdminReportAdapter.ReportViewHolder> {

    public interface OnReportActionListener {
        void onOpenListing(AdminReportGrouping.Group group);

        void onDismiss(AdminReportGrouping.Group group);

        void onHide(AdminReportGrouping.Group group);

        void onViewUser(AdminReportGrouping.Group group);

        void onViewEvidence(AdminReportGrouping.Group group);
    }

    private final List<AdminReportGrouping.Group> groups = new ArrayList<AdminReportGrouping.Group>();
    private final Map<String, Listing> listingsById = new HashMap<String, Listing>();
    private final OnReportActionListener listener;
    private boolean actionsEnabled = true;

    public AdminReportAdapter(OnReportActionListener listener) {
        this.listener = listener;
    }

    public void setData(List<AdminReportGrouping.Group> newGroups, @Nullable List<Listing> listings) {
        groups.clear();
        groups.addAll(newGroups);
        listingsById.clear();
        if (listings != null) {
            for (Listing listing : listings) {
                listingsById.put(listing.getId(), listing);
            }
        }
        notifyDataSetChanged();
    }

    public void setActionsEnabled(boolean enabled) {
        if (actionsEnabled == enabled) {
            return;
        }
        actionsEnabled = enabled;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ReportViewHolder(ItemAdminReportBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        final AdminReportGrouping.Group group = groups.get(position);
        ItemAdminReportBinding binding = holder.binding;
        Context context = binding.getRoot().getContext();
        boolean isListing = group.getTargetType() == ReportTarget.LISTING;
        Listing listing = isListing ? listingsById.get(group.getTargetId()) : null;

        bindTitleAndThumb(binding, group, listing);
        binding.textReasons.setText(reasonsText(context, group));
        binding.textMeta.setText(metaText(context, group));
        bindDescription(binding, group);
        StatusChips.bind(binding.layoutStatusChip.textStatusChip, group.getStatus());
        bindButtons(binding, group, listing);

        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (group.getTargetType() == ReportTarget.LISTING) {
                    listener.onOpenListing(group);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    private void bindTitleAndThumb(ItemAdminReportBinding binding, AdminReportGrouping.Group group,
            @Nullable Listing listing) {
        Context context = binding.getRoot().getContext();
        if (group.getTargetType() != ReportTarget.LISTING) {
            binding.imageThumb.setVisibility(View.GONE);
            int labelRes = group.getTargetType() == ReportTarget.USER
                    ? R.string.admin_moderation_target_user : R.string.admin_moderation_target_message;
            binding.textTitle.setText(context.getString(labelRes, group.getTargetId()));
            return;
        }
        binding.imageThumb.setVisibility(View.VISIBLE);
        if (listing == null) {
            binding.imageThumb.setImageResource(R.drawable.ic_error);
            binding.textTitle.setText(R.string.admin_moderation_listing_gone);
            return;
        }
        binding.textTitle.setText(listing.getTitle());
        ImageLoader.load(listing.getCoverThumb(), binding.imageThumb, R.drawable.ic_inbox, R.drawable.ic_error);
    }

    private void bindDescription(ItemAdminReportBinding binding, AdminReportGrouping.Group group) {
        String description = group.getLatestDescription();
        boolean hasText = description != null && !description.trim().isEmpty();
        binding.textDescription.setVisibility(hasText ? View.VISIBLE : View.GONE);
        binding.textDescription.setText(description);
    }

    // Closed groups are read only. A message report has no hide button until the sender can be shown.
    private void bindButtons(ItemAdminReportBinding binding, AdminReportGrouping.Group group,
            @Nullable Listing listing) {
        final AdminReportGrouping.Group target = group;
        // Evidence stays readable after a report is closed, so this button is wired before the read-only return.
        binding.buttonEvidence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onViewEvidence(target);
            }
        });
        boolean isOpen = group.getStatus() == ReportStatus.OPEN;
        binding.layoutActions.setVisibility(isOpen ? View.VISIBLE : View.GONE);
        binding.textHint.setVisibility(View.GONE);
        if (!isOpen) {
            return;
        }
        binding.buttonDismiss.setEnabled(actionsEnabled);
        binding.buttonDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onDismiss(target);
            }
        });
        if (group.getTargetType() == ReportTarget.LISTING) {
            bindHideButton(binding, target, listing);
        } else if (group.getTargetType() == ReportTarget.USER) {
            bindViewUserButton(binding, target);
        } else {
            binding.buttonAction.setVisibility(View.GONE);
        }
    }

    private void bindHideButton(ItemAdminReportBinding binding, final AdminReportGrouping.Group group,
            @Nullable Listing listing) {
        binding.buttonAction.setVisibility(View.VISIBLE);
        binding.buttonAction.setText(R.string.admin_moderation_action_hide);
        boolean canHide = listing != null && AdminListingActionRules.canHide(listing.getStatus());
        binding.buttonAction.setEnabled(actionsEnabled && canHide);
        binding.buttonAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onHide(group);
            }
        });
        if (listing != null && !canHide) {
            binding.textHint.setVisibility(View.VISIBLE);
            binding.textHint.setText(R.string.admin_moderation_hide_locked);
        }
    }

    private void bindViewUserButton(ItemAdminReportBinding binding, final AdminReportGrouping.Group group) {
        binding.buttonAction.setVisibility(View.VISIBLE);
        binding.buttonAction.setText(R.string.admin_moderation_action_view_user);
        binding.buttonAction.setEnabled(true);
        binding.buttonAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onViewUser(group);
            }
        });
    }

    private String reasonsText(Context context, AdminReportGrouping.Group group) {
        StringBuilder text = new StringBuilder();
        for (ReportReason reason : group.getReasons()) {
            if (text.length() > 0) {
                text.append(context.getString(R.string.admin_moderation_reason_separator));
            }
            text.append(context.getString(reasonLabel(reason)));
        }
        return text.toString();
    }

    private String metaText(Context context, AdminReportGrouping.Group group) {
        String count = context.getString(R.string.admin_moderation_report_count, group.getReportCount());
        if (group.getEvidenceCount() == 0) {
            return count;
        }
        String evidence = context.getString(R.string.admin_moderation_evidence_count, group.getEvidenceCount());
        return count + context.getString(R.string.admin_moderation_reason_separator) + evidence;
    }

    @StringRes
    static int reasonLabel(ReportReason reason) {
        switch (reason) {
            case FAKE_ITEM:
                return R.string.admin_moderation_reason_fake_item;
            case MISLEADING_INFO:
                return R.string.admin_moderation_reason_misleading_info;
            case SCAM:
                return R.string.admin_moderation_reason_scam;
            case INAPPROPRIATE:
                return R.string.admin_moderation_reason_inappropriate;
            default:
                return R.string.admin_moderation_reason_other;
        }
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        final ItemAdminReportBinding binding;

        ReportViewHolder(ItemAdminReportBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
