package com.nhom3.pubgapp.ui.trade;

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
import com.nhom3.pubgapp.databinding.FragmentDealHistoryBinding;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.viewmodel.DealViewModel;

import java.util.List;

/** Screen C04: Deal history list with role and status filters. */
public class DealHistoryFragment extends Fragment {

    private static final String ARG_DEAL_ID = "dealId";

    @Nullable
    private FragmentDealHistoryBinding binding;
    private DealViewModel viewModel;
    private DealHistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentDealHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DealViewModel.class);

        setUpToolbar();
        setUpList();
        setUpFilterChips();
        observeViewModel();

        viewModel.loadHistory();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding == null) {
            return;
        }
        boolean online = NetworkMonitor.getInstance().isOnline();
        binding.viewOfflineBanner.getRoot().setVisibility(online ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Views are gone after this call, so the binding must not outlive them.
        binding = null;
    }

    private void setUpToolbar() {
        if (binding == null) {
            return;
        }
        binding.includeToolbar.textTitle.setText(R.string.trade_deal_history_title);
        binding.includeToolbar.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                NavHostFragment.findNavController(DealHistoryFragment.this).navigateUp();
            }
        });
    }

    private void setUpList() {
        if (binding == null) {
            return;
        }
        adapter = new DealHistoryAdapter(new DealHistoryAdapter.OnDealClickListener() {
            @Override
            public void onDealClick(Deal deal) {
                Bundle args = new Bundle();
                args.putString(ARG_DEAL_ID, deal.getId());
                NavHostFragment.findNavController(DealHistoryFragment.this)
                        .navigate(R.id.action_global_deal_detail, args);
            }
        });
        binding.recyclerDeals.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerDeals.setAdapter(adapter);

        binding.viewEmptyState.textEmptyTitle.setText(R.string.trade_deal_history_empty_title);
        binding.viewEmptyState.textEmptyMessage.setText(R.string.trade_deal_history_empty_message);
        binding.viewEmptyState.buttonEmptyAction.setVisibility(View.GONE);

        binding.viewErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                viewModel.loadHistory();
            }
        });
    }

    private void setUpFilterChips() {
        if (binding == null) {
            return;
        }
        binding.chipGroupStatus.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                int selectedFilter = DealHistoryAdapter.FILTER_ALL;
                if (!checkedIds.isEmpty()) {
                    int checkedId = checkedIds.get(0);
                    if (checkedId == R.id.chipStatusInProgress) {
                        selectedFilter = DealHistoryAdapter.FILTER_IN_PROGRESS;
                    } else if (checkedId == R.id.chipStatusCompleted) {
                        selectedFilter = DealHistoryAdapter.FILTER_COMPLETED;
                    } else if (checkedId == R.id.chipStatusCancelled) {
                        selectedFilter = DealHistoryAdapter.FILTER_CANCELLED;
                    }
                }
                adapter.setFilter(selectedFilter);
                updateState();
            }
        });
    }

    private void observeViewModel() {
        viewModel.getDeals().observe(getViewLifecycleOwner(), new Observer<List<Deal>>() {
            @Override
            public void onChanged(List<Deal> deals) {
                adapter.submitDeals(deals);
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
        boolean hasData = deals != null;
        boolean empty = hasData && adapter.getDisplayedItemCount() == 0;
        boolean failed = !hasData && !loading;

        binding.progressDeals.setVisibility(loading && !hasData ? View.VISIBLE : View.GONE);
        binding.recyclerDeals.setVisibility(hasData && !empty ? View.VISIBLE : View.GONE);
        binding.viewEmptyState.getRoot().setVisibility(empty && !loading ? View.VISIBLE : View.GONE);
        binding.viewErrorState.getRoot().setVisibility(failed ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        if (message == null || message.trim().isEmpty() || binding == null) {
            return;
        }
        if (viewModel.getDeals().getValue() == null) {
            binding.viewErrorState.textErrorMessage.setText(message);
        } else {
            Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
        }
        viewModel.clearError();
    }
}
