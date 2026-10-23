package com.nhom3.pubgapp.ui.account;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom3.pubgapp.databinding.ItemAccountCategoryChoiceBinding;
import com.nhom3.pubgapp.model.Category;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Adapter for selecting interested categories in registration step 3.
public class CategoryChoiceAdapter extends RecyclerView.Adapter<CategoryChoiceAdapter.ViewHolder> {

    private final List<Category> items = new ArrayList<Category>();
    private final Set<String> selectedIds = new HashSet<String>();

    public void submitList(List<Category> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public List<String> getSelectedCategoryIds() {
        return new ArrayList<String>(selectedIds);
    }

    public void setSelectedCategoryIds(List<String> ids) {
        selectedIds.clear();
        if (ids != null) {
            selectedIds.addAll(ids);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemAccountCategoryChoiceBinding binding =
                ItemAccountCategoryChoiceBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = items.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAccountCategoryChoiceBinding binding;

        ViewHolder(ItemAccountCategoryChoiceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Category category) {
            binding.textCategoryName.setText(category.getName());

            final String catId = category.getId();
            binding.checkCategory.setOnCheckedChangeListener(null);
            binding.checkCategory.setChecked(catId != null && selectedIds.contains(catId));

            binding.checkCategory.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (catId == null) {
                        return;
                    }
                    if (isChecked) {
                        selectedIds.add(catId);
                    } else {
                        selectedIds.remove(catId);
                    }
                }
            });

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    binding.checkCategory.toggle();
                }
            });
        }
    }
}
