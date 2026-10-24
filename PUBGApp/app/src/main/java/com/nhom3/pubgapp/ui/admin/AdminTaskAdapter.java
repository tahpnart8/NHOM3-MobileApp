package com.nhom3.pubgapp.ui.admin;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.ItemAdminTaskBinding;
import com.nhom3.pubgapp.model.enums.ReportTarget;

import java.util.ArrayList;
import java.util.List;

// Rows of the "Cần xử lý" list on E01.
public class AdminTaskAdapter extends RecyclerView.Adapter<AdminTaskAdapter.TaskViewHolder> {

    public interface OnTaskClickListener {
        void onTaskClick(AdminDashboardRules.TaskRow row);
    }

    private final List<AdminDashboardRules.TaskRow> rows = new ArrayList<AdminDashboardRules.TaskRow>();
    private final OnTaskClickListener clickListener;

    public AdminTaskAdapter(OnTaskClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void submitRows(List<AdminDashboardRules.TaskRow> newRows) {
        rows.clear();
        if (newRows != null) {
            rows.addAll(newRows);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemAdminTaskBinding binding = ItemAdminTaskBinding.inflate(inflater, parent, false);
        return new TaskViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(rows.get(position));
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        private final ItemAdminTaskBinding binding;

        TaskViewHolder(ItemAdminTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final AdminDashboardRules.TaskRow row) {
            Context context = binding.getRoot().getContext();
            if (row.getKind() == AdminDashboardRules.KIND_REPORT) {
                bindReport(context, row);
            } else {
                bindVerification(context, row);
            }
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.onTaskClick(row);
                }
            });
        }

        private void bindReport(Context context, AdminDashboardRules.TaskRow row) {
            binding.textTaskTitle.setText(reportTitle(row.getTargetType()));
            binding.textTaskDescription.setText(
                    context.getString(R.string.admin_dashboard_task_report_count, row.getCount()));
            styleChip(context, R.string.admin_dashboard_label_new, R.color.success,
                    R.color.chip_background_green);
        }

        private void bindVerification(Context context, AdminDashboardRules.TaskRow row) {
            binding.textTaskTitle.setText(R.string.admin_dashboard_task_verification);
            if (row.getCount() > 0) {
                binding.textTaskDescription.setText(context.getString(
                        R.string.admin_dashboard_task_verification_resubmit, row.getCount()));
            } else {
                binding.textTaskDescription.setText(R.string.admin_dashboard_task_verification_new);
            }
            styleChip(context, R.string.admin_dashboard_label_waiting, R.color.warning,
                    R.color.chip_background_beige);
        }

        private int reportTitle(ReportTarget target) {
            if (target == ReportTarget.USER) {
                return R.string.admin_dashboard_task_report_user;
            }
            if (target == ReportTarget.MESSAGE) {
                return R.string.admin_dashboard_task_report_message;
            }
            return R.string.admin_dashboard_task_report_listing;
        }

        // Same colours as StatusChips uses for the green and beige chips.
        private void styleChip(Context context, int labelRes, int textColorRes, int backgroundColorRes) {
            binding.includeTaskChip.textStatusChip.setText(labelRes);
            binding.includeTaskChip.textStatusChip.setTextColor(
                    ContextCompat.getColor(context, textColorRes));
            binding.includeTaskChip.textStatusChip.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(context, backgroundColorRes)));
        }
    }
}
