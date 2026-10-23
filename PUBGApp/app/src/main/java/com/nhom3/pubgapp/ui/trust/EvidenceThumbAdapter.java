package com.nhom3.pubgapp.ui.trust;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom3.pubgapp.databinding.ItemTrustEvidenceThumbBinding;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/** Horizontal strip of picked evidence images with a remove button; the cap is set by the screen. */
public class EvidenceThumbAdapter extends RecyclerView.Adapter<EvidenceThumbAdapter.ThumbViewHolder> {

    private static final int PREVIEW_SAMPLE_SIZE = 8;

    private final List<Uri> uris = new ArrayList<Uri>();
    private final int maxImages;
    // Called after a remove, so the screen can enable its add button again.
    private final Runnable onChanged;

    public EvidenceThumbAdapter(int maxImages, Runnable onChanged) {
        this.maxImages = maxImages;
        this.onChanged = onChanged;
    }

    public List<Uri> getUris() {
        return new ArrayList<Uri>(uris);
    }

    public boolean isFull() {
        return uris.size() >= maxImages;
    }

    public void setUris(List<Uri> newUris) {
        uris.clear();
        if (newUris != null) {
            for (Uri uri : newUris) {
                if (uris.size() < maxImages) {
                    uris.add(uri);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void addUri(Uri uri) {
        if (uri == null || isFull()) {
            return;
        }
        uris.add(uri);
        notifyItemInserted(uris.size() - 1);
    }

    private void removeAt(int position) {
        if (position < 0 || position >= uris.size()) {
            return;
        }
        uris.remove(position);
        notifyItemRemoved(position);
        onChanged.run();
    }

    @NonNull
    @Override
    public ThumbViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemTrustEvidenceThumbBinding binding = ItemTrustEvidenceThumbBinding.inflate(inflater, parent, false);
        return new ThumbViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ThumbViewHolder holder, int position) {
        holder.bind(uris.get(position));
        holder.binding.buttonRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                removeAt(holder.getBindingAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return uris.size();
    }

    static final class ThumbViewHolder extends RecyclerView.ViewHolder {

        private final ItemTrustEvidenceThumbBinding binding;

        ThumbViewHolder(ItemTrustEvidenceThumbBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // Decoded off the main thread; the tag drops a result meant for a recycled row.
        void bind(Uri uri) {
            ImageView target = binding.imageThumb;
            target.setTag(uri);
            target.setImageDrawable(null);
            Context appContext = target.getContext().getApplicationContext();
            Thread worker = new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = decodePreview(appContext, uri);
                    postThumb(target, uri, bitmap);
                }
            });
            worker.start();
        }

        private static Bitmap decodePreview(Context context, Uri uri) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = PREVIEW_SAMPLE_SIZE;
            try (InputStream input = context.getContentResolver().openInputStream(uri)) {
                return BitmapFactory.decodeStream(input, null, options);
            } catch (IOException | SecurityException readError) {
                return null;
            }
        }

        private static void postThumb(ImageView target, Uri uri, Bitmap bitmap) {
            target.post(new Runnable() {
                @Override
                public void run() {
                    if (bitmap == null || !uri.equals(target.getTag())) {
                        return;
                    }
                    target.setImageBitmap(bitmap);
                }
            });
        }
    }
}
