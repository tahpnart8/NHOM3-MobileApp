package com.nhom3.pubgapp.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentAdminComplaintListBinding;
import com.nhom3.pubgapp.model.Complaint;
import com.nhom3.pubgapp.model.enums.ComplaintStatus;
import com.nhom3.pubgapp.model.enums.Priority;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.viewmodel.AdminComplaintViewModel;

import java.util.List;

/** D06 list: complaints filtered by status and priority, high priority and newest first. */
public class AdminComplaintListFragment extends Fragment {

    private static final String ARG_COMPLAINT_ID = "complaintId";

    @Nullable
    private FragmentAdminComplaintListBinding binding;
    private AdminComplaintViewModel complaintViewModel;
    private AdminComplaintAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminComplaintListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        complaintViewModel = new ViewModelProvider(this).get(AdminComplaintViewModel.class);
        setUpToolbar();
        setUpList();
        setUpFilters();
        observeViewModel();
        loadSelected();
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

    private void setUpToolbar() {
        binding.includeToolbar.textTitle.setText(R.string.admin_complaint_list_title);
        binding.includeToolbar.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                NavHostFragment.findNavController(AdminComplaintListFragment.this).navigateUp();
            }
        });
    }

    private void setUpList() {
        adapter = new AdminComplaintAdapter(new AdminComplaintAdapter.OpenListener() {
            @Override
            public void onOpen(Complaint complaint) {
                openDetail(complaint);
            }
        });
        binding.recyclerComplaints.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerComplaints.setAdapter(adapter);
        binding.includeEmptyState.textEmptyTitle.setText(R.string.admin_complaint_list_empty_title);
        binding.includeEmptyState.textEmptyMessage.setText(R.string.admin_complaint_list_empty_message);
        binding.includeEmptyState.buttonEmptyAction.setVisibility(View.GONE);
        binding.includeErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                loadSelected();
            }
        });
    }

    // The chips keep their checked state by id across a rotation, so the same filter is read again.
    private void setUpFilters() {
        ChipGroup.OnCheckedStateChangeListener reload = new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                loadSelected();
            }
        };
        binding.chipGroupStatus.setOnCheckedStateChangeListener(reload);
        binding.chipGroupPriority.setOnCheckedStateChangeListener(reload);
    }

    private void loadSelected() {
        complaintViewModel.loadComplaints(selectedStatus(), selectedPriority());
    }

    private ComplaintStatus selectedStatus() {
        int checkedId = binding.chipGroupStatus.getCheckedChipId();
        if (checkedId == R.id.chipStatusNeedsEvidence) {
            return ComplaintStatus.NEEDS_EVIDENCE;
        }
        if (checkedId == R.id.chipStatusResolved) {
            return ComplaintStatus.RESOLVED;
        }
        if (checkedId == R.id.chipStatusWithdrawn) {
            return ComplaintStatus.WITHDRAWN;
        }
        return ComplaintStatus.OPEN;
    }

    // Null means every priority, as ComplaintRepository.getByStatus reads it.
    @Nullable
    private Priority selectedPriority() {
        int checkedId = binding.chipGroupPriority.getCheckedChipId();
        if (checkedId == R.id.chipPriorityHigh) {
            return Priority.HIGH;
        }
        if (checkedId == R.id.chipPriorityMedium) {
            return Priority.MEDIUM;
        }
        if (checkedId == R.id.chipPriorityLow) {
            return Priority.LOW;
        }
        return null;
    }

    private void openDetail(Complaint complaint) {
        Bundle args = new Bundle();
        args.putString(ARG_COMPLAINT_ID, complaint.getDealId());
        NavHostFragment.findNavController(this).navigate(R.id.action_admin_complaint_list_to_detail, args);
    }

    private void observeViewModel() {
        complaintViewModel.getComplaints().observe(getViewLifecycleOwner(), new Observer<List<Complaint>>() {
            @Override
            public void onChanged(List<Complaint> complaints) {
                adapter.submitList(complaints);
                updateState();
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
        List<Complaint> complaints = complaintViewModel.getComplaints().getValue();
        boolean hasList = complaints != null;
        boolean empty = hasList && complaints.isEmpty();
        boolean failed = !hasList && !loading;
        binding.progressComplaints.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.recyclerComplaints.setVisibility(hasList && !empty ? View.VISIBLE : View.GONE);
        binding.includeEmptyState.getRoot().setVisibility(empty && !loading ? View.VISIBLE : View.GONE);
        binding.includeErrorState.getRoot().setVisibility(failed ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        if (complaintViewModel.getComplaints().getValue() == null) {
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
}
