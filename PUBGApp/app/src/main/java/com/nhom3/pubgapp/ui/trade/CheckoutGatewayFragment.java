package com.nhom3.pubgapp.ui.trade;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.nhom3.pubgapp.databinding.FragmentCheckoutGatewayBinding;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.enums.PaymentMethod;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.util.SessionManager;
import com.nhom3.pubgapp.viewmodel.DealViewModel;

/** Screen C07, steps 3 and 4: the simulated PUBGApp gateway, then the test OTP. No real card data is asked. */
public class CheckoutGatewayFragment extends Fragment {

    public static final String ARG_DEAL_ID = "dealId";
    public static final String ARG_METHOD = "method";
    public static final String ARG_SELLER_NAME = "sellerName";
    public static final String ARG_PROVIDER_CODE = "providerCode";
    public static final String ARG_PROVIDER_NAME = "providerName";
    public static final String TEST_OTP = "123456";
    private static final String KEY_PHASE = "key_checkout_phase";
    private static final String KEY_DEADLINE = "key_checkout_deadline";
    private static final int PHASE_GATEWAY = 1;
    private static final int PHASE_OTP = 2;
    private static final long SESSION_MILLIS = 10L * 60L * 1000L;
    private static final long TICK_MILLIS = 1000L;
    private static final int QR_BITMAP_SIZE_PX = 580;

