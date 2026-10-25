package com.nhom3.pubgapp.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.MediaRepository;
import com.nhom3.pubgapp.databinding.DialogAdminComplaintDecisionBinding;
import com.nhom3.pubgapp.databinding.FragmentAdminComplaintDetailBinding;
import com.nhom3.pubgapp.model.ChatMessage;
import com.nhom3.pubgapp.model.Complaint;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.StatusChange;
import com.nhom3.pubgapp.model.enums.ComplaintDecision;
import com.nhom3.pubgapp.model.enums.ComplaintStatus;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.model.enums.MessageType;
import com.nhom3.pubgapp.model.enums.Priority;
import com.nhom3.pubgapp.ui.common.ImageLoader;
import com.nhom3.pubgapp.ui.common.StatusChips;
import com.nhom3.pubgapp.util.FormatUtils;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.viewmodel.AdminComplaintViewModel;

import java.util.ArrayList;
import java.util.List;

/** D06 file of one complaint: the deal, both parties, reason, priority, evidence, chat and deal history. */
public class AdminComplaintDetailFragment extends Fragment {

    private static final String ARG_COMPLAINT_ID = "complaintId";
    private static final String STATE_NOTE_FILLED = "adminNoteFilled";
    private static final String STATE_SAVING_NOTE = "adminSavingNote";

