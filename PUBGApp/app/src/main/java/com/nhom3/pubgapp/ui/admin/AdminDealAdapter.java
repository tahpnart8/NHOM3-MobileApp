package com.nhom3.pubgapp.ui.admin;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.UserRepository;
import com.nhom3.pubgapp.databinding.ItemAdminDealBinding;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.ui.common.ImageLoader;
import com.nhom3.pubgapp.ui.common.StatusChips;
import com.nhom3.pubgapp.util.FormatUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Adapter for the deal monitor list in E06.
public class AdminDealAdapter extends RecyclerView.Adapter<AdminDealAdapter.DealViewHolder> {

    public interface OnDealClickListener {
        void onDealClick(Deal deal);
    }

    private final List<Deal> deals = new ArrayList<Deal>();
    private final Map<String, User> userCache = new HashMap<String, User>();
    private final Set<String> requestedUserIds = new HashSet<String>();
    private final UserRepository userRepository = new UserRepository();
    private final OnDealClickListener listener;
    private final FormatUtils formatUtils = new FormatUtils();

    public AdminDealAdapter(OnDealClickListener listener) {
        this.listener = listener;
    }

    // A shipped deal is overdue when autoReleaseAt has passed.
    public static boolean isOverdue(@Nullable Deal deal, @Nullable Timestamp now) {
        if (deal == null || now == null) {
            return false;
        }
        return deal.isAutoReleaseDue(now);
    }

    // StatusChips paints a neutral chip ("Đã hủy") on colorSurface, the same white as the card, so it looked
    // like plain grey text next to the "Hoàn tất" chip. On the admin deal screens it gets a visible pill.
    static void bindDealStatus(TextView chip, DealStatus status) {
        StatusChips.bind(chip, status);
        if (status == DealStatus.CANCELLED) {
            paintNeutralPill(chip);
        }
    }

    // colorSurfaceVariant keeps the grey text readable (4.6:1, giao-dien.md needs 4.5).
    static void paintNeutralPill(TextView chip) {
        int pill = MaterialColors.getColor(chip, com.google.android.material.R.attr.colorSurfaceVariant);
        chip.setBackgroundTintList(ColorStateList.valueOf(pill));
    }

    public void submitList(List<Deal> newDeals) {
        deals.clear();
        if (newDeals != null) {
            deals.addAll(newDeals);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new DealViewHolder(ItemAdminDealBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DealViewHolder holder, int position) {
        final Deal deal = deals.get(position);
        ItemAdminDealBinding binding = holder.binding;
        Context context = binding.getRoot().getContext();

        String dealId = deal.getId();
        binding.textDealCode.setText(context.getString(
                R.string.admin_deal_monitor_code, dealId != null ? dealId : ""));

        String title = deal.getItemTitle();
        binding.textItemTitle.setText(title != null ? title : "");

        ImageLoader.load(deal.getItemThumb(), binding.imageThumb, R.drawable.ic_inbox, R.drawable.ic_error);

        binding.textPrice.setText(formatUtils.formatPrice(deal.getAgreedPrice()));

        bindDealStatus(binding.layoutStatusChip.textStatusChip, deal.getStatus());

        boolean overdue = isOverdue(deal, Timestamp.now());
        binding.chipOverdue.setVisibility(overdue ? View.VISIBLE : View.GONE);

        String buyerId = deal.getBuyerId() != null ? deal.getBuyerId() : "";
        String sellerId = deal.getSellerId() != null ? deal.getSellerId() : "";

        loadUser(buyerId);
        loadUser(sellerId);

        User buyer = userCache.get(buyerId);
        User seller = userCache.get(sellerId);

        String buyerName = (buyer != null && buyer.getDisplayName() != null
                && !buyer.getDisplayName().trim().isEmpty())
                ? buyer.getDisplayName().trim() : buyerId;
        String sellerName = (seller != null && seller.getDisplayName() != null
                && !seller.getDisplayName().trim().isEmpty())
                ? seller.getDisplayName().trim() : sellerId;

        binding.textParties.setText(context.getString(R.string.admin_deal_monitor_parties, buyerName, sellerName));

        Timestamp updatedAt = deal.getUpdatedAt();
        if (updatedAt != null) {
            binding.textUpdated.setText(context.getString(
                    R.string.admin_deal_monitor_updated, formatUtils.formatTime(updatedAt)));
            binding.textUpdated.setVisibility(View.VISIBLE);
        } else {
            binding.textUpdated.setVisibility(View.GONE);
        }

        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDealClick(deal);
            }
        });
    }

    @Override
    public int getItemCount() {
        return deals.size();
    }

    private void loadUser(@Nullable final String userId) {
        if (userId == null || userId.isEmpty() || requestedUserIds.contains(userId)) {
            return;
        }
        requestedUserIds.add(userId);
        userRepository.getProfile(userId, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    return;
                }
                userCache.put(userId, user);
                refreshRowsOf(userId);
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    private void refreshRowsOf(String userId) {
        for (int i = 0; i < deals.size(); i++) {
            Deal d = deals.get(i);
            if (d != null && (userId.equals(d.getBuyerId()) || userId.equals(d.getSellerId()))) {
                notifyItemChanged(i);
            }
        }
    }

    static class DealViewHolder extends RecyclerView.ViewHolder {
        final ItemAdminDealBinding binding;

        DealViewHolder(ItemAdminDealBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
