package com.nhom3.pubgapp.ui.listing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.ItemListingShopBinding;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.enums.ListingStatus;
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
        Context context = holder.itemView.getContext();

        holder.binding.textTitle.setText(listing.getTitle());
        holder.binding.textPrice.setText(formatUtils.formatPrice(listing.getPrice()));

        // Decode cover thumbnail if present
        String coverThumb = listing.getCoverThumb();
        if (coverThumb != null && !coverThumb.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(coverThumb, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.binding.imageThumb.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.binding.imageThumb.setImageDrawable(null);
            }
        } else {
            holder.binding.imageThumb.setImageDrawable(null);
        }

        bindStatusChip(holder, context, listing.getStatus());

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

    private void bindStatusChip(ShopListingViewHolder holder, Context context, ListingStatus status) {
        if (status == null) {
            holder.binding.layoutStatusChip.textStatusChip.setVisibility(View.GONE);
            return;
        }
        holder.binding.layoutStatusChip.textStatusChip.setVisibility(View.VISIBLE);
        int textColorRes;
        int textRes;
        if (status == ListingStatus.AVAILABLE) {
            textRes = R.string.listing_shop_status_available;
            textColorRes = R.color.success;
        } else if (status == ListingStatus.RESERVED) {
            textRes = R.string.listing_shop_status_reserved;
            textColorRes = R.color.warning;
        } else if (status == ListingStatus.SOLD) {
            textRes = R.string.listing_shop_status_sold;
            textColorRes = R.color.on_surface_variant;
        } else if (status == ListingStatus.HIDDEN) {
            textRes = R.string.listing_shop_status_hidden;
            textColorRes = R.color.on_surface_variant;
        } else if (status == ListingStatus.REMOVED) {
            textRes = R.string.listing_shop_status_removed;
            textColorRes = R.color.error;
        } else {
            textRes = R.string.listing_shop_status_draft;
            textColorRes = R.color.on_surface_variant;
        }

        holder.binding.layoutStatusChip.textStatusChip.setText(textRes);
        int color = ContextCompat.getColor(context, textColorRes);
        holder.binding.layoutStatusChip.textStatusChip.setTextColor(color);
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
