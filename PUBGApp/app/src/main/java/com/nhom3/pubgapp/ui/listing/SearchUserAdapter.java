package com.nhom3.pubgapp.ui.listing;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.ItemSearchUserBinding;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.ui.common.ImageLoader;

import java.util.ArrayList;
import java.util.List;

// Adapter for user search results on screen B02 SearchFragment.
public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    private final List<User> items = new ArrayList<User>();
    private OnUserClickListener listener;

    public void setListener(OnUserClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<User> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemSearchUserBinding binding = ItemSearchUserBinding.inflate(inflater, parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        final User user = items.get(position);
        holder.bind(user, listener);
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {

        private final ItemSearchUserBinding binding;

        UserViewHolder(@NonNull ItemSearchUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final User user, final OnUserClickListener listener) {
            String displayName = user.getDisplayName();
            binding.textDisplayName.setText(displayName != null ? displayName : "");

            ImageLoader.load(user.getAvatarBase64(), binding.imageAvatar,
                    R.drawable.bg_avatar, R.drawable.bg_avatar);

            int ratingCount = user.getRatingCount();
            if (ratingCount > 0) {
                double avg = (double) user.getRatingSum() / (double) ratingCount;
                String ratingText = binding.getRoot().getContext().getString(
                        R.string.listing_search_user_rating_format, avg, ratingCount);
                binding.textRatingInfo.setText(ratingText);
            } else {
                binding.textRatingInfo.setText(R.string.listing_search_user_no_rating);
            }

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onUserClick(user);
                    }
                }
            });
        }
    }
}
