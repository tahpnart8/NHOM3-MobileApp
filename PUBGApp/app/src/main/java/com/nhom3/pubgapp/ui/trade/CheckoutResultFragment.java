package com.nhom3.pubgapp.ui.trade;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentCheckoutResultBinding;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Payment;
import com.nhom3.pubgapp.model.enums.PaymentMethod;
import com.nhom3.pubgapp.util.FormatUtils;
import com.nhom3.pubgapp.util.SessionManager;
import com.nhom3.pubgapp.viewmodel.DealViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Screen C07, step 5: a short processing wait, then DealViewModel.pay, then the receipt or the error. */
public class CheckoutResultFragment extends Fragment {

    public static final String ARG_DEAL_ID = "dealId";
    public static final String ARG_METHOD = "method";
    public static final String ARG_PROVIDER_NAME = "providerName";
    private static final String KEY_PHASE = "key_checkout_result_phase";
    private static final String KEY_PAY_REQUESTED = "key_checkout_pay_requested";
    private static final int PHASE_PROCESSING = 1;
    private static final int PHASE_SUCCESS = 2;
    private static final int PHASE_ERROR = 3;
    private static final int PHASE_EMPTY = 4;
    private static final long PROCESSING_DELAY_MILLIS = 1500L;
    private static final String TIME_PATTERN = "dd/MM/yyyy HH:mm";

    private FragmentCheckoutResultBinding binding;
    private DealViewModel viewModel;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String dealId;
    private PaymentMethod method;
    private String providerName;
    private int phase = PHASE_PROCESSING;
    // True once pay() was sent, so a rotation never sends it twice.
    private boolean payRequested;

    private final Runnable payTask = new Runnable() {
        @Override
        public void run() {
            payRequested = true;
            viewModel.pay(method);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCheckoutResultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DealViewModel.class);
        readArguments();
        if (savedInstanceState != null) {
            phase = savedInstanceState.getInt(KEY_PHASE, PHASE_PROCESSING);
            payRequested = savedInstanceState.getBoolean(KEY_PAY_REQUESTED, false);
        }
        setupToolbarAndBack();
        setupButtons();
        observeViewModel();
        showPhase(phase);
        if (dealId == null || dealId.trim().isEmpty() || method == null) {
            showPhase(PHASE_EMPTY);
            return;
        }
        viewModel.loadDeal(dealId);
    }

