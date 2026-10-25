package com.nhom3.pubgapp.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.ItemAdminAccountBinding;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.ui.common.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/** Adapter for displaying admin account list on screen E07. */
public class AdminAccountAdapter extends RecyclerView.Adapter<AdminAccountAdapter.AdminViewHolder> {

    public interface OnRevokeAdminClickListener {
        void onRevokeClick(User user);
    }

    private final List<User> items = new ArrayList<User>();
    private String currentAdminId = "";
    private OnRevokeAdminClickListener listener;

    public void setItems(List<User> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void setCurrentAdminId(String adminId) {
        this.currentAdminId = adminId != null ? adminId : "";
        notifyDataSetChanged();
    }

    public void setListener(OnRevokeAdminClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemAdminAccountBinding binding = ItemAdminAccountBinding.inflate(inflater, parent, false);
        return new AdminViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminViewHolder holder, int position) {
        User user = items.get(position);
        holder.bind(user, currentAdminId, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AdminViewHolder extends RecyclerView.ViewHolder {

        private final ItemAdminAccountBinding binding;

        AdminViewHolder(@NonNull ItemAdminAccountBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final User user, String currentAdminId, final OnRevokeAdminClickListener listener) {
            if (user == null) {
                return;
            }

            String displayName = user.getDisplayName();
            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = user.getEmail();
            }
            binding.textDisplayName.setText(displayName != null ? displayName : "");
            binding.textEmail.setText(user.getEmail() != null ? user.getEmail() : "");

            bindAvatar(user, displayName);

            boolean isSelf = user.getId() != null && user.getId().equals(currentAdminId);
            if (isSelf) {
                binding.buttonRevokeAdmin.setVisibility(View.GONE);
                binding.textSelfLabel.setVisibility(View.VISIBLE);
            } else {
                binding.buttonRevokeAdmin.setVisibility(View.VISIBLE);
                binding.textSelfLabel.setVisibility(View.GONE);
                binding.buttonRevokeAdmin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.onRevokeClick(user);
                        }
                    }
                });
            }
        }

        private void bindAvatar(User user, String displayName) {
            String avatar = user.getAvatarBase64();
            if (avatar != null && !avatar.trim().isEmpty()) {
                binding.includeAvatar.imageAvatar.setVisibility(View.VISIBLE);
                binding.includeAvatar.textInitial.setVisibility(View.GONE);
                ImageLoader.load(avatar, binding.includeAvatar.imageAvatar,
                        R.drawable.bg_avatar, R.drawable.bg_avatar);
            } else {
                binding.includeAvatar.imageAvatar.setVisibility(View.GONE);
                binding.includeAvatar.textInitial.setVisibility(View.VISIBLE);
                String initial = "";
                if (displayName != null && !displayName.trim().isEmpty()) {
                    initial = displayName.trim().substring(0, 1).toUpperCase();
                }
                binding.includeAvatar.textInitial.setText(initial);
            }
        }
    }
}
