package com.nhom3.pubgapp.ui.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.ItemAdminComplaintBinding;
import com.nhom3.pubgapp.model.Complaint;
import com.nhom3.pubgapp.model.enums.ComplaintReason;
import com.nhom3.pubgapp.ui.common.StatusChips;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Rows of complaints on D06 for the admin. */
public class AdminComplaintAdapter extends RecyclerView.Adapter<AdminComplaintAdapter.ComplaintViewHolder> {

    /** Called when the admin taps a row to open its file. */
    interface OpenListener {
        void onOpen(Complaint complaint);
    }

    private static final String DATE_PATTERN = "dd/MM/yyyy";

    private final List<Complaint> complaints = new ArrayList<Complaint>();
    private final OpenListener openListener;

    AdminComplaintAdapter(OpenListener openListener) {
        this.openListener = openListener;
    }

    public void submitList(List<Complaint> newComplaints) {
        complaints.clear();
        if (newComplaints != null) {
            complaints.addAll(newComplaints);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemAdminComplaintBinding binding = ItemAdminComplaintBinding.inflate(inflater, parent, false);
        return new ComplaintViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintViewHolder holder, int position) {
        holder.bind(complaints.get(position), openListener);
    }

    @Override
    public int getItemCount() {
        return complaints.size();
    }

    /** The Vietnamese label of a complaint reason, shared with the detail screen. */
    static int reasonLabel(ComplaintReason reason) {
        if (reason == ComplaintReason.NOT_AS_DESCRIBED) {
            return R.string.admin_complaint_reason_not_as_described;
        }
        if (reason == ComplaintReason.NOT_RECEIVED) {
            return R.string.admin_complaint_reason_not_received;
        }
        if (reason == ComplaintReason.DAMAGED) {
            return R.string.admin_complaint_reason_damaged;
        }
        return R.string.admin_complaint_reason_other;
    }

    static String formatDate(Timestamp time) {
        if (time == null) {
            return "";
        }
        SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN, Locale.ROOT);
        return format.format(time.toDate());
    }

    static final class ComplaintViewHolder extends RecyclerView.ViewHolder {

        private final ItemAdminComplaintBinding binding;

        ComplaintViewHolder(ItemAdminComplaintBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // The complaint id is the dealId (trang-thai.md section 6), so it doubles as the deal code.
        void bind(final Complaint complaint, final OpenListener listener) {
            Context context = binding.getRoot().getContext();
            binding.textComplaintCode.setText(context.getString(R.string.admin_complaint_code, complaint.getDealId()));
            binding.textReason.setText(reasonLabel(complaint.getReason()));
            StatusChips.bind(binding.includePriorityChip.textStatusChip, complaint.getPriority());
            StatusChips.bind(binding.includeStatusChip.textStatusChip, complaint.getStatus());
            String sent = formatDate(complaint.getCreatedAt());
            binding.textSentDate.setText(context.getString(R.string.admin_complaint_sent_on, sent));
            binding.textSentDate.setVisibility(sent.isEmpty() ? View.GONE : View.VISIBLE);
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View clicked) {
                    listener.onOpen(complaint);
                }
            });
        }
    }
}
