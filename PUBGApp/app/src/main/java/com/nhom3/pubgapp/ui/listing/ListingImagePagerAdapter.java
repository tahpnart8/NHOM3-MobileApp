package com.nhom3.pubgapp.ui.listing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom3.pubgapp.databinding.ItemListingDetailImageBinding;
import com.nhom3.pubgapp.ui.common.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Adapter displaying full listing images in horizontal carousel for B03. */
public class ListingImagePagerAdapter extends RecyclerView.Adapter<ListingImagePagerAdapter.ImageViewHolder> {

    private static final int MAX_TARGET_SIZE_PX = 1024;
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

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

    public boolean isOnlyCoverThumb() {
        return images.isEmpty() && coverThumbBase64 != null && !coverThumbBase64.isEmpty();
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
            final byte[] bytes = images.get(position);
            bindBytesImage(holder.binding.imageSlide, bytes);
        } else if (coverThumbBase64 != null && !coverThumbBase64.isEmpty()) {
            ImageLoader.load(coverThumbBase64, holder.binding.imageSlide, 0, 0);
        } else {
            holder.binding.imageSlide.setImageDrawable(null);
        }
    }

    private void bindBytesImage(@NonNull final ImageView imageView, @Nullable final byte[] bytes) {
        imageView.setTag(bytes);
        if (bytes == null || bytes.length == 0) {
            imageView.setImageDrawable(null);
            return;
        }

        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = decodeBytes(bytes, MAX_TARGET_SIZE_PX);
                MAIN_HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        if (bytes.equals(imageView.getTag())) {
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                });
            }
        });
    }

    @Nullable
    private static Bitmap decodeBytes(@NonNull byte[] bytes, int targetSize) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bounds);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight, targetSize);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    private static int calculateSampleSize(int width, int height, int targetSize) {
        int sampleSize = 1;
        if (width <= 0 || height <= 0) {
            return sampleSize;
        }
        while (width / (sampleSize * 2) >= targetSize && height / (sampleSize * 2) >= targetSize) {
            sampleSize = sampleSize * 2;
        }
        return sampleSize;
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
