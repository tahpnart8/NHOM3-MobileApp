package com.nhom3.pubgapp.ui.trade;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.DialogDealTermsBinding;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.enums.DeliveryMethod;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

// The seller writes the deal terms here: when accepting an offer (offerId set) and when editing them later.
public class DealTermsDialogFragment extends DialogFragment {

    public static final String RESULT_KEY = "deal_terms_result";
    public static final String RESULT_OFFER_ID = "offerId";
    public static final String RESULT_METHOD = "method";
    public static final String RESULT_ADDRESS = "address";
    public static final String RESULT_SCHEDULE_MILLIS = "scheduleMillis";
    public static final String RESULT_TERMS = "terms";
    public static final String RESULT_PRICE = "price";
    public static final long PRICE_HIDDEN = -1L;

    private static final String KEY_SCHEDULE_MILLIS = "key_schedule_millis";
    private static final String DATE_PATTERN = "dd/MM/yyyy HH:mm";

    private DialogDealTermsBinding binding;
    private long scheduleMillis;

    /** The offerId is empty when editing; prefill may be null; the price field shows only when showPrice. */
    public static DealTermsDialogFragment newInstance(@Nullable String offerId, @Nullable Deal prefill,
                                                      boolean showPrice) {
        Bundle args = new Bundle();
        args.putString(RESULT_OFFER_ID, offerId != null ? offerId : "");
        if (prefill != null) {
            args.putString(RESULT_METHOD,
                    prefill.getDeliveryMethod() != null ? prefill.getDeliveryMethod().name() : "");
            args.putString(RESULT_ADDRESS, prefill.getDeliveryAddress() != null ? prefill.getDeliveryAddress() : "");
            args.putString(RESULT_TERMS, prefill.getAgreedTerms() != null ? prefill.getAgreedTerms() : "");
            if (prefill.getScheduledAt() != null) {
                args.putLong(RESULT_SCHEDULE_MILLIS, prefill.getScheduledAt().toDate().getTime());
            }
            args.putLong(RESULT_PRICE, showPrice ? prefill.getAgreedPrice() : PRICE_HIDDEN);
        } else {
            args.putLong(RESULT_PRICE, PRICE_HIDDEN);
        }
        DealTermsDialogFragment fragment = new DealTermsDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /** Builds a Deal that carries only the terms from a result bundle. */
    public static Deal termsFromResult(Bundle result) {
        Deal terms = new Deal();
        String method = result.getString(RESULT_METHOD, "");
        if (!method.isEmpty()) {
            terms.setDeliveryMethod(DeliveryMethod.valueOf(method));
        }
        terms.setDeliveryAddress(result.getString(RESULT_ADDRESS, ""));
        terms.setAgreedTerms(result.getString(RESULT_TERMS, ""));
        long millis = result.getLong(RESULT_SCHEDULE_MILLIS, 0L);
        if (millis > 0L) {
            terms.setScheduledAt(new Timestamp(new Date(millis)));
        }
        return terms;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogDealTermsBinding.inflate(LayoutInflater.from(requireContext()));
        Bundle args = requireArguments();
        if (savedInstanceState != null) {
            scheduleMillis = savedInstanceState.getLong(KEY_SCHEDULE_MILLIS, 0L);
        } else {
            scheduleMillis = args.getLong(RESULT_SCHEDULE_MILLIS, 0L);
            fillFromArguments(args);
        }
        updateScheduleText();

        binding.buttonPickSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });

