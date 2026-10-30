package com.nhom3.pubgapp.ui.trade;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.MediaRepository;
import com.nhom3.pubgapp.data.UserRepository;
import com.nhom3.pubgapp.databinding.FragmentDealDetailBinding;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Payment;
import com.nhom3.pubgapp.model.Review;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.model.enums.DeliveryMethod;
import com.nhom3.pubgapp.model.enums.TradeType;
import com.nhom3.pubgapp.ui.common.ImageLoader;
import com.nhom3.pubgapp.util.SessionManager;
import com.nhom3.pubgapp.model.enums.VerificationStatus;
import com.nhom3.pubgapp.util.FormatUtils;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.viewmodel.DealViewModel;

import java.text.SimpleDateFormat;
import java.util.Locale;

/** Screen C03: Deal details, editing terms, two-party confirmation and cancellation. */
public class DealDetailFragment extends Fragment {

    private static final String KEY_CANCEL_OPEN = "key_cancel_open";
    private static final String KEY_WAYBILL_URI = "key_waybill_uri";
    private static final String KEY_RECEIVED_URI = "key_received_uri";
    private static final String KEY_SHIPPING_FORM_OPEN = "key_shipping_form_open";
    private static final String KEY_RECEIVING_FORM_OPEN = "key_receiving_form_open";
    private static final String TAG_TERMS_DIALOG = "deal_terms";
    private static final String TAG_COUNTDOWN_DIALOG = "countdown_confirm";

    private FragmentDealDetailBinding binding;
    private DealViewModel viewModel;
    private String dealId;
    private boolean isUserVerified;
    private Uri selectedWaybillUri;
    private Uri selectedReceivedUri;
    private Deal currentDeal;

    private final Handler countdownHandler = new Handler(Looper.getMainLooper());
    private final Runnable countdownRunnable = new Runnable() {
        @Override
        public void run() {
            updateCountdown();
            countdownHandler.postDelayed(this, 10000);
        }
    };

