package com.nhom3.pubgapp.ui.trade;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.ItemPaymentHistoryBinding;
import com.nhom3.pubgapp.model.Payment;
import com.nhom3.pubgapp.model.enums.PaymentMethod;
import com.nhom3.pubgapp.model.enums.PaymentStatus;
import com.nhom3.pubgapp.ui.common.StatusChips;
import com.nhom3.pubgapp.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

// Adapter for the payment history list in C04.
public class PaymentHistoryAdapter extends RecyclerView.Adapter<PaymentHistoryAdapter.PaymentViewHolder> {

    public interface OnPaymentClickListener {
        void onPaymentClick(Payment payment);
    }

    private final List<Payment> payments = new ArrayList<Payment>();
    private final OnPaymentClickListener listener;
    private final FormatUtils formatUtils = new FormatUtils();

    public PaymentHistoryAdapter(@Nullable OnPaymentClickListener listener) {
        this.listener = listener;
    }

    public void submitPayments(@Nullable List<Payment> newPayments) {
        payments.clear();
        if (newPayments != null) {
            payments.addAll(newPayments);
        }
        notifyDataSetChanged();
    }

    public static int getRoleStringRes(@Nullable Payment payment, @Nullable String currentUid) {
        if (payment == null || currentUid == null || currentUid.isEmpty()) {
            return 0;
        }
        if (currentUid.equals(payment.getPayerId())) {
            return R.string.trade_payment_role_payer;
        }
        if (currentUid.equals(payment.getPayeeId())) {
            return R.string.trade_payment_role_payee;
        }
        return 0;
    }

    public static int getMethodStringRes(@Nullable PaymentMethod method) {
        if (method == null) {
            return 0;
        }
        switch (method) {
            case CARD:
                return R.string.trade_payment_method_card;
            case E_WALLET:
                return R.string.trade_payment_method_e_wallet;
            case BANK_TRANSFER:
                return R.string.trade_payment_method_bank_transfer;
            default:
                return 0;
        }
    }

    public static String formatPaymentTime(@NonNull Context context,
                                          @Nullable Payment payment,
                                          @NonNull FormatUtils formatUtils) {
        if (payment == null) {
            return "";
        }
        if (payment.getStatus() == PaymentStatus.RELEASED && payment.getReleasedAt() != null) {
            return context.getString(
                    R.string.trade_payment_time_released,
                    formatUtils.formatTime(payment.getReleasedAt()));
        }
        if (payment.getStatus() == PaymentStatus.REFUNDED && payment.getReleasedAt() != null) {
            return context.getString(
                    R.string.trade_payment_time_refunded,
                    formatUtils.formatTime(payment.getReleasedAt()));
        }
        if (payment.getHeldAt() != null) {
            return context.getString(
                    R.string.trade_payment_time_held,
                    formatUtils.formatTime(payment.getHeldAt()));
        }
        if (payment.getCreatedAt() != null) {
            return formatUtils.formatTime(payment.getCreatedAt());
        }
        return "";
    }

    public static String formatDealCode(@NonNull Context context, @Nullable Payment payment) {
        if (payment == null || payment.getDealId() == null || payment.getDealId().isEmpty()) {
            return "";
        }
        return context.getString(R.string.trade_payment_deal_code, payment.getDealId());
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new PaymentViewHolder(ItemPaymentHistoryBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        final Payment payment = payments.get(position);
        ItemPaymentHistoryBinding binding = holder.binding;
        Context context = binding.getRoot().getContext();

        binding.textDealCode.setText(formatDealCode(context, payment));
        binding.textAmount.setText(formatUtils.formatPrice(payment.getAmount()));
        StatusChips.bind(binding.layoutStatusChip.textStatusChip, payment.getStatus());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = user != null ? user.getUid() : "";
        int roleRes = getRoleStringRes(payment, currentUid);
        binding.textRole.setText(roleRes != 0 ? context.getString(roleRes) : "");

        int methodRes = getMethodStringRes(payment.getMethod());
        binding.textMethod.setText(methodRes != 0 ? context.getString(methodRes) : "");

        binding.textTime.setText(formatPaymentTime(context, payment, formatUtils));

        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onPaymentClick(payment);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return payments.size();
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        final ItemPaymentHistoryBinding binding;

        PaymentViewHolder(ItemPaymentHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
