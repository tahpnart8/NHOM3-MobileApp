package com.nhom3.pubgapp.ui.listing;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom3.pubgapp.databinding.ItemListingPostImageBinding;

import java.util.ArrayList;
import java.util.List;

/** Adapter showing selected image thumbnails for screen B04. */
public class PostImageAdapter extends RecyclerView.Adapter<PostImageAdapter.ImageViewHolder> {

    /** Callback when user clicks the remove button on an image thumbnail. */
    public interface OnRemoveImageListener {
        void onRemoveImage(int position);
    }

    private final List<Uri> images = new ArrayList<Uri>();
    private final OnRemoveImageListener removeListener;

    public PostImageAdapter(OnRemoveImageListener removeListener) {
        this.removeListener = removeListener;
    }

    public void setImages(List<Uri> newImages) {
        images.clear();
        if (newImages != null) {
            images.addAll(newImages);
        }
        notifyDataSetChanged();
    }

    public List<Uri> getImages() {
        return new ArrayList<Uri>(images);
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemListingPostImageBinding binding = ItemListingPostImageBinding.inflate(inflater, parent, false);
        return new ImageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri uri = images.get(position);
        holder.binding.imageThumbnail.setImageURI(uri);

        final int pos = position;
        holder.binding.buttonRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (removeListener != null) {
                    removeListener.onRemoveImage(pos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        final ItemListingPostImageBinding binding;

        ImageViewHolder(ItemListingPostImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
