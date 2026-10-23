package com.nhom3.pubgapp.ui.trade;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.nhom3.pubgapp.model.enums.MessageType;
import com.nhom3.pubgapp.model.enums.OfferStatus;
import com.nhom3.pubgapp.model.enums.OfferType;
import com.nhom3.pubgapp.util.FormatUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Adapter for chat messages displaying text, image, and offer bubbles.
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
    private final FormatUtils formatUtils = new FormatUtils();
    private final MediaRepository mediaRepository = new MediaRepository();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private String currentUserId = "";
    private final OnMessageLongClickListener longClickListener;
    private OnOfferActionListener offerActionListener;

    public MessageAdapter(OnMessageLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void setOfferActionListener(OnOfferActionListener listener) {
        this.offerActionListener = listener;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId != null ? currentUserId : "";
        notifyDataSetChanged();
    }

    public void setMessages(List<ChatMessage> items) {
        messages.clear();
        if (items != null) {
            messages.addAll(items);
        }
        notifyDataSetChanged();
    }

    public void setOffers(List<Offer> items) {
        offerMap.clear();
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                Offer offer = items.get(i);
                if (offer != null && offer.getId() != null) {
                    offerMap.put(offer.getId(), offer);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messages.get(position);
        if (msg.getType() == MessageType.OFFER) {
            return VIEW_TYPE_OFFER;
        }
        if (msg.getType() == MessageType.IMAGE) {
            return VIEW_TYPE_IMAGE;
        }
        return VIEW_TYPE_TEXT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_OFFER) {
            ItemTradeMessageOfferBinding binding =
                    ItemTradeMessageOfferBinding.inflate(inflater, parent, false);
            return new OfferMessageViewHolder(binding);
        }
        if (viewType == VIEW_TYPE_IMAGE) {
            ItemTradeMessageImageBinding binding =
                    ItemTradeMessageImageBinding.inflate(inflater, parent, false);
            return new ImageMessageViewHolder(binding);
        }
        ItemTradeMessageTextBinding binding =
                ItemTradeMessageTextBinding.inflate(inflater, parent, false);
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

        private void loadImage(String imageId, final android.widget.ImageView imageView) {
            if (imageId == null || imageId.isEmpty()) {
                imageView.setImageDrawable(null);
                return;
            }
            mediaRepository.getImage(imageId, new DataCallback<byte[]>() {
                @Override
                public void onSuccess(final byte[] bytes) {
                    if (bytes != null && bytes.length > 0) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        imageView.setImageBitmap(bmp);
                                    }
                                });
                            }
                        }).start();
                    }
                }

                @Override
                public void onError(String message) {
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

            final Offer offer = message.getOfferId() != null ? offerMap.get(message.getOfferId()) : null;
            if (offer != null) {
                bindOfferDetails(offer);
            } else {
                binding.textOfferTitle.setText(R.string.trade_chat_offer_title);
                binding.textOfferType.setText(message.getText());
                binding.textOfferAmount.setVisibility(View.GONE);
                binding.textOfferExchangeItem.setVisibility(View.GONE);
                binding.textOfferWaitingBuyer.setVisibility(View.GONE);
                binding.layoutSellerActions.setVisibility(View.GONE);
                binding.textOfferStatus.setText(R.string.trade_chat_offer_status_pending);
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
                binding.textOfferAmount.setText(FormatUtils.formatPrice(offer.getAmount()));
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

            OfferStatus status = offer.getStatus();
            if (status == OfferStatus.ACCEPTED) {
                binding.textOfferStatus.setText(R.string.trade_chat_offer_status_accepted);
            } else if (status == OfferStatus.REJECTED) {
                binding.textOfferStatus.setText(R.string.trade_chat_offer_status_rejected);
            } else {
                binding.textOfferStatus.setText(R.string.trade_chat_offer_status_pending);
            }

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
