package com.nhom3.pubgapp.ui.listing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom3.pubgapp.databinding.ItemListingPostImageBinding;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Adapter showing selected image thumbnails for screen B04. */
public class PostImageAdapter extends RecyclerView.Adapter<PostImageAdapter.ImageViewHolder> {

    /** Callback when user clicks the remove button on an image thumbnail. */
    public interface OnRemoveImageListener {
        void onRemoveImage(int position);
    }

    private final List<Uri> images = new ArrayList<Uri>();
    private final OnRemoveImageListener removeListener;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

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
    public void onBindViewHolder(@NonNull final ImageViewHolder holder, int position) {
        final Uri uri = images.get(position);
        holder.binding.imageThumbnail.setTag(uri);
        holder.binding.imageThumbnail.setImageDrawable(null);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = decodeThumbnail(holder.binding.getRoot().getContext(), uri);
                holder.binding.imageThumbnail.post(new Runnable() {
                    @Override
                    public void run() {
                        if (uri.equals(holder.binding.imageThumbnail.getTag())) {
                            holder.binding.imageThumbnail.setImageBitmap(bitmap);
                        }
                    }
                });
            }
        });

        holder.binding.buttonRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && removeListener != null) {
                    removeListener.onRemoveImage(pos);
                }
            }
        });
    }

    private static Bitmap decodeThumbnail(Context context, Uri uri) {
        if (context == null || uri == null) {
            return null;
        }
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
            boundsOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, boundsOptions);
            if (is != null) {
                is.close();
            }

            int inSampleSize = 1;
            int maxDim = Math.max(boundsOptions.outWidth, boundsOptions.outHeight);
            int targetDim = 176;
            while (maxDim / (inSampleSize * 2) >= targetDim) {
                inSampleSize *= 2;
            }

            BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
            decodeOptions.inSampleSize = inSampleSize;
            is = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, decodeOptions);
            return bitmap;
        } catch (Exception e) {
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception ignored) {
                }
            }
        }
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