        boolean accepting = !args.getString(RESULT_OFFER_ID, "").isEmpty();
        int titleRes = accepting ? R.string.trade_terms_dialog_title_accept
                : R.string.trade_deal_detail_edit_terms_title;
        int positiveRes = accepting ? R.string.trade_terms_dialog_accept : R.string.trade_deal_detail_save_terms;
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(titleRes)
                .setView(binding.getRoot())
                .setPositiveButton(positiveRes, null)
                .setNegativeButton(R.string.trade_terms_dialog_cancel, null)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog == null) {
            return;
        }
        // Set after show() so a validation error keeps the dialog open instead of dismissing it.
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_SCHEDULE_MILLIS, scheduleMillis);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void fillFromArguments(Bundle args) {
        String method = args.getString(RESULT_METHOD, "");
        if (DeliveryMethod.SHIPPING.name().equals(method)) {
            binding.radioShipping.setChecked(true);
        } else if (DeliveryMethod.MEET_UP.name().equals(method)) {
            binding.radioMeetUp.setChecked(true);
        }
        binding.editDeliveryAddress.setText(args.getString(RESULT_ADDRESS, ""));
        binding.editAgreedTerms.setText(args.getString(RESULT_TERMS, ""));
        long price = args.getLong(RESULT_PRICE, PRICE_HIDDEN);
        if (price != PRICE_HIDDEN) {
            binding.inputAgreedPrice.setVisibility(View.VISIBLE);
            binding.editAgreedPrice.setText(String.valueOf(price));
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (scheduleMillis > System.currentTimeMillis()) {
            calendar.setTimeInMillis(scheduleMillis);
        }
        DatePickerDialog picker = new DatePickerDialog(requireContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        showTimePicker(year, month, day);
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        picker.getDatePicker().setMinDate(startOfTodayMillis());
        picker.show();
    }

    private void showTimePicker(final int year, final int month, final int day) {
        Calendar now = Calendar.getInstance();
        TimePickerDialog picker = new TimePickerDialog(requireContext(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hour, int minute) {
                        applyPickedTime(year, month, day, hour, minute);
                    }
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
        picker.show();
    }

    // The date picker only blocks past days, so a past time on today is checked here.
    private void applyPickedTime(int year, int month, int day, int hour, int minute) {
        Calendar picked = Calendar.getInstance();
        picked.set(year, month, day, hour, minute, 0);
        picked.set(Calendar.MILLISECOND, 0);
        Timestamp pickedAt = new Timestamp(picked.getTime());
        if (Deal.isScheduleInPast(pickedAt, Timestamp.now())) {
            showError(R.string.trade_terms_error_schedule_past);
            return;
        }
        scheduleMillis = picked.getTimeInMillis();
        binding.textTermsError.setVisibility(View.GONE);
        updateScheduleText();
    }

    private static long startOfTodayMillis() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today.getTimeInMillis();
    }

    private void updateScheduleText() {
        if (scheduleMillis > 0L) {
            SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN, Locale.getDefault());
            binding.textSelectedSchedule.setText(format.format(new Date(scheduleMillis)));
        } else {
            binding.textSelectedSchedule.setText(R.string.trade_terms_dialog_schedule_empty);
        }
    }

    private void showError(int messageRes) {
        binding.textTermsError.setText(messageRes);
        binding.textTermsError.setVisibility(View.VISIBLE);
    }

    private void submit() {
        binding.inputDeliveryAddress.setError(null);
        binding.textTermsError.setVisibility(View.GONE);

        boolean shipping = binding.radioShipping.isChecked();
        boolean meetUp = binding.radioMeetUp.isChecked();
        String address = binding.editDeliveryAddress.getText() != null
                ? binding.editDeliveryAddress.getText().toString().trim() : "";
        if (!shipping && !meetUp) {
            showError(R.string.trade_terms_error_method);
            return;
        }
        if (address.isEmpty()) {
            binding.inputDeliveryAddress.setError(getString(R.string.trade_terms_error_address));
            return;
        }
        if (scheduleMillis <= 0L) {
            showError(R.string.trade_terms_error_schedule_missing);
            return;
        }
        if (Deal.isScheduleInPast(new Timestamp(new Date(scheduleMillis)), Timestamp.now())) {
            showError(R.string.trade_terms_error_schedule_past);
            return;
        }
        deliverResult(shipping, address);
    }

    private void deliverResult(boolean shipping, String address) {
        Bundle args = requireArguments();
        Bundle result = new Bundle();
        result.putString(RESULT_OFFER_ID, args.getString(RESULT_OFFER_ID, ""));
        result.putString(RESULT_METHOD, shipping ? DeliveryMethod.SHIPPING.name() : DeliveryMethod.MEET_UP.name());
        result.putString(RESULT_ADDRESS, address);
        result.putLong(RESULT_SCHEDULE_MILLIS, scheduleMillis);
        String terms = binding.editAgreedTerms.getText() != null
                ? binding.editAgreedTerms.getText().toString().trim() : "";
        result.putString(RESULT_TERMS, terms);
        result.putLong(RESULT_PRICE, readPrice(args.getLong(RESULT_PRICE, PRICE_HIDDEN)));
        getParentFragmentManager().setFragmentResult(RESULT_KEY, result);
        dismiss();
    }

    // The old price stays when the field is hidden or does not hold a number.
    private long readPrice(long oldPrice) {
        if (binding.inputAgreedPrice.getVisibility() != View.VISIBLE || binding.editAgreedPrice.getText() == null) {
            return oldPrice;
        }
        String text = binding.editAgreedPrice.getText().toString().trim();
        long price = oldPrice;
        try {
            price = Long.parseLong(text);
        } catch (NumberFormatException e) {
            price = oldPrice;
        }
        return price >= 0L ? price : oldPrice;
    }
}
