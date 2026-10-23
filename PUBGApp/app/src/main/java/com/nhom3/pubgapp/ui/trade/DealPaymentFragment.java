package com.nhom3.pubgapp.ui.trade;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentDealPaymentBinding;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Payment;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.model.enums.PaymentMethod;
import com.nhom3.pubgapp.model.enums.PaymentStatus;
import com.nhom3.pubgapp.util.FormatUtils;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.viewmodel.DealViewModel;

import java.text.SimpleDateFormat;
import java.util.Locale;

/** Screen C07: Simulated deal payment with a 3-step payment flow. */
public class DealPaymentFragment extends Fragment {

    private static final String KEY_CURRENT_STEP = "key_payment_current_step";
    private static final String KEY_SELECTED_METHOD = "key_payment_selected_method";

    private FragmentDealPaymentBinding binding;
    private DealViewModel viewModel;
    private String dealId;
    private int currentStep = 1;
    private PaymentMethod selectedMethod;

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
        setupStep1Controls();
        setupStep2Controls();
        setupStep3Controls();
        observeViewModel();

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        } else {
            showStep(1);
        }

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

    private void setupStep1Controls() {
        binding.buttonContinue.setEnabled(false);
        binding.radioGroupMethods.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioMethodCard) {
                    selectedMethod = PaymentMethod.CARD;
                } else if (checkedId == R.id.radioMethodWallet) {
                    selectedMethod = PaymentMethod.E_WALLET;
                } else if (checkedId == R.id.radioMethodTransfer) {
                    selectedMethod = PaymentMethod.BANK_TRANSFER;
                } else {
                    selectedMethod = null;
                }
                binding.buttonContinue.setEnabled(selectedMethod != null);
            }
        });

        binding.buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedMethod != null) {
                    showStep(2);
                }
            }
        });
    }

    private void setupStep2Controls() {
        binding.buttonChangeMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStep(1);
            }
        });

        binding.buttonCancelPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateBackToDeal();
            }
        });

        binding.buttonConfirmPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedMethod != null) {
                    viewModel.pay(selectedMethod);
                }
            }
        });
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
        binding.layoutStep1.setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        binding.layoutStep2.setVisibility(step == 2 ? View.VISIBLE : View.GONE);
        binding.layoutStep3.setVisibility(step == 3 ? View.VISIBLE : View.GONE);

        if (step == 2) {
            updateStep2Views();
        } else if (step == 3) {
            updateStep3Views();
        }
    }

    private void updateStep2Views() {
        Deal deal = viewModel.getDeal().getValue();
        if (deal == null) {
            return;
        }

        binding.textConfirmAmount.setText(getString(R.string.trade_deal_payment_amount_label,
                new FormatUtils().formatPrice(deal.getAgreedPrice())));

        int methodRes = getPaymentMethodStringRes(selectedMethod);
        binding.textConfirmMethod.setText(getString(R.string.trade_deal_payment_selected_method_label,
                getString(methodRes)));
    }

    private void updateStep3Views() {
        Deal deal = viewModel.getDeal().getValue();
        Payment payment = viewModel.getPayment().getValue();

        long amount = deal != null ? deal.getAgreedPrice() : 0;
        binding.textReceiptAmount.setText(new FormatUtils().formatPrice(amount));

        PaymentMethod method = payment != null && payment.getMethod() != null
                ? payment.getMethod() : selectedMethod;
        int methodRes = getPaymentMethodStringRes(method);
        binding.textReceiptMethod.setText(getString(R.string.trade_deal_payment_method_label,
                getString(methodRes)));

        if (payment != null && payment.getHeldAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            binding.textReceiptTime.setText(getString(R.string.trade_deal_payment_time_label,
                    sdf.format(payment.getHeldAt().toDate())));
        } else {
            binding.textReceiptTime.setText(getString(R.string.trade_deal_payment_time_label, "Vừa xong"));
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

    private int getPaymentMethodStringRes(PaymentMethod method) {
        if (method == PaymentMethod.E_WALLET) {
            return R.string.trade_deal_payment_method_wallet;
        }
        if (method == PaymentMethod.BANK_TRANSFER) {
            return R.string.trade_deal_payment_method_transfer;
        }
        return R.string.trade_deal_payment_method_card;
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                boolean loading = isLoading != null && isLoading;
                binding.progressBarLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
                binding.buttonConfirmPayment.setEnabled(!loading);
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

        viewModel.getDone().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isDone) {
                if (isDone != null && isDone) {
                    showStep(3);
                    viewModel.clearDone();
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
                    showStep(3);
                }
            }
        });
    }

    private void renderEmptyState() {
        binding.includeEmptyState.layoutEmptyState.setVisibility(View.VISIBLE);
        binding.includeEmptyState.textEmptyTitle.setText(R.string.trade_deal_payment_not_eligible);
        binding.scrollContent.setVisibility(View.GONE);
    }

    private void renderDeal(Deal deal) {
        String currentUid = getCurrentUserId();
        if (!currentUid.equals(deal.getBuyerId())) {
            binding.includeErrorState.layoutErrorState.setVisibility(View.VISIBLE);
            binding.includeErrorState.textErrorMessage.setText(R.string.trade_deal_payment_not_buyer);
            binding.scrollContent.setVisibility(View.GONE);
            return;
        }

        if (deal.getStatus() != DealStatus.CONFIRMED || !deal.needsPayment()) {
            binding.includeEmptyState.layoutEmptyState.setVisibility(View.VISIBLE);
            binding.includeEmptyState.textEmptyTitle.setText(R.string.trade_deal_payment_not_eligible);
            binding.scrollContent.setVisibility(View.GONE);
            return;
        }

        binding.includeErrorState.layoutErrorState.setVisibility(View.GONE);
        binding.includeEmptyState.layoutEmptyState.setVisibility(View.GONE);
        binding.scrollContent.setVisibility(View.VISIBLE);

        binding.textProductTitle.setText(deal.getItemTitle() != null ? deal.getItemTitle() : "");
        binding.textAmountToPay.setText(getString(R.string.trade_deal_payment_amount_label,
                new FormatUtils().formatPrice(deal.getAgreedPrice())));

        decodeProductThumbnail(deal.getItemThumb());
    }

    private void decodeProductThumbnail(String thumbBase64) {
        if (thumbBase64 != null && !thumbBase64.trim().isEmpty()) {
            try {
                byte[] decoded = Base64.decode(thumbBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                if (bitmap != null) {
                    binding.imageProductThumb.setImageBitmap(bitmap);
                    return;
                }
            } catch (Exception e) {
                // Fallback to placeholder
            }
        }
        binding.imageProductThumb.setImageResource(R.drawable.ic_inbox);
    }

    private String getCurrentUserId() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return "";
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_STEP, currentStep);
        if (selectedMethod != null) {
            outState.putString(KEY_SELECTED_METHOD, selectedMethod.name());
        }
    }

    private void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        currentStep = savedInstanceState.getInt(KEY_CURRENT_STEP, 1);
        String methodStr = savedInstanceState.getString(KEY_SELECTED_METHOD);
        if (methodStr != null) {
            try {
                selectedMethod = PaymentMethod.valueOf(methodStr);
                if (selectedMethod == PaymentMethod.CARD) {
                    binding.radioMethodCard.setChecked(true);
                } else if (selectedMethod == PaymentMethod.E_WALLET) {
                    binding.radioMethodWallet.setChecked(true);
                } else if (selectedMethod == PaymentMethod.BANK_TRANSFER) {
                    binding.radioMethodTransfer.setChecked(true);
                }
            } catch (IllegalArgumentException e) {
                selectedMethod = null;
            }
        }
        showStep(currentStep);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
