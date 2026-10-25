package com.nhom3.pubgapp.ui.trust;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.MediaRepository;
import com.nhom3.pubgapp.databinding.DialogComplaintDetailBinding;
import com.nhom3.pubgapp.databinding.ItemMyComplaintBinding;
import com.nhom3.pubgapp.model.Complaint;
import com.nhom3.pubgapp.model.enums.ComplaintDecision;
import com.nhom3.pubgapp.model.enums.ComplaintReason;
import com.nhom3.pubgapp.model.enums.ComplaintStatus;
import com.nhom3.pubgapp.ui.common.ImageLoader;
import com.nhom3.pubgapp.ui.common.StatusChips;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Rows of the complaints the user has sent, on D07, and the detail dialog a row opens. */
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

    /** Fills the D07 detail dialog. The admin's internal note is never read here: users only see the decision. */
    static void bindDetail(DialogComplaintDetailBinding detail, Complaint complaint) {
        Context context = detail.getRoot().getContext();
        String dealId = complaint.getDealId() != null ? complaint.getDealId() : "";
        detail.textDealCode.setText(context.getString(R.string.profile_create_complaint_deal_code, dealId));
        StatusChips.bind(detail.includeStatusChip.textStatusChip, complaint.getStatus());
        detail.textReason.setText(reasonLabel(complaint.getReason()));
        Timestamp createdAt = complaint.getCreatedAt();
        detail.textSentDate.setVisibility(createdAt != null ? View.VISIBLE : View.GONE);
        if (createdAt != null) {
            detail.textSentDate.setText(context.getString(R.string.profile_my_reports_sent_on, formatDate(createdAt)));
        }
        detail.textDescription.setText(complaint.getDescription());
        bindEvidence(detail, complaint.getEvidenceImageIds());
        bindAdminAnswer(detail, complaint);
    }

    private static void bindEvidence(DialogComplaintDetailBinding detail, @Nullable List<String> imageIds) {
        boolean hasImages = imageIds != null && !imageIds.isEmpty();
        detail.textNoEvidence.setVisibility(hasImages ? View.GONE : View.VISIBLE);
        detail.recyclerEvidence.setVisibility(hasImages ? View.VISIBLE : View.GONE);
        EvidenceImageAdapter adapter = new EvidenceImageAdapter();
        Context context = detail.getRoot().getContext();
        LinearLayoutManager layout = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        detail.recyclerEvidence.setLayoutManager(layout);
        detail.recyclerEvidence.setAdapter(adapter);
        adapter.submitIds(imageIds);
    }

    // What the admin asked for while evidence is missing, or the decision and its message once resolved.
    private static void bindAdminAnswer(DialogComplaintDetailBinding detail, Complaint complaint) {
        String request = complaint.getEvidenceRequest();
        boolean asksEvidence = complaint.getStatus() == ComplaintStatus.NEEDS_EVIDENCE
                && request != null && !request.isEmpty();
        detail.layoutEvidenceRequest.setVisibility(asksEvidence ? View.VISIBLE : View.GONE);
        detail.textEvidenceRequest.setText(request);
        int decisionLabel = decisionLabel(complaint.getDecision());
        boolean decided = complaint.getStatus() == ComplaintStatus.RESOLVED && decisionLabel != 0;
        detail.layoutDecision.setVisibility(decided ? View.VISIBLE : View.GONE);
        if (!decided) {
            return;
        }
        detail.textDecision.setText(decisionLabel);
        String message = complaint.getResolutionMessage();
        boolean hasMessage = message != null && !message.isEmpty();
        detail.textResolutionMessage.setVisibility(hasMessage ? View.VISIBLE : View.GONE);
        detail.textResolutionMessage.setText(message);
    }

    @StringRes
    private static int decisionLabel(@Nullable ComplaintDecision decision) {
        if (decision == ComplaintDecision.REFUND) {
            return R.string.profile_my_complaints_decision_refund;
        }
        if (decision == ComplaintDecision.RETURN_ITEM) {
            return R.string.profile_my_complaints_decision_return_item;
        }
        if (decision == ComplaintDecision.CANCEL_DEAL) {
            return R.string.profile_my_complaints_decision_cancel_deal;
        }
        if (decision == ComplaintDecision.REJECT) {
            return R.string.profile_my_complaints_decision_reject;
        }
        return 0;
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

    /** Evidence thumbnails read by id through MediaRepository and drawn by ImageLoader. */
    static final class EvidenceImageAdapter extends RecyclerView.Adapter<EvidenceImageAdapter.ImageHolder> {

        private final List<String> imageIds = new ArrayList<String>();
        private final MediaRepository mediaRepository = new MediaRepository();

        void submitIds(@Nullable List<String> ids) {
            imageIds.clear();
            if (ids != null) {
                imageIds.addAll(ids);
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView image = new ImageView(parent.getContext());
            int size = parent.getResources().getDimensionPixelSize(R.dimen.avatar_size_lg);
            int gap = parent.getResources().getDimensionPixelSize(R.dimen.space_sm);
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(size, size);
            params.setMarginEnd(gap);
            image.setLayoutParams(params);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.setContentDescription(parent.getContext().getString(R.string.profile_my_complaints_evidence_title));
            return new ImageHolder(image);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageHolder holder, int position) {
            holder.bind(imageIds.get(position), mediaRepository);
        }

        @Override
        public int getItemCount() {
            return imageIds.size();
        }

        static final class ImageHolder extends RecyclerView.ViewHolder {

            private final ImageView image;
            // The id this row shows now; a late answer for a recycled row is dropped.
            private String boundId;

            ImageHolder(ImageView image) {
                super(image);
                this.image = image;
            }

            void bind(final String imageId, MediaRepository mediaRepository) {
                boundId = imageId;
                image.setImageResource(R.drawable.ic_inbox);
                mediaRepository.getImage(imageId, new DataCallback<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        if (!imageId.equals(boundId)) {
                            return;
                        }
                        ImageLoader.load(bytes, imageId, image, R.drawable.ic_inbox, R.drawable.ic_error);
                    }

                    @Override
                    public void onError(String message) {
                        if (imageId.equals(boundId)) {
                            image.setImageResource(R.drawable.ic_error);
                        }
                    }
                });
            }
        }
    }
}