    @Nullable
    private FragmentAdminComplaintDetailBinding binding;
    private AdminComplaintViewModel complaintViewModel;
    private EvidenceImageAdapter evidenceAdapter;
    private String complaintId;
    // True while the chips are set from the loaded complaint, so that is not taken as the admin's choice.
    private boolean showingPriority;
    // The saved note is put in the box once; after that the box keeps what the admin types, also on rotation.
    private boolean noteFilled;
    // Which write the next done answers, so the right confirmation is shown.
    private boolean savingNote;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminComplaintDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle arguments = getArguments();
        complaintId = arguments != null ? arguments.getString(ARG_COMPLAINT_ID) : null;
        complaintViewModel = new ViewModelProvider(this).get(AdminComplaintViewModel.class);
        if (savedInstanceState != null) {
            noteFilled = savedInstanceState.getBoolean(STATE_NOTE_FILLED, false);
            savingNote = savedInstanceState.getBoolean(STATE_SAVING_NOTE, false);
        }
        setUpToolbar();
        setUpSections();
        setUpDecision();
        observeViewModel();
        observeDone();
        if (complaintViewModel.getSelected().getValue() == null) {
            complaintViewModel.loadDetail(complaintId);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding == null) {
            return;
        }
        boolean online = NetworkMonitor.getInstance().isOnline();
        binding.includeOfflineBanner.getRoot().setVisibility(online ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_NOTE_FILLED, noteFilled);
        outState.putBoolean(STATE_SAVING_NOTE, savingNote);
    }

    private void setUpDecision() {
        decideOnClick(binding.buttonDecideRefund, ComplaintDecision.REFUND);
        decideOnClick(binding.buttonDecideReturnItem, ComplaintDecision.RETURN_ITEM);
        decideOnClick(binding.buttonDecideCancelDeal, ComplaintDecision.CANCEL_DEAL);
        decideOnClick(binding.buttonDecideReject, ComplaintDecision.REJECT);
        decideOnClick(binding.buttonRequestEvidence, ComplaintDecision.REQUEST_EVIDENCE);
        binding.buttonSaveNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                savingNote = true;
                complaintViewModel.saveNote(complaintId, readText(binding.editAdminNote.getText()));
            }
        });
    }

    private void decideOnClick(View button, final ComplaintDecision decision) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                openDecisionDialog(decision);
            }
        });
    }

    // A decision moves the deal and the payment and cannot be undone, so it always goes through this dialog.
    private void openDecisionDialog(final ComplaintDecision decision) {
        final DialogAdminComplaintDecisionBinding dialogBinding =
                DialogAdminComplaintDecisionBinding.inflate(getLayoutInflater());
        boolean evidence = decision == ComplaintDecision.REQUEST_EVIDENCE;
        dialogBinding.textDecisionWarning.setText(evidence ? R.string.admin_complaint_decision_warning_evidence
                : R.string.admin_complaint_decision_warning);
        dialogBinding.layoutDecisionMessage.setHint(evidence ? R.string.admin_complaint_decision_request_hint
                : R.string.admin_complaint_decision_message_hint);
        final AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(decisionLabel(decision))
                .setView(dialogBinding.getRoot())
                .setNegativeButton(R.string.admin_complaint_decision_back, null)
                .setPositiveButton(R.string.admin_complaint_decision_send, null)
                .show();
        // Set after show so an empty message keeps the dialog open with the error under the box.
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                String message = readText(dialogBinding.editDecisionMessage.getText());
                if (message.isEmpty()) {
                    dialogBinding.layoutDecisionMessage.setError(getString(R.string.admin_complaint_decision_empty));
                    return;
                }
                savingNote = false;
                complaintViewModel.decide(complaintId, decision, message);
                dialog.dismiss();
            }
        });
    }

    private static int decisionLabel(ComplaintDecision decision) {
        if (decision == ComplaintDecision.REFUND) {
            return R.string.admin_complaint_decision_refund;
        }
        if (decision == ComplaintDecision.RETURN_ITEM) {
            return R.string.admin_complaint_decision_return_item;
        }
        if (decision == ComplaintDecision.CANCEL_DEAL) {
            return R.string.admin_complaint_decision_cancel_deal;
        }
        if (decision == ComplaintDecision.REJECT) {
            return R.string.admin_complaint_decision_reject;
        }
        return R.string.admin_complaint_decision_request_evidence;
    }

    private static String readText(@Nullable Editable text) {
        return text == null ? "" : text.toString().trim();
    }

    private void observeDone() {
        complaintViewModel.getDone().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean finished) {
                if (!Boolean.TRUE.equals(finished)) {
                    return;
                }
                complaintViewModel.clearDone();
                int message = savingNote ? R.string.admin_complaint_note_saved : R.string.admin_complaint_decision_sent;
                Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    // D06: a decided or withdrawn complaint shows its result and no buttons; evidence is asked only while OPEN.
    private void showDecisionState(@NonNull Complaint complaint) {
        boolean open = complaint.isOpen();
        binding.layoutDecisionButtons.setVisibility(open ? View.VISIBLE : View.GONE);
        binding.buttonRequestEvidence.setEnabled(complaint.getStatus() == ComplaintStatus.OPEN);
        String request = complaint.getEvidenceRequest();
        boolean waiting = complaint.getStatus() == ComplaintStatus.NEEDS_EVIDENCE
                && request != null && !request.isEmpty();
        binding.textEvidenceRequest.setVisibility(waiting ? View.VISIBLE : View.GONE);
        if (waiting) {
            binding.textEvidenceRequest.setText(getString(R.string.admin_complaint_decision_waiting, request));
        }
        binding.textDecisionResult.setVisibility(open ? View.GONE : View.VISIBLE);
        if (!open) {
            binding.textDecisionResult.setText(resultText(complaint));
        }
    }

    private String resultText(@NonNull Complaint complaint) {
        if (complaint.getStatus() == ComplaintStatus.WITHDRAWN) {
            return getString(R.string.admin_complaint_decision_withdrawn);
        }
        String decision = complaint.getDecision() != null ? getString(decisionLabel(complaint.getDecision())) : "";
        String message = complaint.getResolutionMessage() != null ? complaint.getResolutionMessage() : "";
        return getString(R.string.admin_complaint_decision_result, decision, message);
    }

    private void setDecisionEnabled(boolean enabled) {
        binding.buttonDecideRefund.setEnabled(enabled);
        binding.buttonDecideReturnItem.setEnabled(enabled);
        binding.buttonDecideCancelDeal.setEnabled(enabled);
        binding.buttonDecideReject.setEnabled(enabled);
        binding.buttonSaveNote.setEnabled(enabled);
    }

    // The note is internal (STT 90): it is filled once from the admin-only sub-document, never shown to users.
    private void fillNoteOnce(@NonNull Complaint complaint) {
        if (noteFilled) {
            return;
        }
        noteFilled = true;
        String note = complaint.getAdminNote();
        binding.editAdminNote.setText(note != null ? note : "");
    }

    private void setUpToolbar() {
        String code = complaintId != null ? complaintId : "";
        binding.includeToolbar.textTitle.setText(getString(R.string.admin_complaint_code, code));
        binding.includeToolbar.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                NavHostFragment.findNavController(AdminComplaintDetailFragment.this).navigateUp();
            }
        });
    }

    private void setUpSections() {
        evidenceAdapter = new EvidenceImageAdapter();
        binding.recyclerEvidence.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerEvidence.setAdapter(evidenceAdapter);
        binding.includeErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                complaintViewModel.loadDetail(complaintId);
            }
        });
        binding.buttonChatHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                showChatDialog();
            }
        });
        // Choosing a priority saves it at once (D06).
        binding.chipGroupPriority.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                onPriorityPicked();
            }
        });
    }

    private void onPriorityPicked() {
        Complaint complaint = complaintViewModel.getSelected().getValue();
        Priority picked = checkedPriority();
        if (showingPriority || complaint == null || picked == null || picked == complaint.getPriority()) {
            return;
        }
        complaintViewModel.setPriority(complaintId, picked);
    }

    @Nullable
    private Priority checkedPriority() {
        int checkedId = binding.chipGroupPriority.getCheckedChipId();
        if (checkedId == R.id.chipPriorityLow) {
            return Priority.LOW;
        }
        if (checkedId == R.id.chipPriorityHigh) {
            return Priority.HIGH;
        }
        if (checkedId == R.id.chipPriorityMedium) {
            return Priority.MEDIUM;
        }
        return null;
    }

    private void observeViewModel() {
        complaintViewModel.getSelected().observe(getViewLifecycleOwner(), new Observer<Complaint>() {
            @Override
            public void onChanged(Complaint complaint) {
                showComplaint(complaint);
            }
        });
        complaintViewModel.getDeal().observe(getViewLifecycleOwner(), new Observer<Deal>() {
            @Override
            public void onChanged(Deal deal) {
                showDeal(deal);
            }
        });
        complaintViewModel.getMessages().observe(getViewLifecycleOwner(), new Observer<List<ChatMessage>>() {
            @Override
            public void onChanged(List<ChatMessage> messages) {
                showMessageCount(messages);
            }
        });
        complaintViewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                updateState();
            }
        });
        complaintViewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                showError(message);
            }
        });
    }

    private void updateState() {
        boolean loading = Boolean.TRUE.equals(complaintViewModel.getLoading().getValue());
        boolean hasComplaint = complaintViewModel.getSelected().getValue() != null;
        binding.progressDetail.setVisibility(loading && !hasComplaint ? View.VISIBLE : View.GONE);
        binding.scrollContent.setVisibility(hasComplaint ? View.VISIBLE : View.GONE);
        binding.includeErrorState.getRoot().setVisibility(!hasComplaint && !loading ? View.VISIBLE : View.GONE);
        binding.chipGroupPriority.setEnabled(!loading);
        setDecisionEnabled(!loading);
    }

    private void showComplaint(@Nullable Complaint complaint) {
        updateState();
        if (complaint == null) {
            return;
        }
        StatusChips.bind(binding.includeStatusChip.textStatusChip, complaint.getStatus());
        String reason = getString(AdminComplaintAdapter.reasonLabel(complaint.getReason()));
        binding.textReason.setText(getString(R.string.admin_complaint_detail_reason, reason));
        String sent = AdminComplaintAdapter.formatDate(complaint.getCreatedAt());
        binding.textSentDate.setText(getString(R.string.admin_complaint_sent_on, sent));
        binding.textSentDate.setVisibility(sent.isEmpty() ? View.GONE : View.VISIBLE);
        String description = complaint.getDescription();
        boolean hasDescription = description != null && !description.trim().isEmpty();
        binding.textDescription.setText(hasDescription ? description
                : getString(R.string.admin_complaint_detail_no_description));
        showPriority(complaint.getPriority());
        List<String> imageIds = complaint.getEvidenceImageIds();
        evidenceAdapter.submitIds(imageIds);
        boolean noEvidence = imageIds == null || imageIds.isEmpty();
        binding.textNoEvidence.setVisibility(noEvidence ? View.VISIBLE : View.GONE);
        showComplainantRole();
        fillNoteOnce(complaint);
        showDecisionState(complaint);
    }

    private void showPriority(@Nullable Priority priority) {
        showingPriority = true;
        if (priority == Priority.LOW) {
            binding.chipGroupPriority.check(R.id.chipPriorityLow);
        } else if (priority == Priority.HIGH) {
            binding.chipGroupPriority.check(R.id.chipPriorityHigh);
        } else {
            binding.chipGroupPriority.check(R.id.chipPriorityMedium);
        }
        showingPriority = false;
    }

    private void showDeal(@Nullable Deal deal) {
        if (deal == null) {
            return;
        }
        binding.textDealTitle.setText(deal.getItemTitle());
        binding.textDealCode.setText(getString(R.string.admin_complaint_detail_deal_code, complaintId));
        FormatUtils formatUtils = new FormatUtils();
        binding.textDealPrice.setText(formatUtils.formatPrice(deal.getAgreedPrice()));
        StatusChips.bind(binding.includeDealStatusChip.textStatusChip, deal.getStatus());
        binding.textDealTimeline.setText(buildTimeline(deal.getStatusHistory()));
        showComplainantRole();
    }

    // The role needs both the complaint and the deal, whichever arrives last fills it in.
    private void showComplainantRole() {
        Complaint complaint = complaintViewModel.getSelected().getValue();
        Deal deal = complaintViewModel.getDeal().getValue();
        if (complaint == null || deal == null) {
            binding.textComplainant.setText(R.string.admin_complaint_detail_complainant_unknown);
            return;
        }
        boolean buyer = complaint.getComplainantId() != null
                && complaint.getComplainantId().equals(deal.getBuyerId());
        int role = roleLabel(buyer);
        binding.textComplainant.setText(getString(R.string.admin_complaint_detail_complainant, getString(role)));
    }

    private static int roleLabel(boolean buyer) {
        return buyer ? R.string.admin_complaint_detail_role_buyer : R.string.admin_complaint_detail_role_seller;
    }

    private String buildTimeline(@Nullable List<StatusChange> history) {
        if (history == null || history.isEmpty()) {
            return getString(R.string.admin_complaint_detail_no_timeline);
        }
        StringBuilder lines = new StringBuilder();
        for (StatusChange change : history) {
            if (lines.length() > 0) {
                lines.append('\n');
            }
            String when = AdminComplaintAdapter.formatDate(change.getChangedAt());
            String status = getString(dealStatusLabel(change.getToStatus()));
            lines.append(getString(R.string.admin_complaint_detail_timeline_row, when, status));
        }
        return lines.toString();
    }

    private static int dealStatusLabel(@Nullable DealStatus status) {
        if (status == DealStatus.CONFIRMED) {
            return R.string.common_status_deal_confirmed;
        }
        if (status == DealStatus.SHIPPED) {
            return R.string.common_status_deal_shipped;
        }
        if (status == DealStatus.RECEIVED) {
            return R.string.common_status_deal_received;
        }
        if (status == DealStatus.COMPLETED) {
            return R.string.common_status_deal_completed;
        }
        if (status == DealStatus.CANCELLED) {
            return R.string.common_status_deal_cancelled;
        }
        if (status == DealStatus.DISPUTED) {
            return R.string.common_status_deal_disputed;
        }
        return R.string.common_status_deal_pending_confirm;
    }

    private void showMessageCount(@Nullable List<ChatMessage> messages) {
        int count = messages != null ? messages.size() : 0;
        binding.buttonChatHistory.setText(getString(R.string.admin_complaint_detail_chat_count, count));
        binding.buttonChatHistory.setEnabled(count > 0);
    }

    // Read only: the admin sees who said what, never a reply box (D06).
    private void showChatDialog() {
        List<ChatMessage> messages = complaintViewModel.getMessages().getValue();
        Deal deal = complaintViewModel.getDeal().getValue();
        if (messages == null || messages.isEmpty() || deal == null) {
            return;
        }
        StringBuilder lines = new StringBuilder();
        for (ChatMessage message : messages) {
            if (lines.length() > 0) {
                lines.append("\n\n");
            }
            boolean fromBuyer = message.getSenderId() != null && message.getSenderId().equals(deal.getBuyerId());
            int role = roleLabel(fromBuyer);
            lines.append(getString(R.string.admin_complaint_detail_chat_row, getString(role), messageText(message)));
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.admin_complaint_detail_chat)
                .setMessage(lines.toString())
                .setPositiveButton(R.string.admin_complaint_detail_chat_close, null)
                .show();
    }

    private String messageText(ChatMessage message) {
        if (message.getType() == MessageType.IMAGE) {
            return getString(R.string.admin_complaint_detail_chat_image);
        }
        if (message.getType() == MessageType.OFFER) {
            return getString(R.string.admin_complaint_detail_chat_offer);
        }
        return message.getText();
    }

    private void showError(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        if (complaintViewModel.getSelected().getValue() == null) {
            binding.includeErrorState.textErrorMessage.setText(message);
        } else {
            Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
        }
        complaintViewModel.clearError();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Views are gone after this call, so the binding must not outlive them.
        binding = null;
    }

    /** Evidence thumbnails read by id through MediaRepository and drawn by ImageLoader. */
    private static final class EvidenceImageAdapter extends RecyclerView.Adapter<EvidenceImageAdapter.ImageHolder> {

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
            image.setContentDescription(parent.getContext().getString(R.string.admin_complaint_detail_evidence_image));
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
                        image.setImageResource(R.drawable.ic_error);
                    }
                });
            }
        }
    }
}
