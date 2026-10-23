package com.nhom3.pubgapp.ui.listing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom3.pubgapp.databinding.ItemListingDetailImageBinding;

import java.util.ArrayList;
import java.util.List;

/** Adapter displaying full listing images in horizontal carousel for B03. */
public class ListingImagePagerAdapter extends RecyclerView.Adapter<ListingImagePagerAdapter.ImageViewHolder> {

    private final List<byte[]> images = new ArrayList<byte[]>();
    private String coverThumbBase64;

    public void setImages(List<byte[]> newImages) {
        images.clear();
        if (newImages != null) {
            images.addAll(newImages);
        }
        notifyDataSetChanged();
    }

    public void setCoverThumb(String thumb) {
        this.coverThumbBase64 = thumb;
        if (images.isEmpty()) {
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemListingDetailImageBinding binding = ItemListingDetailImageBinding.inflate(inflater, parent, false);
        return new ImageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        if (!images.isEmpty()) {
            byte[] bytes = images.get(position);
            if (bytes != null && bytes.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                holder.binding.imageSlide.setImageBitmap(bitmap);
            } else {
                holder.binding.imageSlide.setImageDrawable(null);
            }
        } else if (coverThumbBase64 != null && !coverThumbBase64.isEmpty()) {
            try {
                byte[] bytes = Base64.decode(coverThumbBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                holder.binding.imageSlide.setImageBitmap(bitmap);
            } catch (IllegalArgumentException e) {
                holder.binding.imageSlide.setImageDrawable(null);
            }
        } else {
            holder.binding.imageSlide.setImageDrawable(null);
        }
    }

    @Override
    public int getItemCount() {
        if (!images.isEmpty()) {
            return images.size();
        }
        if (coverThumbBase64 != null && !coverThumbBase64.isEmpty()) {
            return 1;
        }
        return 0;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        final ItemListingDetailImageBinding binding;

        ImageViewHolder(ItemListingDetailImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
