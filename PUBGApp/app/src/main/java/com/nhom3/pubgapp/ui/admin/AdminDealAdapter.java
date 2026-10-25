package com.nhom3.pubgapp.ui.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.ItemAdminDealBinding;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.ui.common.ImageLoader;
import com.nhom3.pubgapp.ui.common.StatusChips;
import com.nhom3.pubgapp.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

// Adapter for the deal monitor list in E06.
public class AdminDealAdapter extends RecyclerView.Adapter<AdminDealAdapter.DealViewHolder> {

    public interface OnDealClickListener {
        void onDealClick(Deal deal);
    }

    private final List<Deal> deals = new ArrayList<Deal>();
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

        StatusChips.bind(binding.layoutStatusChip.textStatusChip, deal.getStatus());

        boolean overdue = isOverdue(deal, Timestamp.now());
        binding.chipOverdue.setVisibility(overdue ? View.VISIBLE : View.GONE);

        String buyerId = deal.getBuyerId() != null ? deal.getBuyerId() : "";
        String sellerId = deal.getSellerId() != null ? deal.getSellerId() : "";
        binding.textParties.setText(context.getString(R.string.admin_deal_monitor_parties, buyerId, sellerId));

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

    static class DealViewHolder extends RecyclerView.ViewHolder {
        final ItemAdminDealBinding binding;

        DealViewHolder(ItemAdminDealBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
