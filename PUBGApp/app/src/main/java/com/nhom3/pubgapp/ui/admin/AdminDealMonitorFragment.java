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
import com.nhom3.pubgapp.databinding.FragmentAdminDealMonitorBinding;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.viewmodel.AdminDealViewModel;

import java.util.List;

// E06 deal monitor screen for admin.
public class AdminDealMonitorFragment extends Fragment {

    private static final String ARG_DEAL_ID = "dealId";

    @Nullable
    private FragmentAdminDealMonitorBinding binding;
    private AdminDealViewModel viewModel;
    private AdminDealAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminDealMonitorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AdminDealViewModel.class);

        setUpToolbar();
        setUpList();
        setUpFilters();
        observeViewModel();

        if (viewModel.getDeals().getValue() == null) {
            loadDeals();
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

    private void setUpToolbar() {
        if (binding == null) {
            return;
        }
        binding.includeToolbar.textTitle.setText(R.string.admin_deal_monitor_title);
        binding.includeToolbar.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(AdminDealMonitorFragment.this).navigateUp();
            }
        });
    }

    private void setUpList() {
        if (binding == null) {
            return;
        }
        adapter = new AdminDealAdapter(new AdminDealAdapter.OnDealClickListener() {
            @Override
            public void onDealClick(Deal deal) {
                openDetail(deal);
            }
        });
        binding.recyclerDeals.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerDeals.setAdapter(adapter);

        binding.includeEmptyState.textEmptyTitle.setText(R.string.admin_deal_monitor_empty_title);
        binding.includeEmptyState.textEmptyMessage.setText(R.string.admin_deal_monitor_empty_message);
        binding.includeEmptyState.buttonEmptyAction.setVisibility(View.GONE);

        binding.includeErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDeals();
            }
        });
    }

    private void setUpFilters() {
        if (binding == null) {
            return;
        }
        binding.chipGroupStatus.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                loadDeals();
            }
        });
    }

    @Nullable
    private DealStatus selectedStatus() {
        if (binding == null) {
            return null;
        }
        int checkedId = binding.chipGroupStatus.getCheckedChipId();
        if (checkedId == R.id.chipStatusPendingConfirm) {
            return DealStatus.PENDING_CONFIRM;
        }
        if (checkedId == R.id.chipStatusConfirmed) {
            return DealStatus.CONFIRMED;
        }
        if (checkedId == R.id.chipStatusShipped) {
            return DealStatus.SHIPPED;
        }
        if (checkedId == R.id.chipStatusReceived) {
            return DealStatus.RECEIVED;
        }
        if (checkedId == R.id.chipStatusCompleted) {
            return DealStatus.COMPLETED;
        }
        if (checkedId == R.id.chipStatusCancelled) {
            return DealStatus.CANCELLED;
        }
        if (checkedId == R.id.chipStatusDisputed) {
            return DealStatus.DISPUTED;
        }
        return null;
    }

    private void loadDeals() {
        viewModel.loadDeals(selectedStatus());
    }

    private void observeViewModel() {
        viewModel.getDeals().observe(getViewLifecycleOwner(), new Observer<List<Deal>>() {
            @Override
            public void onChanged(List<Deal> deals) {
                adapter.submitList(deals);
                updateState();
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                updateState();
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                showError(message);
            }
        });
    }

    private void updateState() {
        if (binding == null) {
            return;
        }
        boolean loading = Boolean.TRUE.equals(viewModel.getLoading().getValue());
        List<Deal> deals = viewModel.getDeals().getValue();
        boolean hasList = deals != null;
        boolean empty = hasList && deals.isEmpty();
        boolean failed = !hasList && !loading;

        binding.progressDeals.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.recyclerDeals.setVisibility(hasList && !empty ? View.VISIBLE : View.GONE);
        binding.includeEmptyState.getRoot().setVisibility(empty && !loading ? View.VISIBLE : View.GONE);
        binding.includeErrorState.getRoot().setVisibility(failed ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        if (binding == null || message == null || message.isEmpty()) {
            return;
        }
        if (viewModel.getDeals().getValue() == null) {
            binding.includeErrorState.textErrorMessage.setText(message);
        } else {
            Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
        }
        viewModel.clearError();
    }

    private void openDetail(Deal deal) {
        if (deal == null || deal.getId() == null) {
            return;
        }
        Bundle args = new Bundle();
        args.putString(ARG_DEAL_ID, deal.getId());
        NavHostFragment.findNavController(this).navigate(
                R.id.action_admin_deal_monitor_to_detail, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
