package com.nhom3.pubgapp.ui.trade;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

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
import com.nhom3.pubgapp.data.UserRepository;
import com.nhom3.pubgapp.databinding.FragmentDealDetailBinding;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Payment;
import com.nhom3.pubgapp.model.Review;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.model.enums.DeliveryMethod;
import com.nhom3.pubgapp.model.enums.TradeType;
import com.nhom3.pubgapp.model.enums.VerificationStatus;
import com.nhom3.pubgapp.util.FormatUtils;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.viewmodel.DealViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Screen C03: Deal details, editing terms, two-party confirmation and cancellation. */
public class DealDetailFragment extends Fragment {

    private static final String KEY_EDIT_TERMS_OPEN = "key_edit_terms_open";
    private static final String KEY_CANCEL_OPEN = "key_cancel_open";
    private static final String KEY_SCHEDULE_MILLIS = "key_schedule_millis";

    private FragmentDealDetailBinding binding;
    private DealViewModel viewModel;
    private String dealId;
    private boolean isUserVerified;
    private Timestamp selectedScheduleTimestamp;

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

        if (!isUserVerified && deal.getStatus() == DealStatus.PENDING_CONFIRM) {
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
        } else {
            binding.cardVerificationNotice.setVisibility(View.GONE);
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
                } else {
                    binding.cardPaymentInfo.setVisibility(View.GONE);
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

        binding.includeErrorState.layoutErrorState.setVisibility(View.GONE);
        binding.includeEmptyState.layoutEmptyState.setVisibility(View.GONE);
        binding.scrollContent.setVisibility(View.VISIBLE);

        binding.textDealCode.setText(getString(R.string.trade_deal_detail_code,
                deal.getId() != null ? deal.getId() : ""));

        renderStatusAndProgress(deal);
        renderProductCard(deal);
        renderDealInfo(deal, currentUid);
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

    private void renderActionButtons(Deal deal, String currentUid) {
        boolean online = NetworkMonitor.getInstance().isOnline();
        boolean isBuyer = currentUid.equals(deal.getBuyerId());
        DealStatus status = deal.getStatus();

        if (status == DealStatus.PENDING_CONFIRM) {
            binding.layoutActions.setVisibility(View.VISIBLE);
            binding.buttonEditTerms.setVisibility(View.VISIBLE);
            binding.buttonConfirm.setVisibility(View.VISIBLE);
            binding.buttonCancelDeal.setVisibility(View.VISIBLE);

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
        } else {
            binding.layoutActions.setVisibility(View.GONE);
        }
    }

    private void setFormActionsEnabled(boolean enabled) {
        binding.buttonSaveTerms.setEnabled(enabled);
        binding.buttonConfirmCancel.setEnabled(enabled && binding.editCancelReason.getText() != null
                && !binding.editCancelReason.getText().toString().trim().isEmpty());
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
        if (binding != null) {
            outState.putBoolean(KEY_EDIT_TERMS_OPEN, binding.cardEditTerms.getVisibility() == View.VISIBLE);
            outState.putBoolean(KEY_CANCEL_OPEN, binding.cardCancelDeal.getVisibility() == View.VISIBLE);
        }
        if (selectedScheduleTimestamp != null) {
            outState.putLong(KEY_SCHEDULE_MILLIS, selectedScheduleTimestamp.toDate().getTime());
        }
    }

    private void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        boolean editOpen = savedInstanceState.getBoolean(KEY_EDIT_TERMS_OPEN, false);
        boolean cancelOpen = savedInstanceState.getBoolean(KEY_CANCEL_OPEN, false);
        long scheduleMillis = savedInstanceState.getLong(KEY_SCHEDULE_MILLIS, 0);

        if (editOpen) {
            binding.cardEditTerms.setVisibility(View.VISIBLE);
        }
        if (cancelOpen) {
            binding.cardCancelDeal.setVisibility(View.VISIBLE);
        }
        if (scheduleMillis > 0) {
            selectedScheduleTimestamp = new Timestamp(new Date(scheduleMillis));
            updateSelectedScheduleText();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
