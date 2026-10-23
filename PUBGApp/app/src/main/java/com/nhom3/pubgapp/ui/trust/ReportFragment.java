package com.nhom3.pubgapp.ui.trust;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.BundleCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentReportBinding;
import com.nhom3.pubgapp.model.Report;
import com.nhom3.pubgapp.model.enums.ReportReason;
import com.nhom3.pubgapp.model.enums.ReportTarget;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.util.SessionManager;
import com.nhom3.pubgapp.viewmodel.TrustViewModel;

import java.util.ArrayList;
import java.util.List;

/** D04: report a listing, a user or a message, with a reason, a description and up to three images. */
public class ReportFragment extends Fragment {

    private static final String ARG_TARGET_TYPE = "targetType";
    private static final String ARG_TARGET_ID = "targetId";
    private static final String ARG_CONVERSATION_ID = "conversationId";
    private static final String STATE_IMAGES = "evidenceImages";
    private static final String IMAGE_TYPE_PREFIX = "image/";
    private static final int MAX_IMAGES = 3;

    private FragmentReportBinding binding;
    private TrustViewModel trustViewModel;
    private EvidenceThumbAdapter evidenceAdapter;
    private ActivityResultLauncher<PickVisualMediaRequest> pickImage;
    // Picked images survive rotation through onSaveInstanceState; the radio and the text keep themselves by id.
    private ArrayList<Uri> restoredImages = new ArrayList<Uri>();
    private ReportTarget targetType;
    private String targetId;
    private String conversationId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickImage = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        onImagePicked(uri);
                    }
                });
        if (savedInstanceState != null) {
            ArrayList<Uri> saved = BundleCompat.getParcelableArrayList(savedInstanceState, STATE_IMAGES, Uri.class);
            if (saved != null) {
                restoredImages = saved;
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<Uri> images = restoredImages;
        if (evidenceAdapter != null) {
            images = new ArrayList<Uri>(evidenceAdapter.getUris());
        }
        outState.putParcelableArrayList(STATE_IMAGES, images);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentReportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!SessionManager.getInstance().isLoggedIn()) {
            NavHostFragment.findNavController(this).navigate(R.id.action_global_sign_in);
            return;
        }
        readArguments();
        trustViewModel = new ViewModelProvider(this).get(TrustViewModel.class);
        setUpToolbar();
        setUpEvidence();
        setUpForm();
        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding == null || trustViewModel == null) {
            return;
        }
        boolean online = NetworkMonitor.getInstance().isOnline();
        binding.includeOfflineBanner.getRoot().setVisibility(online ? View.GONE : View.VISIBLE);
        updateSubmitEnabled();
    }

    // An unknown type falls back to LISTING only for the title; the ViewModel and the Rules still check the report.
    private void readArguments() {
        Bundle arguments = getArguments();
        targetType = ReportTarget.LISTING;
        if (arguments == null) {
            return;
        }
        String type = arguments.getString(ARG_TARGET_TYPE);
        if (ReportTarget.USER.name().equals(type)) {
            targetType = ReportTarget.USER;
        } else if (ReportTarget.MESSAGE.name().equals(type)) {
            targetType = ReportTarget.MESSAGE;
        }
        targetId = arguments.getString(ARG_TARGET_ID);
        conversationId = arguments.getString(ARG_CONVERSATION_ID);
    }

    private void setUpToolbar() {
        int titleId = R.string.profile_report_title_listing;
        int summaryId = R.string.profile_report_target_listing;
        if (targetType == ReportTarget.USER) {
            titleId = R.string.profile_report_title_user;
            summaryId = R.string.profile_report_target_user;
        } else if (targetType == ReportTarget.MESSAGE) {
            titleId = R.string.profile_report_title_message;
            summaryId = R.string.profile_report_target_message;
        }
        binding.includeToolbar.textTitle.setText(titleId);
        binding.textTargetSummary.setText(summaryId);
        binding.includeToolbar.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                NavHostFragment.findNavController(ReportFragment.this).navigateUp();
            }
        });
    }

    private void setUpEvidence() {
        evidenceAdapter = new EvidenceThumbAdapter(MAX_IMAGES, new Runnable() {
            @Override
            public void run() {
                updateEvidenceState();
            }
        });
        evidenceAdapter.setUris(restoredImages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL, false);
        binding.recyclerEvidence.setLayoutManager(layoutManager);
        binding.recyclerEvidence.setAdapter(evidenceAdapter);
        binding.buttonAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                PickVisualMediaRequest request = new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build();
                pickImage.launch(request);
            }
        });
        updateEvidenceState();
    }

    private void setUpForm() {
        binding.radioGroupReason.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                updateSubmitEnabled();
            }
        });
        binding.buttonSubmitReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                submit();
            }
        });
    }

    // The picker can still hand back a non-image from some providers, so the type is checked here.
    private void onImagePicked(Uri uri) {
        if (uri == null || binding == null) {
            return;
        }
        String type = requireContext().getContentResolver().getType(uri);
        if (type == null || !type.startsWith(IMAGE_TYPE_PREFIX)) {
            Snackbar.make(binding.getRoot(), R.string.profile_report_not_image, Snackbar.LENGTH_LONG).show();
            return;
        }
        evidenceAdapter.addUri(uri);
        updateEvidenceState();
    }

    private void updateEvidenceState() {
        if (binding == null) {
            return;
        }
        boolean full = evidenceAdapter.isFull();
        binding.buttonAddImage.setEnabled(!full);
        binding.textEvidenceFull.setVisibility(full ? View.VISIBLE : View.GONE);
        binding.recyclerEvidence.setVisibility(evidenceAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
    }

    private ReportReason selectedReason() {
        int checkedId = binding.radioGroupReason.getCheckedRadioButtonId();
        if (checkedId == R.id.radioReasonFakeItem) {
            return ReportReason.FAKE_ITEM;
        }
        if (checkedId == R.id.radioReasonMisleadingInfo) {
            return ReportReason.MISLEADING_INFO;
        }
        if (checkedId == R.id.radioReasonScam) {
            return ReportReason.SCAM;
        }
        if (checkedId == R.id.radioReasonInappropriate) {
            return ReportReason.INAPPROPRIATE;
        }
        if (checkedId == R.id.radioReasonOther) {
            return ReportReason.OTHER;
        }
        return null;
    }

    private void updateSubmitEnabled() {
        if (binding == null) {
            return;
        }
        boolean loading = Boolean.TRUE.equals(trustViewModel.getLoading().getValue());
        boolean online = NetworkMonitor.getInstance().isOnline();
        boolean hasReason = selectedReason() != null;
        binding.buttonSubmitReport.setEnabled(hasReason && online && !loading);
        binding.progressSubmit.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.buttonAddImage.setEnabled(!loading && !evidenceAdapter.isFull());
    }

    // reporterId is not set here: the repository takes it from the signed-in session (R12).
    private void submit() {
        Report report = new Report();
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReason(selectedReason());
        report.setDescription(readDescription());
        if (targetType == ReportTarget.MESSAGE) {
            report.setConversationId(conversationId);
        }
        List<Uri> images = evidenceAdapter.getUris();
        trustViewModel.submitReport(report, images);
    }

    private String readDescription() {
        if (binding.editDescription.getText() == null) {
            return "";
        }
        return binding.editDescription.getText().toString().trim();
    }

    private void observeViewModel() {
        trustViewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                updateSubmitEnabled();
            }
        });
        trustViewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (message == null || message.isEmpty()) {
                    return;
                }
                Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
                trustViewModel.clearError();
            }
        });
        trustViewModel.getDone().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean finished) {
                if (!Boolean.TRUE.equals(finished)) {
                    return;
                }
                trustViewModel.clearDone();
                Toast.makeText(requireContext(), R.string.profile_report_sent, Toast.LENGTH_LONG).show();
                NavHostFragment.findNavController(ReportFragment.this).navigateUp();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (evidenceAdapter != null) {
            restoredImages = new ArrayList<Uri>(evidenceAdapter.getUris());
        }
        // The view is gone; keeping the binding would leak it.
        binding = null;
    }
}