    private final ActivityResultLauncher<String> pickWaybillPhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    new ActivityResultCallback<Uri>() {
                        @Override
                        public void onActivityResult(Uri uri) {
                            if (uri != null) {
                                selectedWaybillUri = uri;
                                displayWaybillPreview(uri);
                                validateShippingForm();
                            }
                        }
                    });

    private final ActivityResultLauncher<String> pickReceivedPhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    new ActivityResultCallback<Uri>() {
                        @Override
                        public void onActivityResult(Uri uri) {
                            if (uri != null) {
                                selectedReceivedUri = uri;
                                displayReceivedPreview(uri);
                                validateReceivingForm();
                            }
                        }
                    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDealDetailBinding.inflate(inflater, container, false);
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
        setupErrorAndEmptyStates();
        setupEditTermsControls();
        setupDialogResults();
        setupMessageButton();
        setupCancelControls();
        setupShippingAndReceivingControls();
        setupCarrierDropdown();
        setupWriteReviewButton();
        observeViewModel();
        checkAccountVerification();

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }

        loadDealData();
    }

    private void setupToolbar() {
        binding.includeToolbar.textTitle.setText(R.string.trade_deal_detail_heading);
        binding.includeToolbar.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = NavHostFragment.findNavController(DealDetailFragment.this);
                navController.navigateUp();
            }
        });
    }

    private void setupErrorAndEmptyStates() {
        binding.includeErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDealData();
            }
        });
    }

    private void loadDealData() {
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

    private void checkAccountVerification() {
        String currentUid = getCurrentUserId();
        if (currentUid.isEmpty()) {
            isUserVerified = false;
            return;
        }

        new UserRepository().getProfile(currentUid, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                isUserVerified = user != null && user.getVerificationStatus() == VerificationStatus.APPROVED;
                updateVerificationUI();
            }

            @Override
            public void onError(String message) {
                isUserVerified = false;
                updateVerificationUI();
            }
        });
    }

    private void updateVerificationUI() {
        if (binding == null) {
            return;
        }

        Deal deal = viewModel.getDeal().getValue();
        if (deal == null) {
            return;
        }

        boolean needsVerification = !isUserVerified && (deal.getStatus() == DealStatus.PENDING_CONFIRM
                || deal.getStatus() == DealStatus.CONFIRMED
                || deal.getStatus() == DealStatus.SHIPPED);
        if (needsVerification) {
            binding.cardVerificationNotice.setVisibility(View.VISIBLE);
            binding.buttonRegisterVerification.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavController navController = NavHostFragment.findNavController(DealDetailFragment.this);
                    navController.navigate(R.id.action_global_my_profile);
                }
            });
            binding.buttonEditTerms.setEnabled(false);
            binding.buttonConfirm.setEnabled(false);
            binding.buttonPay.setEnabled(false);
            binding.buttonShipDeal.setEnabled(false);
            binding.buttonReceiveDeal.setEnabled(false);
            binding.buttonConfirmShipped.setEnabled(false);
            binding.buttonConfirmReceived.setEnabled(false);
        } else {
            binding.cardVerificationNotice.setVisibility(View.GONE);
            validateShippingForm();
            validateReceivingForm();
        }
    }

    private void setupEditTermsControls() {
        binding.buttonEditTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Deal deal = viewModel.getDeal().getValue();
                if (deal != null && deal.canEditTermsBy(getCurrentUserId())) {
                    boolean showPrice = deal.getTradeType() == TradeType.SELL;
                    DealTermsDialogFragment.newInstance(null, deal, showPrice)
                            .show(getParentFragmentManager(), TAG_TERMS_DIALOG);
                }
            }
        });
    }

    // Both dialogs answer through the fragment manager, so a rotation does not lose the answer.
    private void setupDialogResults() {
        getParentFragmentManager().setFragmentResultListener(DealTermsDialogFragment.RESULT_KEY,
                getViewLifecycleOwner(), new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        saveTerms(result);
                    }
                });
        getParentFragmentManager().setFragmentResultListener(CountdownConfirmDialogFragment.RESULT_KEY,
                getViewLifecycleOwner(), new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        runConfirmedAction(result.getString(CountdownConfirmDialogFragment.RESULT_ACTION, ""));
                    }
                });
    }

    private void runConfirmedAction(String action) {
        if (CountdownConfirmDialogFragment.ACTION_ACCEPT_TERMS.equals(action)) {
            viewModel.confirm();
        } else if (CountdownConfirmDialogFragment.ACTION_CANCEL_DEAL.equals(action)) {
            String reason = binding.editCancelReason.getText() != null
                    ? binding.editCancelReason.getText().toString().trim() : "";
            if (!reason.isEmpty()) {
                viewModel.cancel(reason);
            }
        }
    }

    private void showCountdownDialog(String action, int titleRes, int messageRes) {
        CountdownConfirmDialogFragment.newInstance(action, titleRes, messageRes)
                .show(getParentFragmentManager(), TAG_COUNTDOWN_DIALOG);
    }

    private void saveTerms(Bundle result) {
        Deal current = viewModel.getDeal().getValue();
        if (current == null) {
            return;
        }
        Deal terms = DealTermsDialogFragment.termsFromResult(result);
        current.setDeliveryMethod(terms.getDeliveryMethod());
        current.setDeliveryAddress(terms.getDeliveryAddress());
        current.setAgreedTerms(terms.getAgreedTerms());
        current.setScheduledAt(terms.getScheduledAt());
        long price = result.getLong(DealTermsDialogFragment.RESULT_PRICE, DealTermsDialogFragment.PRICE_HIDDEN);
        if (price != DealTermsDialogFragment.PRICE_HIDDEN) {
            current.setAgreedPrice(price);
        }
        viewModel.updateTerms(current);
    }

    private void setupMessageButton() {
        binding.buttonMessagePartner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChat(null);
            }
        });
    }

    // A draft fills the chat box, so a change request reaches the seller as a normal message.
    private void openChat(String draft) {
        Deal deal = viewModel.getDeal().getValue();
        if (deal == null || deal.getListingId() == null || deal.getBuyerId() == null) {
            return;
        }
        Bundle args = new Bundle();
        args.putString("listingId", deal.getListingId());
        args.putString("conversationId", deal.getListingId() + "_" + deal.getBuyerId());
        if (draft != null) {
            args.putString(ChatFragment.ARG_DRAFT_MESSAGE, draft);
        }
        NavHostFragment.findNavController(DealDetailFragment.this)
                .navigate(R.id.action_global_chat, args);
    }

    // The carrier is picked from a fixed list so the buyer sees the same name the seller chose.
    private void setupCarrierDropdown() {
        String[] carriers = getResources().getStringArray(R.array.trade_carrier_names);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(),
                android.R.layout.simple_list_item_1, carriers);
        binding.editCarrierName.setAdapter(adapter);
    }

    // Restoring the saved text filters the list down to one row; setting the adapter again shows all rows.
    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (binding != null) {
            setupCarrierDropdown();
        }
    }

    private void setupCancelControls() {
        binding.buttonCancelDeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.cardCancelDeal.setVisibility(View.VISIBLE);
            }
        });

        binding.buttonDismissCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.cardCancelDeal.setVisibility(View.GONE);
            }
        });

        binding.buttonConfirmCancel.setEnabled(false);
        binding.editCancelReason.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s != null && !s.toString().trim().isEmpty();
                binding.buttonConfirmCancel.setEnabled(hasText);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.buttonConfirmCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reason = binding.editCancelReason.getText() != null
                        ? binding.editCancelReason.getText().toString().trim() : "";
                if (!reason.isEmpty()) {
                    showCountdownDialog(CountdownConfirmDialogFragment.ACTION_CANCEL_DEAL,
                            R.string.trade_cancel_dialog_title, R.string.trade_cancel_dialog_message);
                }
            }
        });

        binding.buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CountdownConfirmDialogFragment.newInstance(CountdownConfirmDialogFragment.ACTION_ACCEPT_TERMS,
                        R.string.trade_accept_terms_dialog_title, buildAcceptTermsMessage())
                        .show(getParentFragmentManager(), TAG_COUNTDOWN_DIALOG);
            }
        });

        binding.buttonRequestTermsChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChat(getString(R.string.trade_request_terms_change_draft));
            }
        });
    }

    // The buyer agrees to exactly what is on screen, so the dialog repeats the terms lines.
    private String buildAcceptTermsMessage() {
        StringBuilder text = new StringBuilder();
        text.append(binding.textDeliveryMethod.getText()).append('\n');
        text.append(binding.textDeliveryAddress.getText()).append('\n');
        text.append(binding.textScheduledAt.getText()).append('\n');
        text.append(binding.textAgreedTerms.getText()).append("\n\n");
        text.append(getString(R.string.trade_accept_terms_dialog_message));
        return text.toString();
    }

    private void setupWriteReviewButton() {
        binding.buttonWriteReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("dealId", dealId);
                NavController navController = NavHostFragment.findNavController(DealDetailFragment.this);
                navController.navigate(R.id.action_global_write_review, args);
            }
        });
    }

    // Shown to both sides once the deal is received or completed, until they have written their review.
    private void renderWriteReviewButton(Deal deal) {
        binding.buttonWriteReview.setVisibility(View.GONE);
        boolean canReview = deal.getStatus() == DealStatus.RECEIVED || deal.getStatus() == DealStatus.COMPLETED;
        if (canReview) {
            viewModel.loadMyReview();
        }
    }

    private void observeMyReview() {
        viewModel.getMyReview().observe(getViewLifecycleOwner(), new Observer<Review>() {
            @Override
            public void onChanged(Review review) {
                Deal deal = viewModel.getDeal().getValue();
                boolean canReview = deal != null && (deal.getStatus() == DealStatus.RECEIVED
                        || deal.getStatus() == DealStatus.COMPLETED);
                binding.buttonWriteReview.setVisibility(canReview && review == null ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void observeViewModel() {
        observeMyReview();
        viewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                boolean loading = isLoading != null && isLoading;
                binding.progressBarLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
                setFormActionsEnabled(!loading);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String errorMessage) {
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    if (viewModel.getDeal().getValue() == null) {
                        binding.includeErrorState.layoutErrorState.setVisibility(View.VISIBLE);
                        binding.includeErrorState.textErrorMessage.setText(errorMessage);
                        binding.scrollContent.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        viewModel.clearError();
                    }
                } else {
                    binding.includeErrorState.layoutErrorState.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getDone().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isDone) {
                if (isDone != null && isDone) {
                    binding.cardCancelDeal.setVisibility(View.GONE);
                    binding.cardShippingForm.setVisibility(View.GONE);
                    binding.cardReceivingForm.setVisibility(View.GONE);
                    selectedWaybillUri = null;
                    selectedReceivedUri = null;
                    if (binding != null) {
                        binding.imageWaybillPreview.setVisibility(View.GONE);
                        binding.imageReceivedPreview.setVisibility(View.GONE);
                    }
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
                Deal deal = viewModel.getDeal().getValue();
                renderPaymentCard(deal, payment);
                if (deal != null) {
                    renderActionButtons(deal, getCurrentUserId());
                }
            }
        });
    }

    // Also called when the deal changes: a deal cancelled before payment has no money to describe.
    private void renderPaymentCard(Deal deal, Payment payment) {
        if (binding == null) {
            return;
        }
        if (!shouldShowPaymentCard(deal, payment)) {
            binding.cardPaymentInfo.setVisibility(View.GONE);
            return;
        }
        binding.cardPaymentInfo.setVisibility(View.VISIBLE);
        if (payment == null) {
            binding.textPaymentStatus.setVisibility(View.GONE);
            binding.textPaymentMethod.setVisibility(View.GONE);
            return;
        }
        binding.textPaymentStatus.setVisibility(View.VISIBLE);
        int statusTextRes = R.string.trade_deal_payment_status_held;
        if (payment.getStatus() == com.nhom3.pubgapp.model.enums.PaymentStatus.RELEASED) {
            statusTextRes = R.string.trade_deal_payment_status_released;
        } else if (payment.getStatus() == com.nhom3.pubgapp.model.enums.PaymentStatus.REFUNDED) {
            statusTextRes = R.string.trade_deal_payment_status_refunded;
        }
        binding.textPaymentStatus.setText(getString(R.string.trade_deal_payment_status_label,
                getString(statusTextRes)));
        binding.textPaymentNotice.setText(paymentNoticeFor(payment));
        renderPaymentMethod(payment);
    }

    private void renderPaymentMethod(Payment payment) {
        if (payment.getMethod() == null) {
            binding.textPaymentMethod.setVisibility(View.GONE);
            return;
        }
        binding.textPaymentMethod.setVisibility(View.VISIBLE);
        int methodTextRes = R.string.trade_deal_payment_method_card;
        if (payment.getMethod() == com.nhom3.pubgapp.model.enums.PaymentMethod.E_WALLET) {
            methodTextRes = R.string.trade_deal_payment_method_wallet;
        } else if (payment.getMethod() == com.nhom3.pubgapp.model.enums.PaymentMethod.BANK_TRANSFER) {
            methodTextRes = R.string.trade_deal_payment_method_transfer;
        }
        binding.textPaymentMethod.setText(getString(R.string.trade_deal_payment_method_label,
                getString(methodTextRes)));
    }

    static boolean shouldShowPaymentCard(Deal deal, Payment payment) {
        if (deal == null || !deal.needsPayment()) {
            return false;
        }
        return !(deal.getStatus() == DealStatus.CANCELLED && payment == null);
    }

    private void renderEmptyState() {
        binding.includeEmptyState.layoutEmptyState.setVisibility(View.VISIBLE);
        binding.includeEmptyState.textEmptyTitle.setText(R.string.trade_deal_detail_not_found);
        binding.scrollContent.setVisibility(View.GONE);
    }

    private void renderDeal(Deal deal) {
        String currentUid = getCurrentUserId();
        if (!deal.isParticipant(currentUid)) {
            binding.includeErrorState.layoutErrorState.setVisibility(View.VISIBLE);
            binding.includeErrorState.textErrorMessage.setText(R.string.trade_deal_detail_not_participant);
            binding.scrollContent.setVisibility(View.GONE);
            return;
        }

        currentDeal = deal;
        binding.includeErrorState.layoutErrorState.setVisibility(View.GONE);
        binding.includeEmptyState.layoutEmptyState.setVisibility(View.GONE);
        binding.scrollContent.setVisibility(View.VISIBLE);

        binding.textDealCode.setText(getString(R.string.trade_deal_detail_code,
                deal.getId() != null ? deal.getId() : ""));

        renderStatusAndProgress(deal);
        renderProductCard(deal);
        renderDealInfo(deal, currentUid);
        bindWaybillAndReceivedInfo(deal);
        renderPaymentCard(deal, viewModel.getPayment().getValue());
        updateCountdown();
        renderActionButtons(deal, currentUid);
        renderWriteReviewButton(deal);
        updateVerificationUI();
    }

    private void renderStatusAndProgress(Deal deal) {
        DealStatus status = deal.getStatus();
        if (status == DealStatus.CANCELLED) {
            binding.layoutSteps.setVisibility(View.GONE);
            binding.layoutStatusSpecial.setVisibility(View.VISIBLE);
            binding.textStatusSpecial.setText(R.string.trade_deal_detail_status_cancelled);
            String reason = deal.getCancelReason() != null ? deal.getCancelReason() : "";
            binding.textCancelReason.setText(getString(R.string.trade_deal_detail_cancel_reason_label, reason));
            return;
        }

        if (status == DealStatus.DISPUTED) {
            binding.layoutSteps.setVisibility(View.GONE);
            binding.layoutStatusSpecial.setVisibility(View.VISIBLE);
            binding.textStatusSpecial.setText(R.string.trade_deal_detail_status_disputed);
            binding.textCancelReason.setVisibility(View.GONE);
            return;
        }

        binding.layoutSteps.setVisibility(View.VISIBLE);
        binding.layoutStatusSpecial.setVisibility(View.GONE);

        int highlightColor = requireContext().getColor(R.color.primary);
        int mutedColor = requireContext().getColor(R.color.on_surface_variant);

        binding.textStepPending.setTextColor(status == DealStatus.PENDING_CONFIRM ? highlightColor : mutedColor);
        binding.textStepConfirmed.setTextColor(status == DealStatus.CONFIRMED ? highlightColor : mutedColor);
        binding.textStepShipped.setTextColor(status == DealStatus.SHIPPED ? highlightColor : mutedColor);
        binding.textStepReceived.setTextColor(status == DealStatus.RECEIVED ? highlightColor : mutedColor);
        binding.textStepCompleted.setTextColor(status == DealStatus.COMPLETED ? highlightColor : mutedColor);

        if (deal.getDeliveryMethod() == DeliveryMethod.MEET_UP) {
            binding.textStepShipped.setAlpha(0.35f);
        } else {
            binding.textStepShipped.setAlpha(1.0f);
        }
    }

    private void renderProductCard(Deal deal) {
        binding.textProductTitle.setText(deal.getItemTitle() != null ? deal.getItemTitle() : "");

        if (deal.getAgreedPrice() <= 0) {
            binding.textAgreedPrice.setText(R.string.trade_deal_detail_price_free);
        } else {
            binding.textAgreedPrice.setText(new FormatUtils().formatPrice(deal.getAgreedPrice()));
        }

        String tradeTypeText = getString(R.string.trade_deal_detail_trade_type_sell);
        if (deal.getTradeType() == TradeType.EXCHANGE) {
            tradeTypeText = getString(R.string.trade_deal_detail_trade_type_exchange);
        } else if (deal.getTradeType() == TradeType.GIVEAWAY) {
            tradeTypeText = getString(R.string.trade_deal_detail_trade_type_giveaway);
        }
        binding.textTradeType.setText(getString(R.string.trade_deal_detail_trade_type_label, tradeTypeText));

        decodeProductThumbnail(deal.getItemThumb());
    }

    private void decodeProductThumbnail(String thumbBase64) {
        if (thumbBase64 != null && !thumbBase64.trim().isEmpty()) {
            ImageLoader.load(thumbBase64, binding.imageProductThumb, R.drawable.ic_inbox, R.drawable.ic_inbox);
        } else {
            binding.imageProductThumb.setImageResource(R.drawable.ic_inbox);
        }
    }

    private void renderDealInfo(Deal deal, String currentUid) {
        boolean isBuyer = currentUid.equals(deal.getBuyerId());
        final boolean finalIsBuyer = isBuyer;
        final String partnerId = isBuyer ? deal.getSellerId() : deal.getBuyerId();
        String fallbackName = getString(R.string.trade_conversation_list_partner_fallback);

        binding.textPartnerRole.setText(getString(
                finalIsBuyer ? R.string.trade_deal_detail_partner_seller : R.string.trade_deal_detail_partner_buyer,
                fallbackName));

        if (partnerId != null && !partnerId.isEmpty()) {
            new UserRepository().getProfile(partnerId, new DataCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    if (binding == null || getContext() == null) {
                        return;
                    }
                    String name = user != null && user.getDisplayName() != null
                            && !user.getDisplayName().trim().isEmpty()
                            ? user.getDisplayName().trim()
                            : getString(R.string.trade_conversation_list_partner_fallback);
                    binding.textPartnerRole.setText(getString(
                            finalIsBuyer ? R.string.trade_deal_detail_partner_seller
                                    : R.string.trade_deal_detail_partner_buyer,
                            name));
                }

                @Override
                public void onError(String message) {
                }
            });
        }

        binding.layoutPartnerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (partnerId != null && !partnerId.isEmpty()) {
                    Bundle args = new Bundle();
                    args.putString("userId", partnerId);
                    NavController navController = NavHostFragment.findNavController(DealDetailFragment.this);
                    navController.navigate(R.id.action_global_user_profile, args);
                }
            }
        });

        // A deal made from an offer has no delivery method yet; showing "Gặp trực tiếp" by default
        // made the buyer think it was chosen, then "Xác nhận" failed with "Chưa chọn phương thức giao nhận".
        String methodText;
        if (deal.getDeliveryMethod() == null) {
            methodText = getString(R.string.trade_deal_detail_not_specified);
        } else if (deal.getDeliveryMethod() == DeliveryMethod.SHIPPING) {
            methodText = getString(R.string.trade_deal_detail_delivery_shipping);
        } else {
            methodText = getString(R.string.trade_deal_detail_delivery_meet_up);
        }
        binding.textDeliveryMethod.setText(getString(R.string.trade_deal_detail_delivery_method_label, methodText));

        boolean online = NetworkMonitor.getInstance().isOnline();
        String addressText = deal.getDeliveryAddress();
        if (addressText == null || addressText.trim().isEmpty()) {
            addressText = !online ? getString(R.string.trade_deal_detail_offline_info_notice)
                    : getString(R.string.trade_deal_detail_not_specified);
        }
        binding.textDeliveryAddress.setText(getString(R.string.trade_deal_detail_address_label, addressText));

        String scheduleText;
        if (deal.getScheduledAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            scheduleText = sdf.format(deal.getScheduledAt().toDate());
        } else {
            scheduleText = !online ? getString(R.string.trade_deal_detail_offline_info_notice)
                    : getString(R.string.trade_deal_detail_not_specified);
        }
        binding.textScheduledAt.setText(getString(R.string.trade_deal_detail_schedule_label, scheduleText));

        String termsText = deal.getAgreedTerms();
        if (termsText == null || termsText.trim().isEmpty()) {
            termsText = !online ? getString(R.string.trade_deal_detail_offline_info_notice)
                    : getString(R.string.trade_deal_detail_not_specified);
        }
        binding.textAgreedTerms.setText(getString(R.string.trade_deal_detail_terms_label, termsText));

        if (deal.getTradeType() == TradeType.EXCHANGE && deal.getExchangeItem() != null
                && !deal.getExchangeItem().trim().isEmpty()) {
            binding.textExchangeItem.setVisibility(View.VISIBLE);
            binding.textExchangeItem.setText(getString(R.string.trade_deal_detail_exchange_item_label,
                    deal.getExchangeItem()));
        } else {
            binding.textExchangeItem.setVisibility(View.GONE);
        }
    }

    private void bindWaybillAndReceivedInfo(Deal deal) {
        if (binding == null) {
            return;
        }
        if (deal.getDeliveryMethod() == DeliveryMethod.SHIPPING
                && deal.getCarrierName() != null && !deal.getCarrierName().trim().isEmpty()
                && deal.getTrackingCode() != null && !deal.getTrackingCode().trim().isEmpty()) {
            binding.cardWaybillInfo.setVisibility(View.VISIBLE);
            binding.textCarrierName.setText(getString(R.string.trade_deal_detail_carrier_label,
                    deal.getCarrierName()));
            binding.textTrackingCode.setText(getString(R.string.trade_deal_detail_tracking_code_label,
                    deal.getTrackingCode()));
            if (deal.getShippedAt() != null) {
                binding.textShippedAt.setText(getString(R.string.trade_deal_detail_shipped_at_label,
                        new FormatUtils().formatTime(deal.getShippedAt())));
            } else {
                binding.textShippedAt.setText("");
            }
            loadWaybillImage(deal.getTrackingImageId());
        } else {
            binding.cardWaybillInfo.setVisibility(View.GONE);
        }

        if (deal.getReceivedImageId() != null && !deal.getReceivedImageId().trim().isEmpty()) {
            binding.cardReceivedInfo.setVisibility(View.VISIBLE);
            if (deal.getReceivedAt() != null) {
                binding.textReceivedAt.setText(getString(R.string.trade_deal_detail_received_at_label,
                        new FormatUtils().formatTime(deal.getReceivedAt())));
            } else {
                binding.textReceivedAt.setText("");
            }
            loadReceivedImage(deal.getReceivedImageId());
        } else {
            binding.cardReceivedInfo.setVisibility(View.GONE);
        }
    }

    private void loadWaybillImage(String imageId) {
        if (binding == null) {
            return;
        }
        if (imageId == null || imageId.trim().isEmpty()) {
            binding.imageWaybillPhoto.setVisibility(View.GONE);
            return;
        }
        if (!NetworkMonitor.getInstance().isOnline()) {
            return;
        }
        new MediaRepository().getImage(imageId, new DataCallback<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                if (binding != null && bytes != null && bytes.length > 0) {
                    ImageLoader.load(bytes, imageId, binding.imageWaybillPhoto,
                            R.drawable.ic_inbox, R.drawable.ic_inbox);
                    binding.imageWaybillPhoto.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    private void loadReceivedImage(final String imageId) {
        if (binding == null) {
            return;
        }
        if (imageId == null || imageId.trim().isEmpty()) {
            binding.cardReceivedInfo.setVisibility(View.GONE);
            return;
        }
        if (!NetworkMonitor.getInstance().isOnline()) {
            return;
        }
        new MediaRepository().getImage(imageId, new DataCallback<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                if (binding != null && bytes != null && bytes.length > 0) {
                    ImageLoader.load(bytes, imageId, binding.imageReceivedPhoto,
                            R.drawable.ic_inbox, R.drawable.ic_inbox);
                    binding.imageReceivedPhoto.setVisibility(View.VISIBLE);
                    binding.cardReceivedInfo.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    private void startCountdown() {
        countdownHandler.removeCallbacks(countdownRunnable);
        countdownHandler.post(countdownRunnable);
    }

    private void stopCountdown() {
        countdownHandler.removeCallbacks(countdownRunnable);
    }

    private void updateCountdown() {
        if (binding == null || currentDeal == null
                || currentDeal.getStatus() != DealStatus.SHIPPED) {
            if (binding != null) {
                binding.cardCountdown.setVisibility(View.GONE);
            }
            return;
        }
        Timestamp autoReleaseAt = currentDeal.getAutoReleaseAt();
        if (autoReleaseAt == null) {
            binding.cardCountdown.setVisibility(View.GONE);
            return;
        }
        binding.cardCountdown.setVisibility(View.VISIBLE);
        Timestamp now = Timestamp.now();
        String remainingText = formatRemainingTime(autoReleaseAt, now, getContext());
        binding.textCountdown.setText(remainingText);

        boolean canComplain = canComplain(currentDeal, getCurrentUserId(), now);
        binding.buttonComplain.setVisibility(canComplain ? View.VISIBLE : View.GONE);
    }

    static String formatRemainingTime(Timestamp autoReleaseAt, Timestamp now) {
        return formatRemainingTime(autoReleaseAt, now, null);
    }

    static String formatRemainingTime(Timestamp autoReleaseAt, Timestamp now, android.content.Context context) {
        if (autoReleaseAt == null || now == null) {
            return "";
        }
        long diffMillis = autoReleaseAt.toDate().getTime() - now.toDate().getTime();
        if (diffMillis <= 0) {
            return context != null
                    ? context.getString(R.string.trade_deal_detail_countdown_expired)
                    : "Đã hết đếm ngược, hệ thống sẽ tự chuyển sang Đã nhận";
        }
        if (diffMillis < 24L * 60 * 60 * 1000) {
            long hours = diffMillis / (60L * 60 * 1000);
            long minutes = (diffMillis % (60L * 60 * 1000)) / (60L * 1000);
            return context != null
                    ? context.getString(R.string.trade_deal_detail_countdown_hours_minutes, hours, minutes)
                    : "Còn " + hours + " giờ " + minutes + " phút để xác nhận";
        }
        long oneDayMillis = 24L * 60 * 60 * 1000;
        long days = (diffMillis + oneDayMillis - 1) / oneDayMillis;
        return context != null
                ? context.getString(R.string.trade_deal_detail_countdown_days, days)
                : "Còn " + days + " ngày để xác nhận";
    }

    static boolean canComplain(Deal deal, String currentUid, Timestamp now) {
        if (deal == null || currentUid == null) {
            return false;
        }
        boolean isBuyer = currentUid.equals(deal.getBuyerId());
        boolean isSeller = currentUid.equals(deal.getSellerId());
        if (!isBuyer && !isSeller) {
            return false;
        }
        if (deal.getStatus() == DealStatus.SHIPPED) {
            return deal.getAutoReleaseAt() != null && now != null
                    && now.compareTo(deal.getAutoReleaseAt()) < 0;
        }
        if (deal.getStatus() == DealStatus.CONFIRMED
                && deal.getDeliveryMethod() == DeliveryMethod.MEET_UP) {
            return isBuyer;
        }
        return false;
    }

    private void setupShippingAndReceivingControls() {
        binding.buttonShipDeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.cardShippingForm.setVisibility(View.VISIBLE);
                binding.buttonShipDeal.setVisibility(View.GONE);
                binding.cardReceivingForm.setVisibility(View.GONE);
                binding.cardCancelDeal.setVisibility(View.GONE);
            }
        });

        binding.buttonCancelShippingForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.cardShippingForm.setVisibility(View.GONE);
                binding.buttonShipDeal.setVisibility(View.VISIBLE);
            }
        });

        binding.buttonPickWaybillPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickWaybillPhotoLauncher.launch("image/*");
            }
        });

        TextWatcher shippingWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateShippingForm();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        binding.editCarrierName.addTextChangedListener(shippingWatcher);
        binding.editTrackingCode.addTextChangedListener(shippingWatcher);

        binding.buttonConfirmShipped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String carrier = binding.editCarrierName.getText() != null
                        ? binding.editCarrierName.getText().toString().trim() : "";
                String tracking = binding.editTrackingCode.getText() != null
                        ? binding.editTrackingCode.getText().toString().trim() : "";
                viewModel.markShipped(carrier, tracking, selectedWaybillUri);
            }
        });

        binding.buttonReceiveDeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.cardReceivingForm.setVisibility(View.VISIBLE);
                binding.buttonReceiveDeal.setVisibility(View.GONE);
                binding.cardShippingForm.setVisibility(View.GONE);
                binding.cardCancelDeal.setVisibility(View.GONE);
            }
        });

        binding.buttonCancelReceivingForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.cardReceivingForm.setVisibility(View.GONE);
                binding.buttonReceiveDeal.setVisibility(View.VISIBLE);
            }
        });

        binding.buttonPickReceivedPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickReceivedPhotoLauncher.launch("image/*");
            }
        });

        binding.buttonConfirmReceived.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.markReceived(selectedReceivedUri);
            }
        });
    }

    private void displayWaybillPreview(Uri uri) {
        if (binding == null || uri == null) {
            return;
        }
        // ImageLoader decodes off the main thread and shrinks the photo to the view size.
        binding.imageWaybillPreview.setVisibility(View.VISIBLE);
        ImageLoader.load(uri, binding.imageWaybillPreview, R.drawable.ic_inbox, R.drawable.ic_inbox);
    }

    private void displayReceivedPreview(Uri uri) {
        if (binding == null || uri == null) {
            return;
        }
        // ImageLoader decodes off the main thread and shrinks the photo to the view size.
        binding.imageReceivedPreview.setVisibility(View.VISIBLE);
        ImageLoader.load(uri, binding.imageReceivedPreview, R.drawable.ic_inbox, R.drawable.ic_inbox);
    }

    private void validateShippingForm() {
        if (binding == null) {
            return;
        }
        String carrier = binding.editCarrierName.getText() != null
                ? binding.editCarrierName.getText().toString().trim() : "";
        String tracking = binding.editTrackingCode.getText() != null
                ? binding.editTrackingCode.getText().toString().trim() : "";
        boolean valid = !carrier.isEmpty() && !tracking.isEmpty() && selectedWaybillUri != null;
        boolean online = NetworkMonitor.getInstance().isOnline();
        binding.buttonConfirmShipped.setEnabled(valid && online && isUserVerified);
    }

    private void validateReceivingForm() {
        if (binding == null) {
            return;
        }
        boolean valid = selectedReceivedUri != null;
        boolean online = NetworkMonitor.getInstance().isOnline();
        binding.buttonConfirmReceived.setEnabled(valid && online && isUserVerified);
    }

    private void renderActionButtons(final Deal deal, final String currentUid) {
        boolean online = NetworkMonitor.getInstance().isOnline();
        final boolean isBuyer = currentUid.equals(deal.getBuyerId());
        final boolean isSeller = currentUid.equals(deal.getSellerId());
        DealStatus status = deal.getStatus();

        binding.textWaitingStatus.setVisibility(View.GONE);
        binding.buttonShipDeal.setVisibility(View.GONE);
        binding.buttonReceiveDeal.setVisibility(View.GONE);
        binding.buttonComplain.setVisibility(View.GONE);
        binding.buttonViewComplaint.setVisibility(View.GONE);

        if (status == DealStatus.PENDING_CONFIRM) {
            renderPendingActions(deal, currentUid, online);
        } else if (status == DealStatus.CONFIRMED) {
            binding.layoutActions.setVisibility(View.VISIBLE);
            binding.buttonEditTerms.setVisibility(View.GONE);
            binding.buttonConfirm.setVisibility(View.GONE);
            binding.buttonRequestTermsChange.setVisibility(View.GONE);
            binding.textConfirmationStatus.setVisibility(View.GONE);
            binding.buttonCancelDeal.setVisibility(View.VISIBLE);
            binding.buttonCancelDeal.setEnabled(online);

            Payment payment = viewModel.getPayment().getValue();
            boolean isPaidOrFree = !deal.needsPayment() || (payment != null && payment.isHeld());

            if (deal.getDeliveryMethod() == DeliveryMethod.SHIPPING) {
                if (isSeller) {
                    if (!isPaidOrFree) {
                        binding.textWaitingStatus.setVisibility(View.VISIBLE);
                        binding.textWaitingStatus.setText(R.string.trade_deal_detail_waiting_buyer_pay);
                    } else {
                        boolean shippingOpen = binding.cardShippingForm.getVisibility() == View.VISIBLE;
                        binding.buttonShipDeal.setVisibility(shippingOpen ? View.GONE : View.VISIBLE);
                        binding.buttonShipDeal.setEnabled(online && isUserVerified);
                    }
                } else if (isBuyer) {
                    if (!isPaidOrFree) {
                        binding.buttonPay.setVisibility(View.VISIBLE);
                        binding.buttonPay.setEnabled(online && isUserVerified);
                        binding.buttonPay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Bundle args = new Bundle();
                                args.putString("dealId", deal.getId());
                                NavController navController =
                                        NavHostFragment.findNavController(DealDetailFragment.this);
                                navController.navigate(R.id.action_global_deal_payment, args);
                            }
                        });
                    } else {
                        binding.buttonPay.setVisibility(View.GONE);
                        binding.textWaitingStatus.setVisibility(View.VISIBLE);
                        binding.textWaitingStatus.setText(R.string.trade_deal_detail_waiting_seller_ship);
                    }
                }
            } else if (deal.getDeliveryMethod() == DeliveryMethod.MEET_UP) {
                if (isBuyer) {
                    if (!isPaidOrFree) {
                        binding.buttonPay.setVisibility(View.VISIBLE);
                        binding.buttonPay.setEnabled(online && isUserVerified);
                        binding.buttonPay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Bundle args = new Bundle();
                                args.putString("dealId", deal.getId());
                                NavController navController =
                                        NavHostFragment.findNavController(DealDetailFragment.this);
                                navController.navigate(R.id.action_global_deal_payment, args);
                            }
                        });
                        binding.textWaitingStatus.setVisibility(View.VISIBLE);
                        binding.textWaitingStatus.setText(R.string.trade_deal_detail_please_pay_first);
                    } else {
                        binding.buttonPay.setVisibility(View.GONE);
                        boolean receivingOpen = binding.cardReceivingForm.getVisibility() == View.VISIBLE;
                        binding.buttonReceiveDeal.setVisibility(receivingOpen ? View.GONE : View.VISIBLE);
                        binding.buttonReceiveDeal.setEnabled(online && isUserVerified);
                        binding.buttonComplain.setVisibility(View.VISIBLE);
                        binding.buttonComplain.setEnabled(online);
                    }
                } else if (isSeller) {
                    binding.buttonPay.setVisibility(View.GONE);
                    binding.textWaitingStatus.setVisibility(View.VISIBLE);
                    binding.textWaitingStatus.setText(R.string.trade_deal_detail_waiting_buyer_receive);
                }
            }
        } else if (status == DealStatus.SHIPPED) {
            binding.layoutActions.setVisibility(View.VISIBLE);
            binding.buttonEditTerms.setVisibility(View.GONE);
            binding.buttonConfirm.setVisibility(View.GONE);
            binding.buttonRequestTermsChange.setVisibility(View.GONE);
            binding.buttonPay.setVisibility(View.GONE);
            binding.buttonCancelDeal.setVisibility(View.GONE);
            binding.textConfirmationStatus.setVisibility(View.GONE);

            if (isBuyer) {
                boolean receivingOpen = binding.cardReceivingForm.getVisibility() == View.VISIBLE;
                binding.buttonReceiveDeal.setVisibility(receivingOpen ? View.GONE : View.VISIBLE);
                binding.buttonReceiveDeal.setEnabled(online && isUserVerified);
            }

            boolean canComplain = canComplain(deal, currentUid, Timestamp.now());
            if (canComplain) {
                binding.buttonComplain.setVisibility(View.VISIBLE);
                binding.buttonComplain.setEnabled(online);
            }
        } else if (status == DealStatus.DISPUTED) {
            binding.layoutActions.setVisibility(View.VISIBLE);
            binding.buttonEditTerms.setVisibility(View.GONE);
            binding.buttonConfirm.setVisibility(View.GONE);
            binding.buttonRequestTermsChange.setVisibility(View.GONE);
            binding.buttonPay.setVisibility(View.GONE);
            binding.buttonCancelDeal.setVisibility(View.GONE);
            binding.textConfirmationStatus.setVisibility(View.GONE);
            binding.buttonViewComplaint.setVisibility(View.VISIBLE);
            binding.buttonViewComplaint.setEnabled(online);
        } else {
            binding.layoutActions.setVisibility(View.GONE);
        }

        binding.buttonComplain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("dealId", deal.getId());
                NavController navController = NavHostFragment.findNavController(DealDetailFragment.this);
                navController.navigate(R.id.action_global_create_complaint, args);
            }
        });

        binding.buttonViewComplaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // initialTab 1 opens the complaints tab of D07.
                Bundle args = new Bundle();
                args.putInt("initialTab", 1);
                NavHostFragment.findNavController(DealDetailFragment.this)
                        .navigate(R.id.action_global_my_reports, args);
            }
        });
    }

    // Seller: edits the terms and waits. Buyer: reads the terms, then accepts or negotiates in chat.
    private void renderPendingActions(Deal deal, String currentUid, boolean online) {
        binding.layoutActions.setVisibility(View.VISIBLE);
        binding.buttonPay.setVisibility(View.GONE);
        binding.buttonCancelDeal.setVisibility(View.VISIBLE);
        binding.buttonCancelDeal.setEnabled(online);

        boolean canEdit = deal.canEditTermsBy(currentUid);
        binding.buttonEditTerms.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        binding.buttonEditTerms.setEnabled(online && isUserVerified);

        boolean isBuyer = currentUid.equals(deal.getBuyerId());
        binding.buttonConfirm.setVisibility(isBuyer ? View.VISIBLE : View.GONE);
        binding.buttonRequestTermsChange.setVisibility(isBuyer ? View.VISIBLE : View.GONE);
        binding.textConfirmationStatus.setVisibility(View.VISIBLE);
        if (isBuyer) {
            binding.buttonConfirm.setEnabled(deal.canConfirmBy(currentUid) && online && isUserVerified);
            binding.buttonConfirm.setText(R.string.trade_deal_detail_confirm);
            binding.textConfirmationStatus.setText(R.string.trade_deal_detail_review_terms_hint);
        } else {
            binding.textConfirmationStatus.setText(R.string.trade_deal_detail_waiting_buyer_terms);
        }
    }

    private void setFormActionsEnabled(boolean enabled) {
        binding.buttonConfirmCancel.setEnabled(enabled && binding.editCancelReason.getText() != null
                && !binding.editCancelReason.getText().toString().trim().isEmpty());
        if (!enabled) {
            binding.buttonConfirmShipped.setEnabled(false);
            binding.buttonConfirmReceived.setEnabled(false);
        } else {
            validateShippingForm();
            validateReceivingForm();
        }
    }

    private String getCurrentUserId() {
        String uid = SessionManager.getInstance().getCurrentUserId();
        return uid != null ? uid : "";
    }

    @Override
    public void onStart() {
        super.onStart();
        startCountdown();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopCountdown();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null) {
            outState.putBoolean(KEY_CANCEL_OPEN,
                    binding.cardCancelDeal.getVisibility() == View.VISIBLE);
            outState.putBoolean(KEY_SHIPPING_FORM_OPEN,
                    binding.cardShippingForm.getVisibility() == View.VISIBLE);
            outState.putBoolean(KEY_RECEIVING_FORM_OPEN,
                    binding.cardReceivingForm.getVisibility() == View.VISIBLE);
        }
        if (selectedWaybillUri != null) {
            outState.putString(KEY_WAYBILL_URI, selectedWaybillUri.toString());
        }
        if (selectedReceivedUri != null) {
            outState.putString(KEY_RECEIVED_URI, selectedReceivedUri.toString());
        }
    }

    private void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        boolean cancelOpen = savedInstanceState.getBoolean(KEY_CANCEL_OPEN, false);
        boolean shippingOpen = savedInstanceState.getBoolean(KEY_SHIPPING_FORM_OPEN, false);
        boolean receivingOpen = savedInstanceState.getBoolean(KEY_RECEIVING_FORM_OPEN, false);

        if (cancelOpen && binding != null) {
            binding.cardCancelDeal.setVisibility(View.VISIBLE);
        }
        if (shippingOpen && binding != null) {
            binding.cardShippingForm.setVisibility(View.VISIBLE);
            binding.buttonShipDeal.setVisibility(View.GONE);
        }
        if (receivingOpen && binding != null) {
            binding.cardReceivingForm.setVisibility(View.VISIBLE);
            binding.buttonReceiveDeal.setVisibility(View.GONE);
        }
        String waybillStr = savedInstanceState.getString(KEY_WAYBILL_URI);
        if (waybillStr != null) {
            selectedWaybillUri = Uri.parse(waybillStr);
            displayWaybillPreview(selectedWaybillUri);
        }
        String receivedStr = savedInstanceState.getString(KEY_RECEIVED_URI);
        if (receivedStr != null) {
            selectedReceivedUri = Uri.parse(receivedStr);
            displayReceivedPreview(selectedReceivedUri);
        }
        validateShippingForm();
        validateReceivingForm();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopCountdown();
        binding = null;
    }

    // The notice explains where the money is now; "held until received" is wrong once it was released or refunded.
    private int paymentNoticeFor(Payment payment) {
        if (payment.getStatus() == com.nhom3.pubgapp.model.enums.PaymentStatus.RELEASED) {
            return R.string.trade_deal_detail_payment_notice_released;
        }
        if (payment.getStatus() == com.nhom3.pubgapp.model.enums.PaymentStatus.REFUNDED) {
            return R.string.trade_deal_detail_payment_notice_refunded;
        }
        return R.string.trade_deal_detail_payment_notice;
    }
}
