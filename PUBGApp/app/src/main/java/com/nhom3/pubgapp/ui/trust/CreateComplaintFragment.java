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
import com.nhom3.pubgapp.databinding.FragmentCreateComplaintBinding;
import com.nhom3.pubgapp.model.Complaint;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.enums.ComplaintReason;
import com.nhom3.pubgapp.util.FormatUtils;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.util.SessionManager;
import com.nhom3.pubgapp.viewmodel.DealViewModel;
import com.nhom3.pubgapp.viewmodel.TrustViewModel;

import java.util.ArrayList;
import java.util.List;

/** D05: a party of a deal files a complaint with a reason, a description and 1 to 5 evidence images. */
public class CreateComplaintFragment extends Fragment {

    private static final String ARG_DEAL_ID = "dealId";
    private static final String STATE_IMAGES = "complaintEvidenceImages";
    private static final String IMAGE_TYPE_PREFIX = "image/";
    // STT 64 and trang-thai.md section 6: at least one image, at most five.
    private static final int MAX_IMAGES = 5;

    @Nullable
    private FragmentCreateComplaintBinding binding;
    private TrustViewModel trustViewModel;
    private DealViewModel dealViewModel;
    private EvidenceThumbAdapter evidenceAdapter;
    private ActivityResultLauncher<PickVisualMediaRequest> pickImage;
    // Picked images survive rotation through onSaveInstanceState; the radio and the text keep themselves by id.
    private ArrayList<Uri> restoredImages = new ArrayList<Uri>();
    private String dealId;

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
        binding = FragmentCreateComplaintBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!SessionManager.getInstance().isLoggedIn()) {
            NavHostFragment.findNavController(this).navigate(R.id.action_global_sign_in);
            return;
        }
        Bundle arguments = getArguments();
        dealId = arguments != null ? arguments.getString(ARG_DEAL_ID) : null;
        trustViewModel = new ViewModelProvider(this).get(TrustViewModel.class);
        dealViewModel = new ViewModelProvider(this).get(DealViewModel.class);
        setUpToolbar();
        setUpEvidence();
        setUpForm();
        observeDeal();
        observeViewModel();
        if (dealViewModel.getDeal().getValue() == null) {
            dealViewModel.loadDeal(dealId);
        }
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

    private void setUpToolbar() {
        binding.includeToolbar.textTitle.setText(R.string.profile_create_complaint_toolbar);
        binding.includeToolbar.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                NavHostFragment.findNavController(CreateComplaintFragment.this).navigateUp();
            }
        });
        // The full id, as C03 shows it, so the user can match the two screens.
        String code = dealId != null ? dealId : "";
        binding.textDealCode.setText(getString(R.string.profile_create_complaint_deal_code, code));
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
        binding.buttonSubmitComplaint.setOnClickListener(new View.OnClickListener() {
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
        binding.textEvidenceFull.setVisibility(full ? View.VISIBLE : View.GONE);
        binding.recyclerEvidence.setVisibility(evidenceAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
        updateSubmitEnabled();
    }

    private ComplaintReason selectedReason() {
        int checkedId = binding.radioGroupReason.getCheckedRadioButtonId();
        if (checkedId == R.id.radioReasonNotAsDescribed) {
            return ComplaintReason.NOT_AS_DESCRIBED;
        }
        if (checkedId == R.id.radioReasonNotReceived) {
            return ComplaintReason.NOT_RECEIVED;
        }
        if (checkedId == R.id.radioReasonDamaged) {
            return ComplaintReason.DAMAGED;
        }
        if (checkedId == R.id.radioReasonOther) {
            return ComplaintReason.OTHER;
        }
        return null;
    }

    // D05: no image yet means the button cannot be pressed, since one image is the minimum.
    private void updateSubmitEnabled() {
        if (binding == null || trustViewModel == null) {
            return;
        }
        boolean loading = Boolean.TRUE.equals(trustViewModel.getLoading().getValue());
        boolean online = NetworkMonitor.getInstance().isOnline();
        boolean hasReason = selectedReason() != null;
        boolean hasImage = evidenceAdapter.getItemCount() > 0;
        binding.buttonSubmitComplaint.setEnabled(hasReason && hasImage && online && !loading);
        binding.progressSubmit.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.buttonAddImage.setEnabled(!loading && !evidenceAdapter.isFull());
    }

    // complainantId is not set here: the repository takes it from the signed-in session (R12).
    private void submit() {
        Complaint complaint = new Complaint();
        complaint.setDealId(dealId);
        complaint.setReason(selectedReason());
        complaint.setDescription(readDescription());
        List<Uri> images = evidenceAdapter.getUris();
        trustViewModel.submitComplaint(complaint, images);
    }

    private String readDescription() {
        if (binding.editDescription.getText() == null) {
            return "";
        }
        return binding.editDescription.getText().toString().trim();
    }

    private void observeDeal() {
        dealViewModel.getDeal().observe(getViewLifecycleOwner(), new Observer<Deal>() {
            @Override
            public void onChanged(Deal deal) {
                showDeal(deal);
            }
        });
    }

    private void showDeal(Deal deal) {
        if (deal == null) {
            return;
        }
        binding.textDealTitle.setText(deal.getItemTitle());
        FormatUtils formatUtils = new FormatUtils();
        binding.textDealPrice.setText(formatUtils.formatPrice(deal.getAgreedPrice()));
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
                Toast.makeText(requireContext(), R.string.profile_create_complaint_sent, Toast.LENGTH_LONG).show();
                NavHostFragment.findNavController(CreateComplaintFragment.this).navigateUp();
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
