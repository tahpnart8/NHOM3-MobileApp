package com.nhom3.pubgapp.ui.listing;

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
import com.nhom3.pubgapp.ui.common.ImageLoader;
import com.nhom3.pubgapp.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

/** Two-column grid card adapter for listings on B01, B02, and B06. */
public class ListingCardAdapter extends RecyclerView.Adapter<ListingCardAdapter.ListingViewHolder> {

    private static final String ARG_LISTING_ID = "listingId";

    public interface OnListingClickListener {
        void onListingClick(Listing listing);
    }

    private final List<Listing> listings = new ArrayList<Listing>();
    private final FormatUtils formatUtils = new FormatUtils();
    private OnListingClickListener clickListener;

    public void setOnListingClickListener(OnListingClickListener listener) {
        this.clickListener = listener;
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
    public ListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemListingCardBinding binding = ItemListingCardBinding.inflate(inflater, parent, false);
        return new ListingViewHolder(binding, clickListener, formatUtils);
    }

    @Override
    public void onBindViewHolder(@NonNull ListingViewHolder holder, int position) {
        holder.bind(listings.get(position));
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    static int conditionLabel(ItemCondition condition) {
        if (condition == ItemCondition.NEW) {
            return R.string.listing_card_condition_new;
        }
        if (condition == ItemCondition.LIKE_NEW) {
            return R.string.listing_card_condition_like_new;
        }
        if (condition == ItemCondition.REPAIRED) {
            return R.string.listing_card_condition_repaired;
        }
        if (condition == ItemCondition.DEFECTIVE) {
            return R.string.listing_card_condition_defective;
        }
        return R.string.listing_card_condition_used;
    }

    static final class ListingViewHolder extends RecyclerView.ViewHolder {

        private final ItemListingCardBinding binding;
        private final OnListingClickListener clickListener;
        private final FormatUtils formatUtils;

        ListingViewHolder(ItemListingCardBinding binding,
                          OnListingClickListener clickListener,
                          FormatUtils formatUtils) {
            super(binding.getRoot());
            this.binding = binding;
            this.clickListener = clickListener;
            this.formatUtils = formatUtils;
        }

        void bind(final Listing listing) {
            binding.textTitle.setText(listing.getTitle());
            binding.textPrice.setText(formatUtils.formatPrice(listing.getPrice()));
            binding.textCondition.setText(conditionLabel(listing.getItemCondition()));
            binding.textProvince.setText(listing.getProvince());

            // Status chip is not needed on the general feed
            binding.includeStatusChip.getRoot().setVisibility(View.GONE);

            ImageLoader.load(listing.getCoverThumb(), binding.imageCover,
                    R.drawable.ic_inbox, R.drawable.ic_error);

            binding.cardListing.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.onListingClick(listing);
                        return;
                    }
                    if (listing.getId() != null && !listing.getId().isEmpty()) {
                        Bundle args = new Bundle();
                        args.putString(ARG_LISTING_ID, listing.getId());
                        Navigation.findNavController(v).navigate(
                                R.id.action_global_listing_detail, args);
                    }
                }
            });
        }
    }
}
