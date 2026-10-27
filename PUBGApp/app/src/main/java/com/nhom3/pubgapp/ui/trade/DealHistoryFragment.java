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
import com.google.android.material.tabs.TabLayout;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentDealHistoryBinding;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Payment;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.viewmodel.DealViewModel;

import java.util.List;

/** Screen C04: Deal history list with role and status filters. */
public class DealHistoryFragment extends Fragment {

    private static final String ARG_DEAL_ID = "dealId";
    private static final String STATE_SELECTED_TAB = "state_selected_tab";
    private static final String STATE_SELECTED_FILTER = "state_selected_filter";
    public static final int TAB_DEALS = 0;
    public static final int TAB_PAYMENTS = 1;

    @Nullable
    private FragmentDealHistoryBinding binding;
    private DealViewModel viewModel;
    private DealHistoryAdapter adapter;
    private PaymentHistoryAdapter paymentAdapter;
    private int currentTab = TAB_DEALS;
    private int currentFilter = DealHistoryAdapter.FILTER_ALL;

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

        if (savedInstanceState != null) {
            currentTab = savedInstanceState.getInt(STATE_SELECTED_TAB, TAB_DEALS);
            currentFilter = savedInstanceState.getInt(STATE_SELECTED_FILTER, DealHistoryAdapter.FILTER_ALL);
        }

        setUpToolbar();
        setUpTabs();
        setUpList();
        setUpFilterChips();
        observeViewModel();

        if (currentTab == TAB_DEALS) {
            viewModel.loadHistory();
        } else {
            viewModel.loadPayments();
        }
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_TAB, currentTab);
        outState.putInt(STATE_SELECTED_FILTER, currentFilter);
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

    private void setUpTabs() {
        if (binding == null) {
            return;
        }
        TabLayout.Tab tab = binding.tabLayoutHistory.getTabAt(currentTab);
        if (tab != null) {
            tab.select();
        }
        applyTabSelection(currentTab);

        binding.tabLayoutHistory.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab selected) {
                currentTab = selected.getPosition();
                applyTabSelection(currentTab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab unselected) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab reselected) {
            }
        });
    }

    private void applyTabSelection(int tabIndex) {
        if (binding == null) {
            return;
        }
        boolean isDeals = tabIndex == TAB_DEALS;
        binding.scrollFilters.setVisibility(isDeals ? View.VISIBLE : View.GONE);
        binding.recyclerDeals.setVisibility(isDeals ? View.VISIBLE : View.GONE);
        binding.recyclerPayments.setVisibility(isDeals ? View.GONE : View.VISIBLE);

        if (isDeals) {
            binding.viewEmptyState.textEmptyTitle.setText(R.string.trade_deal_history_empty_title);
            binding.viewEmptyState.textEmptyMessage.setText(R.string.trade_deal_history_empty_message);
            if (viewModel.getDeals().getValue() == null) {
                viewModel.loadHistory();
            }
        } else {
            binding.viewEmptyState.textEmptyTitle.setText(R.string.trade_payment_history_empty_title);
            binding.viewEmptyState.textEmptyMessage.setText(R.string.trade_payment_history_empty_message);
            if (viewModel.getPayments().getValue() == null) {
                viewModel.loadPayments();
            }
        }
        updateState();
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

        paymentAdapter = new PaymentHistoryAdapter(new PaymentHistoryAdapter.OnPaymentClickListener() {
            @Override
            public void onPaymentClick(Payment payment) {
                if (payment == null || payment.getDealId() == null || payment.getDealId().isEmpty()) {
                    return;
                }
                Bundle args = new Bundle();
                args.putString(ARG_DEAL_ID, payment.getDealId());
                NavHostFragment.findNavController(DealHistoryFragment.this)
                        .navigate(R.id.action_global_deal_detail, args);
            }
        });
        binding.recyclerPayments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerPayments.setAdapter(paymentAdapter);

        binding.viewEmptyState.textEmptyTitle.setText(R.string.trade_deal_history_empty_title);
        binding.viewEmptyState.textEmptyMessage.setText(R.string.trade_deal_history_empty_message);
        binding.viewEmptyState.buttonEmptyAction.setVisibility(View.GONE);

        binding.viewErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                if (currentTab == TAB_DEALS) {
                    viewModel.loadHistory();
                } else {
                    viewModel.loadPayments();
                }
            }
        });
    }

    private void setUpFilterChips() {
        if (binding == null) {
            return;
        }
        int checkId = R.id.chipStatusAll;
        if (currentFilter == DealHistoryAdapter.FILTER_IN_PROGRESS) {
            checkId = R.id.chipStatusInProgress;
        } else if (currentFilter == DealHistoryAdapter.FILTER_COMPLETED) {
            checkId = R.id.chipStatusCompleted;
        } else if (currentFilter == DealHistoryAdapter.FILTER_CANCELLED) {
            checkId = R.id.chipStatusCancelled;
        }
        binding.chipGroupStatus.check(checkId);
        adapter.setFilter(currentFilter);

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
                currentFilter = selectedFilter;
                adapter.setFilter(currentFilter);
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
        viewModel.getPayments().observe(getViewLifecycleOwner(), new Observer<List<Payment>>() {
            @Override
            public void onChanged(List<Payment> payments) {
                if (paymentAdapter != null) {
                    paymentAdapter.submitPayments(payments);
                }
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
        boolean isDeals = currentTab == TAB_DEALS;
        boolean hasData;
        boolean empty;
        if (isDeals) {
            List<Deal> deals = viewModel.getDeals().getValue();
            hasData = deals != null;
            // Coming back from C03 the ViewModel still holds deals before setUpList creates the adapter.
            empty = hasData && adapter != null && adapter.getDisplayedItemCount() == 0;
        } else {
            List<Payment> payments = viewModel.getPayments().getValue();
            hasData = payments != null;
            empty = hasData && paymentAdapter != null && paymentAdapter.getItemCount() == 0;
        }
        boolean failed = !hasData && !loading;

        binding.progressDeals.setVisibility(loading && !hasData ? View.VISIBLE : View.GONE);
        if (isDeals) {
            binding.recyclerDeals.setVisibility(hasData && !empty ? View.VISIBLE : View.GONE);
            binding.recyclerPayments.setVisibility(View.GONE);
        } else {
            binding.recyclerDeals.setVisibility(View.GONE);
            binding.recyclerPayments.setVisibility(hasData && !empty ? View.VISIBLE : View.GONE);
        }
        binding.viewEmptyState.getRoot().setVisibility(empty && !loading ? View.VISIBLE : View.GONE);
        binding.viewErrorState.getRoot().setVisibility(failed ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        if (message == null || message.trim().isEmpty() || binding == null) {
            return;
        }
        boolean isDeals = currentTab == TAB_DEALS;
        boolean noData = isDeals
                ? viewModel.getDeals().getValue() == null
                : viewModel.getPayments().getValue() == null;
        if (noData) {
            binding.viewErrorState.textErrorMessage.setText(message);
        } else {
            Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
        }
        viewModel.clearError();
    }
}
