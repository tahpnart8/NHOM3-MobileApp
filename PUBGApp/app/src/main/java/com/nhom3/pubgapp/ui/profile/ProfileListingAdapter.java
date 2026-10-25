package com.nhom3.pubgapp.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.ItemListingCardBinding;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.enums.ItemCondition;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.ui.common.ImageLoader;
import com.nhom3.pubgapp.ui.common.StatusChips;
import com.nhom3.pubgapp.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

/** Two-column grid of a user's listings on D02 and D03, drawn with the shared item_listing_card. */
public class ProfileListingAdapter extends RecyclerView.Adapter<ProfileListingAdapter.ListingViewHolder> {

    private static final String ARG_LISTING_ID = "listingId";

    private final List<Listing> listings = new ArrayList<Listing>();
    private final FormatUtils formatUtils = new FormatUtils();

    public void submitList(List<Listing> newListings) {
        // A tab switch or rotation hands back the same listings; rebinding would decode every cover again.
        if (holdsSameListings(newListings)) {
            return;
        }
        listings.clear();
        if (newListings != null) {
            listings.addAll(newListings);
        }
        notifyDataSetChanged();
    }

    // Same objects in the same order: the filters keep the ViewModel's Listing instances.
    private boolean holdsSameListings(List<Listing> newListings) {
        if (newListings == null) {
            return listings.isEmpty();
        }
        if (newListings.size() != listings.size()) {
            return false;
        }
        for (int i = 0; i < listings.size(); i++) {
            if (newListings.get(i) != listings.get(i)) {
                return false;
            }
        }
        return true;
    }

    @NonNull
    @Override
    public ListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemListingCardBinding binding = ItemListingCardBinding.inflate(inflater, parent, false);
        return new ListingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ListingViewHolder holder, int position) {
        holder.bind(listings.get(position), formatUtils);
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    // The owner's own query also returns DRAFT, HIDDEN and REMOVED; a public page never shows them.
    public static List<Listing> filterSelling(List<Listing> all) {
        List<Listing> result = new ArrayList<Listing>();
        if (all == null) {
            return result;
        }
        for (Listing listing : all) {
            ListingStatus status = listing.getStatus();
            if (status == ListingStatus.AVAILABLE || status == ListingStatus.RESERVED) {
                result.add(listing);
            }
        }
        return result;
    }

    public static List<Listing> filterSold(List<Listing> all) {
        List<Listing> result = new ArrayList<Listing>();
        if (all == null) {
            return result;
        }
        for (Listing listing : all) {
            if (listing.getStatus() == ListingStatus.SOLD) {
                result.add(listing);
            }
        }
        return result;
    }

    static int conditionLabel(ItemCondition condition) {
        if (condition == ItemCondition.NEW) {
            return R.string.profile_listing_card_condition_new;
        }
        if (condition == ItemCondition.LIKE_NEW) {
            return R.string.profile_listing_card_condition_like_new;
        }
        if (condition == ItemCondition.REPAIRED) {
            return R.string.profile_listing_card_condition_repaired;
        }
        if (condition == ItemCondition.DEFECTIVE) {
            return R.string.profile_listing_card_condition_defective;
        }
        return R.string.profile_listing_card_condition_used;
    }

    static final class ListingViewHolder extends RecyclerView.ViewHolder {

        private final ItemListingCardBinding binding;

        ListingViewHolder(ItemListingCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Listing listing, FormatUtils formatUtils) {
            binding.textTitle.setText(listing.getTitle());
            binding.textPrice.setText(formatUtils.formatPrice(listing.getPrice()));
            binding.textCondition.setText(conditionLabel(listing.getItemCondition()));
            binding.textProvince.setText(listing.getProvince());
            ImageLoader.load(listing.getCoverThumb(), binding.imageCover, R.drawable.ic_inbox, R.drawable.ic_error);
            StatusChips.bind(binding.includeStatusChip.getRoot(), listing.getStatus());
            String listingId = listing.getId();
            binding.cardListing.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View clicked) {
                    openListing(clicked, listingId);
                }
            });
        }

        private static void openListing(View clicked, String listingId) {
            Bundle arguments = new Bundle();
            arguments.putString(ARG_LISTING_ID, listingId);
            Navigation.findNavController(clicked).navigate(R.id.action_global_listing_detail, arguments);
        }
    }
}
