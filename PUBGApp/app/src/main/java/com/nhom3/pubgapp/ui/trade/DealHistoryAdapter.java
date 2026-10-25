package com.nhom3.pubgapp.ui.trade;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.ItemDealHistoryBinding;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.ui.common.ImageLoader;
import com.nhom3.pubgapp.ui.common.StatusChips;
import com.nhom3.pubgapp.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

// Adapter for the deal history list in C04.
public class DealHistoryAdapter extends RecyclerView.Adapter<DealHistoryAdapter.DealViewHolder> {

    public static final int FILTER_ALL = 0;
    public static final int FILTER_IN_PROGRESS = 1;
    public static final int FILTER_COMPLETED = 2;
    public static final int FILTER_CANCELLED = 3;

    public interface OnDealClickListener {
        void onDealClick(Deal deal);
    }

    private final List<Deal> allDeals = new ArrayList<Deal>();
    private final List<Deal> displayedDeals = new ArrayList<Deal>();
    private final OnDealClickListener listener;
    private final FormatUtils formatUtils = new FormatUtils();
    private int currentFilter = FILTER_ALL;

    public DealHistoryAdapter(OnDealClickListener listener) {
        this.listener = listener;
    }

    public static List<Deal> filterDeals(List<Deal> source, int filter) {
        if (source == null) {
            return new ArrayList<Deal>();
        }
        if (filter == FILTER_ALL) {
            return new ArrayList<Deal>(source);
        }
        List<Deal> filtered = new ArrayList<Deal>();
        for (int i = 0; i < source.size(); i++) {
            Deal d = source.get(i);
            if (matchesFilter(d, filter)) {
                filtered.add(d);
            }
        }
        return filtered;
    }

    public static boolean matchesFilter(Deal deal, int filter) {
        if (deal == null || deal.getStatus() == null) {
            return false;
        }
        DealStatus status = deal.getStatus();
        if (filter == FILTER_ALL) {
            return true;
        }
        if (filter == FILTER_IN_PROGRESS) {
            return status == DealStatus.PENDING_CONFIRM
                    || status == DealStatus.CONFIRMED
                    || status == DealStatus.SHIPPED
                    || status == DealStatus.DISPUTED;
        }
        if (filter == FILTER_COMPLETED) {
            return status == DealStatus.RECEIVED
                    || status == DealStatus.COMPLETED;
        }
        if (filter == FILTER_CANCELLED) {
            return status == DealStatus.CANCELLED;
        }
        return false;
    }

    public void submitDeals(List<Deal> deals) {
        allDeals.clear();
        if (deals != null) {
            allDeals.addAll(deals);
        }
        applyFilter();
    }

    public void setFilter(int filter) {
        this.currentFilter = filter;
        applyFilter();
    }

    public int getDisplayedItemCount() {
        return displayedDeals.size();
    }

    public int getAllItemCount() {
        return allDeals.size();
    }

    private void applyFilter() {
        displayedDeals.clear();
        displayedDeals.addAll(filterDeals(allDeals, currentFilter));
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new DealViewHolder(ItemDealHistoryBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DealViewHolder holder, int position) {
        final Deal deal = displayedDeals.get(position);
        ItemDealHistoryBinding binding = holder.binding;
        Context context = binding.getRoot().getContext();

        ImageLoader.load(deal.getItemThumb(), binding.imageThumb, R.drawable.ic_inbox, R.drawable.ic_error);

        String title = deal.getItemTitle();
        binding.textTitle.setText(title != null ? title : "");

        binding.textPrice.setText(formatUtils.formatPrice(deal.getAgreedPrice()));

        StatusChips.bind(binding.layoutStatusChip.textStatusChip, deal.getStatus());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = user != null ? user.getUid() : "";
        boolean isBuyer = currentUid.equals(deal.getBuyerId());
        String roleLabel = context.getString(isBuyer
                ? R.string.trade_deal_detail_role_buyer
                : R.string.trade_deal_detail_role_seller);
        binding.textRole.setText(context.getString(R.string.trade_deal_history_role_prefix, roleLabel));

        Timestamp createdAt = deal.getCreatedAt();
        binding.textDate.setText(createdAt != null ? formatUtils.formatTime(createdAt) : "");

        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDealClick(deal);
            }
        });
    }

    @Override
    public int getItemCount() {
        return displayedDeals.size();
    }

    static class DealViewHolder extends RecyclerView.ViewHolder {
        final ItemDealHistoryBinding binding;

        DealViewHolder(ItemDealHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
