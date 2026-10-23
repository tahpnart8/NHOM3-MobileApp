package com.nhom3.pubgapp.ui.trade;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.ItemTradeConversationBinding;
import com.nhom3.pubgapp.model.Conversation;
import com.nhom3.pubgapp.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

// Adapter for conversation list items in C01.
public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    private final List<Conversation> conversations = new ArrayList<Conversation>();
    private final OnConversationClickListener listener;
    private final FormatUtils formatUtils = new FormatUtils();

    public ConversationAdapter(OnConversationClickListener listener) {
        this.listener = listener;
    }

    public void setConversations(List<Conversation> items) {
        conversations.clear();
        if (items != null) {
            conversations.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemTradeConversationBinding binding = ItemTradeConversationBinding.inflate(inflater, parent, false);
        return new ConversationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.bind(conversations.get(position));
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {

        private final ItemTradeConversationBinding binding;

        ConversationViewHolder(@NonNull ItemTradeConversationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Conversation conversation) {
            binding.textPartnerName.setText(R.string.trade_conversation_list_partner_fallback);
            binding.includeAvatar.textInitial.setText("U");

            String lastMsg = conversation.getLastMessage();
            if (lastMsg != null && !lastMsg.isEmpty()) {
                binding.textLastMessage.setText(lastMsg);
            } else {
                binding.textLastMessage.setText("");
            }

            if (conversation.getLastMessageAt() != null) {
                binding.textLastTime.setText(formatUtils.formatTime(conversation.getLastMessageAt()));
            } else {
                binding.textLastTime.setText("");
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onConversationClick(conversation);
                    }
                }
            });
        }
    }
}