    private void readArguments() {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }
        dealId = args.getString(ARG_DEAL_ID);
        method = CheckoutSummary.parseMethod(args.getString(ARG_METHOD));
        providerName = args.getString(ARG_PROVIDER_NAME);
    }

    private void setupToolbarAndBack() {
        binding.includeToolbar.textTitle.setText(R.string.trade_checkout_result_title);
        binding.includeToolbar.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleBack();
            }
        });
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }

    // Back never returns to the gateway: during processing it waits, after it leaves the checkout.
    private void handleBack() {
        if (phase == PHASE_PROCESSING) {
            return;
        }
        if (phase == PHASE_SUCCESS) {
            openDeal();
            return;
        }
        returnToMethods();
    }

    private void setupButtons() {
        binding.buttonViewDeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDeal();
            }
        });
        binding.includeErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retry();
            }
        });
        binding.buttonBackToMethods.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToMethods();
            }
        });
        binding.includeEmptyState.buttonEmptyAction.setVisibility(View.GONE);
        binding.includeEmptyState.textEmptyTitle.setText(R.string.trade_deal_payment_not_eligible);
    }

    private void retry() {
        payRequested = false;
        showPhase(PHASE_PROCESSING);
        if (viewModel.getDeal().getValue() == null) {
            viewModel.loadDeal(dealId);
            return;
        }
        schedulePay();
    }

    private void schedulePay() {
        handler.removeCallbacks(payTask);
        handler.postDelayed(payTask, PROCESSING_DELAY_MILLIS);
    }

    private void observeViewModel() {
        viewModel.getDeal().observe(getViewLifecycleOwner(), new Observer<Deal>() {
            @Override
            public void onChanged(Deal deal) {
                onDealLoaded(deal);
            }
        });
        viewModel.getPayment().observe(getViewLifecycleOwner(), new Observer<Payment>() {
            @Override
            public void onChanged(Payment payment) {
                onPaymentChanged(payment);
            }
        });
        viewModel.getDone().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isDone) {
                if (isDone != null && isDone) {
                    viewModel.clearDone();
                    showPhase(PHASE_SUCCESS);
                }
            }
        });
        viewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (message != null && !message.isEmpty()) {
                    viewModel.clearError();
                    showError(message);
                }
            }
        });
    }

    private void onDealLoaded(@Nullable Deal deal) {
        if (phase == PHASE_SUCCESS) {
            renderReceipt();
            return;
        }
        if (phase != PHASE_PROCESSING || payRequested) {
            return;
        }
        String uid = SessionManager.getInstance().getCurrentUserId();
        if (!CheckoutSummary.canPay(deal, uid)) {
            showPhase(PHASE_EMPTY);
            return;
        }
        schedulePay();
    }

    // A held payment after our request means it went through, even if the answer was lost in a restart.
    private void onPaymentChanged(@Nullable Payment payment) {
        boolean held = payment != null && payment.isHeld();
        if (held && payRequested && phase != PHASE_SUCCESS) {
            showPhase(PHASE_SUCCESS);
            return;
        }
        if (phase == PHASE_SUCCESS) {
            renderReceipt();
        }
    }

    private void showError(String message) {
        if (phase == PHASE_SUCCESS) {
            return;
        }
        String text = message + "\n" + getString(R.string.trade_checkout_result_error);
        binding.includeErrorState.textErrorMessage.setText(text);
        showPhase(PHASE_ERROR);
    }

    private void showPhase(int newPhase) {
        phase = newPhase;
        binding.layoutProcessing.setVisibility(newPhase == PHASE_PROCESSING ? View.VISIBLE : View.GONE);
        binding.scrollReceipt.setVisibility(newPhase == PHASE_SUCCESS ? View.VISIBLE : View.GONE);
        binding.layoutBottomBar.setVisibility(newPhase == PHASE_SUCCESS ? View.VISIBLE : View.GONE);
        binding.includeErrorState.layoutErrorState.setVisibility(newPhase == PHASE_ERROR ? View.VISIBLE : View.GONE);
        binding.buttonBackToMethods.setVisibility(newPhase == PHASE_ERROR ? View.VISIBLE : View.GONE);
        binding.includeEmptyState.layoutEmptyState.setVisibility(newPhase == PHASE_EMPTY ? View.VISIBLE : View.GONE);
        binding.includeToolbar.buttonBack.setEnabled(newPhase != PHASE_PROCESSING);
        if (newPhase == PHASE_SUCCESS) {
            renderReceipt();
        }
    }

    private void renderReceipt() {
        Deal deal = viewModel.getDeal().getValue();
        Payment payment = viewModel.getPayment().getValue();
        long amount = deal != null ? deal.getAgreedPrice() : 0L;
        binding.textReceiptAmount.setText(new FormatUtils().formatPrice(amount));
        binding.textReceiptMethod.setText(CheckoutSummary.getMethodStringRes(method));
        binding.textReceiptProvider.setText(providerName != null ? providerName : "");
        binding.textReceiptCode.setText(getString(R.string.trade_payment_deal_code, dealId));
        Date heldAt = new Date();
        if (payment != null && payment.getHeldAt() != null) {
            heldAt = payment.getHeldAt().toDate();
        }
        SimpleDateFormat format = new SimpleDateFormat(TIME_PATTERN, Locale.getDefault());
        binding.textReceiptTime.setText(format.format(heldAt));
    }

    private void openDeal() {
        NavController navController = NavHostFragment.findNavController(CheckoutResultFragment.this);
        boolean found = navController.popBackStack(R.id.dealDetailFragment, false);
        if (!found) {
            Bundle args = new Bundle();
            args.putString(ARG_DEAL_ID, dealId);
            navController.navigate(R.id.dealDetailFragment, args);
        }
    }

    private void returnToMethods() {
        NavController navController = NavHostFragment.findNavController(CheckoutResultFragment.this);
        boolean popped = navController.popBackStack(R.id.dealPaymentFragment, false);
        if (!popped) {
            navController.navigateUp();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_PHASE, phase);
        outState.putBoolean(KEY_PAY_REQUESTED, payRequested);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(payTask);
        binding = null;
    }
}
