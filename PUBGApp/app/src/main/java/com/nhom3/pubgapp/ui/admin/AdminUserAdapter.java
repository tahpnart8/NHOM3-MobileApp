package com.nhom3.pubgapp.ui.admin;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.ItemAdminUserBinding;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.ui.common.StatusChips;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Rows of the user list on E02.
public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    private final List<User> users = new ArrayList<User>();
    private final OnUserClickListener clickListener;

    public AdminUserAdapter(OnUserClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void submitUsers(List<User> newUsers) {
        users.clear();
        if (newUsers != null) {
            users.addAll(newUsers);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemAdminUserBinding binding = ItemAdminUserBinding.inflate(inflater, parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        private final ItemAdminUserBinding binding;

        UserViewHolder(ItemAdminUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final User user) {
            Context context = binding.getRoot().getContext();
            String name = user.getDisplayName();
            boolean hasName = name != null && !name.trim().isEmpty();
            binding.textUserName.setText(hasName ? name : context.getString(R.string.admin_user_list_no_name));
            binding.includeAvatar.textInitial.setText(initialOf(name));
            binding.textUserPhone.setText(AdminUserFilterRules.maskPhone(user.getPhone()));
            binding.textUserJoined.setText(context.getString(R.string.admin_user_list_joined,
                    joinedText(user)));

            StatusChips.bind(binding.includeStatusChip.textStatusChip, user.getStatus());
            bindWarningChip(context, user.getWarningCount() > 0);

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.onUserClick(user);
                }
            });
        }

        private String initialOf(String name) {
            if (name == null || name.trim().isEmpty()) {
                return "";
            }
            return name.trim().substring(0, 1).toUpperCase(Locale.ROOT);
        }

        private String joinedText(User user) {
            if (user.getCreatedAt() == null) {
                return "";
            }
            return AdminUserFilterRules.formatDate(user.getCreatedAt().toDate());
        }

        // Orange chip, same colours StatusChips gives a warning-level label.
        private void bindWarningChip(Context context, boolean warned) {
            TextView chip = binding.includeWarningChip.textStatusChip;
            if (!warned) {
                chip.setVisibility(View.GONE);
                return;
            }
            chip.setVisibility(View.VISIBLE);
            chip.setText(R.string.admin_user_list_warned_chip);
            chip.setTextColor(ContextCompat.getColor(context, R.color.warning));
            chip.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.chip_background_beige)));
        }
    }
}
