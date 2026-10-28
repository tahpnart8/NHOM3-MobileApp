package com.nhom3.pubgapp.ui.trade;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.UserRepository;
import com.nhom3.pubgapp.databinding.ItemTradeConversationBinding;
import com.nhom3.pubgapp.model.Conversation;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.ui.common.AvatarBinder;
import com.nhom3.pubgapp.util.FormatUtils;
import com.nhom3.pubgapp.util.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Adapter for conversation list items in C01.
public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    private static final String TAG = "ConversationAdapter";

    private final List<Conversation> conversations = new ArrayList<Conversation>();
    private final OnConversationClickListener listener;
    private final FormatUtils formatUtils = new FormatUtils();
    // users/{partnerId}, for the name and avatar, filled as rows are bound.
    private final Map<String, User> partners = new HashMap<String, User>();
    private final Set<String> requestedIds = new HashSet<String>();
    private final UserRepository userRepository = new UserRepository();

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
        Conversation conversation = conversations.get(position);
        String partnerId = partnerIdOf(conversation);
        holder.bind(conversation, partners.get(partnerId));
        loadPartner(partnerId);
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    @Nullable
    private static String partnerIdOf(Conversation conversation) {
        return conversation.otherParty(SessionManager.getInstance().getCurrentUserId());
    }

    // Each partner is read once per adapter; a failed read keeps the neutral name instead of retrying on every bind.
    private void loadPartner(@Nullable final String partnerId) {
        if (partnerId == null || partnerId.isEmpty() || requestedIds.contains(partnerId)) {
            return;
        }
        requestedIds.add(partnerId);
        userRepository.getProfile(partnerId, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    return;
                }
                partners.put(partnerId, user);
                refreshRowsOf(partnerId);
            }

            @Override
            public void onError(String message) {
                Log.w(TAG, "Partner profile not loaded: " + message);
            }
        });
    }

    private void refreshRowsOf(String partnerId) {
        for (int i = 0; i < conversations.size(); i++) {
            if (partnerId.equals(partnerIdOf(conversations.get(i)))) {
                notifyItemChanged(i);
            }
        }
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {

        private final ItemTradeConversationBinding binding;

        ConversationViewHolder(@NonNull ItemTradeConversationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Conversation conversation, @Nullable User partner) {
            Context context = itemView.getContext();
            String name = partner == null ? null : partner.getDisplayName();
            if (name == null || name.trim().isEmpty()) {
                name = context.getString(R.string.trade_conversation_list_partner_fallback);
            }
            binding.textPartnerName.setText(name);
            String avatarBase64 = partner == null ? null : partner.getAvatarBase64();
            AvatarBinder.bind(binding.includeAvatar, name, avatarBase64);

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
