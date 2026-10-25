package com.nhom3.pubgapp.ui.trust;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.ItemMyReportBinding;
import com.nhom3.pubgapp.model.Report;
import com.nhom3.pubgapp.model.enums.ReportReason;
import com.nhom3.pubgapp.model.enums.ReportTarget;
import com.nhom3.pubgapp.ui.common.StatusChips;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Rows of the reports the user has sent, on D07. */
public class MyReportAdapter extends RecyclerView.Adapter<MyReportAdapter.ReportViewHolder> {

    private static final String DATE_PATTERN = "dd/MM/yyyy";

    private final List<Report> reports = new ArrayList<Report>();

    public void submitList(List<Report> newReports) {
        reports.clear();
        if (newReports != null) {
            reports.addAll(newReports);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemMyReportBinding binding = ItemMyReportBinding.inflate(inflater, parent, false);
        return new ReportViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        holder.bind(reports.get(position));
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static final class ReportViewHolder extends RecyclerView.ViewHolder {

        private final ItemMyReportBinding binding;

        ReportViewHolder(ItemMyReportBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Report report) {
            Context context = binding.getRoot().getContext();
            binding.textTarget.setText(targetLabel(report.getTargetType()));
            binding.textReason.setText(reasonLabel(report.getReason()));
            StatusChips.bind(binding.includeStatusChip.textStatusChip, report.getStatus());
            showSentDate(context, report.getCreatedAt());
        }

        // A report just written has no server time yet, so the date line stays hidden until it arrives.
        private void showSentDate(Context context, Timestamp createdAt) {
            if (createdAt == null) {
                binding.textSentDate.setVisibility(View.GONE);
                return;
            }
            SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN, Locale.ROOT);
            String date = format.format(createdAt.toDate());
            binding.textSentDate.setText(context.getString(R.string.profile_my_reports_sent_on, date));
            binding.textSentDate.setVisibility(View.VISIBLE);
        }

        private static int targetLabel(ReportTarget target) {
            if (target == ReportTarget.USER) {
                return R.string.profile_my_reports_target_user;
            }
            if (target == ReportTarget.MESSAGE) {
                return R.string.profile_my_reports_target_message;
            }
            return R.string.profile_my_reports_target_listing;
        }

        private static int reasonLabel(ReportReason reason) {
            if (reason == ReportReason.FAKE_ITEM) {
                return R.string.profile_report_reason_fake_item;
            }
            if (reason == ReportReason.MISLEADING_INFO) {
                return R.string.profile_report_reason_misleading_info;
            }
            if (reason == ReportReason.SCAM) {
                return R.string.profile_report_reason_scam;
            }
            if (reason == ReportReason.INAPPROPRIATE) {
                return R.string.profile_report_reason_inappropriate;
            }
            return R.string.profile_report_reason_other;
        }
    }
}
