package com.nhom3.pubgapp.ui.trust;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.ItemMyComplaintBinding;
import com.nhom3.pubgapp.model.Complaint;
import com.nhom3.pubgapp.model.enums.ComplaintReason;
import com.nhom3.pubgapp.ui.common.StatusChips;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Rows of the complaints the user has sent, on D07. */
public class MyComplaintAdapter extends RecyclerView.Adapter<MyComplaintAdapter.ComplaintViewHolder> {

    /** Called when the user taps a row. */
    interface ComplaintListener {
        void onOpen(Complaint complaint);
    }

    private static final String DATE_PATTERN = "dd/MM/yyyy";

    private final List<Complaint> complaints = new ArrayList<Complaint>();
    private final ComplaintListener listener;

    MyComplaintAdapter(ComplaintListener listener) {
        this.listener = listener;
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
        ItemMyComplaintBinding binding = ItemMyComplaintBinding.inflate(inflater, parent, false);
        return new ComplaintViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintViewHolder holder, int position) {
        holder.bind(complaints.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return complaints.size();
    }

    // Shared with the detail dialog, so a reason reads the same on the row and in the dialog.
    @StringRes
    static int reasonLabel(ComplaintReason reason) {
        if (reason == ComplaintReason.NOT_AS_DESCRIBED) {
            return R.string.profile_create_complaint_reason_not_as_described;
        }
        if (reason == ComplaintReason.NOT_RECEIVED) {
            return R.string.profile_create_complaint_reason_not_received;
        }
        if (reason == ComplaintReason.DAMAGED) {
            return R.string.profile_create_complaint_reason_damaged;
        }
        return R.string.profile_create_complaint_reason_other;
    }

    // A complaint just written has no server time yet, so callers hide the date line until it arrives.
    static String formatDate(Timestamp time) {
        SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN, Locale.ROOT);
        return format.format(time.toDate());
    }

    static final class ComplaintViewHolder extends RecyclerView.ViewHolder {

        private final ItemMyComplaintBinding binding;

        ComplaintViewHolder(ItemMyComplaintBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Complaint complaint, final ComplaintListener listener) {
            Context context = binding.getRoot().getContext();
            String dealId = complaint.getDealId() != null ? complaint.getDealId() : "";
            binding.textDealCode.setText(context.getString(R.string.profile_create_complaint_deal_code, dealId));
            binding.textReason.setText(reasonLabel(complaint.getReason()));
            StatusChips.bind(binding.includeStatusChip.textStatusChip, complaint.getStatus());
            showSentDate(context, complaint.getCreatedAt());
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View clicked) {
                    listener.onOpen(complaint);
                }
            });
        }

        private void showSentDate(Context context, Timestamp createdAt) {
            if (createdAt == null) {
                binding.textSentDate.setVisibility(View.GONE);
                return;
            }
            binding.textSentDate.setText(context.getString(R.string.profile_my_reports_sent_on, formatDate(createdAt)));
            binding.textSentDate.setVisibility(View.VISIBLE);
        }
    }
}
