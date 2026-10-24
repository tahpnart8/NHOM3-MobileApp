package com.nhom3.pubgapp.ui.listing;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.ItemListingShopBinding;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.ui.common.ImageLoader;
import com.nhom3.pubgapp.ui.common.StatusChips;
import com.nhom3.pubgapp.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

// Adapter displaying seller listings on screen B05.
public class ShopListingAdapter extends RecyclerView.Adapter<ShopListingAdapter.ShopListingViewHolder> {

    public interface OnListingClickListener {
        void onListingClick(Listing listing);
    }

    public interface OnListingMenuClickListener {
        void onListingMenuClick(View anchorView, Listing listing);
    }

    private final List<Listing> listings = new ArrayList<Listing>();
    private final FormatUtils formatUtils = new FormatUtils();
    private final OnListingClickListener clickListener;
    private final OnListingMenuClickListener menuClickListener;

    public ShopListingAdapter(OnListingClickListener clickListener,
                              OnListingMenuClickListener menuClickListener) {
        this.clickListener = clickListener;
        this.menuClickListener = menuClickListener;
    }

    public void setListings(List<Listing> newListings) {
        listings.clear();
        if (newListings != null) {
            listings.addAll(newListings);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ShopListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemListingShopBinding binding = ItemListingShopBinding.inflate(inflater, parent, false);
        return new ShopListingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopListingViewHolder holder, int position) {
        final Listing listing = listings.get(position);

        holder.binding.textTitle.setText(listing.getTitle());
        holder.binding.textPrice.setText(formatUtils.formatPrice(listing.getPrice()));

        ImageLoader.load(listing.getCoverThumb(), holder.binding.imageThumb,
                R.drawable.ic_inbox, R.drawable.ic_error);

        StatusChips.bind(holder.binding.layoutStatusChip.textStatusChip, listing.getStatus());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.onListingClick(listing);
                }
            }
        });

        holder.binding.buttonItemMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (menuClickListener != null) {
                    menuClickListener.onListingMenuClick(v, listing);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    static class ShopListingViewHolder extends RecyclerView.ViewHolder {
        final ItemListingShopBinding binding;

        ShopListingViewHolder(ItemListingShopBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
