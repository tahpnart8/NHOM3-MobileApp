package com.nhom3.pubgapp.ui.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.ItemAdminKeywordBinding;
import com.nhom3.pubgapp.model.BannedKeyword;

import java.util.ArrayList;
import java.util.List;

// Rows of the banned keyword tab in E04: the stored keyword, the day it was added and a remove button.
public class AdminKeywordAdapter extends RecyclerView.Adapter<AdminKeywordAdapter.KeywordViewHolder> {

    public interface OnKeywordRemoveListener {
        void onRemove(BannedKeyword keyword);
    }

    private final List<BannedKeyword> keywords = new ArrayList<BannedKeyword>();
    private final OnKeywordRemoveListener listener;
    private boolean actionsEnabled = true;

    public AdminKeywordAdapter(OnKeywordRemoveListener listener) {
        this.listener = listener;
    }

    public void setData(List<BannedKeyword> newKeywords) {
        keywords.clear();
        keywords.addAll(newKeywords);
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
    public KeywordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new KeywordViewHolder(ItemAdminKeywordBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull KeywordViewHolder holder, int position) {
        final BannedKeyword keyword = keywords.get(position);
        ItemAdminKeywordBinding binding = holder.binding;
        Context context = binding.getRoot().getContext();
        String word = keyword.getKeyword() == null ? "" : keyword.getKeyword();
        binding.textKeyword.setText(word);
        String added = AdminHistoryAdapter.formatDate(keyword.getCreatedAt(), AdminHistoryAdapter.DATE_PATTERN);
        binding.textKeywordDate.setText(context.getString(R.string.admin_moderation_keyword_added_on, added));
        binding.buttonRemoveKeyword.setContentDescription(
                context.getString(R.string.admin_moderation_keyword_remove_desc, word));
        binding.buttonRemoveKeyword.setEnabled(actionsEnabled);
        binding.buttonRemoveKeyword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onRemove(keyword);
            }
        });
    }

    @Override
    public int getItemCount() {
        return keywords.size();
    }

    static class KeywordViewHolder extends RecyclerView.ViewHolder {
        final ItemAdminKeywordBinding binding;

        KeywordViewHolder(ItemAdminKeywordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
