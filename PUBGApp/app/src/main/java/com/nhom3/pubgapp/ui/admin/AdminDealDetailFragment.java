package com.nhom3.pubgapp.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentAdminDealDetailBinding;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Payment;
import com.nhom3.pubgapp.model.StatusChange;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.DeliveryMethod;
import com.nhom3.pubgapp.model.enums.PaymentMethod;
import com.nhom3.pubgapp.ui.common.ImageLoader;
import com.nhom3.pubgapp.ui.common.StatusChips;
import com.nhom3.pubgapp.util.FormatUtils;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.viewmodel.AdminDealViewModel;

import java.util.List;

/** Screen E06 Detail: View-only deal detail and status transition history for admin. */
public class AdminDealDetailFragment extends Fragment {

    private static final String ARG_DEAL_ID = "dealId";

    @Nullable
    private FragmentAdminDealDetailBinding binding;
    private AdminDealViewModel viewModel;
    private AdminStatusHistoryAdapter historyAdapter;
    private final FormatUtils formatUtils = new FormatUtils();
    private String currentDealId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminDealDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AdminDealViewModel.class);

        Bundle args = getArguments();
        currentDealId = args != null ? args.getString(ARG_DEAL_ID) : null;

        setUpToolbar();
        setUpHistoryList();
        setUpErrorRetry();
        observeViewModel();

        viewModel.loadDetail(currentDealId);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding == null) {
            return;
        }
        boolean online = NetworkMonitor.getInstance().isOnline();
        binding.includeOfflineBanner.getRoot().setVisibility(online ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setUpToolbar() {
        if (binding == null) {
            return;
        }
        binding.includeToolbar.textTitle.setText(R.string.admin_deal_detail_title);
        binding.includeToolbar.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                NavHostFragment.findNavController(AdminDealDetailFragment.this).navigateUp();
            }
        });
    }

    private void setUpHistoryList() {
        if (binding == null) {
            return;
        }
        historyAdapter = new AdminStatusHistoryAdapter();
        binding.recyclerStatusHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerStatusHistory.setAdapter(historyAdapter);
    }

    private void setUpErrorRetry() {
        if (binding == null) {
            return;
        }
        binding.includeErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                viewModel.loadDetail(currentDealId);
            }
        });
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                updateState();
            }
        });
        viewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                showError(message);
            }
        });
        viewModel.getSelected().observe(getViewLifecycleOwner(), new Observer<Deal>() {
            @Override
            public void onChanged(Deal deal) {
                if (deal != null) {
                    bindDeal(deal);
                }
                updateState();
            }
        });
        viewModel.getPayment().observe(getViewLifecycleOwner(), new Observer<Payment>() {
            @Override
            public void onChanged(Payment payment) {
                bindPayment(payment);
            }
        });
        viewModel.getBuyer().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User buyer) {
                bindBuyer(buyer);
            }
        });
        viewModel.getSeller().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User seller) {
                bindSeller(seller);
            }
        });
    }

    private void updateState() {
        if (binding == null) {
            return;
        }
        boolean loading = Boolean.TRUE.equals(viewModel.getLoading().getValue());
        Deal deal = viewModel.getSelected().getValue();
        boolean hasData = deal != null;
        boolean failed = !hasData && !loading;

        binding.progressDetail.setVisibility(loading && !hasData ? View.VISIBLE : View.GONE);
        binding.scrollContent.setVisibility(hasData ? View.VISIBLE : View.GONE);
        binding.includeErrorState.getRoot().setVisibility(failed ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        if (message == null || message.trim().isEmpty() || binding == null) {
            return;
        }
        if (viewModel.getSelected().getValue() == null) {
            binding.includeErrorState.textErrorMessage.setText(message);
        } else {
            Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
        }
        viewModel.clearError();
    }

    private void bindDeal(Deal deal) {
        if (binding == null) {
            return;
        }
        binding.textDealCode.setText(getString(R.string.admin_deal_detail_code, deal.getId()));
        binding.textItemTitle.setText(deal.getItemTitle() != null ? deal.getItemTitle() : "");
        StatusChips.bind(binding.layoutStatusChip.textStatusChip, deal.getStatus());
        ImageLoader.load(deal.getItemThumb(), binding.imageItemThumb, R.drawable.ic_inbox, R.drawable.ic_error);

        binding.textPrice.setText(getString(
                R.string.admin_deal_detail_price,
                formatUtils.formatPrice(deal.getAgreedPrice())));

        DeliveryMethod deliveryMethod = deal.getDeliveryMethod();
        int deliveryRes = deliveryMethod == DeliveryMethod.SHIPPING
                ? R.string.admin_deal_detail_delivery_shipping
                : R.string.admin_deal_detail_delivery_meet_up;
        binding.textDelivery.setText(getString(
                R.string.admin_deal_detail_delivery_label,
                getString(deliveryRes)));

        if (deal.getScheduledAt() != null) {
            binding.textScheduledAt.setVisibility(View.VISIBLE);
            binding.textScheduledAt.setText(getString(
                    R.string.admin_deal_detail_scheduled_at,
                    formatUtils.formatTime(deal.getScheduledAt())));
        } else {
            binding.textScheduledAt.setVisibility(View.GONE);
        }

        if (deal.getDeliveryAddress() != null && !deal.getDeliveryAddress().trim().isEmpty()) {
            binding.textDeliveryAddress.setVisibility(View.VISIBLE);
            binding.textDeliveryAddress.setText(getString(
                    R.string.admin_deal_detail_delivery_address,
                    deal.getDeliveryAddress()));
        } else {
            binding.textDeliveryAddress.setVisibility(View.GONE);
        }

        if (deal.getCarrierName() != null && !deal.getCarrierName().trim().isEmpty()) {
            binding.textCarrierName.setVisibility(View.VISIBLE);
            binding.textCarrierName.setText(getString(
                    R.string.admin_deal_detail_carrier_name,
                    deal.getCarrierName()));
        } else {
            binding.textCarrierName.setVisibility(View.GONE);
        }

        if (deal.getTrackingCode() != null && !deal.getTrackingCode().trim().isEmpty()) {
            binding.textTrackingCode.setVisibility(View.VISIBLE);
            binding.textTrackingCode.setText(getString(
                    R.string.admin_deal_detail_tracking_code,
                    deal.getTrackingCode()));
        } else {
            binding.textTrackingCode.setVisibility(View.GONE);
        }

        final String buyerId = deal.getBuyerId();
        binding.textBuyerName.setText(buyerId != null ? buyerId : "");
        binding.textBuyerName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToUser(buyerId);
            }
        });
        binding.textBuyerConfirmed.setText(deal.isBuyerConfirmed()
                ? R.string.admin_deal_detail_confirmed
                : R.string.admin_deal_detail_unconfirmed);

        final String sellerId = deal.getSellerId();
        binding.textSellerName.setText(sellerId != null ? sellerId : "");
        binding.textSellerName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToUser(sellerId);
            }
        });
        binding.textSellerConfirmed.setText(deal.isSellerConfirmed()
                ? R.string.admin_deal_detail_confirmed
                : R.string.admin_deal_detail_unconfirmed);

        if (!deal.needsPayment()) {
            binding.textPaymentNone.setVisibility(View.VISIBLE);
            binding.layoutPaymentInfo.setVisibility(View.GONE);
        } else {
            binding.textPaymentNone.setVisibility(View.GONE);
            binding.layoutPaymentInfo.setVisibility(View.VISIBLE);
        }

        List<StatusChange> history = deal.getStatusHistory();
        if (history == null || history.isEmpty()) {
            binding.textHistoryEmpty.setVisibility(View.VISIBLE);
            binding.recyclerStatusHistory.setVisibility(View.GONE);
            historyAdapter.submitList(null);
        } else {
            binding.textHistoryEmpty.setVisibility(View.GONE);
            binding.recyclerStatusHistory.setVisibility(View.VISIBLE);
            historyAdapter.submitList(history);
        }
    }

    private void bindBuyer(@Nullable User buyer) {
        if (binding == null || buyer == null) {
            return;
        }
        String displayName = buyer.getDisplayName();
        String uid = buyer.getId();
        binding.textBuyerName.setText(getString(
                R.string.admin_deal_detail_party_name,
                displayName != null && !displayName.isEmpty() ? displayName : uid,
                uid != null ? uid : ""));
    }

    private void bindSeller(@Nullable User seller) {
        if (binding == null || seller == null) {
            return;
        }
        String displayName = seller.getDisplayName();
        String uid = seller.getId();
        binding.textSellerName.setText(getString(
                R.string.admin_deal_detail_party_name,
                displayName != null && !displayName.isEmpty() ? displayName : uid,
                uid != null ? uid : ""));
    }

    private void bindPayment(@Nullable Payment payment) {
        if (binding == null || payment == null) {
            return;
        }
        binding.textPaymentAmount.setText(getString(
                R.string.admin_deal_detail_payment_amount,
                formatUtils.formatPrice(payment.getAmount())));
        StatusChips.bind(binding.layoutPaymentStatusChip.textStatusChip, payment.getStatus());

        PaymentMethod method = payment.getMethod();
        int methodRes = 0;
        if (method == PaymentMethod.CARD) {
            methodRes = R.string.admin_deal_detail_payment_method_card;
        } else if (method == PaymentMethod.E_WALLET) {
            methodRes = R.string.admin_deal_detail_payment_method_e_wallet;
        } else if (method == PaymentMethod.BANK_TRANSFER) {
            methodRes = R.string.admin_deal_detail_payment_method_bank_transfer;
        }
        binding.textPaymentMethod.setText(getString(
                R.string.admin_deal_detail_payment_method,
                methodRes != 0 ? getString(methodRes) : ""));

        if (payment.getHeldAt() != null) {
            binding.textPaymentHeldAt.setVisibility(View.VISIBLE);
            binding.textPaymentHeldAt.setText(getString(
                    R.string.admin_deal_detail_payment_held_at,
                    formatUtils.formatTime(payment.getHeldAt())));
        } else {
            binding.textPaymentHeldAt.setVisibility(View.GONE);
        }

        if (payment.getReleasedAt() != null) {
            binding.textPaymentReleasedAt.setVisibility(View.VISIBLE);
            binding.textPaymentReleasedAt.setText(getString(
                    R.string.admin_deal_detail_payment_released_at,
                    formatUtils.formatTime(payment.getReleasedAt())));
        } else {
            binding.textPaymentReleasedAt.setVisibility(View.GONE);
        }

        Deal deal = viewModel.getSelected().getValue();
        if (deal != null && deal.getAutoReleaseAt() != null) {
            binding.textAutoReleaseAt.setVisibility(View.VISIBLE);
            binding.textAutoReleaseAt.setText(getString(
                    R.string.admin_deal_detail_auto_release_at,
                    formatUtils.formatTime(deal.getAutoReleaseAt())));
        } else {
            binding.textAutoReleaseAt.setVisibility(View.GONE);
        }
    }

    private void navigateToUser(@Nullable String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return;
        }
        Bundle args = new Bundle();
        args.putString("userId", userId);
        NavHostFragment.findNavController(AdminDealDetailFragment.this)
                .navigate(R.id.adminUserDetailFragment, args);
    }
}
