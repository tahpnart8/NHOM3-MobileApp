package com.nhom3.pubgapp.ui.listing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.ItemListingSavedBinding;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.enums.ItemCondition;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.ui.common.ImageLoader;
import com.nhom3.pubgapp.ui.common.StatusChips;
import com.nhom3.pubgapp.util.FormatUtils;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.viewmodel.ProfileViewModel;

import java.util.ArrayList;
import java.util.List;

/** B06 grid: saved listings with an unsave button; unreadable ones are dimmed and cannot be opened. */
public class SavedListingAdapter extends RecyclerView.Adapter<SavedListingAdapter.SavedViewHolder> {

    private static final String ARG_LISTING_ID = "listingId";
    private static final float UNAVAILABLE_ALPHA = 0.5f;
    private static final float AVAILABLE_ALPHA = 1f;

    private final List<Listing> listings = new ArrayList<Listing>();
    private final FormatUtils formatUtils = new FormatUtils();
    private final ProfileViewModel profileViewModel;

    public SavedListingAdapter(ProfileViewModel profileViewModel) {
        this.profileViewModel = profileViewModel;
    }

    public void submitList(List<Listing> newListings) {
        listings.clear();
        if (newListings != null) {
            listings.addAll(newListings);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SavedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemListingSavedBinding binding = ItemListingSavedBinding.inflate(inflater, parent, false);
        return new SavedViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedViewHolder holder, int position) {
        Listing listing = listings.get(position);
        boolean unavailable = isUnavailable(listing);
        holder.bind(listing, unavailable, formatUtils);
        String listingId = listing.getId();
        holder.binding.includeCard.cardListing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                openListing(clicked, listingId, unavailable);
            }
        });
        holder.binding.buttonUnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                profileViewModel.unsaveListing(listingId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    // Same rule as ProfileViewModel.isUnavailable, which is package-private and tested there.
    private static boolean isUnavailable(Listing listing) {
        ListingStatus status = listing.getStatus();
        if (status == null) {
            return true;
        }
        return status == ListingStatus.HIDDEN || status == ListingStatus.REMOVED || status == ListingStatus.DRAFT;
    }

    private static void openListing(View clicked, String listingId, boolean unavailable) {
        if (unavailable) {
            Snackbar.make(clicked, R.string.listing_saved_unavailable_message, Snackbar.LENGTH_LONG).show();
            return;
        }
        Bundle arguments = new Bundle();
        arguments.putString(ARG_LISTING_ID, listingId);
        Navigation.findNavController(clicked).navigate(R.id.action_global_listing_detail, arguments);
    }

    static int conditionLabel(ItemCondition condition) {
        if (condition == ItemCondition.NEW) {
            return R.string.listing_saved_condition_new;
        }
        if (condition == ItemCondition.LIKE_NEW) {
            return R.string.listing_saved_condition_like_new;
        }
        if (condition == ItemCondition.REPAIRED) {
            return R.string.listing_saved_condition_repaired;
        }
        if (condition == ItemCondition.DEFECTIVE) {
            return R.string.listing_saved_condition_defective;
        }
        return R.string.listing_saved_condition_used;
    }

    static final class SavedViewHolder extends RecyclerView.ViewHolder {

        private final ItemListingSavedBinding binding;

        SavedViewHolder(ItemListingSavedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Listing listing, boolean unavailable, FormatUtils formatUtils) {
            binding.includeCard.getRoot().setAlpha(unavailable ? UNAVAILABLE_ALPHA : AVAILABLE_ALPHA);
            binding.textUnavailable.setVisibility(unavailable ? View.VISIBLE : View.GONE);
            binding.buttonUnsave.setEnabled(NetworkMonitor.getInstance().isOnline());
            if (unavailable) {
                bindUnavailable();
                return;
            }
            binding.includeCard.textTitle.setText(listing.getTitle());
            binding.includeCard.textPrice.setText(formatUtils.formatPrice(listing.getPrice()));
            binding.includeCard.textCondition.setText(conditionLabel(listing.getItemCondition()));
            binding.includeCard.textProvince.setText(listing.getProvince());
            ImageLoader.load(listing.getCoverThumb(), binding.includeCard.imageCover,
                    R.drawable.ic_inbox, R.drawable.ic_error);
            // Only a sold listing carries a label here; the other live states need none.
            ListingStatus chipStatus = listing.getStatus() == ListingStatus.SOLD ? ListingStatus.SOLD : null;
            StatusChips.bind(binding.includeCard.includeStatusChip.getRoot(), chipStatus);
        }

        private void bindUnavailable() {
            binding.includeCard.textTitle.setText(R.string.listing_saved_unavailable_label);
            binding.includeCard.textPrice.setText("");
            binding.includeCard.textCondition.setText("");
            binding.includeCard.textProvince.setText("");
            ImageLoader.load((String) null, binding.includeCard.imageCover, R.drawable.ic_error, R.drawable.ic_error);
            StatusChips.bind(binding.includeCard.includeStatusChip.getRoot(), (ListingStatus) null);
        }
    }
}
