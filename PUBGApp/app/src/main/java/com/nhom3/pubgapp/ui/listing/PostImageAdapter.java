package com.nhom3.pubgapp.ui.listing;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.MediaRepository;
import com.nhom3.pubgapp.databinding.ItemListingPostImageBinding;
import com.nhom3.pubgapp.ui.common.ImageLoader;

import java.util.ArrayList;
import java.util.List;

// Adapter showing selected image thumbnails for screen B04.
public class PostImageAdapter extends RecyclerView.Adapter<PostImageAdapter.ImageViewHolder> {

    public static class ImageEntry {
        private final String imageId;
        private final Uri uri;

        public ImageEntry(String imageId) {
            this.imageId = imageId;
            this.uri = null;
        }

        public ImageEntry(Uri uri) {
            this.imageId = null;
            this.uri = uri;
        }

        public boolean isExisting() {
            return imageId != null;
        }

        public String getImageId() {
            return imageId;
        }

        public Uri getUri() {
            return uri;
        }
    }

    public interface OnRemoveImageListener {
        void onRemoveImage(int position);
    }

    private final List<ImageEntry> items = new ArrayList<ImageEntry>();
    private final OnRemoveImageListener removeListener;
    private final MediaRepository mediaRepository = new MediaRepository();

    public PostImageAdapter(OnRemoveImageListener removeListener) {
        this.removeListener = removeListener;
    }

    public void setImages(List<Uri> newImages) {
        items.clear();
        if (newImages != null) {
            for (Uri uri : newImages) {
                items.add(new ImageEntry(uri));
            }
        }
        notifyDataSetChanged();
    }

    public void setInitialImages(List<String> existingIds, List<Uri> newImages) {
        items.clear();
        if (existingIds != null) {
            for (String id : existingIds) {
                items.add(new ImageEntry(id));
            }
        }
        if (newImages != null) {
            for (Uri uri : newImages) {
                items.add(new ImageEntry(uri));
            }
        }
        notifyDataSetChanged();
    }

    public List<Uri> getImages() {
        List<Uri> uris = new ArrayList<Uri>();
        for (ImageEntry entry : items) {
            if (!entry.isExisting() && entry.getUri() != null) {
                uris.add(entry.getUri());
            }
        }
        return uris;
    }

    public List<String> getExistingImageIds() {
        List<String> ids = new ArrayList<String>();
        for (ImageEntry entry : items) {
            if (entry.isExisting() && entry.getImageId() != null) {
                ids.add(entry.getImageId());
            }
        }
        return ids;
    }

    public List<Uri> getNewImageUris() {
        return getImages();
    }

    public void addImageUri(Uri uri) {
        if (uri != null && items.size() < 5) {
            items.add(new ImageEntry(uri));
            notifyItemInserted(items.size() - 1);
        }
    }

    public void removeImage(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, items.size() - position);
        }
    }

    public int getAllImageCount() {
        return items.size();
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
        final ImageEntry entry = items.get(position);
        if (entry.isExisting()) {
            final String imageId = entry.getImageId();
            mediaRepository.getImage(imageId, new DataCallback<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    ImageLoader.load(bytes, imageId, holder.binding.imageThumbnail,
                            R.drawable.ic_inbox, R.drawable.ic_error);
                }

                @Override
                public void onError(String errorMessage) {
                    ImageLoader.load((byte[]) null, null, holder.binding.imageThumbnail,
                            R.drawable.ic_inbox, R.drawable.ic_error);
                }
            });
        } else {
            ImageLoader.load(entry.getUri(), holder.binding.imageThumbnail,
                    R.drawable.ic_inbox, R.drawable.ic_error);
        }

        holder.binding.buttonRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPos = holder.getBindingAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION && removeListener != null) {
                    removeListener.onRemoveImage(adapterPos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        final ItemListingPostImageBinding binding;

        ImageViewHolder(ItemListingPostImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
