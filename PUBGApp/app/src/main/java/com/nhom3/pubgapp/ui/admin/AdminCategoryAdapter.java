package com.nhom3.pubgapp.ui.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.ItemAdminCategoryBinding;
import com.nhom3.pubgapp.model.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Rows of the category tree in E05: shows root and indented child categories, listing counts, and options.
public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryActionListener {
        void onEdit(Category category);
        void onDelete(Category category);
        void onToggleActive(Category category, boolean active);
    }

    private final List<Category> categories = new ArrayList<Category>();
    private final Map<String, Integer> counts = new HashMap<String, Integer>();
    private final OnCategoryActionListener listener;
    private boolean actionsEnabled = true;

    public AdminCategoryAdapter(OnCategoryActionListener listener) {
        this.listener = listener;
    }

    public void setData(List<Category> newCategories) {
        categories.clear();
        if (newCategories != null) {
            categories.addAll(AdminCategoryRules.buildTreeList(newCategories));
        }
        notifyDataSetChanged();
    }

    public void setCategoryCounts(Map<String, Integer> newCounts) {
        counts.clear();
        if (newCounts != null) {
            counts.putAll(newCounts);
        }
        notifyDataSetChanged();
    }

    public void setActionsEnabled(boolean enabled) {
        if (actionsEnabled == enabled) {
            return;
        }
        actionsEnabled = enabled;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new CategoryViewHolder(ItemAdminCategoryBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        final Category category = categories.get(position);
        ItemAdminCategoryBinding binding = holder.binding;
        Context context = binding.getRoot().getContext();

        String name = category.getName() != null ? category.getName() : "";
        binding.textCategoryName.setText(name);

        if (category.isRoot()) {
            binding.viewIndent.setVisibility(View.GONE);
            binding.textChildBadge.setVisibility(View.GONE);
        } else {
            binding.viewIndent.setVisibility(View.VISIBLE);
            binding.textChildBadge.setVisibility(View.VISIBLE);
        }

        int count = 0;
        String catId = category.getId();
        if (catId != null && counts.containsKey(catId)) {
            Integer c = counts.get(catId);
            if (c != null) {
                count = c;
            }
        }
        binding.textListingCount.setText(context.getString(R.string.admin_category_listings_count, count));

        binding.switchActive.setOnCheckedChangeListener(null);
        binding.switchActive.setChecked(category.isActive());
        binding.switchActive.setEnabled(actionsEnabled);
        binding.switchActive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    listener.onToggleActive(category, isChecked);
                }
            }
        });

        binding.buttonOptions.setEnabled(actionsEnabled);
        binding.buttonOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v, category);
            }
        });
    }

    private void showPopupMenu(View view, final Category category) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.getMenu().add(0, 1, 0, R.string.admin_category_edit);
        popup.getMenu().add(0, 2, 1, R.string.admin_category_delete);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == 1) {
                    listener.onEdit(category);
                    return true;
                } else if (item.getItemId() == 2) {
                    listener.onDelete(category);
                    return true;
                }
                return false;
            }
        });
        popup.show();
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        final ItemAdminCategoryBinding binding;

        CategoryViewHolder(ItemAdminCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
