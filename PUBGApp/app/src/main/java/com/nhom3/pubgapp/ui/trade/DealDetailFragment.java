package com.nhom3.pubgapp.ui.trade;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
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
import com.nhom3.pubgapp.model.enums.ReportTarget;
import com.nhom3.pubgapp.model.enums.TradeType;
import com.nhom3.pubgapp.model.enums.VerificationStatus;
import com.nhom3.pubgapp.util.FormatUtils;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.viewmodel.DealViewModel;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Screen C03: Deal details, editing terms, two-party confirmation and cancellation. */
public class DealDetailFragment extends Fragment {

    private static final String KEY_EDIT_TERMS_OPEN = "key_edit_terms_open";
    private static final String KEY_CANCEL_OPEN = "key_cancel_open";
    private static final String KEY_SCHEDULE_MILLIS = "key_schedule_millis";
    private static final String KEY_WAYBILL_URI = "key_waybill_uri";
    private static final String KEY_RECEIVED_URI = "key_received_uri";
    private static final String KEY_SHIPPING_FORM_OPEN = "key_shipping_form_open";
    private static final String KEY_RECEIVING_FORM_OPEN = "key_receiving_form_open";

    private FragmentDealDetailBinding binding;
    private DealViewModel viewModel;
    private String dealId;
    private boolean isUserVerified;
    private Timestamp selectedScheduleTimestamp;
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
        setupCancelControls();
        setupShippingAndReceivingControls();
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
                if (deal != null) {
                    populateEditTermsForm(deal);
                    binding.cardEditTerms.setVisibility(View.VISIBLE);
                    binding.cardCancelDeal.setVisibility(View.GONE);
                }
            }
        });

        binding.buttonCancelEditTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.cardEditTerms.setVisibility(View.GONE);
            }
        });

        binding.buttonPickSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker();
            }
        });

        binding.buttonSaveTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTerms();
            }
        });
    }

    private void populateEditTermsForm(Deal deal) {
        if (deal.getDeliveryMethod() == DeliveryMethod.SHIPPING) {
            binding.radioShipping.setChecked(true);
        } else {
            binding.radioMeetUp.setChecked(true);
        }

        binding.editDeliveryAddress.setText(deal.getDeliveryAddress() != null ? deal.getDeliveryAddress() : "");
        binding.editAgreedTerms.setText(deal.getAgreedTerms() != null ? deal.getAgreedTerms() : "");

        if (deal.getTradeType() == TradeType.SELL) {
            binding.inputAgreedPrice.setVisibility(View.VISIBLE);
            binding.editAgreedPrice.setText(String.valueOf(deal.getAgreedPrice()));
        } else {
            binding.inputAgreedPrice.setVisibility(View.GONE);
        }

        selectedScheduleTimestamp = deal.getScheduledAt();
        updateSelectedScheduleText();
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedScheduleTimestamp != null) {
            calendar.setTime(selectedScheduleTimestamp.toDate());
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        calendar.set(Calendar.YEAR, selectedYear);
                        calendar.set(Calendar.MONTH, selectedMonth);
                        calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                        showTimePicker(calendar);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker(Calendar calendar) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                        calendar.set(Calendar.MINUTE, selectedMinute);
                        calendar.set(Calendar.SECOND, 0);
                        selectedScheduleTimestamp = new Timestamp(calendar.getTime());
                        updateSelectedScheduleText();
                    }
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void updateSelectedScheduleText() {
        if (selectedScheduleTimestamp != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            binding.textSelectedSchedule.setText(sdf.format(selectedScheduleTimestamp.toDate()));
        } else {
            binding.textSelectedSchedule.setText(R.string.trade_deal_detail_not_specified);
        }
    }

    private void saveTerms() {
        Deal current = viewModel.getDeal().getValue();
        if (current == null) {
            return;
        }

        DeliveryMethod method = binding.radioShipping.isChecked() ? DeliveryMethod.SHIPPING : DeliveryMethod.MEET_UP;
        current.setDeliveryMethod(method);

        String address = binding.editDeliveryAddress.getText() != null
                ? binding.editDeliveryAddress.getText().toString().trim() : "";
        current.setDeliveryAddress(address);

        String terms = binding.editAgreedTerms.getText() != null
                ? binding.editAgreedTerms.getText().toString().trim() : "";
        current.setAgreedTerms(terms);

        if (selectedScheduleTimestamp != null) {
            current.setScheduledAt(selectedScheduleTimestamp);
        }

        if (current.getTradeType() == TradeType.SELL && binding.editAgreedPrice.getText() != null) {
            try {
                long price = Long.parseLong(binding.editAgreedPrice.getText().toString().trim());
                if (price >= 0) {
                    current.setAgreedPrice(price);
                }
            } catch (NumberFormatException e) {
                // Keep existing price if invalid format
            }
        }

        viewModel.updateTerms(current);
    }

    private void setupCancelControls() {
        binding.buttonCancelDeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.cardCancelDeal.setVisibility(View.VISIBLE);
                binding.cardEditTerms.setVisibility(View.GONE);
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
                    viewModel.cancel(reason);
                }
            }
        });

        binding.buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.confirm();
            }
        });
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
                    binding.cardEditTerms.setVisibility(View.GONE);
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
                if (deal != null && deal.needsPayment()) {
                    binding.cardPaymentInfo.setVisibility(View.VISIBLE);
                    if (payment != null) {
                        binding.textPaymentStatus.setVisibility(View.VISIBLE);
                        int statusTextRes = R.string.trade_deal_payment_status_held;
                        if (payment.getStatus() == com.nhom3.pubgapp.model.enums.PaymentStatus.RELEASED) {
                            statusTextRes = R.string.trade_deal_payment_status_released;
                        } else if (payment.getStatus() == com.nhom3.pubgapp.model.enums.PaymentStatus.REFUNDED) {
                            statusTextRes = R.string.trade_deal_payment_status_refunded;
                        }
                        binding.textPaymentStatus.setText(getString(R.string.trade_deal_payment_status_label,
                                getString(statusTextRes)));

                        if (payment.getMethod() != null) {
                            binding.textPaymentMethod.setVisibility(View.VISIBLE);
                            int methodTextRes = R.string.trade_deal_payment_method_card;
                            if (payment.getMethod() == com.nhom3.pubgapp.model.enums.PaymentMethod.E_WALLET) {
                                methodTextRes = R.string.trade_deal_payment_method_wallet;
                            } else if (payment.getMethod()
                                    == com.nhom3.pubgapp.model.enums.PaymentMethod.BANK_TRANSFER) {
                                methodTextRes = R.string.trade_deal_payment_method_transfer;
                            }
                            binding.textPaymentMethod.setText(getString(R.string.trade_deal_payment_method_label,
                                    getString(methodTextRes)));
                        } else {
                            binding.textPaymentMethod.setVisibility(View.GONE);
                        }
                    } else {
                        binding.textPaymentStatus.setVisibility(View.GONE);
                        binding.textPaymentMethod.setVisibility(View.GONE);
                    }
                } else {
                    binding.cardPaymentInfo.setVisibility(View.GONE);
                }
                if (deal != null) {
                    renderActionButtons(deal, getCurrentUserId());
                }
            }
        });
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
            try {
                byte[] decoded = Base64.decode(thumbBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                if (bitmap != null) {
                    binding.imageProductThumb.setImageBitmap(bitmap);
                    return;
                }
            } catch (Exception e) {
                // Fallback to placeholder on decode failure
            }
        }
        binding.imageProductThumb.setImageResource(R.drawable.ic_inbox);
    }

    private void renderDealInfo(Deal deal, String currentUid) {
        boolean isBuyer = currentUid.equals(deal.getBuyerId());
        int roleRes = isBuyer ? R.string.trade_deal_detail_role_seller
                : R.string.trade_deal_detail_role_buyer;
        String roleName = getString(roleRes);
        String partnerId = isBuyer ? deal.getSellerId() : deal.getBuyerId();

        binding.textPartnerRole.setText(getString(
                isBuyer ? R.string.trade_deal_detail_partner_seller : R.string.trade_deal_detail_partner_buyer,
                roleName));

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

        String methodText = deal.getDeliveryMethod() == DeliveryMethod.SHIPPING
                ? getString(R.string.trade_deal_detail_delivery_shipping)
                : getString(R.string.trade_deal_detail_delivery_meet_up);
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
        if (deal.getCarrierName() != null && deal.getTrackingCode() != null) {
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
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (bitmap != null) {
                        binding.imageWaybillPhoto.setImageBitmap(bitmap);
                        binding.imageWaybillPhoto.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    private void loadReceivedImage(String imageId) {
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
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (bitmap != null) {
                        binding.imageReceivedPhoto.setImageBitmap(bitmap);
                        binding.imageReceivedPhoto.setVisibility(View.VISIBLE);
                        binding.cardReceivedInfo.setVisibility(View.VISIBLE);
                    }
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
        String remainingText = formatRemainingTime(autoReleaseAt, now);
        binding.textCountdown.setText(remainingText);

        boolean canComplain = canComplain(currentDeal, getCurrentUserId(), now);
        binding.buttonComplain.setVisibility(canComplain ? View.VISIBLE : View.GONE);
    }

    static String formatRemainingTime(Timestamp autoReleaseAt, Timestamp now) {
        if (autoReleaseAt == null || now == null) {
            return "";
        }
        long diffMillis = autoReleaseAt.toDate().getTime() - now.toDate().getTime();
        if (diffMillis <= 0) {
            return "Đã hết đếm ngược, hệ thống sẽ tự chuyển sang Đã nhận";
        }
        if (diffMillis < 24L * 60 * 60 * 1000) {
            long hours = diffMillis / (60L * 60 * 1000);
            long minutes = (diffMillis % (60L * 60 * 1000)) / (60L * 1000);
            return "Còn " + hours + " giờ " + minutes + " phút để xác nhận";
        }
        long days = diffMillis / (24L * 60 * 60 * 1000);
        return "Còn " + days + " ngày để xác nhận";
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
                binding.cardReceivingForm.setVisibility(View.GONE);
                binding.cardCancelDeal.setVisibility(View.GONE);
                binding.cardEditTerms.setVisibility(View.GONE);
            }
        });

        binding.buttonCancelShippingForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.cardShippingForm.setVisibility(View.GONE);
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
                binding.cardShippingForm.setVisibility(View.GONE);
                binding.cardCancelDeal.setVisibility(View.GONE);
                binding.cardEditTerms.setVisibility(View.GONE);
            }
        });

        binding.buttonCancelReceivingForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.cardReceivingForm.setVisibility(View.GONE);
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
        try (InputStream is = requireContext().getContentResolver().openInputStream(uri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (bitmap != null) {
                binding.imageWaybillPreview.setImageBitmap(bitmap);
                binding.imageWaybillPreview.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            binding.imageWaybillPreview.setVisibility(View.GONE);
        }
    }

    private void displayReceivedPreview(Uri uri) {
        if (binding == null || uri == null) {
            return;
        }
        try (InputStream is = requireContext().getContentResolver().openInputStream(uri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (bitmap != null) {
                binding.imageReceivedPreview.setImageBitmap(bitmap);
                binding.imageReceivedPreview.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            binding.imageReceivedPreview.setVisibility(View.GONE);
        }
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
            binding.layoutActions.setVisibility(View.VISIBLE);
            binding.buttonEditTerms.setVisibility(View.VISIBLE);
            binding.buttonConfirm.setVisibility(View.VISIBLE);
            binding.buttonCancelDeal.setVisibility(View.VISIBLE);
            binding.buttonPay.setVisibility(View.GONE);

            boolean myConfirmed = isBuyer ? deal.isBuyerConfirmed() : deal.isSellerConfirmed();
            if (myConfirmed) {
                binding.buttonConfirm.setEnabled(false);
                binding.buttonConfirm.setText(R.string.trade_deal_detail_confirmed_by_you);
                binding.textConfirmationStatus.setVisibility(View.VISIBLE);
                binding.textConfirmationStatus.setText(R.string.trade_deal_detail_waiting_partner_confirm);
            } else {
                binding.buttonConfirm.setEnabled(online && isUserVerified);
                binding.buttonConfirm.setText(R.string.trade_deal_detail_confirm);
                binding.textConfirmationStatus.setVisibility(View.GONE);
            }

            binding.buttonEditTerms.setEnabled(online && isUserVerified);
            binding.buttonCancelDeal.setEnabled(online);
        } else if (status == DealStatus.CONFIRMED) {
            binding.layoutActions.setVisibility(View.VISIBLE);
            binding.buttonEditTerms.setVisibility(View.GONE);
            binding.buttonConfirm.setVisibility(View.GONE);
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
                        binding.buttonShipDeal.setVisibility(View.VISIBLE);
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
                        binding.buttonReceiveDeal.setVisibility(View.VISIBLE);
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
            binding.buttonPay.setVisibility(View.GONE);
            binding.buttonCancelDeal.setVisibility(View.GONE);
            binding.textConfirmationStatus.setVisibility(View.GONE);

            if (isBuyer) {
                binding.buttonReceiveDeal.setVisibility(View.VISIBLE);
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
            binding.buttonPay.setVisibility(View.GONE);
            binding.buttonCancelDeal.setVisibility(View.GONE);
            binding.textConfirmationStatus.setVisibility(View.GONE);
            binding.buttonViewComplaint.setVisibility(View.VISIBLE);
            binding.buttonViewComplaint.setEnabled(online);
        } else {
            binding.layoutActions.setVisibility(View.GONE);
        }

        final String partnerId = isBuyer ? deal.getSellerId() : deal.getBuyerId();
        binding.buttonComplain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("targetType", ReportTarget.USER.name());
                args.putString("targetId", partnerId);
                NavController navController = NavHostFragment.findNavController(DealDetailFragment.this);
                navController.navigate(R.id.action_global_report, args);
            }
        });

        binding.buttonViewComplaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(requireContext(), R.string.trade_deal_detail_complaint_not_supported,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setFormActionsEnabled(boolean enabled) {
        binding.buttonSaveTerms.setEnabled(enabled);
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
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return "";
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
            outState.putBoolean(KEY_EDIT_TERMS_OPEN,
                    binding.cardEditTerms.getVisibility() == View.VISIBLE);
            outState.putBoolean(KEY_CANCEL_OPEN,
                    binding.cardCancelDeal.getVisibility() == View.VISIBLE);
            outState.putBoolean(KEY_SHIPPING_FORM_OPEN,
                    binding.cardShippingForm.getVisibility() == View.VISIBLE);
            outState.putBoolean(KEY_RECEIVING_FORM_OPEN,
                    binding.cardReceivingForm.getVisibility() == View.VISIBLE);
        }
        if (selectedScheduleTimestamp != null) {
            outState.putLong(KEY_SCHEDULE_MILLIS, selectedScheduleTimestamp.toDate().getTime());
        }
        if (selectedWaybillUri != null) {
            outState.putString(KEY_WAYBILL_URI, selectedWaybillUri.toString());
        }
        if (selectedReceivedUri != null) {
            outState.putString(KEY_RECEIVED_URI, selectedReceivedUri.toString());
        }
    }

    private void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        boolean editOpen = savedInstanceState.getBoolean(KEY_EDIT_TERMS_OPEN, false);
        boolean cancelOpen = savedInstanceState.getBoolean(KEY_CANCEL_OPEN, false);
        boolean shippingOpen = savedInstanceState.getBoolean(KEY_SHIPPING_FORM_OPEN, false);
        boolean receivingOpen = savedInstanceState.getBoolean(KEY_RECEIVING_FORM_OPEN, false);
        long scheduleMillis = savedInstanceState.getLong(KEY_SCHEDULE_MILLIS, 0);

        if (editOpen && binding != null) {
            binding.cardEditTerms.setVisibility(View.VISIBLE);
        }
        if (cancelOpen && binding != null) {
            binding.cardCancelDeal.setVisibility(View.VISIBLE);
        }
        if (shippingOpen && binding != null) {
            binding.cardShippingForm.setVisibility(View.VISIBLE);
        }
        if (receivingOpen && binding != null) {
            binding.cardReceivingForm.setVisibility(View.VISIBLE);
        }
        if (scheduleMillis > 0) {
            selectedScheduleTimestamp = new Timestamp(new Date(scheduleMillis));
            updateSelectedScheduleText();
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
}
