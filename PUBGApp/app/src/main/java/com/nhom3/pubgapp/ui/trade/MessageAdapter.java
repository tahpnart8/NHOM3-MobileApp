package com.nhom3.pubgapp.ui.trade;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.MediaRepository;
import com.nhom3.pubgapp.databinding.ItemTradeMessageImageBinding;
import com.nhom3.pubgapp.databinding.ItemTradeMessageOfferBinding;
import com.nhom3.pubgapp.databinding.ItemTradeMessageTextBinding;
import com.nhom3.pubgapp.model.ChatMessage;
import com.nhom3.pubgapp.model.Offer;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.model.enums.MessageType;
import com.nhom3.pubgapp.model.enums.OfferStatus;
import com.nhom3.pubgapp.model.enums.OfferType;
import com.nhom3.pubgapp.ui.common.ImageLoader;
import com.nhom3.pubgapp.ui.common.StatusChips;
import com.nhom3.pubgapp.util.FormatUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Adapter for chat messages displaying text, image bubbles, and offer cards.
public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnMessageLongClickListener {
        void onMessageLongClick(ChatMessage message);
    }

    public interface OnOfferActionListener {
        void onOfferAccept(Offer offer);
        void onOfferReject(Offer offer);
    }

    private static final int VIEW_TYPE_TEXT = 1;
    private static final int VIEW_TYPE_IMAGE = 2;
    private static final int VIEW_TYPE_OFFER = 3;

    private final List<ChatMessage> messages = new ArrayList<ChatMessage>();
    private final Map<String, Offer> offerMap = new HashMap<String, Offer>();
    private final Map<String, DealStatus> dealStatusByOffer = new HashMap<String, DealStatus>();
    private final FormatUtils formatUtils = new FormatUtils();
    private final MediaRepository mediaRepository = new MediaRepository();
    private String currentUserId = "";
    private final OnMessageLongClickListener longClickListener;
    private final OnOfferActionListener offerActionListener;

    public MessageAdapter(OnMessageLongClickListener longClickListener) {
        this(longClickListener, null);
    }

    public MessageAdapter(OnMessageLongClickListener longClickListener,
                          OnOfferActionListener offerActionListener) {
        this.longClickListener = longClickListener;
        this.offerActionListener = offerActionListener;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId != null ? currentUserId : "";
        notifyDataSetChanged();
    }

    // Every snapshot sends the whole list. Only rows that really changed are rebound,
    // so image bubbles do not blink and decode again on each new message.
    public void setMessages(List<ChatMessage> items) {
        List<ChatMessage> newItems = items != null ? items : new ArrayList<ChatMessage>();
        if (!startsWithSameMessages(newItems)) {
            // An older page was added on top, or another chat opened: rebind everything once.
            messages.clear();
            messages.addAll(newItems);
            notifyDataSetChanged();
            return;
        }
        int oldSize = messages.size();
        for (int i = 0; i < oldSize; i++) {
            ChatMessage newMessage = newItems.get(i);
            boolean changed = !sameContent(messages.get(i), newMessage);
            messages.set(i, newMessage);
            if (changed) {
                notifyItemChanged(i);
            }
        }
        for (int i = oldSize; i < newItems.size(); i++) {
            messages.add(newItems.get(i));
        }
        if (newItems.size() > oldSize) {
            notifyItemRangeInserted(oldSize, newItems.size() - oldSize);
        }
    }

    // True when the new list keeps every current message at the same position and only adds at the end.
    private boolean startsWithSameMessages(List<ChatMessage> newItems) {
        if (newItems.size() < messages.size()) {
            return false;
        }
        for (int i = 0; i < messages.size(); i++) {
            if (!Objects.equals(messages.get(i).getId(), newItems.get(i).getId())) {
                return false;
            }
        }
        return true;
    }

    // The fields a bubble shows; the send time arrives later from the server, so it counts too.
    private boolean sameContent(ChatMessage oldMessage, ChatMessage newMessage) {
        return oldMessage.getType() == newMessage.getType()
                && Objects.equals(oldMessage.getSenderId(), newMessage.getSenderId())
                && Objects.equals(oldMessage.getText(), newMessage.getText())
                && Objects.equals(oldMessage.getImageId(), newMessage.getImageId())
                && Objects.equals(oldMessage.getOfferId(), newMessage.getOfferId())
                && Objects.equals(oldMessage.getCreatedAt(), newMessage.getCreatedAt());
    }

    // Offer and deal status only change offer cards, so text and image rows are left alone.
    private void notifyOfferRows() {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getType() == MessageType.OFFER) {
                notifyItemChanged(i);
            }
        }
    }

    public void setOffers(List<Offer> offerList) {
        offerMap.clear();
        if (offerList != null) {
            for (int i = 0; i < offerList.size(); i++) {
                Offer offer = offerList.get(i);
                if (offer != null && offer.getId() != null) {
                    offerMap.put(offer.getId(), offer);
                }
            }
        }
        notifyOfferRows();
    }

    public void setDealStatusByOffer(Map<String, DealStatus> map) {
        dealStatusByOffer.clear();
        if (map != null) {
            dealStatusByOffer.putAll(map);
        }
        notifyOfferRows();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messages.get(position);
        if (msg.getType() == MessageType.OFFER) {
            return VIEW_TYPE_OFFER;
        } else if (msg.getType() == MessageType.IMAGE) {
            return VIEW_TYPE_IMAGE;
        }
        return VIEW_TYPE_TEXT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_OFFER) {
            ItemTradeMessageOfferBinding binding = ItemTradeMessageOfferBinding.inflate(
                    inflater, parent, false);
            return new OfferMessageViewHolder(binding);
        } else if (viewType == VIEW_TYPE_IMAGE) {
            ItemTradeMessageImageBinding binding = ItemTradeMessageImageBinding.inflate(
                    inflater, parent, false);
            return new ImageMessageViewHolder(binding);
        }
        ItemTradeMessageTextBinding binding = ItemTradeMessageTextBinding.inflate(
                inflater, parent, false);
        return new TextMessageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        if (holder instanceof TextMessageViewHolder) {
            ((TextMessageViewHolder) holder).bind(msg);
        } else if (holder instanceof ImageMessageViewHolder) {
            ((ImageMessageViewHolder) holder).bind(msg);
        } else if (holder instanceof OfferMessageViewHolder) {
            ((OfferMessageViewHolder) holder).bind(msg);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class TextMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemTradeMessageTextBinding binding;

        TextMessageViewHolder(@NonNull ItemTradeMessageTextBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final ChatMessage message) {
            boolean isSent = currentUserId.equals(message.getSenderId());
            String time = formatUtils.formatTime(message.getCreatedAt());

            String displayText;
            if (message.getType() == MessageType.OFFER) {
                displayText = itemView.getContext().getString(R.string.trade_chat_offer_placeholder);
            } else {
                displayText = message.getText() != null ? message.getText() : "";
            }

            if (isSent) {
                binding.cardSent.setVisibility(View.VISIBLE);
                binding.cardReceived.setVisibility(View.GONE);
                binding.textMessageSent.setText(displayText);
                binding.textTimeSent.setText(time);
            } else {
                binding.cardSent.setVisibility(View.GONE);
                binding.cardReceived.setVisibility(View.VISIBLE);
                binding.textMessageReceived.setText(displayText);
                binding.textTimeReceived.setText(time);

                binding.cardReceived.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (longClickListener != null) {
                            longClickListener.onMessageLongClick(message);
                            return true;
                        }
                        return false;
                    }
                });
            }
        }
    }

    class ImageMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemTradeMessageImageBinding binding;

        ImageMessageViewHolder(@NonNull ItemTradeMessageImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final ChatMessage message) {
            boolean isSent = currentUserId.equals(message.getSenderId());
            String time = formatUtils.formatTime(message.getCreatedAt());

            if (isSent) {
                binding.cardImageSent.setVisibility(View.VISIBLE);
                binding.cardImageReceived.setVisibility(View.GONE);
                binding.textTimeImageSent.setText(time);
                loadImage(message.getImageId(), binding.imageSent);
            } else {
                binding.cardImageSent.setVisibility(View.GONE);
                binding.cardImageReceived.setVisibility(View.VISIBLE);
                binding.textTimeImageReceived.setText(time);
                loadImage(message.getImageId(), binding.imageReceived);

                binding.cardImageReceived.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (longClickListener != null) {
                            longClickListener.onMessageLongClick(message);
                            return true;
                        }
                        return false;
                    }
                });
            }
        }

        // The view's tag holds the image id it should show. A recycled view gets a new tag,
        // so a late answer for its old message is dropped instead of showing the wrong photo.
        private void loadImage(final String imageId, final ImageView imageView) {
            if (imageId == null || imageId.isEmpty()) {
                imageView.setTag(null);
                imageView.setImageDrawable(null);
                return;
            }
            if (imageId.equals(imageView.getTag())) {
                // Already showing or loading this image: rebinding the same row must not decode again.
                return;
            }
            imageView.setTag(imageId);
            imageView.setImageResource(R.drawable.bg_status_chip);
            mediaRepository.getImage(imageId, new DataCallback<byte[]>() {
                @Override
                public void onSuccess(final byte[] bytes) {
                    if (!imageId.equals(imageView.getTag())) {
                        return;
                    }
                    ImageLoader.load(
                            bytes, imageId, imageView,
                            R.drawable.bg_status_chip, R.drawable.bg_status_chip
                    );
                }

                @Override
                public void onError(String message) {
                    if (!imageId.equals(imageView.getTag())) {
                        return;
                    }
                    // Clear the tag so the next bind tries again.
                    imageView.setTag(null);
                    imageView.setImageDrawable(null);
                }
            });
        }
    }

    class OfferMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemTradeMessageOfferBinding binding;

        OfferMessageViewHolder(@NonNull ItemTradeMessageOfferBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final ChatMessage message) {
            String time = formatUtils.formatTime(message.getCreatedAt());
            binding.textOfferTime.setText(time);

            final Offer offer = message.getOfferId() != null
                    ? offerMap.get(message.getOfferId()) : null;
            if (offer != null) {
                bindOfferDetails(offer);
            } else {
                binding.textOfferTitle.setText(R.string.trade_chat_offer_title);
                binding.textOfferType.setText(message.getText() != null ? message.getText() : "");
                binding.textOfferAmount.setVisibility(View.GONE);
                binding.textOfferExchangeItem.setVisibility(View.GONE);
                binding.textOfferDealCancelled.setVisibility(View.GONE);
                binding.textOfferWaitingBuyer.setVisibility(View.GONE);
                binding.layoutSellerActions.setVisibility(View.GONE);
                StatusChips.bind(binding.chipOfferStatus.textStatusChip, OfferStatus.PENDING);
            }
        }

        private void bindOfferDetails(final Offer offer) {
            binding.textOfferTitle.setText(R.string.trade_chat_offer_title);

            OfferType type = offer.getOfferType();
            if (type == OfferType.MONEY) {
                binding.textOfferType.setText(R.string.trade_chat_offer_type_money);
            } else if (type == OfferType.ITEM) {
                binding.textOfferType.setText(R.string.trade_chat_offer_type_item);
            } else if (type == OfferType.MONEY_AND_ITEM) {
                binding.textOfferType.setText(R.string.trade_chat_offer_type_both);
            } else {
                binding.textOfferType.setText("");
            }

            if (offer.getAmount() > 0) {
                binding.textOfferAmount.setVisibility(View.VISIBLE);
                binding.textOfferAmount.setText(formatUtils.formatPrice(offer.getAmount()));
            } else {
                binding.textOfferAmount.setVisibility(View.GONE);
            }

            if (offer.getExchangeItem() != null && !offer.getExchangeItem().trim().isEmpty()) {
                binding.textOfferExchangeItem.setVisibility(View.VISIBLE);
                String itemText = String.format(
                        itemView.getContext().getString(R.string.trade_chat_offer_item_label),
                        offer.getExchangeItem().trim());
                binding.textOfferExchangeItem.setText(itemText);
            } else {
                binding.textOfferExchangeItem.setVisibility(View.GONE);
            }

            StatusChips.bind(binding.chipOfferStatus.textStatusChip, offer.getStatus());

            DealStatus dealStatus = offer.getId() != null
                    ? dealStatusByOffer.get(offer.getId()) : null;
            if (dealStatus == DealStatus.CANCELLED) {
                binding.textOfferDealCancelled.setVisibility(View.VISIBLE);
            } else {
                binding.textOfferDealCancelled.setVisibility(View.GONE);
            }

            OfferStatus status = offer.getStatus();
            boolean isSeller = currentUserId != null && currentUserId.equals(offer.getSellerId());
            if (isSeller) {
                binding.textOfferWaitingBuyer.setVisibility(View.GONE);
                if (status == OfferStatus.PENDING) {
                    binding.layoutSellerActions.setVisibility(View.VISIBLE);
                    binding.buttonOfferAccept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (offerActionListener != null) {
                                offerActionListener.onOfferAccept(offer);
                            }
                        }
                    });
                    binding.buttonOfferReject.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (offerActionListener != null) {
                                offerActionListener.onOfferReject(offer);
                            }
                        }
                    });
                } else {
                    binding.layoutSellerActions.setVisibility(View.GONE);
                }
            } else {
                binding.layoutSellerActions.setVisibility(View.GONE);
                if (status == OfferStatus.PENDING) {
                    binding.textOfferWaitingBuyer.setVisibility(View.VISIBLE);
                } else {
                    binding.textOfferWaitingBuyer.setVisibility(View.GONE);
                }
            }
        }
    }
}
