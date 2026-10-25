package com.nhom3.pubgapp.ui.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.ItemAdminFlaggedListingBinding;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.ui.common.ImageLoader;

import java.util.ArrayList;
import java.util.List;

// Rows of the flagged tab in E04: a listing that matched a banned keyword, with Clear flag and Hide.
public class AdminFlaggedAdapter extends RecyclerView.Adapter<AdminFlaggedAdapter.FlaggedViewHolder> {

    public interface OnFlaggedActionListener {
        void onOpen(Listing listing);

        void onClearFlag(Listing listing);

        void onHide(Listing listing);
    }

    private final List<Listing> listings = new ArrayList<Listing>();
    private final OnFlaggedActionListener listener;
    private boolean actionsEnabled = true;

    public AdminFlaggedAdapter(OnFlaggedActionListener listener) {
        this.listener = listener;
    }

    public void setData(List<Listing> newListings) {
        listings.clear();
        listings.addAll(newListings);
        notifyDataSetChanged();
    }

    public void setActionsEnabled(boolean enabled) {
        if (actionsEnabled == enabled) {
            return;
        }
        actionsEnabled = enabled;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FlaggedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new FlaggedViewHolder(ItemAdminFlaggedListingBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FlaggedViewHolder holder, int position) {
        final Listing listing = listings.get(position);
        ItemAdminFlaggedListingBinding binding = holder.binding;
        Context context = binding.getRoot().getContext();
        binding.textFlaggedTitle.setText(listing.getTitle());
        String posted = AdminHistoryAdapter.formatDate(listing.getCreatedAt(), AdminHistoryAdapter.DATE_PATTERN);
        binding.textFlaggedDate.setText(context.getString(R.string.admin_moderation_flagged_posted, posted));
        ImageLoader.load(listing.getCoverThumb(), binding.imageFlaggedThumb, R.drawable.ic_inbox,
                R.drawable.ic_error);
        boolean canHide = AdminListingActionRules.canHide(listing.getStatus());
        binding.buttonClearFlag.setEnabled(actionsEnabled);
        binding.buttonHideFlagged.setEnabled(actionsEnabled && canHide);
        binding.buttonClearFlag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClearFlag(listing);
            }
        });
        binding.buttonHideFlagged.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onHide(listing);
            }
        });
        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onOpen(listing);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    static class FlaggedViewHolder extends RecyclerView.ViewHolder {
        final ItemAdminFlaggedListingBinding binding;

        FlaggedViewHolder(ItemAdminFlaggedListingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
