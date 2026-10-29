package com.nhom3.pubgapp.ui.trade;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentCheckoutProviderBinding;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.enums.PaymentMethod;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.util.SessionManager;
import com.nhom3.pubgapp.viewmodel.DealViewModel;

import java.util.List;

/** Screen C07, step 2: choose the bank or e-wallet of the simulated checkout. */
public class CheckoutProviderFragment extends Fragment {

    public static final String ARG_DEAL_ID = "dealId";
    public static final String ARG_METHOD = "method";
    public static final String ARG_SELLER_NAME = "sellerName";
    private static final String KEY_SELECTED_CODE = "key_checkout_selected_code";
    private static final String KEY_SELECTED_NAME = "key_checkout_selected_name";
    private static final int TILE_WIDTH_DP = 112;
    private static final int MIN_COLUMNS = 3;

    private FragmentCheckoutProviderBinding binding;
    private DealViewModel viewModel;
    private PaymentProviderAdapter adapter;
    private List<PaymentProvider> allProviders;
    private String dealId;
    private PaymentMethod method;
    private String sellerName;
    private String selectedCode;
    private String selectedName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCheckoutProviderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DealViewModel.class);
        readArguments();
        if (savedInstanceState != null) {
            selectedCode = savedInstanceState.getString(KEY_SELECTED_CODE);
            selectedName = savedInstanceState.getString(KEY_SELECTED_NAME);
        }
        setupToolbar();
        setupProviderGrid();
        setupSearch();
        setupButtons();
        observeViewModel();
        showSelection();
        loadDeal();
    }

    private void readArguments() {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }
        dealId = args.getString(ARG_DEAL_ID);
        method = CheckoutSummary.parseMethod(args.getString(ARG_METHOD));
        sellerName = args.getString(ARG_SELLER_NAME);
    }

    private void setupToolbar() {
        int titleRes = method == PaymentMethod.E_WALLET
                ? R.string.trade_checkout_provider_title_wallet
                : R.string.trade_checkout_provider_title_bank;
        binding.includeToolbar.textTitle.setText(titleRes);
        binding.includeToolbar.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(CheckoutProviderFragment.this).navigateUp();
            }
        });
    }

    // Cards and bank transfers pick a bank; the e-wallet method picks a wallet.
    private void setupProviderGrid() {
        int arrayRes = method == PaymentMethod.E_WALLET
                ? R.array.trade_checkout_wallets
                : R.array.trade_checkout_banks;
        allProviders = PaymentProvider.fromEntries(getResources().getStringArray(arrayRes));
        adapter = new PaymentProviderAdapter(new PaymentProviderAdapter.OnProviderClickListener() {
            @Override
            public void onProviderClick(PaymentProvider provider) {
                selectedCode = provider.getCode();
                selectedName = provider.getName();
                showSelection();
            }
        });
        int widthDp = getResources().getConfiguration().screenWidthDp;
        int columns = Math.max(MIN_COLUMNS, widthDp / TILE_WIDTH_DP);
        binding.recyclerProviders.setLayoutManager(new GridLayoutManager(requireContext(), columns));
        binding.recyclerProviders.setAdapter(adapter);
        binding.includeListEmpty.textEmptyTitle.setText(R.string.trade_checkout_provider_empty_title);
        binding.includeListEmpty.textEmptyMessage.setText(R.string.trade_checkout_provider_empty_message);
        binding.includeListEmpty.buttonEmptyAction.setVisibility(View.GONE);
        applyFilter("");
    }

    // The search box restores its own text after rotation, which runs this watcher again.
    private void setupSearch() {
        binding.editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable text) {
                applyFilter(text.toString());
            }
        });
    }

    private void applyFilter(String query) {
        List<PaymentProvider> visible = PaymentProvider.filter(allProviders, query);
        adapter.submitProviders(visible);
        boolean empty = visible.isEmpty();
        binding.recyclerProviders.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.includeListEmpty.layoutEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void showSelection() {
        adapter.setSelectedCode(selectedCode);
        boolean hasSelection = selectedCode != null && selectedName != null;
        binding.buttonContinue.setEnabled(hasSelection);
        binding.textSelectedProvider.setVisibility(hasSelection ? View.VISIBLE : View.GONE);
        if (hasSelection) {
            binding.textSelectedProvider.setText(getString(R.string.trade_checkout_provider_selected, selectedName));
        }
    }

    private void setupButtons() {
        binding.buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGateway();
            }
        });
        binding.includeErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDeal();
            }
        });
    }

    private void openGateway() {
        if (selectedCode == null || method == null) {
            return;
        }
        Bundle args = new Bundle();
        args.putString(CheckoutGatewayFragment.ARG_DEAL_ID, dealId);
        args.putString(CheckoutGatewayFragment.ARG_METHOD, method.name());
        args.putString(CheckoutGatewayFragment.ARG_SELLER_NAME, sellerName);
        args.putString(CheckoutGatewayFragment.ARG_PROVIDER_CODE, selectedCode);
        args.putString(CheckoutGatewayFragment.ARG_PROVIDER_NAME, selectedName);
        NavController navController = NavHostFragment.findNavController(CheckoutProviderFragment.this);
        navController.navigate(R.id.checkoutGatewayFragment, args);
    }

    private void loadDeal() {
        boolean online = NetworkMonitor.getInstance().isOnline();
        binding.includeOfflineBanner.layoutOfflineBanner.setVisibility(online ? View.GONE : View.VISIBLE);
        if (dealId == null || dealId.trim().isEmpty() || method == null) {
            showError(getString(R.string.trade_deal_payment_not_eligible));
            return;
        }
        binding.includeErrorState.layoutErrorState.setVisibility(View.GONE);
        viewModel.loadDeal(dealId);
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                boolean loading = isLoading != null && isLoading;
                binding.progressBarLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
            }
        });
        viewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (message != null && !message.isEmpty()) {
                    showError(message);
                    viewModel.clearError();
                }
            }
        });
        viewModel.getDeal().observe(getViewLifecycleOwner(), new Observer<Deal>() {
            @Override
            public void onChanged(Deal deal) {
                renderDeal(deal);
            }
        });
    }

    private void renderDeal(@Nullable Deal deal) {
        String uid = SessionManager.getInstance().getCurrentUserId();
        if (!CheckoutSummary.canPay(deal, uid)) {
            binding.scrollContent.setVisibility(View.GONE);
            binding.layoutBottomBar.setVisibility(View.GONE);
            binding.includeEmptyState.layoutEmptyState.setVisibility(View.VISIBLE);
            binding.includeEmptyState.textEmptyTitle.setText(R.string.trade_deal_payment_not_eligible);
            binding.includeEmptyState.buttonEmptyAction.setVisibility(View.GONE);
            return;
        }
        binding.includeEmptyState.layoutEmptyState.setVisibility(View.GONE);
        binding.includeErrorState.layoutErrorState.setVisibility(View.GONE);
        binding.scrollContent.setVisibility(View.VISIBLE);
        binding.layoutBottomBar.setVisibility(View.VISIBLE);
        CheckoutSummary.bind(binding.includeSummary, deal, sellerName);
    }

    private void showError(String message) {
        binding.scrollContent.setVisibility(View.GONE);
        binding.layoutBottomBar.setVisibility(View.GONE);
        binding.includeErrorState.layoutErrorState.setVisibility(View.VISIBLE);
        binding.includeErrorState.textErrorMessage.setText(message);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_SELECTED_CODE, selectedCode);
        outState.putString(KEY_SELECTED_NAME, selectedName);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
