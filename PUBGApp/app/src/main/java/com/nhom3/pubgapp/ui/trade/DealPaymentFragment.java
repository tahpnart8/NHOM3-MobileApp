package com.nhom3.pubgapp.ui.trade;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.color.MaterialColors;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.UserRepository;
import com.nhom3.pubgapp.databinding.FragmentDealPaymentBinding;
import com.nhom3.pubgapp.databinding.ViewCheckoutMethodBinding;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Payment;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.PaymentMethod;
import com.nhom3.pubgapp.model.enums.PaymentStatus;
import com.nhom3.pubgapp.util.FormatUtils;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.util.SessionManager;
import com.nhom3.pubgapp.viewmodel.DealViewModel;

import java.text.SimpleDateFormat;
import java.util.Locale;

/** Screen C07: choose the payment method, then the simulated checkout continues on its own screens. */
public class DealPaymentFragment extends Fragment {

    private static final String KEY_CURRENT_STEP = "key_payment_current_step";
    private static final String KEY_SELECTED_METHOD = "key_payment_selected_method";
    private static final String KEY_SELLER_NAME = "key_payment_seller_name";
    private static final int STEP_CHOOSE = 1;
    private static final int STEP_RECEIPT = 3;

    private FragmentDealPaymentBinding binding;
    private DealViewModel viewModel;
    private String dealId;
    private int currentStep = STEP_CHOOSE;
    private PaymentMethod selectedMethod;
    private String sellerName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDealPaymentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DealViewModel.class);

        if (getArguments() != null) {
            dealId = getArguments().getString("dealId");
        }

        setupToolbar();
        setupMethodCards();
        setupStep1Controls();
        setupStep3Controls();
        observeViewModel();

        // Coming back from a later checkout step recreates the view but keeps the fields.
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }
        selectMethod(selectedMethod);
        showStep(currentStep);

        loadData();
    }

    private void setupToolbar() {
        binding.includeToolbar.textTitle.setText(R.string.trade_deal_payment_heading);
        binding.includeToolbar.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateBackToDeal();
            }
        });
    }

    private void navigateBackToDeal() {
        NavController navController = NavHostFragment.findNavController(DealPaymentFragment.this);
        navController.navigateUp();
    }

    private void loadData() {
        boolean online = NetworkMonitor.getInstance().isOnline();
        if (!online) {
            binding.includeOfflineBanner.layoutOfflineBanner.setVisibility(View.VISIBLE);
        } else {
            binding.includeOfflineBanner.layoutOfflineBanner.setVisibility(View.GONE);
        }

        if (dealId != null && !dealId.trim().isEmpty()) {
            viewModel.loadDeal(dealId);
        }
    }

    private void setupMethodCards() {
        bindMethodCard(binding.includeMethodCard, PaymentMethod.CARD, R.drawable.ic_payment_card,
                R.string.trade_deal_payment_method_card_hint);
        bindMethodCard(binding.includeMethodWallet, PaymentMethod.E_WALLET, R.drawable.ic_payment_wallet,
                R.string.trade_deal_payment_method_wallet_hint);
        bindMethodCard(binding.includeMethodTransfer, PaymentMethod.BANK_TRANSFER, R.drawable.ic_payment_bank,
                R.string.trade_deal_payment_method_transfer_hint);
    }

    private void bindMethodCard(ViewCheckoutMethodBinding card, final PaymentMethod method,
                                int iconRes, int hintRes) {
        card.imageMethodIcon.setImageResource(iconRes);
        card.textMethodTitle.setText(CheckoutSummary.getMethodStringRes(method));
        card.textMethodHint.setText(hintRes);
        card.cardMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectMethod(method);
            }
        });
    }

    private void selectMethod(@Nullable PaymentMethod method) {
        selectedMethod = method;
        markMethodCard(binding.includeMethodCard, method == PaymentMethod.CARD);
        markMethodCard(binding.includeMethodWallet, method == PaymentMethod.E_WALLET);
        markMethodCard(binding.includeMethodTransfer, method == PaymentMethod.BANK_TRANSFER);
        binding.buttonContinue.setEnabled(method != null);
    }

    private void markMethodCard(ViewCheckoutMethodBinding card, boolean checked) {
        card.cardMethod.setChecked(checked);
        int strokeAttr = checked ? androidx.appcompat.R.attr.colorPrimary
                : com.google.android.material.R.attr.colorOutlineVariant;
        card.cardMethod.setStrokeColor(MaterialColors.getColor(card.cardMethod, strokeAttr));
    }

    private void setupStep1Controls() {
        binding.buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProviderStep();
            }
        });
    }

    private void openProviderStep() {
        if (selectedMethod == null || dealId == null) {
            return;
        }
        Bundle args = new Bundle();
        args.putString(CheckoutProviderFragment.ARG_DEAL_ID, dealId);
        args.putString(CheckoutProviderFragment.ARG_METHOD, selectedMethod.name());
        args.putString(CheckoutProviderFragment.ARG_SELLER_NAME, sellerName);
        NavController navController = NavHostFragment.findNavController(DealPaymentFragment.this);
        navController.navigate(R.id.checkoutProviderFragment, args);
    }

    private void setupStep3Controls() {
        binding.buttonReturnToDeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateBackToDeal();
            }
        });
    }

    private void showStep(int step) {
        currentStep = step;
        binding.layoutStep1.setVisibility(step == STEP_CHOOSE ? View.VISIBLE : View.GONE);
        binding.layoutBottomBar.setVisibility(step == STEP_CHOOSE ? View.VISIBLE : View.GONE);
        binding.layoutStep3.setVisibility(step == STEP_RECEIPT ? View.VISIBLE : View.GONE);

        if (step == STEP_RECEIPT) {
            updateStep3Views();
        }
    }

    private void updateStep3Views() {
        Deal deal = viewModel.getDeal().getValue();
        Payment payment = viewModel.getPayment().getValue();

        long amount = deal != null ? deal.getAgreedPrice() : 0;
        binding.textReceiptAmount.setText(new FormatUtils().formatPrice(amount));

        PaymentMethod method = payment != null && payment.getMethod() != null
                ? payment.getMethod() : selectedMethod;
        int methodRes = CheckoutSummary.getMethodStringRes(method);
        binding.textReceiptMethod.setText(getString(R.string.trade_deal_payment_method_label,
                getString(methodRes)));

        if (payment != null && payment.getHeldAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            binding.textReceiptTime.setText(getString(R.string.trade_deal_payment_time_label,
                    sdf.format(payment.getHeldAt().toDate())));
        } else {
            binding.textReceiptTime.setText(getString(R.string.trade_deal_payment_time_label,
                    getString(R.string.trade_deal_payment_just_now)));
        }

        int statusRes = R.string.trade_deal_payment_status_held;
        if (payment != null && payment.getStatus() == PaymentStatus.RELEASED) {
            statusRes = R.string.trade_deal_payment_status_released;
        } else if (payment != null && payment.getStatus() == PaymentStatus.REFUNDED) {
            statusRes = R.string.trade_deal_payment_status_refunded;
        }
        binding.textReceiptStatus.setText(getString(R.string.trade_deal_payment_status_label,
                getString(statusRes)));
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                boolean loading = isLoading != null && isLoading;
                binding.progressBarLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
                binding.buttonContinue.setEnabled(!loading && selectedMethod != null);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String errorMessage) {
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    viewModel.clearError();
                }
            }
        });

        viewModel.getDeal().observe(getViewLifecycleOwner(), new Observer<Deal>() {
            @Override
            public void onChanged(Deal deal) {
                if (deal != null) {
                    renderDeal(deal);
                } else {
                    renderEmptyState();
                }
            }
        });

        viewModel.getPayment().observe(getViewLifecycleOwner(), new Observer<Payment>() {
            @Override
            public void onChanged(Payment payment) {
                if (payment != null && payment.isHeld()) {
                    showStep(STEP_RECEIPT);
                }
            }
        });
    }

    private void renderEmptyState() {
        binding.includeEmptyState.layoutEmptyState.setVisibility(View.VISIBLE);
        binding.includeEmptyState.textEmptyTitle.setText(R.string.trade_deal_payment_not_eligible);
        binding.scrollContent.setVisibility(View.GONE);
        binding.layoutBottomBar.setVisibility(View.GONE);
    }

    private void renderDeal(Deal deal) {
        String currentUid = getCurrentUserId();
        if (!currentUid.equals(deal.getBuyerId())) {
            binding.includeErrorState.layoutErrorState.setVisibility(View.VISIBLE);
            binding.includeErrorState.textErrorMessage.setText(R.string.trade_deal_payment_not_buyer);
            binding.scrollContent.setVisibility(View.GONE);
            binding.layoutBottomBar.setVisibility(View.GONE);
            return;
        }

        boolean alreadyPaid = currentStep == STEP_RECEIPT;
        if (!alreadyPaid && !CheckoutSummary.canPay(deal, currentUid)) {
            renderEmptyState();
            return;
        }

        binding.includeErrorState.layoutErrorState.setVisibility(View.GONE);
        binding.includeEmptyState.layoutEmptyState.setVisibility(View.GONE);
        binding.scrollContent.setVisibility(View.VISIBLE);
        binding.layoutBottomBar.setVisibility(currentStep == STEP_CHOOSE ? View.VISIBLE : View.GONE);

        CheckoutSummary.bind(binding.includeSummary, deal, sellerName);
        loadSellerName(deal);
    }

    // Same lookup as C03 uses for the partner name; the name is display only.
    private void loadSellerName(final Deal deal) {
        if (sellerName != null || deal.getSellerId() == null || deal.getSellerId().isEmpty()) {
            return;
        }
        new UserRepository().getProfile(deal.getSellerId(), new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (binding == null || user == null || user.getDisplayName() == null) {
                    return;
                }
                sellerName = user.getDisplayName().trim();
                CheckoutSummary.bind(binding.includeSummary, deal, sellerName);
            }

            @Override
            public void onError(String message) {
                sellerName = null;
            }
        });
    }

    private String getCurrentUserId() {
        String uid = SessionManager.getInstance().getCurrentUserId();
        return uid != null ? uid : "";
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_STEP, currentStep);
        outState.putString(KEY_SELLER_NAME, sellerName);
        if (selectedMethod != null) {
            outState.putString(KEY_SELECTED_METHOD, selectedMethod.name());
        }
    }

    private void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        currentStep = savedInstanceState.getInt(KEY_CURRENT_STEP, STEP_CHOOSE);
        sellerName = savedInstanceState.getString(KEY_SELLER_NAME);
        String methodName = savedInstanceState.getString(KEY_SELECTED_METHOD);
        selectedMethod = CheckoutSummary.parseMethod(methodName);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