    private FragmentCheckoutGatewayBinding binding;
    private DealViewModel viewModel;
    private CountDownTimer timer;
    private String dealId;
    private PaymentMethod method;
    private String sellerName;
    private String providerName;
    private int phase = PHASE_GATEWAY;
    private long deadlineMillis;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCheckoutGatewayBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DealViewModel.class);
        readArguments();
        if (savedInstanceState != null) {
            phase = savedInstanceState.getInt(KEY_PHASE, PHASE_GATEWAY);
            deadlineMillis = savedInstanceState.getLong(KEY_DEADLINE, 0L);
        }
        if (deadlineMillis == 0L) {
            deadlineMillis = System.currentTimeMillis() + SESSION_MILLIS;
        }
        setupToolbar();
        setupMethodPanel();
        setupButtons();
        observeViewModel();
        showPhase(phase);
        loadDeal();
        startCountdown();
    }

    private void readArguments() {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }
        dealId = args.getString(ARG_DEAL_ID);
        method = CheckoutSummary.parseMethod(args.getString(ARG_METHOD));
        sellerName = args.getString(ARG_SELLER_NAME);
        providerName = args.getString(ARG_PROVIDER_NAME);
    }

    private void setupToolbar() {
        binding.includeToolbar.textTitle.setText(R.string.trade_checkout_gateway_title);
        binding.includeToolbar.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(CheckoutGatewayFragment.this).navigateUp();
            }
        });
    }

    private void setupMethodPanel() {
        String provider = providerName != null ? providerName : "";
        binding.textProvider.setText(getString(R.string.trade_checkout_gateway_provider_label, provider));
        binding.textCardBank.setText(provider);
        binding.textCardExpiry.setText(getString(R.string.trade_checkout_card_expiry_label,
                getString(R.string.trade_checkout_card_expiry)));
        binding.textWalletName.setText(provider);
        binding.textOtpMessage.setText(getString(R.string.trade_checkout_otp_message, TEST_OTP));
        if (method == PaymentMethod.BANK_TRANSFER) {
            drawSampleQr();
        }
    }

    // The QR is drawn on the device from the transfer content; nothing is fetched.
    private void drawSampleQr() {
        String content = SampleQrCode.buildTransferContent(dealId);
        binding.textTransferContent.setText(content);
        int dark = MaterialColors.getColor(binding.imageQr, com.google.android.material.R.attr.colorOnSurface);
        int light = MaterialColors.getColor(binding.imageQr, com.google.android.material.R.attr.colorSurface);
        Bitmap qr = SampleQrCode.createBitmap(content, QR_BITMAP_SIZE_PX, dark, light);
        binding.imageQr.setImageBitmap(qr);
    }

    private void setupButtons() {
        binding.buttonPrimary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPrimaryClicked();
            }
        });
        binding.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToMethods(R.string.trade_checkout_gateway_cancelled);
            }
        });
        binding.buttonFillOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.editOtp.setText(TEST_OTP);
                binding.layoutOtp.setError(null);
            }
        });
        binding.includeErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDeal();
            }
        });
    }

    private void onPrimaryClicked() {
        if (phase == PHASE_GATEWAY) {
            showPhase(PHASE_OTP);
            return;
        }
        String typed = binding.editOtp.getText() != null ? binding.editOtp.getText().toString().trim() : "";
        if (!TEST_OTP.equals(typed)) {
            binding.layoutOtp.setError(getString(R.string.trade_checkout_otp_wrong));
            return;
        }
        openResult();
    }

    private void showPhase(int newPhase) {
        phase = newPhase;
        boolean gateway = newPhase == PHASE_GATEWAY;
        binding.layoutCardPanel.setVisibility(gateway && method == PaymentMethod.CARD ? View.VISIBLE : View.GONE);
        binding.layoutTransferPanel.setVisibility(
                gateway && method == PaymentMethod.BANK_TRANSFER ? View.VISIBLE : View.GONE);
        binding.layoutWalletPanel.setVisibility(
                gateway && method == PaymentMethod.E_WALLET ? View.VISIBLE : View.GONE);
        binding.layoutOtpPanel.setVisibility(gateway ? View.GONE : View.VISIBLE);
        binding.buttonPrimary.setText(getPrimaryLabel());
    }

    private int getPrimaryLabel() {
        if (phase == PHASE_OTP) {
            return R.string.trade_checkout_otp_confirm;
        }
        if (method == PaymentMethod.BANK_TRANSFER) {
            return R.string.trade_checkout_transfer_done;
        }
        if (method == PaymentMethod.E_WALLET) {
            return R.string.trade_checkout_wallet_open;
        }
        return R.string.trade_checkout_card_pay;
    }

    private void openResult() {
        Bundle args = new Bundle();
        args.putString(CheckoutResultFragment.ARG_DEAL_ID, dealId);
        args.putString(CheckoutResultFragment.ARG_METHOD, method.name());
        args.putString(CheckoutResultFragment.ARG_PROVIDER_NAME, providerName);
        NavController navController = NavHostFragment.findNavController(CheckoutGatewayFragment.this);
        navController.navigate(R.id.checkoutResultFragment, args);
    }

    private void returnToMethods(int messageRes) {
        Toast.makeText(requireContext(), messageRes, Toast.LENGTH_SHORT).show();
        NavController navController = NavHostFragment.findNavController(CheckoutGatewayFragment.this);
        boolean popped = navController.popBackStack(R.id.dealPaymentFragment, false);
        if (!popped) {
            navController.navigateUp();
        }
    }

    // The deadline is a clock time, so rotating the screen does not restart the 10 minutes.
    private void startCountdown() {
        long remaining = deadlineMillis - System.currentTimeMillis();
        if (remaining <= 0L) {
            returnToMethods(R.string.trade_checkout_gateway_expired);
            return;
        }
        showRemaining(remaining);
        timer = new CountDownTimer(remaining, TICK_MILLIS) {
            @Override
            public void onTick(long millisUntilFinished) {
                showRemaining(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                returnToMethods(R.string.trade_checkout_gateway_expired);
            }
        };
        timer.start();
    }

    private void showRemaining(long millis) {
        long totalSeconds = millis / TICK_MILLIS;
        int minutes = (int) (totalSeconds / 60L);
        int seconds = (int) (totalSeconds % 60L);
        binding.textCountdown.setText(getString(R.string.trade_checkout_gateway_countdown, minutes, seconds));
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
        outState.putInt(KEY_PHASE, phase);
        outState.putLong(KEY_DEADLINE, deadlineMillis);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        binding = null;
    }
}
